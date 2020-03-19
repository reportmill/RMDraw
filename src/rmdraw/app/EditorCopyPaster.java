package rmdraw.app;
import rmdraw.scene.*;
import snap.geom.Point;
import snap.geom.Rect;
import snap.text.RichText;
import snap.util.ListUtils;
import snap.util.XMLElement;
import snap.view.Clipboard;
import snap.view.ClipboardData;
import snap.view.ViewUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A CopyPaster implementation for Editor.
 */
public class EditorCopyPaster implements CopyPaster {

    // The Editor
    private Editor _editor;

    // The last shape that was copied to the clipboard (used for smart paste)
    private SGView _lastCopyShape;

    // The last shape that was pasted from the clipboard (used for smart paste)
    private SGView _lastPasteShape;

    // The MIME type for Draw archival format
    public static final String DRAW_XML_FORMAT = "snapdraw/xml";

    /**
     * Creates EditorCopyPaster for given editor.
     */
    public EditorCopyPaster(Editor anEditor)
    {
        _editor = anEditor;
    }

    /**
     * Returns the editor.
     */
    public Editor getEditor()  { return _editor; }

    /**
     * Handles editor cut operation.
     */
    public void cut()
    {
        _editor.copy();
        _editor.delete();
        _lastCopyShape = _lastPasteShape = null;
    }

    /**
     * Handles editor copy operation.
     */
    public void copy()
    {
        // If not text editing, add selected shapes (serialized) to pasteboard for DrawPboardType
        if(!(_editor.getSelOrSuperSelView() instanceof SGDoc) &&
                !(_editor.getSelOrSuperSelView() instanceof SGPage)) {

            // Get xml for selected shapes
            XMLElement xml = new RMArchiver().writeToXML(_editor.getSelOrSuperSelViews());
            String xmlStr = xml.toString();

            // Get System clipboard and add data as RMData and String (text/plain)
            Clipboard cb = Clipboard.getCleared();
            cb.addData(DRAW_XML_FORMAT, xmlStr);
            cb.addData(xmlStr);

            // Reset Editor.LastCopyShape/LastPasteShape
            _lastCopyShape = _editor.getSelView(0);
            _lastPasteShape = null;
        }

        // Otherwise beep
        else ViewUtils.beep();
    }

    /**
     * Handles editor paste operation.
     */
    public void paste()
    {
        // If not text editing, do paste for system clipboard
        SGParent parent = _editor.firstSuperSelectedShapeThatAcceptsChildren();
        paste(Clipboard.get(), parent, null);
    }

    /**
     * Handles editor paste operation for given transferable, parent shape and location.
     */
    public void paste(Clipboard aCB, SGParent aParent, Point aPoint)
    {
        // Declare variable for pasted shape
        SGView pastedShape = null;

        // If PasteBoard has ReportMill Data, paste it
        if(aCB.hasData(DRAW_XML_FORMAT)) {

            // Unarchive shapes from clipboard bytes
            Object object = getShapesFromClipboard(aCB);

            // If data is list of previously copied shapes, add them
            if(object instanceof List) {
                List shapes = (List)object;
                _editor.undoerSetUndoTitle("Paste Shape" + (shapes.size()>1? "s" : ""));
                addShapesToShape(shapes, aParent, true);
            }

            // If data is text, create text object and add it
            else if(object instanceof RichText) {
                SGText text = new SGText((RichText)object);
                double width = Math.min(text.getPrefWidth(), aParent.getWidth());
                double height = Math.min(text.getPrefHeight(), aParent.getHeight());
                text.setSize(width, height);
                _editor.undoerSetUndoTitle("Paste Text");
                addShapesToShape(Collections.singletonList(text), aParent, true);
            }

            // Promote _smartPastedShape to anchor and set new _smartPastedShape
            if(_lastPasteShape!=null)
                _lastCopyShape = _lastPasteShape;
            _lastPasteShape = _editor.getSelView(0);

        }

        // Paste Image
        else if(aCB.hasImage()) {
            ClipboardData idata = aCB.getImageData();
            byte bytes[] = idata.getBytes();
            pastedShape = new SGImage(bytes);
        }

        // last one - plain text
        else if((pastedShape=getTransferText(aCB)) != null) { }

        // Might as well log unsupported paste types
        else {
            //for(String type : aCB.getMIMETypes()) System.err.println("Unsupported type: " + type);
            ViewUtils.beep();
        }

        // Add pastedShape
        if(pastedShape!=null) {

            // Set undo title
            _editor.undoerSetUndoTitle("Paste");

            // Resize/relocate shape (if point was provided, move pasted shape to that point)
            pastedShape.setBestSize();
            if(aPoint!=null) {
                aPoint = _editor.convertToSceneView(aPoint.x, aPoint.y, aParent);
                pastedShape.setXY(aPoint.getX() - pastedShape.getWidth()/2, aPoint.getY() - pastedShape.getHeight()/2);
            }

            // Add pasted shape to parent
            aParent.addChild(pastedShape);

            // Select imageShape, set selectTool and redisplay
            _editor.setSelView(pastedShape);
            _editor.setCurrentToolToSelectTool();
            _editor.repaint();
        }
    }

    /**
     * Causes all the children of the current super selected shape to become selected.
     */
    public void selectAll()
    {
        // If document selected, select page
        SGView superSelShape = _editor.getSuperSelView();
        if(superSelShape instanceof SGDoc) {
            _editor.setSuperSelView(((SGDoc)superSelShape).getSelPage());
            superSelShape = _editor.getSuperSelView();
        }

        // Otherwise, select all children
        if(superSelShape.getChildCount()>0) {

            // Get list of all hittable children of super-selected shape
            List shapes = new ArrayList();
            for(SGView shape : superSelShape.getChildren())
                if(shape.isHittable())
                    shapes.add(shape);

            // Select shapes
            _editor.setSelViews(shapes);
        }
    }

    /**
     * Deletes all the currently selected shapes.
     */
    public void delete()
    {
        // Get copy of selected shapes (just beep and return if no selected shapes)
        SGView shapes[] = _editor.getSelViews().toArray(new SGView[0]);
        if (shapes.length==0) {
            ViewUtils.beep(); return; }

        // Get/superSelect parent of selected shapes
        SGParent parent = _editor.getSelView().getParent(); if (parent==null) return;
        _editor.setSuperSelView(parent);

        // Set undo title
        _editor.undoerSetUndoTitle(_editor.getSelViewCount()>1? "Delete Shapes" : "Delete Shape");

        // Remove all shapes from their parent
        for (SGView shape : shapes) {
            parent.removeChild(shape);
            if (_lastPasteShape==shape) _lastPasteShape = null;
            if (_lastCopyShape==shape) _lastCopyShape = null;
        }
    }

    /**
     * Returns the first Shape read from the system clipboard.
     */
    public SGView getShapeFromClipboard()
    {
        Object shapes = getShapesFromClipboard(null);
        if (shapes instanceof List)
            shapes = ListUtils.get((List)shapes, 0);
        return shapes instanceof SGView ? (SGView)shapes : null;
    }

    /**
     * Returns the shape or shapes read from the given transferable (uses system clipboard if null).
     */
    private Object getShapesFromClipboard(Clipboard aCB)
    {
        // If no contents, use system clipboard
        Clipboard cboard = aCB!=null? aCB : Clipboard.get();

        // If no RMData, just return
        if(!cboard.hasData(DRAW_XML_FORMAT))
            return null;

        // Get unarchived object from clipboard bytes
        byte bytes[] = cboard.getDataBytes(DRAW_XML_FORMAT);
        Object obj = new RMArchiver().readFromXMLBytes(bytes);

        // A bit of a hack - remove any non-shapes (plugins for one)
        if(obj instanceof List) { List list = (List)obj;
            for(int i=list.size()-1; i>=0; --i)
                if(!(list.get(i) instanceof SGView))
                    list.remove(i);
        }

        // Return object
        return obj;
    }

    /**
     * Adds shapes as children to given shape.
     */
    private void addShapesToShape(List <SGView> theShapes, SGParent aShape, boolean withCorrection)
    {
        // If no shapes, just return
        if(theShapes.size()==0) return;

        // Declare variables for dx, dy, dr
        double dx = 0, dy = 0, dr = 0;

        // Smart paste
        if (withCorrection) {

            // If there is an last-copy-shape and new shapes will be it's peer, set offset
            if (_lastCopyShape!=null && _lastCopyShape.getParent()==aShape) {

                if (_lastPasteShape!=null) {
                    SGView firstShape = theShapes.get(0);
                    dx = 2*_lastPasteShape.x() - _lastCopyShape.x() - firstShape.x();
                    dy = 2*_lastPasteShape.y() - _lastCopyShape.y() - firstShape.y();
                    dr = 2*_lastPasteShape.getRoll() - _lastCopyShape.getRoll() - firstShape.getRoll();
                }

                else dx = dy = _editor.getDoc().getGridSpacing();
            }
        }

        // Get each individual shape and add it to the superSelectedShape
        for (SGView shape : theShapes) {

            // Add current loop shape to given parent shape
            aShape.addChild(shape);

            // Smart paste
            if (withCorrection) {
                Rect parentShapeRect = aShape.getBoundsLocal();
                shape.setXY(shape.x() + dx, shape.y() + dy);
                shape.setRoll(shape.getRoll() + dr);
                Rect rect = shape.getFrame();
                rect.width = Math.max(1, rect.width);
                rect.height = Math.max(1, rect.height);
                if (!parentShapeRect.intersectsRect(rect))
                    shape.setXY(0, 0);
            }
        }

        // Select shapes
        _editor.setSelViews(theShapes);
    }

    /**
     * Returns Text with contents if there's a plain text string on clipboard.
     */
    private static SGView getTransferText(Clipboard aCB)
    {
        if(!aCB.hasString()) return null;
        String str = aCB.getString();
        return str!=null? new SGText(str) : null;
    }
}

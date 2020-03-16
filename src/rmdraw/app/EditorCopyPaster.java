package rmdraw.app;
import rmdraw.shape.*;
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
    private RMShape _lastCopyShape;

    // The last shape that was pasted from the clipboard (used for smart paste)
    private RMShape _lastPasteShape;

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
        if(!(_editor.getSelectedOrSuperSelectedShape() instanceof RMDocument) &&
                !(_editor.getSelectedOrSuperSelectedShape() instanceof RMPage)) {

            // Get xml for selected shapes
            XMLElement xml = new RMArchiver().writeToXML(_editor.getSelectedOrSuperSelectedShapes());
            String xmlStr = xml.toString();

            // Get System clipboard and add data as RMData and String (text/plain)
            Clipboard cb = Clipboard.getCleared();
            cb.addData(DRAW_XML_FORMAT, xmlStr);
            cb.addData(xmlStr);

            // Reset Editor.LastCopyShape/LastPasteShape
            _lastCopyShape = _editor.getSelectedShape(0);
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
        RMParentShape parent = _editor.firstSuperSelectedShapeThatAcceptsChildren();
        paste(Clipboard.get(), parent, null);
    }

    /**
     * Handles editor paste operation for given transferable, parent shape and location.
     */
    public void paste(Clipboard aCB, RMParentShape aParent, Point aPoint)
    {
        // Declare variable for pasted shape
        RMShape pastedShape = null;

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
                RMTextShape text = new RMTextShape((RichText)object);
                double width = Math.min(text.getPrefWidth(), aParent.getWidth());
                double height = Math.min(text.getPrefHeight(), aParent.getHeight());
                text.setSize(width, height);
                _editor.undoerSetUndoTitle("Paste Text");
                addShapesToShape(Collections.singletonList(text), aParent, true);
            }

            // Promote _smartPastedShape to anchor and set new _smartPastedShape
            if(_lastPasteShape!=null)
                _lastCopyShape = _lastPasteShape;
            _lastPasteShape = _editor.getSelectedShape(0);

        }

        // Paste Image
        else if(aCB.hasImage()) {
            ClipboardData idata = aCB.getImageData();
            byte bytes[] = idata.getBytes();
            pastedShape = new RMImageShape(bytes);
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
                aPoint = _editor.convertToShape(aPoint.x, aPoint.y, aParent);
                pastedShape.setXY(aPoint.getX() - pastedShape.getWidth()/2, aPoint.getY() - pastedShape.getHeight()/2);
            }

            // Add pasted shape to parent
            aParent.addChild(pastedShape);

            // Select imageShape, set selectTool and redisplay
            _editor.setSelectedShape(pastedShape);
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
        RMShape superSelShape = _editor.getSuperSelectedShape();
        if(superSelShape instanceof RMDocument) {
            _editor.setSuperSelectedShape(((RMDocument)superSelShape).getSelPage());
            superSelShape = _editor.getSuperSelectedShape();
        }

        // Otherwise, select all children
        if(superSelShape.getChildCount()>0) {

            // Get list of all hittable children of super-selected shape
            List shapes = new ArrayList();
            for(RMShape shape : superSelShape.getChildren())
                if(shape.isHittable())
                    shapes.add(shape);

            // Select shapes
            _editor.setSelectedShapes(shapes);
        }
    }

    /**
     * Deletes all the currently selected shapes.
     */
    public void delete()
    {
        // Get copy of selected shapes (just beep and return if no selected shapes)
        RMShape shapes[] = _editor.getSelectedShapes().toArray(new RMShape[0]);
        if (shapes.length==0) {
            ViewUtils.beep(); return; }

        // Get/superSelect parent of selected shapes
        RMParentShape parent = _editor.getSelectedShape().getParent(); if (parent==null) return;
        _editor.setSuperSelectedShape(parent);

        // Set undo title
        _editor.undoerSetUndoTitle(_editor.getSelectedShapeCount()>1? "Delete Shapes" : "Delete Shape");

        // Remove all shapes from their parent
        for (RMShape shape : shapes) {
            parent.removeChild(shape);
            if (_lastPasteShape==shape) _lastPasteShape = null;
            if (_lastCopyShape==shape) _lastCopyShape = null;
        }
    }

    /**
     * Returns the first Shape read from the system clipboard.
     */
    public RMShape getShapeFromClipboard()
    {
        Object shapes = getShapesFromClipboard(null);
        if (shapes instanceof List)
            shapes = ListUtils.get((List)shapes, 0);
        return shapes instanceof RMShape ? (RMShape)shapes : null;
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
                if(!(list.get(i) instanceof RMShape))
                    list.remove(i);
        }

        // Return object
        return obj;
    }

    /**
     * Adds shapes as children to given shape.
     */
    private void addShapesToShape(List <RMShape> theShapes, RMParentShape aShape, boolean withCorrection)
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
                    RMShape firstShape = theShapes.get(0);
                    dx = 2*_lastPasteShape.x() - _lastCopyShape.x() - firstShape.x();
                    dy = 2*_lastPasteShape.y() - _lastCopyShape.y() - firstShape.y();
                    dr = 2*_lastPasteShape.getRoll() - _lastCopyShape.getRoll() - firstShape.getRoll();
                }

                else dx = dy = _editor.getDoc().getGridSpacing();
            }
        }

        // Get each individual shape and add it to the superSelectedShape
        for (RMShape shape : theShapes) {

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
        _editor.setSelectedShapes(theShapes);
    }

    /**
     * Returns Text with contents if there's a plain text string on clipboard.
     */
    private static RMShape getTransferText(Clipboard aCB)
    {
        if(!aCB.hasString()) return null;
        String str = aCB.getString();
        return str!=null? new RMTextShape(str) : null;
    }
}

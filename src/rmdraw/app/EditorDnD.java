/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.app;
import rmdraw.shape.*;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.*;
import snap.view.*;
import snap.viewx.DialogBox;
import java.util.List;

/**
 * Handles editor methods specific to drag and drop operations.
 */
public class EditorDnD {
    
    // The editor that this class is working for
    Editor _editor;
    
    // The last shape that a drag and drop action was over
    RMShape         _lastOverShape;

    /**
     * Creates a new editor drop target listener.
     */
    public EditorDnD(Editor anEditor)  { _editor = anEditor; }

    /**
     * Returns the editor.
     */
    public Editor getEditor()  { return _editor; }

    /**
     * Handle events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        switch(anEvent.getType()) {
            case DragEnter: dragEnter(anEvent); break;
            case DragOver: dragOver(anEvent); break;
            case DragExit: dragExit(anEvent); break;
            case DragDrop: dragDrop(anEvent); break;
            default: throw new RuntimeException("EditorDnD: Unknown event type: " + anEvent.getType());
            //case DragActionChanged: anEvent.acceptDrag(DnDConstants.ACTION_COPY);
        }
    }

    /**
     * Drop target listener method.
     */
    public void dragEnter(ViewEvent anEvent)
    {
        _lastOverShape = null;  // Reset last over shape and last drag point
        dragOver(anEvent);                             // Do a drag over to get things started
    }

    /**
     * Drop target listener method.
     */
    public void dragOver(ViewEvent anEvent)
    {
        // Windows calls this method continuously, as long as the mouse is held down
        //if(anEvent.getPoint().equals(_lastDragPoint)) return; _lastDragPoint = anEvent.getPoint();

        // Accept drag
        anEvent.acceptDrag(); //DnDConstants.ACTION_COPY);

        // Get shape at drag point (or the page, if none there)
        RMShape overShape = _editor.getShapeAtPoint(anEvent.getPoint(), true);
        if(overShape==null)
            overShape = _editor.getSelPage();

        // Go up chain until we find a shape that accepts drag
        while(!_editor.getTool(overShape).acceptsDrag(overShape, anEvent))
            overShape = overShape.getParent();

        // If new overShape, do drag exit/enter and reset border
        if(overShape!=_lastOverShape) {

            // Send drag exit
            if(_lastOverShape!=null)
                _editor.getTool(_lastOverShape).dragExit(_lastOverShape, anEvent);

            // Send drag enter
            _editor.getTool(overShape).dragEnter(overShape, anEvent);

            // Get bounds of over shape in editor coords
            Rect bounds = overShape.getBoundsInside();
            _editor._dragShape = _editor.convertFromShape(bounds, overShape);
            _editor.repaint();

            // Update last drop shape
            _lastOverShape = overShape;
        }

        // If over shape didn't change, send drag over
        else _editor.getTool(overShape).dragOver(overShape, anEvent);
    }

    /**
     * Drop target listener method.
     */
    public void dragExit(ViewEvent anEvent)
    {
        _editor._dragShape = null; _editor.repaint();        // Clear DragShape
        EditorProxGuide.clearGuidelines(_editor);          // Reset proximity guide
    }

    /**
     * Drop target listener method.
     */
    protected void dragDrop(ViewEvent anEvent)
    {
        // Formally accept drop
        anEvent.acceptDrag();//DnDConstants.ACTION_COPY);

        // Order window front (for any getMainEditor calls, but really should be true anyway)
        _editor.getWindow().toFront();

        // Forward drop to last over shape
        _editor.getTool(_lastOverShape).drop(_lastOverShape, anEvent);

        // Formally complete drop
        anEvent.dropComplete();  //(true);

        // Clear DragShape (which may have been set during dragOver)
        _editor._dragShape = null; _editor.repaint();
    }

    /**
     * Called to drop string.
     */
    public void dropForView(RMShape aView, ViewEvent anEvent)
    {
        // Handle String drop
        Clipboard cb = anEvent.getClipboard();
        if(cb.hasString())
            dropStringForView(aView, anEvent);

        // Handle color panel drop
        else if(cb.hasColor())
            dropColorForView(aView, anEvent);

        // Handle File drop - get list of dropped files and add individually
        else if(cb.hasFiles())
            dropFilesForView(aView, anEvent);
    }

    /**
     * Called to handle dropping a string.
     */
    public void dropStringForView(RMShape aShape, ViewEvent anEvent)
    {
        if(aShape instanceof RMParentShape)
        {
            Editor editor = getEditor();
            RMParentShape par = (RMParentShape)aShape;
            Clipboard cb = anEvent.getClipboard(); //Transferable transferable = anEvent.getTransferable();
            editor.undoerSetUndoTitle("Drag and Drop Key");
            EditorClipboard.paste(editor, cb, par, anEvent.getPoint());
        }
    }

    /**
     * Called to handle dropping a color.
     */
    public void dropColorForView(RMShape aShape, ViewEvent anEvent)
    {
        Color color = anEvent.getClipboard().getColor();
        getEditor().undoerSetUndoTitle("Set Fill Color");
        aShape.setFill(color);
    }

    /**
     * Called to handle dropping a file.
     */
    public void dropFilesForView(RMShape aShape, ViewEvent anEvent)
    {
        List<ClipboardData> filesList = anEvent.getClipboard().getFiles();
        for(ClipboardData file : filesList)
            dropFileForView(aShape, file, anEvent.getPoint());
    }

    /**
     * Called to handle a file drop on the editor.
     */
    private Point dropFileForView(RMShape aShape, ClipboardData aFile, Point aPoint)
    {
        // If file not loaded, come back when it is
        if(!aFile.isLoaded()) { aFile.addLoadListener(f -> dropFileForView(aShape, aFile, aPoint)); return aPoint; }

        // Get path and extension (set to empty string if null)
        String ext = aFile.getExtension(); if(ext==null) return aPoint;
        ext = ext.toLowerCase();

        // If xml file, pass it to setDataSource()
        if(ext.equals("xml"))
            dropXMLFile(aFile, aPoint);

        // If image file, add image shape
        else if(Image.canRead(ext))
            dropImageFile(aShape, aFile, aPoint);

        // If PDF file, add image shape
        else if(ext.equals("pdf"))
            dropPDFFile(aShape, aFile, aPoint);

        // Return point offset by 10
        aPoint.offset(10, 10); return aPoint;
    }

    /**
     * Called to handle drop XML file.
     */
    protected void dropXMLFile(ClipboardData aFile, Point aPoint)
    {
        System.err.println("EditorDnD.dropXMLFile: Not implemented");
    }

    /**
     * Called to handle an image drop on the editor.
     */
    private void dropImageFile(RMShape aShape, ClipboardData aFile, Point aPoint)
    {
        // Get image source
        Object imgSrc = aFile.getSourceURL()!=null? aFile.getSourceURL() : aFile.getBytes();

        // If image hit a real shape, see if user wants it to be a texture
        Editor editor = getEditor();
        if(aShape!=editor.getSelPage()) {

            // Create drop image file options array
            String options[] = { "Image Shape", "Texture", "Cancel" };

            // Run drop image file options panel
            String msg = "Image can be either image shape or texture", title = "Image import";
            DialogBox dbox = new DialogBox(title); dbox.setQuestionMessage(msg); dbox.setOptions(options);
            switch(dbox.showOptionDialog(null, options[0])) {

                // Handle Create Image Shape
                case 0:
                    while(!editor.getTool(aShape).getAcceptsChildren(aShape))
                        aShape = aShape.getParent();
                    break;

                // Handle Create Texture
                case 1: {
                    Image img = Image.get(imgSrc);
                    ImagePaint imgFill = img!=null ? new ImagePaint(img) : null;
                    aShape.setFill(imgFill);
                }

                    // Handle Cancel
                case 2: return;
            }
        }

        // Get parent to add image shape to and drop point in parent coords
        RMParentShape parent = aShape instanceof RMParentShape? (RMParentShape)aShape : aShape.getParent();
        Point point = editor.convertToShape(aPoint.x, aPoint.y, parent);

        // Create new image shape
        RMImageShape imgShape = new RMImageShape(imgSrc);

        // If image is bigger than hit shape, shrink down
        if(imgShape.getWidth()>parent.getWidth() || imgShape.getHeight()>parent.getHeight()) {
            double w = imgShape.getWidth(), h = imgShape.getHeight();
            double w2 = w>h? 320 : 320/h*w, h2 = h>w? 320 : 320/w*h;
            imgShape.setSize(w2, h2);
        }

        // Set bounds centered around point (or centered on page if image covers 75% of page or more)
        imgShape.setXY(point.x - imgShape.getWidth()/2, point.y - imgShape.getHeight()/2);
        if(imgShape.getWidth()/editor.getWidth()>.75f || imgShape.getHeight()/editor.getHeight()>.75) imgShape.setXY(0, 0);

        // Add imageShape with undo
        editor.undoerSetUndoTitle("Add Image");
        parent.addChild(imgShape);

        // Select imageShape and SelectTool
        editor.setSelectedShape(imgShape);
        editor.setCurrentToolToSelectTool();

        // If image not loaded, resize when loaded
        Image img = imgShape.getImage();
        if(!img.isLoaded()) {
            img.addLoadListener(() -> {
                double dw = img.getWidth() - imgShape.getWidth(), dh = img.getHeight() - imgShape.getHeight();
                Rect bnds = imgShape.getBounds(); bnds.inset(-dw/2, -dh/2); bnds.snap();
                imgShape.setBounds(bnds);
            });
        }
    }

    /**
     * Called to handle an PDF file drop on the editor.
     */
    protected void dropPDFFile(RMShape aShape, ClipboardData aFile, Point aPoint)
    {
        ViewUtils.beep();
    }

}
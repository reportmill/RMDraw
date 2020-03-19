/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.app;
import rmdraw.scene.*;
import snap.geom.Point;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.gfx.*;
import snap.view.*;
import snap.viewx.DialogBox;
import java.util.List;

/**
 * Handles editor methods specific to drag and drop operations.
 */
public class EditorDragDropper implements DragDropper {
    
    // The editor that this class is working for
    private Editor  _editor;

    // A view to be drawn if set to drag-over view during drag and drop
    private Shape _dragShape;

    // The last view that a drag and drop action was over
    private SGView _lastOverView;

    /**
     * Creates a new editor drop target listener.
     */
    public EditorDragDropper(Editor anEditor)  { _editor = anEditor; }

    /**
     * Returns the editor.
     */
    public Editor getEditor()  { return _editor; }

    /**
     * The shape of view that drag/drop is currently over.
     */
    public Shape getDragShape()  { return _dragShape; }

    /**
     * Implemented by views that can handle drag & drop.
     */
    public boolean acceptsDrag(SGView aView, ViewEvent anEvent)
    {
        // If Tool acceptsDrag, return true
        Tool tool = getTool(aView);
        if (tool.acceptsDrag(aView, anEvent))
            return true;

        // Bogus, but currently the page accepts everything
        if (aView instanceof SGPage)
            return true;

        // Return true for Color drag or File drag
        if (anEvent.getClipboard().hasColor())
            return true;

        // Handle file drag - really just want to check for images here, but can't ask for transferable contents yet
        if (anEvent.getClipboard().hasFiles())
            return true;

        // Return true in any case if accepts children
        return tool.getAcceptsChildren(aView);
    }

    /**
     * Drop target listener method.
     */
    public void dragEnter(ViewEvent anEvent)
    {
        _lastOverView = null;  // Reset last over view and last drag point
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

        // Get view at drag point (or the page, if none there)
        SGView overView = _editor.getViewAtPoint(anEvent.getPoint(), true);
        if (overView==null)
            overView = _editor.getSelPage();

        // Go up chain until we find a view that accepts drag
        while (!acceptsDrag(overView, anEvent))
            overView = overView.getParent();

        // If new overView, do drag exit/enter and reset border
        if (overView!= _lastOverView) {

            // Send drag exit
            if(_lastOverView !=null)
                getTool(_lastOverView).dragExit(_lastOverView, anEvent);

            // Send drag enter
            getTool(overView).dragEnter(overView, anEvent);

            // Get bounds of over view in editor coords
            Rect bounds = overView.getBoundsLocal();
            _dragShape = _editor.convertFromSceneView(bounds, overView);
            _editor.repaint();

            // Update last drop view
            _lastOverView = overView;
        }

        // If same OverView, send dragOver
        else if (overView!=null)
            getTool(overView).dragOver(overView, anEvent);
    }

    /**
     * Drop target listener method.
     */
    public void dragExit(ViewEvent anEvent)
    {
        // Repaint editor and clear guidelines and DragShape
        _editor.repaint();
        EditorProxGuide.clearGuidelines(_editor);
        _dragShape = null;
    }

    /**
     * Drop target listener method.
     */
    public void dragDrop(ViewEvent anEvent)
    {
        // Formally accept drop
        anEvent.acceptDrag(); //DnDConstants.ACTION_COPY);

        // Order window front (for any getMainEditor calls, but really should be true anyway)
        _editor.getWindow().toFront();

        // Forward drop to last over view
        Tool tool = getTool(_lastOverView);
        tool.dragDrop(_lastOverView, anEvent);

        // Formally complete drop
        anEvent.dropComplete();  //(true);

        // Clear DragShape (which may have been set during dragOver)
        _editor.repaint();
        _dragShape = null;
    }

    /**
     * Called to drop string.
     */
    public void dropForView(SGView aView, ViewEvent anEvent)
    {
        // Handle String drop
        Clipboard cb = anEvent.getClipboard();
        if (cb.hasString())
            dropStringForView(aView, anEvent);

        // Handle color panel drop
        else if (cb.hasColor())
            dropColorForView(aView, anEvent);

        // Handle File drop - get list of dropped files and add individually
        else if (cb.hasFiles())
            dropFilesForView(aView, anEvent);
    }

    /**
     * Called to handle dropping a string.
     */
    public void dropStringForView(SGView aView, ViewEvent anEvent)
    {
        if (aView instanceof SGParent)
        {
            Editor editor = getEditor();
            SGParent par = (SGParent)aView;
            Clipboard cb = anEvent.getClipboard(); //Transferable transferable = anEvent.getTransferable();
            editor.undoerSetUndoTitle("Drag and Drop Key");
            editor.getCopyPasterDefault().paste(cb, par, anEvent.getPoint());
        }
    }

    /**
     * Called to handle dropping a color.
     */
    public void dropColorForView(SGView aView, ViewEvent anEvent)
    {
        Color color = anEvent.getClipboard().getColor();
        getEditor().undoerSetUndoTitle("Set Fill Color");
        aView.setFill(color);
    }

    /**
     * Called to handle dropping a file.
     */
    public void dropFilesForView(SGView aView, ViewEvent anEvent)
    {
        List<ClipboardData> filesList = anEvent.getClipboard().getFiles();
        for(ClipboardData file : filesList)
            dropFileForView(aView, file, anEvent.getPoint());
    }

    /**
     * Called to handle a file drop on the editor.
     */
    private Point dropFileForView(SGView aView, ClipboardData aFile, Point aPoint)
    {
        // If file not loaded, come back when it is
        if (!aFile.isLoaded()) {
            aFile.addLoadListener(f -> dropFileForView(aView, aFile, aPoint));
            return aPoint;
        }

        // Get path and extension (set to empty string if null)
        String ext = aFile.getExtension(); if(ext==null) return aPoint;
        ext = ext.toLowerCase();

        // If xml file, pass it to setDataSource()
        if (ext.equals("xml"))
            dropXMLFile(aFile, aPoint);

        // If image file, add image view
        else if (Image.canRead(ext))
            dropImageFile(aView, aFile, aPoint);

        // If PDF file, add image view
        else if (ext.equals("pdf"))
            dropPDFFile(aView, aFile, aPoint);

        // Return point offset by 10
        aPoint.offset(10, 10); return aPoint;
    }

    /**
     * Called to handle drop XML file.
     */
    protected void dropXMLFile(ClipboardData aFile, Point aPoint)
    {
        System.err.println("EditorDragDropper.dropXMLFile: Not implemented");
    }

    /**
     * Called to handle an image drop on the editor.
     */
    private void dropImageFile(SGView aView, ClipboardData aFile, Point aPoint)
    {
        // Get image source
        Object imgSrc = aFile.getSourceURL()!=null? aFile.getSourceURL() : aFile.getBytes();

        // If image hit a real view, see if user wants it to be a texture
        Editor editor = getEditor();
        if (aView!=editor.getSelPage()) {

            // Create drop image file options array
            String options[] = { "Image View", "Texture", "Cancel" };

            // Run drop image file options panel
            String msg = "Image can be either image view or texture", title = "Image import";
            DialogBox dbox = new DialogBox(title); dbox.setQuestionMessage(msg); dbox.setOptions(options);
            switch (dbox.showOptionDialog(null, options[0])) {

                // Handle Create Image view
                case 0:
                    while(!getTool(aView).getAcceptsChildren(aView))
                        aView = aView.getParent();
                    break;

                // Handle Create Texture
                case 1: {
                    Image img = Image.get(imgSrc);
                    ImagePaint imgFill = img!=null ? new ImagePaint(img) : null;
                    aView.setFill(imgFill);
                }

                    // Handle Cancel
                case 2: return;
            }
        }

        // Get parent to add image view to and drop point in parent coords
        SGParent parent = aView instanceof SGParent ? (SGParent)aView : aView.getParent();
        Point point = editor.convertToSceneView(aPoint.x, aPoint.y, parent);

        // Create new image view
        SGImage imgView = new SGImage(imgSrc);

        // If image is bigger than hit view, shrink down
        if (imgView.getWidth()>parent.getWidth() || imgView.getHeight()>parent.getHeight()) {
            double w = imgView.getWidth(), h = imgView.getHeight();
            double w2 = w>h? 320 : 320/h*w, h2 = h>w? 320 : 320/w*h;
            imgView.setSize(w2, h2);
        }

        // Set bounds centered around point (or centered on page if image covers 75% of page or more)
        imgView.setXY(point.x - imgView.getWidth()/2, point.y - imgView.getHeight()/2);
        if(imgView.getWidth()/editor.getWidth()>.75f || imgView.getHeight()/editor.getHeight()>.75) imgView.setXY(0, 0);

        // Add imageView with undo
        editor.undoerSetUndoTitle("Add Image");
        parent.addChild(imgView);

        // Select imageView and SelectTool
        editor.setSelView(imgView);
        editor.setCurrentToolToSelectTool();

        // If image not loaded, resize when loaded
        Image img = imgView.getImage();
        if (!img.isLoaded())
            img.addLoadListener(() -> repositionDroppedImage(img, imgView));
    }

    /**
     * Called when dropped image is finished loading to reposition now that we know image size.
     */
    private void repositionDroppedImage(Image img, SGImage imgView)
    {
        double dw = img.getWidth() - imgView.getWidth();
        double dh = img.getHeight() - imgView.getHeight();
        Rect bnds = imgView.getBounds();
        bnds.inset(-dw/2, -dh/2); bnds.snap();
        imgView.setBounds(bnds);
    }

    /**
     * Called to handle an PDF file drop on the editor.
     */
    protected void dropPDFFile(SGView aView, ClipboardData aFile, Point aPoint)
    {
        ViewUtils.beep();
    }

    /**
     * Helper.
     */
    private Tool getTool(SGView aView)  { return _editor.getToolForView(aView); }

}
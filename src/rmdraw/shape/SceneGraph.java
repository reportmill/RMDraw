/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.shape;
import rmdraw.app.Editor;
import snap.gfx.*;
import snap.util.PropChange;
import snap.util.PropChangeListener;
import snap.util.Undoer;

/**
 * The root of a hierarchy of views.
 */
public class SceneGraph extends RMParentShape {

    // The object using this SceneGraph
    private Client _client;

    // The document being viewed
    private RMDocument  _doc;
    
    // A PropChangeListener to catch doc changes (Showing, PageSize, )
    private PropChangeListener _docLsnr = pc -> docDidPropChange(pc);
    
    // An optional undoer object to track document changes
    private Undoer _undoer;

    // Whether SceneGraph is currently painting
    private boolean  _ptg;

    /**
     * Creates a SceneGraph for given SceneGraph.Client.
     */
    public SceneGraph(Client aClient)
    {
        // Set Viewer
        _client = aClient;

        // If Viewer is really editor, do more
        if (_client instanceof Editor) {
            Editor editor = (Editor) _client;
            addDeepChangeListener(editor);
        }
    }

    /**
     * Returns the document.
     */
    public RMDocument getDoc()  { return _doc; }

    /**
     * Sets the document to be viewed in viewer.
     */
    public void setDoc(RMDocument aDoc)
    {
        // Resolve page references on document and make sure it has a selected page
        aDoc.resolvePageReferences();
        aDoc.layoutDeep();

        // If old document, stop listening to shape changes and notify shapes hidden
        if (_doc!=null) {
            _doc.removePropChangeListener(_docLsnr);
            _doc.setSceneGraph(null);
        }

        // Set new document
        if (_doc!=null) removeChild(_doc);
        addChild(_doc = aDoc, 0);

        // Start listening to shape changes and notify shapes shown
        _doc.setSceneGraph(this);
        _doc.addPropChangeListener(_docLsnr);

        // If working for editor, do more
        if (_client instanceof Editor) { Editor editor = (Editor) _client;

            // Make sure current document page is super-selected
            RMPage page = getDoc().getSelPage();
            editor.setSuperSelectedShape(page);

            // Create and install undoer
            if (editor.isEditing())
                _undoer = new Undoer();
        }
    }

    /**
     * Returns the undoer.
     */
    public Undoer getUndoer()  { return _undoer; }

    /**
     * Override to return content preferred width.
     */
    protected double getPrefWidthImpl(double aHeight)
    {
        RMDocument d = getDoc(); return d!=null? d.getPrefWidth() : 0;
    }

    /**
     * Override to return content preferred height.
     */
    protected double getPrefHeightImpl(double aWidth)
    {
        RMDocument d = getDoc(); return d!=null? d.getPrefHeight() : 0;
    }

    /**
     * Override to notify viewer.
     */
    protected void setNeedsLayoutDeep(boolean aVal)
    {
        super.setNeedsLayoutDeep(aVal);
        if (aVal)
            _client.sceneNeedsRelayout();
    }

    /**
     * Lays out children deep.
     */
    public void layoutDeep()
    {
        undoerDisable();
        super.layoutDeep();
        undoerEnable();
    }

    /**
     * Override to layout doc.
     */
    protected void layoutImpl()
    {
        // Get Doc, parent Width/Height and doc bounds in center of SceneGraph
        RMDocument doc = getDoc();
        double pw = getWidth(), ph = getHeight();
        double dw = doc.getPrefWidth();
        double dh = doc.getPrefHeight();
        double dx = pw>dw? Math.floor((pw-dw)/2) : 0;
        double dy = ph>dh? Math.floor((ph-dh)/2) : 0;

        // Set doc location and scale for zoom factor
        doc.setBounds(dx,dy,dw,dh);
        if(doc.getScaleX()!= _client.getSceneZoomFactor()) {
            double sc = _client.getSceneZoomFactor();
            doc.setScaleXY(sc, sc);
        }
    }

    /**
     * This is a notification call for impending visual shape attribute changes.
     */
    protected void repaint(RMShape aShape)
    {
        // If painting, complain that someone is calling a repaint during painting
        if (_ptg) // Should never happen, but good to check
            System.err.println("SceneGraph.repaint(): called during painting");

        // Forward to viewer
        _client.sceneNeedsRepaint(aShape);
    }

    /**
     * Override to set Painting flag.
     */
    public void paint(Painter aPntr)
    {
        _ptg = true; super.paint(aPntr); _ptg = false;
    }

    /**
     * Called when document has a prop change.
     */
    private void docDidPropChange(PropChange aPC)
    {
        _client.sceneDocDidPropChange(aPC);
    }

    /**
     * An interface for objects that want to use a SceneGraph.
     */
    public interface Client {

        /** Called when SceneGraph Doc has prop change. */
        void sceneDocDidPropChange(PropChange aPC);

        /** Called when SceneGraph view needs repaint. */
        void sceneNeedsRepaint(RMShape aShape);

        /** Called when SceneGraph needs relayout. */
        void sceneNeedsRelayout();

        /** Called to get ZoomFactor. */
        double getSceneZoomFactor();
    }
}
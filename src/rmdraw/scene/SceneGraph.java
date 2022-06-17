/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.scene;
import snap.gfx.*;
import snap.props.DeepChangeListener;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.props.Undoer;

/**
 * The root of a hierarchy of views.
 */
public class SceneGraph {

    // The object using this SceneGraph
    private Client _client;

    // The document being viewed
    private SGDoc _view;

    // The size of ScenGraph
    private double _width, _height;
    
    // An optional undoer object to track document changes
    private Undoer _undoer;

    // A PropChange listener to catch root view changes (Showing, PageSize, )
    private PropChangeListener _propLsnr = pc -> viewPropChanged(pc);

    // A deep PropChange listener to catch any view changes
    private DeepChangeListener _deepLsnr = (obj,pc) -> viewPropChangedDeep(pc);

    // Whether SceneGraph is currently painting
    private boolean  _ptg;

    /**
     * Creates a SceneGraph for given SceneGraph.Client.
     */
    public SceneGraph(Client aClient)
    {
        _client = aClient;
    }

    /**
     * Returns the document.
     */
    public SGDoc getDoc()  { return (SGDoc)getRootView(); }

    /**
     * Returns the RootView of this SceneGraph.
     */
    public SGParent getRootView()  { return _view; }

    /**
     * Sets the RootView of this SceneGraph.
     */
    public void setRootView(SGDoc aDoc)
    {
        // Resolve page references on document and make sure it has a selected page
        aDoc.resolvePageReferences();
        aDoc.layoutDeep();

        // If old view, stop listening to shape changes and notify shapes hidden
        if (_view!=null) {
            _view.removePropChangeListener(_propLsnr);
            _view.setSceneGraph(null);
        }

        // Set new view
        _view = aDoc;

        // Start listening to shape changes and notify shapes shown
        _view.setSceneGraph(this);
        _view.addPropChangeListener(_propLsnr);
        if (_client.isSceneDeepChangeListener())
            _view.addDeepChangeListener(_deepLsnr);
    }

    /**
     * Returns the width of this SceneGraph.
     */
    public double getWidth()  { return _width; }

    /**
     * Sets the width of this SceneGraph.
     */
    public void setWidth(double aValue)  { _width = aValue; }

    /**
     * Returns the height of this SceneGraph.
     */
    public double getHeight()  { return _height; }

    /**
     * Sets the height of this SceneGraph.
     */
    public void setHeight(double aValue)  { _height = aValue; }

    /**
     * Sets the size of SceneGraph.
     */
    public void setSize(double aWidth, double aHeight)  { setWidth(aWidth); setHeight(aHeight); }

    /**
     * Returns the undoer.
     */
    public Undoer getUndoer()  { return _undoer; }

    /**
     * Sets the undoer.
     */
    public void setUndoer(Undoer anUndoer)  { _undoer = anUndoer; }

    /**
     * Override to return content preferred width.
     */
    public double getPrefWidth()
    {
        SGParent view = getRootView();
        return view!=null ? view.getPrefWidth() : 0;
    }

    /**
     * Override to return content preferred height.
     */
    public double getPrefHeight()
    {
        SGParent view = getRootView();
        return view!=null ? view.getPrefHeight() : 0;
    }

    /**
     * Layout out SceneGraph views.
     */
    public void layoutViews()
    {
        // Disable undo
        undoerDisable();

        // Get width/height of Scene
        double pw = getWidth();
        double ph = getHeight();

        // Get root view and bounds in center of SceneGraph
        SGParent view = getRootView();
        double vw = view.getPrefWidth();
        double vh = view.getPrefHeight();
        double vx = pw>vw ? Math.floor((pw-vw)/2) : 0;
        double vy = ph>vh ? Math.floor((ph-vh)/2) : 0;

        // Set view bounds
        view.setBounds(vx, vy, vw, vh);

        // Set scale for ZoomFactor
        double zoom = _client.getSceneZoomFactor();
        if (view.getScaleX()!=zoom)
            view.setScaleXY(zoom, zoom);

        // Call layout on view
        view.layoutDeep();

        // Re-anable undo
        undoerEnable();
    }

    /**
     * Override to notify viewer.
     */
    protected void relayoutScene()
    {
        _client.sceneNeedsRelayout();
    }

    /**
     * Called by views to request paint when they change visual properties.
     */
    protected void repaintSceneForView(SGView aView)
    {
        // If painting, complain that someone is calling repaint during paint (should never happen, but good to check)
        if (_ptg)
            System.err.println("SceneGraph.repaint(): called during painting");

        // Forward to viewer
        _client.sceneNeedsRepaint(aView);
    }

    /**
     * Paints the Scene.
     */
    public void paintScene(Painter aPntr)
    {
        // Cache gstate and set Painting flag
        aPntr.save(); _ptg = true;

        // Paint view
        _view.paint(aPntr);

        // Restore gstate and reset Painting flag
        aPntr.restore(); _ptg = false;
    }

    /**
     * Called when root view has a prop change.
     */
    private void viewPropChanged(PropChange aPC)
    {
        _client.sceneViewPropChanged(aPC);
    }

    /**
     * Called when any scene view has a prop change.
     */
    private void viewPropChangedDeep(PropChange aPC)
    {
        _client.sceneViewPropChangedDeep(aPC);
    }

    /**
     * Undoer convenience - disable the undoer.
     */
    public void undoerDisable()  { Undoer u = getUndoer(); if (u!=null) u.disable(); }

    /**
     * Undoer convenience - enables the undoer.
     */
    public void undoerEnable()  { Undoer u = getUndoer(); if (u!=null) u.enable(); }

    /**
     * Returns whether painting is for editor.
     */
    public boolean isEditing()  { return _client.isSceneEditing(); }

    /**
     * Returns whether given view is selected.
     */
    public boolean isSelectedView(SGView aView)  { return _client.isSceneSelected(aView); }

    /**
     * Returns whether given view is super selected.
     */
    public boolean isSuperSelectedView(SGView aView)  { return _client.isSceneSuperSelected(aView); }

    /**
     * Returns whether given view is THE super selected view.
     */
    public boolean isSuperSelectedLeafView(SGView aView)  { return _client.isSceneSuperSelectedLeaf(aView); }

    /**
     * Returns whether painting is for editor.
     */
    public static boolean isEditing(SGView aView)
    {
        SceneGraph scene = aView.getSceneGraph();
        return scene!=null && scene.isEditing();
    }

    /**
     * Returns whether given view is selected.
     */
    public static boolean isSelected(SGView aView)
    {
        SceneGraph scene = aView.getSceneGraph();
        return scene!=null && scene.isSelectedView(aView);
    }

    /**
     * Returns whether given view is super selected.
     */
    public static boolean isSuperSelected(SGView aView)
    {
        SceneGraph scene = aView.getSceneGraph();
        return scene!=null && scene.isSuperSelectedView(aView);
    }

    /**
     * Returns whether given view is THE super selected view.
     */
    public static boolean isSuperSelectedLeaf(SGView aView)
    {
        SceneGraph scene = aView.getSceneGraph();
        return scene!=null && scene.isSuperSelectedLeafView(aView);
    }

    /**
     * An interface for objects that want to use a SceneGraph.
     */
    public interface Client {

        /** Called to get ZoomFactor. */
        double getSceneZoomFactor();

        /** Called when SceneGraph needs relayout. */
        void sceneNeedsRelayout();

        /** Called when SceneGraph view needs repaint. */
        void sceneNeedsRepaint(SGView aShape);

        /** Called when SceneGraph View has prop change. */
        void sceneViewPropChanged(PropChange aPC);

        /** Called to see if client wants deep changes. */
        default boolean isSceneDeepChangeListener()  { return false; }

        /** Called when SceneGraph View has prop change. */
        default void sceneViewPropChangedDeep(PropChange aPC)  { }

        /** Returns whether SceneGraph is being edited. */
        default boolean isSceneEditing()  { return false; }

        /** Returns whether given view is selected. */
        default boolean isSceneSelected(SGView aView)  { return false; }

        /** Returns whether given view is super selected. */
        default boolean isSceneSuperSelected(SGView aView)  { return false; }

        /** Returns whether given view is THE super selected view. */
        default boolean isSceneSuperSelectedLeaf(SGView aView)  { return false; }
    }
}
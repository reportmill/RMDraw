/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.app;
import rmdraw.scene.*;
import snap.gfx.Paint;
import snap.util.*;
import snap.view.*;
import snap.viewx.DialogBox;
import snap.web.WebURL;

/**
 * This class is a container for a viewer and tool bars. The default tool bars add document controls (save,
 * print, copy), input controls (select, pan, text select, image select), zoom controls and page controls. 
 * 
 * You might use it like this to view in a Swing component hierarchy:
 * <p><blockquote><pre>
 *   RMViewerPane viewer = new RMViewerPane(); viewer.getViewer().setDoc(new SGDoc(aSource));
 *   JComponent vcomp = viewer.getRootView().getNative(JComponent.class);
 *   myFrame.setContentPane(viewer);
 * </pre></blockquote>
 */
public class ViewerPane extends ViewOwner {

    // The real viewer
    private Viewer  _viewer;
    
    // The ScrollView for this viewer
    private ScrollView  _scrollView;
    
    // The RulerBox that holds the ScrollView
    private RulerBox  _rulerBox;
    
    // The controls at the top of the document
    private ViewOwner  _topToolBar;
    
    // The controls at the bottom of the document
    private ViewOwner  _btmToolBar;

    // Constants
    private static Paint BACK_FILL = ViewUtils.getBackFill();
    
    /**
     * Returns the viewer for this viewer pane.
     */
    public Viewer getViewer()
    {
        if(_viewer==null) getUI();
        return _viewer;
    }

    /**
     * Sets the viewer for this viewer pane.
     */
    protected void setViewer(Viewer aViewer)
    {
        _viewer = aViewer;
        getScrollView().setContent(_viewer);
    }

    /**
     * Creates the real viewer for this viewer pane.
     */
    protected Viewer createViewer()  { return new Viewer(); }

    /**
     * Returns the scroll view for this viewer pane.
     */
    public ScrollView getScrollView()  { return _scrollView; }

    /**
     * Returns the RulerBox that holds the ScrollView.
     */
    public RulerBox getRulerBox()  { return _rulerBox; }

    /**
     * Returns the document associated with this viewer.
     */
    public SGDoc getDoc()  { return getViewer().getDoc(); }

    /**
     * Returns the document source.
     */
    protected WebURL getSourceURL()
    {
        return getViewer().getSourceURL();
    }

    /**
     * Returns the top controls.
     */
    public ViewOwner getTopToolBar()
    {
        return _topToolBar!=null ? _topToolBar : (_topToolBar=createTopToolBar());
    }

    /**
     * Creates the top tool bar.
     */
    protected ViewOwner createTopToolBar()
    {
        return new ViewerTopToolBar(this);
    }

    /**
     * Returns the bottom controls.
     */
    public ViewOwner getBottomToolBar()
    {
        return _btmToolBar!=null ? _btmToolBar : (_btmToolBar=createBottomToolBar());
    }

    /**
     * Creates bottom tool bar.
     */
    protected ViewOwner createBottomToolBar()
    {
        return new ViewerBottomToolBar(this);
    }

    /**
     * Saves the current viewer document.
     */
    public void save()  { }

    /**
     * Prints the current viewer document.
     */
    public void print()  { getViewer().print(); }

    /**
     * Copies the current viewer document selection.
     */
    public void copy()  { getViewer().getInteractor().copy(); }

    /**
     * Returns whether editor pane shows rulers.
     */
    public boolean isShowRulers()  { return getRulerBox().isShowRulers(); }

    /**
     * Sets whether editor pane shows rulers.
     */
    public void setShowRulers(boolean aValue)
    {
        // Determine if we should resize window after toggle (depending on whether window is at preferred size)
        WindowView win = getWindow();
        boolean doPack = win.getSize().equals(win.getPrefSize());

        // Forward to RulerBox
        getRulerBox().setShowRulers(aValue);

        // Resize window if window was previously at preferred size
        if(doPack)
            getWindow().pack();
    }

    /**
     * Runs a dialog panel to request a percentage zoom (which is then set with setZoomFactor).
     */
    public void runZoomPanel()
    {
        // Run input dialog to get zoom factor string
        DialogBox dbox = new DialogBox("Zoom Panel"); dbox.setQuestionMessage("Enter Percentage to Zoom to:");
        String string = dbox.showInputDialog(getUI(), "120");

        // If string is valid, set zoom factor to float value
        if(string!=null) {
            float factor = StringUtils.floatValue(string)/100;
            if(factor>0)
                getViewer().setZoomFactor(factor);
        }

        // Request focus
        requestFocus(getViewer());
    }

    /**
     * Initializes the UI.
     */
    protected View createUI()
    {
        // Create and configure viewer
        _viewer = createViewer();

        // Create/configure ScrollView
        _scrollView = new ScrollView();
        _scrollView.setFill(BACK_FILL);
        _scrollView.setContent(_viewer);

        // Create RulerBox
        _rulerBox = new RulerBox();
        _rulerBox.setContent(_scrollView);

        // Create BorderView and add TopToolBar, ScrollView/Viewer and BottomToolBar
        BorderView bpane = new BorderView();
        bpane.setTop(getTopToolBar().getUI());
        bpane.setCenter(_rulerBox);
        bpane.setBottom(getBottomToolBar().getUI());
        return bpane;
    }

    /**
     * Resets UI.
     */
    protected void resetUI()
    {
        // Repaint rulers if visible
        if(isShowRulers()) getRulerBox().repaint();

        // Trigger top/bottom toolbar resets
        if (!ViewUtils.isMouseDown()) getTopToolBar().resetLater();
        if (!ViewUtils.isMouseDown()) getBottomToolBar().resetLater();
    }
}
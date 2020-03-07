/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.app;
import snap.view.*;

/**
 * UI controls for RMViewerPlus top.
 */
public class ViewerTopToolBar extends ViewOwner {

    // The viewer associated with this tool bar
    ViewerPane _viewerPane;
    
/**
 * Creates a new top ui.
 */
public ViewerTopToolBar(ViewerPane aViewerPane)  { _viewerPane = aViewerPane; }

/**
 * Returns the viewer pane.
 */
public ViewerPane getViewerPane()  { return _viewerPane; }

/**
 * Returns the viewer.
 */
public Viewer getViewer()  { return getViewerPane().getViewer(); }

/**
 * Resets to UI.
 */
public void resetUI()  { }

/**
 * Responds to UI.
 */
public void respondUI(ViewEvent anEvent)
{
    // Handle SaveButton
    if(anEvent.equals("SaveButton"))
        getViewerPane().save();
    
    // Handle PrintButton
    if(anEvent.equals("PrintButton"))
        getViewerPane().print();
    
    // Handle CopyButton
    if(anEvent.equals("CopyButton"))
        getViewerPane().copy();
    
    // Handle MoveButton
    if(anEvent.equals("MoveButton"))
        getViewer().getInteractor().setMode(ViewerInteractor.DEFAULT);
    
    // Handle TextButton
    if(anEvent.equals("TextButton"))
        getViewer().getInteractor().setMode(ViewerInteractor.SELECT_TEXT);

    // Handle SelectButton
    if(anEvent.equals("SelectButton"))
        getViewer().getInteractor().setMode(ViewerInteractor.SELECT_IMAGE);
}

}
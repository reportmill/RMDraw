/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.app;
import rmdraw.shape.*;
import snap.geom.*;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;
import snap.viewx.Printer;
import snap.web.WebURL;

/**
 * The Viewer is used to display or print a document/SceneGraph.
 *
 * You might use it like this to simply print a document:
 * <p><blockquote><pre>
 *   new Viewer(aDocument).print();
 * </pre></blockquote><p>
 * Or you might want to allocate one and add it to a Swing component hierarchy:
 * <p><blockquote><pre>
 *   ViewerPane viewer = new ViewerPane(); viewer.getViewer().setContent(new RMDocument(aSource));
 *   JComponent vcomp = viewer.getRootView().getNative(JComponent.class);
 *   myFrame.setContentPane(viewer);
 * </pre></blockquote>
 */
public class Viewer extends ParentView implements SceneGraph.Client {

    // The shape viewer uses to manage real root of shapes
    private SceneGraph _sceneGraph = new SceneGraph(this);
    
    // The Zoom mode
    private ZoomMode  _zoomMode = ZoomMode.ZoomAsNeeded;
    
    // Zoom factor
    private double  _zoomFactor = 1;
    
    // The previous zoom factor (for toggle zoom)
    private double  _lastZoomFactor = 1;

    // The helper class that handles events for viewer
    private ViewerInteractor _interactor = createInteractor();

    // Zoom modes
    public enum ZoomMode { ZoomToFit, ZoomAsNeeded, ZoomToFactor }
    
    // Constants for PropertyChanges
    public static final String Content_Prop = "Content";
        
/**
 * Creates a new RMViewer with an empty document in it.
 */
public Viewer()
{
    enableEvents(MouseEvents);
    enableEvents(KeyEvents);
    setFocusable(true);
    setFocusWhenPressed(true);
    setFill(Color.LIGHTGRAY);
}

/**
 * Returns the SceneGraph.
 */
public SceneGraph getSceneGraph()  { return _sceneGraph; }

/**
 * Returns the document associated with this viewer.
 */
public RMDocument getDoc()  { return getSceneGraph().getDoc(); }

/**
 * Sets the document associated with this viewer.
 */
public void setDoc(RMDocument aDoc)
{
    // If already set, just return
    RMDocument doc = getDoc(); if (aDoc==doc) return;
    
    // Set new document in SceneGraph
    _sceneGraph.setRootView(aDoc);

    // Fire property change
    firePropChange(Content_Prop, doc, aDoc);
    
    // Set ZoomToFitFactor and relayout/repaint (for possible size change)
    setZoomToFitFactor();
    relayout(); repaint();
}

/**
 * Sets the document from given source.
 */
public void setDocFromSource(Object aSource)
{
    RMArchiver archiver = createArchiver();
    RMDocument doc = archiver.getDocFromSource(aSource);
    setDoc(doc);
}

/**
 * Returns the source URL.
 */
public WebURL getSourceURL()  { RMDocument d = getDoc(); return d!=null? d.getSourceURL() : null; }

/**
 * Creates an archiver.
 */
public RMArchiver createArchiver()
{
    return new RMArchiver();
}

/**
 * Returns whether viewer is really doing editing.
 */
public boolean isEditing()  { return false; }

/**
 * Returns whether editor is preview (or viewer) mode.
 */
public boolean isPreview()  { return !isEditing(); }

/**
 * Returns the page count.
 */
public int getPageCount()  { return getDoc().getPageCount(); }

/**
 * Returns the currently selected page shape.
 */
public RMPage getSelPage()  { return getDoc().getSelPage(); }

/**
 * Returns the index of the current visible document page.
 */
public int getSelPageIndex()  { return getDoc().getSelPageIndex(); }

/**
 * Sets the page of viewer's document that is visible (by index).
 */
public void setSelPageIndex(int anIndex)  { getDoc().setSelPageIndex(anIndex); }

/**
 * Selects the next page.
 */
public void pageForward()  { setSelPageIndex(getSelPageIndex()+1); }

/**
 * Selects the previous page.
 */
public void pageBack()  { setSelPageIndex(getSelPageIndex()-1); }

/**
 * Returns the bounds of the viewer document.
 */
public Rect getDocBounds()  { return convertFromShape(getDoc().getBoundsLocal(), null).getBounds(); }

/**
 * Returns the first shape hit by the given point.
 */
public RMShape getShapeAtPoint(double aX, double aY, boolean goDeep) { return getShapeAtPoint(new Point(aX,aY),goDeep);}

/**
 * Returns the first shape hit by the given point.
 */
public RMShape getShapeAtPoint(Point aPoint, boolean goDeep)
{
    // Convert point from viewer to selected page
    RMParentShape parent = getSelPage();
    Point point = convertToShape(aPoint.x, aPoint.y, parent);
    
    // Iterate over children to find shape hit by point
    RMShape shape = null; Point point2 = null;
    for (int i=parent.getChildCount(); i>0 && shape==null; i--) { RMShape child = parent.getChild(i-1);
        point2 = child.parentToLocal(point);
        if (child.contains(point2))
            shape = child;
    }
    
    // If we need to goDeep (and there was a top level hit shape), recurse until shape is found
    while (goDeep && shape instanceof RMParentShape) { parent = (RMParentShape)shape;
        RMShape shp = parent.getChildContaining(point2);
        if (shp!=null) { shape = shp; point2 = shape.parentToLocal(point2); }
        else break;
    }
    
    // Return hit shape
    return shape;
}

/**
 * Returns the viewer's zoom factor (1 by default).
 */
public double getZoomFactor()  { return _zoomFactor; }

/**
 * Sets the viewer's zoom factor (1 for 100%).
 */
public void setZoomFactor(double aFactor)
{
    setZoomMode(ZoomMode.ZoomToFactor);
    setZoomFactorImpl(aFactor);
}

/**
 * Sets the viewer's zoom factor (1 for 100%) and mode.
 */
protected void setZoomFactorImpl(double aFactor)
{    
    // Constrain zoom factor to valid range (ZoomToFactor: 20%...10000%, ZoomAsNeed: Max of 1)
    ZoomMode zmode = getZoomMode();
    if (zmode==ZoomMode.ZoomToFactor)
        aFactor = Math.min(Math.max(.2f, aFactor), 100);
    else if (zmode==ZoomMode.ZoomAsNeeded)
        aFactor = Math.min(aFactor, 1);

    // If already at given factor, just return
    if (aFactor==_zoomFactor) return;

    // Set last zoom factor and new zoom factor and fire property change
    firePropChange("ZoomFactor", _lastZoomFactor = _zoomFactor, _zoomFactor = aFactor);
    
    // If ZoomToFactor and parent is viewport, resize and scroll to center of previous zoom
    if (isZoomToFactor()) {
        Rect vr = getZoomFocusRect(), vr2 = vr.clone();
        setSize(getPrefWidth(), getPrefHeight());
        vr2.scale(_zoomFactor/_lastZoomFactor);
        vr2.inset((vr2.getWidth() - vr.getWidth())/2, (vr2.getHeight() - vr.getHeight())/2);
        scrollToVisible(vr2);
    }
    
    // Relayout and repaint
    relayout(); relayoutParent(); repaint();
}

/**
 * Returns the ZoomMode (ZoomToFit, ZoomIfNeeded, ZoomToFactor).
 */
public ZoomMode getZoomMode()  { return _zoomMode; }

/**
 * Sets the ZoomMode.
 */
public void setZoomMode(ZoomMode aZoomMode)
{
    if (aZoomMode==getZoomMode()) return;
    firePropChange("ZoomMode", _zoomMode, _zoomMode = aZoomMode);
    setZoomToFitFactor(); // Reset ZoomFactor
}

/**
 * Returns whether viewer is set to ZoomToFactor.
 */
public boolean isZoomToFactor()  { return getZoomMode()==ZoomMode.ZoomToFactor; }

/**
 * Returns the zoom factor for the given mode at the current viewer size.
 */
public double getZoomFactor(ZoomMode aMode)
{
    // If ZoomToFactor, just return ZoomFactor
    if (aMode==ZoomMode.ZoomToFactor) return getZoomFactor();
    
    // Get ideal size and current size (if size is zero, return 1)
    double pw = _sceneGraph.getPrefWidth();
    double ph = _sceneGraph.getPrefHeight();
    double width = getWidth();
    double height = getHeight(); if (width==0 || height==0) return 1;
    
    // If ZoomAsNeeded and IdealSize is less than size, return
    if (aMode==ZoomMode.ZoomAsNeeded && pw<=width && ph<=height) return 1;
    if (aMode==ZoomMode.ZoomToFit && pw==width && ph==height) return 1;
    
    // Otherwise get ratio of parent size to ideal size (with some gutter added in) and return smaller axis
    double zw = width/(pw + 8f), zh = height/(ph + 8f);
    return Math.min(zw, zh);
}

/**
 * Sets the zoom to fit factor, based on the current zoom mode.
 */
public void setZoomToFitFactor()  { setZoomFactorImpl(getZoomFactor(getZoomMode())); }

/**
 * Returns zoom focus rect (just the visible rect by default, but overriden by editor to return selected shapes rect).
 */
public Rect getZoomFocusRect()  { return getVisRect(); }

/**
 * Returns the zoom factor to view the document at actual size taking into account the current screen resolution.
 */
public double getZoomToActualSizeFactor()  { return GFXEnv.getEnv().getScreenResolution()/72; }

/**
 * Sets the viewer's zoom to its previous value.
 */
public void zoomToggleLast()  { setZoomFactor(_lastZoomFactor); }

/**
 * Overrides to update ZoomFactor if dynamic.
 */
public void setWidth(double aValue)  { super.setWidth(aValue); setZoomToFitFactor(); }

/**
 * Overrides to update ZoomFactor if dynamic.
 */
public void setHeight(double aValue)  { super.setHeight(aValue); setZoomToFitFactor(); }

/**
 * Returns a point converted from the coordinate space of the given shape to viewer coords.
 */
public Point convertFromShape(double aX, double aY, RMShape aShape)
{
    return aShape!=null? aShape.localToParent(aX, aY, null) : new Point(aX,aY);
}

/**
 * Returns a point converted from viewer coords to the coordinate space of the given shape.
 */
public Point convertToShape(double aX, double aY, RMShape aShape)
{
    return aShape!=null? aShape.parentToLocal(aX, aY, null) : new Point(aX,aY);
}

/**
 * Returns a rect converted from the coordinate space of the given shape to viewer coords.
 */
public Shape convertFromShape(Shape aShp, RMShape aShape)
{
    return aShape!=null? aShape.localToParent(aShp, null) : new Path(aShp);
}

/**
 * Returns a shape converted from viewer coords to the coordinate space of the given RMShape.
 */
public Shape convertToShape(Shape aShp, RMShape aShape)
{
    return aShape!=null? aShape.parentToLocal(aShp, null) : new Path(aShp);
}

/**
 * Override to paint viewer shapes and page, margin, grid, etc.
 */
public void paintFront(Painter aPntr)
{
    // Paint SceneGraph
    _sceneGraph.paintScene(aPntr);

    // Give interactor opportunity to paint
    getInteractor().paint(aPntr); // Have event helper paint above
}

/**
 * Returns the ViewerInteractor for viewer which handles mouse and keyboard input.
 */
public ViewerInteractor getInteractor()  { return _interactor; }

/**
 * Creates a default ViewerInteractor.
 */
protected ViewerInteractor createInteractor()  { return new ViewerInteractor(this); }

/**
 * Handle mouse events.
 */
protected void processEvent(ViewEvent anEvent)
{
    super.processEvent(anEvent); // Do normal version
    getInteractor().processEvent(anEvent); // Forward to event helper
}

/**
 * Returns the preferred size of the viewer (includes ZoomFactor).
 */
protected double getPrefWidthImpl(double aH)
{
    double pw = _sceneGraph.getPrefWidth();
    if (isZoomToFactor()) pw *= getZoomFactor();
    return pw;
}

/**
 * Returns the preferred size of the viewer (includes ZoomFactor).
 */
protected double getPrefHeightImpl(double aW)
{
    double ph = _sceneGraph.getPrefHeight();
    if (isZoomToFactor()) ph *= getZoomFactor();
    return ph;
}

/**
 * Override to reposition SceneGraph.
 */
protected void layoutImpl()
{
    setZoomToFitFactor();
    _sceneGraph.setSize(getWidth(), getHeight());
    _sceneGraph.layoutViews();
}

/**
 * Returns the undoer associated with the viewer's document.
 */
public Undoer getUndoer()  { return _sceneGraph.getUndoer(); }

/**
 * Sets the title of the next registered undo in the viewer's documents's undoer (convenience).
 */
public void undoerSetUndoTitle(String aTitle)
{
    if(getUndoer()!=null)
        getUndoer().setUndoTitle(aTitle);
}

/**
 * Returns whether undos exist in the viewer's documents's undoer (convenience).
 */
public boolean undoerHasUndos()  { return getUndoer()!=null && getUndoer().hasUndos(); }

/**
 * Returns the ZoomFactor for SceneGraph.
 */
public double getSceneZoomFactor()  { return getZoomFactor(); }

/**
 * SceneGraph.Client method: Called when SceneGraph needs relayout.
 */
public void sceneNeedsRelayout()
{
    relayout();
}

/**
 * SceneGraph.Client method: Called when SceneGraph view needs repaint.
 */
public void sceneNeedsRepaint(RMShape aShape)
{
    // Get shape bounds in viewer coords and repaint
    Rect bnds0 = getRepaintBoundsForShape(aShape);
    Rect bnds1 = aShape.localToParent(bnds0, null).getBounds();
    repaint(bnds1);
}

/**
 * SceneGraph.Client method: Called when SceneGraph Doc has prop change.
 */
public void sceneViewPropChanged(PropChange anEvent)
{
    // Handle SelectedPageIndex, PageSize, PageLayout
    String pname = anEvent.getPropertyName();
    if (pname.equals(RMDocument.SelPageIndex_Prop) || pname.equals("PageSize") || pname.equals("PageLayout")) {
        setZoomToFitFactor();
        relayout();
        repaint();
        firePropChange("ContentChange" + pname, anEvent.getOldValue(), anEvent.getNewValue());
    }
}

/**
 * Returns the bounds for a given shape in the viewer. Editor overrides this to account for handles.
 */
protected Rect getRepaintBoundsForShape(RMShape aShape)
{
    //Rect bnds = aShape.getBoundsLocal();
    //if(aShape.getStroke()!=null) bnds.inset(-aShape.getStroke().getWidth()/2);
    //if(aShape.getEffect()!=null) bnds = aShape.getEffect().getBounds(bnds); return bnds;
    return aShape.getBoundsMarkedDeep();
}

/**
 * Creates a shape mouse event.
 */
public ViewEvent createShapeEvent(RMShape aShape, ViewEvent anEvent, ViewEvent.Type aType)
{
    Point point = convertToShape(anEvent.getX(), anEvent.getY(), aShape);
    return new RMShapeEvent(aShape, anEvent, point, aType); // was ne
}

/**
 * This method tells the RMViewer to print by running the print dialog (configured to the default printer).
 */
public void print()  { print(null, true); }

/**
 * This method tells the RMViewer to print to the printer with the given printer name (use null for default printer). It
 * also offers an option to run the printer dialog.
 */
public void print(String aPrinterName, boolean showPanel)
{
    Printer.Printable printable = new RMVPrintable();
    Printer.print(printable, aPrinterName, showPanel);
}

/**
 * A Printable implmentation for RMViewer.
 */
private class RMVPrintable implements Printer.Printable {
    
    /** Returns a print page count for given printer. */
    public int getPageCount(Printer aPrinter)  { return Viewer.this.getPageCount(); }
    
    /** Returns the page size for given page index. */
    public Size getPageSize(Printer aPrinter, int anIndex)
    {
        RMShape page = getDoc().getPage(anIndex);
        return page.getSize();
    }
    
    /** Executes a print for given printer and page index. */
    public void print(Printer aPrinter, int anIndex)
    {
        RMShape page = getDoc().getPage(anIndex);
        Painter pntr = aPrinter.getPainter();
        RMShapeUtils.paintShape(pntr, page, null, 1);
    }
}

}
/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.app;
import rmdraw.apptools.TextToolStyler;
import rmdraw.scene.*;
import java.text.DecimalFormat;
import java.util.*;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;

/**
 * This is the base class for tools in draw to provide UI editing for Scene views.
 */
public class Tool<T extends SGView> extends ViewOwner {
    
    // The Editor that owns this tool
    private Editor _editor;
    
    // The Editor pane
    private EditorPane _editorPane;

    // The newly created view instance
    protected SGView _newView;
    
    // The mouse down point that initiated last tool mouse loop
    protected Point _downPoint;
    
    // A formatter for bounds fields
    private static DecimalFormat    _fmt = new DecimalFormat("0.##");
    
    // The image for a view handle
    private static Image  _handle = Image.get(Editor.class, "Handle8x8.png");
    
    // Handle constants
    public static final byte HandleWidth = 8;
    public static final byte HandleNW = 0;
    public static final byte HandleNE = 1;
    public static final byte HandleSW = 2;
    public static final byte HandleSE = 3;
    public static final byte HandleW = 4;
    public static final byte HandleE = 5;
    public static final byte HandleN = 6;
    public static final byte HandleS = 7;

    // Constants
    public static final Border DEFAULT_BORDER = Border.createLineBorder(Color.RED, 4); //Border.blackBorder()
    public static final boolean DEFAULT_PENCIL_ONESHOT = true;

    /**
     * Returns the view class that this tool handles.
     */
    public Class <T> getViewClass()  { return (Class<T>) SGView.class; }

    /**
     * Returns a new instance of the view class that this tool is responsible for.
     */
    protected T newInstance()  { return ClassUtils.newInstance(getViewClass()); }

    /**
     * Returns the string to be used for the inspector window title.
     */
    public String getWindowTitle()  { return "View Inspector"; }

    /**
     * Create Node.
     */
    protected View createUI()  { return getClass()== Tool.class? new Label() : super.createUI(); }

    /**
     * Returns the currently active editor.
     */
    public Editor getEditor()  { return _editor; }

    /**
     * Sets the currently active editor.
     */
    public void setEditor(Editor anEditor)  { _editor = anEditor; }

    /**
     * Returns the currently active editor pane.
     */
    public EditorPane getEditorPane()
    {
        if(_editorPane!=null) return _editorPane;
        return _editorPane = _editor.getEditorPane();
    }

    /**
     * Returns the editor event handler.
     */
    public EditorInteractor getEditorEvents()  { return getEditor().getInteractor(); }

    /**
     * Returns a CopyPaster for editor to use.
     * Might want to override if tool manages it's own selection (like TextTool).
     */
    protected CopyPaster getCopyPaster()
    {
        return getEditor().getCopyPasterDefault();
    }

    /**
     * Returns the current selected view for the current editor.
     */
    public T getSelView()
    {
        Editor e = getEditor(); if(e==null) return null;
        SGView s = e.getSelOrSuperSelView();
        return ClassUtils.getInstance(s, getViewClass());
    }

    /**
     * Returns the current selected views for the current editor.
     */
    public List <? extends SGView> getSelViews()  { return getEditor().getSelOrSuperSelViews(); }

    /**
     * Returns the tool for a given view.
     */
    public Tool getTool(SGView aView)  { return _editor.getToolForView(aView); }

    /**
     * Returns the tool for a given view class.
     */
    public Tool getToolForClass(Class <? extends SGView> aClass)  { return _editor.getToolForClass(aClass); }

    /**
     * Sets undo title.
     */
    public void setUndoTitle(String aTitle)
    {
        getEditor().undoerSetUndoTitle(aTitle);
    }

    /**
     * Called when a tool is selected.
     */
    public void activateTool()  { }

    /**
     * Called when a tool is deselected (when another tool is selected).
     */
    public void deactivateTool()  { }

    /**
     * Called when a tool is selected even when it's already the current tool.
     */
    public void reactivateTool()  { }

    /**
     * Returns whether a given view is selected in the editor.
     */
    public boolean isSelected(SGView aView)  { return getEditor().isSelected(aView); }

    /**
     * Returns whether given view is super-selected in the editor.
     */
    public boolean isSuperSelected(SGView aView)  { return getEditor().isSuperSelected(aView); }

    /**
     * Returns whether a given view is super-selectable.
     */
    public boolean isSuperSelectable(SGView aView)  { return aView.superSelectable(); }

    /**
     * Returns whether a given view accepts children.
     */
    public boolean getAcceptsChildren(SGView aView)  { return aView.acceptsChildren(); }

    /**
     * Returns whether a given view can be ungrouped.
     */
    public boolean isUngroupable(SGView aView)  { return aView.getChildCount()>0; }

    /**
     * Editor method - called when an instance of this tool's view is super selected.
     */
    public void didBecomeSuperSel(T aView)  { }

    /**
     * Editor method - called when an instance of this tool's view in de-super-selected.
     */
    public void willLoseSuperSel(T aView)  { }

    /**
     * Returns the bounds of the view in parent coords when super selected (same as getBoundsMarkedDeep by default).
     */
    public Rect getBoundsSuperSel(T aView)  { return aView.getBoundsMarkedDeep(); }

    /**
     * Converts from view units to tool units.
     */
    public double getUnitsFromPoints(double aValue)
    {
        Editor editor = getEditor(); SGDoc doc = editor.getDoc();
        return doc!=null? doc.getUnitsFromPoints(aValue) : aValue;
    }

    /**
     * Converts from view units to tool units.
     */
    public String getUnitsFromPointsStr(double aValue)  { return _fmt.format(getUnitsFromPoints(aValue)); }

    /**
     * Converts from tool units to view units.
     */
    public double getPointsFromUnits(double aValue)
    {
        Editor editor = getEditor(); SGDoc doc = editor.getDoc();
        return doc!=null? doc.getPointsFromUnits(aValue) : aValue;
    }

    /**
     * Returns a Styler for tool and view.
     */
    public ToolStyler getStyler(SGView aView)
    {
        if (aView instanceof SGText)
            return new TextToolStyler(this, aView);
        return new ToolStyler(this, aView);
    }

    /**
     * Event handling - called on mouse move when this tool is active.
     */
    public void mouseMoved(ViewEvent anEvent)  { }

    /**
     * Event handling for view creation.
     */
    public void mousePressed(ViewEvent anEvent)
    {
        // Set undo title
        setUndoTitle("Add View");

        // Save the mouse down point
        _downPoint = getEditorEvents().getEventPointInView(true);

        // Create view and move to downPoint
        _newView = newInstance();
        _newView.setXY(_downPoint);

        // Add view to superSelView and select
        getEditor().getSuperSelParentView().addChild(_newView);
        getEditor().setSelView(_newView);
    }

    /**
     * Event handling for view creation.
     */
    public void mouseDragged(ViewEvent anEvent)
    {
        _newView.repaint();
        Point currentPoint = getEditorEvents().getEventPointInView(true);
        double x = Math.min(_downPoint.getX(), currentPoint.getX());
        double y = Math.min(_downPoint.getY(), currentPoint.getY());
        double w = Math.abs(currentPoint.getX() - _downPoint.getX());
        double h = Math.abs(currentPoint.getY() - _downPoint.getY());
        _newView.setFrame(x, y, w, h);
    }

    /**
     * Event handling for view creation.
     */
    public void mouseReleased(ViewEvent anEvent)
    {
        // If user basically just clicked, expand view to semi-reasonable size
        if (_newView.getWidth()<=2 && _newView.getHeight()<=2)
            _newView.setSize(20,20);

        // Reset Editor.CurrentTool to SelectTool and clear view
        getEditor().setCurrentToolToSelectTool(); _newView = null;
    }

    /**
     * Event handling from SelectTool for super selected views.
     */
    public void processEvent(T aView, ViewEvent anEvent)
    {
        switch(anEvent.getType()) {
            case MousePress: mousePressed(aView, anEvent); break;
            case MouseDrag: mouseDragged(aView, anEvent); break;
            case MouseRelease: mouseReleased(aView, anEvent); break;
            case MouseMove: mouseMoved(aView, anEvent); break;
            default: if (anEvent.isKeyEvent()) processKeyEvent(aView, anEvent);
        }
    }

    /**
     * Event handling from select tool for super selected views.
     */
    public void mousePressed(T aView, ViewEvent anEvent)  { }

    /**
     * Event handling from select tool for super selected views.
     */
    public void mouseDragged(T aView, ViewEvent anEvent)  { }

    /**
     * Event handling from select tool for super selected views.
     */
    public void mouseReleased(T aView, ViewEvent anEvent)  { }

    /**
     * Event handling from select tool - called on mouse move when tool view is super selected.
     * MouseMoved is useful for setting a custom cursor.
     */
    public void mouseMoved(T aView, ViewEvent anEvent)
    {
        // Just return if view isn't the super-selected view
        //if(aView!=getEditor().getSuperSelView()) return;

        // Get handle view
        ViewHandle viewHandle = getHandleAtPoint(anEvent.getPoint());

        // Declare variable for cursor
        Cursor cursor = null;

        // If view handle is non-null, set cursor and return
        if(viewHandle!=null)
            cursor = viewHandle.tool.getHandleCursor(viewHandle.view, viewHandle.handle);

        // If mouse not on handle, check for mouse over a view
        else {

            // Get mouse over view
            SGView view = getEditor().getViewAtPoint(anEvent.getX(),anEvent.getY());

            // If view isn't super selected and it's parent doesn't superselect children immediately, choose move cursor
            if(!isSuperSelected(view) && !view.getParent().childrenSuperSelectImmediately())
                cursor = Cursor.MOVE;

            // If view is text and either super-selected or child of a super-select-immediately, choose text cursor
            if(view instanceof SGText && (isSuperSelected(view) || view.getParent().childrenSuperSelectImmediately()))
                cursor = Cursor.TEXT;
        }

        // Set cursor if it differs
        getEditor().setCursor(cursor);
    }

    /**
     * Event hook during selection.
     */
    public boolean mousePressedSelection(ViewEvent anEvent)  { return false; }

    /**
     * Returns a tool tip string for given view and event.
     */
    public String getToolTip(T aView, ViewEvent anEvent)  { return null; }

    /**
     * Editor method.
     */
    protected void processKeyEvent(T aView, ViewEvent anEvent)  { }

    /**
     * Paints when tool is active for things like SelectTool's handles & selection rect or polygon's in-progress path.
     */
    public void paintTool(Painter aPntr)
    {
        // Paint handles for super selected views
        paintHandlesForSuperSelViews(aPntr);

        // If Editor.MouseDown, just return
        Editor editor = getEditor();
        if(editor.isMouseDown())
            return;

        // Otherwise, paint handles for selected views
        paintHandlesForViews(aPntr, null);
    }

    /**
     * Paints handles for given list of views (uses Editor.SelViews if null).
     */
    protected void paintHandlesForViews(Painter aPntr, List <SGView> theViews)
    {
        // Get editor and views
        Editor editor = getEditor();
        List <SGView> views = theViews!=null? theViews : editor.getSelViews();

        // Iterate over views and have tool paintHandles
        for (SGView view : views) {
            Tool tool = getTool(view);
            tool.paintHandles(view, aPntr, false);
        }
    }

    /**
     * Paints handles for super selected views.
     */
    protected void paintHandlesForSuperSelViews(Painter aPntr)
    {
        // Iterate over super selected views and have tool paint SuperSelected
        Editor editor = getEditor();
        for (int i = 1, iMax = editor.getSuperSelViewCount(); i<iMax; i++) {
            SGView view = editor.getSuperSelView(i);
            Tool tool = getTool(view);
            tool.paintHandles(view, aPntr, true);
        }
    }

    /**
     * Handles painting view handles (or any indication that a view is selected/super-selected).
     */
    public void paintHandles(T aView, Painter aPntr, boolean isSuperSelected)
    {
        // If no handles, just return
        if (getHandleCount(aView)==0) return;

        // Turn off antialiasing and cache current opacity
        aPntr.setAntialiasing(false);
        double opacity = aPntr.getOpacity();

        // If super-selected, set composite to make drawing semi-transparent
        if (isSuperSelected)
            aPntr.setOpacity(.64);

        // Determine if rect should be reduced if the view is especially small
        boolean mini = aView.getWidth()<=20 || aView.getHeight()<=20;

        // Iterate over view handles, get rect (reduce if needed) and draw
        for (int i=0, iMax=getHandleCount(aView); i<iMax; i++) {
            Rect hr = getHandleRect(aView, i, isSuperSelected);
            if (mini) hr.inset(1, 1);
            aPntr.drawImage(_handle, hr.x, hr.y, hr.width, hr.height);
        }

        // Restore opacity and turn on antialiasing
        aPntr.setOpacity(opacity);
        aPntr.setAntialiasing(true);
    }

    /**
     * Returns the number of handles for this view.
     */
    public int getHandleCount(T aView)  { return 8; }

    /**
     * Returns the point for the handle of the given view at the given handle index in the given view's coords.
     */
    public Point getHandlePoint(T aView, int aHandle, boolean isSuperSel)
    {
        // Get bounds of given view
        Rect bounds = isSuperSel ? getBoundsSuperSel(aView).getInsetRect(-HandleWidth/2):aView.getBoundsLocal();

        // Get minx and miny of given view
        double minX = aView.width()>=0? bounds.x : bounds.getMaxX();
        double minY = aView.height()>=0? bounds.y : bounds.getMaxY();

        // Get maxx and maxy of givn view
        double maxX = aView.width()>=0? bounds.getMaxX() : bounds.x;
        double maxY = aView.height()>=0? bounds.getMaxY() : bounds.y;

        // Get midx and midy of given view
        double midX = minX + (maxX-minX)/2;
        double midY = minY + (maxY-minY)/2;

        // Get point for given handle
        switch(aHandle) {
            case HandleNW: return new Point(minX, minY);
            case HandleNE: return new Point(maxX, minY);
            case HandleSW: return new Point(minX, maxY);
            case HandleSE: return new Point(maxX, maxY);
            case HandleW: return new Point(minX, midY);
            case HandleE: return new Point(maxX, midY);
            case HandleN: return new Point(midX, minY);
            case HandleS: return new Point(midX, maxY);
        }

        // Return null if invalid handle
        return null;
    }

    /**
     * Returns the rect for the handle at the given index in editor coords.
     */
    public Rect getHandleRect(T aView, int aHandle, boolean isSuperSelected)
    {
        // Get handle point for given handle index in view coords and editor coords
        Point hp = getHandlePoint(aView, aHandle, isSuperSelected);
        Point hpEd = getEditor().convertFromSceneView(hp.getX(), hp.getY(), aView);

        // Get handle rect at handle point, outset rect by handle width and return
        Rect hr = new Rect(Math.round(hpEd.getX()), Math.round(hpEd.getY()), 0, 0);
        hr.inset(-HandleWidth/2);
        return hr;
    }

    /**
     * Returns the handle hit by the given editor coord point.
     */
    public int getHandleAtPoint(T aView, Point aPoint, boolean isSuperSelected)
    {
        // Iterate over view handles, get handle rect for current loop handle and return index if rect contains point
        for(int i=0, iMax=getHandleCount(aView); i<iMax; i++) {
            Rect hr = getHandleRect(aView, i, isSuperSelected);
            if(hr.contains(aPoint.getX(), aPoint.getY()))
                return i; }
        return -1; // Return -1 since no handle at given point
    }

    /**
     * Returns the cursor for given handle.
     */
    public Cursor getHandleCursor(T aView, int aHandle)
    {
        // Get cursor for handle type
        switch(aHandle) {
            case HandleN: return Cursor.N_RESIZE;
            case HandleS: return Cursor.S_RESIZE;
            case HandleE: return Cursor.E_RESIZE;
            case HandleW: return Cursor.W_RESIZE;
            case HandleNW: return Cursor.NW_RESIZE;
            case HandleNE: return Cursor.NE_RESIZE;
            case HandleSW: return Cursor.SW_RESIZE;
            case HandleSE: return Cursor.SE_RESIZE;
        }

        // Return null
        return null;
    }

    /**
     * Moves the handle at the given index to the given point.
     */
    public void moveHandle(T aView, int aHandle, Point toPoint)
    {
        // Get handle point in view coords and view parent coords
        Point p1 = getHandlePoint(aView, aHandle, false);
        Point p2 = aView.parentToLocal(toPoint);

        // If middle handle is used, set delta and p2 of that component to 0
        boolean minX = false, maxX = false, minY = false, maxY = false;
        switch(aHandle) {
            case HandleNW: minX = minY = true; break;
            case HandleNE: maxX = minY = true; break;
            case HandleSW: minX = maxY = true; break;
            case HandleSE: maxX = maxY = true; break;
            case HandleW: minX = true; break;
            case HandleE: maxX = true; break;
            case HandleS: maxY = true; break;
            case HandleN: minY = true; break;
        }

        // Calculate new width and height for handle move
        double dx = p2.getX() - p1.getX(), dy = p2.getY() - p1.getY();
        double nw = minX? aView.width() - dx : maxX? aView.width() + dx : aView.width();
        double nh = minY? aView.height() - dy : maxY? aView.height() + dy : aView.height();

        // Set new width and height, but calc new X & Y such that opposing handle is at same location w.r.t. parent
        Point op = getHandlePoint(aView, getHandleOpposing(aHandle), false);
        op = aView.localToParent(op);

        // Make sure new width and height are not too small
        if(Math.abs(nw)<.1) nw = MathUtils.sign(nw)*.1f;
        if(Math.abs(nh)<.1) nh = MathUtils.sign(nh)*.1f;

        // Set size
        aView.setSize(nw, nh);

        // Get point
        Point p = getHandlePoint(aView, getHandleOpposing(aHandle), false);
        p = aView.localToParent(p);

        // Set frame
        aView.setFrameXY(aView.getFrameX() + op.getX() - p.getX(), aView.getFrameY() + op.getY() - p.getY());
    }

    /**
     * Returns the handle index that is across from given handle index.
     */
    public int getHandleOpposing(int handle)
    {
        // Return opposing handle from given panel
        switch(handle) {
            case HandleNW: return HandleSE;
            case HandleNE: return HandleSW;
            case HandleSW: return HandleNE;
            case HandleSE: return HandleNW;
            case HandleW: return HandleE;
            case HandleE: return HandleW;
            case HandleS: return HandleN;
            case HandleN: return HandleS;
        }

        // Return -1 if given handle is unknown
        return -1;
    }

    /**
     * An inner class describing a view and a handle.
     */
    public static class ViewHandle {

        // The view, handle index and tool
        public SGView  view;
        public int  handle;
        public Tool  tool;

        /** Creates ViewHandle. */
        public ViewHandle(SGView aView, int aHndl, Tool aTool) { view = aView; handle = aHndl; tool = aTool; }
    }

    /**
     * Returns the view handle for the given editor point.
     */
    public ViewHandle getHandleAtPoint(Point aPoint)
    {
        // Declare variable for view and handle and tool
        SGView view = null; int handle = -1; Tool tool = null;
        Editor editor = getEditor();

        // Check selected views for a selected handle index
        for (int i = 0, iMax = editor.getSelViewCount(); handle==-1 && i<iMax; i++) {
            view = editor.getSelView(i);
            tool = getTool(view);
            handle = tool.getHandleAtPoint(view, aPoint, false);
        }

        // Check super selected views for a selected handle index
        for (int i = 0, iMax = editor.getSuperSelViewCount(); handle==-1 && i<iMax; i++) {
            view = editor.getSuperSelView(i);
            tool = getTool(view);
            handle = tool.getHandleAtPoint(view, aPoint, true);
        }

        // Return view handle
        return handle>=0 ? new ViewHandle(view, handle, tool) : null;
    }

    /**
     * Asks tool whether given view accepts given drag event.
     */
    public boolean acceptsDrag(T aView, ViewEvent anEvent)  { return false; }

    /**
     * Notifies tool that a something was dragged into of one of its views with drag and drop.
     */
    public void dragEnter(SGView aView, ViewEvent anEvent)  { }

    /**
     * Notifies tool that a something was dragged out of one of its views with drag and drop.
     */
    public void dragExit(SGView aView, ViewEvent anEvent)  { }

    /**
     * Notifies tool that something was dragged over one of its views with drag and drop.
     */
    public void dragOver(SGView aView, ViewEvent anEvent)  { }

    /**
     * Notifies tool that something was dropped on one of it's views with drag and drop.
     */
    public void dragDrop(T aView, ViewEvent anEvent)
    {
        Editor editor = getEditor();
        editor.getDragDropper().dropForView(aView, anEvent);
    }

    /**
     * Creates a view mouse event.
     */
    protected ViewEvent createSceneEvent(SGView s, ViewEvent e)  { return getEditor().createSceneViewEvent(s, e, null); }

    /**
     * Returns the image used to represent views that this tool represents.
     */
    public Image getImage()
    {
        return _image!=null? _image : (_image=getImageImpl());
    }
    Image _image;

    /**
     * Returns the image used to represent views that this tool represents.
     */
    protected Image getImageImpl()
    {
        for (Class c = getClass(); c!= Tool.class; c=c.getSuperclass()) {
            String name = c.getSimpleName().replace("Tool", "") + ".png";
            Image img = Image.get(c, name);
            if (img!=null) return img;
        }
        return Image.get(Tool.class, "Tool.png");
    }

}
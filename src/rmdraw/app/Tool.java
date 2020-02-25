/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.app;
import rmdraw.shape.*;
import java.text.DecimalFormat;
import java.util.*;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;

/**
 * This is the base class for tools in RM - the objects that provide GUI editing for RM shapes.
 */
public class Tool<T extends RMShape> extends ViewOwner {
    
    // The Editor that owns this tool
    private Editor _editor;
    
    // The Editor pane
    private EditorPane _editorPane;

    // The newly created shape instance
    protected RMShape _shape;
    
    // The mouse down point that initiated last tool mouse loop
    protected Point _downPoint;
    
    // A formatter for bounds fields
    private static DecimalFormat    _fmt = new DecimalFormat("0.##");
    
    // The image for a shape handle
    private static Image            _handle = Image.get(Editor.class, "Handle8x8.png");
    
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

/**
 * Returns the shape class that this tool handles.
 */
public Class <T> getShapeClass()  { return (Class<T>)RMShape.class; }

/**
 * Returns a new instance of the shape class that this tool is responsible for.
 */
protected T newInstance()  { return ClassUtils.newInstance(getShapeClass()); }

/**
 * Returns the string to be used for the inspector window title.
 */
public String getWindowTitle()  { return "Shape Inspector"; }

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
public EditorEvents getEditorEvents()  { return getEditor().getEvents(); }
    
/**
 * Returns the current selected shape for the current editor.
 */
public T getSelectedShape()
{
    Editor e = getEditor(); if(e==null) return null;
    RMShape s = e.getSelectedOrSuperSelectedShape();
    return ClassUtils.getInstance(s, getShapeClass());
}

/**
 * Returns the current selected shapes for the current editor.
 */
public List <? extends RMShape> getSelectedShapes()  { return getEditor().getSelectedOrSuperSelectedShapes(); }

/**
 * Returns the tool for a given shape.
 */
public Tool getTool(RMShape aShape)  { return _editor.getTool(aShape); }

/**
 * Returns the tool for a given shape class.
 */
public Tool getToolForClass(Class <? extends RMShape> aClass)  { return _editor.getTool(aClass); }

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
 * Called when a tool is deselected to give an opportunity to finalize changes in progress.
 */
public void flushChanges(Editor anEditor, RMShape aShape)  { }

/**
 * Returns whether a given shape is selected in the editor.
 */
public boolean isSelected(RMShape aShape)  { return getEditor().isSelected(aShape); }

/**
 * Returns whether a given shape is superselected in the editor.
 */
public boolean isSuperSelected(RMShape aShape)  { return getEditor().isSuperSelected(aShape); }

/**
 * Returns whether a given shape is super-selectable.
 */
public boolean isSuperSelectable(RMShape aShape)  { return aShape.superSelectable(); }

/**
 * Returns whether a given shape accepts children.
 */
public boolean getAcceptsChildren(RMShape aShape)  { return aShape.acceptsChildren(); }

/**
 * Returns whether a given shape can be ungrouped.
 */
public boolean isUngroupable(RMShape aShape)  { return aShape.getChildCount()>0; }

/**
 * Editor method - called when an instance of this tool's shape is super selected.
 */
public void didBecomeSuperSelected(T aShape)  { }

/**
 * Editor method - called when an instance of this tool's shape in de-super-selected.
 */
public void willLoseSuperSelected(T aShape)  { }

/**
 * Returns the bounds of the shape in parent coords when super selected (same as getBoundsMarkedDeep by default).
 */
public Rect getBoundsSuperSelected(T aShape)  { return aShape.getBoundsMarkedDeep(); }

/**
 * Converts from shape units to tool units.
 */
public double getUnitsFromPoints(double aValue)
{
    Editor editor = getEditor(); RMDocument doc = editor.getDoc();
    return doc!=null? doc.getUnitsFromPoints(aValue) : aValue;
}

/**
 * Converts from shape units to tool units.
 */
public String getUnitsFromPointsStr(double aValue)  { return _fmt.format(getUnitsFromPoints(aValue)); }

/**
 * Converts from tool units to shape units.
 */
public double getPointsFromUnits(double aValue)
{
    Editor editor = getEditor(); RMDocument doc = editor.getDoc();
    return doc!=null? doc.getPointsFromUnits(aValue) : aValue;
}

/**
 * Returns a Styler for tool and shape.
 */
public ToolStyler getStyler(RMShape aShape)
{
    if (aShape instanceof RMTextShape)
        return new ToolStylerText(this, aShape);
    return new ToolStyler(this, aShape);
}

/**
 * Event handling - called on mouse move when this tool is active.
 */
public void mouseMoved(ViewEvent anEvent)  { }

/**
 * Event handling for shape creation.
 */
public void mousePressed(ViewEvent anEvent)
{
    // Set undo title
    getEditor().undoerSetUndoTitle("Add Shape");

    // Save the mouse down point
    _downPoint = getEditorEvents().getEventPointInShape(true);

    // Create shape and move to downPoint
    _shape = newInstance();
    _shape.setXY(_downPoint);
    
    // Add shape to superSelectedShape and select shape
    getEditor().getSuperSelectedParentShape().addChild(_shape);
    getEditor().setSelectedShape(_shape);
}

/**
 * Event handling for shape creation.
 */
public void mouseDragged(ViewEvent anEvent)
{
    _shape.repaint();
    Point currentPoint = getEditorEvents().getEventPointInShape(true);
    double x = Math.min(_downPoint.getX(), currentPoint.getX());
    double y = Math.min(_downPoint.getY(), currentPoint.getY());
    double w = Math.abs(currentPoint.getX() - _downPoint.getX());
    double h = Math.abs(currentPoint.getY() - _downPoint.getY());
    _shape.setFrame(x, y, w, h);
}

/**
 * Event handling for shape creation.
 */
public void mouseReleased(ViewEvent anEvent)
{
    // If user basically just clicked, expand shape to semi-reasonable size
    if(_shape.getWidth()<=2 && _shape.getHeight()<=2)
        _shape.setSize(20,20);
    
    // Reset Editor.CurrentTool to SelectTool and clear Shape
    getEditor().setCurrentToolToSelectTool(); _shape = null;
}

/**
 * Event handling from SelectTool for super selected shapes.
 */
public void processEvent(T aShape, ViewEvent anEvent)
{
    switch(anEvent.getType()) {
        case MousePress: mousePressed(aShape, anEvent); break;
        case MouseDrag: mouseDragged(aShape, anEvent); break;
        case MouseRelease: mouseReleased(aShape, anEvent); break;
        case MouseMove: mouseMoved(aShape, anEvent); break;
        default: if(anEvent.isKeyEvent()) processKeyEvent(aShape, anEvent);
    }
}

/**
 * Event handling from select tool for super selected shapes.
 */
public void mousePressed(T aShape, ViewEvent anEvent)  { }

/**
 * Event handling from select tool for super selected shapes.
 */
public void mouseDragged(T aShape, ViewEvent anEvent)  { }

/**
 * Event handling from select tool for super selected shapes.
 */
public void mouseReleased(T aShape, ViewEvent anEvent)  { }

/**
 * Event handling from select tool - called on mouse move when tool shape is super selected.
 * MouseMoved is useful for setting a custom cursor.
 */
public void mouseMoved(T aShape, ViewEvent anEvent)
{
    // Just return if shape isn't the super-selected shape
    //if(aShape!=getEditor().getSuperSelectedShape()) return;
    
    // Get handle shape
    RMShapeHandle shapeHandle = getShapeHandleAtPoint(anEvent.getPoint());
    
    // Declare variable for cursor
    Cursor cursor = null;
    
    // If shape handle is non-null, set cursor and return
    if(shapeHandle!=null)
        cursor = shapeHandle.tool.getHandleCursor(shapeHandle.shape, shapeHandle.handle);
    
    // If mouse not on handle, check for mouse over a shape
    else {
        
        // Get mouse over shape
        RMShape shape = getEditor().getShapeAtPoint(anEvent.getX(),anEvent.getY());
        
        // If shape isn't super selected and it's parent doesn't superselect children immediately, choose move cursor
        if(!isSuperSelected(shape) && !shape.getParent().childrenSuperSelectImmediately())
            cursor = Cursor.MOVE;
        
        // If shape is text and either super-selected or child of a super-select-immediately, choose text cursor
        if(shape instanceof RMTextShape && (isSuperSelected(shape) || shape.getParent().childrenSuperSelectImmediately()))
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
 * Returns a tool tip string for given shape and event.
 */
public String getToolTip(T aShape, ViewEvent anEvent)  { return null; }

/**
 * Editor method.
 */
public void processKeyEvent(T aShape, ViewEvent anEvent)  { }

/**
 * Paints when tool is active for things like SelectTool's handles & selection rect or polygon's in-progress path.
 */
public void paintTool(Painter aPntr)
{
    // Paint handles for super selected shapes
    paintHandlesForSuperSelectedShapes(aPntr);
    
    // If Editor.MouseDown, just return
    Editor editor = getEditor();
    if(editor.isMouseDown())
        return;
        
    // Otherwise, paint handles for selected shapes
    paintHandlesForShapes(aPntr, null);
}

/**
 * Paints handles for given list of shapes (uses Editor.SelectedShapes if null).
 */
protected void paintHandlesForShapes(Painter aPntr, List <RMShape> theShapes)
{
    // Get editor and shapes
    Editor editor = getEditor();
    List <RMShape> shapes = theShapes!=null? theShapes : editor.getSelectedShapes();
    
    // Iterate over shapes and have tool paintHandles
    for(RMShape shape : shapes) {
        Tool tool = getTool(shape);
        tool.paintHandles(shape, aPntr, false);
    }
}

/**
 * Paints handles for super selected shapes.
 */
protected void paintHandlesForSuperSelectedShapes(Painter aPntr)
{
    // Iterate over super selected shapes and have tool paint SuperSelected
    Editor editor = getEditor();
    for(int i=1, iMax=editor.getSuperSelectedShapeCount(); i<iMax; i++) {
        RMShape shape = editor.getSuperSelectedShape(i);
        Tool tool = getTool(shape);
        tool.paintHandles(shape, aPntr, true);
    }
}

/**
 * Handles painting shape handles (or any indication that a shape is selected/super-selected).
 */
public void paintHandles(T aShape, Painter aPntr, boolean isSuperSelected)
{
    // If no handles, just return
    if(getHandleCount(aShape)==0) return;
    
    // Turn off antialiasing and cache current opacity
    aPntr.setAntialiasing(false);
    double opacity = aPntr.getOpacity();
    
    // If super-selected, set composite to make drawing semi-transparent
    if(isSuperSelected)
        aPntr.setOpacity(.64);
    
    // Determine if rect should be reduced if the shape is especially small
    boolean mini = aShape.getWidth()<=20 || aShape.getHeight()<=20;
        
    // Iterate over shape handles, get rect (reduce if needed) and draw
    for(int i=0, iMax=getHandleCount(aShape); i<iMax; i++) {
        Rect hr = getHandleRect(aShape, i, isSuperSelected); if(mini) hr.inset(1, 1);
        aPntr.drawImage(_handle, hr.x, hr.y, hr.width, hr.height);
    }
        
    // Restore opacity and turn on antialiasing
    aPntr.setOpacity(opacity);
    aPntr.setAntialiasing(true);
}

/**
 * Returns the number of handles for this shape.
 */
public int getHandleCount(T aShape)  { return 8; }

/**
 * Returns the point for the handle of the given shape at the given handle index in the given shape's coords.
 */
public Point getHandlePoint(T aShape, int aHandle, boolean isSuperSelected)
{
    // Get bounds of given shape
    Rect bounds = isSuperSelected? getBoundsSuperSelected(aShape).getInsetRect(-HandleWidth/2):aShape.getBoundsInside();
    
    // Get minx and miny of given shape
    double minX = aShape.width()>=0? bounds.x : bounds.getMaxX();
    double minY = aShape.height()>=0? bounds.y : bounds.getMaxY();
    
    // Get maxx and maxy of givn shape
    double maxX = aShape.width()>=0? bounds.getMaxX() : bounds.x;
    double maxY = aShape.height()>=0? bounds.getMaxY() : bounds.y;
    
    // Get midx and midy of given shape
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
public Rect getHandleRect(T aShape, int aHandle, boolean isSuperSelected)
{
    // Get handle point for given handle index in shape coords and editor coords
    Point hp = getHandlePoint(aShape, aHandle, isSuperSelected);
    Point hpEd = getEditor().convertFromShape(hp.getX(), hp.getY(), aShape);
    
    // Get handle rect at handle point, outset rect by handle width and return
    Rect hr = new Rect(Math.round(hpEd.getX()), Math.round(hpEd.getY()), 0, 0);
    hr.inset(-HandleWidth/2);
    return hr;
}

/**
 * Returns the handle hit by the given editor coord point.
 */
public int getHandleAtPoint(T aShape, Point aPoint, boolean isSuperSelected)
{
    // Iterate over shape handles, get handle rect for current loop handle and return index if rect contains point
    for(int i=0, iMax=getHandleCount(aShape); i<iMax; i++) {
        Rect hr = getHandleRect(aShape, i, isSuperSelected);
        if(hr.contains(aPoint.getX(), aPoint.getY()))
            return i; }
    return -1; // Return -1 since no handle at given point
}

/**
 * Returns the cursor for given handle.
 */
public Cursor getHandleCursor(T aShape, int aHandle)
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
public void moveShapeHandle(T aShape, int aHandle, Point toPoint)
{
    // Get handle point in shape coords and shape parent coords
    Point p1 = getHandlePoint(aShape, aHandle, false);
    Point p2 = aShape.parentToLocal(toPoint);
    
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
    double nw = minX? aShape.width() - dx : maxX? aShape.width() + dx : aShape.width();
    double nh = minY? aShape.height() - dy : maxY? aShape.height() + dy : aShape.height();

    // Set new width and height, but calc new X & Y such that opposing handle is at same location w.r.t. parent
    Point op = getHandlePoint(aShape, getHandleOpposing(aHandle), false);
    op = aShape.localToParent(op);
    
    // Make sure new width and height are not too small
    if(Math.abs(nw)<.1) nw = MathUtils.sign(nw)*.1f;
    if(Math.abs(nh)<.1) nh = MathUtils.sign(nh)*.1f;

    // Set size
    aShape.setSize(nw, nh);
    
    // Get point
    Point p = getHandlePoint(aShape, getHandleOpposing(aHandle), false);
    p = aShape.localToParent(p);
    
    // Set frame
    aShape.setFrameXY(aShape.getFrameX() + op.getX() - p.getX(), aShape.getFrameY() + op.getY() - p.getY());
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
 * An inner class describing a shape and a handle.
 */
public static class RMShapeHandle {

    // The shape, handle index and shape tool
    public RMShape  shape;
    public int      handle;
    public Tool tool;
    
    /** Creates a new shape-handle. */
    public RMShapeHandle(RMShape aShape, int aHndl, Tool aTool) { shape = aShape; handle = aHndl; tool = aTool; }
}

/**
 * Returns the shape handle for the given editor point.
 */
public RMShapeHandle getShapeHandleAtPoint(Point aPoint)
{
    // Declare variable for shape and handle and shape tool
    RMShape shape = null; int handle = -1; Tool tool = null;
    Editor editor = getEditor();

    // Check selected shapes for a selected handle index
    for(int i=0, iMax=editor.getSelectedShapeCount(); handle==-1 && i<iMax; i++) {
        shape = editor.getSelectedShape(i);
        tool = getTool(shape);
        handle = tool.getHandleAtPoint(shape, aPoint, false);
    }

    // Check super selected shapes for a selected handle index
    for(int i=0, iMax=editor.getSuperSelectedShapeCount(); handle==-1 && i<iMax; i++) {
        shape = editor.getSuperSelectedShape(i);
        tool = getTool(shape);
        handle = tool.getHandleAtPoint(shape, aPoint, true);
    }

    // Return shape handle
    return handle>=0? new RMShapeHandle(shape, handle, tool) : null;
}

/**
 * Implemented by shapes that can handle drag & drop.
 */
public boolean acceptsDrag(T aShape, ViewEvent anEvent)
{
    // Bogus, but currently the page accepts everything
    if(aShape.isRoot()) return true;
    
    // Return true for Color drag or File drag
    if(anEvent.getClipboard().hasColor()) return true;
    
    // Handle file drag - really just want to check for images here, but can't ask for transferable contents yet
    if(anEvent.getClipboard().hasFiles())
        return true;
    
    // Return true in any case if accepts children
    return getTool(aShape).getAcceptsChildren(aShape);
}

/**
 * Notifies tool that a something was dragged into of one of it's shapes with drag and drop.
 */
public void dragEnter(RMShape aShape, ViewEvent anEvent)  { }

/**
 * Notifies tool that a something was dragged out of one of it's shapes with drag and drop.
 */
public void dragExit(RMShape aShape, ViewEvent anEvent)  { }

/**
 * Notifies tool that something was dragged over one of it's shapes with drag and drop.
 */
public void dragOver(RMShape aShape, ViewEvent anEvent)  { }

/**
 * Notifies tool that something was dropped on one of it's shapes with drag and drop.
 */
public void drop(T aShape, ViewEvent anEvent)
{
    Editor editor = getEditor();
    editor.getDragHelper().dropForView(aShape, anEvent);
}

/**
 * Creates a shape mouse event.
 */
protected ViewEvent createShapeEvent(RMShape s, ViewEvent e)  { return getEditor().createShapeEvent(s, e, null); }

/**
 * Returns the image used to represent shapes that this tool represents.
 */
public Image getImage()
{
    return _image!=null? _image : (_image=getImageImpl());
}
Image _image;

/**
 * Returns the image used to represent shapes that this tool represents.
 */
protected Image getImageImpl()
{
    for(Class c = getClass(); c!= Tool.class; c=c.getSuperclass()) {
        String name = c.getSimpleName().replace("Tool", "") + ".png";
        Image img = Image.get(c, name);
        if(img!=null) return img;
    }
    return Image.get(Tool.class, "RMShape.png");
}

}
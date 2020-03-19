/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.scene;
import java.util.*;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.gfx.*;

/**
 * Some shape utility methods.
 */
public class SGViewUtils {

/**
 * Returns the average width of shapes.
 */
public static double getAverageWidth(List <SGView> theShapes)
{
    double w = 0; for(SGView s : theShapes) w += s.getWidth();
    return w/theShapes.size();
}

/**
 * Returns the average width of shapes.
 */
public static double getMaxFrameMaxY(List <SGView> theShapes)
{
    double my = 0; for(SGView s : theShapes) if(s.isVisible()) my = Math.max(my, s.getFrameMaxY());
    return my;
}

/**
 * Sorts given list of shapes by X.
 */
public static void sortByX(List <SGView> theShapes)
{
    Collections.sort(theShapes, (s0,s1) -> compareShapeX(s0,s1));
}

/**
 * Returns a copy of given list of shapes sorted by X.
 */
public static List <SGView> getShapesSortedByX(List <SGView> theShapes)
{
    return getShapesSorted(theShapes, (s0,s1) -> compareShapeX(s0,s1));
}

/**
 * Returns a copy of given list of shapes sorted by FrameX.
 */
public static List <SGView> getShapesSortedByFrameX(List <SGView> theShapes)
{
    return getShapesSorted(theShapes, (s0,s1) -> compareShapeFrameX(s0,s1));
}

/**
 * Returns a copy of given list of shapes sorted by FrameY.
 */
public static List <SGView> getShapesSortedByFrameY(List <SGView> theShapes)
{
    return getShapesSorted(theShapes, (s0,s1) -> compareShapeFrameY(s0,s1));
}

/**
 * Returns a copy of given list of shapes sorted by FrameY.
 */
public static List <SGView> getShapesSortedByFrameYFrameX(List <SGView> theShapes)
{
    return getShapesSorted(theShapes, (s0,s1) -> compareShapeFrameYFrameX(s0,s1));
}

/**
 * Returns a copy of given list of shapes sorted by shape Index.
 */
public static List <SGView> getShapesSortedByIndex(List <SGView> theShapes)
{
    return getShapesSorted(theShapes, (s0,s1) -> compareShapeIndex(s0,s1));
}

/**
 * Returns a copy of given list of shapes sorted by Comparator.
 */
public static List <SGView> getShapesSorted(List <SGView> theShapes, Comparator <SGView> aComp)
{
    List <SGView> shapes = new ArrayList(theShapes);
    Collections.sort(shapes, aComp);
    return shapes;
}

/** Compare methods for Shape X, FrameX, FrameY, FrameYFrameX, Index */
private static int compareShapeX(SGView s0, SGView s1)
{ double v0 = s0.getX(), v1 = s1.getX(); return v0<v1? -1 : v0>v1? 1 : 0; }
private static int compareShapeFrameX(SGView s0, SGView s1)
{ double v0 = s0.getFrameX(), v1 = s1.getFrameX(); return v0<v1? -1 : v0>v1? 1 : 0; }
private static int compareShapeFrameY(SGView s0, SGView s1)
{ double v0 = s0.getFrameY(), v1 = s1.getFrameY(); return v0<v1? -1 : v0>v1? 1 : 0; }
private static int compareShapeFrameYFrameX(SGView s0, SGView s1)
{ int c = compareShapeFrameY(s0,s1); if(c==0) c = compareShapeFrameX(s0,s1); return c; }
private static int compareShapeIndex(SGView s0, SGView s1)
{ int v0 = s0.indexOf(), v1 = s1.indexOf(); return v0<v1? -1 : v0>v1? 1 : 0; }

/**
 * Returns the bounds of a given subset of this shape's children.
 */
public static Rect getBoundsOfChildren(SGView aShape, List <? extends SGView> aList)
{
    // If list is null or empty, return this shape's bounds inside
    if(aList==null || aList.size()==0)
        return aShape.getBoundsLocal();
    
    // Declare and initialize a rect to frame of first shape in list
    Rect rect = aList.get(0).getFrame();
    
    // Iterate over successive shapes in list and union their frames
    for(int i=1, iMax=aList.size(); i<iMax; i++) {
        SGView child = aList.get(i);
        rect.unionEvenIfEmpty(child.getFrame());
    }
    
    // Return frame
    return rect;
}

/**
 * Returns a polygon shape by combining paths of given shapes.
 */
public static SGPolygon getCombinedPathsShape(List <SGView> theShapes)
{
    // Get first shape, parent and combined bounds
    SGView shape0 = theShapes.size()>0? theShapes.get(0) : null; if(shape0==null) return null;
    SGView parent = shape0.getParent();
    Rect combinedBounds = getBoundsOfChildren(parent, theShapes);
    
    // Get the path of the combined shapes
    Shape combinedPath = getCombinedPath(theShapes);

    // Create combined shape, configure and return
    SGPolygon shape = new SGPolygon(combinedPath);
    shape.copyView(shape0); shape._rss = null;
    shape.setFrame(combinedBounds);
    return shape;
}

/**
 * Returns the combined path from given shapes.
 */
public static Shape getCombinedPath(List <SGView> theShapes)
{
    List <Shape> paths = getPathsFromViews(theShapes, 0); Shape s1 = paths.get(0);
    for(int i=1, iMax=paths.size(); i<iMax; i++) { Shape s2 = paths.get(i);
        s1 = Shape.add(s1, s2); }
    return s1;
}

/**
 * Returns a polygon shape by combining paths of given shapes.
 */
public static SGPolygon getSubtractedPathsView(List <SGView> theViews, int anInset)
{
    // Get SubtractedPath by subtracting paths and its bounds
    Shape subtractedPath = getSubtractedPath(theViews, 0);
    Rect subtractedBounds = subtractedPath.getBounds();

    // Create view, configure and return
    SGPolygon view = new SGPolygon(subtractedPath);
    view.copyView(theViews.get(0)); view._rss = null;
    view.setBounds(subtractedBounds);
    return view;
}

/**
 * Returns the combined path from given views.
 */
public static Shape getSubtractedPath(List <SGView> theViews, int anInset)
{
    // Eliminate views that don't intersect first view frame
    SGView view0 = theViews.get(0);
    Rect view0Frame = view0.getFrame();
    List <SGView> views = theViews;
    for(int i=views.size()-1; i>=0; i--) { SGView view = views.get(i);
        if(!view.getFrame().intersects(view0Frame)) {
            if(views==theViews) views = new ArrayList(theViews); views.remove(i); }}
    
    // Get view paths, iterate over them, successively subtract them and return final
    List <Shape> paths = getPathsFromViews(views, anInset); Shape s1 = paths.get(0);
    for (int i=1, iMax=paths.size(); i<iMax; i++) { Shape s2 = paths.get(i);
        s1 = Shape.subtract(s1, s2); }
    return s1;
}

/**
 * Returns the list of paths from the given views list.
 */
private static List <Shape> getPathsFromViews(List <SGView> theViews, int anInset)
{
    // Iterate over views, get bounds of each (inset), path of each (in parent coords) and add to list
    List paths = new ArrayList(theViews.size());
    for(int i=0, iMax=theViews.size(); i<iMax; i++) { SGView view = theViews.get(i);
        Rect bounds = view.getBoundsLocal(); if(anInset!=0 && i>0) bounds.inset(anInset);
        Shape path = view.getPath().copyFor(bounds);
        path = view.localToParent(path);
        paths.add(path);
    }
    
    // Return paths list
    return paths;
}

/**
 * Returns an image for the given view, with given background color (null for clear) and scale.
 */
public static Image createImage(SGView aView, Color aColor)
{
    // Get marked bounds for view
    Rect bounds = aView instanceof SGPage ? aView.getBounds() : aView.getBoundsMarkedDeep();
    
    // Calculate image size from view bounds and scale (rounded up to integral size)
    int w = (int)Math.ceil(bounds.getWidth());
    int h = (int)Math.ceil(bounds.getHeight());
    
    // If view has no area, return empty image
    if(w==0 || h==0)
        return Image.get(1,1,false);
    
    // Create new image
    Image img = Image.getImageForSizeAndScale(w, h, aColor==null || aColor.getAlphaInt()!=255, 2);
    
    // Create painter and configure
    Painter pntr = img.getPainter();
    pntr.setPrinting(true);
    pntr.setImageQuality(1);
    
    // Fill background
    if(aColor!=null) {
        pntr.setColor(aColor); pntr.fillRect(0,0,w,h); }

    // Paint view and return image
    layoutDeep(aView);
    paintView(aView, pntr, new Rect(0,0,w,h), 1);
    return img;
}

/**
 * Makes sure view layout is up to date.
 */
public static void layoutDeep(SGView aView)
{
    if(aView instanceof SGParent) ((SGParent)aView).layoutDeep();
}

/**
 * Paints a simple view.
 */
public static void paintView(SGView aView, Painter aPntr, Rect aBounds, double aScale)
{
    // Cache gstate
    aPntr.save();
    
    // If bounds are present, set transform to position content
    if(aBounds!=null) {
        
        // Get view marked bounds
        Rect sbnds = aView.getBoundsMarked();
        double sw = sbnds.getWidth();
        double sh = sbnds.getHeight();
    
        // Get the discrepancy of bounds size and view scaled size
        double dw = aBounds.getWidth() - sw*aScale;
        double dh = aBounds.getHeight() - sh*aScale;
        
        // Constrain alignment to bounds (maybe this should be an option)
        if(dw<0) dw = 0; if(dh<0) dh = 0;
        
        // Get the translations to bounds with specified alignments (don't allow alignment outside)
        double tx = aBounds.getX() + dw*.5, ty = aBounds.getY() + dh*.5;
        tx = Math.round(tx - .01); ty = Math.round(ty - .01); // Round down?
        aPntr.translate(tx, ty);
    }
    
    // Do scale
    if(aScale!=1)
        aPntr.scale(aScale, aScale);
    
    // Apply inverse view transform to negate effects of view paint applying transform
    aPntr.transform(aView.getParentToLocal());

    // Paint view and restore gstate
    aView.paint(aPntr);
    aPntr.restore();
}

}
/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.app;
import rmdraw.scene.*;
import java.util.*;
import java.util.List;

import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.*;
import snap.util.*;

/**
 * This class offers some methods to provide "proximity guides" for Editor. This feature draws lines indicating when
 * dragged views share alignments with some of their neighboring views (and also snaps to these alignments, too).
 */
public class EditorProxGuide {

    // Whether proximity guides are enabled.
    private static boolean  _enabled = Prefs.get().getBoolean("ProximityGuide", false);
    
    // Whether bounds of parent view are also check for proximity
    private static boolean  _includeSuperSelView = false;
    
    // The list of rects that need to be repainted for proximity guides
    private static List <Rect>  _guidelineRects = new Vector();

    /**
     * Returns whether proximity guides are enabled.
     */
    public static boolean isEnabled()  { return _enabled; }

    /**
     * Sets whether proximity guides are enabled.
     */
    public static void setEnabled(boolean aFlag)
    {
        _enabled=aFlag;
        Prefs.get().setValue("ProximityGuide", aFlag);
    }

    /**
     * Empties the guideline list and marks the old guideline region for repaint
     */
    public static void clearGuidelines(Editor anEditor)
    {
        markGuidelinesDirty(anEditor);
        _guidelineRects.clear();
    }

    /**
     * Runs through the guideline list and asks the editor to repaint the enclosing rect.
     */
    public static void markGuidelinesDirty(Editor anEditor)
    {
        // If no GuidelineRects, just return
        if (_guidelineRects.size()==0) return;

        // Get copy of first rect and union with successive rects
        Rect dirty = _guidelineRects.get(0).clone();
        for (int i=1; i<_guidelineRects.size(); i++)
            dirty.union(_guidelineRects.get(i));

        // Outset by 2 to cover stroke and repaint rect
        dirty.inset(-2, -2);
        anEditor.repaint(dirty);
    }

    /**
     * Called by the editor to paint all the guidelines in the guideline list
     */
    public static void paintProximityGuides(Editor anEditor, Painter aPntr)
    {
        // If no GuidelineRects, just return
        if (_guidelineRects.size()==0) return;

        // Set color to blue and stroke to 1.3pt dashed line
        aPntr.setColor(Color.BLUE);
        Stroke stroke = new Stroke(1.3, new float[] { 4,2 }, 0); aPntr.setStroke(stroke);

        // Draw proximity guide lines (with AntiAliasing on?)
        boolean aa = aPntr.setAntialiasing(true);
        for (Rect r : _guidelineRects) aPntr.drawLine(r.x, r.y, r.getMaxX(), r.getMaxY());
        aPntr.setAntialiasing(aa);
    }

    /**
     * If this flag is set, the bounds of parent view are also checked for proximity.
     */
    public static void setIncludeSuperSelView(boolean aFlag)  { _includeSuperSelView = aFlag; }

    /**
     * Returns the list of views to be included in the proximity check.
     */
    public static List <SGView> getCandidateViews(Editor anEditor)
    {
        // Get super selected view
        SGView parent = anEditor.getSuperSelView();
        if (parent.getChildCount()==0)
            return Collections.emptyList();

        // Get all peers of selected views
        List <SGView> candidates = new ArrayList(parent.getChildren());
        for (int i = 0, iMax = anEditor.getSelViewCount(); i<iMax; i++)
            ListUtils.removeId(candidates, anEditor.getSelView(i));

        // Optionally, also check against the bounds of the parent.
        // The "stepParent" is merely an empty view whose bounds match the parent, but in the same coordinate
        // system as the other candidate views.
        if (_includeSuperSelView) {
            SGView stepParent = new SGView();
            stepParent.copyView(parent);
            stepParent.setXY(0f, 0f);
            candidates.add(stepParent);
        }

        // Return candidate views
        return candidates;
    }

    /**
     * Calculate guidelines for the bounds of the selected views against all other supersel views.
     */
    public static void createGuidelines(Editor anEditor)
    {
        // If not in select tool drag move or resize, just return
        SelectTool.DragMode dragMode = anEditor.getSelectTool().getDragMode();
        if (dragMode!= SelectTool.DragMode.Move && dragMode!= SelectTool.DragMode.Resize)
            return;

        // If no selected views, just return
        if (anEditor.getSelViewCount()==0)
            return;

        // Get parent of selected views (just return if structured table row)
        SGView parent = anEditor.getSuperSelView();
        //if(parent instanceof RMTableRow && ((RMTableRow) parent).isStructured()) return;

        // Get candidate views for editor
        List <SGView> candidates = getCandidateViews(anEditor);

        // Get bounds
        Rect bounds = SGViewUtils.getBoundsOfChildren(parent, anEditor.getSelViews());

        // Create guidelines
        createGuidelines(anEditor, parent, bounds, candidates);
    }

    /**
     * Recalculates all the proximity guides and marks dirty region in editor for old & new guide regions.
     * Guides are calculated between the bounds rectangle and each of the candidates, within the parent.
     */
    public static void createGuidelines(Editor anEditor, SGView parent, Rect bounds, List candidateViews)
    {
        // If disabled, just return
        if (!_enabled) return;

        // Empty list and mark old guides dirty
        clearGuidelines(anEditor);

        // If no candidate views, just return
        if (candidateViews==null || candidateViews.isEmpty()) return;

        double minDX = 9999, maxDX = 9999, minDY = 9999, maxDY = 9999;
        SGView minDXminYView=null, minDXmaxYView=null, maxDXminYView=null, maxDXmaxYView=null;
        SGView minDYminXView=null, minDYmaxXView=null, maxDYminXView=null, maxDYmaxXView=null;
        double delta, x1, y1, x2, y2;
        Point p1, p2;

        // Iterate over children to see which is the closest to selViews min/max X
        for (int i=0, iMax=candidateViews.size(); i<iMax; i++) {

            // Get current child
            SGView child = (SGView) candidateViews.get(i);

            delta=Math.abs(child.getFrameX() - bounds.x);
            if (delta < minDX) {
                minDX = delta;
                minDXminYView = minDXmaxYView = child;
            }
            else if (delta == minDX) {
                if (child.getFrameY() < minDXminYView.getFrameY())
                    minDXminYView = child;
                if (child.getFrameMaxY() > minDXmaxYView.getFrameMaxY())
                    minDXmaxYView = child;
            }

            delta = Math.abs(child.getFrameMaxX() - bounds.getMaxX());
            if (delta < maxDX) {
                maxDX = delta;
                maxDXminYView = maxDXmaxYView=child;
            }
            else if (delta == maxDX) {
                if (child.getFrameY() < maxDXminYView.getFrameY())
                    maxDXminYView = child;
                if (child.getFrameMaxY() > maxDXmaxYView.getFrameMaxY())
                    maxDXmaxYView = child;
            }

            delta=Math.abs(child.getFrameY() - bounds.y);
            if (delta < minDY) {
                minDY = delta;
                minDYminXView = minDYmaxXView = child;
            }
            else if (delta==minDY) {
                if (child.getFrameX() < minDYminXView.getFrameX())
                    minDYminXView = child;
                if (child.getFrameMaxX() > minDYmaxXView.getFrameMaxX())
                    minDYmaxXView = child;
            }

            delta = Math.abs(child.getFrameMaxY() - bounds.getMaxY());
            if (delta<maxDY) {
                maxDY = delta;
                maxDYminXView = maxDYmaxXView=child;
            }
            else if (delta==maxDY) {
                if (child.getFrameX() < maxDYminXView.getFrameX())
                    maxDYminXView = child;
                if (child.getFrameMaxX() > maxDYmaxXView.getFrameMaxX())
                    maxDYmaxXView = child;
            }
        }

        // Add any new guides to guidelines list
        if (minDX <= maxDX && minDX < 5) {
            x1 = minDXminYView.getFrameX();
            y1 = Math.min(bounds.y, minDXminYView.getFrameY());
            y2 = Math.max(bounds.getMaxY(), minDXmaxYView.getFrameMaxY());
            p1 = anEditor.convertFromSceneView(x1, y1, parent);
            p2 = anEditor.convertFromSceneView(x1, y2, parent);
            addGuideline(p1, p2);
        }

        if (maxDX <= minDX && maxDX < 5) {
            x1 = maxDXminYView.getFrameMaxX();
            y1 = Math.min(bounds.y, maxDXminYView.getFrameY());
            y2 = Math.max(bounds.getMaxY(), maxDXmaxYView.getFrameMaxY());
            p1 = anEditor.convertFromSceneView(x1, y1, parent);
            p2 = anEditor.convertFromSceneView(x1, y2, parent);
            addGuideline(p1, p2);
        }

        if (minDY <= maxDY && minDY < 5) {
            y1 = minDYminXView.getFrameY();
            x1 = Math.min(bounds.x, minDYminXView.getFrameX());
            x2 = Math.max(bounds.getMaxX(), minDYmaxXView.getFrameMaxX());
            p1 = anEditor.convertFromSceneView(x1, y1, parent);
            p2 = anEditor.convertFromSceneView(x2, y1, parent);
            addGuideline(p1, p2);
        }

        if (maxDY <= minDY && maxDY < 5) {
            y1 = maxDYminXView.getFrameMaxY();
            x1 = Math.min(bounds.x, maxDYminXView.getFrameX());
            x2 = Math.max(bounds.getMaxX(), maxDYmaxXView.getFrameMaxX());
            p1 = anEditor.convertFromSceneView(x1, y1, parent);
            p2 = anEditor.convertFromSceneView(x2, y1, parent);
            addGuideline(p1, p2);
        }

        markGuidelinesDirty(anEditor);
    }

    /**
     * Adds a guideline rect for the given points.
     */
    private static void addGuideline(Point p1, Point p2)  { _guidelineRects.add(Rect.get(p1, p2)); }

    /**
     * Returns the given point snapped to relevant proximity guides.
     */
    public static Point pointSnappedToProximityGuides(Editor anEditor, Point aPoint)
    {
        return pointSnappedToProximityGuides(anEditor,aPoint, anEditor.getSelectTool().getDragMode());
    }

    /**
     * Returns the given point snapped to relevant proxity guides for a given drag mode.
     */
    public static Point pointSnappedToProximityGuides(Editor anEditor, Point aPoint, SelectTool.DragMode aDragMode)
    {
        // If not enabled, just return point
        if (!_enabled) return aPoint;

        // If drag mode is not move or resize, just return point
        if (aDragMode!= SelectTool.DragMode.Move && aDragMode!= SelectTool.DragMode.Resize)
            return aPoint;

        // Get parent
        SGView parent = anEditor.getSuperSelView();

        // If parent is structured table row, just return point (wish this wasn't hard coded)
        //if(parent instanceof RMTableRow && ((RMTableRow)parent).isStructured()) return aPoint;

        // Get list of selected views
        List selViews = anEditor.getSelViews();

        // Get list of candidate views
        List <SGView> candidates = getCandidateViews(anEditor);

        // Declare variable for bounds
        Rect bounds;

        // If mode is move, set bounds to snap the entire bounding box
        if (aDragMode== SelectTool.DragMode.Move)
            bounds = SGViewUtils.getBoundsOfChildren(parent, selViews);

        // If mode is resize, set bounds to just snap a handle
        else {
            bounds = new Rect(aPoint.x, aPoint.y, 0, 0);
            bounds = parent.parentToLocal(bounds, null).getBounds();
        }

        // Declare variables for minDX, maxDX, minDY and maxDY
        double minDX = 9999;
        double maxDX = 9999;
        double minDY = 9999;
        double maxDY = 9999;

        // Declare variables for minDX, maxDX, minDY, maxDY views
        SGView minDXView = null;
        SGView maxDXView = null;
        SGView minDYView = null;
        SGView maxDYView = null;

        // Iterate over children to see which is the closest to selViews min/max X
        for (int i=0, iMax=candidates.size(); i < iMax; i++) {

            // Get current child
            SGView child = (SGView)candidates.get(i);

            double dx1 = Math.abs(child.getFrameX() - bounds.x);
            if (dx1 < minDX) {
                minDX = dx1;
                minDXView = child;
            }

            double dx2 = Math.abs(child.getFrameMaxX() - bounds.getMaxX());
            if (dx2 < maxDX) {
                maxDX = dx2;
                maxDXView = child;
            }

            double dy1 = Math.abs(child.getFrameY() - bounds.y);
            if (dy1 < minDY) {
                minDY = dy1;
                minDYView = child;
            }

            double dy2 = Math.abs(child.getFrameMaxY() - bounds.getMaxY());
            if (dy2 < maxDY) {
                maxDY = dy2;
                maxDYView = child;
            }
        }

        // If
        if (minDX <= maxDX && minDX < 5)
            aPoint.setX(aPoint.x - (bounds.getX() - minDXView.getFrameX()));

        // If
        if (maxDX < minDX && maxDX < 5)
            aPoint.setX(aPoint.x - (bounds.getMaxX() - maxDXView.getFrameMaxX()));

        // If
        if (minDY <= maxDY && minDY < 5)
            aPoint.setY(aPoint.y - (bounds.getY() - minDYView.getFrameY()));

        // If
        if (maxDY < minDY && maxDY < 5)
            aPoint.setY(aPoint.y - (bounds.getMaxY() - maxDYView.getFrameMaxY()));

        // Return point
        return aPoint;
    }
}
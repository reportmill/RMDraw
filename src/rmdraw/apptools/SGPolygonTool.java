/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.apptools;
import rmdraw.app.Editor;
import rmdraw.app.Tool;
import rmdraw.scene.*;
import snap.geom.*;
import snap.gfx.*;
import snap.view.*;

/**
 * This class manages creation and editing of polygons.
 */
public class SGPolygonTool<T extends SGPolygon> extends Tool<T> {

    // The current path being added
    private Path _path;

    // Whether path should be smoothed on mouse up
    private boolean _smoothPathOnMouseUp;

    // Used to determine which path element to start smoothing from
    private int _pointCountOnMouseDown;

    // The point (in path coords) for new control point additions
    private Point _newPoint;

    // The path point handle hit by current mouse down
    private static int _selPointIndex = 0;

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        getView("PathText", TextView.class).setFireActionOnFocusLost(true);
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get current PathView and path
        SGPolygon pview = getSelView();
        Path path = pview.getPath();

        // Update PathText
        setViewText("PathText", path.getString());
    }

    /**
     * Handles the pop-up menu
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Get current PathView and path
        SGPolygon pview = getSelView();

        // Handle PathText
        if (anEvent.equals("PathText")) {
            String str = anEvent.getStringValue();
            Path path = Path.getPathFromSVG(str);
            if (path == null) return;
            pview.resetPath(new Path(path));
        }

        // Handle DeletePointMenuItem
        if (anEvent.equals("DeletePointMenuItem"))
            deleteSelectedPoint();

        // Handle AddPointMenuItem
        if (anEvent.equals("AddPointMenuItem"))
            addNewPointAt(_newPoint);
    }

    /**
     * Returns the class that this tool is responsible for.
     */
    public Class getViewClass()
    {
        return SGPolygon.class;
    }

    /**
     * Returns a new instance of the view class that this tool is responsible for.
     */
    protected T newInstance()
    {
        T view = super.newInstance();
        view.setBorder(Border.blackBorder());
        return view;
    }

    /**
     * Returns whether a given view is super-selectable.
     */
    public boolean isSuperSelectable(SGView aView)
    {
        return true;
    }

    /**
     * Returns whether tool should smooth path segments during creation.
     */
    public boolean getSmoothPath()
    {
        return false;
    }

    /**
     * Handles mouse pressed for polygon creation.
     */
    public void mousePressed(ViewEvent anEvent)
    {
        boolean smoothPath = getSmoothPath();
        if (anEvent.isAltDown()) smoothPath = !smoothPath;
        Point point = getEditorEvents().getEventPointInDoc(!smoothPath);

        // Register all selViews dirty because their handles will probably need to be wiped out
        for (SGView shp : getEditor().getSelViews()) shp.repaint(); // Was views.forEach(i -> i.repaint())

        // If this is the first mouseDown of a new path, create path and add moveTo. Otherwise add lineTo to current path
        if (_path == null) {
            _path = new Path();
            _path.moveTo(point.x, point.y);
        }
        else _path.lineTo(point.x, point.y);

        // Get the value of _shouldSmoothPathOnMouseUp for the mouseDrag and store current pointCount
        _smoothPathOnMouseUp = smoothPath;
        _pointCountOnMouseDown = _path.getPointCount();

        Rect rect = _path.getBounds().getInsetRect(-10);
        rect = getEditor().convertFromSceneView(rect, null).getBounds();
        getEditor().repaint(rect);
    }

    /**
     * Handles mouse dragged for polygon creation.
     */
    public void mouseDragged(ViewEvent anEvent)
    {
        Point point = getEditorEvents().getEventPointInDoc(!_smoothPathOnMouseUp);
        Rect rect = _path.getBounds();

        if (_smoothPathOnMouseUp || _path.getPointCount() == 1) _path.lineTo(point.x, point.y);
        else _path.setPoint(_path.getPointCount() - 1, point.x, point.y);

        rect.union(_path.getBounds());
        rect.inset(-10, -10);
        rect = getEditor().convertFromSceneView(rect, null).getBounds();
        getEditor().repaint(rect);
    }

    /**
     * Handles mouse released for polygon creation.
     */
    public void mouseReleased(ViewEvent anEvent)
    {
        if (_smoothPathOnMouseUp && _pointCountOnMouseDown < _path.getPointCount()) {
            getEditor().repaint();
            _path.fitToCurve(_pointCountOnMouseDown);
        }

        // Check to see if point landed in first point
        if (_path.getPointCount() > 2) {
            Seg lastElmnt = _path.getLastSeg();
            int lastPointIndex = _path.getPointCount() - (lastElmnt == Seg.LineTo ? 2 : 4);
            Point beginPoint = _path.getPoint(0);
            Point lastPoint = _path.getPoint(lastPointIndex);
            Point thisPoint = _path.getLastPoint();
            Rect firstHandleRect = new Rect(beginPoint.x - 3, beginPoint.y - 3, 6f, 6f);
            Rect lastHandleRect = new Rect(lastPoint.x - 3, lastPoint.y - 3, 6f, 6f);
            Rect currentHandleRect = new Rect(thisPoint.x - 3, thisPoint.y - 3, 6f, 6f);
            boolean createPath = DEFAULT_PENCIL_ONESHOT;

            // If mouseUp is in startPoint, create poly and surrender to selectTool
            if (!createPath && currentHandleRect.intersectsRect(firstHandleRect)) {
                if (lastElmnt == Seg.LineTo) _path.removeLastSeg();
                _path.close();
                createPath = true;
            }

            // If mouseUp is in startPoint, create poly and surrender to selectTool
            if (!createPath && currentHandleRect.intersectsRect(lastHandleRect)) {
                if (_path.getLastSeg() == Seg.LineTo) _path.removeLastSeg();
                createPath = true;
            }

            // Create poly, register for redisplay and surrender to selectTool
            if (createPath) {
                createPoly();
                getEditor().repaint();
                getEditor().setCurrentToolToSelectTool();
            }
        }
    }

    /**
     * Event handling - overridden to maintain default cursor.
     */
    public void mouseMoved(T aView, ViewEvent anEvent)
    {
        // Get the mouse down point in view coords
        Point point = getEditor().convertToSceneView(anEvent.getX(), anEvent.getY(), aView);

        // If control point is hit, change cursor to move
        if (handleAtPoint(aView.getPath(), point, _selPointIndex) >= 0) {
            getEditor().setCursor(Cursor.MOVE);
            anEvent.consume();
        }

        // Otherwise, do normal mouse moved
        else super.mouseMoved(aView, anEvent);
    }

    /**
     * Event handling for view editing.
     */
    public void mousePressed(T aView, ViewEvent anEvent)
    {
        // If view isn't super selected, just return
        if (!isSuperSelected(aView)) return;

        // Get mouse down point in view coords (but don't snap to the grid)
        Point point = getEditorEvents().getEventPointInView(false);

        // Register view for repaint
        aView.repaint();

        // check for degenerate path
        if (aView.getPath().getPointCount() < 2)
            _selPointIndex = -1;

            // Otherwise, figure out the size of a handle in path coordinates and set index of path point hit by mouse down
        else {
            int oldSelectedPt = _selPointIndex;
            int hp = handleAtPoint(aView.getPath(), point, oldSelectedPt);
            _selPointIndex = hp;

            if (anEvent.isPopupTrigger()) {
                runContextMenu(aView, anEvent);
                anEvent.consume();
            }
        }

        // Consume event
        anEvent.consume();
    }

    /**
     * Event handling for view editing.
     */
    public void mouseDragged(T aView, ViewEvent anEvent)
    {
        // If not dragging a point, just return
        if (_selPointIndex < 0) return;

        // Repaint, create path with moved point and set new path
        aView.repaint();
        Point point = getEditorEvents().getEventPointInView(true);
        Path path = aView.getPath(), newPath = path.clone();
        setPointStructured(newPath, _selPointIndex, point);
        aView.resetPath(newPath);
    }

    /**
     * Actually creates a new polygon view from the polygon tool's current path.
     */
    private void createPoly()
    {
        if (_path != null && _path.getPointCount() > 2) {
            SGPolygon poly = new SGPolygon();
            Rect polyFrame = getEditor().getSuperSelView().parentToLocal(_path.getBounds(), null).getBounds();
            poly.setFrame(polyFrame);
            poly.setBorder(DEFAULT_BORDER);
            poly.setPath(_path);

            // Add view to superSelView (within an undo grouping).
            setUndoTitle("Add Polygon");
            getEditor().getSuperSelParentView().addChild(poly);

            // Select view
            getEditor().setSelView(poly);
        }

        // Reset path
        _path = null;
    }

    /**
     * Overrides standard tool method to trigger polygon creation when the tool is deactivated.
     */
    public void deactivateTool()
    {
        createPoly();
    }

    /**
     * Overrides standard tool method to trigger polygon creation when the tool is reactivated.
     */
    public void reactivateTool()
    {
        createPoly();
    }

    /**
     * Editor method - called when an instance of this tool's view in de-super-selected.
     */
    public void willLoseSuperSel(T aView)
    {
        super.willLoseSuperSel(aView);
        _selPointIndex = -1;
    }

    /**
     * Draws the polygon tool's path during path creation.
     */
    public void paintTool(Painter aPntr)
    {
        if (_path == null) return;
        aPntr.setColor(DEFAULT_BORDER.getColor());
        aPntr.setStroke(DEFAULT_BORDER.getStroke());
        aPntr.draw(_path);
    }

    /**
     * Handles painting a polygon view.
     */
    public void paintHandles(T aView, Painter aPntr, boolean isSuperSelected)
    {
        // Do normal version (and just return if not super-selected)
        super.paintHandles(aView, aPntr, isSuperSelected);
        if (!isSuperSelected) return;

        // Get plygon path
        Path pathInLocal = aView.getPath();
        Shape shapeInEditor = aView.localToParent(pathInLocal, null);
        Path path = shapeInEditor instanceof Path ? (Path) shapeInEditor : new Path(shapeInEditor);

        // Declare some path iteration variables
        Seg lastElement = null;
        int currentPointIndex = 0;
        Point pnts[] = new Point[3];
        float HW = 6, HHW = HW / 2;

        // Iterate over path segements
        for (int i = 0; i < path.getSegCount(); i++) {
            int pointIndex = path.getSegPointIndex(i);

            // Get points
            pnts[0] = pointIndex < path.getPointCount() ? path.getPoint(pointIndex++) : null;
            pnts[1] = pointIndex < path.getPointCount() ? path.getPoint(pointIndex++) : null;
            pnts[2] = pointIndex < path.getPointCount() ? path.getPoint(pointIndex++) : null;

            // Get segment type and next segment type
            Seg element = path.getSeg(i);
            Seg nextElement = i + 1 < path.getSegCount() ? path.getSeg(i + 1) : null;

            // Set color black for control lines and so alpha is correct for buttons
            aPntr.setColor(Color.BLACK);

            // Draw buttons for all segment endPoints
            switch (element) {

                // Handle MoveTo & LineTo: just draw button
                case MoveTo:
                case LineTo: {
                    Rect hrect = new Rect(pnts[0].x - HHW, pnts[0].y - HHW, HW, HW);
                    aPntr.drawButton(hrect, false);
                    currentPointIndex++;
                    break;
                }

                // Handle CURVE_TO: If selectedPointIndex is CurveTo, draw line to nearest endPoint and button
                case CubicTo: {

                    // If controlPoint1's point index is the selectedPointIndex or last end point was selectedPointIndex
                    // or lastElement was a CurveTo and it's controlPoint2's pointIndex was the selectedPointIndex
                    //   then draw control line from controlPoint1 to last end point and draw handle for control point 1
                    if (currentPointIndex == _selPointIndex || currentPointIndex - 1 == _selPointIndex ||
                            (lastElement == Seg.CubicTo && currentPointIndex - 2 == _selPointIndex)) {
                        Point lastPoint = path.getPoint(currentPointIndex - 1);
                        aPntr.setStroke(Stroke.Stroke1);
                        aPntr.drawLine(pnts[0].getX(), pnts[0].getY(), lastPoint.getX(), lastPoint.getY());
                        aPntr.drawButton(pnts[0].x - HHW, pnts[0].y - HHW, HW, HW, false); // control pnt handle rect
                        aPntr.drawButton(lastPoint.x - HHW, lastPoint.y - HHW, HW, HW, false); // last pnt handle rect
                    }

                    // If controlPoint2's point index is selectedPointIndex or if end point's index is
                    // selectedPointIndex or if next element is CurveTo and it's cp1 point index is
                    // selectedPointIndex then draw control line from cp2 to end point and draw handle for cp2
                    else if (currentPointIndex + 1 == _selPointIndex || currentPointIndex + 2 == _selPointIndex ||
                            (nextElement == Seg.CubicTo && currentPointIndex + 3 == _selPointIndex)) {
                        aPntr.setStroke(Stroke.Stroke1);
                        aPntr.drawLine(pnts[1].getX(), pnts[1].getY(), pnts[2].getX(), pnts[2].getY());
                        aPntr.drawButton(pnts[1].x - HHW, pnts[1].y - HHW, HW, HW, false);
                    }

                    // Draw button
                    Rect hrect = new Rect(pnts[2].x - HHW, pnts[2].y - HHW, HW, HW);
                    aPntr.drawButton(hrect, false);
                    currentPointIndex += 3;
                    break;
                }

                // Break
                default:
                    break;
            }

            // Remember last element
            lastElement = element;
        }
    }

    /**
     * Returns the bounds for this view when it's super-selected.
     */
    public Rect getBoundsSuperSel(T aView)
    {
        Rect bnds = getControlPointBounds(aView.getPath());
        bnds.inset(-3, -3);
        return bnds;
    }

    /**
     * Returns the bounds for all the control points.
     */
    private Rect getControlPointBounds(Path aPath)
    {
        // Get segment index for selected control point handle
        int mouseDownIndex = aPath.getSegIndexForPointIndex(_selPointIndex);
        if (mouseDownIndex >= 0 && aPath.getSeg(mouseDownIndex) == Seg.CubicTo &&
                (aPath.getSegPointIndex(mouseDownIndex) == _selPointIndex))
            mouseDownIndex--;

        // Iterate over path elements
        Point p0 = aPath.getPointCount() > 0 ? new Point(aPath.getPoint(0)) : new Point();
        double p1x = p0.x, p1y = p0.y;
        double p2x = p1x, p2y = p1y;
        PathIter piter = aPath.getPathIter(null);
        double pts[] = new double[6];
        for (int i = 0; piter.hasNext(); i++)
            switch (piter.getNext(pts)) {

                // Handle MoveTo
                case MoveTo:

                    // Handle LineTo
                case LineTo: {
                    p1x = Math.min(p1x, pts[0]);
                    p1y = Math.min(p1y, pts[1]);
                    p2x = Math.max(p2x, pts[0]);
                    p2y = Math.max(p2y, pts[1]);
                }
                break;

                // Handle CubicTo
                case CubicTo: {
                    if ((i - 1) == mouseDownIndex) {
                        p1x = Math.min(p1x, pts[0]);
                        p1y = Math.min(p1y, pts[1]);
                        p2x = Math.max(p2x, pts[0]);
                        p2y = Math.max(p2y, pts[1]);
                    }
                    if (i == mouseDownIndex) {
                        p1x = Math.min(p1x, pts[2]);
                        p1y = Math.min(p1y, pts[3]);
                        p2x = Math.max(p2x, pts[2]);
                        p2y = Math.max(p2y, pts[3]);
                    }
                    p1x = Math.min(p1x, pts[4]);
                    p1y = Math.min(p1y, pts[5]);
                    p2x = Math.max(p2x, pts[4]);
                    p2y = Math.max(p2y, pts[5]);
                }
                break;

                // Handle default
                default:
                    break;
            }

        // Create control point bounds rect, union with path bounds and return
        Rect cpbnds = new Rect(p1x, p1y, Math.max(1, p2x - p1x), Math.max(1, p2y - p1y));
        cpbnds.union(aPath.getBounds());
        return cpbnds;
    }

    /**
     * Runs a context menu for the given event.
     */
    public void runContextMenu(SGPolygon aPoly, ViewEvent anEvent)
    {
        // Get the handle that was clicked on
        Path path = aPoly.getPath();
        int pindex = _selPointIndex;
        String mtitle = null, mname = null;

        // If clicked on a valid handle, add 'delete point' to menu,
        if (pindex >= 0) {
            if (pointOnPath(path, pindex)) { // Only on-path points can be deleted
                mtitle = "Delete Anchor Point";
                mname = "DeletePointMenuItem";
            }
        }

        // Otherwise if the path itself was hit, use 'add point'
        else {
            // Convert event point to view coords
            _newPoint = getEditor().convertToSceneView(anEvent.getX(), anEvent.getY(), aPoly);

            // linewidth is probably in view coords, and might need to get transformed to path coords here
            if (path.intersects(_newPoint.x, _newPoint.y, Math.max(aPoly.getBorderWidth(), 8))) {
                mtitle = "Add Anchor Point";
                mname = "AddPointMenuItem";
            }
        }

        // return if there's nothing to be done
        if (mname == null) return;

        // Create new PopupMenu
        Menu pmenu = new Menu();
        MenuItem mitem = new MenuItem();
        mitem.setText(mtitle);
        mitem.setName(mname);
        pmenu.addItem(mitem);
        pmenu.setOwner(this);
        pmenu.show(anEvent.getView(), anEvent.getX(), anEvent.getY());
    }

    /**
     * Delete the selected control point and readjust view bounds
     */
    public void deleteSelectedPoint()
    {
        // Make changes to a clone of the path so deletions can be undone
        SGPolygon p = getSelView();
        Path path = new Path(p.getPath());

        // get the index of the path segment corresponding to the selected control point
        int sindex = path.getSegIndexForPointIndex(_selPointIndex);

        // mark for repaint & undo
        p.repaint();

        // Delete the point from path in parent coords
        path.removeSeg(sindex);

        // if all points have been removed, delete the view itself
        if (path.getSegCount() == 0) {
            setUndoTitle("Delete Polygon");
            p.getParent().repaint();
            p.removeFromParent();
            getEditor().setSelView(null);
        }

        // otherwise update path and bounds and deselect the deleted point
        else {
            setUndoTitle("Delete Control Point");
            p.resetPath(path);
            _selPointIndex = -1;
        }
    }

    /**
     * Add a point to the curve by subdividing the path segment at the hit point.
     */
    public void addNewPointAt(Point aPoint)
    {
        // Get old path and new path
        SGPolygon poly = getSelView();
        Path path = poly.getPath();
        Path path2 = new Path();

        // Create small horizontal and vertical lines around mouse point
        Line hor = new Line(aPoint.x - 3, aPoint.y, aPoint.x + 3, aPoint.y);
        Line vert = new Line(aPoint.x, aPoint.y - 3, aPoint.x, aPoint.y + 3);

        // Iterate over path and if segment is hit by mouse point, split segment
        PathIter piter = path.getPathIter(null);
        double pts[] = new double[6];
        double mx = 0, my = 0, lx = 0, ly = 0;
        while (piter.hasNext()) switch (piter.getNext(pts)) {

            // Handle MoveTo
            case MoveTo:
                path2.moveTo(mx = lx = pts[0], my = ly = pts[1]);
                break;

            // Handle LineTo
            case LineTo: {
                Line seg = new Line(lx, ly, lx = pts[0], ly = pts[1]), seg2 = null;
                double ix = seg.getHitPoint(hor), iy = seg.getHitPoint(vert);
                if (.1 < ix && ix < .9) seg2 = seg.split(ix);
                else if (.1 < iy && iy < .9) seg2 = seg.split(iy);
                path2.appendSegment(seg);
                if (seg2 != null) path2.appendSegment(seg2);
            }
            break;

            // Handle QuadTo
            case QuadTo: {
                Quad seg = new Quad(lx, ly, pts[0], pts[1], lx = pts[2], ly = pts[3]), seg2 = null;
                double ix = seg.getHitPoint(hor), iy = seg.getHitPoint(vert);
                if (.1 < ix && ix < .9) seg2 = seg.split(ix);
                else if (.1 < iy && iy < .9) seg2 = seg.split(iy);
                path2.appendSegment(seg);
                if (seg2 != null) path2.appendSegment(seg2);
            }
            break;

            // Handle CubicTo
            case CubicTo: {
                Cubic seg = new Cubic(lx, ly, pts[0], pts[1], pts[2], pts[3], lx = pts[4], ly = pts[5]), seg2 = null;
                double ix = seg.getHitPoint(hor), iy = seg.getHitPoint(vert);
                if (.1 < ix && ix < .9) seg2 = seg.split(ix);
                else if (.1 < iy && iy < .9) seg2 = seg.split(iy);
                path2.appendSegment(seg);
                if (seg2 != null) path2.appendSegment(seg2);
            }
            break;

            // Handle Close
            case Close: {
                Line seg = new Line(lx, ly, lx = mx, ly = my), seg2 = null;
                double ix = seg.getHitPoint(hor), iy = seg.getHitPoint(vert);
                if (.1 < ix && ix < .9) seg2 = seg.split(ix);
                else if (.1 < iy && iy < .9) seg2 = seg.split(iy);
                if (seg2 != null) path2.appendSegment(seg);
                path2.close();
            }
            break;
        }

        // If new path differs, set new path
        if (!path2.equals(path))
            poly.resetPath(path2);
    }

    /**
     * Resets the point at the given index to the given point, while preserving something.
     */
    private static void setPointStructured(Path aPath, int index, Point point)
    {
        int elmtIndex = aPath.getSegIndexForPointIndex(index);
        Seg elmt = aPath.getSeg(elmtIndex);

        // If point at index is part of a curveto, perform structured set
        if (elmt == Seg.CubicTo) {
            int pointIndexForElmtIndex = aPath.getSegPointIndex(elmtIndex);

            // If point index is control point 1, and previous element is a curveto, bring control point 2 of previous curveto in line
            if (index - pointIndexForElmtIndex == 0) {
                if (elmtIndex - 1 > 0 && aPath.getSeg(elmtIndex - 1) == Seg.CubicTo) {
                    Point endPoint = aPath.getPoint(index - 1), cntrlPnt2 = aPath.getPoint(index - 2);
                    // endpoint==point winds up putting a NaN in the path
                    if (!endPoint.equals(point)) {
                        Size size = new Size(point.x - endPoint.x, point.y - endPoint.y);
                        size.normalize();
                        size.negate();
                        Size size2 = new Size(cntrlPnt2.x - endPoint.x, cntrlPnt2.y - endPoint.y);
                        double mag = size2.getMagnitude();
                        aPath.setPoint(index - 2, endPoint.x + size.getWidth() * mag, endPoint.y + size.getHeight() * mag);
                    }
                }
            }

            // If point index is control point 2, and next element is a curveto, bring control point 1 of next curveto in line
            else if (index - pointIndexForElmtIndex == 1) {
                if (elmtIndex + 1 < aPath.getSegCount() && aPath.getSeg(elmtIndex + 1) == Seg.CubicTo) {
                    Point endPoint = aPath.getPoint(index + 1), otherControlPoint = aPath.getPoint(index + 2);
                    // don't normalize a point
                    if (!endPoint.equals(point)) {
                        Size size = new Size(point.x - endPoint.x, point.y - endPoint.y);
                        size.normalize();
                        size.negate();
                        Size size2 = new Size(otherControlPoint.x - endPoint.x, otherControlPoint.y - endPoint.y);
                        double mag = size2.getMagnitude();
                        aPath.setPoint(index + 2, endPoint.x + size.width * mag, endPoint.y + size.height * mag);
                    }
                }
            }

            // If point index is curve end point, move the second control point by the same amount as main point move
            else if (index - pointIndexForElmtIndex == 2) {
                Point p1 = new Point(point);
                p1.subtract(aPath.getPoint(index));
                Point p2 = new Point(aPath.getPoint(index - 1));
                p2.add(p1);
                aPath.setPoint(index - 1, p2.x, p2.y);
                if (elmtIndex + 1 < aPath.getSegCount() && aPath.getSeg(elmtIndex + 1) == Seg.CubicTo) {
                    p1 = new Point(point);
                    p1.subtract(aPath.getPoint(index));
                    p2 = new Point(aPath.getPoint(index + 1));
                    p2.add(p1);
                    aPath.setPoint(index + 1, p2.x, p2.y);
                }
            }
        }

        // If there is a next element and it is a curveto, move its first control point by the same amount as main point move
        else if (elmtIndex + 1 < aPath.getSegCount() && aPath.getSeg(elmtIndex + 1) == Seg.CubicTo) {
            Point p1 = new Point(point);
            p1.subtract(aPath.getPoint(index));
            Point p2 = new Point(aPath.getPoint(index + 1));
            p2.add(p1);
            aPath.setPoint(index + 1, p2.x, p2.y);
        }

        // Set point at index to requested point
        aPath.setPoint(index, point.x, point.y);
    }

    /**
     * Returns the handle index for a given point for given path. Only returns points that are on the path,
     * except for the control points of selectedPoint (if not -1)
     */
    private static int handleAtPoint(Path aPath, Point aPoint, int selectedPoint)
    {
        // Check against off-path control points of selected path first, otherwise you might never be able to select one
        if (selectedPoint != -1) {
            int offPathPoints[] = new int[2];
            int noffPathPoints = 0;
            int ecount = aPath.getSegCount();
            int eindex = getSegIndexForPointIndex(aPath, selectedPoint);
            Seg elmt = aPath.getSeg(eindex);

            // If the selected point is one of the on path points, figure out the indices of the others
            if (pointOnPath(aPath, selectedPoint)) {

                // If the selected element is a curveto or quadto, the second to the last control point will be active
                if (elmt == Seg.CubicTo || elmt == Seg.QuadTo)
                    offPathPoints[noffPathPoints++] = selectedPoint - 1;

                // If the element following the selected element is a curveto, it's first control point will be active
                if (eindex < ecount - 1 && aPath.getSeg(eindex + 1) == Seg.CubicTo)
                    offPathPoints[noffPathPoints++] = selectedPoint + 1;
            }

            // If selected point is off-path, add it to list to check and then figure out what other point might be active
            else {
                offPathPoints[noffPathPoints++] = selectedPoint;

                // if selected point is first control point, check previous segment, otherwise check next segment
                if (selectedPoint == aPath.getSegPointIndex(eindex)) {
                    if (eindex > 0 && aPath.getSeg(eindex - 1) == Seg.CubicTo)
                        offPathPoints[noffPathPoints++] = selectedPoint - 2;
                }
                else {
                    if (eindex < ecount - 1 && aPath.getSeg(eindex + 1) == Seg.CubicTo)
                        offPathPoints[noffPathPoints++] = selectedPoint + 2;
                }
            }

            // hit test any selected off-path handles
            for (int i = 0; i < noffPathPoints; ++i)
                if (hitHandle(aPath, aPoint, offPathPoints[i]))
                    return offPathPoints[i];
        }

        // Check the rest of the points, but only ones that are actually on the path
        for (int i = 0, iMax = aPath.getPointCount(); i < iMax; i++)
            if (hitHandle(aPath, aPoint, i) && pointOnPath(aPath, i))
                return i;

        // nothing hit
        return -1;
    }

    /**
     * Hit test the point (in path coords) against a given path point.
     */
    private static boolean hitHandle(Path aPath, Point aPoint, int ptIndex)
    {
        Point p = aPath.getPoint(ptIndex);
        double handleSize = 9;
        Rect br = new Rect(p.x - handleSize / 2, p.y - handleSize / 2, handleSize, handleSize);
        return br.contains(aPoint.x, aPoint.y);
    }

    /**
     * Returns the element index for the given point index.
     */
    private static int getSegIndexForPointIndex(Path aPath, int index)
    {
        // Iterate over segments and increment element index
        int elementIndex = 0;
        for (int pointIndex = 0; pointIndex <= index; elementIndex++)
            switch (aPath.getSeg(elementIndex)) {
                case MoveTo:
                case LineTo:
                    pointIndex++;
                    break;
                case QuadTo:
                    pointIndex += 2;
                    break;
                case CubicTo:
                    pointIndex += 3;
                    break;
                default:
                    break;
            }

        // Return calculated element index
        return elementIndex - 1;
    }

    /**
     * Returns true of the point at pointIndex is on the path, and false if it is on the convex hull.
     */
    private static boolean pointOnPath(Path aPath, int pointIndex)
    {
        int sindex = getSegIndexForPointIndex(aPath, pointIndex);
        int indexInElement = pointIndex - aPath.getSegPointIndex(sindex);

        // Only the last point is actually on the path
        Seg seg = aPath.getSeg(sindex);
        int numPts = seg.getCount();
        return indexInElement == numPts - 1;
    }

    /**
     * This inner class defines a polygon tool subclass for drawing freehand pencil sketches instead.
     */
    public static class PencilTool extends SGPolygonTool {

        /**
         * Creates a new PencilTool.
         */
        public PencilTool(Editor anEd)
        {
            setEditor(anEd);
        }

        /**
         * Overrides polygon tool method to flip default smoothing.
         */
        public boolean getSmoothPath()
        {
            return true;
        }
    }
}
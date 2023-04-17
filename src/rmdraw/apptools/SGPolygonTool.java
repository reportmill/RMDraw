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
    private Path2D _path;

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
        Path2D path = pview.getPath();

        // Update PathText
        setViewText("PathText", path.getSvgString());
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
            Path2D path = new Path2D();
            path.appendSvgString(str);
            pview.setPathAndBounds(new Path2D(path));
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
            _path = new Path2D();
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
            _path.fitToCurveFromPointIndex(_pointCountOnMouseDown);
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
        if (Path2DUtils.handleAtPoint(aView.getPath(), point, _selPointIndex) >= 0) {
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
            int hp = Path2DUtils.handleAtPoint(aView.getPath(), point, oldSelectedPt);
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
        Path2D path = aView.getPath(), newPath = path.clone();
        Path2DUtils.setPointSmoothly(newPath, _selPointIndex, point);
        aView.setPathAndBounds(newPath);
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
    public void paintHandles(T aPoly, Painter aPntr, boolean isSuperSelected)
    {
        // Do normal version (and just return if not super-selected)
        super.paintHandles(aPoly, aPntr, isSuperSelected);
        if (!isSuperSelected)
            return;


        // Get plygon path
        Path2D pathInLocal = aPoly.getPath();
        Shape shapeInEditor = aPoly.localToParent(pathInLocal, null);
        Path2D path = shapeInEditor instanceof Path2D ? (Path2D) shapeInEditor : new Path2D(shapeInEditor);
        Path2DUtils.paintHandles(path, aPntr, _selPointIndex);
    }

    /**
     * Returns the bounds for this view when it's super-selected.
     */
    public Rect getBoundsSuperSel(T aView)
    {
        Rect bnds = Path2DUtils.getControlPointBoundsWithSelPointIndex(aView.getPath(), _selPointIndex);
        bnds.inset(-3, -3);
        return bnds;
    }

    /**
     * Runs a context menu for the given event.
     */
    public void runContextMenu(SGPolygon aPoly, ViewEvent anEvent)
    {
        // Get the handle that was clicked on
        Path2D path = aPoly.getPath();
        int pindex = _selPointIndex;
        String mtitle = null, mname = null;

        // If clicked on a valid handle, add 'delete point' to menu,
        if (pindex >= 0) {
            if (Path2DUtils.isPointOnPath(path, pindex)) { // Only on-path points can be deleted
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
        // Get selected polygon shape and register for repaint
        SGPolygon polygonShape = getSelView();
        polygonShape.repaint();

        // Get path and remove point
        Shape path = polygonShape.getPath();
        Path2D newPath = Path2DUtils.removePointAtIndexSmoothly(path, _selPointIndex);

        // If new path is valid, set in polygon shape
        if (newPath.getSegCount() > 0)  {
            getEditor().undoerSetUndoTitle("Delete Path Point");
            polygonShape.setPathAndBounds(newPath);
            _selPointIndex = -1;
        }

        // Otherwise complain
        else beep();
    }

    /**
     * Add a point to the curve by subdividing the path segment at the hit point.
     */
    public void addNewPointAt(Point aPoint)
    {
        // Get old path and new path
        SGPolygon polygonShape = getSelView();
        Path2D path = polygonShape.getPath();
        Path2D newPath = Path2DUtils.addPathPointAtPoint(path, aPoint);

        // If new path differs, set new path
        if (!newPath.equals(path))
            polygonShape.setPathAndBounds(newPath);
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
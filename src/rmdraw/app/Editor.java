/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.app;
import rmdraw.apptools.*;
import rmdraw.editors.Placer;
import snap.geom.Point;
import snap.geom.Rect;
import snap.geom.Shape;
import rmdraw.scene.*;
import java.util.*;
import snap.gfx.*;
import snap.props.PropChange;
import snap.props.UndoSet;
import snap.props.Undoer;
import snap.util.*;
import snap.view.*;

/**
 * Viewer subclass to support Document/SceneGraph editing.
 */
public class Editor extends Viewer {

    // Whether actually editing
    private boolean _editing = true;

    // Current selected views
    private List<SGView> _selViews = new ArrayList();

    // Current super selected views (all ancestors of selected views)
    private List<SGView> _superSelViews = new ArrayList();

    // An EditorCellStyler to get/set style attributes of current selection
    private EditorStyler _styler = new EditorStyler(this);

    // The default CopyPaster
    private EditorCopyPaster _copyPaster;

    // A helper class to handle drag and drop
    private EditorDragDropper _dragDropper;

    // The select tool
    private SelectTool _selectTool;

    // Map of tool instances by Scene view class
    private Map<Class, Tool> _tools = new HashMap();

    // The current editor tool
    private Tool _currentTool = getSelectTool();

    // The EditorPane - I wish we didn't need this
    private EditorPane _ep;

    // Constants for PropertyChanges
    public static final String CurrentTool_Prop = "CurrentTool";
    public static final String SelViews_Prop = "SelViews";
    public static final String SuperSelView_Prop = "SuperSelView";

    /**
     * Creates a new editor.
     */
    public Editor()
    {
        super();

        // Enable Drag events
        enableEvents(DragEvents);

        // Enable ToolTips so getToolTip gets called and disable FocusKeys so tab doesn't leave editor
        setToolTipEnabled(true);
        setFocusKeysEnabled(false);
    }

    /**
     * Returns the editor pane for this editor, if there is one.
     */
    public EditorPane getEditorPane()
    {
        return _ep != null ? _ep : (_ep = getOwner(EditorPane.class));
    }

    /**
     * Returns whether viewer is really doing editing.
     */
    public boolean isEditing()
    {
        return _editing;
    }

    /**
     * Sets whether viewer is really doing editing.
     */
    public void setEditing(boolean aFlag)
    {
        _editing = aFlag;
    }

    /**
     * Override to do editor things.
     */
    @Override
    public void setDoc(SGDoc aSource)
    {
        // Do normal version
        super.setDoc(aSource);

        // Super-select new doc page
        SGPage page = getDoc().getSelPage();
        setSuperSelView(page);

        // Set new undoer
        if (isEditing())
            getSceneGraph().setUndoer(new Undoer());
    }

    /**
     * Returns the first selected view.
     */
    public SGView getSelView()
    {
        return getSelViewCount() == 0 ? null : getSelView(0);
    }

    /**
     * Selects given view.
     */
    public void setSelView(SGView aView)
    {
        setSelViews(aView == null ? null : Arrays.asList(aView));
    }

    /**
     * Returns the number of selected views.
     */
    public int getSelViewCount()
    {
        return _selViews.size();
    }

    /**
     * Returns the selected view at given index.
     */
    public SGView getSelView(int anIndex)
    {
        return ListUtils.get(_selViews, anIndex);
    }

    /**
     * Returns the selected views list.
     */
    public List<SGView> getSelViews()
    {
        return _selViews;
    }

    /**
     * Selects the views in given list.
     */
    public void setSelViews(List<SGView> theViews)
    {
        // If views already set, just return
        if (ListUtils.equalsId(theViews, _selViews)) return;

        // Request focus in case current focus view has changes
        requestFocus();

        // If views is null or empty super-select the selected page and return
        if (theViews == null || theViews.size() == 0) {
            setSuperSelView(getSelPage());
            return;
        }

        // Get the first view in given views list
        SGView view = theViews.get(0);

        // If views contains superSelViews, superSelect last and return (hidden trick for undoSelectedObjects)
        if (theViews.size() > 1 && view == getDoc()) {
            SGView last = theViews.get(theViews.size() - 1);
            setSuperSelView(last);
            return;
        }

        // Get the view's parent
        SGView viewsParent = view.getParent();

        // If views parent is the document, super select view instead
        if (viewsParent == getDoc()) {
            setSuperSelView(view);
            return;
        }

        // Super select views parent
        setSuperSelView(viewsParent);

        // Add views to selected list
        _selViews.addAll(theViews);

        // Fire PropertyChange
        firePropChange(SelViews_Prop, null, theViews);
    }

    /**
     * Add a view to the selected views list.
     */
    public void addSelView(SGView aView)
    {
        List list = new ArrayList(getSelViews());
        list.add(aView);
        setSelViews(list);
    }

    /**
     * Remove a view from the selected views list.
     */
    public void removeSelView(SGView aView)
    {
        List list = new ArrayList(getSelViews());
        list.remove(aView);
        setSelViews(list);
    }

    /**
     * Returns the first super-selected view.
     */
    public SGView getSuperSelView()
    {
        int ssc = getSuperSelViewCount();
        return ssc != 0 ? getSuperSelView(ssc - 1) : null;
    }

    /**
     * Super select a view.
     */
    public void setSuperSelView(SGView aView)
    {
        // Request focus in case current focus view has changes
        requestFocus();

        // If given view is null, reset to selected page
        SGView view = aView != null ? aView : getSelPage();

        // Unselect selected views
        _selViews.clear();

        // Remove current super-selected views that aren't an ancestor of given view
        while (view != getSuperSelView() && !view.isAncestor(getSuperSelView())) {
            SGView ssView = getSuperSelView();
            getToolForView(ssView).willLoseSuperSel(ssView);
            ListUtils.removeLast(_superSelViews);
        }

        // Add super selected view (recursively adds parents if missing)
        if (view != getSuperSelView())
            addSuperSelView(view);

        // Fire PropertyChange and repaint
        firePropChange(SuperSelView_Prop, null, aView);
        repaint();
    }

    /**
     * Adds a super selected view.
     */
    private void addSuperSelView(SGView aView)
    {
        // If parent isn't super selected, add parent first
        if (aView.getParent() != null && !isSuperSelected(aView.getParent()))
            addSuperSelView(aView.getParent());

        // Add ancestor to super selected list
        _superSelViews.add(aView);

        // Notify tool
        getToolForView(aView).didBecomeSuperSel(aView);

        // If ancestor is page but not document's selected page, make it the selected page
        if (aView instanceof SGPage && aView != getDoc().getSelPage())
            getDoc().setSelPage((SGPage) aView);
    }

    /**
     * Returns the first super selected view, if parent view.
     */
    public SGParent getSuperSelParentView()
    {
        SGView ss = getSuperSelView();
        return ss instanceof SGParent ? (SGParent) ss : null;
    }

    /**
     * Returns whether a given view is selected in the editor.
     */
    public boolean isSelected(SGView aView)
    {
        return ListUtils.containsId(_selViews, aView);
    }

    /**
     * Returns whether a given view is super-selected in the editor.
     */
    public boolean isSuperSelected(SGView aView)
    {
        return ListUtils.containsId(_superSelViews, aView);
    }

    /**
     * Returns the number of super-selected views.
     */
    public int getSuperSelViewCount()
    {
        return _superSelViews.size();
    }

    /**
     * Returns the super-selected view at the given index.
     */
    public SGView getSuperSelView(int anIndex)
    {
        return _superSelViews.get(anIndex);
    }

    /**
     * Returns the super selected view list.
     */
    public List<SGView> getSuperSelViews()
    {
        return _superSelViews;
    }

    /**
     * Returns the number of currently selected views or simply 1, if a view is super-selected.
     */
    public int getSelOrSuperSelViewCount()
    {
        int sc = getSelViewCount();
        return sc > 0 ? sc : 1;
    }

    /**
     * Returns the currently selected view at the given index, or the super-selected view.
     */
    public SGView getSelOrSuperSelView(int anIndex)
    {
        return getSelViewCount() > 0 ? getSelView(anIndex) : getSuperSelView();
    }

    /**
     * Returns the currently selected view or, if none, the super-selected view.
     */
    public SGView getSelOrSuperSelView()
    {
        return getSelViewCount() > 0 ? getSelView() : getSuperSelView();
    }

    /**
     * Returns the currently selected views or, if none, the super-selected view in a list.
     */
    public List<SGView> getSelOrSuperSelViews()
    {
        return getSelViewCount() > 0 ? _selViews : Arrays.asList(getSuperSelView());
    }

    /**
     * Un-SuperSelect currently super selected view.
     */
    public void popSelection()
    {
        // If there is a selected view, just super-select parent (clear selected views)
        SGView selView = getSelView();
        if (selView != null && selView.getParent() != null) {
            setSuperSelView(selView.getParent());
            return;
        }

        // Otherwise select super-selected view (or its parent if it has childrenSuperSelectImmediately)
        if (getSuperSelViewCount() > 1) {
            SGView superSelView = getSuperSelView();
            if (superSelView instanceof SGText)
                setSelView(superSelView);
            else if (superSelView.getParent().childrenSuperSelectImmediately())
                setSuperSelView(superSelView.getParent());
            else setSelView(superSelView);
        }

        // Otherwise, beep
        else beep();
    }

    /**
     * Override to account for selected views potentially having different bounds.
     */
    protected Rect getRepaintBoundsForSceneView(SGView aView)
    {
        // Do normal version
        Rect bnds = super.getRepaintBoundsForSceneView(aView);

        // If view is selected, correct for handles
        if (isSelected(aView))
            bnds.inset(-4, -4);

            // If view is super-selected, correct for handles
        else if (isSuperSelected(aView)) {
            bnds = getToolForView(aView).getBoundsSuperSel(aView);
            bnds.inset(-16, -16);
        }

        // Return bounds
        return bnds;
    }

    /**
     * Returns first view hit by point given in View coords.
     */
    public SGView getViewAtPoint(double aX, double aY)
    {
        return getViewAtPoint(new Point(aX, aY));
    }

    /**
     * Returns first view hit by point given in View coords.
     */
    public SGView getViewAtPoint(Point aPoint)
    {
        // Get superSelView
        SGView superSelView = getSuperSelView();

        // If superSelView is document, start with page instead (maybe should go)
        if (superSelView == getDoc())
            superSelView = getSelPage();

        // Get the point in superSelView's coords
        Point point = convertToSceneView(aPoint.x, aPoint.y, superSelView);

        // Get child of superSelView hit by point
        SGView viewAtPoint = getChildViewAtPoint(superSelView, point);

        // If no superSelView child hit by point, find first superSelView that is hit & set to viewAtPoint
        while (superSelView != getDoc() && viewAtPoint == null) {
            point = superSelView.localToParent(point);
            superSelView = superSelView.getParent();
            viewAtPoint = getChildViewAtPoint(superSelView, point);
        }

        // See if point really hits an upper level view that overlaps viewAtPoint
        if (viewAtPoint != null && viewAtPoint != getSelPage()) {

            // Declare view/point variables used to iterate up view hierarchy
            SGView ssView = viewAtPoint;
            Point pnt = point;

            // Iterate up view hierarchy
            while (ssView != getSelPage() && ssView.getParent() != null) {

                // Get child of parent hit point point
                SGView hitChild = getChildViewAtPoint(ssView.getParent(), pnt);

                // If child not equal to original view, change viewAtPoint
                if (hitChild != ssView) {
                    viewAtPoint = hitChild;
                }

                // Update loop view/point variables
                ssView = ssView.getParent();
                pnt = ssView.localToParent(pnt);
            }
        }

        // Make sure page is worst case
        if (viewAtPoint == null || viewAtPoint == getDoc())
            viewAtPoint = getSelPage();

        // Return view at point
        return viewAtPoint;
    }

    /**
     * Returns the child of the given view hit by the given point.
     */
    public SGView getChildViewAtPoint(SGView aView, Point aPoint)
    {
        // If given view is null, return null
        if (aView == null) return null;

        // Iterate over view children
        for (int i = aView.getChildCount(); i > 0; i--) {
            SGView child = aView.getChild(i - 1);

            // If not hittable, continue
            if (!child.isHittable()) continue;

            // Get given point in child view coords
            Point point = child.parentToLocal(aPoint);

            // If child is super selected and point is in child super selected bounds, return child
            if (isSuperSelected(child) && getToolForView(child).getBoundsSuperSel(child).contains(point))
                return child;

                // If child isn't super selected and contains point, return child
            else if (child.contains(point))
                return child;
        }

        // Return null if no children hit by point
        return null;
    }

    /**
     * Returns the first SuperSelView that accepts children.
     */
    public SGParent firstSuperSelViewThatAcceptsChildren()
    {
        // Get super selected view
        SGView view = getSuperSelView();
        SGParent parent = view instanceof SGParent ? (SGParent) view : view.getParent();

        // Iterate up hierarchy until we find a view that acceptsChildren
        while (!getToolForView(parent).getAcceptsChildren(parent))
            parent = parent.getParent();

        // Make sure page is worst case
        if (parent == getDoc())
            parent = getSelPage();

        // Return parent
        return parent;
    }

    /**
     * Returns the first SuperSelected view that accepts children at a given point.
     */
    public SGView firstSuperSelViewThatAcceptsChildrenAtPoint(Point aPoint)
    {
        // Go up chain of superSelViews until one acceptsChildren and is hit by aPoint
        SGView view = getSuperSelView();
        SGParent parent = view instanceof SGParent ? (SGParent) view : view.getParent();

        // Iterate up view hierarchy until we find a view that is hit and accepts children
        while (!getToolForView(parent).getAcceptsChildren(parent) ||
                !parent.contains(parent.parentToLocal(aPoint, null))) {

            // If view childrenSuperSelImmd and view hitByPt, see if any view children qualify (otherwise use parent)
            if (parent.childrenSuperSelectImmediately() && parent.contains(parent.parentToLocal(aPoint, null))) {
                SGView child = parent.getChildContaining(parent.parentToLocal(aPoint, null));
                if (child != null && getToolForView(child).getAcceptsChildren(child))
                    parent = (SGParent) child;
                else parent = parent.getParent();
            }

            // If view's children don't superSelectImmediately or it is not hit by aPoint, just go up parent chain
            else parent = parent.getParent();

            if (parent == null)
                return getSelPage();
        }

        // Make sure page is worst case
        if (parent == getDoc())
            parent = getSelPage();

        // Return view
        return parent;
    }

    /**
     * Returns a Placer to handle setting placement attributes for editor selection.
     */
    public Placer getPlacer()
    {
        return new EditorPlacer(this);
    }

    /**
     * Returns an EditorCellStyler that can get/set style attributes of current selection.
     */
    public EditorStyler getStyler()
    {
        return _styler;
    }

    /**
     * Returns the editor copy/paster.
     */
    public CopyPaster getCopyPaster()
    {
        Tool tool = getToolForViews(getSelOrSuperSelViews());
        return tool.getCopyPaster();
    }

    /**
     * Returns the editor copy/paster.
     */
    public EditorCopyPaster getCopyPasterDefault()
    {
        if (_copyPaster != null) return _copyPaster;
        return _copyPaster = new EditorCopyPaster(this);
    }

    /**
     * Standard clipboard cut functionality.
     */
    public void cut()
    {
        getCopyPaster().cut();
    }

    /**
     * Standard clipboard copy functionality.
     */
    public void copy()
    {
        getCopyPaster().copy();
    }

    /**
     * Standard clipbard paste functionality.
     */
    public void paste()
    {
        getCopyPaster().paste();
    }

    /**
     * Causes all the children of the current super selected view to become selected.
     */
    public void selectAll()
    {
        getCopyPaster().selectAll();
    }

    /**
     * Deletes all the currently selected views.
     */
    public void delete()
    {
        getCopyPaster().delete();
    }

    /**
     * Returns the DragDropper implementation that handles response to drag operations.
     */
    public EditorDragDropper getDragDropper()
    {
        if (_dragDropper != null) return _dragDropper;
        return _dragDropper = createDragDropper();
    }

    /**
     * Creates the DragDropper that handles drag ops.
     */
    protected EditorDragDropper createDragDropper()
    {
        return new EditorDragDropper(this);
    }

    /**
     * Adds a page to the document after current page.
     */
    public void addPage()
    {
        addPage(null, getSelPageIndex() + 1);
    }

    /**
     * Adds a page to the document before current page.
     */
    public void addPagePrevious()
    {
        addPage(null, getSelPageIndex());
    }

    /**
     * Adds a given page to the current document at the given index.
     */
    public void addPage(SGPage aPage, int anIndex)
    {
        SGDoc doc = getDoc();
        if (doc == null) {
            beep();
            return;
        }
        SGPage page = aPage != null ? aPage : doc.createPage();
        doc.addPage(page, anIndex);
        setSelPageIndex(anIndex);
    }

    /**
     * Removes current page from document.
     */
    public void removePage()
    {
        SGDoc doc = getDoc();
        if (doc == null || doc.getPageCount() <= 1) {
            beep();
            return;
        }
        removePage(getSelPageIndex());
    }

    /**
     * Removes the document page at the given index.
     */
    public void removePage(int anIndex)
    {
        // Register for Undo, remove page and set page to previous one
        SGDoc doc = getDoc();
        if (doc == null) {
            beep();
            return;
        }
        undoerSetUndoTitle("Remove Page");
        doc.removePage(anIndex);
        setSelPageIndex(Math.min(anIndex, doc.getPageCount() - 1));
    }

    /**
     * Returns the SelectTool.
     */
    public SelectTool getSelectTool()
    {
        if (_selectTool != null) return _selectTool;
        _selectTool = new SelectTool();
        _selectTool.setEditor(this);
        return _selectTool;
    }

    /**
     * Returns the specific tool for given view.
     */
    public Tool getToolForView(SGView aView)
    {
        Class cls = aView.getClass();
        return getToolForClass(cls);
    }

    /**
     * Returns the specific tool for a list of views (if they are of common class).
     */
    public Tool getToolForViews(List<SGView> aList)
    {
        Class commonClass = ClassUtils.getCommonClass(aList);
        return getToolForClass(commonClass);
    }

    /**
     * Returns the specific tool for a given view.
     */
    public Tool getToolForClass(Class aClass)
    {
        // Get tool from tools map - if not there, find and set
        Tool tool = _tools.get(aClass);
        if (tool == null) {
            _tools.put(aClass, tool = createToolForClass(aClass));
            tool.setEditor(this);
        }
        return tool;
    }

    /**
     * Returns the specific tool for a given view class.
     */
    protected Tool createToolForClass(Class aClass)
    {
        if (aClass == SGDoc.class) return new SGDocTool();
        if (aClass == SGImage.class) return new SGImageTool();
        if (aClass == SGLine.class) return new SGLineTool();
        if (aClass == SGLinkedText.class) return new TextTool();
        if (aClass == SGOval.class) return new SGOvalTool();
        if (aClass == SGPage.class) return new SGPageTool();
        if (aClass == SGParent.class) return new SGParentTool();
        if (aClass == SGPolygon.class) return new SGPolygonTool();
        if (aClass == SGRect.class) return new SGRectTool();
        if (aClass == SGScene3D.class) return new SGScene3DTool();
        if (aClass == SGView.class) return new Tool();
        if (aClass == SGSpringsView.class) return new SGSpringsTool();
        if (aClass == SGText.class) return new TextTool();
        if (aClass == SceneGraph.class) return new Tool();
        System.out.println("RMTool.createTool: " + aClass.getName());
        return new Tool();
    }

    /**
     * Tool method - returns the currently selected tool.
     */
    public Tool getCurrentTool()
    {
        return _currentTool;
    }

    /**
     * Tool method - sets the currently select tool to the given tool.
     */
    public void setCurrentTool(Tool aTool)
    {
        // If tool is already current tool, just reactivate and return
        if (aTool == _currentTool) {
            aTool.reactivateTool();
            return;
        }

        // Deactivate current tool and reset to new tool
        _currentTool.deactivateTool();

        // Set new current tool
        firePropChange(CurrentTool_Prop, _currentTool, _currentTool = aTool);

        // Activate new tool and have editor repaint
        _currentTool.activateTool();

        // Repaint editor
        repaint();
    }

    /**
     * Returns whether the select tool is currently selected.
     */
    public boolean isCurrentToolSelectTool()
    {
        return _currentTool == getSelectTool();
    }

    /**
     * Sets the current tool to the select tool.
     */
    public void setCurrentToolToSelectTool()
    {
        if (getCurrentTool() != getSelectTool())
            setCurrentTool(getSelectTool());
    }

    /**
     * Tool method - Returns whether the select tool is currently selected and if it's currently being used to select.
     */
    public boolean isCurrentToolSelectToolAndSelecting()
    {
        return isCurrentToolSelectTool() && getSelectTool().getDragMode() == SelectTool.DragMode.Select;
    }

    /** Resets the currently selected tool. */
    //public void resetCurrentTool() { _currentTool.deactivateTool(); _currentTool.activateTool(); }

    /**
     * Override viewer method to reset selected views on page change.
     */
    public void setSelPageIndex(int anIndex)
    {
        super.setSelPageIndex(anIndex); // Do normal version
        setSuperSelView(getSelPage()); // Super-select new page
    }

    /**
     * Scrolls selected views to visible.
     */
    protected Rect getSelViewsBounds()
    {
        // Get selected/super-selected view(s) and parent (just return if parent is null or document)
        List<? extends SGView> views = getSelOrSuperSelViews();
        SGView parent = views.get(0).getParent();
        if (parent == null || parent instanceof SGDoc)
            return getDocBounds();

        // Get select views rect in viewer coords and return
        Rect sbounds = SGViewUtils.getBoundsOfChildren(parent, views);
        sbounds = convertFromSceneView(sbounds, parent).getBounds();
        return sbounds;
    }

    /**
     * Override to have zoom focus on selected views rect.
     */
    public Rect getZoomFocusRect()
    {
        Rect sbounds = getSelViewsBounds();
        Rect vrect = getVisRect();
        sbounds.inset((sbounds.getWidth() - vrect.getWidth()) / 2, (sbounds.getHeight() - vrect.getHeight()) / 2);
        return sbounds;
    }

    /**
     * Overrides Viewer implementation to paint tool extras, guides.
     */
    public void paintFront(Painter aPntr)
    {
        // Do normal paint
        super.paintFront(aPntr);

        // Have current tool paintTool (paints selected view handles by default)
        Tool tool = getCurrentTool();
        tool.paintTool(aPntr);

        // Paint proximity guides
        EditorProxGuide.paintProximityGuides(this, aPntr);

        // Paint DragShape, if set
        Shape dragShape = getDragDropper().getDragShape();
        if (dragShape != null) {
            aPntr.setColor(new Color(0, .6, 1, .5));
            aPntr.setStrokeWidth(3);
            aPntr.draw(dragShape);
        }
    }

    /**
     * Override to return as EditorInteractor.
     */
    public EditorInteractor getInteractor()
    {
        return (EditorInteractor) super.getInteractor();
    }

    /**
     * Override to return EditorInteractor.
     */
    public ViewerInteractor createInteractor()
    {
        return new EditorInteractor(this);
    }

    /**
     * Override to revalidate when ideal size changes.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Do normal version
        super.processEvent(anEvent);

        // Handle DragEvent
        if (anEvent.isDragEvent())
            DragDropper.dispatchDragEvent(getDragDropper(), anEvent);

            // See if zoom needs to be reset for any input events
        else if (anEvent.isMouseDrag() || anEvent.isMouseRelease() || anEvent.isKeyRelease()) {

            // If zoom to factor, revalidate when preferred size changes
            if (isZoomToFactor()) {
                if (!getSize().equals(getPrefSize()))
                    relayout();
                if (!getVisRect().contains(getSelViewsBounds()) &&
                        getSelectTool().getDragMode() == SelectTool.DragMode.Move)
                    scrollToVisible(getSelViewsBounds());
            }

            // If zoom to fit, update zoom to fit factor (just returns if unchanged)
            else setZoomToFitFactor();
        }
    }

    /**
     * Returns a tool tip string by asking deepest view's tool.
     */
    public String getToolTip(ViewEvent anEvent)
    {
        // If not editing, do normal get tool tip text
        if (!isEditing()) return super.getToolTip(anEvent);

        // Get deepest view under point (just return if null), get tool and return tool's ToolTip for view
        SGView view = getViewAtPoint(anEvent.getX(), anEvent.getY(), true);
        if (view == null) return null;
        Tool tool = getToolForView(view);
        return tool.getToolTip(view, anEvent);
    }

    /**
     * Resets the editor pane later.
     */
    public void resetEditorPaneLater()
    {
        EditorPane ep = getEditorPane();
        ep.resetLater();
    }

    /**
     * Resets the editor pane later.
     */
    public void resetEditorPaneOnMouseUp()
    {
        EditorPane ep = getEditorPane();
        ViewUtils.runOnMouseUp(() -> ep.resetLater());
    }

    /**
     * Called to undo the last edit operation in the editor.
     */
    public void undo()
    {
        // If undoer exists, do undo, select views and repaint
        if (getUndoer() != null && getUndoer().getUndoSetLast() != null) {
            UndoSet undoSet = getUndoer().undo();
            setUndoSelection(undoSet.getUndoSelection());
            repaint();
        }

        // Otherwise beep
        else beep();
    }

    /**
     * Called to redo the last undo operation in the editor.
     */
    public void redo()
    {
        // If undoer exists, do undo, select views and repaint
        if (getUndoer() != null && getUndoer().getRedoSetLast() != null) {
            UndoSet redoSet = getUndoer().redo();
            setUndoSelection(redoSet.getRedoSelection());
            repaint();
        }

        // Otherwise beep
        else beep();
    }

    /**
     * Sets the undo selection.
     */
    protected void setUndoSelection(Object aSelection)
    {
        // Handle List
        if (aSelection instanceof List)
            setSelViews((List) aSelection);
    }

    /**
     * SceneGraph.Client method: Called to see if client wants deep changes.
     */
    @Override
    public boolean isSceneDeepChangeListener()
    {
        return true;
    }

    /**
     * SceneGraph.Client method: Called when SceneGraph View has prop change.
     */
    @Override
    public void sceneViewPropChangedDeep(PropChange aPC)
    {
        // If deep change for EditorTextEditor, just return since it registers Undo itself (with better coalesce)
        //if(getTextEditor()!=null && getTextEditor().getTextShape()==aShape &&
        //    (anEvent.getSource() instanceof RichText || anEvent.getSource() instanceof RichTextRun)) return;

        // Add undo change
        addUndoChange(aPC);

        // Reset EditorPane UI
        resetEditorPaneLater();
    }

    /**
     * SceneGraph.Client method: Returns whether SceneGraph is being edited.
     */
    public boolean isSceneEditing()
    {
        return isEditing();
    }

    /**
     * SceneGraph.Client method: Returns whether given view is selected.
     */
    public boolean isSceneSelected(SGView aView)
    {
        return isSelected(aView);
    }

    /**
     * SceneGraph.Client method: Returns whether given view is super selected.
     */
    public boolean isSceneSuperSelected(SGView aView)
    {
        return isSuperSelected(aView);
    }

    /**
     * SceneGraph.Client method: Returns whether given view is THE super selected view.
     */
    public boolean isSceneSuperSelectedLeaf(SGView aView)
    {
        return getSuperSelView() == aView;
    }

    /**
     * Property change.
     */
    protected void addUndoChange(PropChange aPC)
    {
        // Get undoer (just return if null)
        Undoer undoer = getUndoer();
        if (undoer == null) return;

        // Handle some changes special
        String pname = aPC.getPropName();
        if (pname.equals("StyleProxy")) { // For RMGraph
            resetEditorPaneOnMouseUp();
            return;
        }

        // If no undos and change is Doc.SelectedPage or RMTableGroup.MainTable, just return
        if (!undoer.hasUndos()) {
            if (pname == SGDoc.SelPageIndex_Prop) return;
            if (pname == "MainTable") return;
            if (pname == "Version") return;
        }

        // If no changes yet, set selected objects
        if (undoer.getActiveUndoSet().getChangeCount() == 0)
            undoer.setUndoSelection(new ArrayList(getSelOrSuperSelViews()));

        // Add property change
        undoer.addPropChange(aPC);

        // Save UndoerChanges after delay
        saveUndoerChangesLater();
    }

    /**
     * Saves Undo Changes.
     */
    protected void saveUndoerChanges()
    {
        // Get undoer
        Undoer undoer = getUndoer();
        if (undoer == null || !undoer.isEnabled()) return;

        // Set undo selected views
        List views = getSelViewCount() > 0 ? getSelViews() : getSuperSelViews();
        if (undoer.getRedoSelection() == null)
            undoer.setRedoSelection(new ArrayList(views));

        // Save undo changes
        undoer.saveChanges();

        // Reset EditorPane
        resetEditorPaneLater();
    }

    /**
     * Saves undo changes after a delay.
     */
    protected void saveUndoerChangesLater()
    {
        // If runnable already set, just return
        if (_saveChangesRun != null) return;
        _saveChangesRun = _scrShared;

        // If MouseDown, run on mouse up, otherwise run later
        if (ViewUtils.isMouseDown()) ViewUtils.runOnMouseUp(_saveChangesRun);
        else getEnv().runLater(_saveChangesRun);
    }

    // A Runnable for runLater(saveUndoerChanges())
    private Runnable _saveChangesRun, _scrShared = () -> {
        saveUndoerChanges();
        _saveChangesRun = null;
    };

    /**
     * Play beep.
     */
    public void beep()
    {
        ViewUtils.beep();
    }
}
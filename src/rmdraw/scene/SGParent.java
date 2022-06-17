/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.scene;
import java.util.ArrayList;
import java.util.List;
import snap.geom.Point;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.props.DeepChangeListener;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.util.*;

/**
 * A Scene View implementation that can have children.
 */
public class SGParent extends SGView implements PropChange.DoChange {

    // The children of this view
    List <SGView> _children = new ArrayList();
    
    // Whether children need layout
    boolean        _needsLayout, _needsLayoutDeep;
    
    // Whether layout is in the process of being done
    boolean        _inLayout, _inLayoutDeep;

    // A listener to catch child PropChange (for editor undo)
    PropChangeListener _childPCL;
    
    // A listener to catch child DeepChange (for editor undo)
    DeepChangeListener _childDCL;
    
    // Constants for properties
    public static final String Child_Prop = "Child";

    /**
     * Returns the number of children associated with this view.
     */
    public int getChildCount()  { return _children.size(); }

    /**
     * Returns the child at the given index.
     */
    public SGView getChild(int anIndex)  { return _children.get(anIndex); }

    /**
     * Returns the list of children associated with this view.
     */
    public List <SGView> getChildren()  { return _children; }

    /**
     * Adds the given child to the end of this view's children list.
     */
    public final void addChild(SGView aChild)  { addChild(aChild, getChildCount()); }

    /**
     * Adds the given child to this view's children list at the given index.
     */
    public void addChild(SGView aChild, int anIndex)
    {
        // If child already has parent, remove from parent
        if (aChild._parent!=null && aChild._parent!=this)
            aChild._parent.removeChild(aChild);

        // Add child to children list and set child's parent to this view
        aChild.setParent(this);
        _children.add(anIndex, aChild);

        // If this view has PropChangeListeners, start listening to children as well
        if (_childPCL!=null) {
            aChild.addPropChangeListener(_childPCL);
            aChild.addDeepChangeListener(_childDCL);
        }

        // Fire property change
        firePropChange(Child_Prop, null, aChild, anIndex);

        // Register to layout this view and parents and repaint
        relayout(); relayoutParent(); repaint(); setNeedsLayoutDeep(true);
    }

    /**
     * Remove's the child at the given index from this view's children list.
     */
    public SGView removeChild(int anIndex)
    {
        // Remove child from children list and clear parent
        SGView child = _children.remove(anIndex);
        child.setParent(null);

        // If this view has child prop listeners, clear from child
        if (_childPCL!=null) {
            child.removePropChangeListener(_childPCL);
            child.removeDeepChangeListener(_childDCL);
        }

        // Register to layout this view and parents and repaint
        relayout(); relayoutParent(); repaint();

        // Fire property change and return
        firePropChange(Child_Prop, child, null, anIndex);
        return child;
    }

    /**
     * Removes the given child from this view's children list.
     */
    public int removeChild(SGView aChild)
    {
        int index = indexOfChild(aChild);
        if(index>=0) removeChild(index);
        return index;
    }

    /**
     * Returns the index of the given child in this view's children list.
     */
    public int indexOfChild(SGView aChild)  { return ListUtils.indexOfId(_children, aChild); }

    /**
     * Returns the last child of this view.
     */
    public SGView getChildLast()  { return getChildCount()>0? getChild(getChildCount()-1) : null; }

    /**
     * Returns a copy of the children as an array.
     */
    public SGView[] getChildArray()  { return _children.toArray(new SGView[getChildCount()]); }

    /**
     * Removes all children from this view (in reverse order).
     */
    public void removeChildren()  { for(int i=getChildCount()-1; i>=0; i--) removeChild(i); }

    /**
     * Returns bounds of all children of this view, which can sometimes differ from this views bounds.
     */
    public Rect getBoundsOfChildren()
    {
        // Iterate over (visible) children and union child frames
        Rect rect = null;
        for (int i=0, iMax=getChildCount(); i<iMax; i++) { SGView child = getChild(i);
            if(!child.isVisible()) continue;
            if(rect==null) rect = child.getFrame();
            else rect.unionEvenIfEmpty(child.getFrame());
        }

        // Return frame (or bounds inside if null)
        return rect!=null? rect : getBoundsLocal();
    }

    /**
     * Returns first child found with the given name (called recursively on children if not found at current level).
     */
    public SGView getChildWithName(String aName)
    {
        // Iterate over children to see if any match given name
        for (int i=0, iMax=getChildCount(); i<iMax; i++) { SGView child = getChild(i);
            if (aName.equals(child.getName()))
                return child; }

        // Iterate over children and forward call to them
        for (int i=0, iMax=getChildCount(); i<iMax; i++) { SGView child = getChild(i);
            if (child instanceof SGParent && ((child = ((SGParent)child).getChildWithName(aName)) != null))
                return child; }

        // Return null since no child of given name was found
        return null;
    }

    /**
     * Returns first child found with the given class (called recursively on children if not found at current level).
     */
    public <T> T getChildWithClass(Class<T> aClass)
    {
        // Iterate over children to see if any match given class
        for (int i=0, iMax=getChildCount(); i<iMax; i++) { SGView child = getChild(i);
            if (aClass.isInstance(child))
                return (T)child; }

        // Iterate over children and forward call to them
        for (int i=0, iMax=getChildCount(); i<iMax; i++) { Object child = getChild(i);
            if (child instanceof SGParent && ((child=((SGParent)child).getChildWithClass(aClass)) != null))
                return (T)child; }

        // Return null since no child of given class was found
        return null;
    }

    /**
     * Returns all the views in view hierarchy of a particular class.
     */
    public <T extends SGView> List<T> getChildrenWithClass(Class<T> aClass)
    {
        return getChildrenWithClass(aClass, new ArrayList());
    }

    /**
     * Adds all the views in view hierarchy of a particular class to the list.
     * Returns the list as a convenience.
     */
    public <T extends SGView> List<T> getChildrenWithClass(Class<T> aClass, List aList)
    {
        // Iterate over children and add children with class
        for (int i=0, iMax=getChildCount(); i<iMax; i++) {  SGView child = getChild(i);
            if (aClass.isInstance(child))
                aList.add(child);
            else if (child instanceof SGParent)
                ((SGParent)child).getChildrenWithClass(aClass, aList);
        }

        // Return list
        return aList;
    }

    /**
     * Override to add change listener to children on first call.
     */
    public void addDeepChangeListener(DeepChangeListener aLsnr)
    {
        // Do normal version
        super.addDeepChangeListener(aLsnr);

        // If child listeners not yet set, create/add for children
        if (_childPCL==null) {
            _childPCL = pc -> childDidPropChange(pc);
            _childDCL = (lsnr,pc) -> childDidDeepChange(lsnr,pc);
            for (SGView child : getChildren()) {
                child.addPropChangeListener(_childPCL);
                child.addDeepChangeListener(_childDCL);
            }
        }
    }

    /**
     * Override to remove this view as change listener to children when not needed.
     */
    public void removeDeepChangeListener(DeepChangeListener aDCL)
    {
        // Do normal version
        super.removeDeepChangeListener(aDCL);

        // If no more deep listeners, remove
        if (!_pcs.hasDeepListener() && _childPCL!=null) {
            for (SGView child : getChildren()) {
                child.removePropChangeListener(_childPCL);
                child.removeDeepChangeListener(_childDCL);
            }
            _childPCL = null; _childDCL = null;
        }
    }

    /**
     * Property change listener implementation to forward changes on to deep listeners.
     */
    void childDidPropChange(PropChange aPC)
    {
        _pcs.fireDeepChange(this, aPC);
    }

    /**
     * Deep property change listener implementation to forward deep changes to deep listeners.
     */
    void childDidDeepChange(Object aLsnr, PropChange aPC)
    {
        _pcs.fireDeepChange(aLsnr, aPC);
    }

    /**
     * Sets view layout to invalid and requests deferred layout.
     */
    public void relayout()  { setNeedsLayout(true); }

    /**
     * Returns whether children need to be laid out.
     */
    public boolean isNeedsLayout()  { return _needsLayout; }

    /**
     * Sets whether children need to be laid out.
     */
    protected void setNeedsLayout(boolean aValue)
    {
        if (aValue==_needsLayout || _inLayout) return;
        _needsLayout = aValue;
        SGParent par = getParent();
        if (par!=null) par.setNeedsLayoutDeep(true);
    }

    /**
     * Returns whether any children need layout.
     */
    public boolean isNeedsLayoutDeep()  { return _needsLayoutDeep; }

    /**
     * Sets whether any children need layout.
     */
    protected void setNeedsLayoutDeep(boolean aVal)
    {
        // If value already set, just return
        if (_needsLayoutDeep) return;
        _needsLayoutDeep = true;

        // If still in layout, just return
        if (_inLayoutDeep) return;

        // If Parent available, forward to it
        SGParent par = getParent();
        if (par!=null)
            par.setNeedsLayoutDeep(true);

        // Otherwise, if SceneGraph available, request scene layout
        else {
            SceneGraph scene = getSceneGraph();
            if (scene!=null)
                scene.relayoutScene();
        }
    }

    /**
     * Returns whether view is currently performing layout.
     */
    public boolean isInLayout()  { return _inLayout; }

    /**
     * Lays out children deep.
     */
    public void layoutDeep()
    {
        // Set InLayoutDeep
        _inLayoutDeep = true;

        // Do layout
        if (_needsLayout) layout();

        // Do layout deep
        if (_needsLayoutDeep)
            layoutDeepImpl();

        // Clear flags
        _needsLayout = _needsLayoutDeep = _inLayoutDeep = false;
    }

    /**
     * Lays out children deep.
     */
    protected void layoutDeepImpl()
    {
        for (SGView child : getChildren())
            if (child instanceof SGParent) { SGParent par = (SGParent)child;
                if (par._needsLayout || par._needsLayoutDeep)
                    par.layoutDeep(); }
    }

    /**
     * Does immediate layout of this view and children (if invalid).
     */
    public void layout()
    {
        if (_inLayout) return;
        undoerDisable(); _inLayout = true;
        layoutImpl(); setNeedsLayout(false);
        undoerEnable(); _inLayout = false;
    }

    /**
     * Called to reposition/resize children.
     */
    protected void layoutImpl()  { }

    /**
     * Returns whether given child view is hittable.
     */
    protected boolean isHittable(SGView aChild)  { return aChild.isVisible(); }

    /**
     * Override to trigger layout.
     */
    public void setWidth(double aValue)  { super.setWidth(aValue); relayout(); }

    /**
     * Override to trigger layout.
     */
    public void setHeight(double aValue)  { super.setHeight(aValue); relayout(); }

    /**
     * Returns the first (top) view hit by the point given in this view's coords.
     */
    public SGView getChildContaining(Point aPoint)
    {
        // Iterate over children
        for (int i=getChildCount()-1; i>=0; i--) { SGView child = getChild(i);
            if (!child.isHittable()) continue; // Get current loop child
            Point point = child.parentToLocal(aPoint); // Get point converted to child
            if (child.contains(point)) // If child contains point, return child
                return child;
        }

        // Return null if no child contains point
        return null;
    }

    /**
     * Returns the child views hit by path given in this view's coords.
     */
    public List <SGView> getChildrenIntersecting(Shape aPath)
    {
        // Create list for intersecting children
        List hit = new ArrayList();

        // Iterate over children
        for (int i=0, iMax=getChildCount(); i<iMax; i++) { SGView child = getChild(i);

            // If not hittable, continue
            if (!child.isHittable()) continue;

            // If child frame doesn't intersect path, just continue
            if (!child.getFrame().intersectsRect(aPath.getBounds()))
                continue;

            // Get path converted to child and if child intersects path, add child to hit list
            Shape path = child.parentToLocal(aPath);
            if (child.intersects(path))
                hit.add(child);
        }

        // Return hit list
        return hit;
    }

    /**
     * Divides the view by a given amount from the top. Returns a clone of given view with bounds
     * set to the remainder. Divides children among the two views (recursively calling divide view for those straddling).
     */
    public SGView divideViewFromTop(double anAmount)
    {
        // Make sure layout is up to date
        layoutDeep();

        // Call normal divide from top edge
        double oldHeight = getHeight();
        SGParent bottomView = (SGParent)super.divideViewFromTop(anAmount);
        double bottomHeight = bottomView.getHeight();

        // Iterate over children to see if they belong to self or newView (or need to be recursively split)
        for (int i=0, iMax=getChildCount(); i<iMax; i++) { SGView child = getChild(i);

            // Get child min y
            double childMinY = child.getFrameY();

            // If child is below border move it to new y in BottomView
            if (childMinY>=getHeight()) {
                child._y = childMinY - getHeight();
                bottomView.addChild(child);
                i--; iMax--; // Reset counters for removed child
            }

            // If child stradles border, divide it and add divided part to newView
            else if (child.getFrameMaxY()>getHeight()) {

                // Get child top/bottom height and divide from top by amount
                double bottomMargin = oldHeight - child.getMaxY();
                double childTopHeight = getHeight() - childMinY; // , cbh = child.getHeight() - childTopHeight;
                SGView childBottom = child.divideViewFromTop(childTopHeight);

                // Move new child bottom view to new y in BottomView
                childBottom._y = 0;
                if (bottomHeight - childBottom.getHeight()<bottomMargin)
                    bottomView.setHeight(childBottom.getHeight()+bottomMargin);
                bottomView.addChild(childBottom);

                // Reset autosizing so that child bottom is nailed to bottomView top
                StringBuffer as = new StringBuffer(childBottom.getAutosizing()); as.setCharAt(4, '-');
                childBottom.setAutosizing(as.toString());
            }
        }

        // Return bottom view
        return bottomView;
    }

    /**
     * Moves the subset of children in the given list to the front of the children list.
     */
    public void bringViewsToFront(List <SGView> theViews)
    {
        for (SGView view : theViews) {
            removeChild(view); addChild(view); }
    }

    /**
     * Moves the subset of children in the given list to the back of the children list.
     */
    public void sendViewsToBack(List <SGView> theViews)
    {
        for (int i=0, iMax=theViews.size(); i<iMax; i++) { SGView view = theViews.get(i);
            removeChild(view); addChild(view, i); }
    }

    /**
     * Standard clone implementation.
     */
    public SGParent clone()
    {
        SGParent clone = (SGParent)super.clone();
        clone._children = new ArrayList();
        clone._childPCL = null; clone._childDCL = null;
        return clone;
    }

    /**
     * Clones all attributes of this view with complete clones of its children as well.
     */
    public SGParent cloneDeep()
    {
        SGParent clone = clone();
        for (int i=0, iMax=getChildCount(); i<iMax; i++) clone.addChild(getChild(i).cloneDeep());
        return clone;
    }

    /**
     * Returns the value for given key.
     */
    public Object getPropValue(String aPropName)
    {
        if (aPropName==Child_Prop) return null;
        return super.getKeyValue(aPropName);
    }

    /**
     * Sets the value for given key.
     */
    public void processPropChange(PropChange aPC, Object oldVal, Object newVal)
    {
        String pname = aPC.getPropName();
        if (pname==Child_Prop) {
            SGView oldC = (SGView)oldVal, newC = (SGView)newVal;
            int ind = aPC.getIndex();
            if (oldC==null) addChild(newC, ind);
            else removeChild(ind);
        }
        else setKeyValue(pname, newVal);
    }

    /**
     * XML Archival generic - break toXML into toXMLView and toXMLChildren.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        XMLElement e = toXMLView(anArchiver);
        toXMLChildren(anArchiver, e);
        return e;
    }

    /**
     * XML Archival of basic view.
     */
    protected XMLElement toXMLView(XMLArchiver anArchiver)  { return super.toXML(anArchiver); }

    /**
     * XML archival of children.
     */
    protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Archive children
        for (int i=0, iMax=getChildCount(); i<iMax; i++) { SGView child = getChild(i);
            anElement.add(anArchiver.toXML(child, this)); }
    }

    /**
     * XML unarchival generic - break fromXML into fromXMLView and fromXMLChildren.
     */
    public SGView fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Legacy
        if (getClass()== SGParent.class) {
            //if(anElement.getElement("layout")!=null) return new RMFlowShape().fromXML(anArchiver, anElement);
            if(anElement.getName().equals("shape")) return new SGSpringsView().fromXML(anArchiver, anElement);
        }

        // Unarchive view and children and return
        fromXMLView(anArchiver, anElement);
        fromXMLChildren(anArchiver, anElement);
        layoutDeep();
        return this;
    }

    /**
     * XML unarchival.
     */
    protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)  { super.fromXML(anArchiver,anElement); }

    /**
     * XML unarchival for view children.
     */
    protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Iterate over child elements and unarchive views
        for (int i=0, iMax=anElement.size(); i<iMax; i++) { XMLElement childXML = anElement.get(i);

            // Get child class - if SGView, unarchive and add
            Class childClass = anArchiver.getClass(childXML.getName());
            if (childClass!=null && SGView.class.isAssignableFrom(childClass)) {
                SGView view = (SGView)anArchiver.fromXML(childXML, this);
                addChild(view);
            }
        }
    }
}
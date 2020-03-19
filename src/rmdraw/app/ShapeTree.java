package rmdraw.app;
import rmdraw.scene.*;
import snap.gfx.Color;
import snap.view.*;

/**
 * An inspector to show ShapeTree.
 */
public class ShapeTree extends EditorPane.SupportPane {
    
    // The ShapeTree
    TreeView       _shapeTree;

/**
 * Creates a new ShapeFills pane.
 */
public ShapeTree(EditorPane anEP)  { super(anEP); }

/**
 * Returns the ViewTree.
 */
protected View createUI()
{
    if(_shapeTree!=null) return _shapeTree;
    TreeView tview = new TreeView();
    tview.setName("ShapeTree");
    tview.setGrowHeight(true);
    tview.setBorder(Color.GRAY, 1);
    tview.getScrollView().setBarSize(14);
    tview.setResolver(new ShapeTreeResolver());
    return _shapeTree = tview;
}

/**
 * Initialize UI.
 */
protected void initUI()
{
    enableEvents(_shapeTree, MouseRelease);
}

/**
 * Reset UI.
 */
protected void resetUI()
{
    _shapeTree.setItems(getEditor().getDoc());
    _shapeTree.expandAll();
    _shapeTree.setSelItem(getEditor().getSelOrSuperSelView());
}

/**
 * Respond UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle ShapeTree
    if(anEvent.equals("ShapeTree") && anEvent.isActionEvent())
        getEditor().setSelView((SGView)anEvent.getSelItem());
        
    // Handle MouseClick
    if(anEvent.isMouseClick() && anEvent.getClickCount()==2)
        getEditor().setSuperSelView((SGView)anEvent.getSelItem());
}

/**
 * A TreeResolver for Document Shapes.
 */
public class ShapeTreeResolver extends TreeResolver <SGView> {
    
    /** Returns the parent of given item. */
    public SGView getParent(SGView anItem)  { return anItem!=getEditor().getDoc()? anItem.getParent() : null; }

    /** Whether given object is a parent (has children). */
    public boolean isParent(SGView anItem)
    {
        return anItem instanceof SGParent && anItem.getChildCount()>0;
    }

    /** Returns the children. */
    public SGView[] getChildren(SGView aParent)
    {
        SGParent par = (SGParent)aParent;
        return par.getChildArray();
    }

    /** Returns the text to be used for given item. */
    public String getText(SGView anItem)
    {
        String str = anItem.getClass().getSimpleName(); if(str.startsWith("RM")) str = str.substring(2);
        String name = anItem.getName(); if(name!=null) str += " - " + name;
        if(anItem instanceof SGText) { SGText ts = (SGText)anItem;
            String text = ts.getText(); if(text!=null) str += " \"" + text + "\" "; }
        return str;
    }

    /** Return the image to be used for given item. */
    public View getGraphic(SGView anItem)  { return null; }
}

}
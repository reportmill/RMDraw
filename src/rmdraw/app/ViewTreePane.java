package rmdraw.app;
import rmdraw.scene.*;
import snap.gfx.Color;
import snap.view.*;

/**
 * An inspector to show ViewTree.
 */
public class ViewTreePane extends EditorPane.SupportPane {

    // The TreeView
    private TreeView _treeView;

    /**
     * Creates ViewTreePane.
     */
    public ViewTreePane(EditorPane anEP)
    {
        super(anEP);
    }

    /**
     * Returns the ViewTree.
     */
    protected View createUI()
    {
        if (_treeView != null) return _treeView;
        TreeView tview = new TreeView();
        tview.setName("TreeView");
        tview.setGrowHeight(true);
        tview.setBorder(Color.GRAY, 1);
        tview.getScrollView().setBarSize(14);
        tview.setResolver(new ViewTreeResolver());
        return _treeView = tview;
    }

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        enableEvents(_treeView, MouseRelease);
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        _treeView.setItems(getEditor().getDoc());
        _treeView.expandAll();
        _treeView.setSelItem(getEditor().getSelOrSuperSelView());
    }

    /**
     * Respond UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle TreeView
        if (anEvent.equals("TreeView") && anEvent.isActionEvent())
            getEditor().setSelView((SGView) anEvent.getSelItem());

        // Handle MouseClick
        if (anEvent.isMouseClick() && anEvent.getClickCount() == 2)
            getEditor().setSuperSelView((SGView) anEvent.getSelItem());
    }

    /**
     * A TreeResolver for Scene Views.
     */
    public class ViewTreeResolver extends TreeResolver<SGView> {

        /**
         * Returns the parent of given item.
         */
        public SGView getParent(SGView anItem)
        {
            return anItem != getEditor().getDoc() ? anItem.getParent() : null;
        }

        /**
         * Whether given object is a parent (has children).
         */
        public boolean isParent(SGView anItem)
        {
            return anItem instanceof SGParent && anItem.getChildCount() > 0;
        }

        /**
         * Returns the children.
         */
        public SGView[] getChildren(SGView aParent)
        {
            SGParent par = (SGParent) aParent;
            return par.getChildArray();
        }

        /**
         * Returns the text to be used for given item.
         */
        public String getText(SGView anItem)
        {
            String str = anItem.getClass().getSimpleName();
            if (str.startsWith("RM")) str = str.substring(2);
            String name = anItem.getName();
            if (name != null) str += " - " + name;
            if (anItem instanceof SGText) {
                SGText ts = (SGText) anItem;
                String text = ts.getText();
                if (text != null) str += " \"" + text + "\" ";
            }
            return str;
        }

        /**
         * Return the image to be used for given item.
         */
        public View getGraphic(SGView anItem)
        {
            return null;
        }
    }
}
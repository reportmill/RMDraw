package rmdraw.app;
import rmdraw.scene.SGDoc;
import rmdraw.scene.SGPage;
import snap.gfx.Font;
import snap.view.*;

/**
 * An EditorPane subclass that allows for markup of an embedded view.
 */
public class MarkupEditorPane extends EditorPane {

    // The embedded view
    private View _embedView;

    // A reference to ToolsView
    protected RowView _toolsView;

    /**
     * Constructor.
     */
    public MarkupEditorPane(View aView)
    {
        _embedView = aView;

        // Create default doc
        SGDoc doc = new SGDoc();
        doc.setShowMargin(false);
        doc.setFont(Font.Arial14);
        SGPage page = doc.getPage(0);
        page.setPaintBackground(false);

        // Set Doc
        getEditor().setDoc(doc);
    }

    /**
     * Returns whether editor pane is selecting.
     */
    public boolean isSelecting()
    {
        return isShowing() && getEditor().getCurrentTool() == getEditor().getSelectTool();
    }

    /**
     * Override to return as MarkupEditor.
     */
    @Override
    public MarkupEditor getEditor()
    {
        return (MarkupEditor) super.getEditor();
    }

    @Override
    protected Viewer createViewer()
    {
        return new MarkupEditor(_embedView);
    }

    /**
     * Override to suppress creation of MenuBar.
     */
    @Override
    protected View createMenuBarUI(View contentView)
    {
        return contentView;
    }

    @Override
    protected void initUI()
    {
        super.initUI();

        // Remove inspector
        InspectorPanel insp = getInspectorPanel();
        View inspUI = insp.getUI();
        ViewUtils.removeChild(inspUI.getParent(), inspUI);

        // Suppress Scrolling
        getScrollView().setShowHBar(false);
        getScrollView().setShowVBar(false);
        getScrollView().getScroller().setFillWidth(true);
        getScrollView().getScroller().setFillHeight(true);

        // Remove toolbar
        EditorPaneToolBar toolBarTop = getTopToolBar();
        View toolBarTopUI = toolBarTop.getUI();
        BorderView borderView = (BorderView) toolBarTopUI.getParent();
        borderView.setTop(null);
        borderView.setBottom(null);
        borderView.setRight(null);

        // Get reference to ToolsView
        _toolsView = toolBarTop.getToolsView();
    }

    @Override
    protected void resetUI()
    {
        super.resetUI();

        // Get selected tool button name and button - if found and not selected, select it
        String toolButtonName = getEditor().getCurrentTool().getClass().getSimpleName() + "Button";
        ToggleButton toolButton = (ToggleButton) _toolsView.getChild(toolButtonName);
        if (toolButton != null && !toolButton.isSelected())
            toolButton.setSelected(true);
    }

    /**
     * Returns the ToolsView.
     */
    public RowView getToolsView()
    {
        return _toolsView;
    }
}

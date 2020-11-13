package rmdraw.app;
import rmdraw.scene.SGDoc;
import rmdraw.scene.SGPage;
import snap.gfx.Font;
import snap.view.BorderView;
import snap.view.View;
import snap.view.ViewOwner;
import snap.view.ViewUtils;

/**
 * An EditorPane subclass that allows for markup of an embedded view.
 */
public class MarkupEditorPane extends EditorPane {

    // The embedded view
    private View  _embedView;

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
    }
}

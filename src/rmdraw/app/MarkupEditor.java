package rmdraw.app;
import rmdraw.scene.SGDoc;
import rmdraw.scene.SGPage;
import rmdraw.scene.SGView;
import snap.gfx.Painter;
import snap.view.BoxView;
import snap.view.View;
import snap.view.ViewEvent;

/**
 * An EditorPane subclass that allows for markup of an embedded view.
 */
public class MarkupEditor extends Editor {

    // The embedded view
    private View _embedView;

    /**
     * Constructor.
     */
    public MarkupEditor(View aView)
    {
        super();
        setPadding(40, 40, 40, 40);
        _embedView = aView;
        addChild(_embedView);
    }

    /**
     * Returns whether anything is selected.
     */
    public boolean isAnySelected()
    {
        SGView selView = getSelView();
        return !(selView==null || selView instanceof SGDoc || selView instanceof SGPage);
    }

    /**
     * Override to suppress normal paintFront().
     */
    @Override
    public void paintFront(Painter aPntr)  { }

    /**
     * Override to paint above EmbedView.
     */
    @Override
    protected void paintAbove(Painter aPntr)
    {
        super.paintFront(aPntr);
    }

    /**
     * Returns the preferred size of the viewer (includes ZoomFactor).
     */
    protected double getPrefWidthImpl(double aH)
    {
        return BoxView.getPrefWidth(this, _embedView, aH);
    }

    /**
     * Returns the preferred size of the viewer (includes ZoomFactor).
     */
    protected double getPrefHeightImpl(double aW)
    {
        return BoxView.getPrefHeight(this, _embedView, aW);
    }

    /**
     * Override to handle EmbedView
     */
    @Override
    protected void layoutImpl()
    {
        super.layoutImpl();
        BoxView.layout(this, _embedView, null, true, true);

        double embW = _embedView.getWidth();
        double embH = _embedView.getHeight();
        getDoc().setSize(embW, embH);
    }

    /**
     * Override to hide EmbedView if Mouse is Over something.
     */
    @Override
    protected void processEvent(ViewEvent anEvent)
    {
        if (anEvent.isMouseMove()) {
            SGView hitView = getViewAtPoint(anEvent.getX(), anEvent.getY());
            boolean overChart = hitView == null || hitView instanceof SGDoc || hitView instanceof SGPage;
            if (overChart) overChart = getSelectTool().getHandleAtPoint(anEvent.getPoint())==null;
            _embedView.setPickable(overChart);
        }
        super.processEvent(anEvent);
    }
}

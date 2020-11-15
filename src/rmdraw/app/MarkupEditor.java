package rmdraw.app;
import rmdraw.scene.SGDoc;
import rmdraw.scene.SGPage;
import rmdraw.scene.SGView;
import snap.gfx.Painter;
import snap.view.BoxView;
import snap.view.View;
import snap.view.ViewEvent;

import java.util.List;

/**
 * An EditorPane subclass that allows for markup of an embedded view.
 */
public class MarkupEditor extends Editor {

    // The embedded view
    private View _embedView;

    // Whether MarkupEditor needs to show inspector
    private boolean  _needsInspector;

    // Constants for Properties
    public static final String NeedsInspector_Prop = "NeedsInspector";

    /**
     * Constructor.
     */
    public MarkupEditor(View aView)
    {
        super();
        setPadding(40, 40, 40, 40);
        _embedView = aView;
        addChild(_embedView);

        setZoomFactor(1);
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
     * Returns whether editor needs inspector to show.
     */
    public boolean isNeedsInspector()  { return _needsInspector; }

    /**
     * Sets whether editor needs inspector to show.
     */
    public void setNeedsInspector(boolean aValue)
    {
        if (aValue==_needsInspector) return;
        firePropChange(NeedsInspector_Prop, _needsInspector, _needsInspector = aValue);
    }

    /**
     * Returns whether editor needs inspector to show.
     */
    public boolean isNeedsInspectorCalculated()
    {
        if (!isShowing())
            return false;
        if (getCurrentTool()!=getSelectTool())
            return true;
        SGView supView = getSuperSelView();
        if (supView!=null && !(supView instanceof SGDoc) && !(supView instanceof SGPage))
            return true;
        SGView selView = getSelView();
        return !(selView==null || selView instanceof SGDoc || selView instanceof SGPage);
    }

    /**
     * Updates the NeedsInspector property based on current tool and/or selection.
     */
    public void updateNeedsInspector()
    {
        boolean needs = isNeedsInspectorCalculated();
        setNeedsInspector(needs);
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
        // Update EmbedView.Pickable on MouseMove (if SelectTool is current)
        if (anEvent.isMouseMove() && isCurrentToolSelectTool()) {
            SGView hitView = getViewAtPoint(anEvent.getX(), anEvent.getY());
            boolean overChart = hitView == null || hitView instanceof SGDoc || hitView instanceof SGPage;
            if (overChart) overChart = getSelectTool().getHandleAtPoint(anEvent.getPoint())==null;
            _embedView.setPickable(overChart);
        }

        // Do normal version
        super.processEvent(anEvent);
    }

    @Override
    public void setSelViews(List<SGView> theViews)
    {
        super.setSelViews(theViews);
        updateNeedsInspector();
    }

    @Override
    public void setSuperSelView(SGView aView)
    {
        super.setSuperSelView(aView);
        updateNeedsInspector();
    }

    /**
     * Override so that EmbedView is only pickable when SelectTool is active.
     */
    @Override
    public void setCurrentTool(Tool aTool)
    {
        // Do normal version
        super.setCurrentTool(aTool);

        // Make EmbedView pickable for SelectTool only
        _embedView.setPickable(aTool==getSelectTool());
        updateNeedsInspector();

        getEditorPane().resetLater();
    }
}

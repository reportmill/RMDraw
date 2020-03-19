package rmdraw.app;
import rmdraw.editors.Placer;
import rmdraw.scene.SGView;

/**
 * A Placer implementation to edit placement attributes.
 */
public class ToolPlacer implements Placer {

    // The View
    private SGView _view;

    /**
     * Creates ToolPlacer.
     */
    public ToolPlacer(SGView aView)
    {
        _view = aView;
    }

    @Override
    public double getX()  { return _view.getFrameX(); }

    @Override
    public void setX(double aValue)  { _view.setFrameX(aValue); }

    @Override
    public double getY()  { return _view.getFrameY(); }

    @Override
    public void setY(double aValue)  { _view.setFrameY(aValue); }

    @Override
    public double getWidth()  { return _view.width(); }

    @Override
    public void setWidth(double aValue)  { _view.setWidth(aValue); }

    @Override
    public double getHeight()  { return _view.height(); }

    @Override
    public void setHeight(double aValue)  { _view.setHeight(aValue); }

    @Override
    public double getRotation()  { return _view.getRoll(); }

    @Override
    public void setRotation(double aValue)  { _view.setRoll(aValue); }

    @Override
    public double getScaleX()  { return _view.getScaleX(); }

    @Override
    public void setScaleX(double aValue)  { _view.setScaleX(aValue); }

    @Override
    public double getScaleY()  { return _view.getScaleY(); }

    @Override
    public void setScaleY(double aValue)  { _view.setScaleY(aValue); }

    @Override
    public double getSkewX()  { return _view.getSkewX(); }

    @Override
    public void setSkewX(double aValue)  { _view.setSkewX(aValue); }

    @Override
    public double getSkewY()  { return _view.getSkewY(); }

    @Override
    public void setSkewY(double aValue)  { _view.setSkewY(aValue); }

    @Override
    public boolean isMinWidthSet()  { return _view.isMinWidthSet(); }

    @Override
    public double getMinWidth()  { return _view.getMinWidth(); }

    @Override
    public void setMinWidth(double aValue)  { _view.setMinWidth(aValue); }

    @Override
    public boolean isMinHeightSet()  { return _view.isMinHeightSet(); }

    @Override
    public double getMinHeight()  { return _view.getMinHeight(); }

    @Override
    public void setMinHeight(double aValue)  { _view.setMinHeight(aValue); }

    @Override
    public boolean isPrefWidthSet()  { return _view.isPrefWidthSet(); }

    @Override
    public double getPrefWidth()  { return _view.getPrefWidth(); }

    @Override
    public void setPrefWidth(double aValue)  { _view.setPrefWidth(aValue); }

    @Override
    public boolean isPrefHeightSet()  { return _view.isPrefHeightSet(); }

    @Override
    public double getPrefHeight()  { return _view.getPrefHeight(); }

    @Override
    public void setPrefHeight(double aValue)  { _view.setPrefHeight(aValue); }

    @Override
    public boolean isLocked()  { return _view.isLocked(); }

    @Override
    public void setLocked(boolean aValue)  { _view.setLocked(aValue); }

    @Override
    public String getAutosizing()  { return _view.getAutosizing(); }

    @Override
    public void setAutosizing(String aValue)  { _view.setAutosizing(aValue); }
}

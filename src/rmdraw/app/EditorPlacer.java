package rmdraw.app;
import rmdraw.gfx.Placer;
import rmdraw.shape.RMDocument;
import rmdraw.shape.RMPage;
import rmdraw.shape.RMShape;
import java.util.List;

/**
 * A Placer implementation for editing placement of editor selection.
 */
public class EditorPlacer implements Placer {

    // The editor
    private Editor  _editor;

    /**
     * Creates EditorPlacer.
     */
    public EditorPlacer(Editor anEditor)
    {
        _editor = anEditor;
    }

    @Override
    public double getX()
    {
        return getSelOrSuperSelPlacer().getX();
    }

    @Override
    public void setX(double aValue)
    {
        setUndoTitle("Location Change");
        for (Placer placer : getSelOrSuperSelPlacers())
            placer.setX(aValue);
    }

    @Override
    public double getY()
    {
        return getSelOrSuperSelPlacer().getY();
    }

    @Override
    public void setY(double aValue)
    {
        setUndoTitle("Location Change");
        for (Placer placer : getSelOrSuperSelPlacers())
            placer.setY(aValue);
    }

    @Override
    public double getWidth()
    {
        return getSelOrSuperSelPlacer().getWidth();
    }

    @Override
    public void setWidth(double aValue)
    {
        setUndoTitle("Size Change");
        for (Placer placer : getSelOrSuperSelPlacers())
            placer.setWidth(aValue);
    }

    @Override
    public double getHeight()
    {
        return getSelOrSuperSelPlacer().getHeight();
    }

    @Override
    public void setHeight(double aValue)
    {
        setUndoTitle("Size Change");
        for (Placer placer : getSelOrSuperSelPlacers())
            placer.setHeight(aValue);
    }

    @Override
    public double getRotation()
    {
        return getSelOrSuperSelPlacer().getRotation();
    }

    @Override
    public void setRotation(double aValue)
    {
        setUndoTitle("Rotation Change");
        for (Placer placer : getSelOrSuperSelPlacers())
            placer.setRotation(aValue);
    }

    @Override
    public double getScaleX()
    {
        return getSelOrSuperSelPlacer().getScaleX();
    }

    @Override
    public void setScaleX(double aValue)
    {
        setUndoTitle("Scale Change");
        for (Placer placer : getSelOrSuperSelPlacers())
            placer.setScaleX(aValue);
    }

    @Override
    public double getScaleY()
    {
        return getSelOrSuperSelPlacer().getScaleY();
    }

    @Override
    public void setScaleY(double aValue)
    {
        setUndoTitle("Scale Change");
        for (Placer placer : getSelOrSuperSelPlacers())
            placer.setScaleY(aValue);
    }

    @Override
    public double getSkewX()
    {
        return getSelOrSuperSelPlacer().getSkewX();
    }

    @Override
    public void setSkewX(double aValue)
    {
        setUndoTitle("Skew Change");
        for (Placer placer : getSelOrSuperSelPlacers())
            placer.setSkewX(aValue);
    }

    @Override
    public double getSkewY()
    {
        return getSelOrSuperSelPlacer().getSkewY();
    }

    @Override
    public void setSkewY(double aValue)
    {
        setUndoTitle("Skew Change");
        for (Placer placer : getSelOrSuperSelPlacers())
            placer.setSkewY(aValue);
    }

    @Override
    public boolean isMinWidthSet()
    {
        return getSelOrSuperSelPlacer().isMinWidthSet();
    }

    @Override
    public double getMinWidth()
    {
        return getSelOrSuperSelPlacer().getMinWidth();
    }

    @Override
    public void setMinWidth(double aValue)
    {
        for (Placer placer : getSelOrSuperSelPlacers())
            placer.setMinWidth(aValue);
    }

    @Override
    public boolean isMinHeightSet()
    {
        return getSelOrSuperSelPlacer().isMinHeightSet();
    }

    @Override
    public double getMinHeight()
    {
        return getSelOrSuperSelPlacer().getMinHeight();
    }

    @Override
    public void setMinHeight(double aValue)
    {
        for (Placer placer : getSelOrSuperSelPlacers())
            placer.setMinHeight(aValue);
    }

    @Override
    public boolean isPrefWidthSet()
    {
        return getSelOrSuperSelPlacer().isPrefWidthSet();
    }

    @Override
    public double getPrefWidth()
    {
        return getSelOrSuperSelPlacer().getPrefWidth();
    }

    @Override
    public void setPrefWidth(double aValue)
    {
        for (Placer placer : getSelOrSuperSelPlacers())
            placer.setPrefWidth(aValue);
    }

    @Override
    public boolean isPrefHeightSet()
    {
        return getSelOrSuperSelPlacer().isPrefHeightSet();
    }

    @Override
    public double getPrefHeight()
    {
        return getSelOrSuperSelPlacer().getPrefHeight();
    }

    @Override
    public void setPrefHeight(double aValue)
    {
        for (Placer placer : getSelOrSuperSelPlacers())
            placer.setPrefHeight(aValue);
    }

    @Override
    public boolean isLocked()
    {
        return getSelOrSuperSelPlacer().isLocked();
    }

    @Override
    public void setLocked(boolean aValue)
    {
        for (Placer placer : getSelOrSuperSelPlacers())
            placer.setLocked(aValue);
    }

    @Override
    public String getAutosizing()
    {
        return getSelOrSuperSelPlacer().getAutosizing();
    }

    @Override
    public void setAutosizing(String aValue)
    {
        for (Placer placer : getSelOrSuperSelPlacers())
            placer.setAutosizing(aValue);
    }

    /**
     * Returns whether values are editable.
     */
    public boolean isEditable()
    {
        RMShape view = _editor.getSuperSelectedShape();
        return !(view instanceof RMDocument || view instanceof RMPage);
    }

    /**
     * Converts from shape units to tool units.
     */
    public double getUnitsFromPoints(double aValue)
    {
        RMDocument doc = _editor.getDoc();
        return doc!=null ? doc.getUnitsFromPoints(aValue) : aValue;
    }

    /**
     * Converts from tool units to shape units.
     */
    public double getPointsFromUnits(double aValue)
    {
        RMDocument doc = _editor.getDoc();
        return doc!=null ? doc.getPointsFromUnits(aValue) : aValue;
    }

    /**
     * Returns the currently selected shape or, if none, the super-selected shape.
     */
    private Placer getSelOrSuperSelPlacer()
    {
        RMShape view = _editor.getSelectedOrSuperSelectedShape();
        return getPlacer(view);
    }

    /**
     * Returns the currently selected shape or, if none, the super-selected shape.
     */
    private Placer[] getSelOrSuperSelPlacers()
    {
        List<RMShape> views = _editor.getSelectedOrSuperSelectedShapes();
        Placer placers[] = new Placer[views.size()];
        for (int i=0, iMax=views.size(); i<iMax; i++) placers[i] = getPlacer(views.get(i));
        return placers;
    }

    /**
     * Returns the currently selected shape or, if none, the super-selected shape.
     */
    private Placer getPlacer(RMShape aView)
    {
        return new ToolPlacer(aView);
    }

    /**
     * Sets the Undo title.
     */
    private void setUndoTitle(String aString)
    {
        _editor.undoerSetUndoTitle(aString);
    }
}

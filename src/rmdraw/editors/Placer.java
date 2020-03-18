package rmdraw.editors;

/**
 * An interface for editing placement attributes: bounds, transform, etc.
 */
public interface Placer {

    /**
     * Returns X Coord.
     */
    double getX();

    /**
     * Sets X Coord.
     */
    void setX(double aValue);

    /**
     * Returns Y Coord.
     */
    double getY();

    /**
     * Sets Y Coord.
     */
    void setY(double aValue);

    /**
     * Returns the width.
     */
    double getWidth();

    /**
     * Sets the width.
     */
    void setWidth(double aValue);

    /**
     * Returns the height.
     */
    double getHeight();

    /**
     * Sets the height.
     */
    void setHeight(double aValue);

    /**
     * Returns the rotation.
     */
    double getRotation();

    /**
     * Sets the rotation.
     */
    void setRotation(double aValue);

    /**
     * Returns the scale X.
     */
    double getScaleX();

    /**
     * Sets the scale X.
     */
    void setScaleX(double aValue);

    /**
     * Returns the scale Y.
     */
    double getScaleY();

    /**
     * Sets the scale Y.
     */
    void setScaleY(double aValue);

    /**
     * Returns the skew X.
     */
    double getSkewX();

    /**
     * Sets the skew X.
     */
    void setSkewX(double aValue);

    /**
     * Returns the skew Y.
     */
    double getSkewY();

    /**
     * Sets the skew Y.
     */
    void setSkewY(double aValue);

    /**
     * Returns whether MinWidth is set.
     */
    boolean isMinWidthSet();

    /**
     * Returns the MinWidth.
     */
    double getMinWidth();

    /**
     * Sets the MinWidth.
     */
    void setMinWidth(double aValue);

    /**
     * Returns whether MinHeight is set.
     */
    boolean isMinHeightSet();

    /**
     * Returns the MinHeight.
     */
    double getMinHeight();

    /**
     * Sets the MinHeight.
     */
    void setMinHeight(double aValue);

    /**
     * Returns whether PrefWidth is set.
     */
    boolean isPrefWidthSet();

    /**
     * Returns the PrefWidth.
     */
    double getPrefWidth();

    /**
     * Sets the PrefWidth.
     */
    void setPrefWidth(double aValue);

    /**
     * Returns whether PrefHeight is set.
     */
    boolean isPrefHeightSet();

    /**
     * Returns the PrefHeight.
     */
    double getPrefHeight();

    /**
     * Sets the PrefHeight.
     */
    void setPrefHeight(double aValue);

    /**
     * Returns whether target is locked.
     */
    boolean isLocked();

    /**
     * Sets whether target is locked.
     */
    void setLocked(boolean aValue);

    /**
     * Returns autosizing string.
     */
    String getAutosizing();

    /**
     * Sets autosizing string.
     */
    void setAutosizing(String aValue);

    /**
     * Returns whether values are editable.
     */
    default boolean isEditable()  { return true; }

    /**
     * Converts from shape units to tool units.
     */
    default double getUnitsFromPoints(double aValue)  { return aValue; }

    /**
     * Converts from tool units to shape units.
     */
    default double getPointsFromUnits(double aValue)  { return aValue; }
}

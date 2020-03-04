package rmdraw.gfx;
import rmdraw.shape.RMShape;
import snap.geom.Pos;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Paint;
import snap.gfx.Stroke;

/**
 * A class to handle getting/setting style attributes for Paint/Border/Effect tools.
 */
public class Styler {

    /**
     * Returns the selected border.
     */
    public Border getBorder()  { return null; }

    /**
     * Sets the selected border.
     */
    public void setBorder(Border aBorder)  { }

    /**
     * Sets the selected border stroke color.
     */
    public void setBorderStrokeColor(Color aColor)
    {
        Border b1 = getBorder();
        Border b2 = b1.copyForColor(aColor);
        setBorder(b2);
    }

    /**
     * Sets the selected border stroke width.
     */
    public void setBorderStrokeWidth(double aWidth)
    {
        Border b1 = getBorder();
        Border b2 = b1.copyForStrokeWidth(aWidth);
        setBorder(b2);
    }

    /**
     * Sets the selected border stroke dash array.
     */
    public void setBorderStrokeDashArray(double theDashes[])
    {
        Border bdr1 = getBorder(); if (bdr1==null) bdr1 = Border.blackBorder();
        Stroke str1 = bdr1.getStroke(), str2 = str1.copyForDashes(theDashes);
        Border bdr2 = bdr1.copyForStroke(str2);
        setBorder(bdr2);
    }

    /**
     * Sets the selected border stroke dash array.
     */
    public void setBorderStrokeDashPhase(double aValue)
    {
        Border bdr1 = getBorder(); if (bdr1==null) bdr1 = Border.blackBorder();
        Stroke str1 = bdr1.getStroke(), str2 = str1.copyForDashOffset(aValue);
        Border bdr2 = bdr1.copyForStroke(str2);
        setBorder(bdr2);
    }

    /**
     * Sets the selected border show edge.
     */
    public void setBorderShowEdge(Pos aPos, boolean aValue)  { }

    /**
     * Returns the fill of currently selected view.
     */
    public Paint getFill()  { return null; }

    /**
     * Sets the fill of currently selected views.
     */
    public void setFill(Paint aPaint)  { }

    /**
     * Returns the color of currently selected view.
     */
    public Color getFillColor()
    {
        Paint fill = getFill();
        return fill!=null ? fill.getColor() : null;
    }

    /**
     * Sets the color of currently selected view.
     */
    public void setFillColor(Color aColor)  { setFill(aColor); }
}

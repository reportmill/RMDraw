package rmdraw.editors;
import snap.geom.Pos;
import snap.gfx.*;
import snap.view.View;

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
        Border b1 = getBorder(); if (b1==null) b1 = Border.blackBorder();
        Border b2 = b1.copyForColor(aColor);
        setBorder(b2);
    }

    /**
     * Sets the selected border stroke width.
     */
    public void setBorderStrokeWidth(double aWidth)
    {
        Border b1 = getBorder(); if (b1==null) b1 = Border.blackBorder();
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
    public void setBorderShowEdge(Pos aPos, boolean aValue)
    {
        Border bdr = getBorder();
        Borders.EdgeBorder ebdr = bdr instanceof Borders.EdgeBorder ? (Borders.EdgeBorder)bdr : new Borders.EdgeBorder();
        ebdr = ebdr.copyForShowEdge(aPos, aValue);
        setBorder(ebdr);
    }

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

    /**
     * Returns the currently selected effect.
     */
    public Effect getEffect()  { return null; }

    /**
     * Sets the currently selected effect.
     */
    public void setEffect(Effect anEffect)  { }

    /**
     * Returns the currently selected opacity.
     */
    public double getOpacity()  { return 0; }

    /**
     * Sets the currently selected opacity.
     */
    public void setOpacity(double aValue)  { }

    /**
     * Returns the current font.
     */
    public Font getFont()  { return null; }

    /**
     * Sets the current font.
     */
    public void setFont(Font aFont)  { }

    /**
     * Returns the default font.
     */
    public Font getFontDefault()
    {
        return Font.Arial11;
    }

    /**
     * Returns whether current text is underlined.
     */
    public boolean isUnderlined()
    {
        return false;
    }

    /**
     * Sets whether current text is underlined.
     */
    public void setUnderlined(boolean aValue)  { }

    /**
     * Returns the outline state of the currently selected shape (null if none).
     */
    public Border getTextBorder()  { return null; }

    /**
     * Sets the currently selected shapes to be outlined.
     */
    public void setTextBorder(Border aBorder)  { }

    /**
     * Returns the text color current text.
     */
    public Color getTextColor()  { return null; }

    /**
     * Sets the text color current text.
     */
    public void setTextColor(Color aColor)  { }

    /**
     * Resets the current font to given font name (preserving size).
     */
    public void setFontName(String aName)
    {
        Font font1 = getFont(); if (font1==null) font1 = getFontDefault();
        Font font2 = Font.getFont(aName, font1.getSize());
        setFont(font2);
    }

    /**
     * Resets the current font to given family name (preserving size).
     */
    public void setFontFamily(String aName)
    {
        String fnames[] = Font.getFontNames(aName); if (fnames.length==0) return;
        String fname = fnames[0];
        setFontName(fname);
    }

    /**
     * Sets the font size of editor's selected shape(s).
     */
    public void setFontSize(float aSize, boolean isRelative)
    {
        Font font = getFont();
        double size = isRelative ? font.getSize() + aSize : aSize;
        Font font2 = font.deriveFont(size);
        setFont(font2);
    }

    /**
     * Sets the "boldness" of text in the currently selected shapes.
     */
    public void setFontBold(boolean aFlag)
    {
        Font font = getFont();
        Font font2 = font.getBold();
        setFont(font2);
    }

    /**
     * Sets the italic state of text in the currently selected shapes.
     */
    public void setFontItalic(boolean aFlag)
    {
        Font font = getFont();
        Font font2 = font.getItalic();
        setFont(font2);
    }

    /**
     * Returns the client View.
     */
    public View getClientView()  { return null; }
}

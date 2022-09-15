package rmdraw.app;
import rmdraw.scene.SGDoc;
import rmdraw.scene.SGPage;
import rmdraw.scene.SGView;
import snap.geom.HPos;
import snap.geom.Pos;
import snap.gfx.*;
import snap.text.TextFormat;
import snap.util.SnapUtils;
import snap.view.ViewUtils;

/**
 * Sets shape style attributes for shape.
 */
public class ToolStyler<T extends SGView> {

    // The Tool
    protected Tool _tool;

    // The editor
    protected Editor _editor;

    // The Shape
    protected T _shape;

    // Constants for font keys
    private static final String FontName_Key = "FontName";
    private static final String FontFamily_Key = "FontFamily";
    private static final String FontSize_Key = "FontSize";
    private static final String FontSizeDelta_Key = "FontSizeDelta";
    private static final String FontBold_Key = "FontBold";
    private static final String FontItalic_Key = "FontItalic";

    /**
     * Creates ToolStyler for given shape.
     */
    public ToolStyler(Tool aTool, SGView aShape)
    {
        _tool = aTool;
        _shape = (T) aShape;
        _editor = aTool.getEditor();
    }

    /**
     * Returns the border.
     */
    public Border getBorder()
    {
        return _shape.getBorder();
    }

    /**
     * Sets the border.
     */
    public void setBorder(Border aBorder)
    {
        _shape.setBorder(aBorder);
    }

    /**
     * Sets the selected border stroke color.
     */
    public void setBorderStrokeColor(Color aColor)
    {
        Border b1 = getBorder();
        if (b1 == null) b1 = Border.blackBorder();
        Border b2 = b1.copyForColor(aColor);
        setBorder(b2);
    }

    /**
     * Sets the selected border stroke width.
     */
    public void setBorderStrokeWidth(double aWidth)
    {
        Border b1 = getBorder();
        if (b1 == null) b1 = Border.blackBorder();
        Border b2 = b1.copyForStrokeWidth(aWidth);
        setBorder(b2);
    }

    /**
     * Sets the selected border stroke dash array.
     */
    public void setBorderStrokeDashArray(double theDashes[])
    {
        Border bdr1 = getBorder();
        if (bdr1 == null) bdr1 = Border.blackBorder();
        Stroke str1 = bdr1.getStroke(), str2 = str1.copyForDashes(theDashes);
        Border bdr2 = bdr1.copyForStroke(str2);
        setBorder(bdr2);
    }

    /**
     * Sets the selected border stroke dash array.
     */
    public void setBorderStrokeDashPhase(double aValue)
    {
        Border bdr1 = getBorder();
        if (bdr1 == null) bdr1 = Border.blackBorder();
        Stroke str1 = bdr1.getStroke(), str2 = str1.copyForDashOffset(aValue);
        Border bdr2 = bdr1.copyForStroke(str2);
        setBorder(bdr2);
    }

    /**
     * Sets the selected border show edge.
     */
    public void setBorderShowEdge(Pos aPos, boolean aValue)
    {
        Border bdr1 = getBorder();
        Borders.EdgeBorder ebdr = bdr1 instanceof Borders.EdgeBorder ? (Borders.EdgeBorder) bdr1 : null;
        if (ebdr == null) ebdr = new Borders.EdgeBorder();
        Border bdr2 = ebdr.copyForShowEdge(aPos, aValue);
        setBorder(bdr2);
    }

    /**
     * Returns the fill.
     */
    public Paint getFill()
    {
        // If selected or super selected shape is page that doesn't draw color, return "last color" (otherwise, reset it)
        if ((_shape instanceof SGPage || _shape instanceof SGDoc) && _shape.getFill() == null)
            return Color.WHITE;

        // Return shape color
        return _shape.getFill();
    }

    /**
     * Sets the fill.
     */
    public void setFill(Paint aPaint)
    {
        // If Doc or Page, just return
        if (_shape instanceof SGPage || _shape instanceof SGDoc) return;

        // Set color fill
        _shape.setFill(aPaint);
    }

    /**
     * Returns the fill color.
     */
    public Color getFillColor()
    {
        // If selected or super selected shape is page that doesn't draw color, return "last color" (otherwise, reset it)
        if ((_shape instanceof SGPage || _shape instanceof SGDoc) && _shape.getFill() == null)
            return Color.WHITE;

        // Return shape color
        return _shape.getFillColor();
    }

    /**
     * Sets the fill color.
     */
    public void setFillColor(Color aColor)
    {
        // If Doc or Page, just return
        if (_shape instanceof SGPage || _shape instanceof SGDoc) return;

        // If command-click, set gradient fill
        if (ViewUtils.isShortcutDown()) {
            Color c1 = _shape.getFill() != null ? _shape.getFillColor() : Color.CLEARWHITE;
            _shape.setFill(new GradientPaint(c1, aColor, 0));
        }

        // Set color
        else _shape.setFillColor(aColor);
    }

    /**
     * Sets the stroke color of the editor's selected shapes.
     */
    public void setStrokeColor(Color aColor)
    {
        _shape.setBorderColor(aColor);
    }

    /**
     * Returns the text color current text.
     */
    public Color getTextColor()
    {
        return Color.BLACK;
    }

    /**
     * Sets the text color of the editor's selected shapes.
     */
    public void setTextColor(Color aColor)
    {
    }

    /**
     * Returns the font of editor's selected shape.
     */
    public Font getFont()
    {
        Font font = _shape.getFont();
        return font != null ? font : Font.getDefaultFont();
    }

    /**
     * Sets the current font.
     */
    public void setFont(Font aFont)
    {
        _shape.setFont(aFont);
    }

    /**
     * Returns the default font.
     */
    public Font getFontDefault()
    {
        return Font.getDefaultFont();
    }

    /**
     * Returns the font for the given shape.
     */
    public Font getFontDeep()
    {
        return getFontDeep(_shape);
    }

    /**
     * Returns the font for the given shape.
     */
    private Font getFontDeep(SGView aShape)
    {
        // Look for font from shape
        Font font = aShape.getFont();

        // If not found, look for font in children
        for (int i = 0, iMax = aShape.getChildCount(); i < iMax && font == null; i++)
            font = aShape.getChild(i).getFont();

        // If not found, look for font with child tools (recurse)
        for (int i = 0, iMax = aShape.getChildCount(); i < iMax && font == null; i++) {
            SGView child = aShape.getChild(i);
            font = getFontDeep(child);
        }

        // Return font
        return font;
    }

    /**
     * Resets the current font to given font name (preserving size).
     */
    public void setFontName(String aName)
    {
        setFontKeyDeep(FontName_Key, aName);
    }

    /**
     * Resets the current font to given family name (preserving size).
     */
    public void setFontFamily(String aName)
    {
        setFontKeyDeep(FontFamily_Key, aName);
    }

    /**
     * Sets the font size of editor's selected shape(s).
     */
    public void setFontSize(double aSize, boolean isRelative)
    {
        String key = isRelative ? FontSizeDelta_Key : FontSize_Key;
        setFontKeyDeep(key, aSize);
    }

    /**
     * Sets the "boldness" of text in the currently selected shapes.
     */
    public void setFontBold(boolean aFlag)
    {
        setUndoTitle("Make Bold");
        setFontKeyDeep(FontBold_Key, aFlag);
    }

    /**
     * Sets the italic state of text in the currently selected shapes.
     */
    public void setFontItalic(boolean aFlag)
    {
        setUndoTitle("Make Italic");
        setFontKeyDeep(FontItalic_Key, aFlag);
    }

    /**
     * Sets the font family for given shape.
     */
    private void setFontKey(String aKey, Object aVal)
    {
        // Get current font
        Font font = getFont();

        // Handle given key
        switch (aKey) {

            // Handle FontName
            case FontName_Key: {

                // Get new font for name and current shape size and set
                String name = (String) aVal;
                Font font1 = font != null ? font : getFontDefault();
                Font font2 = Font.getFont(name, font1.getSize());
                setFont(font2);
                break;
            }

            // Handle FontFamily
            case FontFamily_Key: {

                // Get new font for given font family font and current shape font size/style and set
                String name = (String) aVal;
                String fnames[] = Font.getFontNames(name);
                if (fnames.length == 0) return;
                String fname = fnames[0];
                Font font1 = font != null ? font : getFontDefault();
                Font font2 = Font.getFont(fname, font1.getSize());
                if (font != null) {
                    if (font.isBold() != font2.isBold() && font2.getBold() != null)
                        font2 = font2.getBold();
                    if (font.isItalic() != font2.isItalic() && font2.getItalic() != null)
                        font2 = font2.getItalic();
                    font2 = font2.deriveFont(font.getSize());
                }
                setFont(font2);
                break;
            }

            // Handle FontSize
            case FontSize_Key: {

                // Get new font for current shape font at new size and set
                double aSize = SnapUtils.doubleValue(aVal);
                if (font == null) return;
                Font font2 = font.deriveFont(aSize);
                setFont(font2);
                break;
            }

            // Handle FontSizeDelta
            case FontSizeDelta_Key: {

                // Get new font for current shape font at new size and set
                double aSize = SnapUtils.doubleValue(aVal);
                if (font == null) return;
                Font font2 = font.deriveFont(font.getSize() + aSize);
                setFont(font2);
                break;
            }

            // Handle FontBold
            case FontBold_Key: {

                // Get new font
                boolean aFlag = SnapUtils.boolValue(aVal);
                if (font == null || font.isBold() == aFlag) return;
                Font font2 = font.getBold();
                if (font2 == null) return;
                setFont(font2);
                break;
            }

            // Handle FontItalic
            case FontItalic_Key: {

                // Get new font
                boolean aFlag = SnapUtils.boolValue(aVal);
                if (font == null || font.isItalic() == aFlag) return;
                Font font2 = font.getItalic();
                if (font2 == null) return;
                setFont(font2);
                break;
            }

            // Handle anything else
            default:
                System.err.println("ToolStyler.setFontKey: Unknown key: " + aKey);
        }
    }

    /**
     * Sets the font family for given shape.
     */
    private void setFontKeyDeep(String aKey, Object aVal)
    {
        // Set font key for shape
        setFontKey(aKey, aVal);

        // Set for children
        for (int i = 0, iMax = _shape.getChildCount(); i < iMax; i++) {
            SGView child = _shape.getChild(i);
            Tool tool = _editor.getToolForView(child);
            tool.getStyler(child).setFontKeyDeep(aKey, aVal);
        }
    }

    /**
     * Returns whether the currently selected shape is underlined.
     */
    public boolean isUnderlined()
    {
        return false;
    }

    /**
     * Sets the currently selected shapes to be underlined.
     */
    public void setUnderlined(boolean aValue)
    {
    }

    /**
     * Returns the outline state of the currently selected shape (null if none).
     */
    public Border getTextBorder()
    {
        return null;
    }

    /**
     * Sets the outline state of the currently selected shapes.
     */
    public void setTextBorder(Border aBorder)
    {
    }

    /**
     * Returns the horizontal alignment of the text of the currently selected shapes.
     */
    public HPos getAlignX()
    {
        return _shape.getAlignX();
    }

    /**
     * Sets the horizontal alignment of the text of the currently selected shapes.
     */
    public void setAlignX(HPos anAlign)
    {
        setUndoTitle("Alignment Change");
        _shape.setAlignX(anAlign);
    }

    /**
     * Returns returns justify of the text of the currently selected shapes.
     */
    public boolean isJustify()
    {
        return false;
    }

    /**
     * Sets the horizontal alignment of the text of the currently selected shapes.
     */
    public void setJustify(boolean aValue)
    {
    }

    /**
     * Sets the currently selected shapes to show text as superscript.
     */
    public void setSuperscript()
    {
    }

    /**
     * Sets the currently selected shapes to show text as subscript.
     */
    public void setSubscript()
    {
    }

    /**
     * Returns the format of the editor's selected shape.
     */
    public TextFormat getFormat()
    {
        return _shape.getFormat();
    }

    /**
     * Sets the format of editor's selected shape(s).
     */
    public void setFormat(TextFormat aFormat)
    {
        _shape.setFormat(aFormat);
    }

    /**
     * Returns the current effect.
     */
    public Effect getEffect()
    {
        return _shape.getEffect();
    }

    /**
     * Sets the current effect.
     */
    public void setEffect(Effect anEffect)
    {
        _shape.setEffect(anEffect);
    }

    /**
     * Returns the current opacity.
     */
    public double getOpacity()
    {
        return _shape.getOpacity();
    }

    /**
     * Sets the currently selected opacity.
     */
    public void setOpacity(double aValue)
    {
        _shape.setOpacity(aValue);
    }

    /**
     * Sets the title of the next registered undo in the viewer's documents's undoer (convenience).
     */
    protected void setUndoTitle(String aTitle)
    {
        _editor.undoerSetUndoTitle(aTitle);
    }
}

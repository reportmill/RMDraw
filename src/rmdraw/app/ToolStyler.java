package rmdraw.app;
import rmdraw.shape.RMDocument;
import rmdraw.shape.RMPage;
import rmdraw.shape.RMShape;
import rmdraw.shape.RMTextShape;
import snap.geom.HPos;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.GradientPaint;
import snap.text.TextEditor;
import snap.text.TextFormat;
import snap.util.SnapUtils;
import snap.view.ViewUtils;

/**
 * Sets shape style attributes for shape.
 */
public class ToolStyler <T extends RMShape> {

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
    public ToolStyler(Tool aTool, RMShape aShape)
    {
        _tool = aTool;
        _shape = (T)aShape;
        _editor = aTool.getEditor();
    }

    /**
     * Returns the specified type of color (text, stroke or fill) of editor's selected shape.
     */
    public Color getFillColor()
    {
        // If selected or super selected shape is page that doesn't draw color, return "last color" (otherwise, reset it)
        if((_shape instanceof RMPage || _shape instanceof RMDocument) && _shape.getFill()==null)
            return Color.WHITE;

        // Return shape color
        return _shape.getColor();
    }

    /**
     * Sets the specified type of color (text, stroke or fill) of editor's selected shape.
     */
    public void setFillColor(Color aColor)
    {
        // If Doc or Page, just return
        if(_shape instanceof RMPage || _shape instanceof RMDocument) return;

        // If command-click, set gradient fill
        if(ViewUtils.isShortcutDown()) {
            Color c1 = _shape.getFill()!=null? _shape.getColor() : Color.CLEARWHITE;
            _shape.setFill(new GradientPaint(c1, aColor, 0));
        }

        // Set color
        else _shape.setColor(aColor);
    }

    /**
     * Sets the stroke color of the editor's selected shapes.
     */
    public void setStrokeColor(Color aColor)
    {
        _shape.setStrokeColor(aColor);
    }

    /**
     * Sets the text color of the editor's selected shapes.
     */
    public void setTextColor(Color aColor)
    {
        _shape.setTextColor(aColor);
    }

    /**
     * Returns the font of editor's selected shape.
     */
    public Font getFont()
    {
        Font font = _shape.getFont();
        return font!=null? font : Font.getDefaultFont();
    }

    /**
     * Returns the font for the given shape.
     */
    public Font getFontDeep(RMShape aShape)
    {
        // Look for font from shape
        Font font = aShape.getFont();

        // If not found, look for font in children
        for(int i=0, iMax=aShape.getChildCount(); i<iMax && font==null; i++)
            font = aShape.getChild(i).getFont();

        // If not found, look for font with child tools (recurse)
        for(int i=0, iMax=aShape.getChildCount(); i<iMax && font==null; i++) {
            RMShape child = aShape.getChild(i);
            font = getFontDeep(child);
        }

        // Return font
        return font;
    }

    /**
     * Sets the font family of editor's selected shape(s).
     */
    public void setFontFamily(Font aFont)
    {
        setFontKeyDeep(_editor, _shape, FontFamily_Key, aFont);
    }

    /**
     * Sets the font name of editor's selected shape(s).
     */
    public void setFontName(Font aFont)
    {
        setFontKeyDeep(_editor, _shape, FontName_Key, aFont);
    }

    /**
     * Sets the font size of editor's selected shape(s).
     */
    public void setFontSize(float aSize, boolean isRelative)
    {
        String key = isRelative? FontSizeDelta_Key : FontSize_Key;
        setFontKeyDeep(_editor, _shape, key, aSize);
    }

    /**
     * Sets the "boldness" of text in the currently selected shapes.
     */
    public void setFontBold(boolean aFlag)
    {
        _editor.undoerSetUndoTitle("Make Bold");
        setFontKeyDeep(_editor, _shape, FontBold_Key, aFlag);
    }

    /**
     * Sets the italic state of text in the currently selected shapes.
     */
    public void setFontItalic(boolean aFlag)
    {
        _editor.undoerSetUndoTitle("Make Italic");
        setFontKeyDeep(_editor, _shape, FontItalic_Key, aFlag);
    }

    /**
     * Sets the font family for given shape.
     */
    private void setFontKey(Editor anEditor, RMShape aShape, String aKey, Object aVal)
    {
        // Get current font
        Font font = aShape.getFont();

        // Handle given key
        switch(aKey) {

            // Handle FontName
            case FontName_Key: {

                // Get new font for name and current shape size and set
                Font aFont = (Font)aVal;
                Font font2 = font!=null? aFont.deriveFont(font.getSize()) : aFont;
                aShape.setFont(font2);
                break;
            }

            // Handle FontFamily
            case FontFamily_Key: {

                // Get new font for given font family font and current shape font size/style and set
                Font aFont = (Font)aVal;
                Font font2 = aFont;
                if(font!=null) {
                    if(font.isBold()!=font2.isBold() && font2.getBold()!=null)
                        font2 = font2.getBold();
                    if(font.isItalic()!=font2.isItalic() && font2.getItalic()!=null)
                        font2 = font2.getItalic();
                    font2 = font2.deriveFont(font.getSize());
                }
                aShape.setFont(font2);
                break;
            }

            // Handle FontSize
            case FontSize_Key: {

                // Get new font for current shape font at new size and set
                double aSize = SnapUtils.doubleValue(aVal); if(font==null) return;
                Font font2 = font.deriveFont(aSize);
                aShape.setFont(font2);
                break;
            }

            // Handle FontSizeDelta
            case FontSizeDelta_Key: {

                // Get new font for current shape font at new size and set
                double aSize = SnapUtils.doubleValue(aVal); if(font==null) return;
                Font font2 = font.deriveFont(font.getSize() + aSize);
                aShape.setFont(font2);
                break;
            }

            // Handle FontBold
            case FontBold_Key: {

                // Get new font
                boolean aFlag = SnapUtils.boolValue(aVal);
                if(font==null || font.isBold()==aFlag) return;
                Font font2 = font.getBold(); if(font2==null) return;
                aShape.setFont(font2);
                break;
            }

            // Handle FontItalic
            case FontItalic_Key: {

                // Get new font
                boolean aFlag = SnapUtils.boolValue(aVal);
                if(font==null || font.isItalic()==aFlag) return;
                Font font2 = font.getItalic(); if(font2==null) return;
                aShape.setFont(font2);
                break;
            }

            // Handle anything else
            default: System.err.println("ToolStyler.setFontKey: Unknown key: " + aKey)   ;
        }
    }

    /**
     * Sets the font family for given shape.
     */
    private void setFontKeyDeep(Editor anEditor, RMShape aShape, String aKey, Object aVal)
    {
        // Set font key for shape
        setFontKey(anEditor, aShape, aKey, aVal);

        // Set for children
        for(int i=0, iMax=aShape.getChildCount(); i<iMax; i++) { RMShape child = aShape.getChild(i);
            Tool tool = _editor.getTool(child);
            tool.getStyler(child).setFontKeyDeep(anEditor, child, aKey, aVal);
        }
    }

    /**
     * Returns whether the currently selected shape is underlined.
     */
    public boolean isUnderlined()
    {
        return _shape.isUnderlined();
    }

    /**
     * Sets the currently selected shapes to be underlined.
     */
    public void setUnderlined()
    {
        _editor.undoerSetUndoTitle("Make Underlined");
        _shape.setUnderlined(!_shape.isUnderlined());
    }

    /**
     * Returns the outline state of the currently selected shape (null if none).
     */
    public Border getTextBorder()
    {
        RMTextShape tshp = _shape instanceof RMTextShape? (RMTextShape)_shape : null;
        if( tshp==null) return null;
        return tshp.getTextBorder();
    }

    /**
     * Sets the currently selected shapes to be outlined.
     */
    public void setTextBorder()
    {
        if(getTextBorder()==null) {
            setTextBorder(Border.createLineBorder(Color.BLACK,1));
            setTextColor(Color.WHITE);
        }
        else {
            setTextBorder(null);
            setTextColor(Color.BLACK);
        }
    }

    /**
     * Sets the outline state of the currently selected shapes.
     */
    public void setTextBorder(Border aBorder)
    {
        _editor.undoerSetUndoTitle("Make Outlined");
        if(_shape instanceof RMTextShape)
            ((RMTextShape)_shape).setTextBorder(aBorder);
    }

    /**
     * Returns the horizontal alignment of the text of the currently selected shapes.
     */
    public HPos getAlignX()
    {
        return _shape.getAlignmentX();
    }

    /**
     * Sets the horizontal alignment of the text of the currently selected shapes.
     */
    public void setAlignX(HPos anAlign)
    {
        _editor.undoerSetUndoTitle("Alignment Change");
        _shape.setAlignmentX(anAlign);
    }

    /**
     * Returns returns justify of the text of the currently selected shapes.
     */
    public boolean isJustify()
    {
        return false; //_shape.getAlignmentX();
    }

    /**
     * Sets the horizontal alignment of the text of the currently selected shapes.
     */
    public void setJustify(boolean aValue)
    {
        _editor.undoerSetUndoTitle("Jusify Change");
        //_shape.setJustify(anAlign);
    }

    /**
     * Sets the currently selected shapes to show text as superscript.
     */
    public void setSuperscript()
    {
        _editor.undoerSetUndoTitle("Make Superscript");
        TextEditor ted = _editor.getTextEditor();
        if(ted!=null)
            ted.setSuperscript();
    }

    /**
     * Sets the currently selected shapes to show text as subscript.
     */
    public void setSubscript()
    {
        _editor.undoerSetUndoTitle("Make Subscript");
        TextEditor ted = _editor.getTextEditor();
        if(ted!=null)
            ted.setSubscript();
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
}

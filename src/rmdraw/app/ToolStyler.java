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
import snap.view.ViewUtils;

/**
 * Sets shape style attributes for shape.
 */
public class ToolStyler <T extends RMShape> {

    // The Tool
    protected Tool _tool;
    Editor _editor;

    // The Shape
    protected T _shape;

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
     * Sets the font family of editor's selected shape(s).
     */
    public void setFontFamily(Font aFont)
    {
        _tool.setFontKeyDeep(_editor, _shape, Tool.FontFamily_Key, aFont);
    }

    /**
     * Sets the font name of editor's selected shape(s).
     */
    public void setFontName(Font aFont)
    {
        _tool.setFontKeyDeep(_editor, _shape, Tool.FontName_Key, aFont);
    }

    /**
     * Sets the font size of editor's selected shape(s).
     */
    public void setFontSize(float aSize, boolean isRelative)
    {
        String key = isRelative? Tool.FontSizeDelta_Key : Tool.FontSize_Key;
        _tool.setFontKeyDeep(_editor, _shape, key, aSize);
    }

    /**
     * Sets the "boldness" of text in the currently selected shapes.
     */
    public void setFontBold(boolean aFlag)
    {
        _editor.undoerSetUndoTitle("Make Bold");
        _tool.setFontKeyDeep(_editor, _shape, Tool.FontBold_Key, aFlag);
    }

    /**
     * Sets the italic state of text in the currently selected shapes.
     */
    public void setFontItalic(boolean aFlag)
    {
        _editor.undoerSetUndoTitle("Make Italic");
        _tool.setFontKeyDeep(_editor, _shape, Tool.FontItalic_Key, aFlag);
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

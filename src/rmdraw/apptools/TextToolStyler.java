package rmdraw.apptools;
import rmdraw.app.Tool;
import rmdraw.app.ToolStyler;
import rmdraw.shape.RMShape;
import rmdraw.shape.RMTextShape;
import snap.geom.HPos;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.text.*;
import snap.view.ViewUtils;

/**
 * Sets shape style attributes for shape.
 */
public class TextToolStyler<T extends RMTextShape> extends ToolStyler<T> {

    /**
     * Creates ToolStylerText for given shape.
     */
    public TextToolStyler(Tool aTool, RMShape aShape)
    {
        super(aTool, aShape);
    }

    /**
     * Returns the specified type of color (text, stroke or fill) of editor's selected shape.
     */
    public Color getFillColor()
    {
        // If text color and text editing, return color of text editor
        if(isTextEditorSet())
            return getTextEditor().getTextColor();
        return super.getFillColor();
    }

    /**
     * Sets the specified type of color (text, stroke or fill) of editor's selected shape.
     */
    public void setFillColor(Color aColor)
    {
        // If text color and text editing, return color of text editor
        if(isTextEditorSet()) {

            // If command down, and text is outlined, set color of outline instead
            TextEditor ted = getTextEditor();
            if(ViewUtils.isShortcutDown() && ted.getTextBorder()!=null) {
                Border lbrdr = ted.getTextBorder();
                ted.setTextBorder(Border.createLineBorder(aColor, lbrdr.getWidth()));
            }

            // If no command down, set color of text editor
            else ted.setTextColor(aColor);
        }

        // Otherwise do normal version
        else super.setFillColor(aColor);
    }

    /**
     * Sets the stroke color of the editor's selected shapes.
     */
    public void setStrokeColor(Color aColor)
    {
        if (isTextEditorSet())
            getTextEditor().setTextBorder(Border.createLineBorder(aColor, 1));
        else super.setStrokeColor(aColor);
    }

    /**
     * Returns the text color current text.
     */
    public Color getTextColor()
    {
        if (isTextEditorSet())
            return getTextEditor().getTextColor();
        return super.getTextColor();
    }

    /**
     * Sets the text color current text.
     */
    public void setTextColor(Color aColor)
    {
        if (isTextEditorSet())
            getTextEditor().setTextColor(aColor);
        else super.setTextColor(aColor);
    }

    /**
     * Returns the font of editor's selected shape.
     */
    public Font getFont()
    {
        if(isTextEditorSet())
            return getTextEditor().getFont();
        return super.getFont();
    }

    /**
     * Sets the current font.
     */
    public void setFont(Font aFont)
    {
        if(isTextEditorSet())
            getTextEditor().setFont(aFont);
        else super.setFont(aFont);
    }

    /**
     * Returns whether the currently selected shape is underlined.
     */
    public boolean isUnderlined()
    {
        if(isTextEditorSet())
            return getTextEditor().isUnderlined();
        return super.isUnderlined();
    }

    /**
     * Sets the currently selected shapes to be underlined.
     */
    public void setUnderlined(boolean aValue)
    {
        setUndoTitle("Make Underlined");
        if(isTextEditorSet())
            getTextEditor().setUnderlined(aValue);
        else super.setUnderlined(aValue);
    }

    /**
     * Returns the outline state of the currently selected shape (null if none).
     */
    public Border getTextBorder()
    {
        if (isTextEditorSet())
            return getTextEditor().getTextBorder();
        return super.getTextBorder();
    }

    /**
     * Sets the outline state of the currently selected shapes.
     */
    public void setTextBorder(Border aBorder)
    {
        if (isTextEditorSet())
            getTextEditor().setTextBorder(aBorder);
        else super.setTextBorder(aBorder);
    }

    /**
     * Returns the horizontal alignment of the text of the currently selected shapes.
     */
    public HPos getAlignX()
    {
        if (isTextEditorSet())
            return getTextEditor().getLineAlign();
        return super.getAlignX();
    }

    /**
     * Sets the horizontal alignment of the text of the currently selected shapes.
     */
    public void setAlignX(HPos anAlign)
    {
        if (isTextEditorSet())
            getTextEditor().setLineAlign(anAlign);
        else super.setAlignX(anAlign);
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
        setUndoTitle("Jusify Change");
        //_shape.setJustify(anAlign);
    }

    /**
     * Sets the currently selected shapes to show text as superscript.
     */
    public void setSuperscript()
    {
        setUndoTitle("Make Superscript");
        TextEditor ted = getTextEditor();
        if(isTextEditorSet())
            ted.setSuperscript();
    }

    /**
     * Sets the currently selected shapes to show text as subscript.
     */
    public void setSubscript()
    {
        setUndoTitle("Make Subscript");
        TextEditor ted = getTextEditor();
        if(isTextEditorSet())
            ted.setSubscript();
    }

    /**
     * Returns the format of the editor's selected shape.
     */
    public TextFormat getFormat()
    {
        if (isTextEditorSet())
            return getTextEditor().getFormat();
        return super.getFormat();
    }

    /**
     * Sets the format of editor's selected shape(s).
     */
    public void setFormat(TextFormat aFormat)
    {
        if (isTextEditorSet())
            getTextEditor().setFormat(aFormat);
        else super.setFormat(aFormat);
    }

    /**
     * Returns the char spacing at char 0.
     */
    public float getCharSpacing()
    {
        return getRichText().getRunAt(0).getCharSpacing();
    }

    /**
     * Sets the char spacing for the text string.
     */
    public void setCharSpacing(float aValue)
    {
        if (isTextEditorSet())
            getTextEditor().setCharSpacing(aValue);
        else getRichText().setStyleValue(TextStyle.CHAR_SPACING_KEY, aValue==0? null : aValue);
    }

    /**
     * Returns the line spacing at char 0.
     */
    public double getLineSpacing()
    {
        if (isTextEditorSet())
            return getTextEditor().getLineSpacing();
        return getRichText().getLineStyleAt(0).getSpacing();
    }

    /**
     * Sets the line spacing for all chars.
     */
    public void setLineSpacing(float aHeight)
    {
        if(isTextEditorSet())
            getTextEditor().setLineSpacing(aHeight);
        else {

            TextLineStyle ps = getRichText().getLineStyleAt(0).copyFor(TextLineStyle.SPACING_FACTOR_KEY, aHeight);
            getRichText().setLineStyle(ps, 0, length());
        }
    }

    /**
     * Returns the line gap at char 0.
     */
    public double getLineGap()
    {
        if (isTextEditorSet())
            return getTextEditor().getLineGap();
        return getRichText().getLineStyleAt(0).getSpacing();
    }

    /**
     * Sets the line gap for all chars.
     */
    public void setLineGap(double aHeight)
    {
        if (isTextEditorSet())
            getTextEditor().setLineGap(aHeight);
        else {
            TextLineStyle ps = getRichText().getLineStyleAt(0).copyFor(TextLineStyle.SPACING_KEY, aHeight);
            getRichText().setLineStyle(ps, 0, length());
        }
    }

    /**
     * Returns the minimum line height at char 0.
     */
    public double getLineHeightMin()
    {
        if (isTextEditorSet())
            return getTextEditor().getLineHeightMin();
        return getRichText().getLineStyleAt(0).getMinHeight();
    }

    /**
     * Sets the minimum line height for all chars.
     */
    public void setLineHeightMin(float aHeight)
    {
        if (isTextEditorSet())
            getTextEditor().setLineHeightMin(aHeight);
        else {
            TextLineStyle ps = getRichText().getLineStyleAt(0).copyFor(TextLineStyle.MIN_HEIGHT_KEY, aHeight);
            getRichText().setLineStyle(ps, 0, length());
        }
    }

    /**
     * Returns the maximum line height at char 0.
     */
    public double getLineHeightMax()
    {
        if (isTextEditorSet())
            return getTextEditor().getLineHeightMax();
        return getRichText().getLineStyleAt(0).getMaxHeight();
    }

    /**
     * Sets the maximum line height for all chars.
     */
    public void setLineHeightMax(float aHeight)
    {
        if (isTextEditorSet())
            getTextEditor().setLineHeightMax(aHeight);
        else {
            TextLineStyle ps = getRichText().getLineStyleAt(0).copyFor(TextLineStyle.MAX_HEIGHT_KEY, aHeight);
            getRichText().setLineStyle(ps, 0, length());
        }
    }

    /**
     * Returns the length of text.
     */
    private int length()  { return _shape.length(); }

    /**
     * Returns the TextShape RichText.
     */
    private RichText getRichText()  { return _shape.getRichText(); }

    /**
     * Returns whether TextEditor is active.
     */
    private boolean isTextEditorSet()  { return getTextEditor()!=null; }

    /**
     * Returns the TextEditor (or null if not editing).
     */
    private TextEditor getTextEditor()
    {
        RMTextTool tool = _tool instanceof RMTextTool ? (RMTextTool)_tool : null;
        return tool!=null ? tool.getTextEditor() : null;
    }
}

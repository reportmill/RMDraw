package rmdraw.app;
import rmdraw.shape.RMShape;
import rmdraw.shape.RMTextShape;
import snap.geom.HPos;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.text.TextEditor;
import snap.text.TextFormat;
import snap.view.ViewUtils;

/**
 * Sets shape style attributes for shape.
 */
public class ToolStylerText<T extends RMTextShape> extends ToolStyler<T> {

    /**
     * Creates ToolStylerText for given shape.
     */
    public ToolStylerText(Tool aTool, RMShape aShape)
    {
        super(aTool, aShape);
    }

    /**
     * Returns the specified type of color (text, stroke or fill) of editor's selected shape.
     */
    public Color getFillColor()
    {
        // If text color and text editing, return color of text editor
        if(getTextEditor()!=null)
            return getTextEditor().getTextColor();
        return super.getFillColor();
    }

    /**
     * Sets the specified type of color (text, stroke or fill) of editor's selected shape.
     */
    public void setFillColor(Color aColor)
    {
        // If text color and text editing, return color of text editor
        if(getTextEditor()!=null) {

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
        if (getTextEditor()!=null)
            getTextEditor().setTextBorder(Border.createLineBorder(aColor, 1));
        else super.setStrokeColor(aColor);
    }

    /**
     * Returns the text color current text.
     */
    public Color getTextColor()
    {
        if (getTextEditor()!=null)
            return getTextEditor().getTextColor();
        return super.getTextColor();
    }

    /**
     * Sets the text color current text.
     */
    public void setTextColor(Color aColor)
    {
        if (getTextEditor()!=null)
            getTextEditor().setTextColor(aColor);
        else super.setTextColor(aColor);
    }

    /**
     * Returns the font of editor's selected shape.
     */
    public Font getFont()
    {
        if(getTextEditor()!=null)
            return getTextEditor().getFont();
        return super.getFont();
    }

    /**
     * Sets the current font.
     */
    public void setFont(Font aFont)
    {
        if(getTextEditor()!=null)
            getTextEditor().setFont(aFont);
        else super.setFont(aFont);
    }

    /**
     * Returns whether the currently selected shape is underlined.
     */
    public boolean isUnderlined()
    {
        if(getTextEditor()!=null)
            return getTextEditor().isUnderlined();
        return super.isUnderlined();
    }

    /**
     * Sets the currently selected shapes to be underlined.
     */
    public void setUnderlined(boolean aValue)
    {
        setUndoTitle("Make Underlined");
        if(getTextEditor()!=null)
            getTextEditor().setUnderlined(aValue);
        else super.setUnderlined(aValue);
    }

    /**
     * Returns the outline state of the currently selected shape (null if none).
     */
    public Border getTextBorder()
    {
        if (getTextEditor()!=null)
            return getTextEditor().getTextBorder();
        return super.getTextBorder();
    }

    /**
     * Sets the outline state of the currently selected shapes.
     */
    public void setTextBorder(Border aBorder)
    {
        if (getTextEditor()!=null)
            getTextEditor().setTextBorder(aBorder);
        else super.setTextBorder(aBorder);
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
     * Returns the horizontal alignment of the text of the currently selected shapes.
     */
    public HPos getAlignX()
    {
        if (getTextEditor()!=null)
            return getTextEditor().getLineAlign();
        return super.getAlignX();
    }

    /**
     * Sets the horizontal alignment of the text of the currently selected shapes.
     */
    public void setAlignX(HPos anAlign)
    {
        if (getTextEditor()!=null)
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
        if(ted!=null)
            ted.setSuperscript();
    }

    /**
     * Sets the currently selected shapes to show text as subscript.
     */
    public void setSubscript()
    {
        setUndoTitle("Make Subscript");
        TextEditor ted = getTextEditor();
        if(ted!=null)
            ted.setSubscript();
    }

    /**
     * Returns the format of the editor's selected shape.
     */
    public TextFormat getFormat()
    {
        if (getTextEditor()!=null)
            return getTextEditor().getFormat();
        return super.getFormat();
    }

    /**
     * Sets the format of editor's selected shape(s).
     */
    public void setFormat(TextFormat aFormat)
    {
        if (getTextEditor()!=null)
            getTextEditor().setFormat(aFormat);
        else super.setFormat(aFormat);
    }

    /**
     * Returns the TextEditor (or null if not editing).
     */
    private TextEditor getTextEditor()
    {
        return _editor.getTextEditor();
    }
}

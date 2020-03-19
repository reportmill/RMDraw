package rmdraw.app;
import rmdraw.editors.Styler;
import rmdraw.scene.SGView;
import snap.geom.HPos;
import snap.geom.Pos;
import snap.gfx.*;
import snap.text.TextFormat;
import snap.view.View;
import java.util.List;

/**
 * Sets style attributes.
 */
public class EditorStyler extends Styler {

    // The editor
    private Editor _editor;

    /**
     * Creates EditorStyler.
     */
    public EditorStyler(Editor anEditor)
    {
        _editor = anEditor;
    }

    /**
     * Returns the currently selected border.
     */
    public Border getBorder()
    {
        return getSelOrSuperSelStyler().getBorder();
    }

    /**
     * Sets the currently selected border.
     */
    public void setBorder(Border aBorder)
    {
        for (ToolStyler styler : getSelOrSuperSelStylers())
            styler.setBorder(aBorder);
    }

    /**
     * Sets the selected border stroke color.
     */
    public void setBorderStrokeColor(Color aColor)
    {
        for (ToolStyler styler : getSelOrSuperSelStylers())
            styler.setBorderStrokeColor(aColor);
    }

    /**
     * Sets the selected border stroke width.
     */
    public void setBorderStrokeWidth(double aWidth)
    {
        for (ToolStyler styler : getSelOrSuperSelStylers())
            styler.setBorderStrokeWidth(aWidth);
    }

    /**
     * Sets the selected border stroke dash array.
     */
    public void setBorderStrokeDashArray(double theDashes[])
    {
        for (ToolStyler styler : getSelOrSuperSelStylers())
            styler.setBorderStrokeDashArray(theDashes);
    }

    /**
     * Sets the selected border stroke dash array.
     */
    public void setBorderStrokeDashPhase(double aValue)
    {
        for (ToolStyler styler : getSelOrSuperSelStylers())
            styler.setBorderStrokeDashPhase(aValue);
    }

    /**
     * Sets the selected border show edge.
     */
    public void setBorderShowEdge(Pos aPos, boolean aValue)
    {
        for (ToolStyler styler : getSelOrSuperSelStylers())
            styler.setBorderShowEdge(aPos, aValue);
    }

    /**
     * Returns the fill of currently selected view.
     */
    public Paint getFill()
    {
        return getSelOrSuperSelStyler().getFill();
    }

    /**
     * Sets the fill of currently selected views.
     */
    public void setFill(Paint aPaint)
    {
        for(ToolStyler styler : getSelOrSuperSelStylers())
            styler.setFill(aPaint);
    }

    /**
     * Returns the color of currently selected view.
     */
    public Color getFillColor()
    {
        return getSelOrSuperSelStyler().getFillColor();
    }

    /**
     * Sets the color of currently selected view.
     */
    public void setFillColor(Color aColor)
    {
        for (ToolStyler styler : getSelOrSuperSelStylers())
            styler.setFillColor(aColor);
    }

    /**
     * Sets the stroke color of the editor's selected shapes.
     */
    public void setStrokeColor(Color aColor)
    {
        for (ToolStyler styler : getSelOrSuperSelStylers())
            styler.setStrokeColor(aColor);
    }

    /**
     * Returns the text color current text.
     */
    public Color getTextColor()
    {
        return getSelOrSuperSelStyler().getTextColor();
    }

    /**
     * Sets the text color current text.
     */
    public void setTextColor(Color aColor)
    {
        for (ToolStyler styler : getSelOrSuperSelStylers())
            styler.setTextColor(aColor);
    }

    /**
     * Returns the font of editor's selected shape.
     */
    public Font getFont()
    {
        Font font = null;
        for (int i=0, iMax=getSelOrSuperSelShapeCount(); i<iMax && font==null; i++) {
            SGView shape = getSelOrSuperSelShape(i);
            font = getStyler(shape).getFont();
        }
        for (int i=0, iMax=getSelOrSuperSelShapeCount(); i<iMax && font==null; i++) {
            SGView shape = getSelOrSuperSelShape(i);
            font = getStyler(shape).getFontDeep();
        }
        return font!=null ? font : Font.getDefaultFont();
    }

    /**
     * Sets the current font.
     */
    public void setFont(Font aFont)
    {
        for (ToolStyler styler : getSelOrSuperSelStylers())
            styler.setFont(aFont);
    }

    /**
     * Sets the font family of editor's selected shape(s).
     */
    public void setFontFamily(Font aFont)
    {
        for (ToolStyler styler : getSelOrSuperSelStylers())
            styler.setFontFamily(aFont);
    }

    /**
     * Sets the font name of editor's selected shape(s).
     */
    public void setFontName(Font aFont)
    {
        for (ToolStyler styler : getSelOrSuperSelStylers())
            styler.setFontName(aFont);
    }

    /**
     * Sets the font size of editor's selected shape(s).
     */
    public void setFontSize(float aSize, boolean isRelative)
    {
        for (ToolStyler styler : getSelOrSuperSelStylers())
            styler.setFontSize(aSize, isRelative);
    }

    /**
     * Sets the "boldness" of text in the currently selected shapes.
     */
    public void setFontBold(boolean aFlag)
    {
        setUndoTitle("Make Bold");
        for (ToolStyler styler : getSelOrSuperSelStylers())
            styler.setFontBold(aFlag);
    }

    /**
     * Sets the italic state of text in the currently selected shapes.
     */
    public void setFontItalic(boolean aFlag)
    {
        setUndoTitle("Make Italic");
        for (ToolStyler styler : getSelOrSuperSelStylers())
            styler.setFontItalic(aFlag);
    }

    /**
     * Returns whether the currently selected shape is underlined.
     */
    public boolean isUnderlined()
    {
        return getSelOrSuperSelStyler().isUnderlined();
    }

    /**
     * Sets the currently selected shapes to be underlined.
     */
    public void setUnderlined(boolean aValue)
    {
        setUndoTitle("Make Underlined");
        for (ToolStyler styler : getSelOrSuperSelStylers())
            styler.setUnderlined(aValue);
    }

    /**
     * Returns the outline state of the currently selected shape (null if none).
     */
    public Border getTextBorder()
    {
        return getSelOrSuperSelStyler().getTextBorder();
    }

    /**
     * Sets the outline state of the currently selected shapes.
     */
    public void setTextBorder(Border aBorder)
    {
        setUndoTitle("Make Outlined");
        for (ToolStyler styler : getSelOrSuperSelStylers())
            styler.setTextBorder(aBorder);
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
        return getSelOrSuperSelStyler().getAlignX();
    }

    /**
     * Sets the horizontal alignment of the text of the currently selected shapes.
     */
    public void setAlignX(HPos anAlign)
    {
        setUndoTitle("Alignment Change");
        for (ToolStyler styler : getSelOrSuperSelStylers())
            styler.setAlignX(anAlign);
    }

    /**
     * Returns returns justify of the text of the currently selected shapes.
     */
    public boolean isJustify()
    {
        return false; //anEditor.getSelectedOrSuperSelectedShape().getAlignmentX();
    }

    /**
     * Sets the horizontal alignment of the text of the currently selected shapes.
     */
    public void setJustify(boolean aValue)
    {
        setUndoTitle("Jusify Change");
        //for (RMShape shape : anEditor.getSelectedOrSuperSelectedShapes())
        //    shape.setJustify(anAlign);
    }

    /**
     * Sets the current text to show as superscript.
     */
    public void setSuperscript()
    {
        setUndoTitle("Make Superscript");
        getSelOrSuperSelStyler().setSuperscript();
    }

    /**
     * Sets the current text to show as subscript.
     */
    public void setSubscript()
    {
        setUndoTitle("Make Subscript");
        getSelOrSuperSelStyler().setSubscript();
    }

    /**
     * Returns the current format.
     */
    public TextFormat getFormat()
    {
        return getSelOrSuperSelStyler().getFormat();
    }

    /**
     * Sets the current format.
     */
    public void setFormat(TextFormat aFormat)
    {
        for (ToolStyler styler : getSelOrSuperSelStylers())
            styler.setFormat(aFormat);
    }

    /**
     * Returns the current effect.
     */
    public Effect getEffect()
    {
        return getSelOrSuperSelStyler().getEffect();
    }

    /**
     * Sets the current effect.
     */
    public void setEffect(Effect anEffect)
    {
        for (ToolStyler styler : getSelOrSuperSelStylers())
            styler.setEffect(anEffect);
    }

    /**
     * Returns the current opacity.
     */
    public double getOpacity()
    {
        return getSelOrSuperSelStyler().getOpacity();
    }

    /**
     * Sets the currently selected opacity.
     */
    public void setOpacity(double aValue)
    {
        setUndoTitle("Transparency Change");
        for (ToolStyler styler : getSelOrSuperSelStylers())
            styler.setOpacity(aValue);
    }

    /**
     * Returns the client View.
     */
    public View getClientView()  { return _editor; }

    /**
     * Returns the currently selected shape or, if none, the super-selected shape.
     */
    private ToolStyler getSelOrSuperSelStyler()
    {
        SGView shape = _editor.getSelOrSuperSelView();
        return getStyler(shape);
    }

    /**
     * Returns the currently selected shape or, if none, the super-selected shape.
     */
    private ToolStyler[] getSelOrSuperSelStylers()
    {
        List<SGView> shapes = getSelOrSuperSelShapes();
        ToolStyler stylers[] = new ToolStyler[shapes.size()];
        for (int i=0, iMax=shapes.size(); i<iMax; i++) stylers[i] = getStyler(shapes.get(i));
        return stylers;
    }

    /**
     * Returns the currently selected shapes or, if none, the super-selected shape in a list.
     */
    private List<SGView> getSelOrSuperSelShapes()  { return _editor.getSelOrSuperSelViews(); }

    /**
     * Returns the number of currently selected shapes or simply 1, if a shape is super-selected.
     */
    private int getSelOrSuperSelShapeCount()  { return _editor.getSelOrSuperSelViewCount(); }

    /**
     * Returns the currently selected shape at the given index, or the super-selected shape.
     */
    private SGView getSelOrSuperSelShape(int anIndex)  { return _editor.getSelOrSuperSelView(anIndex); }

    /**
     * Returns the specific tool for a given shape.
     */
    private Tool getTool(SGView aShape)  { return _editor.getToolForView(aShape); }

    /**
     * Returns the currently selected shape or, if none, the super-selected shape.
     */
    private ToolStyler getStyler(SGView aShape)
    {
        Tool tool = getTool(aShape);
        return tool.getStyler(aShape);
    }

    /**
     * Sets undo title.
     */
    public void setUndoTitle(String aTitle)
    {
        _editor.undoerSetUndoTitle(aTitle);
    }
}

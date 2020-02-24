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
import java.util.List;

/**
 * Sets style attributes.
 */
public class EditorStyler {

    // The editor
    private Editor _editor;

    /**
     * Creates EditorCellStyler.
     */
    public EditorStyler(Editor anEditor)
    {
        _editor = anEditor;
    }

    /**
     * Returns the specified type of color (text, stroke or fill) of editor's selected shape.
     */
    public Color getFillColor()
    {
        // Get selected or super selected shape
        RMShape shape = getSelOrSuperSelShape();

        // If selected or super selected shape is page that doesn't draw color, return "last color" (otherwise, reset it)
        if((shape instanceof RMPage || shape instanceof RMDocument) && shape.getFill()==null)
            return Color.WHITE;

        // If text color and text editing, return color of text editor
        if(_editor.getTextEditor()!=null)
            return _editor.getTextEditor().getTextColor();

        // Return selected shape's color
        return getSelOrSuperSelShape().getColor();
    }

    /**
     * Sets the specified type of color (text, stroke or fill) of editor's selected shape.
     */
    public void setFillColor(Color aColor)
    {
        // Get selected or super selected shape
        Color color = aColor;
        RMShape shape = getSelOrSuperSelShape();

        // If editor selected or super selected shape is document or page, set "last color" and return
        if(shape instanceof RMPage || shape instanceof RMDocument) return;

        // If text color and text editing, return color of text editor
        if(_editor.getTextEditor()!=null) {

            // Get text editor
            TextEditor ted = _editor.getTextEditor();

            // If command down, and text is outlined, set color of outline instead
            if(ViewUtils.isShortcutDown() && ted.getTextBorder()!=null) {
                Border lbrdr = ted.getTextBorder();
                ted.setTextBorder(Border.createLineBorder(aColor, lbrdr.getWidth()));
            }

            // If no command down, set color of text editor
            else ted.setTextColor(color);
        }

        // If fill color, set selected shapes' fill color
        else {

            // If command-click, set gradient fill
            if(ViewUtils.isShortcutDown()) {
                Color c1 = shape.getFill()!=null? shape.getColor() : Color.CLEARWHITE;
                shape.setFill(new GradientPaint(c1, color, 0));
            }

            // If not command click, just set color of selected shapes
            else {
                for(RMShape shp : getSelOrSuperSelShapes())
                    shp.setColor(aColor);
            }
        }
    }

    /**
     * Sets the stroke color of the editor's selected shapes.
     */
    public void setStrokeColor(Color aColor)
    {
        // Iterate over editor selected shapes or super selected shape
        for(RMShape shape : getSelOrSuperSelShapes())
            shape.setStrokeColor(aColor);
    }

    /**
     * Sets the text color of the editor's selected shapes.
     */
    public void setTextColor(Color aColor)
    {
        // If text editing, forward on to text editor
        if(_editor.getTextEditor()!=null)
            _editor.getTextEditor().setTextColor(aColor);

            // Otherwise, iterate over editor selected shapes or super selected shape
        else for(RMShape shape : getSelOrSuperSelShapes())
            shape.setTextColor(aColor);
    }

    /**
     * Returns the font of editor's selected shape.
     */
    public Font getFont()
    {
        Font font = null;
        for(int i = 0, iMax = getSelOrSuperSelShapeCount(); i<iMax && font==null; i++) {
            RMShape shape = getSelOrSuperSelShape(i);
            Tool tool = getTool(shape);
            font = tool.getFont(_editor, shape);
        }
        for(int i = 0, iMax = getSelOrSuperSelShapeCount(); i<iMax && font==null; i++) {
            RMShape shape = getSelOrSuperSelShape(i);
            Tool tool = getTool(shape);
            font = tool.getFontDeep(_editor, shape);
        }
        return font!=null? font : Font.getDefaultFont();
    }

    /**
     * Sets the font family of editor's selected shape(s).
     */
    public void setFontFamily(Font aFont)
    {
        for(int i = 0, iMax = getSelOrSuperSelShapeCount(); i<iMax; i++) {
            RMShape shape = getSelOrSuperSelShape(i);
            Tool tool = getTool(shape);
            tool.setFontKeyDeep(_editor, shape, Tool.FontFamily_Key, aFont);
        }
    }

    /**
     * Sets the font name of editor's selected shape(s).
     */
    public void setFontName(Font aFont)
    {
        for(int i = 0, iMax = getSelOrSuperSelShapeCount(); i<iMax; i++) {
            RMShape shape = getSelOrSuperSelShape(i);
            Tool tool = getTool(shape);
            tool.setFontKeyDeep(_editor, shape, Tool.FontName_Key, aFont);
        }
    }

    /**
     * Sets the font size of editor's selected shape(s).
     */
    public void setFontSize(float aSize, boolean isRelative)
    {
        for(int i = 0, iMax = getSelOrSuperSelShapeCount(); i<iMax; i++) {
            RMShape shape = getSelOrSuperSelShape(i);
            Tool tool = getTool(shape);
            String key = isRelative? Tool.FontSizeDelta_Key : Tool.FontSize_Key;
            tool.setFontKeyDeep(_editor, shape, key, aSize);
        }
    }

    /**
     * Sets the "boldness" of text in the currently selected shapes.
     */
    public void setFontBold(boolean aFlag)
    {
        _editor.undoerSetUndoTitle("Make Bold");
        for(int i = 0, iMax = getSelOrSuperSelShapeCount(); i<iMax; i++) {
            RMShape shape = getSelOrSuperSelShape(i);
            Tool tool = getTool(shape);
            tool.setFontKeyDeep(_editor, shape, Tool.FontBold_Key, aFlag);
        }
    }

    /**
     * Sets the italic state of text in the currently selected shapes.
     */
    public void setFontItalic(boolean aFlag)
    {
        _editor.undoerSetUndoTitle("Make Italic");
        for(int i = 0, iMax = getSelOrSuperSelShapeCount(); i<iMax; i++) {
            RMShape shape = getSelOrSuperSelShape(i);
            Tool tool = getTool(shape);
            tool.setFontKeyDeep(_editor, shape, Tool.FontItalic_Key, aFlag);
        }
    }

    /**
     * Returns whether the currently selected shape is underlined.
     */
    public boolean isUnderlined()
    {
        return _editor.getSelectedOrSuperSelectedShape().isUnderlined();
    }

    /**
     * Sets the currently selected shapes to be underlined.
     */
    public void setUnderlined()
    {
        _editor.undoerSetUndoTitle("Make Underlined");
        for(RMShape shape : getSelOrSuperSelShapes())
            shape.setUnderlined(!shape.isUnderlined());
    }

    /**
     * Returns the outline state of the currently selected shape (null if none).
     */
    public Border getTextBorder()
    {
        RMShape shp = getSelOrSuperSelShape();
        RMTextShape tshp = shp instanceof RMTextShape? (RMTextShape)shp : null; if(tshp==null) return null;
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
        for(RMShape shp : getSelOrSuperSelShapes()) {
            if(shp instanceof RMTextShape)
                ((RMTextShape)shp).setTextBorder(aBorder);
        }
    }

    /**
     * Returns the horizontal alignment of the text of the currently selected shapes.
     */
    public HPos getAlignX()
    {
        return getSelOrSuperSelShape().getAlignmentX();
    }

    /**
     * Sets the horizontal alignment of the text of the currently selected shapes.
     */
    public void setAlignX(HPos anAlign)
    {
        _editor.undoerSetUndoTitle("Alignment Change");
        for(RMShape shape : getSelOrSuperSelShapes())
            shape.setAlignmentX(anAlign);
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
        _editor.undoerSetUndoTitle("Jusify Change");
        //for (RMShape shape : anEditor.getSelectedOrSuperSelectedShapes())
        //    shape.setJustify(anAlign);
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
        return getSelOrSuperSelShape().getFormat();
    }

    /**
     * Sets the format of editor's selected shape(s).
     */
    public void setFormat(TextFormat aFormat)
    {
        for (RMShape shape : getSelOrSuperSelShapes())
            shape.setFormat(aFormat);
    }

    /**
     * Returns the number of selected shapes.
     */
    private int getSelShapeCount()  { return _editor.getSelectedShapeCount(); }

    /**
     * Returns the first super-selected shape.
     */
    private RMShape getSuperSelShape()  { return _editor.getSuperSelectedShape(); }

    /**
     * Returns the currently selected shapes or, if none, the super-selected shape in a list.
     */
    private List<RMShape> getSelOrSuperSelShapes()  { return _editor.getSelectedOrSuperSelectedShapes(); }

    /**
     * Returns the currently selected shape or, if none, the super-selected shape.
     */
    private RMShape getSelOrSuperSelShape()  { return _editor.getSelectedOrSuperSelectedShape(); }

    /**
     * Returns the number of currently selected shapes or simply 1, if a shape is super-selected.
     */
    private int getSelOrSuperSelShapeCount()  { return _editor.getSelectedOrSuperSelectedShapeCount(); }

    /**
     * Returns the currently selected shape at the given index, or the super-selected shape.
     */
    private RMShape getSelOrSuperSelShape(int anIndex)  { return _editor.getSelectedOrSuperSelectedShape(anIndex); }

    /**
     * Returns the specific tool for a given shape.
     */
    private Tool getTool(Object anObj)  { return _editor.getTool(anObj); }
}

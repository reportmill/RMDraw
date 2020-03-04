package rmdraw.app;
import rmdraw.gfx.Styler;
import rmdraw.shape.RMShape;
import rmdraw.shape.RMTextShape;
import snap.geom.HPos;
import snap.geom.Pos;
import snap.gfx.*;
import snap.text.TextEditor;
import snap.text.TextFormat;
import java.util.List;

/**
 * Sets style attributes.
 */
public class EditorStyler extends Styler {

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
     * Returns the currently selected border.
     */
    public Border getBorder()
    {
        RMShape shape = getSelOrSuperSelShape();
        return shape.getBorder();
    }

    /**
     * Sets the currently selected border.
     */
    public void setBorder(Border aBorder)
    {
        for (int i=0, iMax=getSelOrSuperSelShapeCount(); i<iMax; i++) {
            RMShape shape = getSelOrSuperSelShape(i);
            shape.setBorder(aBorder);
        }
    }

    /**
     * Sets the selected border stroke color.
     */
    public void setBorderStrokeColor(Color aColor)
    {
        for (int i=0, iMax=getSelOrSuperSelShapeCount(); i<iMax; i++) {
            RMShape shape = getSelOrSuperSelShape(i);
            Border b1 = shape.getBorder(); if (b1==null) b1 = Border.blackBorder();
            Border b2 = b1.copyForColor(aColor);
            shape.setBorder(b2);
        }
    }

    /**
     * Sets the selected border stroke width.
     */
    public void setBorderStrokeWidth(double aWidth)
    {
        for (int i=0, iMax=getSelOrSuperSelShapeCount(); i<iMax; i++) {
            RMShape shape = getSelOrSuperSelShape(i);
            Border b1 = shape.getBorder(); if (b1==null) b1 = Border.blackBorder();
            Border b2 = b1.copyForStrokeWidth(aWidth);
            shape.setBorder(b2);
        }
    }

    /**
     * Sets the selected border stroke dash array.
     */
    public void setBorderStrokeDashArray(double theDashes[])
    {
        for (int i=0, iMax=getSelOrSuperSelShapeCount(); i<iMax; i++) {
            RMShape shape = getSelOrSuperSelShape(i);
            Border bdr1 = shape.getBorder(); if (bdr1==null) bdr1 = Border.blackBorder();
            Stroke str1 = bdr1.getStroke(), str2 = str1.copyForDashes(theDashes);
            Border bdr2 = bdr1.copyForStroke(str2);
            shape.setBorder(bdr2);
        }
    }

    /**
     * Sets the selected border stroke dash array.
     */
    public void setBorderStrokeDashPhase(double aValue)
    {
        for (int i=0, iMax=getSelOrSuperSelShapeCount(); i<iMax; i++) {
            RMShape shape = getSelOrSuperSelShape(i);
            Border bdr1 = shape.getBorder(); if (bdr1==null) bdr1 = Border.blackBorder();
            Stroke str1 = bdr1.getStroke(), str2 = str1.copyForDashOffset(aValue);
            Border bdr2 = bdr1.copyForStroke(str2);
            shape.setBorder(bdr2);
        }
    }

    /**
     * Sets the selected border show edge.
     */
    public void setBorderShowEdge(Pos aPos, boolean aValue)
    {
        for (int i=0, iMax=getSelOrSuperSelShapeCount(); i<iMax; i++) {
            RMShape shape = getSelOrSuperSelShape(i);
            Border bdr1 = shape.getBorder();
            Borders.EdgeBorder ebdr = bdr1 instanceof Borders.EdgeBorder ? (Borders.EdgeBorder)bdr1 : null;
            if (ebdr==null) ebdr = new Borders.EdgeBorder();
            Border bdr2 = ebdr.copyForShowEdge(aPos, aValue);
            shape.setBorder(bdr2);
        }
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
        for(RMShape shape : getSelOrSuperSelShapes())
            getStyler(shape).setFill(aPaint);
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
        for(RMShape shape : getSelOrSuperSelShapes())
            getStyler(shape).setFillColor(aColor);
    }

    /**
     * Sets the stroke color of the editor's selected shapes.
     */
    public void setStrokeColor(Color aColor)
    {
        for(RMShape shape : getSelOrSuperSelShapes())
            getStyler(shape).setStrokeColor(aColor);
    }

    /**
     * Sets the text color of the editor's selected shapes.
     */
    public void setTextColor(Color aColor)
    {
        for(RMShape shape : getSelOrSuperSelShapes())
            getStyler(shape).setTextColor(aColor);
    }

    /**
     * Returns the font of editor's selected shape.
     */
    public Font getFont()
    {
        Font font = null;
        for (int i=0, iMax=getSelOrSuperSelShapeCount(); i<iMax && font==null; i++) {
            RMShape shape = getSelOrSuperSelShape(i);
            Tool tool = getTool(shape);
            font = shape.getFont();
        }
        for (int i=0, iMax=getSelOrSuperSelShapeCount(); i<iMax && font==null; i++) {
            RMShape shape = getSelOrSuperSelShape(i);
            getStyler(shape).getFontDeep(shape);
        }
        return font!=null ? font : Font.getDefaultFont();
    }

    /**
     * Sets the font family of editor's selected shape(s).
     */
    public void setFontFamily(Font aFont)
    {
        for (int i=0, iMax=getSelOrSuperSelShapeCount(); i<iMax; i++) {
            RMShape shape = getSelOrSuperSelShape(i);
            getStyler(shape).setFontFamily(aFont);
        }
    }

    /**
     * Sets the font name of editor's selected shape(s).
     */
    public void setFontName(Font aFont)
    {
        for (int i=0, iMax=getSelOrSuperSelShapeCount(); i<iMax; i++) {
            RMShape shape = getSelOrSuperSelShape(i);
            getStyler(shape).setFontName(aFont);
        }
    }

    /**
     * Sets the font size of editor's selected shape(s).
     */
    public void setFontSize(float aSize, boolean isRelative)
    {
        for (int i=0, iMax=getSelOrSuperSelShapeCount(); i<iMax; i++) {
            RMShape shape = getSelOrSuperSelShape(i);
            getStyler(shape).setFontSize(aSize, isRelative);
        }
    }

    /**
     * Sets the "boldness" of text in the currently selected shapes.
     */
    public void setFontBold(boolean aFlag)
    {
        _editor.undoerSetUndoTitle("Make Bold");
        for (int i=0, iMax=getSelOrSuperSelShapeCount(); i<iMax; i++) {
            RMShape shape = getSelOrSuperSelShape(i);
            getStyler(shape).setFontBold(aFlag);
        }
    }

    /**
     * Sets the italic state of text in the currently selected shapes.
     */
    public void setFontItalic(boolean aFlag)
    {
        _editor.undoerSetUndoTitle("Make Italic");
        for (int i=0, iMax=getSelOrSuperSelShapeCount(); i<iMax; i++) {
            RMShape shape = getSelOrSuperSelShape(i);
            getStyler(shape).setFontItalic(aFlag);
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

    /**
     * Returns the currently selected shape or, if none, the super-selected shape.
     */
    private ToolStyler getStyler(RMShape aShape)
    {
        Tool tool = getTool(aShape);
        return tool.getStyler(aShape);
    }

    /**
     * Returns the currently selected shape or, if none, the super-selected shape.
     */
    private ToolStyler getSelOrSuperSelStyler()
    {
        RMShape shape = _editor.getSelectedOrSuperSelectedShape();
        return getStyler(shape);
    }
}

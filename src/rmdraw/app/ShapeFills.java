/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.app;
import rmdraw.gfx.BorderTool;
import rmdraw.gfx.EffectTool;
import rmdraw.gfx.PaintTool;
import rmdraw.shape.*;
import java.util.List;
import snap.gfx.*;
import snap.view.*;

/**
 * This class provides UI for editing the currently selected shapes stroke, fill, effect, transparency.
 */
public class ShapeFills extends EditorPane.SupportPane {
    
    // The PaintTool
    private PaintTool _fillTool = new PaintTool();

    // The BorderTool
    private BorderTool _borderTool = new BorderTool();
    
    // The EffectTool
    private EffectTool _effectTool = new EffectTool();
    
/**
 * Creates a new ShapeFills pane.
 */
public ShapeFills(EditorPane anEP)
{
    super(anEP);
}

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Initialize tools
    _fillTool.setStyler(getEditor().getStyler());
    _borderTool.setStyler(getEditor().getStyler());
    _effectTool.setEditorPane(getEditorPane());

    // Get array of known stroke names and initialize StrokeComboBox
    int scount = _borderTool.getBorderCount();
    Object snames[] = new String[scount];
    for (int i=0;i<scount;i++) snames[i] = _borderTool.getBorder(i).getName();
    setViewItems("StrokeComboBox", snames);
    
    // Get array of known fill names and initialize FillComboBox
    int fcount = _fillTool.getFillCount();
    Object fnames[] = new String[fcount];
    for (int i=0;i<fcount;i++) fnames[i] = _fillTool.getFill(i).getName();
    setViewItems("FillComboBox", fnames);
    
    // Get array of known effect names and initialize EffectComboBox
    int ecount = _effectTool.getEffectCount();
    Object enames[] = new String[ecount];
    for (int i=0;i<ecount;i++) enames[i] = _effectTool.getEffect(i).getName();
    setViewItems("EffectComboBox", enames);
}

/**
 * Reset UI controls from current selection.
 */
public void resetUI()
{
    // Get currently selected shape
    RMShape shape = getEditor().getSelectedOrSuperSelectedShape();
    
    // Get border from shape (or default, if not available)
    Border border = shape.getBorder(); if(border==null) border = Border.blackBorder();

    // Update StrokeCheckBox, StrokeComboBox
    setViewValue("StrokeCheckBox", shape.getBorder()!=null);
    setViewValue("StrokeComboBox", border.getName());
    
    // Get stroke tool, install tool UI in stroke panel and ResetUI
    BorderTool btool = _borderTool.getTool(border);
    getView("StrokePane", BoxView.class).setContent(btool.getUI());
    btool.resetLater();
    
    // Get fill from shape (or default, if not available)
    Paint fill = shape.getFill();
    if(fill==null) fill = Color.BLACK;

    // Update FillCheckBox, FillComboBox
    setViewValue("FillCheckBox", shape.getFill()!=null);
    setViewValue("FillComboBox", fill.getName());
    
    // Get fill tool, install tool UI in fill panel and ResetUI
    PaintTool ftool = _fillTool.getTool(fill);
    getView("FillPane", BoxView.class).setContent(ftool.getUI());
    ftool.resetLater();
    
    // Get effect from shape (or default, if not available)
    Effect effect = shape.getEffect(); if(effect==null) effect = new ShadowEffect();

    // Update EffectCheckBox, EffectComboBox
    setViewValue("EffectCheckBox", shape.getEffect()!=null);
    setViewValue("EffectComboBox", effect.getName());
    
    // Get effect tool, install tool UI in effect panel and ResetUI
    EffectTool etool = _effectTool.getTool(effect);
    getView("EffectPane", BoxView.class).setContent(etool.getUI());
    etool.resetLater();
    
    // Update TransparencySlider, TransparencyText (transparency is opposite of opacity and on 0-100 scale)
    double transparency = 100 - shape.getOpacity()*100;
    setViewValue("TransparencySlider", transparency);
    setViewValue("TransparencyText", transparency);
}

/**
 * Updates currently selected shapes from UI controls.
 */
public void respondUI(ViewEvent anEvent)
{
    // Get the current editor and currently selected shapes list (just return if null)
    Editor editor = getEditor(); if(editor==null) return;
    RMShape shape = editor.getSelectedOrSuperSelectedShape(); if(shape==null) return;
    List <RMShape> shapes = editor.getSelectedOrSuperSelectedShapes();
    
    // Handle StrokeCheckBox: Iterate over shapes and add stroke if not there or remove if there
    if (anEvent.equals("StrokeCheckBox")) {
        boolean selected = anEvent.getBoolValue();
        for (RMShape s : shapes) {
            if (selected && s.getBorder()==null) s.setBorder(Border.blackBorder()); // If requested and missing, add
            if (!selected && s.getBorder()!=null) s.setBorder(null); // If turned off and present, remove
        }
    }
    
    // Handle StrokeComboBox: Get selected border and iterate over shapes and set
    if (anEvent.equals("StrokeComboBox")) {
        Border newBorder = _borderTool.getBorder(anEvent.getSelIndex());
        for (RMShape s : shapes) s.setBorder(newBorder);
    }

    // Handle FillCheckBox: Iterate over shapes and add fill if not there or remove if there
    if (anEvent.equals("FillCheckBox")) {
        boolean selected = anEvent.getBoolValue();
        for (RMShape s : shapes) {
            if (selected && s.getFill()==null) s.setFill(Color.BLACK); // If requested and missing, add
            if (!selected && s.getFill()!=null) s.setFill(null); // If turned off and present, remove
        }
    }
    
    // Handle FillComboBox: Get selected fill instance and iterate over shapes and add fill if not there
    if (anEvent.equals("FillComboBox")) {
        Paint newFill = _fillTool.getFill(anEvent.getSelIndex());
        for (RMShape s : shapes)
            s.setFill(newFill);
    }

    // Handle EffectCheckBox: Iterate over shapes and add effect if not there or remove if there
    if (anEvent.equals("EffectCheckBox")) {
        boolean selected = anEvent.getBoolValue();
        for (RMShape s : shapes) {
            if (selected && s.getEffect()==null) s.setEffect(new ShadowEffect()); // If requested and missing, add
            if (!selected && s.getEffect()!=null) s.setEffect(null); // If turned off and present, remove
        }
    }
    
    // Handle EffectComboBox: Get selected effect instance and iterate over shapes and add effect if not there
    if (anEvent.equals("EffectComboBox")) {
        Effect eff = _effectTool.getEffect(anEvent.getSelIndex());
        for (RMShape s : shapes) s.setEffect(eff);
    }

    // Handle Transparency Slider and Text
    if (anEvent.equals("TransparencySlider") || anEvent.equals("TransparencyText")) {
        shape.undoerSetUndoTitle("Transparency Change");
        double eval = anEvent.equals("TransparencySlider")? anEvent.getIntValue() : anEvent.getFloatValue();
        double val = 1 - eval/100;
        for (RMShape s : shapes)
            s.setOpacity(val);
    }
}

/**
 * Returns the display name for the inspector.
 */
public String getWindowTitle()  { return "Paint/Fill Inspector"; }

}
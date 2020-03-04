/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.gfx;
import rmdraw.app.*;
import rmdraw.shape.*;
import java.util.*;

import snap.gfx.*;
import snap.view.*;
import snap.viewx.ColorWell;

/**
 * Provides a tool for editing RMFills.
 */
public class PaintTool extends EditorPane.SupportPane {

    // Map of tool instances by shape class
    private Map<Class,PaintTool> _tools = new Hashtable();
    
    // List of known fills
    private static Paint  _fill0, _fill1;
    protected static ImagePaint  _imageFill;

/**
 * Creates a new RMFillTool panel.
 */
public PaintTool()  { super(null); }

/**
 * Called to reset UI controls.
 */
protected void resetUI()
{
    // Get currently selected shape
    RMShape shape = getEditor().getSelectedOrSuperSelectedShape();
    
    // Update FillColorWell
    setViewValue("FillColorWell", shape.getColor());    
}

/**
 * Called to respond to UI controls
 */
protected void respondUI(ViewEvent anEvent)
{
    // Get the current editor and currently selected shape (just return if null)
    Editor editor = getEditor(); if(editor==null) return;
    RMShape shape = editor.getSelectedOrSuperSelectedShape(); if(shape==null) return;
    
    // Handle FillColorWell
    if(anEvent.equals("FillColorWell")) {
        
        // Get Color from color well
        ColorWell cwell = getView("FillColorWell", ColorWell.class);
        Color color = cwell.getColor();

        // Set in editor
        editor.getStyler().setFillColor(color);
    }
}

/**
 * Returns the number of known fills.
 */
public int getFillCount()  { return 3; }

/**
 * Returns an individual fill at given index.
 */
public Paint getFill(int anIndex)
{
    if (anIndex==0)
        return _fill0!=null ? _fill0 : (_fill0 = Color.BLACK);
    if (anIndex==1)
        return _fill1!=null ? _fill1 : (_fill1 = new GradientPaint());
    if (_imageFill==null) {
        Image img = Image.get(getClass(), "pkg.images/Clouds.jpg");
        _imageFill = new ImagePaint(img);
    }
    return _imageFill;
}

/**
 * Returns the currently selected shape's fill.
 */
public Paint getSelectedFill()
{
    RMShape shape = getEditor().getSelectedOrSuperSelectedShape();
    return shape.getFill();
}

/**
 * Iterate over editor selected shapes and set fill.
 */
public void setSelectedFill(Paint aFill)
{
    Editor editor = getEditor();
    for(int i=0, iMax=editor.getSelectedOrSuperSelectedShapeCount(); i<iMax; i++) {
        RMShape shape = editor.getSelectedOrSuperSelectedShape(i);
        shape.setFill(aFill);
    }
}

/**
 * Returns the specific tool for a given fill.
 */
public PaintTool getTool(Object anObj)
{
    // Get tool from tools map - just return if present
    Class cls = anObj instanceof Class? (Class)anObj : anObj.getClass();
    PaintTool tool = _tools.get(cls);
    if(tool==null) {
        _tools.put(cls, tool=getToolImpl(cls));
        tool.setEditorPane(getEditorPane());
    }
    return tool;
}

/**
 * Returns the specific tool for a given fill.
 */
private static PaintTool getToolImpl(Class aClass)
{
    if(aClass==Color.class) return new PaintTool();
    if(aClass==GradientPaint.class) return new GradientPaintTool();
    if(aClass==ImagePaint.class) return new ImagePaintTool();
    throw new RuntimeException("PaintTool.getToolImp: No tool class for " + aClass);
}

}
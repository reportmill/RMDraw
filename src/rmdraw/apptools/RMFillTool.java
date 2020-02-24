/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.apptools;
import rmdraw.app.*;
import rmdraw.gfx.*;
import rmdraw.shape.*;
import java.util.*;

import snap.gfx.*;
import snap.view.*;
import snap.viewx.ColorWell;

/**
 * Provides a tool for editing RMFills.
 */
public class RMFillTool extends EditorPane.SupportPane {

    // Map of tool instances by shape class
    Map                 _tools = new Hashtable();
    
    // List of known strokes
    static RMStroke     _strokes[] = { new RMStroke(), new RMBorderStroke() };
    
    // List of known fills
    static Paint       _fill0, _fill1;
    static ImagePaint  _imageFill;

/**
 * Creates a new RMFillTool panel.
 */
public RMFillTool()  { super(null); }

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
 * Returns the number of known strokes.
 */
public int getStrokeCount()  { return _strokes.length; }

/**
 * Returns an individual stroke at given index.
 */
public RMStroke getStroke(int anIndex)  { return _strokes[anIndex]; }

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
 * Returns the currently selected shape's stroke.
 */
public RMStroke getSelectedStroke()
{
    RMShape shape = getEditor().getSelectedOrSuperSelectedShape();
    return shape.getStroke();
}

/**
 * Iterate over editor selected shapes and set stroke.
 */
public void setSelectedStroke(RMStroke aStroke)
{
    Editor editor = getEditor();
    for(int i=0, iMax=editor.getSelectedOrSuperSelectedShapeCount(); i<iMax; i++) {
        RMShape shape = editor.getSelectedOrSuperSelectedShape(i);
        shape.setStroke(i==0? aStroke : aStroke.clone());
    }
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
public RMFillTool getTool(Object anObj)
{
    // Get tool from tools map - just return if present
    Class cls = anObj instanceof Class? (Class)anObj : anObj.getClass();
    RMFillTool tool = (RMFillTool)_tools.get(cls);
    if(tool==null) {
        _tools.put(cls, tool=getToolImpl(cls));
        tool.setEditorPane(getEditorPane());
    }
    return tool;
}

/**
 * Returns the specific tool for a given fill.
 */
static RMFillTool getToolImpl(Class aClass)
{
    if(aClass==RMStroke.class) return new RMStrokeTool();
    if(aClass==RMBorderStroke.class) return new RMBorderStrokeTool();
    if(aClass==Color.class) return new RMFillTool();
    if(aClass==GradientPaint.class) return new GradientPaintTool();
    if(aClass==ImagePaint.class) return new ImagePaintTool();
    System.err.println("RMFillTool.getToolImp: No tool class for " + aClass);
    return new RMFillTool();
}

}
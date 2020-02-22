/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.apptools;
import rmdraw.app.Editor;
import rmdraw.gfx.*;
import rmdraw.shape.RMShape;
import java.util.*;

import snap.gfx.Color;
import snap.view.*;
import snap.viewx.ColorWell;

/**
 * UI editing for RMStroke.
 */
public class RMStrokeTool extends RMFillTool {

    // The last list of strokes provided to UI
    List <RMStroke>  _strokes;

/**
 * Returns a list of strokes for all MainEditor selected shapes (creating stand-ins for selected shapes with no stroke).
 */
public List <RMStroke> getStrokes()  { return _strokes; }

/**
 * Returns a list of strokes for all MainEditor selected shapes (creating stand-ins for selected shapes with no stroke).
 */
private List <RMStroke> createStrokes()
{
    Editor editor = getEditor();
    List <RMStroke> strokes = new ArrayList();
    for(RMShape shape : editor.getSelectedOrSuperSelectedShapes())
        strokes.add(shape.getStroke()!=null? shape.getStroke() : new RMStroke());
    return _strokes = strokes;
}

/**
 * Override to load Strokes list.
 */
public void processResetUI()
{
    _strokes = createStrokes();
    super.processResetUI();
}

/**
 * Reset UI controls.
 */
public void resetUI()
{
    // Get currently selected shape
    RMShape shape = getEditor().getSelectedOrSuperSelectedShape();
    RMStroke stroke = shape.getStroke(); if(stroke==null) stroke = new RMStroke();
    
    // Update StrokeColorWell, StrokeWidthText, StrokeWidthThumb, DashArrayText, DashPhaseSpinner
    setViewValue("StrokeColorWell", stroke.getColor());
    setViewValue("StrokeWidthText", stroke.getWidth());
    setViewValue("StrokeWidthThumb", stroke.getWidth());
    setViewValue("DashArrayText", stroke.getDashArrayString());
    setViewValue("DashPhaseSpinner", stroke.getDashPhase());
}

/**
 * Respond to UI changes
 */
public void respondUI(ViewEvent anEvent)
{
    // Get editor selected shapes and selected shape
    Editor editor = getEditor();
    List <RMShape> shapes = editor.getSelectedOrSuperSelectedShapes();
    RMShape shape = editor.getSelectedOrSuperSelectedShape();
    
    // Handle StrokeColorWell - get color and set in selected shapes
    if(anEvent.equals("StrokeColorWell")) {
        ColorWell cwell = getView("StrokeColorWell", ColorWell.class);
        Color color = cwell.getColor();
        for(RMShape s : shapes)
            s.setStrokeColor(color);
    }
    
    // Handle StrokeWidthText, StrokeWidthThumb
    if(anEvent.equals("StrokeWidthText") || anEvent.equals("StrokeWidthThumb")) {
        float width = anEvent.getFloatValue();
        for(RMShape s : shapes)
            s.setStrokeWidth(width);
    }
    
    // Handle DashArrayText
    if(anEvent.equals("DashArrayText")) {
        float darray[] = RMStroke.getDashArray(anEvent.getStringValue(), ",");
        for(RMShape shp : shapes) { RMStroke stroke = shp.getStroke(); if(stroke==null) stroke = new RMStroke();
            shp.setStroke(stroke.deriveDashArray(darray)); }
    }

    // Handle DashPhaseSpinner
    if(anEvent.equals("DashPhaseSpinner")) {
        float dphase = anEvent.getFloatValue();
        for(RMShape shp : shapes) { RMStroke stroke = shp.getStroke(); if(stroke==null) stroke = new RMStroke();
            shp.setStroke(stroke.deriveDashPhase(dphase)); }
    }

}

}
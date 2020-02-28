/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.apptools;
import rmdraw.app.Editor;
import rmdraw.gfx.*;
import rmdraw.shape.RMShape;
import java.util.*;

import snap.gfx.Border;
import snap.gfx.Color;
import snap.view.*;
import snap.viewx.ColorWell;

/**
 * UI editing for RMStroke.
 */
public class RMStrokeTool extends RMFillTool {

    // The last list of borders provided to UI
    List <Border>  _strokes;

/**
 * Returns a list of strokes for all MainEditor selected shapes (creating stand-ins for selected shapes with no stroke).
 */
public List <Border> getStrokes()  { return _strokes; }

/**
 * Returns a list of strokes for all MainEditor selected shapes (creating stand-ins for selected shapes with no stroke).
 */
private List <Border> createStrokes()
{
    Editor editor = getEditor();
    List <Border> strokes = new ArrayList();
    for (RMShape shape : editor.getSelectedOrSuperSelectedShapes())
        strokes.add(shape.getBorder()!=null? shape.getBorder() : new RMStroke());
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
    Border border = shape.getBorder(); if (border==null) border = new RMStroke();
    
    // Update StrokeColorWell, StrokeWidthText, StrokeWidthThumb, DashArrayText, DashPhaseSpinner
    setViewValue("StrokeColorWell", border.getColor());
    setViewValue("StrokeWidthText", border.getWidth());
    setViewValue("StrokeWidthThumb", border.getWidth());
    setViewValue("DashArrayText", RMStroke.getDashArrayString(border));
    setViewValue("DashPhaseSpinner", RMStroke.getDashPhase(border));
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
        double darray[] = RMStroke.getDashArray(anEvent.getStringValue(), ",");
        for (RMShape shp : shapes) {
            Border border = shp.getBorder();
            if (border==null) border = new RMStroke();
            shp.setBorder(RMStroke.copyForDashArray(border, darray));
        }
    }

    // Handle DashPhaseSpinner
    if(anEvent.equals("DashPhaseSpinner")) {
        float dphase = anEvent.getFloatValue();
        for(RMShape shp : shapes) {
            Border border = shp.getBorder(); if (border==null) border = new RMStroke();
            shp.setBorder(RMStroke.copyForDashPhase(border, dphase));
        }
    }
}

}
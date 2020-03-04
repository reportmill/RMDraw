/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.gfx;
import rmdraw.app.Editor;
import rmdraw.shape.RMShape;
import java.util.*;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Stroke;
import snap.view.*;
import snap.viewx.ColorWell;

/**
 * UI editing for Borders.
 */
public class BorderTool extends PaintTool {

/**
 * Reset UI controls.
 */
public void resetUI()
{
    // Get currently selected shape
    RMShape shape = getEditor().getSelectedOrSuperSelectedShape();
    Border border = shape.getBorder(); if (border==null) border = Border.createLineBorder(Color.BLACK, 1);
    Stroke stroke = border.getStroke();
    
    // Update StrokeColorWell, StrokeWidthText, StrokeWidthThumb, DashArrayText, DashPhaseSpinner
    setViewValue("StrokeColorWell", border.getColor());
    setViewValue("StrokeWidthText", border.getWidth());
    setViewValue("StrokeWidthThumb", border.getWidth());
    setViewValue("DashArrayText", Stroke.getDashArrayString(stroke));
    setViewValue("DashPhaseSpinner", stroke.getDashOffset());
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
    if (anEvent.equals("StrokeColorWell")) {
        ColorWell cwell = getView("StrokeColorWell", ColorWell.class);
        Color color = cwell.getColor();
        for (RMShape s : shapes)
            s.setStrokeColor(color);
    }
    
    // Handle StrokeWidthText, StrokeWidthThumb
    if (anEvent.equals("StrokeWidthText") || anEvent.equals("StrokeWidthThumb")) {
        float width = anEvent.getFloatValue();
        for (RMShape s : shapes)
            s.setStrokeWidth(width);
    }
    
    // Handle DashArrayText
    if (anEvent.equals("DashArrayText")) {
        double darray[] = Stroke.getDashArray(anEvent.getStringValue());
        for (RMShape shp : shapes) {
            Border bdr1 = shp.getBorder(); if (bdr1==null) bdr1 = Border.blackBorder();
            Border bdr2 = bdr1.copyForStroke(bdr1.getStroke().copyForDashes(darray));
            shp.setBorder(bdr2);
        }
    }

    // Handle DashPhaseSpinner
    if (anEvent.equals("DashPhaseSpinner")) {
        float dphase = anEvent.getFloatValue();
        for (RMShape shp : shapes) {
            Border bdr1 = shp.getBorder(); if (bdr1==null) bdr1 = Border.blackBorder();
            Border bdr2 = bdr1.copyForStroke(bdr1.getStroke().copyForDashOffset(dphase));
            shp.setBorder(bdr2);
        }
    }
}

}
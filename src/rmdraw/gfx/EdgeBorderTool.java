/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.gfx;
import rmdraw.app.Editor;
import rmdraw.shape.RMShape;
import java.util.List;

import snap.geom.Pos;
import snap.gfx.Border;
import snap.gfx.Borders;
import snap.gfx.Color;
import snap.view.ViewEvent;
import snap.viewx.ColorWell;

public class EdgeBorderTool extends BorderTool {

/**
 * Reset UI controls.
 */
public void resetUI()
{
    // Get currently selected shape
    RMShape shape = getEditor().getSelectedOrSuperSelectedShape();
    Border border = shape.getBorder(); if (border==null) border = Border.blackBorder();
    Borders.EdgeBorder bstroke = border instanceof Borders.EdgeBorder ? (Borders.EdgeBorder)border : new Borders.EdgeBorder();
    
    // Update StrokeColorWell, StrokeWidthText, StrokeWidthThumb
    setViewValue("StrokeColorWell", border.getColor());
    setViewValue("StrokeWidthText", border.getWidth());
    setViewValue("StrokeWidthThumb", border.getWidth());
    
    // Update TopCheckBox, RightCheckBox, BottomCheckBox, LeftCheckBox
    setViewValue("TopCheckBox", bstroke.isShowTop());
    setViewValue("RightCheckBox", bstroke.isShowRight());
    setViewValue("BottomCheckBox", bstroke.isShowBottom());
    setViewValue("LeftCheckBox", bstroke.isShowLeft());
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
    
    // Handle TopCheckBox, RightCheckBox, BottomCheckBox, LeftCheckBox
    if (anEvent.equals("TopCheckBox")) {
        for (RMShape shp : shapes) { Border bdr = shp.getBorder();
            Borders.EdgeBorder ebdr = bdr instanceof Borders.EdgeBorder ? (Borders.EdgeBorder)bdr : new Borders.EdgeBorder();
            shp.setBorder(ebdr.copyForShowEdge(Pos.TOP_CENTER, anEvent.getBoolValue()));
        }
    }
    if (anEvent.equals("RightCheckBox")) {
        for (RMShape shp : shapes) { Border bdr = shp.getBorder();
            Borders.EdgeBorder ebdr = bdr instanceof Borders.EdgeBorder ? (Borders.EdgeBorder)bdr : new Borders.EdgeBorder();
            shp.setBorder(ebdr.copyForShowEdge(Pos.CENTER_RIGHT, anEvent.getBoolValue()));
        }
    }
    if (anEvent.equals("BottomCheckBox")) {
        for (RMShape shp : shapes) { Border bdr = shp.getBorder();
            Borders.EdgeBorder ebdr = bdr instanceof Borders.EdgeBorder ? (Borders.EdgeBorder)bdr : new Borders.EdgeBorder();
            shp.setBorder(ebdr.copyForShowEdge(Pos.BOTTOM_CENTER, anEvent.getBoolValue()));
        }
    }
    if (anEvent.equals("LeftCheckBox")) {
        for (RMShape shp : shapes) { Border bdr = shp.getBorder();
            Borders.EdgeBorder ebdr = bdr instanceof Borders.EdgeBorder ? (Borders.EdgeBorder)bdr : new Borders.EdgeBorder();
            shp.setBorder(ebdr.copyForShowEdge(Pos.CENTER_LEFT, anEvent.getBoolValue()));
        }
    }
}
    
}

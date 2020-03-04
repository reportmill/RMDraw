/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.apptools;
import rmdraw.app.Editor;
import rmdraw.gfx.*;
import rmdraw.shape.RMShape;
import java.util.List;

import snap.gfx.Border;
import snap.gfx.Color;
import snap.view.ViewEvent;
import snap.viewx.ColorWell;

public class RMBorderStrokeTool extends RMStrokeTool {

/**
 * Reset UI controls.
 */
public void resetUI()
{
    // Get currently selected shape
    RMShape shape = getEditor().getSelectedOrSuperSelectedShape();
    Border border = shape.getBorder(); if (border==null) border = Border.blackBorder();
    RMBorderStroke bstroke = border instanceof RMBorderStroke? (RMBorderStroke)border : new RMBorderStroke();
    
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
    
    // Handle TopCheckBox, RightCheckBox, BottomCheckBox, LeftCheckBox
    if(anEvent.equals("TopCheckBox")) {
        for(RMShape shp : shapes) { Border str = shp.getBorder();
            RMBorderStroke bstr = str instanceof RMBorderStroke? (RMBorderStroke)str : new RMBorderStroke();
            shp.setBorder(bstr.copyForShowTop(anEvent.getBoolValue()));
        }
    }
    if(anEvent.equals("RightCheckBox")) {
        for(RMShape shp : shapes) { Border str = shp.getBorder();
            RMBorderStroke bstr = str instanceof RMBorderStroke? (RMBorderStroke)str : new RMBorderStroke();
            shp.setBorder(bstr.copyForShowRight(anEvent.getBoolValue()));
        }
    }
    if(anEvent.equals("BottomCheckBox")) {
        for(RMShape shp : shapes) { Border str = shp.getBorder();
            RMBorderStroke bstr = str instanceof RMBorderStroke? (RMBorderStroke)str : new RMBorderStroke();
            shp.setBorder(bstr.copyForShowBottom(anEvent.getBoolValue()));
        }
    }
    if(anEvent.equals("LeftCheckBox")) {
        for(RMShape shp : shapes) { Border str = shp.getBorder();
            RMBorderStroke bstr = str instanceof RMBorderStroke? (RMBorderStroke)str : new RMBorderStroke();
            shp.setBorder(bstr.copyForShowLeft(anEvent.getBoolValue()));
        }
    }
}
    
}

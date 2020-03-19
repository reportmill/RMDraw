/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.apptools;
import rmdraw.app.Tool;
import rmdraw.scene.*;
import java.util.*;
import snap.gfx.Border;
import snap.view.*;

/**
 * A tool subclass for editing RMOval.
 */
public class RMOvalShapeTool <T extends SGOval> extends Tool<T> {
    
/**
 * Returns a new instance of the shape class that this tool is responsible for.
 */
protected T newInstance()
{
    T shape = super.newInstance();
    shape.setBorder(Border.blackBorder());
    return shape;
}

/**
 * Updates the UI controls from the currently selected oval.
 */
public void resetUI()
{
    // Get current oval shape
    SGOval oval = getSelectedShape(); if(oval==null) return;
    
    // Update StartThumb, StartText
    setViewValue("StartThumb", oval.getStartAngle());
    setViewValue("StartText", oval.getStartAngle());
    
    // Update SweepThumb, SweepText
    setViewValue("SweepThumb", oval.getSweepAngle());
    setViewValue("SweepText", oval.getSweepAngle());
    
    // Update HoleRatioSlider, HoleRatioText
    setViewValue("HoleRatioSlider", oval.getHoleRatio()*100);
    setViewValue("HoleRatioText", Math.round(oval.getHoleRatio()*100));
}

/**
 * Updates the currently selected oval from the UI controls.
 */
public void respondUI(ViewEvent anEvent)
{
    SGOval oval = getSelectedShape(); if(oval==null) return;
    List <SGOval> ovals = (List)getSelectedShapes();
    
    // Handle StartThumb, StartText
    if(anEvent.equals("StartThumb") || anEvent.equals("StartText")) {
        oval.undoerSetUndoTitle("Start Angle Change");
        for(SGOval o : ovals)
            o.setStartAngle(anEvent.getFloatValue());
    }

    // Handle SweepThumb, SweepText
    if(anEvent.equals("SweepThumb") || anEvent.equals("SweepText")) {
        oval.undoerSetUndoTitle("Sweep Angle Change");
        for(SGOval o : ovals)
            o.setSweepAngle(anEvent.getFloatValue());
    }
    
    // Handle HoleRatioSlider, HoleRatioText
    if(anEvent.equals("HoleRatioSlider") || anEvent.equals("HoleRatioText")) {
        oval.undoerSetUndoTitle("Hole Ratio Change");
        double hratio = anEvent.getFloatValue()/100;
        for(SGOval o : ovals)
            o.setHoleRatio(hratio);
    }
}

/**
 * Event handling - overridden to install crosshair cursor.
 */
public void mouseMoved(ViewEvent anEvent)  { getEditor().setCursor(Cursor.CROSSHAIR); }

/**
 * Returns the shape class this tool is responsible for.
 */
public Class <T> getShapeClass()  { return (Class<T>) SGOval.class; }

/**
 * Returns the string used for the inspector window title.
 */
public String getWindowTitle()  { return "Oval Tool"; }

}
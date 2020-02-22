/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.apptools;
import rmdraw.shape.*;
import snap.gfx.Color;
import snap.gfx.GradientPaint;
import snap.gfx.Paint;
import snap.gfx.Point;
import snap.view.ViewEvent;

/**
 * UI editing for GradientPaint.
 */
public class GradientPaintTool extends RMFillTool {
    
/**
 * Initialize UI.
 */
protected void initUI()
{
    setViewItems("TypeComboBox", new String[] { "Linear", "Radial" });
}

/**
 * Updates the UI controls from the currently selected shape.
 */
protected void resetUI()
{
    // Get currently selected shape and shape gradient fill (just return if null)
    RMShape shape = getEditor().getSelectedOrSuperSelectedShape(); if(shape==null) return;
    GradientPaint fill = getDefaultFill(shape);
    boolean isRadial = fill.isRadial();

    // Update ColorStopPicker
    GradientStopPicker picker = getView("ColorStopPicker", GradientStopPicker.class);
    picker.setStops(fill.getStops());
    
    // Update TypeComboBox, RadialPicker and LinearControls
    setViewSelIndex("TypeComboBox", isRadial? 1 : 0);
    getView("RadialPicker").setVisible(isRadial);
    getView("LinearControls").setVisible(!isRadial);
        
    // Update angle controls for a linear gradient
    if(!isRadial) {
        setViewValue("AngleThumb", fill.getRoll());
        setViewValue("AngleText", fill.getRoll());
    }
    
    // or the axis picker for a radial gradient
    else {
        GradientAxisPicker radialControl = getView("RadialPicker", GradientAxisPicker.class);
        radialControl.setStartPoint(fill.getStartX(), fill.getStartY());
        radialControl.setEndPoint(fill.getEndX(), fill.getEndY());
        radialControl.setStops(fill.getStops());
    }
}

/**
 * Updates the currently selected shape from the UI controls.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Get currently selected shape and its fill (just return if null)
    RMShape shape = getEditor().getSelectedOrSuperSelectedShape(); if(shape==null) return;
    GradientPaint oldfill = getDefaultFill(shape), newFill = null;
    
    // Handle ColorStopPicker
    if(anEvent.equals("ColorStopPicker")) {
        GradientStopPicker picker = anEvent.getView(GradientStopPicker.class);
        newFill = oldfill.copyForStops(picker.getStops());
    }
    
    // Handle ReverseStopsButton
    else if(anEvent.equals("ReverseStopsButton"))
        newFill = oldfill.copyForReverseStops();
    
    // Handle AngleThumb and AngleText
    else if(anEvent.equals("AngleThumb") || anEvent.equals("AngleText")) {
        double angle = anEvent.equals("AngleThumb")? anEvent.getIntValue() : anEvent.getFloatValue();
        newFill = oldfill.copyForRoll(angle);
    }
    
    // Handle linear/radial popup
    else if(anEvent.equals("TypeComboBox")) {
        GradientPaint.Type t = anEvent.getSelIndex()==1? GradientPaint.Type.RADIAL : GradientPaint.Type.LINEAR;
        newFill = oldfill.copyForType(t);
    }

    // Handle radial axis control
    else if(anEvent.equals("RadialPicker")) {
        GradientAxisPicker picker = (GradientAxisPicker)anEvent.getView();
        Point p0 = picker.getStartPoint(), p1 = picker.getEndPoint();
        newFill = oldfill.copyForPoints(p0.x, p0.y, p1.x, p1.y);
    }
    
    // Reset fill of all selected shapes
    if(newFill!=null)
        setSelectedFill(newFill);
}

/**
 * Returns the gradient for the shape.  Creates one if the shape doesn't have a gradient fill.
 */
public GradientPaint getDefaultFill(RMShape shape)
{
    // Get shape gradient fill, if present
    Paint fill = shape.getFill();
    GradientPaint def = fill instanceof GradientPaint ? (GradientPaint)fill : null;
    
    // If missing, create one - second color defaults to black, unless that would result in a black-black gradient
    if(def==null) {
        Color c = shape.getColor();
        Color c2 = c.equals(Color.BLACK)? Color.WHITE : Color.BLACK;
        def = new GradientPaint(c, c2, 0);
    }
    
    // Return fill
    return def;
}

/**
 * Returns the string used for the inspector window title.
 */
public String getWindowTitle()  { return "Fill Inspector (Gradient)"; }

}
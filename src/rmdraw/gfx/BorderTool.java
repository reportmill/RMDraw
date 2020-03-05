/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.gfx;
import java.util.*;
import snap.gfx.*;
import snap.view.*;
import snap.viewx.ColorWell;

/**
 * UI editing for Borders.
 */
public class BorderTool extends ViewOwner {

    // The Styler used to get/set border attributes
    private Styler _styler;

    // Map of tool instances by shape class
    private Map<Class,BorderTool> _tools = new Hashtable();

    // List of known borders
    private static Border  _borders[] = { Border.blackBorder(), new Borders.EdgeBorder() };

    /**
     * Returns the styler.
     */
    public Styler getStyler()  { return _styler; }

    /**
     * Sets the styler.
     */
    public void setStyler(Styler aStyler)
    {
        _styler = aStyler;
    }

    /**
     * Returns the number of known borders.
     */
    public int getBorderCount()  { return _borders.length; }

    /**
     * Returns the individual border at given index.
     */
    public Border getBorder(int anIndex)  { return _borders[anIndex]; }

    /**
     * Reset UI controls.
     */
    public void resetUI()
    {
        // Get currently selected border/stroke
        Styler styler = getStyler();
        Border border = styler.getBorder();
        if (border==null) border = Border.createLineBorder(Color.BLACK, 1);
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
        Styler styler = getStyler();

        // Handle StrokeColorWell - get color and set in selected shapes
        if (anEvent.equals("StrokeColorWell")) {
            ColorWell cwell = getView("StrokeColorWell", ColorWell.class);
            Color color = cwell.getColor();
            styler.setBorderStrokeColor(color);
        }

        // Handle StrokeWidthText, StrokeWidthThumb
        if (anEvent.equals("StrokeWidthText") || anEvent.equals("StrokeWidthThumb")) {
            double width = anEvent.getFloatValue();
            styler.setBorderStrokeWidth(width);
        }

        // Handle DashArrayText
        if (anEvent.equals("DashArrayText")) {
            double darray[] = Stroke.getDashArray(anEvent.getStringValue());
            styler.setBorderStrokeDashArray(darray);
        }

        // Handle DashPhaseSpinner
        if (anEvent.equals("DashPhaseSpinner")) {
            double dphase = anEvent.getFloatValue();
            styler.setBorderStrokeDashPhase(dphase);
        }
    }

    /**
     * Returns the specific tool for a given fill.
     */
    public BorderTool getTool(Object anObj)
    {
        // Get tool from tools map - just return if present
        Class cls = anObj instanceof Class? (Class)anObj : anObj.getClass();
        BorderTool tool = _tools.get(cls);
        if(tool==null) {
            _tools.put(cls, tool=getToolImpl(cls));
            tool.setStyler(getStyler());
        }
        return tool;
    }

    /**
     * Returns the specific tool for a given fill.
     */
    private static BorderTool getToolImpl(Class aClass)
    {
        if(aClass==Borders.EdgeBorder.class) return new EdgeBorderTool();
        if(Border.class.isAssignableFrom(aClass)) return new BorderTool();
        throw new RuntimeException("BorderTool.getToolImpl: Unknown border class: " + aClass);
    }
}
/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.gfx;
import java.util.*;
import snap.gfx.*;
import snap.view.*;
import snap.viewx.ColorWell;

/**
 * Provides a tool for editing Paint.
 */
public class PaintTool extends ViewOwner {

    // The Styler used to get/set paint attributes
    private Styler _styler;

    // Map of PaintTool instances by Paint class
    private Map<Class,PaintTool> _tools = new Hashtable();
    
    // Array of known fills
    private static Paint  _fills[];

    // The default image fill
    protected static ImagePaint  _imageFill;

    /**
     * Creates PaintTool.
     */
    public PaintTool()
    {
        super();
    }

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
     * Called to reset UI controls.
     */
    protected void resetUI()
    {
        // Get currently selected color
        Color color = getStyler().getFillColor();

        // Update FillColorWell
        setViewValue("FillColorWell", color);
    }

    /**
     * Called to respond to UI controls
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle FillColorWell: Get color from ColorWell and set with styler
        if (anEvent.equals("FillColorWell")) {
            ColorWell cwell = getView("FillColorWell", ColorWell.class);
            Color color = cwell.getColor();
            getStyler().setFillColor(color);
        }
    }

    /**
     * Returns the number of known fills.
     */
    public int getFillCount()  { return getFills().length; }

    /**
     * Returns an individual fill at given index.
     */
    public Paint getFill(int anIndex)  { return getFills()[anIndex]; }

    /**
     * Returns the fills.
     */
    private Paint[] getFills()
    {
        // If already set, just return
        if (_fills!=null) return _fills;

        // Create default fills array and return
        Paint f0 = Color.BLACK;
        Paint f1 = new GradientPaint();
        Image img = Image.get(getClass(), "pkg.images/Clouds.jpg");
        _imageFill = new ImagePaint(img);
        return _fills = new Paint[] { f0, f1, _imageFill };
    }

    /**
     * Returns the specific tool for a given fill.
     */
    public PaintTool getTool(Object anObj)
    {
        // Get tool from tools map - just return if present
        Class cls = anObj instanceof Class? (Class)anObj : anObj.getClass();
        PaintTool tool = _tools.get(cls);
        if (tool==null) {
            _tools.put(cls, tool=getToolImpl(cls));
            tool.setStyler(getStyler());
        }
        return tool;
    }

    /**
     * Returns the specific tool for a given fill.
     */
    private static PaintTool getToolImpl(Class aClass)
    {
        if (aClass==Color.class) return new PaintTool();
        if (aClass==GradientPaint.class) return new GradientPaintTool();
        if (aClass==ImagePaint.class) return new ImagePaintTool();
        throw new RuntimeException("PaintTool.getToolImp: No tool class for " + aClass);
    }
}
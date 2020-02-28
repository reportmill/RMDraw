/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.gfx;
import snap.geom.Shape;
import snap.gfx.*;
import snap.util.*;

/**
 * An Border subclass that paints a border for a stroke and color.
 */
public class RMStroke extends Border {
    
    // The color
    private Color _color = Color.BLACK;
    
    // The stroke width
    private double _width = 1;
    
    // The dash array
    private double _dashArray[];
    
    // The dash phase
    private double _dashPhase = 0;

    /**
     * Creates a plain, black stroke.
     */
    public RMStroke()  { }

    /**
     * Creates a stroke with the given color and line width.
     */
    public RMStroke(Color aColor, double aStrokeWidth)  { _color = aColor; _width = (float)aStrokeWidth; }

    /**
     * Returns the color associated with this fill.
     */
    public Color getColor()  { return _color; }

    /**
     * Returns the line width of this stroke.
     */
    public double getWidth()  { return _width; }

    /**
     * Returns the dash array for this stroke.
     */
    public double[] getDashArray()  { return _dashArray; }

    /**
     * Returns the dash array for this stroke as a string.
     */
    public String getDashArrayString()  { return getDashArrayString(getDashArray(), ", "); }

    /**
     * Returns the dash phase.
     */
    public double getDashPhase()  { return _dashPhase; }

    /**
     * Returns the path to be stroked, transformed from the input path.
     */
    public Shape getStrokePath(Shape aShape)  { return aShape; }

    /**
     * Returns the name of the fill.
     */
    public String getName()
    {
        if(getClass()==RMStroke.class) return "Stroke";
        String cname = getClass().getSimpleName(); return cname.substring(2,cname.length()-6);
    }

    /**
     * Paint border.
     */
    public void paint(Painter aPntr, Shape aShape)
    {
        Stroke stroke = getStroke();
        aPntr.setPaint(getColor());
        aPntr.setStroke(getStroke());
        aPntr.draw(aShape);
    }

    /**
     * Returns a duplicate stroke with new color.
     */
    public RMStroke copyForColor(Color aColor)  { RMStroke s = clone(); s._color = aColor; return s; }

    /**
     * Returns a duplicate stroke with new stroke width.
     */
    public RMStroke copyForWidth(double aWidth)  { RMStroke s = clone(); s._width = aWidth; return s; }

    /**
     * Returns a duplicate stroke with new dash array.
     */
    public RMStroke copyForDashArray(double ... aDA)  { RMStroke s = clone(); s._dashArray = aDA; return s; }

    /**
     * Returns a duplicate stroke with new dash phase.
     */
    public RMStroke copyForDashPhase(double aDP)  { RMStroke s = clone(); s._dashPhase = aDP; return s; }

    /**
     * Returns a copy of this border, used for copy methods.
     */
    protected RMStroke clone()   { return (RMStroke)super.clone(); }

    /**
     * Returns the stroke.
     */
    public Stroke getStroke()
    {
        return new Stroke(getWidth(), getDashArray(), getDashPhase());
    }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        // Check identity, superclass and get other
        if(anObj==this) return true;
        if(!super.equals(anObj)) return false;
        RMStroke other = (RMStroke)anObj;

        // Check Width, DashArray, DashPhase
        if(!MathUtils.equals(other._width, _width)) return false;
        if(!ArrayUtils.equals(other._dashArray, _dashArray)) return false;
        if(other._dashPhase!=_dashPhase) return false;
        return true; // Return true since all checks passed
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Create element
        XMLElement e = new XMLElement("stroke");

        // Archive Color, Width, DashArray, DashPhase
        if(!getColor().equals(Color.BLACK)) e.add("color", "#" + getColor().toHexString());
        if(_width!=1) e.add("width", _width);
        if(getDashArrayString()!=null && getDashArrayString().length()>0) e.add("dash-array", getDashArrayString());
        if(getDashPhase()!=0) e.add("dash-phase", getDashPhase());
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive Color
        String color = anElement.getAttributeValue("color");
        if(color!=null) _color = new Color(color);

        // Unarchive Width, DashArray, DashPhase
        if(anElement.hasAttribute("width"))  _width = anElement.getAttributeFloatValue("width", 1);
        else if(anElement.hasAttribute("linewidth")) _width = anElement.getAttributeFloatValue("linewidth", 1);
        if(anElement.hasAttribute("dash-array")) _dashArray = getDashArray(anElement.getAttributeValue("dash-array"), ",");
        if(anElement.hasAttribute("dash-phase")) _dashPhase = anElement.getAttributeFloatValue("dash-phase");
        return this;
    }

    /**
     * Returns a dash array for given border.
     */
    public static double[] getDashArray(Border aBorder)
    {
        return aBorder instanceof RMStroke ? ((RMStroke)aBorder).getDashArray() : null;
    }

    /**
     * Returns a dash phase for given border.
     */
    public static double getDashPhase(Border aBorder)
    {
        return aBorder instanceof RMStroke ? ((RMStroke)aBorder).getDashPhase() : null;
    }

    /**
     * Returns a duplicate border with new dash array.
     */
    public static RMStroke copyForDashArray(Border aBorder, double ... aDA)
    {
        if (aBorder instanceof RMStroke)
            return ((RMStroke)aBorder).copyForDashArray(aDA);
        return new RMStroke(aBorder.getColor(), aBorder.getWidth()).copyForDashArray(aDA);
    }

    /**
     * Returns a duplicate border with new dash phase.
     */
    public static RMStroke copyForDashPhase(Border aBorder, double aDP)
    {
        if (aBorder instanceof RMStroke)
            return ((RMStroke)aBorder).copyForDashPhase(aDP);
        return new RMStroke(aBorder.getColor(), aBorder.getWidth()).copyForDashPhase(aDP);
    }

    /**
     * Returns a dash array for given dash array string and delimeter.
     */
    public static double[] getDashArray(String aString, String aDelimeter)
    {
        // Just return null if empty
        if (aString==null || aString.length()==0) return null;

        String dashStrings[] = aString.split(",");
        double dashArray[] = new double[dashStrings.length];
        for (int i=0; i<dashStrings.length; i++)
            dashArray[i] = SnapUtils.doubleValue(dashStrings[i]);
        return dashArray;
    }

    /**
     * Returns the dash array for given border as a string.
     */
    public static String getDashArrayString(Border aBorder)
    {
        double dashes[] = aBorder instanceof RMStroke ? ((RMStroke)aBorder).getDashArray() : null;
        return getDashArrayString(dashes, ", ");
    }

    /**
     * Returns the dash array for this stroke as a string.
     */
    private static String getDashArrayString(double dashArray[], String aDelimiter)
    {
        if (dashArray==null || dashArray.length==0) return null;
        String dstring = SnapUtils.stringValue(dashArray[0]);
        for (int i=1; i<dashArray.length; i++)
            dstring += aDelimiter + SnapUtils.stringValue(dashArray[i]);
        return dstring;
    }
}
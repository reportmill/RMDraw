/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.gfx;
import snap.geom.Shape;
import snap.gfx.*;
import snap.util.*;

/**
 * A Border subclass that paints a border for a stroke and color.
 */
public class RMStroke extends Borders.LineBorder {
    
    /**
     * Creates a plain, black stroke.
     */
    public RMStroke()  { }

    /**
     * Creates a stroke with the given color and line width.
     */
    public RMStroke(Color aColor, Stroke aStroke)  { super(aColor,  aStroke); }

    /**
     * Returns the name of the fill.
     */
    public String getName()  { return "Stroke"; }

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
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Create element
        XMLElement e = new XMLElement("stroke");

        // Archive Color, Width, DashArray, DashPhase
        if (!getColor().equals(Color.BLACK)) e.add("color", "#" + getColor().toHexString());

        // Archive stroke
        Stroke stroke = getStroke();
        if (getWidth()!=1) e.add("width", getWidth());
        String dashStr = Stroke.getDashArrayString(stroke);
        if (dashStr!=null && dashStr.length()>0) e.add("dash-array", dashStr);
        if (stroke.getDashOffset()!=0) e.add("dash-phase", stroke.getDashOffset());
        return e;
    }

    /**
     * XML unarchival.
     */
    public Borders.LineBorder fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive Color
        String colorStr = anElement.getAttributeValue("color");
        Color color = colorStr!=null ? new Color(colorStr) : Color.BLACK;

        // Unarchive Width, DashArray, DashPhase
        double width = 1, dashArray[] = null, dashPhase = 0;
        if(anElement.hasAttribute("width"))  width = anElement.getAttributeFloatValue("width", 1);
        else if(anElement.hasAttribute("linewidth")) width = anElement.getAttributeFloatValue("linewidth", 1);
        if(anElement.hasAttribute("dash-array")) dashArray = Stroke.getDashArray(anElement.getAttributeValue("dash-array"));
        if(anElement.hasAttribute("dash-phase")) dashPhase = anElement.getAttributeFloatValue("dash-phase");
        Stroke stroke = new Stroke(width, dashArray, dashPhase);
        return new RMStroke(color, stroke);
    }
}
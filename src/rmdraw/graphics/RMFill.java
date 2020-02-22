/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.graphics;
import snap.gfx.*;
import snap.util.*;

/**
 * This class represents a simple shape fill, drawing a given color in a provided path. Subclasses support things
 * like gradients, textures, etc.
 */
public class RMFill implements Paint, Cloneable, XMLArchiver.Archivable {

    // Fill color
    Color        _color = Color.BLACK;

/**
 * Creates a plain, black fill.
 */
public RMFill()  { }

/**
 * Returns the color associated with this fill.
 */
public Color getColor()  { return _color; }

/**
 * Returns the name of the fill.
 */
public String getName()
{
    if(getClass()==RMFill.class) return "Color Fill";
    String cname = getClass().getSimpleName();
    return cname.substring(2,cname.length()-4);
}

/**
 * Derives an instance of this class from another fill.
 */
public Paint copyForColor(Color aColor)
{
    return snap().copyForColor(aColor);
}
  
/**
 * Returns whether paint is defined in terms independent of primitive to be filled.
 */
public boolean isAbsolute()  { return snap().isAbsolute(); }

/**
 * Returns whether paint is opaque.
 */
public boolean isOpaque()  { return snap().isOpaque(); }

/**
 * Returns an absolute paint for given bounds of primitive to be filled.
 */
public Paint copyForRect(Rect aRect)  { return snap().copyForRect(aRect); }

/**
 * Returns the snap version of this fill.
 */
public Paint snap()  { return getColor(); }

/**
 * Standard clone implementation.
 */
public RMFill clone()
{
    try { return (RMFill)super.clone(); }
    catch(CloneNotSupportedException e) { throw new RuntimeException(e); }
}

/**
 * Standard equals implementation.
 */
public boolean equals(Object anObj)
{
    // Check identity, class and get other
    if(anObj==this) return true;
    if(anObj==null || anObj.getClass()!=getClass()) return false;
    RMFill other = (RMFill)anObj;
    
    // Check Color
    if(!SnapUtils.equals(other._color, _color)) return false;
    return true; // Return true since all checks passed
}

/**
 * XML archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    XMLElement e = new XMLElement("fill");
    if(!getColor().equals(Color.BLACK)) e.add("color", "#" + getColor().toHexString());
    return e;
}

/**
 * XML unarchival.
 */
public Paint fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    String colorStr = anElement.getAttributeValue("color");
    Color color = colorStr!=null ? new Color(colorStr) : Color.BLACK;
    return color;
}

/**
 * Returns a string representation.
 */
public String toString()
{
    return String.format("%s: { color:%s }", getClass().getSimpleName(), getColor().toHexString());
}

}
/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.gfx;
import snap.geom.Path;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.gfx.Borders;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.util.*;

/**
 * This Border subclass strokes the rectangular border of a given shape, with option include/exclude
 * individual sides.
 */
public class RMBorderStroke extends Borders.LineBorder {

    // Whether to show left border
    private boolean _showLeft = true;
    
    // Whether to show right border
    private boolean _showRight = true;
    
    // Whether to show top border
    private boolean _showTop = true;
    
    // Whether to show left border
    private boolean _showBottom = true;
    
    /**
     * Returns whether to show left border.
     */
    public boolean isShowLeft()  { return _showLeft; }

    /**
     * Returns whether to show right border.
     */
    public boolean isShowRight()  { return _showRight; }

    /**
     * Returns whether to show top border.
     */
    public boolean isShowTop()  { return _showTop; }

    /**
     * Returns whether to show bottom border.
     */
    public boolean isShowBottom()  { return _showBottom; }

    /**
     * Returns whether to show all borders.
     */
    public boolean isShowAll()  { return _showLeft && _showRight && _showTop && _showBottom; }

    /**
     * Paint border.
     */
    public void paint(Painter aPntr, Shape aShape)
    {
        Color color = getColor();
        Stroke stroke = getStroke();
        aPntr.setPaint(color);
        aPntr.setStroke(stroke);
        Shape spath = getBoxPath(aShape);
        aPntr.draw(spath);
    }

    /**
     * Returns the path to be stroked, transformed from the input path.
     */
    private Shape getBoxPath(Shape aShape)
    {
        // If showing all borders, just return bounds
        Rect rect = aShape.getBounds(); if(isShowAll()) return rect;
        boolean st = isShowTop(), sr = isShowRight(), sb = isShowBottom(), sl = isShowLeft();
        double w = rect.width, h = rect.height;

        // Otherwise, build path based on sides showing and return
        Path path = new Path();
        if(st) { path.moveTo(0,0); path.lineTo(w, 0); }
        if(sr) { if(!st) path.moveTo(w, 0); path.lineTo(w, h); }
        if(sb) { if(!sr) path.moveTo(w, h); path.lineTo(0, h); }
        if(sl) { if(!sb) path.moveTo(0, h); path.lineTo(0,0); }
        return path;
    }

    /**
     * Returns a duplicate stroke with new ShowTop.
     */
    public RMBorderStroke copyForShowTop(boolean aValue)
    {
        RMBorderStroke s = clone(); s._showTop = aValue; return s;
    }

    /**
     * Returns a duplicate stroke with new ShowRight.
     */
    public RMBorderStroke copyForShowRight(boolean aValue)
    {
        RMBorderStroke s = clone(); s._showRight = aValue; return s;
    }

    /**
     * Returns a duplicate stroke with new ShowBottom.
     */
    public RMBorderStroke copyForShowBottom(boolean aValue)
    {
        RMBorderStroke s = clone(); s._showBottom = aValue; return s;
    }

    /**
     * Returns a duplicate stroke with new ShowLeft.
     */
    public RMBorderStroke copyForShowLeft(boolean aValue)
    {
        RMBorderStroke s = clone(); s._showLeft = aValue; return s;
    }

    /**
     * Standard clone implementation.
     */
    protected RMBorderStroke clone()  { return (RMBorderStroke)super.clone(); }

    /**
     * Standard equals implementation.
     */
    public boolean equals(Object anObj)
    {
        // Check identity, super, class and get other
        if(anObj==this) return true;
        if(!super.equals(anObj)) return false;
        RMBorderStroke other = anObj instanceof RMBorderStroke ? (RMBorderStroke)anObj : null; if (other==null) return false;

        // Check ShowLeft, ShowRight, ShowTop, ShowBottom
        if(other._showLeft!=_showLeft) return false;
        if(other._showRight!=_showRight) return false;
        if(other._showTop!=_showTop) return false;
        if(other._showBottom!=_showBottom) return false;
        return true; // Return true since all checks passed
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic stroke attributes
        XMLElement e = super.toXML(anArchiver); e.add("type", "border");

        // Archive ShowLeft, ShowRight, ShowTop, ShowBottom
        if(!isShowLeft()) e.add("show-left", false);
        if(!isShowRight()) e.add("show-right", false);
        if(!isShowTop()) e.add("show-top", false);
        if(!isShowBottom()) e.add("show-bottom", false);
        return e;
    }

    /**
     * XML unarchival.
     */
    public RMBorderStroke fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic stroke attributes
        super.fromXML(anArchiver, anElement);

        // Unarchive ShowLeft, ShowRight, ShowTop, ShowBottom
        if(anElement.hasAttribute("show-left")) _showLeft = anElement.getAttributeBoolValue("show-left");
        if(anElement.hasAttribute("show-right")) _showRight = anElement.getAttributeBoolValue("show-right");
        if(anElement.hasAttribute("show-top")) _showTop = anElement.getAttributeBoolValue("show-top");
        if(anElement.hasAttribute("show-bottom")) _showBottom = anElement.getAttributeBoolValue("show-bottom");
        return this;
    }
}
/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.scene;
import snap.geom.Line;
import snap.geom.Point;
import snap.geom.Shape;
import snap.geom.Transform;
import snap.gfx.*;
import snap.util.*;

/**
 * This class represents a simple line view, drawing a line from a start point to an end point.
 */
public class SGLine extends SGParent {
    
    /**
     * Creates a basic line (a point actually at 0,0).
     */
    public SGLine()  { setBorder(Border.blackBorder()); }

    /**
     * Creates a basic black line from the given x1, y1 to the given x2, y2.
     */
    public SGLine(double x1, double y1, double x2, double y2)  { this(); setFrame(x1, y1, x2-x1, y2-y1); }

    /**
     * Returns the line path.
     */
    public Shape getPath()
    {
        double x1 = _width<0? -_width : 0, y1 = _height<0? -_height : 0;
        double x2 = _width>0? _width : 0, y2 = _height>0? _height : 0;
        return new Line(x1, y1, x2, y2);
    }

    /**
     * Returns the line segment arrow head.
     */
    public ArrowHead getArrowHead()  { return getChildCount()==0? null : (ArrowHead)getChild(0); }

    /**
     * Sets the line segment arrow head.
     */
    public void setArrowHead(ArrowHead anArrowHead)
    {
        repaint(); // Register for repaint
        removeChildren(); // Remove children then add arrow head
        if (anArrowHead!=null) addChild(anArrowHead);
    }

    /**
     * Override to prevent arrow heads from selecting.
     */
    public boolean childrenSuperSelectImmediately()  { return false; }

    /**
     * Override to handle arrow heads special.
     */
    @Override
    public void setBorder(Border aBorder)
    {
        // Sets stroke
        super.setBorder(aBorder);

        // Get arrow head (just return if null)
        ArrowHead arrowHead = getArrowHead(); if(arrowHead==null) return;

        // If stroke, set arrow head to stroke color and scale to line width
        if (aBorder!=null) {
            arrowHead.setFillColor(aBorder.getColor());
            arrowHead.setScaleXY(aBorder.getWidth(), aBorder.getWidth());
        }

        // Otherwise, just clear arrow head fill
        else arrowHead.setFill(null);
    }

    /**
     * Override to handle arrow heads special.
     */
    public void setBorderColor(Color aColor)
    {
        super.setBorderColor(aColor);
        if (getArrowHead()!=null) getArrowHead().setFillColor(aColor);
    }

    /**
     * Override to handle arrow heads special.
     */
    public void setBorderWidth(double aValue)
    {
        super.setBorderWidth(aValue);
        if (getArrowHead()!=null)
            getArrowHead().setScaleXY(aValue, aValue);
    }

    /**
     * Override to layout arrow heads.
     */
    protected void layoutImpl()
    {
        // Get line and arrow head (just return if no arrow head)
        ArrowHead head = getArrowHead(); if(head==null) return;

        // Use width() instead of getWidth(), because we need the sign
        double w = width(), h = height();
        double angle = Math.atan2(h, w)*180/Math.PI;
        double x = w<0? 0 : w;
        double y = h<0? 0 : h;

        // Reset head location
        head.setRoll(angle);
        head.setXY(0,0);
        Point originInParent = head.getOriginInParent();
        head.setXY(x - originInParent.x, y - originInParent.y);
    }

    /**
     * Override to prevent width from going to zero.
     */
    public void setWidth(double aWidth)
    {
        if (getArrowHead()!=null && Math.abs(aWidth)<=0.15) super.setWidth(0);
        else super.setWidth(aWidth);
    }

    /**
     * Override to prevent width from going to zero.
     */
    public void setHeight(double aHeight)
    {
        if (getArrowHead()!=null && Math.abs(aHeight)<=0.15) super.setHeight(0);
        else super.setHeight(aHeight);
    }

    /**
     * XML archival.
     */
    protected XMLElement toXMLView(XMLArchiver anArchiver)
    {
        XMLElement e = super.toXMLView(anArchiver); e.setName("line"); return e;
    }

    /**
     * A shape for RMLineShape arrow head.
     */
    public static class ArrowHead extends SGPolygon {

        // Point, in path coords, that gets attached to end (for arrowheads) or start (for arrow tails) of the line
        Point     _originInPath, _originInShape;

        /** Returns the point, in the shape's coordinate system, of the origin (attachment point). */
        public Point getOrigin()  { return _originInPath; }

        /** Returns the line origin in parent. */
        public Point getOriginInParent()  { return localToParent(_originInShape); }

        /** Override to trigger line relayout. */
        public void setScaleX(double aVal)  { super.setScaleX(aVal); if(getParent()!=null) getParent().relayout(); }
        public void setScaleY(double aVal)  { super.setScaleY(aVal); if(getParent()!=null) getParent().relayout(); }

        /** XML archival. */
        public XMLElement toXMLView(XMLArchiver anArchiver)
        {
            XMLElement e = super.toXMLView(anArchiver); e.setName("arrow-head");
            if (_originInPath.x != 0) e.add("xorigin", _originInPath.x);
            if (_originInPath.y != 0) e.add("yorigin", _originInPath.y);
            return e;
        }

        /** XML unarchival. */
        protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
        {
            super.fromXMLView(anArchiver, anElement);
            float x = anElement.getAttributeFloatValue("xorigin", 0);
            float y = anElement.getAttributeFloatValue("yorigin", 0);
            _originInPath = new Point(x,y);

            // Get origin in shape coords from origin in path coords
            _originInShape = Transform.getTransformBetweenRects(_path.getBounds(), getBoundsLocal()).transformXY(x, y);
        }
    }
}
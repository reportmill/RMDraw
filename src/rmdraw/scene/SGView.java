/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.scene;
import java.util.*;
import snap.geom.*;
import snap.gfx.*;
import snap.text.TextFormat;
import snap.util.*;
import snap.util.XMLArchiver.*;
import snap.view.*;

/**
 * This class is the basis for all graphic elements in a ReportMill document. You'll rarely use this class directly,
 * however, it encapsulates all the basic view attributes and the most common methods used in template manipulation,
 * like setX(), setY(), setWidth(), setColor(), etc.
 *
 * Here's an example of programatically adding a watermark to a document:
 * <p><blockquote><pre>
 *   Font font = RMFont.getFont("Arial Bold", 72);
 *   Color color = new Color(.9f, .9f, .9f);
 *   RichText string = new RichText("REPORTMILL", font, color);
 *   SGText text = new SGText(string);
 *   myDocument.getPage(0).addChild(text);
 *   text.setBounds(36, 320, 540, 140);
 *   text.setRoll(45);
 *   text.setOpacity(.667f);
 * </pre></blockquote>
 */
public class SGView extends PropObject implements Cloneable, Archivable, Key.GetSet {

    // X location of view
    double         _x = 0;
    
    // Y location of view
    double         _y = 0;
    
    // Width of view
    double         _width = 0;
    
    // Height of view
    double         _height = 0;
    
    // An array to hold optional roll/scale/skew values
    protected double[]  _rss;
    
    // The border for this view
    private Border  _border = null;
    
    // The fill for this view
    private Paint  _fill = null;
    
    // The effect for this view
    private Effect  _effect = null;
    
    // The opacity of view
    private double  _opacity = 1;
    
    // Whether this view is visible
    private boolean  _visible = true;
    
    // The parent of this view
    protected SGParent _parent = null;
    
    // A string describing how this view should autosize in an SGSpringsView
    private String  _asize;
    protected Object _springInfo;
    
    // Map to hold less used attributes (name, url, etc.)
    private SGViewSharedMap _attrMap = SHARED_MAP;
    
    // A shared/root RMSharedMap (cloned to turn on shared flag)
    private static final SGViewSharedMap SHARED_MAP = new SGViewSharedMap().clone();
    
    // Constants for properties
    public static final String X_Prop = "X";
    public static final String Y_Prop = "Y";
    public static final String Width_Prop = "Width";
    public static final String Height_Prop = "Height";
    public static final String Roll_Prop = "Roll";
    public static final String ScaleX_Prop = "ScaleX";
    public static final String ScaleY_Prop = "ScaleY";
    public static final String SkewX_Prop = "SkewX";
    public static final String SkewY_Prop = "SkewY";
    public static final String Border_Prop = "Border";
    public static final String Fill_Prop = "Fill";
    public static final String Effect_Prop = "Effect";
    public static final String Opacity_Prop = "Opacity";
    public static final String Name_Prop = "Name";
    public static final String Visible_Prop = "Visible";
    public static final String Locked_Prop = "Locked";
    public static final String MinWidth_Prop = "MinWidth";
    public static final String MinHeight_Prop = "MinHeight";
    public static final String PrefWidth_Prop = "PrefWidth";
    public static final String PrefHeight_Prop = "PrefHeight";
    
    /**
     * Returns raw x location of view. Developers should use the more common getX, which presents positive x.
     */
    public double x()  { return _x; }

    /**
     * Returns raw y location of view. Developers should use the more common getY, which presents positive y.
     */
    public double y()  { return _y; }

    /**
     * Returns raw width of view. Developers should use the more common getWidth, which presents positive width.
     */
    public double width()  { return _width; }

    /**
     * Returns raw height of view. Developers should use the more common getHeight, which presents positive height.
     */
    public double height()  { return _height; }

    /**
     * Returns the X location of the view.
     */
    public double getX()  { return _width<0? _x + _width : _x; }

    /**
     * Sets the X location of the view.
     */
    public void setX(double aValue)
    {
        if(_x==aValue) return;
        repaint();
        firePropChange(X_Prop, _x, _x = aValue);
        repaint();
    }

    /**
     * Returns the Y location of the view.
     */
    public double getY()  { return _height<0? _y + _height : _y; }

    /**
     * Sets the Y location of the view.
     */
    public void setY(double aValue)
    {
        if(_y==aValue) return;
        repaint();
        firePropChange(Y_Prop, _y, _y = aValue);
        repaint();
    }

    /**
     * Returns the width of the view.
     */
    public double getWidth()  { return _width<0? -_width : _width; }

    /**
     * Sets the width of the view.
     */
    public void setWidth(double aValue)
    {
        double old = getWidth(); if(aValue==old) return;
        if(old>aValue) repaint();
        firePropChange(Width_Prop, _width, _width = aValue);
        if(old<aValue) repaint();
    }

    /**
     * Returns the height of the view.
     */
    public double getHeight()  { return _height<0? -_height : _height; }

    /**
     * Sets the height of the view.
     */
    public void setHeight(double aValue)
    {
        double old = getHeight(); if(aValue==old) return;
        if(old>aValue) repaint();
        firePropChange(Height_Prop, _height, _height = aValue);
        if(old<aValue) repaint();
    }

    /**
     * Returns the max X of the view (assumes not rotated, scaled or skewed).
     */
    public double getMaxX()  { return getX() + getWidth(); }

    /**
     * Returns the max Y of the view (assumes not rotated, scaled or skewed).
     */
    public double getMaxY()  { return getY() + getHeight(); }

    /**
     * Returns the XY location of the view as a point.
     */
    public Point getXY()  { return new Point(getX(), getY()); }

    /**
     * Sets the X and Y location of the view to the given point (convenience).
     */
    public void setXY(Point aPoint)  { setXY(aPoint.getX(), aPoint.getY()); }

    /**
     * Sets the X and Y location of the view to the given point (convenience).
     */
    public void setXY(double anX, double aY)  { setX(anX); setY(aY); }

    /**
     * Returns the size of the view.
     */
    public Size getSize()  { return new Size(getWidth(), getHeight()); }

    /**
     * Sets the size of the view.
     */
    public void setSize(Size aSize)  { setSize(aSize.getWidth(), aSize.getHeight()); }

    /**
     * Sets the size of the view.
     */
    public void setSize(double aWidth, double aHeight)  { setWidth(aWidth); setHeight(aHeight); }

    /**
     * Returns the X, Y, width and height of the view as a rect (use getFrame if view has roll/scale/skew).
     */
    public Rect getBounds()  { return new Rect(getX(), getY(), getWidth(), getHeight()); }

    /**
     * Sets X, Y, width and height of view to dimensions in given rect.
     */
    public void setBounds(Rect aRect) { setBounds(aRect.x, aRect.y, aRect.width, aRect.height); }

    /**
     * Sets X, Y, width and height of view to given dimensions.
     */
    public void setBounds(double anX, double aY, double aW, double aH)
    {
        setX(anX); setY(aY); setWidth(aW); setHeight(aH);
    }

    /**
     * Returns the rect in parent coords that fully encloses the view.
     */
    public Rect getFrame()
    {
        if(isRSS()) return localToParent(getBoundsLocal()).getBounds();
        return getBounds();
    }

    /**
     * Sets the bounds of the view such that it exactly fits in the given parent coord rect.
     */
    public void setFrame(Rect aRect)  { setFrame(aRect.x, aRect.y, aRect.width, aRect.height); }

    /**
     * Sets the bounds of the view such that it exactly fits in the given parent coord rect.
     */
    public void setFrame(double anX, double aY, double aWidth, double aHeight)
    {
        setFrameXY(anX, aY);
        setFrameSize(aWidth, aHeight);
    }

    /**
     * Returns the X of the rect that fully encloses the view in parent coords.
     */
    public double getFrameX()  { return isRSS()? getFrameXY().x : getX(); }

    /**
     * Sets a view's X such that its bounds rect (in parent coords) has origin at the given X.
     */
    public void setFrameX(double anX)  { double x = _x + anX - getFrameX(); setX(x); }

    /**
     * Returns the Y of the rect that fully encloses the view in parent coords.
     */
    public double getFrameY()  { return isRSS()? getFrameXY().y : getY(); }

    /**
     * Sets a view's Y such that its bounds rect (in parent coords) has origin at the given Y.
     */
    public void setFrameY(double aY)  { double y = _y + aY - getFrameY(); setY(y); }

    /**
     * Returns the width of the rect that fully encloses the view in parent coords.
     */
    public double getFrameWidth()  { return isRSS()? getFrame().width : getWidth(); }

    /**
     * Returns the height of the rect that fully encloses the view in parent coords.
     */
    public double getFrameHeight()  { return isRSS()? getFrame().height : getHeight(); }

    /**
     * Returns the origin of the view's bounds rect in parent coords.
     */
    public Point getFrameXY()  { return isRSS()? new Point(getFrame().getXY()) : getXY(); }

    /**
     * Sets a view's origin such that its bounds rect (in parent coords) has origin at the given point.
     */
    public void setFrameXY(Point aPoint)  { setFrameXY(aPoint.getX(), aPoint.getY()); }

    /**
     * Sets a view's origin such that its frame (enclosing rect in parent coords) will have the given X and Y.
     */
    public void setFrameXY(double anX, double aY)  { setFrameX(anX); setFrameY(aY); }

    /**
     * Sets the height of the rect that fully encloses the view in parent coords.
     */
    public void setFrameSize(double aWidth, double aHeight)
    {
        // If view not rotated, scaled or skewed, just set and return
        if(!isRSS()) {
            if(_width<0) { setX(_x + (aWidth+_width)); aWidth = -aWidth; }
            if(_height<0) { setY(_y + (aHeight+_height)); aHeight = -aHeight; }
            setSize(aWidth, aHeight); return;
        }

        // Convert X & Y axis to parent coords
        Transform toParent = getLocalToParent();
        Size x_axis = new Size(_width, 0); toParent.transformVector(x_axis);
        Size y_axis = new Size(0, _height); toParent.transformVector(y_axis);

        // Scale widths of X & Y axes in parent coords by ratio of NewWidth/OldWidth
        double sizeByRatio1 = Math.abs(aWidth)/(Math.abs(x_axis.width) + Math.abs(y_axis.width));
        x_axis.width *= sizeByRatio1; y_axis.width *= sizeByRatio1;

        // Scale heights of X & Y axes in parent coords by ratio of NewHeight/OldHeight
        double sizeByRatio2 = Math.abs(aHeight)/(Math.abs(x_axis.height) + Math.abs(y_axis.height));
        x_axis.height *= sizeByRatio2; y_axis.height *= sizeByRatio2;

        // Cache current bounds origin (this shouldn't change)
        Point origin = getFrameXY();

        // Reset current Skew and convert X & Y axis from parent coords
        setSkewXY(0, 0);
        Transform fromParent = getParentToLocal();
        fromParent.transformVector(x_axis);
        fromParent.transformVector(y_axis);

        // Set the size to compensate for the skew
        setSize(x_axis.width, y_axis.height);

        // Calculate new skew angles (or roll, if width or height is zero)
        if(width()==0)
            setRoll(getRoll() - Math.toDegrees(Math.atan(y_axis.width/y_axis.height)));
        else if(height()==0)
            setRoll(getRoll() - Math.toDegrees(Math.atan(x_axis.height/x_axis.width)));
        else {
            setSkewX(Math.toDegrees(Math.atan(x_axis.height/x_axis.width)));
            setSkewY(Math.toDegrees(Math.atan(y_axis.width/y_axis.height)));
        }

        // Reset original bounds origin (it may have been effected by skew changes)
        setFrameXY(origin);
    }

    /**
     * Returns the max X of the view's frame.
     */
    public double getFrameMaxX()  { return isRSS()? getFrame().getMaxX() : getMaxX(); }

    /**
     * Returns the max Y of the view's frame.
     */
    public double getFrameMaxY()  { return isRSS()? getFrame().getMaxY() : getMaxY(); }

    /**
     * Returns the origin point of the view in parent's coords.
     */
    public Point getXYP()  { return localToParent(0,0); }

    /**
     * Sets the origin point of the view to the given X and Y in parent's coords.
     */
    public void setXYP(double anX, double aY)
    {
        // If rotated-scaled-skewd, get XY in parent coords and set XY as an offset from parent
        if(isRSS()) {
            Point p = getXYP();
            setXY(_x + anX - p.getX(), _y + aY - p.getY());
        }

        // If not rotated-scaled-skewed, just set x/y (adjusted if width/height are negative)
        else setXY(_width<0? anX-_width : anX, _height<0? aY-_height : aY);
    }

    /**
     * Offsets the X and Y location of the view by the given dx & dy amount (convenience).
     */
    public void offsetXY(double dx, double dy)  { setXY(_x + dx, _y + dy); }

    /**
     * Returns the bounds local coords.
     */
    public Rect getBoundsLocal()  { return new Rect(0, 0, getWidth(), getHeight()); }

    /**
     * Returns the bounds of the path associated with this view in local coords, adjusted to account for stroke width.
     */
    public Rect getBoundsStroked()
    {
        Rect bnds = getBoundsLocal();
        Border border = getBorder();
        if (border==null) return bnds;
        bnds.inset(-border.getWidth()/2);
        return bnds;
    }

    /**
     * Returns the marked bounds of this view and it's children.
     */
    public Rect getBoundsStrokedDeep()
    {
        // Get normal marked bounds and union with children BoundsStrokedDeep (converted to this view coords)
        Rect bounds = getBoundsStroked();
        for(int i=0, iMax=getChildCount(); i<iMax; i++) { SGView child = getChild(i); if(!child.isVisible()) continue;
            Rect cbounds = child.getBoundsStrokedDeep();
            cbounds = child.localToParent(cbounds).getBounds();
            bounds.unionEvenIfEmpty(cbounds); }
        return bounds;
    }

    /**
     * Returns the bounds of the path associated with this view in local coords, adjusted to account for stroke width.
     */
    public Rect getBoundsMarked()
    {
        Rect bounds = getBoundsStroked();
        if(getEffect()!=null) bounds = getEffect().getBounds(bounds);
        return bounds;
    }

    /**
     * Returns the marked bounds of this view and it's children.
     */
    public Rect getBoundsMarkedDeep()
    {
        // Get normal marked bounds and union with children BoundsMarkedDeep (converted to this view coords)
        Rect bounds = getBoundsMarked();
        for(int i=0, iMax=getChildCount(); i<iMax; i++) { SGView child = getChild(i); if(!child.isVisible()) continue;
            Rect cbounds = child.getBoundsMarkedDeep();
            cbounds = child.localToParent(cbounds).getBounds();
            bounds.unionEvenIfEmpty(cbounds); }
        return bounds;
    }

    /**
     * Returns the roll of the view.
     */
    public double getRoll()  { return _rss==null? 0 : _rss[0]; }

    /**
     * Sets the roll of the view.
     */
    public void setRoll(double aValue)
    {
        aValue = Math.round(aValue*100)/100d; if(aValue==getRoll()) return;
        repaint();
        firePropChange(Roll_Prop, getRSS()[0], _rss[0] = aValue);
        repaint();
    }

    /**
     * Returns the scale of the X axis of the view.
     */
    public double getScaleX()  { return _rss==null? 1 : _rss[1]; }

    /**
     * Sets the scale of the X axis of the view.
     */
    public void setScaleX(double aValue)
    {
        double old = getScaleX(); aValue = Math.round(aValue*100)/100d; if(aValue==old) return;
        if(old>aValue) repaint();
        firePropChange(ScaleX_Prop, old, getRSS()[1] = aValue);
        if(old<aValue) repaint();
    }

    /**
     * Returns the scale of the Y axis of the view.
     */
    public double getScaleY()  { return _rss==null? 1 : _rss[2]; }

    /**
     * Sets the scale of the Y axis of the view.
     */
    public void setScaleY(double aValue)
    {
        double old = getScaleY(); aValue = Math.round(aValue*100)/100d; if(aValue==old) return;
        if(old>aValue) repaint();
        firePropChange(ScaleY_Prop, old, getRSS()[2] = aValue);
        if(old<aValue) repaint();
    }

    /**
     * Sets the scale of the X and Y axis.
     */
    public void setScaleXY(double sx, double sy)  { setScaleX(sx); setScaleY(sy); }

    /**
     * Returns the skew of the X axis of the view.
     */
    public double getSkewX()  { return _rss==null? 0 : _rss[3]; }

    /**
     * Sets the skew of the X axis of the view.
     */
    public void setSkewX(double aValue)
    {
        aValue = Math.round(aValue*100)/100d; if(aValue==getSkewX()) return;
        repaint();
        firePropChange(SkewX_Prop, getRSS()[3], _rss[3] = aValue);
        repaint();
    }

    /**
     * Returns the skew of the Y axis of the view.
     */
    public double getSkewY()  { return _rss==null? 0 : _rss[4]; }

    /**
     * Sets the skew of the Y axis of the view.
     */
    public void setSkewY(double aValue)
    {
        aValue = Math.round(aValue*100)/100d; if(aValue==getSkewY()) return;
        repaint();
        firePropChange(SkewY_Prop, getRSS()[4], _rss[4] = aValue);
        repaint();
    }

    /**
     * Sets the skew of the X and Y axis.
     */
    public void setSkewXY(double skx, double sky)  { setSkewX(skx); setSkewY(sky); }

    /**
     * Returns whether the view has been rotated, scaled or skewed (for efficiency).
     */
    public boolean isRSS()  { return _rss!=null; }

    /**
     * Returns the roll scale skew array: [ Roll, ScaleX, ScaleY, SkewX, SkewY ].
     */
    protected double[] getRSS()  { return _rss!=null? _rss : (_rss=new double[] { 0, 1, 1, 0, 0 }); }

    /**
     * Returns the border for this view.
     */
    public Border getBorder()  { return _border; }

    /**
     * Sets the border for this view.
     */
    public void setBorder(Border aBorder)
    {
        if (SnapUtils.equals(getBorder(), aBorder)) return;
        repaint();
        firePropChange(Border_Prop, _border, _border = aBorder);
    }

    /**
     * Returns the stroke color of the view.
     */
    public Color getBorderColor()  { return getBorder()==null? Color.BLACK : getBorder().getColor(); }

    /**
     * Sets the stroke color of the view.
     */
    public void setBorderColor(Color aColor)
    {
        if (aColor==null) setBorder(null);
        else if (getBorder()==null) setBorder(Border.createLineBorder(aColor, 1));
        else setBorder(getBorder().copyForColor(aColor));
    }

    /**
     * Returns the stroke width of the view's stroke in printer points.
     */
    public double getBorderWidth()
    {
        return getBorder()==null? 0 : getBorder().getWidth();
    }

    /**
     * Sets the stroke width of the view's stroke in printer points.
     */
    public void setBorderWidth(double aValue)
    {
        if (getBorder()==null) setBorder(Border.createLineBorder(Color.BLACK, aValue));
        else setBorder(getBorder().copyForStrokeWidth(aValue));
    }

    /**
     * Sets the stroke for this view, with an option to turn on drawsStroke.
     */
    public void setBorder(Color aColor, double aWidth)
    {
        setBorder(Border.createLineBorder(aColor, aWidth));
    }

    /**
     * Returns the fill for this view.
     */
    public Paint getFill()  { return _fill; }

    /**
     * Sets the fill for this view.
     */
    public void setFill(Paint aFill)
    {
        aFill = aFill!=null ? aFill.snap() : null; // this can go when RMFill is gone
        if (SnapUtils.equals(getFill(), aFill)) return;
        repaint();
        firePropChange(Fill_Prop, _fill, _fill = aFill);
    }

    /**
     * Returns the color of the view.
     */
    public Color getFillColor()  { return getFill()==null? Color.BLACK : getFill().getColor(); }

    /**
     * Sets the color of the view.
     */
    public void setFillColor(Color aColor)
    {
        // Set color
        if (aColor==null) setFill(null);
        else if (getFill()==null) setFill(aColor);
        else setFill(getFill().copyForColor(aColor));
    }

    /**
     * Returns the effect for this view.
     */
    public Effect getEffect()  { return _effect; }

    /**
     * Sets the effect for this view.
     */
    public void setEffect(Effect anEffect)
    {
        if (SnapUtils.equals(getEffect(), anEffect)) return;
        repaint();
        firePropChange(Effect_Prop, _effect, _effect = anEffect); _pdvr1 = _pdvr2 = null;
    }

    /**
     * Returns the opacity of the view (1 for opaque, 0 for transparent).
     */
    public double getOpacity()  { return _opacity; }

    /**
     * Sets the opacity of the view (1 for opaque, 0 for transparent).
     */
    public void setOpacity(double aValue)
    {
        if (aValue==getOpacity()) return; // If value already set, just return
        repaint(); // Register repaint
        firePropChange(Opacity_Prop, _opacity, _opacity = aValue);
    }

    /**
     * Returns the combined opacity of this view and its parent.
     */
    public double getOpacityDeep()
    {
        double op = getOpacity();
        for (SGView s = _parent; s!=null; s=s._parent) op *= s.getOpacity();
        return op;
    }

    /**
     * Returns whether this view is visible.
     */
    public boolean isVisible()  { return _visible; }

    /**
     * Sets whether this view is visible.
     */
    public void setVisible(boolean aValue)
    {
        if (isVisible()==aValue) return;
        firePropChange(Visible_Prop, _visible, _visible = aValue);
    }

    /**
     * Returns the autosizing settings as a string with hyphens for struts and tilde for sprints (horiz,vert).
     */
    public String getAutosizing()  { return _asize!=null? _asize : getAutosizingDefault(); }

    /**
     * Sets the autosizing settings as a string with hyphens for struts and tilde for sprints (horiz,vert).
     */
    public void setAutosizing(String aValue)
    {
        if (aValue!=null && (aValue.length()<7 || !(aValue.charAt(0)=='-' || aValue.charAt(0)=='~'))) {
            System.err.println("SGView.setAutosizing: Invalid string: " + aValue); return; }
        if (SnapUtils.equals(aValue, _asize)) return;
        firePropChange("Autosizing", _asize, _asize = aValue);
    }

    /**
     * Returns the autosizing default.
     */
    public String getAutosizingDefault()  { return "--~,--~"; }

    /**
     * Returns whether this view is hittable in its parent.
     */
    public boolean isHittable()  { return isVisible() && (_parent==null || _parent.isHittable(this)); }

    /**
     * Returns whether font has been set.
     */
    public boolean isFontSet()  { return false; }

    /**
     * Returns the font for the view (defaults to parent font).
     */
    public Font getFont()  { return getParent()!=null ? getParent().getFont() : null; }

    /**
     * Sets the font for the view.
     */
    public void setFont(Font aFont)  { }

    /**
     * Returns the alignment.
     */
    public Pos getAlign()
    {
        return Pos.get(getAlignX(), getAlignY());
    }

    /**
     * Sets the alignment.
     */
    public void setAlign(Pos aPos)
    {
        setAlignX(aPos.getHPos());
        setAlignY(aPos.getVPos());
    }

    /**
     * Returns the horizontal alignment.
     */
    public HPos getAlignX()  { return HPos.LEFT; }

    /**
     * Sets the horizontal alignment.
     */
    public void setAlignX(HPos anAlignX)  { }

    /**
     * Returns the vertical alignment.
     */
    public VPos getAlignY()  { return VPos.TOP; }

    /**
     * Sets the vertical alignment.
     */
    public void setAlignY(VPos anAlignX)  { }

    /**
     * Returns the format for the view.
     */
    public TextFormat getFormat()  { return null; } //if(getBindingCount()>0) return (RMFormat)getBinding(0).getFormat();

    /**
     * Sets the format for the view.
     */
    public void setFormat(TextFormat aFormat)
    {
        // Add format to first binding
        //if((aFmt==null || aFmt instanceof java.text.Format) && getBindingCount()>0) getBinding(0).setFormat(aFmt);

        // Pass down to children
        for(int i=0, iMax=getChildCount(); i<iMax; i++)
            getChild(i).setFormat(aFormat);
    }

    /**
     * Returns the name for the view.
     */
    public String getName()  { return (String)get("Name"); }

    /**
     * Sets the name for the view.
     */
    public void setName(String aName)
    {
        if(SnapUtils.equals(aName, getName())) return;
        Object oldVal = put("Name", StringUtils.min(aName));
        firePropChange("Name", oldVal, StringUtils.min(aName));
    }

    /**
     * Sets the URL for the view.
     */
    public String getURL()  { return (String)get("SGViewURL"); }

    /**
     * Returns the URL for the view.
     */
    public void setURL(String aURL)
    {
        if(SnapUtils.equals(aURL, getURL())) return;
        Object oldVal = put("SGViewURL", StringUtils.min(aURL));
        firePropChange("SGViewURL", oldVal, aURL);
    }

    /**
     * Returns the locked state of the view (really just to prevent location/size changes in the editor).
     */
    public boolean isLocked()  { return SnapUtils.boolValue(get("Locked")); }

    /**
     * Sets the locked state of the view (really just to prevent location/size changes in the editor).
     */
    public void setLocked(boolean aValue)
    {
        if(aValue==isLocked()) return;
        Object oldVal = put("Locked", aValue);
        firePropChange(Locked_Prop, oldVal, aValue);
    }

    /**
     * Returns the Object associated with the given name for the view.
     * This is a general purpose property facility to allow views to hold many less common properties without the overhead
     * of explicitly including ivars for them. The map that holds these properties is shared so that there is only ever one
     * instance of the map for each unique permutation of attributes.
     */
    public Object get(String aName)  { return _attrMap.get(aName); }

    /**
     * Returns the value associated with given key, using the given default if not found.
     */
    public Object get(String aName, Object aDefault)  { Object val = get(aName); return val!=null? val : aDefault; }

    /**
     * Sets a value to be associated with the given name for the view.
     */
    public Object put(String aName, Object anObj)
    {
        // If map shared, clone it for real
        if(_attrMap.isShared) _attrMap = _attrMap.cloneReal();

        // Put value (or remove if null)
        return anObj!=null? _attrMap.put(aName, anObj) : _attrMap.remove(aName);
    }

    /**
     * Returns the view's path.
     */
    public Shape getPath()  { return new Rect(0, 0, getWidth(), getHeight()); }

    /**
     * Returns the parent of this view.
     */
    public SGParent getParent()  { return _parent; }

    /**
     * Sets the parent of this view (called automatically by addChild()).
     */
    public void setParent(SGParent aView)  { _parent = aView; }

    /**
     * Returns the first parent with given class by iterating up parent hierarchy.
     */
    public <T extends SGView> T getParent(Class<T> aClass)
    {
        for(SGView s = getParent(); s!=null; s=s.getParent()) if(aClass.isInstance(s)) return (T)s;
        return null; // Return null since parent of class wasn't found
    }

    /**
     * Removes this view from it's parent.
     */
    public void removeFromParent()  { if(_parent!=null) _parent.removeChild(this); }

    /**
     * Returns the index of this child in its parent.
     */
    public int indexOf()  { return _parent!=null? _parent.indexOfChild(this) : -1; }

    /**
     * Returns the child count.
     */
    public int getChildCount()  { return 0; }

    /**
     * Returns the child at given index.
     */
    public SGView getChild(int anIndex)  { return null; }

    /**
     * Returns the children list.
     */
    public List <SGView> getChildren()  { return Collections.emptyList(); }

    /**
     * Returns the SceneGraph that owns the whole view tree.
     */
    public SceneGraph getSceneGraph()  { return _parent!=null? _parent.getSceneGraph() : null; }

    /**
     * Returns the Document ancestor of this view.
     */
    public SGDoc getDoc()  { return _parent!=null? _parent.getDoc() : null; }

    /**
     * Returns the Page ancestor of this view (or null if not there).
     */
    public SGParent getPage()  { return _parent!=null? _parent.getPage() : (SGParent)this; }

    /**
     * Returns the undoer for this view (or null if not there).
     */
    public Undoer getUndoer()
    {
        SceneGraph scene = getSceneGraph();
        return scene!=null ? scene.getUndoer() : null;
    }

    /**
     * Undoer convenience - sets title of next registered undo.
     */
    public void undoerSetUndoTitle(String aTitle)
    {
        Undoer u = getUndoer();
        if (u!=null) u.setUndoTitle(aTitle);
    }

    /**
     * Undoer convenience - disable the undoer.
     */
    public void undoerDisable()  { Undoer u = getUndoer(); if (u!=null) u.disable(); }

    /**
     * Undoer convenience - enables the undoer.
     */
    public void undoerEnable()  { Undoer u = getUndoer(); if (u!=null) u.enable(); }

    /**
     * Returns the number of ancestors (from this view's parent up to the document).
     */
    public int getAncestorCount()
    {
        int count = 0;
        for (SGView p = getParent(); p!=null; p=p.getParent()) count++;
        return count;
    }

    /**
     * Returns true if given view is one of this view's ancestors.
     */
    public boolean isAncestor(SGView aView)
    {
        return aView==_parent || (_parent!=null && _parent.isAncestor(aView));
    }

    /**
     * Returns true if given view is one of this view's descendants.
     */
    public boolean isDescendant(SGView aView)
    {
        return aView!=null && aView.isAncestor(this);
    }

    /**
     * Converts a point from local to parent.
     */
    public Point localToParent(double aX, double aY)
    {
        if (isTransformSimple()) return new Point(aX+getX(),aY+getY());
        return getLocalToParent().transformXY(aX, aY);
    }

    /**
     * Converts a point from local to given parent.
     */
    public Point localToParent(double aX, double aY, SGView aPar)
    {
        Point point = new Point(aX,aY);
        for (SGView n = this; n!=aPar && n!=null; n=n.getParent()) {
            if (n.isTransformSimple()) point.offset(n.getX(),n.getY());
            else point = n.localToParent(point.x,point.y);
        }
        return point;
    }

    /**
     * Converts a point from local to parent.
     */
    public Point localToParent(Point aPoint)  { return localToParent(aPoint.x, aPoint.y); }

    /**
     * Converts a point from local to given parent.
     */
    public Point localToParent(Point aPoint, SGView aPar)  { return localToParent(aPoint.x, aPoint.y, aPar); }

    /**
     * Converts a shape from local to parent.
     */
    public Shape localToParent(Shape aShape)
    {
        Transform xfm = getLocalToParent();
        return aShape.copyFor(xfm);
    }

    /**
     * Converts a point from local to given parent.
     */
    public Shape localToParent(Shape aShape, SGView aPar)
    {
        Transform xfm = getLocalToParent(aPar);
        return aShape.copyFor(xfm);
    }

    /**
     * Converts a point from parent to local.
     */
    public Point parentToLocal(double aX, double aY)
    {
        if (isTransformSimple()) return new Point(aX-getX(),aY-getY());
        return getParentToLocal().transformXY(aX, aY);
    }

    /**
     * Converts a point from given parent to local.
     */
    public Point parentToLocal(double aX, double aY, SGView aPar)  { return getParentToLocal(aPar).transformXY(aX,aY); }

    /**
     * Converts a point from parent to local.
     */
    public Point parentToLocal(Point aPoint)  { return parentToLocal(aPoint.x, aPoint.y); }

    /**
     * Converts a point from given parent to local.
     */
    public Point parentToLocal(Point aPoint, SGView aPar)  { return parentToLocal(aPoint.x, aPoint.y, aPar); }

    /**
     * Converts a shape from parent to local.
     */
    public Shape parentToLocal(Shape aShape)
    {
        Transform xfm = getParentToLocal();
        return aShape.copyFor(xfm);
    }

    /**
     * Converts a shape from parent to local.
     */
    public Shape parentToLocal(Shape aShape, SGView aPar)
    {
        Transform xfm = getParentToLocal(aPar);
        return aShape.copyFor(xfm);
    }

    /**
     * Returns the transform.
     */
    public Transform getLocalToParent()  { return getTransform(); }

    /**
     * Returns the transform.
     */
    public Transform getLocalToParent(SGView aPar)
    {
        Transform tfm = getLocalToParent();
        for (SGView shp = getParent(); shp!=aPar && shp!=null; shp=shp.getParent()) {
            if (shp.isTransformSimple()) tfm.preTranslate(shp.getX(),shp.getY());
            else tfm.multiply(shp.getLocalToParent());
        }
        return tfm;
    }

    /**
     * Returns the transform from parent to local coords.
     */
    public Transform getParentToLocal()
    {
        if (isTransformSimple()) return new Transform(-getX(), -getY());
        Transform tfm = getLocalToParent(); tfm.invert();
        return tfm;
    }

    /**
     * Returns the transform from parent to local coords.
     */
    public Transform getParentToLocal(SGView aPar)
    {
        Transform tfm = getLocalToParent(aPar); tfm.invert();
        return tfm;
    }

    /**
     * Returns whether transform to parent is simple (contains no rotate, scale, skew).
     */
    public boolean isTransformSimple()  { return !isRSS(); }

    /**
     * Returns the transform to this view from its parent.
     */
    public Transform getTransform()
    {
        // Create transform (if not rotated/scaled/skewed, just translate and return)
        Transform t = new Transform(getX(), getY()); if(!isRSS()) return t;

        // Get location, size, point of rotation, rotation, scale, skew
        double w = getWidth(), h = getHeight();
        double prx = w/2, pry = h/2;
        double roll = getRoll();
        double sx = getScaleX(), sy = getScaleY();
        double skx = getSkewX(), sky = getSkewY();

        // Transform about point of rotation and return
        t.translate(prx, pry);
        if (roll!=0) t.rotate(roll);
        if (sx!=1 || sy!=1) t.scale(sx, sy);
        if (skx!=0 || sky!=0) t.skew(skx, sky);
        t.translate(-prx, -pry); return t;
    }

    /**
     * Returns whether view minimum width is set.
     */
    public boolean isMinWidthSet()  { return get(MinWidth_Prop)!=null; }

    /**
     * Returns the view minimum width.
     */
    public double getMinWidth()  { Double w = (Double)get(MinWidth_Prop); return w!=null? w : 0; }

    /**
     * Sets the view minimum width.
     */
    public void setMinWidth(double aWidth)
    {
        double w = aWidth<=0? 0 : aWidth; if (w==getMinWidth()) return;
        firePropChange(MinWidth_Prop, put(MinWidth_Prop, w), w);
    }

    /**
     * Returns whether view minimum height is set.
     */
    public boolean isMinHeightSet()  { return get(MinHeight_Prop)!=null; }

    /**
     * Returns the view minimum height.
     */
    public double getMinHeight()  { Double h = (Double)get(MinHeight_Prop); return h!=null? h : 0; }

    /**
     * Sets the view minimum height.
     */
    public void setMinHeight(double aHeight)
    {
        double h = aHeight<=0? 0 : aHeight; if(h==getMinHeight()) return;
        firePropChange(MinHeight_Prop, put(MinHeight_Prop, h), h);
    }

    /**
     * Sets the view minimum size.
     */
    public void setMinSize(double aWidth, double aHeight)  { setMinWidth(aWidth); setMinHeight(aHeight); }

    /**
     * Returns whether view preferred width is set.
     */
    public boolean isPrefWidthSet()  { return get(PrefWidth_Prop)!=null; }

    /**
     * Returns the view preferred width.
     */
    public double getPrefWidth()
    {
        Double v = (Double)get(PrefWidth_Prop); if (v!=null) return v;
        return getPrefWidthImpl(-1);
    }

    /**
     * Sets the view preferred width.
     */
    public void setPrefWidth(double aWidth)
    {
        double w = aWidth<=0? 0 : aWidth; if (w==getPrefWidth()) return;
        firePropChange(PrefWidth_Prop, put(PrefWidth_Prop, w), w);
    }

    /**
     * Returns whether view preferred height is set.
     */
    public boolean isPrefHeightSet()  { return get(PrefHeight_Prop)!=null; }

    /**
     * Returns the view preferred height.
     */
    public double getPrefHeight()
    {
        Double v = (Double)get(PrefHeight_Prop); if (v!=null) return v;
        return getPrefHeightImpl(-1);
    }

    /**
     * Sets the view preferred height.
     */
    public void setPrefHeight(double aHeight)
    {
        double h = aHeight<=0? 0 : aHeight; if (h==getPrefHeight()) return;
        firePropChange(PrefHeight_Prop, put(PrefHeight_Prop, h), h);
    }

    /**
     * Computes the preferred width for given height.
     */
    protected double getPrefWidthImpl(double aHeight)  { return getWidth(); }

    /**
     * Computes the preferred height for given width.
     */
    protected double getPrefHeightImpl(double aWidth)  { return getHeight(); }

    /**
     * Returns the best width for current height.
     */
    public double getBestWidth()  { return Math.max(getMinWidth(), getPrefWidth()); }

    /**
     * Returns the best height for current width.
     */
    public double getBestHeight()  { return Math.max(getMinHeight(), getPrefHeight()); }

    /**
     * Sets the view to its best height (which is just the current height for most views).
     */
    public void setBestHeight()  { setHeight(getBestHeight()); }

    /**
     * Sets the view to its best size.
     */
    public void setBestSize()
    {
        setWidth(getBestWidth());
        setHeight(getBestHeight());
    }

    /**
     * Divides this view by given amount from top edge. Returns remainder view with bounds set to the remainder.
     */
    public SGView divideViewFromTop(double anAmt)
    {
        // Create remainder view
        SGView newView = createDivideViewRemainder((byte)0);

        // Get bounds for this view and remainder bounds (split by amount from top)
        Rect bnds0 = getFrame();
        Rect bnds1 = bnds0.clone();
        bnds0.height = anAmt;
        bnds1.y += anAmt; bnds1.height -= anAmt;

        // Set this view's new bounds and NewView bounds as remainder
        setFrame(bnds0);
        newView.setFrame(bnds1);
        return newView;
    }

    /**
     * Divides this view by given amount from left edge. Returns remainder view with bounds set to the remainder.
     */
    public SGView divideViewFromLeft(double anAmt)
    {
        // Create remainder view
        SGView newView = createDivideViewRemainder((byte)1);

        // Get bounds for this view and remainder bounds (split by amount from left)
        Rect bnds0 = getFrame();
        Rect bnds1 = bnds0.clone();
        bnds0.width = anAmt;
        bnds1.x += anAmt; bnds1.width -= anAmt;

        // Set this view's new bounds and NewView bounds as remainder
        setFrame(bnds0);
        newView.setFrame(bnds1);
        return newView;
    }

    /**
     * Creates a view suitable for the "remainder" portion of a divideView call (just a clone by default).
     */
    protected SGView createDivideViewRemainder(byte anEdge)  { return clone(); }

    /**
     * Returns whether view accepts mouse events (true if URL is present).
     */
    public boolean acceptsMouse()  { return getURL()!=null; }

    /**
     * Handle view events.
     */
    public void processEvent(ViewEvent anEvent)  { }

    /**
     * Returns whether this view is hit by the point, given in this view's parent's coords.
     */
    public boolean contains(Point aPoint)
    {
        // Get line width to be used in contain test
        double lineWidth = getBorderWidth();

        // If polygon or line, make line width effectively at least 8, so users will have a better shot of selecting it
        if (this instanceof SGPolygon || this instanceof SGLine)
            lineWidth = Math.max(8, getBorderWidth());

        // Get bounds, adjusted for line width
        Rect bounds = getBoundsLocal();
        bounds.inset(-lineWidth/2, -lineWidth/2);

        // If point isn't even in bounds rect, just return false
        if (!bounds.contains(aPoint.getX(), aPoint.getY()))
            return false;

        // Get shape in bounds rect and return whether shape intersects point
        Shape path = getPath();
        return path.contains(aPoint.getX(), aPoint.getY(), lineWidth);
    }

    /**
     * Returns whether this view is hit by the path, given in this view's parent's coords.
     */
    public boolean intersects(Shape aPath)
    {
        // Get line width to be used in intersects test
        double lineWidth = getBorderWidth();

        // Get bounds, adjusted for line width
        Rect bounds = getBoundsLocal();
        bounds.inset(-lineWidth/2, -lineWidth/2);

        // If paths don't even intersect bounds, just return false
        if (!aPath.getBounds().intersectsRect(bounds))
            return false;

        // Get shape in bounds and return whether shape intersects given path
        Shape path = getPath();
        return path.intersects(aPath, lineWidth);
    }

    /**
     * Returns the dataset key associated with this view.
     */
    public String getDatasetKey()  { return null; }

    /**
     * Returns the property names for helper's instance class.
     */
    public String[] getPropNames()
    {
        return new String[] { Visible_Prop, X_Prop, Y_Prop, Width_Prop, Height_Prop, Roll_Prop, ScaleX_Prop, ScaleY_Prop,
            "Font", "TextColor", "FillColor", "StrokeColor", "URL" };
    }

    /**
     * Returns the number of bindings associated with view.
     */
    public int getBindingCount()
    {
        List bindings = getBindings(false);
        return bindings!=null ? bindings.size() : 0;
    }

    /**
     * Returns the individual binding at the given index.
     */
    public Binding getBinding(int anIndex)  { return getBindings(true).get(anIndex); }

    /**
     * Returns the list of bindings, with an option to create if missing.
     */
    protected List <Binding> getBindings(boolean doCreate)
    {
        List <Binding> bindings = (List)get("RMBindings");
        if (bindings==null && doCreate) put("RMBindings", bindings = new ArrayList());
        return bindings;
    }

    /**
     * Adds the individual binding to the view's bindings list.
     */
    public void addBinding(Binding aBinding)
    {
        removeBinding(aBinding.getPropertyName()); // Remove current binding for property name (if it exists)
        List <Binding> bindings = getBindings(true); // Add binding
        bindings.add(aBinding);
        aBinding.setView(this); // Set binding width to this view
    }

    /**
     * Removes the binding at the given index from view's bindings list.
     */
    public Binding removeBinding(int anIndex)  { return getBindings(true).remove(anIndex); }

    /**
     * Returns the individual binding with the given property name.
     */
    public Binding getBinding(String aPropertyName)
    {
        // Iterate over bindings and return the first that matches given property name
        for (int i=0, iMax=getBindingCount(); i<iMax; i++)
            if (getBinding(i).getPropertyName().equals(aPropertyName))
                return getBinding(i);
        return null; // Return null since binding not found
    }

    /**
     * Removes the binding with given property name.
     */
    public boolean removeBinding(String aPropertyName)
    {
        // Iterate over binding and remove given binding
        for (int i=0, iMax=getBindingCount(); i<iMax; i++)
            if (getBinding(i).getPropertyName().equals(aPropertyName)) {
                removeBinding(i); return true; }
        return false; // Return false since binding not found
    }

    /**
     * Adds a binding for given name and key.
     */
    public void addBinding(String aPropName, String aKey)  { addBinding(new Binding(aPropName, aKey)); }

    /**
     * Standard implementation of Object clone. Null's out view's parent and children.
     */
    public SGView clone()
    {
        // Do normal version
        SGView clone; try { clone = (SGView)super.clone(); }
        catch(CloneNotSupportedException e) { throw new RuntimeException(e); }

        // Clear Parent
        clone._parent = null;

        // Clone Rotate/Scale/Skew array
        if (_rss!=null) clone._rss = Arrays.copyOf(_rss,_rss.length);

        // Copy attributes map
        clone._attrMap = _attrMap.clone();

        // Clone bindings and add to clone (with hack to make sure clone has it's own, non-shared, attr map)
        for (int i=0, iMax=getBindingCount(); i<iMax; i++) {
            if (i==0) clone.put("RMBindings", null);
            clone.addBinding(getBinding(i).clone());
        }

        // Return clone
        return clone;
    }

    /**
     * Clones all attributes of this view with complete clones of its children as well.
     */
    public SGView cloneDeep()  { return clone(); }

    /**
     * Copies basic view attributes from given View (location, size, fill, stroke, roll, scale, name, url, etc.).
     */
    public void copyView(SGView aView)
    {
        // Copy bounds
        setBounds(aView._x, aView._y, aView._width, aView._height);

        // Copy roll, scale, skew
        if (aView.isRSS()) {
            setRoll(aView.getRoll());
            setScaleXY(aView.getScaleX(), aView.getScaleY());
            setSkewXY(aView.getSkewX(), aView.getSkewY());
        }

        // Copy Stroke, Fill, Effect
        setBorder(aView.getBorder());
        setFill(aView.getFill());
        setEffect(aView.getEffect());

        // Copy Opacity and Visible
        setOpacity(aView.getOpacity());
        setVisible(aView.isVisible());

        // Copy Name, URL, Autosizing
        setName(aView.getName());
        setURL(aView.getURL());
        setAutosizing(aView.getAutosizing());

        // Copy bindings
        while (getBindingCount()>0) removeBinding(0);
        for (int i=0, iMax=aView.getBindingCount(); i<iMax; i++)
            addBinding(aView.getBinding(i).clone());
    }

    /**
     * Called to relayout.
     */
    public void relayout()  { }

    /**
     * Called to notify parents to relayout because preferred sizes have potentially changed.
     */
    public void relayoutParent()
    {
        SGParent par = getParent(); if(par==null) return;
        par.relayout(); par.relayoutParent();
    }

    /**
     * Called to register view for repaint.
     */
    public void repaint()
    {
        SceneGraph sceneGraph = getSceneGraph(); if (sceneGraph==null) return;
        sceneGraph.repaintSceneForView(this);
    }

    /** Editor method - indicates whether this view can be super selected. */
    public boolean superSelectable()  { return getClass()== SGParent.class; }

    /** Editor method. */
    public boolean acceptsChildren()  { return getClass()== SGParent.class; }

    /** Editor method. */
    public boolean childrenSuperSelectImmediately()  { return _parent==null; }

    /**
     * Page number resolution.
     */
    public int page()  { return _parent!=null? _parent.page() : 0; }

    /**
     * Page number resolution.
     */
    public int pageMax()  { return _parent!=null? _parent.pageMax() : 0; }

    /**
     * Returns the "PageBreak" for this view as defined by views that define a page break (currently only RMTable).
     */
    public int getPageBreak()  { return _parent!=null? _parent.getPageBreak() : 0; }

    /**
     * Returns the "PageBreakMax" for this view as defined by views that define a page break (currently only RMTable).
     */
    public int getPageBreakMax()  { return _parent!=null? _parent.getPageBreakMax() : 0; }

    /**
     * Returns the "PageBreakPage" for this view, or the page number relative to the last page break,
     * as defined by views that define explicit page breaks (currently only RMTable).
     */
    public int getPageBreakPage()  { return _parent!=null? _parent.getPageBreakPage() : 0; }

    /**
     * Returns the "PageBreakPageMax" for this view, or the max page number relative to the last and next page breaks,
     * as defined by views that define explicit page breaks (currently only RMTable).
     */
    public int getPageBreakPageMax()  { return _parent!=null? _parent.getPageBreakPageMax() : 0; }

    /**
     * Top-level generic view painting - sets transform and opacity then does a paintAll.
     * If a effect is present, has it paint instead of doing paintAll.
     */
    public void paint(Painter aPntr)
    {
        // Clone graphics
        aPntr.save();

        // Apply transform for view
        if (isRSS()) aPntr.transform(getTransform());
        else aPntr.translate(getX(), getY());

        // If view bounds don't intersect clip bounds, just return
        Rect cbounds = aPntr.getClipBounds();
        if (cbounds!=null && !getBoundsMarkedDeep().intersects(cbounds)) {
            aPntr.restore(); return; }

        // If view is semi-transparent, apply composite
        if (getOpacityDeep()!=1) {
            boolean isEditing = SceneGraph.isEditing(this);
            double op = isEditing ? Math.max(.15, getOpacityDeep()) : getOpacityDeep();
            aPntr.setOpacity(op);
        }

        // If view has a effect, have it paint
        if (getEffect()!=null) { Effect eff = getEffect();
            PainterDVR pdvr = new PainterDVR(aPntr);
            paintAll(pdvr);
            if (!pdvr.equals(_pdvr1)) {
                _pdvr1 = pdvr; _pdvr2 = new PainterDVR();
                eff.applyEffect(pdvr, _pdvr2, getBoundsStrokedDeep());
            }
            _pdvr2.exec(aPntr);
        }

        // Otherwise paintAll
        else paintAll(aPntr);

        // Dispose of graphics
        aPntr.restore();
    }

    // DVR painters for caching effect drawing
    PainterDVR _pdvr1, _pdvr2;

    /**
     * Calls paintView, paintChildren and paintOver.
     */
    public void paintAll(Painter aPntr)
    {
        // Get graphics
        boolean didGsave = false;

        // If clipping, clip to view
        if (getClipShape()!=null) {
            aPntr.save(); didGsave = true;
            aPntr.clip(getClipShape());
        }

        // Have view paint only itself
        paintView(aPntr);

        // Have view paint children
        paintChildren(aPntr);

        // If graphics copied, dispose
        if (didGsave) aPntr.restore();

        // Have view paint over
        paintOver(aPntr);
    }

    /**
     * Basic view painting - paints view fill and stroke.
     */
    protected void paintView(Painter aPntr)
    {
        // Get fill/border
        Paint fill = getFill();
        Border border = getBorder();

        // Paint fill
        if (fill!=null) { //getFill().paint(aPntr, this);
            Paint fill2 = fill.copyForRect(getBoundsLocal());
            aPntr.setPaint(fill2);
            Shape path = getPath();
            aPntr.fill(path);
        }

        // Paint border
        if (border!=null && !isStrokeOnTop()) {
            Shape path = getPath();
            border.paint(aPntr, path);
        }
    }

    /**
     * Paints children.
     */
    protected void paintChildren(Painter aPntr)
    {
        for (int i=0, iMax=getChildCount(); i<iMax; i++) { SGView child = getChild(i);
            if (child.isVisible())
                child.paint(aPntr);
        }
    }

    /**
     * Paints after (on top) of children.
     */
    protected void paintOver(Painter aPntr)
    {
        Border border = getBorder();
        if (border!=null && isStrokeOnTop()) {
            Shape path = getPath();
            border.paint(aPntr, path);
        }
    }

    /**
     * Returns whether to stroke on top.
     */
    public boolean isStrokeOnTop()  { return false; }

    /**
     * Returns clip shape for view.
     */
    public Shape getClipShape()  { return null; }

    /**
     * Returns the value for given key.
     */
    public Object getKeyValue(String aPropName)
    {
        return getPropValue(aPropName);
    }

    /**
     * Sets the value for given key.
     */
    public void setKeyValue(String aPropName, Object aValue)
    {
        setPropValue(aPropName, aValue);
    }

    /**
     * Returns the value for given key.
     */
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch(aPropName) {
            case X_Prop: return getX();
            case Y_Prop: return getY();
            case Width_Prop: return getWidth();
            case Height_Prop: return getHeight();
            case Roll_Prop: return getRoll();
            case ScaleX_Prop: return getScaleX();
            case ScaleY_Prop: return getScaleY();
            case SkewX_Prop: return getSkewX();
            case SkewY_Prop: return getSkewY();
            case Border_Prop: return getBorder();
            case Fill_Prop: return getFill();
            case Effect_Prop: return getEffect();
            case Opacity_Prop: return getOpacity();
            case Name_Prop: return getName();
            case Visible_Prop: return isVisible();
            case Locked_Prop: return isLocked();
            case MinWidth_Prop: return getMinWidth();
            case MinHeight_Prop: return getMinHeight();
            case PrefWidth_Prop: return getPrefWidth();
            case PrefHeight_Prop: return getPrefHeight();
            default: return Key.getValueImpl(this, aPropName);
        }
    }

    /**
     * Sets the value for given key.
     */
    public void setPropValue(String aPropName, Object aValue)
    {
        // Handle properties
        switch(aPropName) {
            case X_Prop: setX(SnapUtils.doubleValue(aValue)); break;
            case Y_Prop: setY(SnapUtils.doubleValue(aValue)); break;
            case Width_Prop: setWidth(SnapUtils.doubleValue(aValue)); break;
            case Height_Prop: setHeight(SnapUtils.doubleValue(aValue)); break;
            case Roll_Prop: setRoll(SnapUtils.doubleValue(aValue)); break;
            case ScaleX_Prop: setScaleX(SnapUtils.doubleValue(aValue)); break;
            case ScaleY_Prop: setScaleY(SnapUtils.doubleValue(aValue)); break;
            case SkewX_Prop: setSkewX(SnapUtils.doubleValue(aValue)); break;
            case SkewY_Prop: setSkewY(SnapUtils.doubleValue(aValue)); break;
            case Border_Prop: setBorder(aValue instanceof Border? (Border)aValue : null); break;
            case Fill_Prop: setFill(aValue instanceof Paint? (Paint)aValue : null); break;
            case Effect_Prop: setEffect(aValue instanceof Effect? (Effect)aValue : null); break;
            case Opacity_Prop: setOpacity(SnapUtils.doubleValue(aValue)); break;
            case Name_Prop: setName(SnapUtils.stringValue(aValue)); break;
            case Visible_Prop: setVisible(SnapUtils.boolValue(aValue)); break;
            case Locked_Prop: setLocked(SnapUtils.boolValue(aValue)); break;
            case MinWidth_Prop: setMinWidth(SnapUtils.doubleValue(aValue)); break;
            case MinHeight_Prop: setMinHeight(SnapUtils.doubleValue(aValue)); break;
            case PrefWidth_Prop: setPrefWidth(SnapUtils.doubleValue(aValue)); break;
            case PrefHeight_Prop: setPrefHeight(SnapUtils.doubleValue(aValue)); break;
            default: Key.setValueReflectSafe(this, aPropName, aValue);
        }
    }

    /**
     * XML Archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Get new element called shape
        XMLElement e = new XMLElement("shape");

        // Archive name
        if (getName()!=null && getName().length()>0) e.add("name", getName());

        // Archive X, Y, Width, Height
        if (_x!=0) e.add("x", _x);
        if (_y!=0) e.add("y", _y);
        if (_width!=0) e.add("width", _width);
        if (_height!=0) e.add("height", _height);

        // Archive Roll, ScaleX, ScaleY, SkewX, SkewY
        if (getRoll()!=0) e.add("roll", getRoll());
        if (getScaleX()!=1) e.add("scalex", getScaleX());
        if (getScaleY()!=1) e.add("scaley", getScaleY());
        if (getSkewX()!=0) e.add("skewx", getSkewX());
        if (getSkewY()!=0) e.add("skewy", getSkewY());

        // Archive Border, Fill, Effect
        if (getBorder()!=null) e.add(anArchiver.toXML(getBorder(), this));
        if (getFill()!=null) e.add(anArchiver.toXML(getFill(), this));
        if (getEffect()!=null) e.add(anArchiver.toXML(getEffect(), this));

        // Archive font
        if (isFontSet()) e.add(getFont().toXML(anArchiver));

        // Archive Opacity, Visible
        if (getOpacity()<1) e.add("opacity", getOpacity());
        if (!isVisible()) e.add("visible", false);

        // Archive URL
        if (getURL()!=null && getURL().length()>0) e.add("url", getURL());

        // Archive MinWidth, MinHeight, PrefWidth, PrefHeight
        if (isMinWidthSet()) e.add(MinWidth_Prop, getMinWidth());
        if (isMinHeightSet()) e.add(MinHeight_Prop, getMinHeight());
        if (isPrefWidthSet()) e.add(PrefWidth_Prop, getPrefWidth());
        if (isPrefHeightSet()) e.add(PrefHeight_Prop, getPrefHeight());

        // Archive Autosizing
        if (!getAutosizing().equals(getAutosizingDefault())) e.add("asize", getAutosizing());

        // Archive Locked
        if (isLocked()) e.add("locked", true);

        // Archive bindings
        for (int i=0, iMax=getBindingCount(); i<iMax; i++)
            e.add(getBinding(i).toXML(anArchiver));

        // Return the element
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive name
        setName(anElement.getAttributeValue("name"));

        // Unarchive X, Y, Width, Height
        _x = anElement.getAttributeFloatValue("x", 0);
        _y = anElement.getAttributeFloatValue("y", 0);
        _width = anElement.getAttributeFloatValue("width", 0);
        _height = anElement.getAttributeFloatValue("height", 0);

        // Unarchive Roll, ScaleX, ScaleY, SkewX, SkewY
        setRoll(anElement.getAttributeFloatValue("roll"));
        setScaleX(anElement.getAttributeFloatValue("scalex", 1));
        setScaleY(anElement.getAttributeFloatValue("scaley", 1));
        setSkewX(anElement.getAttributeFloatValue("skewx", 0));
        setSkewY(anElement.getAttributeFloatValue("skewy", 0));

        // Unarchive Border
        for (int i=anArchiver.indexOf(anElement, Border.class); i>=0; i=-1) {
            Border border = (Border)anArchiver.fromXML(anElement.get(i), this);
            setBorder(border);
        }

        // Unarchive Fill
        for (int i=anArchiver.indexOf(anElement, Paint.class); i>=0; i=-1) { XMLElement e = anElement.get(i);
            if (e.getName().equals("color") && this instanceof SGText) continue; // Bogus till we figure out RMFill to Paint stuff!
            Paint fill = (Paint)anArchiver.fromXML(e, this);
            setFill(fill);
        }

        // Unarchive Effect
        for (int i=anArchiver.indexOf(anElement, Effect.class); i>=0; i=-1) {
            Effect fill = (Effect)anArchiver.fromXML(anElement.get(i), this);
            setEffect(fill);
        }

        // Unarchive font
        XMLElement fontXML = anElement.getElement("font");
        if (fontXML!=null)
            setFont((Font)anArchiver.fromXML(fontXML, this));

        // Unarchive Opacity, Visible
        setOpacity(anElement.getAttributeFloatValue("opacity", 1));
        if (anElement.hasAttribute("visible")) _visible = anElement.getAttributeBoolValue("visible");

        // Unarchive URL
        setURL(anElement.getAttributeValue("url"));

        // Unarchive MinWidth, MinHeight, PrefWidth, PrefHeight
        if (anElement.hasAttribute(MinWidth_Prop)) setMinWidth(anElement.getAttributeFloatValue(MinWidth_Prop));
        if (anElement.hasAttribute(MinHeight_Prop)) setMinHeight(anElement.getAttributeFloatValue(MinHeight_Prop));
        if (anElement.hasAttribute(PrefWidth_Prop)) setPrefWidth(anElement.getAttributeFloatValue(PrefWidth_Prop));
        if (anElement.hasAttribute(PrefHeight_Prop)) setPrefHeight(anElement.getAttributeFloatValue(PrefHeight_Prop));

        // Unarchive Autosizing
        String asize = anElement.getAttributeValue("asize");
        if (asize==null) asize = anElement.getAttributeValue("LayoutInfo");
        if (asize!=null) setAutosizing(asize);

        // Unarchive Locked
        setLocked(anElement.getAttributeBoolValue("locked"));

        // Unarchive bindings
        for (int i=anElement.indexOf("binding"); i>=0; i=anElement.indexOf("binding",i+1)) { XMLElement bxml=anElement.get(i);
            addBinding(new Binding().fromXML(anArchiver, bxml)); }

        // Unarchive property keys (legacy)
        for (int i=anElement.indexOf("property-key"); i>=0; i=anElement.indexOf("property-key", i+1)) {
            XMLElement prop = anElement.get(i); String name = prop.getAttributeValue("name");
            if (name.equals("FontColor")) name = "TextColor"; if(name.equals("IsVisible")) name = "Visible";
            String key = prop.getAttributeValue("key"); addBinding(new Binding(name, key));
        }

        // Return this view
        return this;
    }

    /**
     * Standard to string implementation (prints class name and view bounds).
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append(' ');
        if (getName()!=null) sb.append(getName()).append(' ');
        sb.append(getFrame().toString());
        return sb.toString();
    }

    /**
     * A HashMap subclass to hold uncommon attributes, with Shared flag to indicate whether it has to be copied when modded.
     */
    private static class SGViewSharedMap extends HashMap {

        // Whether this map is being shared
        boolean isShared = false;

        /** Overrides hashtable method to just mark hashtable shared and return it. */
        public SGViewSharedMap clone()  { isShared = true; return this; }

        /** Provides real clone implementation. */
        public SGViewSharedMap cloneReal()
        {
            SGViewSharedMap cln = (SGViewSharedMap)super.clone();
            cln.isShared = false; return cln;
        }
    }
}
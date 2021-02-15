/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.scene;
import snap.geom.*;
import snap.gfx.*;
import snap.util.*;

/**
 * This class is a view representation of an image.
 */
public class SGImage extends SGRect {
    
    // The key used to get image during RPG
    String             _key;
    
    // An ImageRef reference to uniqued image
    ImageRef           _imgRef;
    
    // The padding
    int                _padding;
    
    // Whether to grow image to fit available area if view larger than image.
    boolean            _growToFit = true;
    
    // Whether to preserve the natural width to height ratio of image
    boolean            _preserveRatio = true;
    
    // The image name, if image read from external file
    String             _iname;

    /**
     * Creates SGImage.
     */
    public SGImage()  { }

    /**
     * Creates an SGImage from the image source provided.
     */
    public SGImage(Object aSource)  { setImageForSource(aSource); setBestSize(); }

    /**
     * Returns the report key used to load an image if none is provided.
     */
    public String getKey()  { return _key; }

    /**
     * Sets the report key used to load an image if none is provided.
     */
    public void setKey(String aString)
    {
        firePropChange("Key", _key, _key = aString);
    }

    /**
     * Returns the ImageRef reference to uniqued image.
     */
    public ImageRef getImageRef()  { return _imgRef; }

    /**
     * Sets the ImageRef reference to uniqued image.
     */
    protected void setImageRef(ImageRef anImageRef)
    {
        if(anImageRef==getImageRef()) return;
        _imgRef = anImageRef;
        if(getParent()!=null) getParent().relayout(); repaint();
    }

    /**
     * Returns the image.
     */
    public Image getImage()  { return _imgRef!=null? _imgRef.getImage() : null; }

    /**
     * Sets the image from given source.
     */
    public void setImageForSource(Object aSource)
    {
        ImageRef iref = ImageRef.getImageRef(aSource);
        setImageRef(iref);
    }

    /**
     * Returns the padding.
     */
    public int getPadding()  { return _padding; }

    /**
     * Sets the padding.
     */
    public void setPadding(int aPadding)
    {
        firePropChange("Padding", _padding, _padding = aPadding);
        repaint();
    }

    /**
     * Returns the horizontal alignment.
     */
    public HPos getAlignX()  { return _alignX; } HPos _alignX = HPos.CENTER;

    /**
     * Sets the horizontal alignment.
     */
    public void setAlignX(HPos anAlignX)  { _alignX = anAlignX; }

    /**
     * Returns the vertical alignment.
     */
    public VPos getAlignY()  { return _alignY; } VPos _alignY = VPos.CENTER;

    /**
     * Sets the vertical alignment.
     */
    public void setAlignY(VPos anAlignY)  { _alignY = anAlignY; }

    /**
     * Returns whether to grow image to fit available area if view larger than image.
     */
    public boolean isGrowToFit()  { return _growToFit; }

    /**
     * Sets whether to grow image to fit available area if view larger than image.
     */
    public void setGrowToFit(boolean aValue)
    {
        firePropChange("GrowToFit", _growToFit, _growToFit = aValue);
        repaint();
    }

    /**
     * Returns whether to preserve the natural width to height ratio of image.
     */
    public boolean getPreserveRatio()  { return _preserveRatio; }

    /**
     * Sets whether to preserve the natural width to height ratio of image.
     */
    public void setPreserveRatio(boolean aValue)
    {
        firePropChange("PreserveRatio", _preserveRatio, _preserveRatio = aValue);
        repaint();
    }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aHeight)
    {
        Image img = getImage(); if (img==null) return 0;
        double pw = img.getWidth(), ph = img.getHeight();
        if (aHeight>0 && getPreserveRatio() && ph>aHeight) pw = aHeight*pw/ph;
        return pw;
    }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aWidth)
    {
        Image img = getImage(); if (img==null) return 0;
        double pw = img.getWidth(), ph = img.getHeight();
        if (aWidth>0 && getPreserveRatio() && pw>aWidth) ph = aWidth*ph/pw;
        return ph;
    }

    /**
     * Override to paint view.
     */
    protected void paintView(Painter aPntr)
    {
        // Do normal version
        super.paintView(aPntr);

        // Get image (use empty placeholder image if null and editing)
        Image img = getImage();
        if (img==null) {
            if (!SceneGraph.isEditing(this)) return;
            img = ImageUtils.getEmptyImage(); if (img==null) return;
        }

        // Draw image transformed to bounds
        aPntr.clip(getPath());
        Rect ibnds = getImageBounds();
        double sx = ibnds.width/img.getPixWidth(), sy = ibnds.height/img.getPixHeight();
        Transform transform = new Transform(sx, 0, 0, sy, ibnds.x, ibnds.y);
        aPntr.drawImage(img, transform);
    }

    /**
     * Returns the image bounds.
     */
    public Rect getImageBounds()
    {
        // Get image and padding
        Image img = getImage(); if (img==null) img = ImageUtils.getEmptyImage();
        int pd = getPadding();

        // Get width/height for view, image and padded area
        double sw = getWidth(), sh = getHeight();
        double iw = img.getWidth(), ih = img.getHeight();
        double pw = sw - pd*2; if (pw<0) pw = 0;
        double ph = sh - pd*2; if (ph<0) ph = 0;

        // Get image bounds width/height, ShrinkToFit if greater than available space (with PreserveRatio, if set)
        double w = iw, h = ih; if(isGrowToFit()) { w = pw+1; h = ph+1; }
        if (w>pw) { w = pw; if(getPreserveRatio()) h = ih*w/iw; }
        if (h>ph) { h = ph; if(getPreserveRatio()) w = iw*h/ih; }

        // Get image bounds x/y for width/height and return rect
        HPos ax = getAlignX();
        VPos ay = getAlignY();
        double x = ax==HPos.CENTER ? (sw - w)/2 : ax==HPos.LEFT ? pd : (sw - w);
        double y = ay==VPos.CENTER ? (sh - h)/2 : ay==VPos.TOP ? pd : (sh - h);
        return new Rect(x, y, w, h);
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic view attributes and reset element name to image-shape
        XMLElement e = super.toXML(anArchiver); e.setName("image-shape");

        // Archive ImageName, if image read from external file
        if (_iname!=null) e.add("ImageName", _iname);

        // Archive Image
        else if (getImage()!=null) {
            String resName = anArchiver.addResource(getImage().getBytes(), getImageRef().getName());
            e.add("resource", resName);
        }

        // Archive Key, Padding, Alignment, GrowToFit, PreserveRatio
        if (_key!=null && _key.length()>0) e.add("key", _key);
        if (_padding>0) e.add("Padding", _padding);
        if (getAlign()!= Pos.CENTER) e.add("Alignment", getAlign());
        if (!isGrowToFit()) e.add("GrowToFit", isGrowToFit());
        if (!getPreserveRatio()) e.add("PreserveRatio", getPreserveRatio());

        // Return
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic attributes
        super.fromXML(anArchiver, anElement);

        // Unarchive Image resource: get resource bytes and set Image
        String rname = anElement.getAttributeValue("resource");
        if (rname!=null) {
            byte bytes[] = anArchiver.getResource(rname);
            _imgRef = ImageRef.getImageRef(bytes);
        }

        // Unarchive ImageName
        _iname = anElement.getAttributeValue("ImageName");
        if (_iname!=null) {
            Image img = Image.get(anArchiver.getSourceURL(), _iname);
            if (img!=null)
                _imgRef = ImageRef.getImageRef(img.getSource());
        }

        // Unarchive Key, Padding, GrowToFit, PreserveRatio
        if (anElement.hasAttribute("key")) setKey(anElement.getAttributeValue("key"));
        if (anElement.hasAttribute("Padding")) setPadding(anElement.getAttributeIntValue("Padding"));
        if (anElement.hasAttribute("GrowToFit")) setGrowToFit(anElement.getAttributeBooleanValue("GrowToFit"));
        if (anElement.hasAttribute("PreserveRatio")) setPreserveRatio(anElement.getAttributeBooleanValue("PreserveRatio"));

        // Return
        return this;
    }
}
/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.scene;
import snap.geom.Path;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.geom.Transform;
import snap.util.*;

/**
 * This class is an SGView subclass that encapsulates an arbitrary path.
 */
public class SGPolygon extends SGParent {

    // The explicit path associated with this view
    protected Path _path;

    /**
     * Creates SGPolygon.
     */
    public SGPolygon()
    {
    }

    /**
     * Creates a new polygon for given path.
     */
    public SGPolygon(Shape aShape)
    {
        this();
        _path = new Path(aShape);
    }

    /**
     * Returns the path for this polygon.
     */
    public Path getPath()
    {
        return _path.copyFor(getBoundsLocal());
    }

    /**
     * Sets the path for this polygon.
     */
    public void setPath(Path aPath)
    {
        _path = aPath;
        repaint();
    }

    /**
     * Replace the polygon's current path with a new path, adjusting the view's bounds to match the new path.
     */
    public void resetPath(Path newPath)
    {
        // Get the transform to parent view coords
        Transform toParentXF = getTransform();

        // Set the new path and new size
        setPath(newPath);
        Rect bounds = newPath.getBounds();
        setSize(bounds.getWidth(), bounds.getHeight());

        // Transform to parent for new x & y
        Rect boundsInParent = bounds.clone();
        toParentXF.transformRect(boundsInParent);
        setFrameXY(boundsInParent.getXY());
    }

    /**
     * XML archival.
     */
    protected XMLElement toXMLView(XMLArchiver anArchiver)
    {
        XMLElement e = super.toXMLView(anArchiver);
        e.setName("polygon");
        e.add(_path.toXML(anArchiver));
        return e;
    }

    /**
     * XML unarchival.
     */
    protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        super.fromXMLView(anArchiver, anElement);
        XMLElement pathXML = anElement.get("path");
        _path = anArchiver.fromXML(pathXML, Path.class, this);
    }

    /**
     * Standard clone implementation.
     */
    public SGPolygon clone()
    {
        SGPolygon clone = (SGPolygon) super.clone();
        if (_path != null) clone._path = _path.clone();
        return clone;
    }
}
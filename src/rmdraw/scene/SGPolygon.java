/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.scene;
import snap.geom.*;
import snap.util.*;

/**
 * This class is an SGView subclass that encapsulates an arbitrary path.
 */
public class SGPolygon extends SGParent {

    // The explicit path associated with this view
    protected Path2D _path;

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
        _path = new Path2D(aShape);
    }

    /**
     * Returns the path for this polygon.
     */
    public Path2D getPath()
    {
        return _path.copyFor(getBoundsLocal());
    }

    /**
     * Sets the path for this polygon.
     */
    public void setPath(Shape aPath)
    {
        _path = aPath instanceof Path2D ? (Path2D) aPath : new Path2D(aPath);
        repaint();
    }

    /**
     * Replace the polygon's current path with a new path, adjusting the view's bounds to match the new path.
     */
    public void setPathAndBounds(Shape newPath)
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
        XMLElement e = super.toXMLView(anArchiver); e.setName("polygon");

        // Archive path
        XMLElement pathXML = getXmlForPath(_path);
        e.add(pathXML);

        // Return
        return e;
    }

    /**
     * XML unarchival.
     */
    protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        super.fromXMLView(anArchiver, anElement);
        XMLElement pathXML = anElement.get("path");
        _path = getPathFromXML(pathXML);
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


    /**
     * XML archival for path.
     */
    private XMLElement getXmlForPath(Path2D aPath)
    {
        // Get new element named path
        XMLElement e = new XMLElement("path");

        // Archive individual elements/points
        PathIter pathIter = aPath.getPathIter(null);
        double[] points = new double[6];
        while (pathIter.hasNext()) switch (pathIter.getNext(points)) {

            // Handle MoveTo
            case MoveTo:
                XMLElement move = new XMLElement("mv");
                move.add("x", points[0]);
                move.add("y", points[1]);
                e.add(move);
                break;

            // Handle LineTo
            case LineTo:
                XMLElement line = new XMLElement("ln");
                line.add("x", points[0]);
                line.add("y", points[1]);
                e.add(line);
                break;

            // Handle QuadTo
            case QuadTo:
                XMLElement quad = new XMLElement("qd");
                quad.add("cx", points[0]);
                quad.add("cy", points[1]);
                quad.add("x", points[2]);
                quad.add("y", points[3]);
                e.add(quad);
                break;

            // Handle CubicTo
            case CubicTo:
                XMLElement curve = new XMLElement("cv");
                curve.add("cp1x", points[0]);
                curve.add("cp1y", points[1]);
                curve.add("cp2x", points[2]);
                curve.add("cp2y", points[3]);
                curve.add("x", points[4]);
                curve.add("y", points[5]);
                e.add(curve);
                break;

            // Handle Close
            case Close:
                XMLElement close = new XMLElement("cl");
                e.add(close);
                break;
        }

        return e;
    }

    /**
     * XML unarchival for path.
     */
    public Path2D getPathFromXML(XMLElement anElement)
    {
        Path2D path = new Path2D();

        // Unarchive individual elements/points
        for (int i = 0, iMax = anElement.size(); i < iMax; i++) {
            XMLElement segXML = anElement.get(i);
            String segName = segXML.getName();
            double endX = segXML.getAttributeFloatValue("x");
            double endY = segXML.getAttributeFloatValue("y");

            switch (segName) {
                case "mv": path.moveTo(endX, endY); break;
                case "ln":path.lineTo(endX, endY); break;
                case "qd": {
                    double cpx = segXML.getAttributeFloatValue("cx");
                    double cpy = segXML.getAttributeFloatValue("cy");
                    path.quadTo(cpx, cpy, endX, endY);
                    break;
                }
                case "cv": {
                    double cp1x = segXML.getAttributeFloatValue("cp1x");
                    double cp1y = segXML.getAttributeFloatValue("cp1y");
                    double cp2x = segXML.getAttributeFloatValue("cp2x");
                    double cp2y = segXML.getAttributeFloatValue("cp2y");
                    path.curveTo(cp1x, cp1y, cp2x, cp2y, endX, endY);
                    break;
                }
                case "cl":
                    path.close();
                    break;
            }
        }

        // Return
        return path;
    }
}
/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.scene;

import java.util.*;

import snap.geom.Path;
import snap.geom.Rect;
import snap.gfx.*;
import snap.gfx3d.*;
import snap.util.*;
import snap.view.ViewEvent;

/**
 * This encapsulates a Snap Scene3D to render simple 3d.
 */
public class SGScene3D extends SGParent {

    // The depth
    private double _depth = 40;

    // A Scene3D to do real scene management
    private Scene3D _scene = new Scene3D();

    // A Camera to do camera work
    private Camera _camera;

    // List of real child shapes
    private List<SGView> _rmshapes = new ArrayList<>();

    // Constants for properties
    public static final String Depth_Prop = "Depth";

    /**
     * Creates an RMScene3D.
     */
    public SGScene3D()
    {
        _camera = _scene.getCamera();
        _camera.addPropChangeListener(pce -> sceneChanged(pce));
    }

    /**
     * Returns the camera as a vector.
     */
    public Camera getCamera()
    {
        return _camera;
    }

    /**
     * Returns the Scene3D.
     */
    public Scene3D getScene()
    {
        return _scene;
    }

    /**
     * Returns the depth of the scene.
     */
    public double getDepth()
    {
        return _depth;
    }

    /**
     * Sets the depth of the scene.
     */
    public void setDepth(double aValue)
    {
        if (aValue == getDepth()) return;
        firePropChange(Depth_Prop, _depth, _depth = aValue);
        relayout();
        repaint();
    }

    /**
     * Returns the rotation about the Y axis in degrees.
     */
    public double getYaw()
    {
        return _camera.getYaw();
    }

    /**
     * Sets the rotation about the Y axis in degrees.
     */
    public void setYaw(double aValue)
    {
        _camera.setYaw(aValue);
    }

    /**
     * Returns the rotation about the X axis in degrees.
     */
    public double getPitch()
    {
        return _camera.getPitch();
    }

    /**
     * Sets the rotation about the X axis in degrees.
     */
    public void setPitch(double aValue)
    {
        _camera.setPitch(aValue);
    }

    /**
     * Returns the rotation about the Z axis in degrees.
     */
    public double getRoll3D()
    {
        return _camera.getRoll();
    }

    /**
     * Sets the rotation about the Z axis in degrees.
     */
    public void setRoll3D(double aValue)
    {
        _camera.setRoll(aValue);
    }

    /**
     * Returns the focal length of the camera (derived from the field of view and with view size).
     */
    public double getFocalLength()
    {
        return _camera.getFocalLength();
    }

    /**
     * Sets the focal length of the camera. Two feet is normal (1728 points).
     */
    public void setFocalLength(double aValue)
    {
        _camera.setFocalLength(aValue);
    }

    /**
     * Adds a shape to the end of the shape list.
     */
    public void addShape(Shape3D aShape)
    {
        _scene.addChild(aShape);
    }

    /**
     * Removes the shape at the given index from the shape list.
     */
    public void removeShapes()
    {
        _scene.removeChildren();
    }

    /**
     * Returns whether a vector is facing camera.
     */
    public boolean isFacing(Vector3D aV3D)
    {
        return _camera.isFacing(aV3D);
    }

    /**
     * Returns whether a vector is facing away from camera.
     */
    public boolean isFacingAway(Vector3D aV3D)
    {
        return _camera.isFacingAway(aV3D);
    }

    /**
     * Returns whether a Path3d is facing camera.
     */
    public boolean isFacing(Path3D aPath)
    {
        return _camera.isFacing(aPath);
    }

    /**
     * Returns whether a Path3d is facing away from camera.
     */
    public boolean isFacingAway(Path3D aPath)
    {
        return _camera.isFacingAway(aPath);
    }

    /**
     * Rebuilds display list of Path3Ds from Shapes.
     */
    protected void layoutImpl()
    {
        // If RMShapes, recreate Shape list from RMShapes
        if (getShapeRMCount() > 0) {
            removeShapes();
            for (SGView shp : _rmshapes)
                addShapesForRMShape(shp, 0, getDepth());
        }
    }

    /**
     * Paints shape children.
     */
    protected void paintChildren(Painter aPntr)
    {
        // Paint Scene paths
        _camera.paintScene(aPntr);

        // Do normal version
        super.paintChildren(aPntr);
    }

    /**
     * Viewer method.
     */
    public void processEvent(ViewEvent anEvent)
    {
        _camera.processEvent(anEvent);
    }

    /**
     * Override to forward to Scene3D.
     */
    public void setWidth(double aValue)
    {
        super.setWidth(aValue);
        _camera.setViewWidth(aValue);
    }

    /**
     * Override to forward to Scene3D.
     */
    public void setHeight(double aValue)
    {
        super.setHeight(aValue);
        _camera.setViewHeight(aValue);
    }

    /**
     * Override to account for Scene3D bounds.
     */
    public Rect getBoundsMarked()
    {
        Rect bounds = super.getBoundsMarked();
        Rect camBnds = _camera.getSceneBounds2D();
        if (camBnds.x < bounds.x) bounds.x = camBnds.x;
        if (camBnds.y < bounds.y) bounds.y = camBnds.y;
        if (camBnds.getMaxX() > bounds.getMaxX()) bounds.width = camBnds.getMaxX() - bounds.x;
        if (camBnds.getMaxY() > bounds.getMaxY()) bounds.height = camBnds.getMaxY() - bounds.y;
        return bounds;
    }

    /**
     * Called when scene changes.
     */
    protected void sceneChanged(PropChange aPC)
    {
        _pcs.fireDeepChange(this, aPC);
        relayout();
        repaint();
    }

    /**
     * Returns the number of shapes in the shape list.
     */
    public int getShapeRMCount()
    {
        return _rmshapes.size();
    }

    /**
     * Returns the specific shape at the given index from the shape list.
     */
    public SGView getShapeRM(int anIndex)
    {
        return _rmshapes.get(anIndex);
    }

    /**
     * Adds a shape to the end of the shape list.
     */
    public void addShapeRM(SGView aShape)
    {
        _rmshapes.add(aShape);
        relayout();
    }

    /**
     * Adds Shape3D objects for given RMShape.
     * FixEdges flag indicates wheter to stroke polygons created during extrusion, to try to make them mesh better.
     */
    protected void addShapesForRMShape(SGView aShape, double z1, double z2)
    {
        // If aShape is text, add shape3d for background and add shape3d for char path shape
        if (aShape instanceof SGText) {
            SGText text = (SGText) aShape;

            // If text draws fill or stroke, add child for background
            if (text.getFill() != null || text.getBorder() != null) {
                SGView background = new SGPolygon(aShape.getPath()); // Create background shape from text
                background.copyView(aShape);
                addShapesForRMShape(background, z1 + .1f, z2); // Add background shape
            }

            // Get shape for char paths and add shape3d for char path shape
            SGView charsShape = SGTextUtils.getTextPathView(text);
            addShapesForRMShape(charsShape, z1, z1);
            return;
        }

        // Get shape path, flattened and in parent coords
        Path shapePath = new Path(aShape.getPath());
        shapePath = shapePath.getPathFlattened();
        shapePath.transformBy(aShape.getTransform());

        // Get path3d for shape path
        PathBox3D pathBox = new PathBox3D(shapePath, z1, z2);

        // Create 3D shape from path, set fill/stroke/opacity and add
        Paint fill = aShape.getFill();
        if (fill != null)
            pathBox.setColor(fill.getColor());
        Border border = aShape.getBorder();
        if (border != null)
            pathBox.setStroke(border.getColor(), border.getWidth());
        pathBox.setOpacity(aShape.getOpacity());
        addShape(pathBox);
    }

    /**
     * Override to indicate that scene children are unhittable.
     */
    public boolean isHittable(SGView aChild)
    {
        return false;
    }

    /**
     * Viewer method.
     */
    public boolean acceptsMouse()
    {
        return true;
    }

    /**
     * Copy 3D attributes only.
     */
    public void copy3D(SGScene3D aScene3D)
    {
        getCamera().copy3D(aScene3D.getCamera());
    }

    /**
     * XML archival.
     */
    protected XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic shape attributes and reset element name
        XMLElement e = super.toXMLView(anArchiver);
        e.setName("scene3d");

        // Archive the RMShape children: create element for shape, iterate over shapes and add
        if (getShapeRMCount() > 0) {
            XMLElement shapesXML = new XMLElement("shapes");
            for (int i = 0, iMax = getShapeRMCount(); i < iMax; i++)
                shapesXML.add(anArchiver.toXML(getShapeRM(i)));
            e.add(shapesXML);
        }

        // Archive Depth, Yaw, Pitch, Roll, FocalLength
        if (getDepth() != 0) e.add("depth", getDepth());
        if (getYaw() != 0) e.add("yaw", getYaw());
        if (getPitch() != 0) e.add("pitch", getPitch());
        if (getRoll3D() != 0) e.add("zroll", getRoll3D());
        if (getFocalLength() != 60 * 72) e.add("focal-length", getFocalLength());

        // Return xml element
        return e;
    }

    /**
     * XML archival of children - overrides shape implementation to suppress archival of generated 3D shapes.
     */
    protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
    }

    /**
     * XML unarchival.
     */
    protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic shape attributes
        super.fromXMLView(anArchiver, anElement);

        // Fix scene width/height
        _camera.setViewWidth(getWidth());
        _camera.setViewHeight(getHeight());

        // Unarchive Depth, Yaw, Pitch, Roll, FocalLength
        setDepth(anElement.getAttributeFloatValue("depth"));
        setYaw(anElement.getAttributeFloatValue("yaw"));
        setPitch(anElement.getAttributeFloatValue("pitch"));
        setRoll3D(anElement.getAttributeFloatValue("zroll"));
        setFocalLength(anElement.getAttributeFloatValue("focal-length", 60 * 72));

        // Unarchive the 2d children
        XMLElement shapesXML = anElement.get("shapes");
        if (shapesXML != null)
            for (int i = 0, iMax = shapesXML.size(); i < iMax; i++)
                addShapeRM((SGView) anArchiver.fromXML(shapesXML.get(i), this));
    }
}
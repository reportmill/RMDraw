/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.scene;
import java.util.*;

import snap.geom.Rect;
import snap.gfx.*;
import snap.util.*;

/**
 * The Page class represents an individual page in Document. For the most part, it's like every other Scene view,
 * except that it has the ability to break children into "layers" for more convenient editing. Layers are sub-ranges of
 * children that can be set to be invisible or locked.
 * <p>
 * It's not common for developers to use much of page's unique functionality programmatically, however, you might use
 * it briefly when disecting a template like this:
 * <p><blockquote><pre>
 *   SGDoc template = new SGDoc(aSource);
 *   SGPage page = template.getPage(0);
 *   RMTable table = page.getChildWithName("MyTable");
 *   ...
 * </pre></blockquote><p>
 * Or you might use it briefly when adding pages to a document (which comes with one by default):
 * <p><blockquote><pre>
 *   RMDoc template = new RMDoc(612, 792);
 *   template.getPage(0).addChild(new RMText(new RXString("Hello Page 1!", RMFont("Arial", 100))));
 *   template.addPage(new SGPage());
 *   template.getPage(1).addChild(new RMText(new RXString("Hello Page 2!", RMFont("Arial", 100))));
 * </pre></blockquote>
 */
public class SGPage extends SGParent {

    // The key chain used to get a list of objects from document's datasource
    String _datasetKey;

    // Whether to paint white background even if not explicitly defined and drop shadow
    boolean _paintBackground = true;

    // The selected layer index
    int _layerIndex = 0;

    // The list of layers for this page
    List<SGPageLayer> _layers = new Vector();

    /**
     * Creates a plain empty page.
     */
    public SGPage()
    {
        resetLayers();
    }

    /**
     * Returns the dataset key associated with the table.
     */
    public String getDatasetKey()
    {
        return _datasetKey;
    }

    /**
     * Sets the dataset key associated with the table.
     */
    public void setDatasetKey(String aKeyPath)
    {
        if (SnapUtils.equals(aKeyPath, _datasetKey)) return;
        firePropChange("DatasetKey", aKeyPath, _datasetKey = StringUtils.delete(aKeyPath, "@"));
    }

    /**
     * Returns whether to paint white background even if not explicitly defined and drop shadow.
     */
    public boolean getPaintBackground()
    {
        return _paintBackground;
    }

    /**
     * Sets whether to paint white background even if not explicitly defined and drop shadow.
     */
    public void setPaintBackground(boolean aValue)
    {
        if (aValue == _paintBackground) return;
        repaint();
        firePropChange("PaintBackground", _paintBackground, _paintBackground = aValue);
    }

    /**
     * Returns the number of layers associated with this page.
     */
    public int getLayerCount()
    {
        return _layers == null ? 0 : _layers.size();
    }

    /**
     * Returns the layer at the given index.
     */
    public SGPageLayer getLayer(int anIndex)
    {
        return _layers.get(anIndex);
    }

    /**
     * Returns the list of page layers.
     */
    public List<SGPageLayer> getLayers()
    {
        return _layers;
    }

    /**
     * Adds a layer to page.
     */
    public void addLayer(SGPageLayer aLayer)
    {
        addLayer(aLayer, getLayerCount());
    }

    /**
     * Adds a layer to page.
     */
    public void addLayer(SGPageLayer aLayer, int anIndex)
    {
        // If value already present, just return
        if (ListUtils.containsId(_layers, aLayer)) return;

        // If list is missing, create it
        if (_layers == null) _layers = new Vector();

        // Register for repaint
        repaint();

        // Add layer
        _layers.add(anIndex, aLayer);

        // Fire property change
        firePropChange("Layer", null, aLayer, anIndex);

        // Add children
        int index = aLayer.getPageChildIndex();
        for (int i = 0, iMax = aLayer.getChildCount(); i < iMax; i++)
            super.addChild(aLayer.getChild(i), index + i);
    }

    /**
     * Removes the layer at given index (and its children).
     */
    public SGPageLayer removeLayer(int anIndex)
    {
        // If last layer, bail (shouldn't need this)
        if (getLayerCount() < 2) return null;

        // Request repaint
        repaint();

        // Remove layer
        SGPageLayer layer = _layers.remove(anIndex);

        // Fire property change
        firePropChange("Layer", layer, null, anIndex);

        // Remove layer children
        for (SGView child : layer.getChildren())
            removeChild(child);

        // Ensure selected layer index is valid
        _layerIndex = Math.min(_layerIndex, getLayerCount() - 1);

        // Return layer
        return layer;
    }

    /**
     * Removes the given layer.
     */
    public int removeLayer(SGPageLayer aLayer)
    {
        int index = ListUtils.indexOfId(_layers, aLayer);
        if (index >= 0)
            removeLayer(index);
        return index;
    }

    /**
     * Creates a new layer with the given name and adds it to this page's layer list.
     */
    public void addLayerNamed(String aString)
    {
        // Create and add new layer
        addLayer(new SGPageLayer(this, aString));

        // Reset selected layer index to last layer
        _layerIndex = getLayerCount() - 1;
    }

    /**
     * Returns the layer for a given child.
     */
    public SGPageLayer getChildLayer(SGView aChild)
    {
        if (getLayers() != null)
            for (SGPageLayer layer : getLayers())
                if (layer.getChildIndex(aChild) >= 0)
                    return layer;
        return null;
    }

    /**
     * Returns the index of the selected layer.
     */
    public int getSelLayerIndex()
    {
        return _layerIndex;
    }

    /**
     * Returns the selected layer.
     */
    public SGPageLayer getSelLayer()
    {
        return _layerIndex < getLayerCount() ? getLayer(_layerIndex) : null;
    }

    /**
     * Selects the given layer.
     */
    public void selectLayer(SGPageLayer aLayer)
    {
        repaint();
        _layerIndex = aLayer.getIndex();
    }

    /**
     * Resets this page's list of layers to a single, selecctable layer named "Layer 1".
     */
    public void resetLayers()
    {
        // Clear layers
        _layers.clear();

        // Create new layer 1
        SGPageLayer layer = new SGPageLayer(this, "Layer 1");

        // Add all children
        layer.addChildren(_children);

        // Add new layer
        _layers.add(layer);

        // Reset layer index
        _layerIndex = 0;
    }

    /**
     * Add the given child at the given index (overrides to propagate to PageLayer).
     */
    public void addChild(SGView aChild, int anIndex)
    {
        // Add child normally
        super.addChild(aChild, anIndex);

        // If there is a selected layer, add child to layer and reorder children
        if (getChildLayer(aChild) == null && getSelLayer() != null) {
            getSelLayer().addChild(aChild, Math.min(anIndex, getSelLayer().getChildCount()));
            orderChildrenFromLayers();
        }
    }

    /**
     * Removes the child at the given index (overrides to propagate to PageLayer).
     */
    public SGView removeChild(int anIndex)
    {
        // Do normal remove child
        SGView child = super.removeChild(anIndex);

        // Get child layer and remove child from it
        SGPageLayer layer = getChildLayer(child);
        if (layer != null)
            layer.removeChild(child);

        // Return child
        return child;
    }

    /**
     * Overrides shape implementation to keep shapes in their proper layers.
     */
    public void bringViewsToFront(List theViews)
    {
        // Register for repaint
        repaint();

        // Have layers bring shapes to front
        for (int i = 0; i < getLayerCount(); i++)
            getLayer(i).bringShapesToFront(theViews);

        // Re-order page children from layers
        orderChildrenFromLayers();
    }

    /**
     * Overrides shape implementation to keep shapes in their proper layers.
     */
    public void sendViewsToBack(List theViews)
    {
        // Register for repaint
        repaint();

        // Have layers send shapes to back
        for (int i = 0; i < getLayerCount(); i++)
            getLayer(i).sendShapesToBack(theViews);

        // Re-order page children from layers
        orderChildrenFromLayers();
    }

    /**
     * Creates a new layer and adds the shapes in the given list to it.
     */
    public void moveToNewLayer(List shapes)
    {
        // Register for repaint
        repaint();

        // Remove children from selected layer
        getSelLayer().removeChildren(shapes);

        // Add new layer
        addLayerNamed("Layer " + (getLayerCount() + 1));

        // Add children to new layer
        getSelLayer().addChildren(shapes);

        // Re-order page children from layers
        orderChildrenFromLayers();
    }

    /**
     * Resets children list from layers.
     */
    private void orderChildrenFromLayers()
    {
        // If children or layers is null, just return
        if (getChildCount() < 2 || getLayerCount() == 0)
            return;

        // Request repaint
        repaint();

        // Clear children
        _children.clear();

        // Load children from layers
        for (int i = 0, iMax = getLayerCount(); i < iMax; i++)
            _children.addAll(getLayer(i).getChildren());
    }

    /**
     * Override so page layers can make children not visible.
     */
    protected boolean isShowing(SGView aChild)
    {
        // If no separate layers, just return whether child is visible
        if (getLayerCount() < 2 || !aChild.isVisible())
            return aChild.isVisible();

        // Iterate over layers and if layer contains child, return whether layer is visible
        for (SGPageLayer layer : getLayers())
            if (ListUtils.contains(layer.getChildren(), aChild))
                return layer.isVisible() || layer == getSelLayer();

        // Return true since layer not found (shouldn't ever get here)
        return true;
    }

    /**
     * Override so page layers can make children unhittable.
     */
    public boolean isHittable(SGView aChild)
    {
        // If no separate layers, just return whether child is visible
        if (getLayerCount() < 2 || !aChild.isVisible())
            return aChild.isVisible();

        // Iterate over layers and if layer contains child, return whether layer is not locked and visible
        for (SGPageLayer layer : getLayers())
            if (ListUtils.contains(layer.getChildren(), aChild))
                return !layer.isLocked() && (layer.isVisible() || layer == getSelLayer());

        // Return false since layer not found (shouldn't ever get here)
        return false;
    }

    /**
     * Overrides shape implementation to return this page, since it is the page shape.
     */
    public SGParent getPage()
    {
        return this;
    }


    /**
     * Returns the "Page" number of this page (used to resolve @Page@ key references).
     */
    public int page()
    {
        if (getDoc() == null) return 0;
        return ListUtils.indexOfId(getDoc().getPages(), this) + 1;
    }

    /**
     * Returns the "PageMax" of the document associated with this page (used to resolve @PageMax@ key references).
     */
    public int pageMax()
    {
        return getDoc() == null ? 0 : getDoc().getPageCount();
    }

    /**
     * Top-level generic shape painting (sets transform, recurses to children, paints this).
     */
    protected void paintView(Painter aPntr)
    {
        // If printing, do normal shape painting and return
        if (aPntr.isPrinting()) {
            super.paintView(aPntr);
            return;
        }

        // Get page bounds, clip bounds and intersection of those
        Rect bounds = getBoundsLocal();
        Rect clipBounds = aPntr.getClipBounds();
        Rect drawBounds = bounds.getIntersectRect(clipBounds);

        // If clip extends outside page bounds, draw page drop-shadow
        if (getPaintBackground() && (clipBounds.getMaxX() > getWidth() || clipBounds.getMaxY() > getHeight())) {
            aPntr.setColor(Color.DARKGRAY);
            aPntr.fillRect(3, 3, getWidth(), getHeight());
        }

        // If no explicit page fill, draw page background white
        if (getPaintBackground() && getFill() == null) {
            aPntr.setColor(Color.WHITE);
            aPntr.fill(drawBounds);
        }

        // do normal shape painting
        super.paintView(aPntr);

        // Turn off antialiasing for page stuff
        aPntr.setAntialiasing(false);

        // If no explicit page stroke, draw page border fill (1 point black by default)
        if (getPaintBackground() && getBorder() == null) {
            aPntr.setStroke(Stroke.Stroke1);
            aPntr.setColor(Color.GRAY);
            aPntr.drawRect(.5, .5, getWidth() - 1, getHeight() - 1);
        }

        // Draw grid if needed
        if (getDoc().isShowGrid() && SceneGraph.isEditing(this)) {

            // Get grid spacing (corrected for zoom factor) and document's min x and y
            double gs = getDoc().getGridSpacing();

            // Set color to grid color
            aPntr.setColor(new Color(13 / 15f)); // gridColor = new Color(13/15f, 13/15f, 13/15f)

            // Draw vertical lines
            for (double x = MathUtils.ceil(drawBounds.getX() - .001, gs); x < drawBounds.getMaxX(); x += gs)
                aPntr.drawLine((int) x, (int) drawBounds.getY(), (int) x, (int) drawBounds.getMaxY() - 1);

            // Draw horizontal lines
            for (double y = MathUtils.ceil(drawBounds.getY() - .001, gs); y < drawBounds.getMaxY(); y += gs)
                aPntr.drawLine((int) drawBounds.getX(), (int) y, (int) drawBounds.getMaxX() - 1, (int) y);
        }

        // If Draw Margin is requested and editing, draw margin
        if (getDoc().isShowMargin() && SceneGraph.isEditing(this)) {
            aPntr.setColor(new Color(9 / 15f)); // marginColor = new Color(9/15f, 9/15f, 9/15f)
            aPntr.draw(getDoc().getMarginRect());
        }

        // Turn on antialiasing for shape stuff
        aPntr.setAntialiasing(true);
    }

    /**
     * Paints shape children.
     */
    protected void paintChildren(Painter aPntr)
    {
        for (int i = 0, iMax = getChildCount(); i < iMax; i++) {
            SGView child = getChild(i);
            if (child.isVisible() && isShowing(child))
                child.paint(aPntr);
        }
    }

    /**
     * Standard clone method.
     */
    public SGPage clone()
    {
        SGPage clone = (SGPage) super.clone(); // Do normal shape clone
        clone._layerIndex = 0;
        clone._layers = null; // Clear LayerIndex and Layers
        return clone;
    }

    /**
     * XML archival.
     */
    protected XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic shape attributes and reset name
        XMLElement e = super.toXMLView(anArchiver);
        e.setName("page");

        // Archive DatasetKey, PaintBackground, ClipChildren
        if (getDatasetKey() != null && getDatasetKey().length() > 0) e.add("dataset-key", getDatasetKey());
        if (!getPaintBackground()) e.add("paint-background", false);

        // Return xml element
        return e;
    }

    /**
     * XML archival of children.
     */
    protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // If no layers, archive page as normal shape
        if (getLayerCount() < 2)
            super.toXMLChildren(anArchiver, anElement);

            // Otherwise, iterate over layers and archive
        else for (int i = 0, iMax = getLayerCount(); i < iMax; i++) {
            SGPageLayer layer = getLayer(i);

            // Create layer xml element
            XMLElement layerXML = new XMLElement("layer");

            // Archive Layer Name, Visible, Locked
            if (layer.getName() != null && layer.getName().length() > 0) layerXML.add("name", layer.getName());
            if (!layer.isVisible()) layerXML.add("visible", false);
            //if(layer.isLocked()) layerXML.add("locked", true);

            // Archive children and add to layerElm
            for (int j = 0, jMax = layer.getChildCount(); j < jMax; j++)
                layerXML.add(layer.getChild(j).toXML(anArchiver));

            // Add layer xml to page element
            anElement.add(layerXML);
        }
    }

    /**
     * XML unarchival.
     */
    protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic shape attributes
        super.fromXMLView(anArchiver, anElement);

        // Unarchive DatasetKey, PaintBackground, ClipChildren
        if (anElement.hasAttribute("dataset-key"))
            setDatasetKey(anElement.getAttributeValue("dataset-key"));
        if (anElement.hasAttribute("paint-background"))
            setPaintBackground(anElement.getAttributeBoolValue("paint-background"));
        setVisible(true); // Stupid RM13 RMDocumentLayout set visible to false on multi-page templates and wrote it out
    }

    /**
     * XML unarchival for shape children.
     */
    protected void fromXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // If no layers, unarchive children normally
        if (anElement.get("layer") == null)
            super.fromXMLChildren(anArchiver, anElement);

            // Unarchive layers if present
        else {

            // Clear layer list
            _layers.clear();

            // Iterate over layer elements, unarchiving layers
            for (int i = anElement.indexOf("layer"); i >= 0; i = anElement.indexOf("layer", i + 1)) {

                // Get layer xml element and create layer
                XMLElement layerXML = anElement.get(i);
                SGPageLayer layer = new SGPageLayer(this, layerXML.getAttributeValue("name"));

                // Unarchive Layer Visible, Locked
                if (layerXML.hasAttribute("visible")) layer.setVisible(layerXML.getAttributeBoolValue("visible"));
                if (layerXML.hasAttribute("locked")) layer.setLocked(layerXML.getAttributeBoolValue("locked"));

                // Unarchive children, add to layer and add layer to page
                List children = anArchiver.fromXMLList(layerXML, null, SGView.class, this);
                layer.addChildren(children);
                addLayer(layer);
            }
        }
    }

    /**
     * Editor method - indicates that page supports added children.
     */
    public boolean acceptsChildren()
    {
        return true;
    }

    /**
     * Editor method - indicates that pages can be super-selected.
     */
    public boolean superSelectable()
    {
        return true;
    }
}
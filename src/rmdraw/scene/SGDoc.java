/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.scene;
import java.util.*;
import java.io.File;

import snap.geom.Rect;
import snap.geom.Size;
import snap.gfx.*;
import snap.util.*;
import snap.web.WebURL;

/**
 * The SGDoc class represents an SnapDraw document and is also an SGView subclass, so it can be a real part of
 * the document/view hierarchy.
 * On rare occasions, you may also want to create a document programmatically. Here's an example:
 * <p><blockquote><pre>
 *   SGDoc doc = new SGDoc(612, 792); // Standard US Letter size (8.5" x 11"), in points
 *   RMTable table = new RMTable(); // Create new table ...
 *   doc.getPage(0).addChild(table); // ... and add to first page
 *   table.setBounds(36, 36, 540, 680); // Position and size table
 *   table.getRow("Objects Details").getColumn(0).setText("Title: @getTitle@"); // Configure first text
 * </pre></blockquote><p>
 */
public class SGDoc extends SGParent {

    // The SceneGraph that owns this doc
    private SceneGraph _sceneGraph;

    // The SourceURL
    private WebURL _sourceURL;

    // The currently selected page index
    private int _selIndex;

    // The page layout for the document (single, facing, continuous)
    private PageLayout _pageLayout = PageLayout.Single;

    // The native units of measure for this document
    private Unit _unit = Unit.Point;

    // Whether to show a grid
    private boolean _showGrid = false;

    // Whether to snap to grid
    private boolean _snapGrid = false;

    // Space between grid lines in points
    private double _gridSpacing = 9f;

    // Whether to show margin
    private boolean _showMargin = true;

    // Whether to snap to margin
    private boolean _snapMargin = true;

    // The margin rect
    private Rect _margins = getMarginRectDefault();

    // The string to be used when report encounters a null value
    private String _nullString = "<NA>";

    // Whether document should paginate or grow
    private boolean _paginate = true;

    // Whether output file formats should compress (PDF really)
    private boolean _compress = true;

    // Locale
    public static Locale _locale = Locale.ENGLISH;  // Used by date/number formats    

    // Page Layout Enumerations
    public enum PageLayout {Single, Double, Quadruple, Facing, Continuous, ContinuousDouble}

    // Unit Enumerations
    public enum Unit {Inch, Point, CM, MM, Pica}

    // Constants for property changes
    public static final String SelPageIndex_Prop = "SelPageIndex";

    /**
     * Creates a plain empty document. It's really only used by the archiver.
     */
    public SGDoc()
    {
        addPage();
    }

    /**
     * Creates a document with the given width and height (in printer points).
     */
    public SGDoc(double aWidth, double aHeight)
    {
        addPage();
        setPageSize(aWidth, aHeight);
    }

    /**
     * Creates a new document from aSource using RMArchiver.
     */
    public static SGDoc getDocFromSource(Object aSource)
    {
        RMArchiver arch = new RMArchiver();
        return arch.getDocFromSource(aSource);
    }

    /**
     * Returns the SceneGraph.
     */
    public SceneGraph getSceneGraph()
    {
        return _sceneGraph;
    }

    /**
     * Sets the SceneGraph.
     */
    protected void setSceneGraph(SceneGraph aSG)
    {
        _sceneGraph = aSG;
    }

    /**
     * Returns the Source URL.
     */
    public WebURL getSourceURL()
    {
        return _sourceURL;
    }

    /**
     * Sets the Source URL.
     */
    public void setSourceURL(WebURL aURL)
    {
        _sourceURL = aURL;
    }

    /**
     * Returns the filename associated with this document, if available.
     */
    public String getFilename()
    {
        return getSourceURL() != null ? getSourceURL().getPath() : null;
    }

    /**
     * Returns the document's default font.
     */
    public Font getFont()
    {
        return Font.getDefaultFont();
    }

    /**
     * Returns the number of pages in this document.
     */
    public int getPageCount()
    {
        return getChildCount();
    }

    /**
     * Returns the page at the given index.
     */
    public SGPage getPage(int anIndex)
    {
        return (SGPage) getChild(anIndex);
    }

    /**
     * Returns the last page (convenience).
     */
    public SGPage getPageLast()
    {
        return getPage(getPageCount() - 1);
    }

    /**
     * Returns the list of pages associated with this document.
     */
    public List<SGPage> getPages()
    {
        return (List) _children;
    }

    /**
     * Adds a new page to this document.
     */
    public SGPage addPage()
    {
        addPage(createPage());
        return getPageLast();
    }

    /**
     * Adds a given page to this document.
     */
    public void addPage(SGPage aPage)
    {
        addPage(aPage, getPageCount());
    }

    /**
     * Adds a given page to this document at the given index.
     */
    public void addPage(SGPage aPage, int anIndex)
    {
        addChild(aPage, anIndex);
    }

    /**
     * Removes a page from this document.
     */
    public SGPage removePage(int anIndex)
    {
        return (SGPage) removeChild(anIndex);
    }

    /**
     * Removes the given page.
     */
    public int removePage(SGPage aPage)
    {
        return removeChild(aPage);
    }

    /**
     * Creates a new page.
     */
    public SGPage createPage()
    {
        SGPage page = new SGPage();
        page.setSize(getPageSize());
        return page;
    }

    /**
     * Override to make sure document has size.
     */
    public void addChild(SGView aChild, int anIndex)
    {
        super.addChild(aChild, anIndex);
        if (getWidth() == 0) setBestSize();
    }

    /**
     * Add the pages in the given document to this document (at end) and clears the pages list in the given document.
     */
    public void addPages(SGDoc aDoc)
    {
        // Add pages from given document
        for (SGView page : aDoc.getChildArray())
            addPage((SGPage) page);
    }

    /**
     * Returns the selected page of document.
     */
    public SGPage getSelPage()
    {
        return _selIndex >= 0 && _selIndex < getPageCount() ? getPage(_selIndex) : null;
    }

    /**
     * Selects the selected page.
     */
    public void setSelPage(SGPage aPage)
    {
        setSelPageIndex(aPage.indexOf());
    }

    /**
     * Returns the selected page index of this document.
     */
    public int getSelPageIndex()
    {
        return _selIndex;
    }

    /**
     * Selects the selected page by index.
     */
    public void setSelPageIndex(int anIndex)
    {
        int index = Math.min(anIndex, getPageCount() - 1);
        if (index == _selIndex) return;
        firePropChange(SelPageIndex_Prop, _selIndex, _selIndex = index);
        relayout(); // Rebuild
    }

    /**
     * Returns the page layout for the document.
     */
    public PageLayout getPageLayout()
    {
        return _pageLayout;
    }

    /**
     * Sets the page layout for the document.
     */
    public void setPageLayout(PageLayout aValue)
    {
        if (aValue == _pageLayout) return;
        firePropChange("PageLayout", _pageLayout, _pageLayout = aValue);
        relayoutParent();
    }

    /**
     * Set page layout from string.
     */
    public void setPageLayout(String aValue)
    {
        try {
            setPageLayout(PageLayout.valueOf(StringUtils.firstCharUpperCase(aValue)));
        }
        catch (Exception e) {
            System.err.println("Unsupported Document.PageLayout: " + aValue);
        }
    }

    /**
     * Returns the units used to express sizes in the current document (POINTS, INCHES, CENTIMETERS).
     */
    public Unit getUnit()
    {
        return _unit;
    }

    /**
     * Sets the units used to express sizes in the current document (POINTS, INCHES, CENTIMETERS).
     */
    public void setUnit(Unit aValue)
    {
        if (aValue == _unit) return;
        firePropChange("Unit", _unit, _unit = aValue);
    }

    /**
     * Sets the units used to express sizes in the current document with one of the strings: point, inch or cm.
     */
    public void setUnit(String aString)
    {
        try {
            SGDoc.Unit unit = EnumUtils.valueOfIC(Unit.class, aString);
            setUnit(unit);
        }
        catch (Exception e) {
            System.err.println("Unsupported Document.Unit: " + aString);
        }
    }

    /**
     * Converts given value from document units to printer points (1/72 of an inch).
     */
    public double getPointsFromUnits(double aValue)
    {
        return aValue * getUnitsMultiplier();
    }

    /**
     * Converts given value to document units from printer points (1/72 of an inch).
     */
    public double getUnitsFromPoints(double aValue)
    {
        return aValue / getUnitsMultiplier();
    }

    /**
     * Returns the multiplier used to convert printer points to document units.
     */
    public float getUnitsMultiplier()
    {
        switch (getUnit()) {
            case Inch:
                return 72;
            case CM:
                return 28.34646f;
            case MM:
                return 2.834646f;
            case Pica:
                return 12;
            default:
                return 1;
        }
    }

    /**
     * Returns whether the document should show an alignment grid.
     */
    public boolean isShowGrid()
    {
        return _showGrid;
    }

    /**
     * Sets whether the document should show an alignment grid.
     */
    public void setShowGrid(boolean aValue)
    {
        repaint();
        _showGrid = aValue;
    }

    /**
     * Returns whether the document should snap to an alignment grid.
     */
    public boolean isSnapGrid()
    {
        return _snapGrid;
    }

    /**
     * Sets whether the document should snap to an alignment grid.
     */
    public void setSnapGrid(boolean aValue)
    {
        _snapGrid = aValue;
    }

    /**
     * Returns the grid spacing for the document's grid.
     */
    public double getGridSpacing()
    {
        return _gridSpacing;
    }

    /**
     * Sets the grid spacing for the document's grid.
     */
    public void setGridSpacing(double aValue)
    {
        repaint();
        if (aValue > 0) _gridSpacing = aValue;
    }

    /**
     * Returns whether the document should show a margin rect.
     */
    public boolean isShowMargin()
    {
        return _showMargin;
    }

    /**
     * Sets whether the document should show a margin rect.
     */
    public void setShowMargin(boolean aValue)
    {
        repaint();
        _showMargin = aValue;
    }

    /**
     * Returns whether the document should snap to a margin rect.
     */
    public boolean isSnapMargin()
    {
        return _snapMargin;
    }

    /**
     * Sets whether the document should snap to a margin rect.
     */
    public void setSnapMargin(boolean aValue)
    {
        _snapMargin = aValue;
    }

    /**
     * Returns the margin rect for this document.
     */
    public Rect getMarginRect()
    {
        double marginWidth = getSelPage().getWidth() - getMarginLeft() - getMarginRight();
        double marginHeight = getSelPage().getHeight() - getMarginTop() - getMarginBottom();
        return new Rect(getMarginLeft(), getMarginTop(), marginWidth, marginHeight);
    }

    /**
     * Sets the margin rect for this document.
     */
    public void setMarginRect(Rect aRect)
    {
        repaint();
        _margins = aRect;
    }

    /**
     * Returns the default margin rect.
     */
    public Rect getMarginRectDefault()
    {
        return new Rect(36, 36, 36, 36);
    }

    /**
     * Sets the margin rect for this document.
     */
    public void setMargins(double left, double right, double top, double bottom)
    {
        setMarginRect(new Rect(left, top, right, bottom));
    }

    /**
     * Returns the margin rects left value.
     */
    public double getMarginLeft()
    {
        return _margins.x;
    }

    /**
     * Returns the margin rects right value.
     */
    public double getMarginRight()
    {
        return _margins.width;
    }

    /**
     * Returns the margin rects top value.
     */
    public double getMarginTop()
    {
        return _margins.y;
    }

    /**
     * Returns the margin rects bottom value.
     */
    public double getMarginBottom()
    {
        return _margins.height;
    }

    /**
     * Returns the size of a document page.
     */
    public Size getPageSize()
    {
        return getPageCount() > 0 ? getSelPage().getSize() : getPageSizeDefault();
    }

    /**
     * Sets the size of the document (and all of its pages).
     */
    public void setPageSize(double aWidth, double aHeight)
    {
        // Cache old value
        Object oldVal = getPageSize();

        // Set size of all doc pages
        for (int i = 0, iMax = getPageCount(); i < iMax; i++)
            getPage(i).setSize(aWidth, aHeight);

        // Fire property change and relayout parent
        firePropChange("PageSize", oldVal, new Size(aWidth, aHeight));
        relayoutParent();
    }

    /**
     * Returns the default page size.
     */
    public Size getPageSizeDefault()
    {
        return new Size(612, 792);
    }

    /**
     * Returns the autosizing default.
     */
    public String getAutosizingDefault()
    {
        return "~-~,~-~";
    }

    /**
     * Returns the string used to replace any occurrances of null values in a generated report.
     */
    public String getNullString()
    {
        return _nullString;
    }

    /**
     * Sets the string used to replace any occurrances of null values in a generated report.
     */
    public void setNullString(String aValue)
    {
        _nullString = aValue;
    }

    /**
     * Returns whether the document should paginate generated reports by default.
     */
    public boolean isPaginate()
    {
        return _paginate;
    }

    /**
     * Sets whether the document should paginate generated reports by default.
     */
    public void setPaginate(boolean aValue)
    {
        _paginate = aValue;
    }

    /**
     * Returns whether the document should compress images in generated file formats like PDF.
     */
    public boolean getCompress()
    {
        return _compress;
    }

    /**
     * Sets whether the document should compress images in generated file formats like PDF.
     */
    public void setCompress(boolean aValue)
    {
        _compress = aValue;
    }

    /**
     * Returns the document as an XML byte array.
     */
    public byte[] getBytes()
    {
        return getXML().getBytes();
    }

    /**
     * Returns the document as byte array of a JPEG file.
     */
    public byte[] getBytesJPEG()
    {
        return SGViewUtils.createImage(getPage(0), Color.WHITE).getBytesJPEG();
    }

    /**
     * Returns the document as byte array of PNG file.
     */
    public byte[] getBytesPNG()
    {
        return SGViewUtils.createImage(getPage(0), null).getBytesPNG();
    }

    /**
     * Writes the document out to the given path String (it extracts type from path extension).
     */
    public void write(String aPath)
    {
        String path = aPath.toLowerCase();
        if (path.endsWith(".jpg"))
            SnapUtils.writeBytes(getBytesJPEG(), aPath);
        else if (path.endsWith(".png"))
            SnapUtils.writeBytes(getBytesPNG(), aPath);
        else if (path.endsWith(".rpt") || path.endsWith(".xml"))
            SnapUtils.writeBytes(getXML().getBytes(), aPath);
    }

    /**
     * Writes the document to the given File object
     */
    public void write(File aFile)
    {
        write(aFile.getAbsolutePath());
    }

    /**
     * Override to returns this document.
     */
    public SGDoc getDoc()
    {
        return this;
    }

    /**
     * Overrides paint shape, because document should never really paint itself.
     */
    public void paintView(Painter aPntr)
    {
    }

    /**
     * Performs page substitutions on any text fields that were identified as containing @Page@ keys.
     */
    public void resolvePageReferences()
    {
    }

    /**
     * Override to layout pages.
     */
    protected void layoutImpl()
    {
        // Get document
        int selIndex = getSelPageIndex();
        double offscreen = getWidth() + 5000;

        // If no pages or selected page, return
        if (getPageCount() == 0 || getSelPage() == null) return;

        // Handle PageLayout Single: Iterate over pages, set location to zero and set current page to visible
        if (getPageLayout() == SGDoc.PageLayout.Single) {
            for (int i = 0, iMax = getChildCount(); i < iMax; i++) {
                SGPage page = getPage(i);
                boolean showing = i == selIndex;
                page.setXY(showing ? 0 : offscreen, 0);
            }
        }

        // Handle PageLayout Double: Iterate over pages, set location of alternating pages to zero/page-width
        else if (getPageLayout() == SGDoc.PageLayout.Double) {
            for (int i = 0, iMax = getChildCount(); i < iMax; i += 2) {
                SGPage page1 = getPage(i), page2 = i + 1 < iMax ? getPage(i + 1) : null;
                boolean showing = i == selIndex || i + 1 == selIndex;
                page1.setXY(showing ? 0 : offscreen, 0);
                if (page2 != null)
                    page2.setXY(showing ? page1.getWidth() : offscreen, 0);
            }
        }

        // Handle PageLayout Facing
        else if (getPageLayout() == SGDoc.PageLayout.Facing) {

            // Set location of page 1
            SGPage page = getPage(0);
            page.setXY(selIndex == 0 ? getPageSize().width : offscreen, 0);

            // Iterate over pages, set location of alternating pages to zero/page-width, set current pages to visible
            for (int i = 1, iMax = getChildCount(); i < iMax; i += 2) {
                SGPage page1 = getPage(i), page2 = i + 1 < iMax ? getPage(i + 1) : null;
                boolean showing = i == selIndex || i + 1 == selIndex;
                page1.setXY(showing ? 0 : offscreen, 0);
                if (page2 != null)
                    page2.setXY(showing ? page1.getWidth() : offscreen, 0);
            }
        }

        // Handle PageLayout Continuous: Add all pages and set appropriate xy
        else if (getPageLayout() == SGDoc.PageLayout.Continuous) {
            float y = 0;
            for (int i = 0, iMax = getChildCount(); i < iMax; i++) {
                SGPage page = getPage(i);
                page.setXY(0, y);
                y += page.getHeight() + 10;
            }
        }
    }

    /**
     * Override to return double page width for PageLayout.Double & Facing.
     */
    protected double getPrefWidthImpl(double aHeight)
    {
        double pw = getPageSize().width;
        switch (getPageLayout()) {
            case Double:
            case Facing:
                pw *= 2;
                break;
        }
        if (getPageLayout() != SGDoc.PageLayout.Single) pw += 8;
        return pw;
    }

    /**
     * Override to return height*PageCount (plus spacing) for Continuous.
     */
    protected double getPrefHeightImpl(double aWidth)
    {
        double ph = getPageSize().height;
        if (getPageLayout() == PageLayout.Continuous && getPageCount() > 0) {
            ph *= getPageCount();
            ph += (getPageCount() - 1) * 10;
        }
        if (getPageLayout() != SGDoc.PageLayout.Single) ph += 8;
        return ph;
    }

    /**
     * Returns RXElement for document.
     */
    public XMLElement getXML()
    {
        layoutDeep();
        resolvePageReferences();
        return new RMArchiver().writeToXML(this);
    }

    /**
     * Copies basic document attributes (shallow copy only - no children or pages).
     */
    public SGDoc clone()
    {
        SGDoc clone = (SGDoc) super.clone();
        return clone;
    }

    /**
     * XML archival.
     */
    protected XMLElement toXMLView(XMLArchiver anArchiver)
    {
        // Archive basic shape attributes and reset element name
        XMLElement e = super.toXMLView(anArchiver);
        e.setName("document");

        // Remove questionable document/shape attributes
        e.removeAttribute("x");
        e.removeAttribute("y");
        e.removeAttribute("width");
        e.removeAttribute("height");
        e.removeAttribute("scalex");
        e.removeAttribute("scaley");

        // Archive PageLayout, Unit
        if (getPageLayout() != PageLayout.Single) e.add("page-layout", getPageLayout().name());
        if (getUnit() != Unit.Point) e.add("unit", getUnit().name());

        // Archive ShowMargin, SnapMargin, MarginRect
        if (_showMargin) e.add("show-margin", true);
        if (_snapMargin) e.add("snap-margin", true);
        if ((_showMargin || _snapMargin) && !getMarginRect().equals(getMarginRectDefault()))
            e.add("margin", _margins.getSvgString());

        // Archive ShowGrid, SnapGrid, GridSpacing
        if (_showGrid) e.add("show-grid", true);
        if (_snapGrid) e.add("snap-grid", true);
        if ((_showGrid || _snapGrid) && _gridSpacing != 9) e.add("grid", _gridSpacing);

        // Archive NullString, Paginate, Compress
        if (_nullString != null && _nullString.length() > 0) e.add("null-string", _nullString);
        if (!_paginate) e.add("paginate", _paginate);
        if (!_compress) e.add("compress", false);

        // Return element
        return e;
    }

    /**
     * XML archival of children.
     */
    protected void toXMLChildren(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Archive pages
        for (int i = 0, iMax = getPageCount(); i < iMax; i++)
            anElement.add(anArchiver.toXML(getPage(i), this));
    }

    /**
     * XML unarchival.
     */
    protected void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Remove default page and unarchive basic shape attributes
        while (getPageCount() > 0) removePage(0);
        super.fromXMLView(anArchiver, anElement);
        setSourceURL(anArchiver.getSourceURL());

        // Unarchive PageLayout, Unit
        if (anElement.hasAttribute("page-layout"))
            setPageLayout(anElement.getAttributeValue("page-layout"));
        if (anElement.hasAttribute("unit"))
            setUnit(anElement.getAttributeValue("unit"));

        // Unarchive ShowMargin, SnapMargin, MarginRect
        setShowMargin(anElement.getAttributeBoolValue("show-margin"));
        setSnapMargin(anElement.getAttributeBoolValue("snap-margin"));
        if (anElement.getAttributeValue("margin") != null)
            setMarginRect(Rect.get(anElement.getAttributeValue("margin")));

        // Unarchive ShowGrid, SnapGrid, GridSpacing
        setShowGrid(anElement.getAttributeBoolValue("show-grid"));
        setSnapGrid(anElement.getAttributeBoolValue("snap-grid"));
        setGridSpacing(anElement.getAttributeFloatValue("grid", 9));

        // Unarchive NullString, Paginate, Compress
        setNullString(anElement.getAttributeValue("null-string", ""));
        setPaginate(anElement.getAttributeBoolValue("paginate", true));
        setCompress(anElement.getAttributeBoolValue("compress", true));
    }

    /**
     * Editor method indicates that document is super selectable.
     */
    public boolean superSelectable()
    {
        return true;
    }

    /**
     * Editor method indicates that pages super select immediately.
     */
    public boolean childrenSuperSelectImmediately()
    {
        return true;
    }

    /**
     * Editor method indicates that document accepts children (should probably be false).
     */
    public boolean acceptsChildren()
    {
        return true;
    }
}
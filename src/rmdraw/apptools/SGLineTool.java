/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.apptools;
import rmdraw.app.Tool;
import rmdraw.scene.*;
import java.util.*;

import snap.geom.Point;
import snap.gfx.*;
import snap.util.SnapUtils;
import snap.view.*;
import snap.web.WebURL;

/**
 * This class handles creation of lines.
 */
public class SGLineTool<T extends SGLine> extends Tool<T> {
    
    // Indicates whether line should try to be strictly horizontal or vertical
    private boolean  _hysteresis = false;
    
    // The list of arrow heads
    private static SGView _arrowHeads[];

    // Constants for line segment points
    public static final byte HandleStartPoint = 0;
    public static final byte HandleEndPoint = 1;

    /**
     * Returns the view class that this tool is responsible for.
     */
    public Class getViewClass()  { return SGLine.class; }

    /**
     * Returns the name of this tool to be displayed by inspector.
     */
    public String getWindowTitle()  { return "Line Inspector"; }

    /**
     * Event handling - overridden to install cross-hair cursor.
     */
    public void mouseMoved(ViewEvent anEvent)  { getEditor().setCursor(Cursor.CROSSHAIR); }

    /**
     * Handles mouse press for line creation.
     */
    public void mousePressed(ViewEvent anEvent)
    {
        super.mousePressed(anEvent);
        _hysteresis = true;
    }

    /**
     * Handles mouse drag for line creation.
     */
    public void mouseDragged(ViewEvent anEvent)
    {
        Point currentPoint = getEditorEvents().getEventPointInView(true);
        double dx = currentPoint.getX() - _downPoint.getX();
        double dy = currentPoint.getY() - _downPoint.getY();
        double breakingPoint = 20f;

        if (_hysteresis) {
            if(Math.abs(dx) > Math.abs(dy)) {
                if(Math.abs(dy) < breakingPoint) dy = 0;
                else _hysteresis = false;
            }

            else if(Math.abs(dx) < breakingPoint) dx = 0;
            else _hysteresis = false;
        }

        // Register for repaint
        _newView.repaint();

        // Set adjusted bounds
        _newView.setBounds(_downPoint.getX(), _downPoint.getY(), dx, dy);
    }

    /**
     * Editor method (returns the number of handles).
     */
    public int getHandleCount(T aView)  { return 2; }

    /**
     * Editor method.
     */
    public Point getHandlePoint(T aView, int anIndex, boolean isSuperSel)
    {
        return super.getHandlePoint(aView, anIndex==HandleEndPoint? HandleSE : anIndex, isSuperSel);
    }

    /**
     * Editor method.
     */
    public void moveHandle(T aView, int aHandle, Point aPoint)
    {
        super.moveHandle(aView, aHandle==HandleEndPoint? HandleSE : aHandle, aPoint);
    }

    /**
     * Loads the list of arrows from a .rpt file.
     */
    private SGView[] getArrowHeads()
    {
        // If already set, just return
        if (_arrowHeads!=null) return _arrowHeads;

        // Load document with defined arrow heads
        WebURL url = WebURL.getURL(SGLineTool.class, "SGLineToolArrowHeads.rpt");
        SGDoc doc = SGDoc.getDocFromSource(url);

        // Extract lines and heads and return array of heads
        List <SGLine> lines = doc.getChildrenWithClass(SGLine.class);
        List <SGView> heads = new ArrayList(lines.size()); for(SGLine ln : lines) heads.add(ln.getArrowHead());
        return _arrowHeads = heads.toArray(new SGView[lines.size()]);
    }

    /**
     * Initialize UI panel.
     */
    protected void initUI()
    {
        // Get arrows menu button
        MenuButton menuButton = getView("ArrowsMenuButton", MenuButton.class);

        // Add arrows menu button
        SGView arrowHeads[] = getArrowHeads();
        for (int i=0; i<arrowHeads.length; i++) { SGView ahead = arrowHeads[i];
            Image image = SGViewUtils.createImage(ahead, null);
            MenuItem mi = new MenuItem(); mi.setImage(image); mi.setName("ArrowsMenuButtonMenuItem" + i);
            menuButton.addItem(mi);
        }

        // Add "None" menu item
        MenuItem mi = new MenuItem(); mi.setText("None"); mi.setName("ArrowsMenuButtonMenuItem 999");
        menuButton.addItem(mi);
    }

    /**
     * Update UI panel.
     */
    public void resetUI()
    {
        // Get selected line and arrow head
        SGLine line = getSelView(); if (line==null) return;
        SGLine.ArrowHead ahead = line.getArrowHead();

        // Update ArrowsMenuButton
        Image image = ahead!=null? SGViewUtils.createImage(line.getArrowHead(), null) : null;
        getView("ArrowsMenuButton", MenuButton.class).setImage(image);

        // Update ScaleText and ScaleThumbWheel
        setViewValue("ScaleText", ahead!=null? ahead.getScaleX() : 0);
        setViewValue("ScaleThumbWheel", ahead!=null? ahead.getScaleX() : 0);
        setViewEnabled("ScaleText", ahead!=null);
        setViewEnabled("ScaleThumbWheel", ahead!=null);
    }

    /**
     * Respond to UI change.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Get selected line and arrow head
        SGLine line = getSelView();
        SGLine.ArrowHead arrowHead = line.getArrowHead();

        // Handle ScaleText and ScaleThumbWheel
        if (anEvent.equals("ScaleText") || anEvent.equals("ScaleThumbWheel"))
            arrowHead.setScaleXY(anEvent.getFloatValue(), anEvent.getFloatValue());

        // Handle ArrowsMenuButtonMenuItem
        if (anEvent.getName().startsWith("ArrowsMenuButtonMenuItem")) {
            int ind = SnapUtils.intValue(anEvent.getName());
            SGView ahead = ind<getArrowHeads().length ? getArrowHeads()[ind] : null;
            line.setArrowHead(ahead!=null ? (SGLine.ArrowHead)ahead.clone() : null);
        }
    }
}
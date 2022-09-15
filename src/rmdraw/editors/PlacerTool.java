/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.editors;
import java.text.DecimalFormat;

import snap.geom.Point;
import snap.gfx.*;
import snap.util.MathUtils;
import snap.view.*;

/**
 * This class provides UI editing for a Placer.
 */
public class PlacerTool extends ViewOwner {

    // The Placer
    private Placer _placer;

    // The Autosizing Panel
    private AutosizingPanel _autosizingPanel;

    // A formatter for bounds fields
    private DecimalFormat _fmt = new DecimalFormat("0.##");

    /**
     * Creates a new ShapePlacement pane.
     */
    public PlacerTool(Placer aPlacer)
    {
        _placer = aPlacer;
    }

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        // Get AutosizingPanel
        _autosizingPanel = getView("AutosizingPanel", AutosizingPanel.class);
    }

    /**
     * Updates UI controls from current selection.
     */
    public void resetUI()
    {
        // Get current placer
        Placer placer = _placer;
        double x = placer.getX(), y = placer.getY();
        double width = placer.getWidth(), height = placer.getHeight();
        double roll = placer.getRotation();
        double scaleX = placer.getScaleX();
        double scaleY = placer.getScaleY();
        double skewX = placer.getSkewX();
        double skewY = placer.getSkewY();

        // Update LockedCheckBox
        setViewValue("LockedCheckBox", placer.isLocked());

        // Update XThumb & XText
        setViewValue("XThumb", getUnitsFromPoints(x));
        setViewValue("XText", getUnitsFromPointsStr(x));

        // Update YThumb & YText
        setViewValue("YThumb", getUnitsFromPoints(y));
        setViewValue("YText", getUnitsFromPointsStr(y));

        // Update WThumb & WText
        setViewValue("WThumb", getUnitsFromPoints(width));
        setViewValue("WText", getUnitsFromPointsStr(width));

        // Update HThumb & HText
        setViewValue("HThumb", getUnitsFromPoints(height));
        setViewValue("HText", getUnitsFromPointsStr(height));

        // Update RotationThumb and RotationText
        setViewValue("RotationThumb", roll);
        setViewValue("RotationText", roll);

        // Update ScaleXThumb and ScaleXText
        setViewValue("ScaleXThumb", scaleX);
        setViewValue("ScaleXText", scaleX);

        // Update ScaleYThumb and ScaleYText
        setViewValue("ScaleYThumb", scaleY);
        setViewValue("ScaleYText", scaleY);

        // Update SkewXThumb and SkewXText
        setViewValue("SkewXThumb", skewX);
        setViewValue("SkewXText", skewX);

        // Update SkewYThumb and SkewYText
        setViewValue("SkewYThumb", skewY);
        setViewValue("SkewYText", skewY);

        // Update MinWText and MinHText
        setViewValue("MinWText", placer.isMinWidthSet() ? placer.getMinWidth() : "-");
        setViewValue("MinHText", placer.isMinHeightSet() ? placer.getMinHeight() : "-");

        // Update PrefWText and PrefHText
        setViewValue("PrefWText", placer.isPrefWidthSet() ? placer.getPrefWidth() : "-");
        setViewValue("PrefHText", placer.isPrefHeightSet() ? placer.getPrefHeight() : "-");

        // Disable if document or page
        getUI().setEnabled(placer.isEditable());

        // Update AutosizingPanel
        _autosizingPanel.setAutosizing(placer.getAutosizing());
    }

    /**
     * Updates currently selected shape from UI controls.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Get current placer
        Placer placer = _placer;

        // Handle LockedCheckBox
        if (anEvent.equals("LockedCheckBox")) {
            boolean value = anEvent.getBoolValue();
            placer.setLocked(value);
        }

        // Handle X ThumbWheel and Text
        if (anEvent.equals("XThumb") || anEvent.equals("XText")) {
            double value = anEvent.getFloatValue();
            value = getPointsFromUnits(value);
            placer.setX(value);
        }

        // Handle Y ThumbWheel and Text
        if (anEvent.equals("YThumb") || anEvent.equals("YText")) {
            double value = anEvent.getFloatValue();
            value = getPointsFromUnits(value);
            placer.setY(value);
        }

        // Handle Width ThumbWheel and Text
        if (anEvent.equals("WThumb") || anEvent.equals("WText")) {
            double value = anEvent.getFloatValue();
            value = getPointsFromUnits(value);
            if (Math.abs(value) < .1) value = MathUtils.sign(value) * .1f;
            placer.setWidth(value);
        }

        // Handle Height ThumbWheel and Text
        if (anEvent.equals("HThumb") || anEvent.equals("HText")) {
            double value = anEvent.getFloatValue();
            value = getPointsFromUnits(value);
            if (Math.abs(value) < .1) value = MathUtils.sign(value) * .1f;
            placer.setHeight(value);
        }

        // Handle Rotation Thumb & Text
        if (anEvent.equals("RotationThumb") || anEvent.equals("RotationText")) {
            float value = anEvent.getFloatValue();
            placer.setRotation(value);
        }

        // Handle ScaleX/ScaleY Thumb & Text
        if (anEvent.equals("ScaleXThumb") || anEvent.equals("ScaleXText") ||
                anEvent.equals("ScaleYThumb") || anEvent.equals("ScaleYText")) {
            float value = anEvent.getFloatValue();
            boolean symmetrical = getViewBoolValue("ScaleSymetricCheckBox");

            // Handle ScaleX (and symmetrical)
            if (anEvent.equals("ScaleXThumb") || anEvent.equals("ScaleXText") || symmetrical)
                placer.setScaleX(value);

            // Handle ScaleY (and symmetrical)
            if (anEvent.equals("ScaleYThumb") || anEvent.equals("ScaleYText") || symmetrical)
                placer.setScaleY(value);
        }

        // Handle SkewX Thumb & Text
        if (anEvent.equals("SkewXThumb") || anEvent.equals("SkewXText"))
            placer.setSkewX(anEvent.getFloatValue());

        // Handle SkewY Thumb & Text
        if (anEvent.equals("SkewYThumb") || anEvent.equals("SkewYText"))
            placer.setSkewY(anEvent.getFloatValue());

        // Handle MinWText & MinHText
        if (anEvent.equals("MinWText"))
            placer.setMinWidth(anEvent.getFloatValue());
        if (anEvent.equals("MinHText"))
            placer.setMinHeight(anEvent.getFloatValue());

        // Handle MinWSyncButton & MinHSyncButton
        if (anEvent.equals("MinWSyncButton"))
            placer.setMinWidth(placer.getWidth());
        if (anEvent.equals("MinHSyncButton"))
            placer.setMinHeight(placer.getHeight());

        // Handle PrefWText & PrefHText
        if (anEvent.equals("PrefWText"))
            placer.setPrefWidth(anEvent.getFloatValue());
        if (anEvent.equals("PrefHText"))
            placer.setPrefHeight(anEvent.getFloatValue());

        // Handle PrefWSyncButton & PrefHSyncButton
        if (anEvent.equals("PrefWSyncButton"))
            placer.setPrefWidth(placer.getWidth());
        if (anEvent.equals("PrefHSyncButton"))
            placer.setPrefHeight(placer.getHeight());

        // Handle AutosizingPanel
        if (anEvent.equals("AutosizingPanel"))
            placer.setAutosizing(_autosizingPanel.getAutosizing());

        // Handle ResetAutosizingButton
        if (anEvent.equals("ResetAutosizingButton"))
            placer.setAutosizing("--~,--~");
    }

    /**
     * Converts from shape units to tool units.
     */
    public double getUnitsFromPoints(double aValue)
    {
        return _placer.getUnitsFromPoints(aValue);
    }

    /**
     * Converts from tool units to shape units.
     */
    public double getPointsFromUnits(double aValue)
    {
        return _placer.getPointsFromUnits(aValue);
    }

    /**
     * Converts from shape units to tool units.
     */
    public String getUnitsFromPointsStr(double aValue)
    {
        return _fmt.format(getUnitsFromPoints(aValue));
    }

    /**
     * Returns the name to be used in the inspector's window title.
     */
    public String getWindowTitle()
    {
        return "Placement Inspector";
    }

    /**
     * An inner class to provide a simple springs and struts control.
     */
    public static class AutosizingPanel extends View {

        // Autosizing string
        String _autosizing = "-~~,-~~";

        // Autosizing spring/strut images
        Image _images[];

        // Constants for images
        public static final int BACKGROUND = 0;
        public static final int OUTER_HORIZONTAL_SPRING = 1;
        public static final int OUTER_VERTICAL_SPRING = 2;
        public static final int OUTER_HORIZONTAL_STRUT = 3;
        public static final int OUTER_VERTICAL_STRUT = 4;
        public static final int INNER_HORIZONTAL_SPRING = 5;
        public static final int INNER_VERTICAL_SPRING = 6;
        public static final int INNER_HORIZONTAL_STRUT = 7;
        public static final int INNER_VERTICAL_STRUT = 8;

        /**
         * Creates a new autosizing panel.
         */
        public AutosizingPanel()
        {
            // Get image names
            String imageNames[] = {"SpringsBack.png",
                    "SpringOuterX.png", "SpringOuterY.png",
                    "StrutOuterX.png", "StrutOuterY.png",
                    "SpringX.png", "SpringY.png",
                    "StrutX.png", "StrutY.png"
            };

            // Create images array and load images
            _images = new Image[imageNames.length];
            for (int i = 0; i < imageNames.length; ++i)
                _images[i] = Image.get(getClass(), imageNames[i]);

            // Add mouse listener to send action
            enableEvents(MouseRelease, Action);
        }

        /**
         * ProcessEvent.
         */
        protected void processEvent(ViewEvent anEvent)
        {
            if (!isEnabled() || !anEvent.isMouseEvent()) return;
            StringBuffer sb = new StringBuffer(_autosizing);
            Point p = new Point(anEvent.getX(), anEvent.getY());
            double w = getWidth(), h = getHeight();

            if (p.getDistance(w / 8, h / 2) < w / 8) sb.setCharAt(0, sb.charAt(0) == '-' ? '~' : '-');
            else if (p.getDistance(w * 3 / 8, h / 2) < w / 8) sb.setCharAt(1, sb.charAt(1) == '-' ? '~' : '-');
            else if (p.getDistance(w * 5 / 8, h / 2) < w / 8) sb.setCharAt(1, sb.charAt(1) == '-' ? '~' : '-');
            else if (p.getDistance(w * 7 / 8, h / 2) < w / 8) sb.setCharAt(2, sb.charAt(2) == '-' ? '~' : '-');
            else if (p.getDistance(w / 2, h / 8) < w / 8) sb.setCharAt(4, sb.charAt(4) == '-' ? '~' : '-');
            else if (p.getDistance(w / 2, h * 3 / 8) < w / 8) sb.setCharAt(5, sb.charAt(5) == '-' ? '~' : '-');
            else if (p.getDistance(w / 2, h * 5 / 8) < w / 8) sb.setCharAt(5, sb.charAt(5) == '-' ? '~' : '-');
            else if (p.getDistance(w / 2, h * 7 / 8) < w / 8) sb.setCharAt(6, sb.charAt(6) == '-' ? '~' : '-');

            // Set new autosizing string, send node event and repaint
            _autosizing = sb.toString();
            fireActionEvent(anEvent);
            repaint();
        }

        /**
         * Returns autosizing string.
         */
        public String getAutosizing()
        {
            return _autosizing;
        }

        /**
         * Sets autosizing string.
         */
        public void setAutosizing(String aString)
        {
            _autosizing = aString;
            repaint();
        }

        /**
         * Paints view.
         */
        public void paintFront(Painter aPntr)
        {
            double w = getWidth(), h = getHeight();
            aPntr.setColor(Color.WHITE);
            aPntr.fillRect(0, 0, w, h);
            aPntr.setColor(Color.BLACK);
            aPntr.drawRect(0, 0, w, h);
            aPntr.drawImage(_images[BACKGROUND], 24, 24);

            // Draw horizontal springs (left, middle, right)
            Image i1 = _images[_autosizing.charAt(0) == '-' ? OUTER_HORIZONTAL_STRUT : OUTER_HORIZONTAL_SPRING];
            Image i2 = _images[_autosizing.charAt(1) == '-' ? INNER_HORIZONTAL_STRUT : INNER_HORIZONTAL_SPRING];
            Image i3 = _images[_autosizing.charAt(2) == '-' ? OUTER_HORIZONTAL_STRUT : OUTER_HORIZONTAL_SPRING];
            aPntr.drawImage(i1, 0, 41);
            aPntr.drawImage(i2, 25, 41);
            aPntr.drawImage(i3, 73, 41);

            // Draw vertical springs (top, middle, bottom)
            Image i4 = _images[_autosizing.charAt(4) == '-' ? OUTER_VERTICAL_STRUT : OUTER_VERTICAL_SPRING];
            Image i5 = _images[_autosizing.charAt(5) == '-' ? INNER_VERTICAL_STRUT : INNER_VERTICAL_SPRING];
            Image i6 = _images[_autosizing.charAt(6) == '-' ? OUTER_VERTICAL_STRUT : OUTER_VERTICAL_SPRING];
            aPntr.drawImage(i4, 41, 0);
            aPntr.drawImage(i5, 41, 25);
            aPntr.drawImage(i6, 41, 73);

            // If disabled then dim everything out
            if (!isEnabled()) {
                aPntr.setColor(new Color(1d, .5));
                aPntr.fillRect(0, 0, w, h);
            }
        }
    }
}
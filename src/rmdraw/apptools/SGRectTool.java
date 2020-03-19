/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.apptools;
import rmdraw.app.Tool;
import rmdraw.scene.*;
import java.util.*;
import snap.gfx.Border;
import snap.view.*;

/**
 * This class handles editing of SGRect.
 */
public class SGRectTool<T extends SGRect> extends Tool<T> {
    
    /**
     * Returns a new instance of view class that this tool is responsible for.
     */
    protected T newInstance()
    {
        T view = super.newInstance();
        view.setBorder(Border.blackBorder());
        return view;
    }

    /**
     * Updates the UI controls from the currently selected rectangle.
     */
    public void resetUI()
    {
        // Get selected rectangle (just return if null)
        SGRect rect = getSelView(); if (rect==null) return;

        // Update RoundingThumb and RoundingText
        setViewValue("RoundingThumb", rect.getRadius());
        setViewValue("RoundingText", rect.getRadius());
    }

    /**
     * Updates the currently selected rectangle from the UI controls.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Get the current rect and list of rects (just return if null)
        SGRect rect = getSelView(); if (rect==null) return;
        List <SGRect> rects = (List) getSelViews();

        // Handle Rounding Radius Thumb & Text
        if (anEvent.equals("RoundingThumb") || anEvent.equals("RoundingText")) {
            rect.undoerSetUndoTitle("Rounding Change");
            float value = anEvent.getFloatValue();
            for (SGRect r : rects) {
                r.setRadius(value);
                if (r.getBorder()==null)
                    r.setBorder(Border.blackBorder());
            }
        }
    }

    /**
     * Event handling - overridden to install cross-hair cursor.
     */
    public void mouseMoved(ViewEvent anEvent)  { getEditor().setCursor(Cursor.CROSSHAIR); }

    /**
     * Returns the class that this tool is responsible for.
     */
    public Class getViewClass()  { return SGRect.class; }

    /**
     * Returns the name to be presented to user.
     */
    public String getWindowTitle()  { return "Rectangle Tool"; }
}
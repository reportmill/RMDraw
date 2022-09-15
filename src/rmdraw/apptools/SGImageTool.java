/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.apptools;
import rmdraw.app.*;
import rmdraw.scene.*;

import java.util.List;

import snap.gfx.Border;
import snap.gfx.Image;
import snap.util.*;
import snap.view.*;
import snap.viewx.FilePanel;

/**
 * Provides UI for SGImage editing.
 */
public class SGImageTool<T extends SGImage> extends Tool<T> {

    /**
     * Returns the class that this tool is responsible for.
     */
    public Class getViewClass()
    {
        return SGImage.class;
    }

    /**
     * Returns the string used for the inspector window title.
     */
    public String getWindowTitle()
    {
        return "Image Tool";
    }

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        enableEvents("KeyText", DragDrop);
    }

    /**
     * Updates the UI controls from the currently selected image.
     */
    public void resetUI()
    {
        // Get selected image view and image (just return if null)
        SGImage imgView = getSelView();
        if (imgView == null) return;
        Image img = imgView.getImage();

        // Reset KeyText, MarginsText, GrowToFitCheckBox, PreserveRatioCheckBox
        setViewValue("KeyText", imgView.getKey());
        setViewValue("PaddingText", StringUtils.toString(getUnitsFromPoints(imgView.getPadding())));
        setViewValue("GrowToFitCheckBox", imgView.isGrowToFit());
        setViewValue("PreserveRatioCheckBox", imgView.getPreserveRatio());

        // Reset RoundingThumb and RoundingText
        setViewValue("RoundingThumb", imgView.getRadius());
        setViewValue("RoundingText", imgView.getRadius());

        // Reset TypeLabel
        if (img == null) setViewValue("TypeLabel", "");
        else
            setViewValue("TypeLabel", "Type: " + img.getType() + "\nSize: " + img.getPixWidth() + "x" + img.getPixHeight() +
                    " (" + (int) (imgView.getWidth() / img.getWidth() * imgView.getScaleX() * 100) + "%)");

        // Reset SaveButton, JPEGButton enabled
        setViewEnabled("SaveButton", img != null);
        setViewEnabled("JPEGButton", img != null && !img.getType().equals("jpg"));
    }

    /**
     * Updates the currently selected image from the UI controls.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Get selected image view and image views (just return if null)
        SGImage imgView = getSelView();
        if (imgView == null) return;
        List<SGImage> images = (List) getSelViews();

        // Handle KeyText
        if (anEvent.equals("KeyText"))
            imgView.setKey(StringUtils.delete(anEvent.getStringValue(), "@"));

        // Handle KeysButton
        if (anEvent.equals("KeysButton"))
            getEditorPane().getAttributesPanel().setVisibleName(AttributesPanel.KEYS);

        // Handle PaddingText
        if (anEvent.equals("PaddingText"))
            for (SGImage im : images) im.setPadding(anEvent.getIntValue());

        // Handle GrowToFitCheckBox, PreserveRatioCheckBox
        if (anEvent.equals("GrowToFitCheckBox"))
            for (SGImage im : images) im.setGrowToFit(anEvent.getBoolValue());
        if (anEvent.equals("PreserveRatioCheckBox"))
            for (SGImage im : images) im.setPreserveRatio(anEvent.getBoolValue());

        // Handle Rounding Radius Thumb & Text
        if (anEvent.equals("RoundingThumb") || anEvent.equals("RoundingText")) {
            imgView.undoerSetUndoTitle("Rounding Change");
            float value = anEvent.getFloatValue();
            for (SGImage im : images) {
                im.setRadius(value);
                if (im.getBorder() == null)
                    im.setBorder(Border.blackBorder());
            }
        }

        // Handle SaveButton
        if (anEvent.equals("SaveButton")) {
            Image img = imgView.getImage();
            if (img == null) return;
            String type = img.getType();
            if (StringUtils.length(type) == 0) return;
            String path = FilePanel.showSavePanel(getEditor(), type.toUpperCase() + " File", type);
            if (path == null) return;
            SnapUtils.writeBytes(img.getBytes(), path);
        }

        // Handle JPEGButton
        if (anEvent.equals("JPEGButton")) {
            Image img = imgView.getImage();
            if (img == null) return;
            byte jpegBytes[] = img.getBytesJPEG();
            imgView.setImageForSource(jpegBytes);
        }
    }
}
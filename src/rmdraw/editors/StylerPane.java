/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.editors;
import snap.view.*;

/**
 * This class provides UI for editing fills, borders, effects, transparency.
 */
public class StylerPane extends StylerOwner {

    // The PaintTool
    private PaintTool _fillTool;

    // The BorderTool
    private BorderTool _borderTool;

    // The FontTool
    private FontTool _fontTool;
    
    // The EffectTool
    private EffectTool _effectTool;
    
    /**
     * Creates StylerPane.
     */
    public StylerPane(Styler aStyler)
    {
        setStyler(aStyler);
    }

    /**
     * Initialize UI panel.
     */
    protected void initUI()
    {
        // Get Main UI
        ColView mainView = getUI(ColView.class);

        // Create/install BorderTool
        _fillTool = new PaintTool();
        _fillTool.setStyler(getStyler());
        mainView.addChild(_fillTool.getUI(), 0);

        // Create/install BorderTool
        _borderTool = new BorderTool(getStyler());
        mainView.addChild(_borderTool.getUI(), 1);

        // Install FontTool
        _fontTool = new FontTool();
        _fontTool.setStyler(getStyler());
        mainView.addChild(_fontTool.getUI(), 2);

        // Initialize tools
        _effectTool = new EffectTool();
        _effectTool.setStyler(getStyler());
        mainView.addChild(_effectTool.getUI(), 3);
    }

    /**
     * Reset UI controls from current selection.
     */
    public void resetUI()
    {
        // Reset FillTool, BorderTool, EffectTool
        _fillTool.resetLater();
        _borderTool.resetLater();
        _effectTool.resetLater();

        // Reset FontTool
        _fontTool.resetLater();

        // Update TransparencySlider, TransparencyText (transparency is opposite of opacity and on 0-100 scale)
        Styler styler = getStyler();
        double transparency = 100 - styler.getOpacity()*100;
        setViewValue("TransparencySlider", transparency);
        setViewValue("TransparencyText", transparency);
    }

    /**
     * Updates currently selected shapes from UI controls.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Get styler
        Styler styler = getStyler();

        // Handle Transparency Slider and Text
        if (anEvent.equals("TransparencySlider") || anEvent.equals("TransparencyText")) {
            double eval = anEvent.equals("TransparencySlider")? anEvent.getIntValue() : anEvent.getFloatValue();
            double val = 1 - eval/100;
            styler.setOpacity(val);
        }
    }

    /**
     * Returns the display name for the inspector.
     */
    public String getWindowTitle()  { return "Paint/Fill Inspector"; }
}
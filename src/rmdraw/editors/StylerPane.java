/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.editors;
import snap.gfx.*;
import snap.view.*;

/**
 * This class provides UI for editing fills, borders, effects, transparency.
 */
public class StylerPane extends ViewOwner {

    // The Styler used to get/set Paint/Border/Effect attributes
    private Styler _styler;

    // The PaintTool
    private PaintTool _fillTool = new PaintTool();

    // The BorderTool
    private BorderTool _borderTool = new BorderTool();

    // The FontPanel
    private FontPanel _fontPanel;
    
    // The EffectTool
    private EffectTool _effectTool = new EffectTool();
    
    /**
     * Creates a new ShapeFills pane.
     */
    public StylerPane(Styler aStyler)
    {
        setStyler(aStyler);
    }

    /**
     * Returns the styler.
     */
    public Styler getStyler()  { return _styler; }

    /**
     * Sets the styler.
     */
    public void setStyler(Styler aStyler)
    {
        _styler = aStyler;
    }

    /**
     * Initialize UI panel.
     */
    protected void initUI()
    {
        // Initialize tools
        _fillTool.setStyler(getStyler());
        _borderTool.setStyler(getStyler());
        _effectTool.setStyler(getStyler());

        // Get array of known stroke names and initialize StrokeComboBox
//        int scount = _borderTool.getBorderCount();
//        Object snames[] = new String[scount];
//        for (int i=0;i<scount;i++) snames[i] = _borderTool.getBorder(i).getName();
//        setViewItems("StrokeComboBox", snames);

        // Get array of known fill names and initialize FillComboBox
        int fcount = _fillTool.getFillCount();
        Object fnames[] = new String[fcount];
        for (int i=0;i<fcount;i++) fnames[i] = _fillTool.getFill(i).getName();
        setViewItems("FillComboBox", fnames);

        // Configure Borders
        Label l1 = getView("LineBdrButton", ButtonBase.class).getLabel();
        l1.setPrefSize(16,16);
        l1.setBorder(Color.BLACK,1);
        Label l2 = getView("LowerBdrButton", ButtonBase.class).getLabel();
        l2.setPrefSize(16,16);
        l2.setBorder(new Borders.BevelBorder(0));
        Label l3 = getView("RaiseBdrButton", ButtonBase.class).getLabel();
        l3.setPrefSize(16,16);
        l3.setBorder(new Borders.BevelBorder(1));
        Label l4 = getView("EtchBdrButton", ButtonBase.class).getLabel();
        l4.setPrefSize(16,16);
        l4.setBorder(new Borders.EtchBorder());
        Label l5 = getView("EdgeBdrButton", ButtonBase.class).getLabel();
        l5.setPrefSize(16,16);
        l5.setBorder(new Borders.EdgeBorder());
        Label l0 = getView("NoBdrButton", ButtonBase.class).getLabel();
        l0.setPrefSize(16,16);
        l0.setBorder(Color.LIGHTGRAY,1);

        // Install FontPanel
        _fontPanel = new FontPanel(getStyler());
        TitleView textTitleView = getView("TextTitleView", TitleView.class);
        textTitleView.setContent(_fontPanel.getUI());

        // Get array of known effect names and initialize EffectComboBox
        int ecount = _effectTool.getEffectCount();
        Object enames[] = new String[ecount];
        for (int i=0;i<ecount;i++) enames[i] = _effectTool.getEffect(i).getName();
        setViewItems("EffectComboBox", enames);
    }

    /**
     * Reset UI controls from current selection.
     */
    public void resetUI()
    {
        // Get current border (or default, if not available)
        Styler styler = getStyler();
        Border border = styler.getBorder();
        if (border==null) border = Border.blackBorder();

        // Update StrokeCheckBox, StrokeComboBox, Border Buttons
        setViewValue("StrokeCheckBox", styler.getBorder()!=null);
        //setViewValue("StrokeComboBox", border.getName());
        Border bdr = styler.getBorder();
        setViewValue("NoBdrButton", bdr==null);
        setViewValue("LineBdrButton", bdr instanceof Borders.LineBorder);
        setViewValue("LowerBdrButton", bdr instanceof Borders.BevelBorder && ((Borders.BevelBorder)bdr).getType()==0);
        setViewValue("RaiseBdrButton", bdr instanceof Borders.BevelBorder && ((Borders.BevelBorder)bdr).getType()==1);
        setViewValue("EtchBdrButton", bdr instanceof Borders.EtchBorder);
        setViewValue("EdgeBdrButton", bdr instanceof Borders.EdgeBorder);

        // Get stroke tool, install tool UI in stroke panel and ResetUI
        BorderTool btool = _borderTool.getTool(border);
        getView("StrokePane", BoxView.class).setContent(btool.getUI());
        btool.resetLater();

        // Get current fill (or default, if not available)
        Paint fill = styler.getFill();
        if (fill==null) fill = Color.BLACK;

        // Update FillCheckBox, FillComboBox
        setViewValue("FillCheckBox", styler.getFill()!=null);
        setViewValue("FillComboBox", fill.getName());

        // Get fill tool, install tool UI in fill panel and ResetUI
        PaintTool ftool = _fillTool.getTool(fill);
        getView("FillPane", BoxView.class).setContent(ftool.getUI());
        ftool.resetLater();

        // Get current effect (or default, if not available)
        Effect effect = styler.getEffect();
        if (effect==null) effect = new ShadowEffect();

        // Update EffectCheckBox, EffectComboBox
        setViewValue("EffectCheckBox", styler.getEffect()!=null);
        setViewValue("EffectComboBox", effect.getName());

        // Get effect tool, install tool UI in effect panel and ResetUI
        EffectTool etool = _effectTool.getTool(effect);
        getView("EffectPane", BoxView.class).setContent(etool.getUI());
        etool.resetLater();

        // Reset FontPanel
        _fontPanel.resetLater();

        // Update TransparencySlider, TransparencyText (transparency is opposite of opacity and on 0-100 scale)
        double transparency = 100 - styler.getOpacity()*100;
        setViewValue("TransparencySlider", transparency);
        setViewValue("TransparencyText", transparency);
    }

    /**
     * Updates currently selected shapes from UI controls.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Get the current editor and currently selected shapes list (just return if null)
        Styler styler = getStyler();

        // Handle StrokeCheckBox: Add border if not there or remove if there
        if (anEvent.equals("StrokeCheckBox")) {
            boolean selected = anEvent.getBoolValue();
            Border border = selected ? Border.blackBorder() : null;
            styler.setBorder(border);
        }

        // Handle StrokeComboBox: Get selected border and iterate over shapes and set
//        if (anEvent.equals("StrokeComboBox")) {
//            Border border = _borderTool.getBorder(anEvent.getSelIndex());
//            styler.setBorder(border);
//        }

        // Handle NoBdrButton, LineBdrButton, LowerBdrButton, RaiseBdrButton, EtchBdrButton
        if (anEvent.equals("NoBdrButton")) styler.setBorder(null);
        if (anEvent.equals("LineBdrButton")) styler.setBorder(Border.blackBorder());
        if (anEvent.equals("LowerBdrButton")) styler.setBorder(new Borders.BevelBorder(0));
        if (anEvent.equals("RaiseBdrButton")) styler.setBorder(new Borders.BevelBorder(1));
        if (anEvent.equals("EtchBdrButton")) styler.setBorder(new Borders.EtchBorder());
        if (anEvent.equals("EdgeBdrButton")) styler.setBorder(new Borders.EdgeBorder());

        // Handle FillCheckBox: Iterate over shapes and add fill if not there or remove if there
        if (anEvent.equals("FillCheckBox")) {
            boolean selected = anEvent.getBoolValue();
            Paint fill = selected ? Color.BLACK : null;
            styler.setFill(fill);
        }

        // Handle FillComboBox: Get selected fill instance and iterate over shapes and add fill if not there
        if (anEvent.equals("FillComboBox")) {
            Paint fill = _fillTool.getFill(anEvent.getSelIndex());
            styler.setFill(fill);
        }

        // Handle EffectCheckBox: Iterate over shapes and add effect if not there or remove if there
        if (anEvent.equals("EffectCheckBox")) {
            boolean selected = anEvent.getBoolValue();
            Effect eff = selected ? new ShadowEffect() : null;
            styler.setEffect(eff);
        }

        // Handle EffectComboBox: Get selected effect instance and iterate over shapes and add effect if not there
        if (anEvent.equals("EffectComboBox")) {
            Effect eff = _effectTool.getEffect(anEvent.getSelIndex());
            styler.setEffect(eff);
        }

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
/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.app;
import rmdraw.apptools.*;
import rmdraw.scene.*;
import java.util.*;

import snap.geom.HPos;
import snap.geom.Pos;
import snap.gfx.*;
import snap.props.Undoer;
import snap.util.*;
import snap.view.*;
import snap.viewx.*;

/**
 * Tool bar for EditorPane.
 */
public class EditorPaneToolBar extends EditorPane.SupportPane {

    // The font face ComboBox
    private ComboBox <String> _fontFaceComboBox;
    
    // The font size ComboBox
    private ComboBox _fontSizeComboBox;
    
    // The editor selected color ColorWell (hidden)
    ColorWell         _colorWell;
    
    // The toolbar tools
    Tool _toolBarTools[];
    
/**
 * Creates a new editor pane tool bar.
 */
public EditorPaneToolBar(EditorPane anEP)
{
    super(anEP);
    _toolBarTools = createToolBarTools();
}

/**
 * Returns the ToolBar view.
 */
public RowView getToolsView()
{
    View selectTool = getView("SelectToolButton");
    return (RowView) selectTool.getParent();
}

/**
 * Initialize UI.
 */
protected void initUI()
{
    // Get/configure FontFaceComboBox
    _fontFaceComboBox = getView("FontFaceComboBox", ComboBox.class);
    _fontFaceComboBox.getPopupList().setMaxRowCount(20);
    _fontFaceComboBox.setItems(Font.getFamilyNames());
    
    // Get/configure FontSizeComboBox
    _fontSizeComboBox = getView("FontSizeComboBox", ComboBox.class);
    Object sizes[] = { 6, 8, 9, 10, 11, 12, 14, 16, 18, 22, 24, 36, 48, 64, 72, 96, 128, 144 };
    _fontSizeComboBox.setItems(sizes);
    _fontSizeComboBox.setItemTextFunction(i -> SnapUtils.stringValue(i) + " pt");
    
    // Create/configure hidden ColorWell
    _colorWell = new ColorWell();
    _colorWell.setName("ColorWell");
    _colorWell.setOwner(this);

    // Register ColorWell as ColorPanel.DefaultColorWell (after delay so we get Editor ColorPanel, not system)
    runLater(() -> ColorPanel.getShared().setDefaultColorWell(_colorWell));

    // Install InspectorPanel.TitleLabel
    RowView rowView = (RowView)getUI(ColView.class).getChild(1);
    Label titleLabel = getEditorPane().getInspectorPanel().getView("TitleLabel", Label.class);
    titleLabel.setAlign(Pos.BOTTOM_CENTER);
    titleLabel.setPadding(0,0,0,0);
    titleLabel.setPrefSize(275, 20);
    titleLabel.setLean(Pos.BOTTOM_RIGHT);
    titleLabel.setTransY(2);
    rowView.addChild(titleLabel);
}

/**
 * Updates the UI panel controls.
 */
protected void resetUI()
{
    // Get the editor
    Editor editor = getEditor();
    Font font = editor.getStyler().getFont();
    
    // Update UndoButton, RedoButton
    Undoer undoer = editor.getUndoer();
    setViewEnabled("UndoButton", undoer!=null && undoer.getUndoSetLast()!=null);
    setViewEnabled("RedoButton", undoer!=null && undoer.getRedoSetLast()!=null);
    
    // Reset PreviewEditButton state if out of sync
    if(getViewBoolValue("PreviewEditButton")==getEditorPane().isEditing())
        setViewValue("PreviewEditButton", !getEditorPane().isEditing());

    // Get selected tool button name and button - if found and not selected, select it
    String toolButtonName = editor.getCurrentTool().getClass().getSimpleName() + "Button";
    ToggleButton toolButton = getView(toolButtonName, ToggleButton.class);
    if(toolButton!=null && !toolButton.isSelected())
        toolButton.setSelected(true);
        
    // Reset FontFaceComboBox, FontSizeComboBox
    _fontFaceComboBox.setSelItem(font.getFamily());
    String fstext = _fontSizeComboBox.getText(font.getSize());
    _fontSizeComboBox.setText(fstext);
        
    // Reset BoldButton, ItalicButton, UnderlineButton
    setViewValue("BoldButton", font.isBold());
    setViewEnabled("BoldButton", font.getBold()!=null);

    // Update ColorWell
    Color color = editor.getStyler().getFillColor();
    _colorWell.setColor(color);
}

/**
 * Responds to UI panel control changes.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Get the editor
    EditorPane epane = getEditorPane();
    Editor editor = getEditor();
    
    // Handle File NewButton, OpenButton, SaveButton, PrintButton
    if (anEvent.equals("NewButton")) epane.respondUI(anEvent);
    if (anEvent.equals("OpenButton")) epane.respondUI(anEvent);
    if (anEvent.equals("SaveButton")) epane.respondUI(anEvent);
    if (anEvent.equals("PrintButton")) epane.respondUI(anEvent);
        
    // Handle Edit CutButton, CopyButton, PasteButton, DeleteButton
    if (anEvent.equals("CutButton")) epane.respondUI(anEvent);
    if (anEvent.equals("CopyButton")) epane.respondUI(anEvent);
    if (anEvent.equals("PasteButton")) epane.respondUI(anEvent);
    if (anEvent.equals("DeleteButton")) editor.delete();
        
    // Handle Edit UndoButton, RedoButton
    if (anEvent.equals("UndoButton")) epane.respondUI(anEvent);
    if (anEvent.equals("RedoButton")) epane.respondUI(anEvent);
    
    // Handle FillColorButton, StrokeColorButton, TextColorButton
    if (anEvent.equals("FillColorButton"))
        editor.getStyler().setFillColor(anEvent.getView(ColorButton.class).getColor());
    if (anEvent.equals("StrokeColorButton"))
        editor.getStyler().setStrokeColor(anEvent.getView(ColorButton.class).getColor());
    if (anEvent.equals("TextColorButton"))
        editor.getStyler().setTextColor(anEvent.getView(ColorButton.class).getColor());

    // Handle SamplesButton
    if(anEvent.equals("SamplesButton")) {
        stopSamplesButtonAnim();
        epane.hideAttributesDrawer();
        new SamplesPane().showSamples(epane);
    }
    
    // Handle Preview/Edit button and PreviewMenuItem
    if(anEvent.equals("PreviewEditButton") || anEvent.equals("PreviewMenuItem")) {
        
        // Hack to open edited file as text file
        if(anEvent.isAltDown()) openDocTextFile();
        
        // Normal preview
        else getEditorPane().setEditing(!getEditorPane().isEditing());
    }
    
    // Handle PreviewXMLMenuItem
    if (anEvent.equals("PreviewXMLMenuItem"))
        EditorPaneUtils.previewXML(getEditorPane());

    // Handle ToolButton(s)
    if (anEvent.getName().endsWith("ToolButton"))
        setToolForButtonName(anEvent.getName());

    // Handle FontFaceComboBox
    if (anEvent.equals("FontFaceComboBox")) {
        String fname = anEvent.getText();
        editor.getStyler().setFontFamily(fname);
        editor.requestFocus();
    }
    
    // Handle FontSizeComboBox
    if (anEvent.equals("FontSizeComboBox")) {
        editor.getStyler().setFontSize(anEvent.getFloatValue(), false);
        editor.requestFocus();
    }
    
    // Handle FontSizeUpButton, FontSizeDownButton, BoldButton
    if (anEvent.equals("FontSizeUpButton")) { Font font = editor.getStyler().getFont();
        editor.getStyler().setFontSize(font.getSize()<16? 1 : 2, true); }
    if (anEvent.equals("FontSizeDownButton")) { Font font = editor.getStyler().getFont();
        editor.getStyler().setFontSize(font.getSize()<16? -1 : -2, true); }
    if (anEvent.equals("BoldButton"))
        editor.getStyler().setFontBold(anEvent.getBoolValue());

    // Handle AlignLeftButton, AlignCenterButton, AlignRightButton, AlignFullButton
    if (anEvent.equals("AlignLeftButton"))
        editor.getStyler().setAlignX(HPos.LEFT);
    if (anEvent.equals("AlignCenterButton"))
        editor.getStyler().setAlignX(HPos.CENTER);
    if (anEvent.equals("AlignRightButton"))
        editor.getStyler().setAlignX(HPos.RIGHT);
    if (anEvent.equals("AlignFullButton"))
        editor.getStyler().setJustify(true);
    
    // Handle ColorWell
    if (anEvent.equals("ColorWell"))
        editor.getStyler().setFillColor(_colorWell.getColor());
}

/**
 * Sets the current tool for toolbar name.
 */
protected void setToolForButtonName(String aName)
{
    Tool tool = getToolForButtonName(aName);
    getEditor().setCurrentTool(tool);
}

/**
 * Sets the current tool for toolbar name.
 */
protected Tool getToolForButtonName(String aName)
{
    for (Tool tool : _toolBarTools)
        if (aName.startsWith(tool.getClass().getSimpleName()))
            return tool;
    System.out.println("EditorPaneToolBar.getToolForToolButtonName: Unknown name: " + aName);
    return null;
}

/**
 * Opens the editor document as a text file.
 */
private void openDocTextFile()
{
    // Get filename for doc (if not set, write doc to temp file)
    String fname = getEditor().getDoc().getFilename();
    if (fname==null) {
        fname = SnapUtils.getTempDir() + "RMDoc.rpt";
        getEditor().getDoc().write(fname);
    }
    
    // Open file
    GFXEnv.getEnv().openTextFile(fname);
}

/**
 * Animate SampleButton.
 */
public void startSamplesButtonAnim()
{
    // Get button
    View btn = getView("SamplesButton");
    btn.setScale(1.2);
    
    // Configure anim
    ViewAnim anim = btn.getAnim(0);
    anim.getAnim(400).setScale(1.4).getAnim(800).setScale(1.2).getAnim(1200).setScale(1.4).getAnim(1600).setScale(1.2)
    .getAnim(2400).setRotate(360);
    anim.setLoopCount(3).play();
}

/**
 * Stops SampleButton animation.
 */
public void stopSamplesButtonAnim()
{
    View btn = getView("SamplesButton");
    btn.getAnim(0).finish();
}

/**
 * Returns the ToolBar tools.
 */
public Tool[] getToolBarTools()  { return _toolBarTools; }

/**
 * Creates the list of tool instances for tool bar.
 */
protected Tool[] createToolBarTools()
{
    List <Tool> tools = new ArrayList();
    Editor editor = getEditor();
    tools.add(editor.getSelectTool());
    tools.add(editor.getToolForClass(SGLine.class));
    tools.add(editor.getToolForClass(SGRect.class));
    tools.add(editor.getToolForClass(SGOval.class));
    tools.add(editor.getToolForClass(SGText.class));
    tools.add(editor.getToolForClass(SGPolygon.class));
    tools.add(new SGPolygonTool.PencilTool(editor));
    return tools.toArray(new Tool[0]);
}

}
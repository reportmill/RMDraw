/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.app;
import java.util.ArrayList;
import java.util.List;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;

/**
 * This class is a Font panel for selecting fonts. It lets the user easily select a font family,
 * font size and font style (bold, italic, underline, outline). It also has a convenient slider for interactively
 * changing the size and a text field for specifically setting a size. In addition, there is a pick list that
 * shows all the individual fonts available for a given family.
 */
public class FontPanel extends ViewOwner {
    
    // The EditorPane
    EditorPane _editorPane;
    
    // Whether to show all fonts (or PDF only)
    boolean         _showAll = true;
    
    // The PDF Family names
    String          _pdfFonts[];

    // All PDF built in font family names.
    static String   _pdfBuiltIns[] = { "Arial", "Helvetica", "Times", "Courier", "Symbol", "ZapfDingbats" };

/**
 * Creates a new FontPanel for EditorPane.
 */
public FontPanel(EditorPane anEP)  { _editorPane = anEP; }

/**
 * Returns the Editor.
 */
public Editor getEditor()  { return _editorPane.getEditor(); }

/**
 * Returns the EditorPane.
 */
public EditorPane getEditorPane()  { return _editorPane; }

/**
 * Returns the array of font family names.
 */
public String[] getFamilyNames()
{
    return _showAll? Font.getFamilyNames() : getPDFFamilyNames();
}

/**
 * Returns the array of font family names.
 */
public String[] getPDFFamilyNames()
{
    if(_pdfFonts!=null) return _pdfFonts;
    List pdfs = new ArrayList();
    for(String name : _pdfBuiltIns) { Font font = Font.get(name, 12);
        if(font!=null) pdfs.add(font.getFamily()); }
    return _pdfFonts = (String[])pdfs.toArray(new String[0]);
}

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Get/configure FamilyList
    ListView <String> familyList = getView("FamilyList", ListView.class);
    familyList.setItems(getFamilyNames());
    
    // Get/configure FamilyComboBox
    ComboBox familyComboBox = getView("FamilyComboBox", ComboBox.class);
    familyComboBox.setListView(familyList);
    
    // Configure SizesList
    setViewItems("SizesList", new Object[] { 6,8,9,10,11,12,14,16,18,22,24,36,48,64,72,96,128,144 });
    
    // Configure SizeText
    TextField sizeText = getView("SizeText", TextField.class);
    sizeText.addPropChangeListener(pce -> { if(sizeText.isFocused()) sizeText.selectAll(); }, View.Focused_Prop);
}

/**
 * Reset UI from the current selection.
 */
public void resetUI()
{
    // Get current font
    Editor editor = getEditor();
    Font font = EditorUtils.getFont(editor);
    
    // Get family name and size
    String familyName = font.getFamily();
    double size = font.getSize();
    
    // Reset FamilyList, SizesList, SizeText, SizeThumb, and Bold, Italic, Underline and Outline buttons
    setViewItems("FamilyList", getFamilyNames());
    setViewSelItem("FamilyList", familyName);
    setViewValue("SizesList", (int)size);
    setViewValue("SizeText", StringUtils.toString(size) + " pt");
    setViewValue("BoldButton", font.isBold());
    setViewEnabled("BoldButton", font.getBold()!=null);
    setViewValue("ItalicButton", font.isItalic());
    setViewEnabled("ItalicButton", font.getItalic()!=null);
    setViewValue("UnderlineButton", EditorUtils.isUnderlined(editor));
    setViewValue("OutlineButton", EditorUtils.getTextBorder(editor)!=null);
    
    // Get font names in currently selected font's family
    String familyNames[] = Font.getFontNames(font.getFamily());
    
    // Reset FontNameComboBox Items, SelItem and Enabled
    setViewItems("FontNameComboBox", familyNames);
    String fn = font.getFontFile().getNativeName(); setViewSelItem("FontNameComboBox", fn);
    setViewEnabled("FontNameComboBox", familyNames.length>1);
}

/**
 * Respond to UI controls.
 */
public void respondUI(ViewEvent anEvent)
{
    // Get current editor
    Editor editor = getEditor();
    
    // Handle FontSizeUpButton, FontSizeDownButton
    if(anEvent.equals("FontSizeUpButton")) { Font font = EditorUtils.getFont(editor);
        EditorUtils.setFontSize(editor, font.getSize()<16? 1 : 2, true); }
    if(anEvent.equals("FontSizeDownButton")) { Font font = EditorUtils.getFont(editor);
        EditorUtils.setFontSize(editor, font.getSize()<16? -1 : -2, true); }
    
    // Handle BoldButton, ItalicButton, UnderlineButton, OutlineButton
    if(anEvent.equals("BoldButton")) EditorUtils.setFontBold(editor, anEvent.getBoolValue());
    if(anEvent.equals("ItalicButton")) EditorUtils.setFontItalic(editor, anEvent.getBoolValue());
    if(anEvent.equals("UnderlineButton")) EditorUtils.setUnderlined(editor);
    if(anEvent.equals("OutlineButton")) EditorUtils.setTextBorder(editor);
    
    // Handle FontPickerButton
    if(anEvent.equals("FontPickerButton")) {
        Font ofont = EditorUtils.getFont(editor);
        Font font = new FontPicker().showPicker(getEditorPane().getUI(), ofont);
        if(font!=null)
            EditorUtils.setFontFamily(editor, font);
    }
    
    // Handle SizesList
    if(anEvent.equals("SizesList") && anEvent.getValue()!=null)
        EditorUtils.setFontSize(editor, anEvent.getFloatValue(), false);
    
    // Handle SizeText
    if(anEvent.equals("SizeText"))
        EditorUtils.setFontSize(editor, anEvent.getFloatValue(), false);

    // Handle FamilyList, FamilyComboBox
    if(anEvent.equals("FamilyList") || (anEvent.equals("FamilyComboBox") && anEvent.isActionEvent())) {
        String familyName = getViewStringValue("FamilyList");
        String fontNames[] = Font.getFontNames(familyName); if(fontNames.length==0) return;
        String fontName = fontNames[0];
        Font font = Font.getFont(fontName, 12);
        EditorUtils.setFontFamily(editor, font);
    }
    
    // Handle AllButton, PDFButton
    if(anEvent.equals("AllButton")) _showAll = true;
    if(anEvent.equals("PDFButton")) _showAll = false;
    
    // Handle FontNameComboBox
    if(anEvent.equals("FontNameComboBox")) {
        Font font = Font.getFont(anEvent.getStringValue(), 12);
        EditorUtils.setFontName(editor, font);
    }
}

/** Returns the name for the inspector window. */
public String getWindowTitle()  { return "Font Panel"; }

}
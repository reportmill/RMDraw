/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.app;
import rmdraw.scene.*;

import java.util.*;

import snap.view.*;

/**
 * An editor for general View attributes, like property keys, name, text wrap around, etc.
 */
public class ViewGeneral extends EditorPane.SupportPane {

    // The bindings table
    TableView<String> _bindingsTable;

    /**
     * Creates a new ViewGeneral pane.
     */
    public ViewGeneral(EditorPane anEP)
    {
        super(anEP);
    }

    /**
     * Initialize UI panel for this inspector.
     */
    protected void initUI()
    {
        // Get bindings table
        _bindingsTable = getView("BindingsTable", TableView.class);
        _bindingsTable.setRowHeight(18);
        _bindingsTable.setCellConfigure(this::configureBindingsTable);
        enableEvents(_bindingsTable, MouseRelease, DragDrop);
    }

    /**
     * Updates UI controsl from current selection.
     */
    public void resetUI()
    {
        // Get currently selected view
        SGView view = getSelView();

        // Reset NameText, UrlText
        setViewValue("NameText", view.getName());
        setViewValue("UrlText", view.getURL());

        // Reset table model view
        _bindingsTable.setItems(view.getPropNames());
        if (_bindingsTable.getSelIndex() < 0) _bindingsTable.setSelIndex(0);
        _bindingsTable.updateItems();

        // Reset BindingsText
        String pname = _bindingsTable.getSelItem();
        Binding binding = view.getBinding(pname);
        setViewValue("BindingsText", binding != null ? binding.getKey() : null);
    }

    /**
     * Updates current selection from UI controls.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Get the current editor and selected view (just return if null) and selected views
        SGView view = getSelView();
        if (view == null) return;
        List<? extends SGView> views = getEditor().getSelOrSuperSelViews();

        // Handle NameText, UrlText
        if (anEvent.equals("NameText")) {
            String value = anEvent.getStringValue();
            for (SGView vue : views) vue.setName(value);
        }
        if (anEvent.equals("UrlText")) {
            String value = anEvent.getStringValue();
            for (SGView vue : views) vue.setURL(value);
        }

        // Handle BindingsTable
        if (anEvent.equals("BindingsTable")) {

            // Handle MouseRelease: Select text
            if (anEvent.isMouseRelease()) {
                requestFocus("BindingsText");
                getView("BindingsText", TextView.class).selectAll();
            }

            // Handle DragDrop
            if (anEvent.isDragDrop()) {
                Clipboard cb = anEvent.getClipboard();
                anEvent.acceptDrag();
                if (cb.hasString()) {
                    String bkey = cb.getString();
                    int row = _bindingsTable.getRowIndexForY(anEvent.getY());
                    if (row < 0) return;
                    String pname = view.getPropNames()[row];
                    view.addBinding(pname, bkey);
                }
                anEvent.dropComplete();
            }
        }

        // Handle BindingsText
        if (anEvent.equals("BindingsText")) {

            // Get selected PropertyName and Key
            String pname = _bindingsTable.getSelItem();
            if (pname == null) return;
            String key = getViewStringValue("BindingsText");
            if (key != null && key.length() == 0) key = null;

            // Remove previous binding and add new one (if valid)
            for (SGView vue : views)
                if (key != null) vue.addBinding(pname, key);
                else vue.removeBinding(pname);
        }
    }

    /**
     * Returns the current selected view for the current editor.
     */
    public SGView getSelView()
    {
        Editor e = getEditor();
        if (e == null) return null;
        return e.getSelOrSuperSelView();
    }

    /**
     * Called to configure BindingsTable.
     */
    private void configureBindingsTable(ListCell<String> aCell)
    {
        if (aCell.getCol() == 0) return;
        String pname = aCell.getItem();
        SGView view = getSelView();
        if (view == null) return;
        Binding binding = view.getBinding(pname);
        aCell.setText(binding != null ? binding.getKey() : null);
    }

    /**
     * Returns the name to be used in the inspector's window title.
     */
    public String getWindowTitle()
    {
        return "General Inspector";
    }
}
/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.apptools;
import rmdraw.scene.*;
import snap.gfx.Image;
import snap.view.*;
import snap.viewx.DialogBox;

/**
 * This class provides UI editing for RMPage shapes.
 */
public class RMPageTool <T extends SGPage> extends RMParentShapeTool <T> {
    
    // The Layers table
    TableView <SGPageLayer>  _layersTable;

    // Icons
    Image                    _visibleIcon = getImage("LayerVisible.png");
    Image                    _invisibleIcon = getImage("LayerInvisible.png");
    Image                    _lockedIcon = getImage("LayerLocked.png");

/**
 * Initialize UI panel for this tool.
 */
protected void initUI()
{
    // Configure LayersTable CellConfigure and MouseClicks
    _layersTable = getView("LayersTable", TableView.class);
    _layersTable.setCellConfigure(this :: configureLayersTable);
    enableEvents(_layersTable, MouseRelease);
    enableEvents("DatasetKeyText", DragDrop);
}

/**
 * Updates the UI controls from currently selected page.
 */
public void resetUI()
{
    // Get currently selected page (just return if null)
    SGPage page = getSelectedShape(); if(page==null) return;
    
    // Update DatasetKeyText, PaintBackCheckBox
    setViewValue("DatasetKeyText", page.getDatasetKey());
    setViewValue("PaintBackCheckBox", page.getPaintBackground());

    // Update AddButton, RemoveButton, RenameButton, MergeButton enabled state
    setViewEnabled("AddButton", page.getLayerCount()>0);
    setViewEnabled("RemoveButton", page.getLayerCount()>1);
    setViewEnabled("RenameButton", page.getLayerCount()>0);
    setViewEnabled("MergeButton", page.getLayerCount()>1 && page.getSelectedLayerIndex()>0);
    
    // Update layers table selection
    _layersTable.setItems(page.getLayers());
    _layersTable.setSelIndex(page.getSelectedLayerIndex());
}

/**
 * Updates currently selected page from the UI controls.
 */
public void respondUI(ViewEvent anEvent)
{
    // Get currently selected page (just return if null)
    SGPage page = getSelectedShape(); if(page==null) return;

    // Handle DatasetKeyText, PaintBackCheckBox
    if(anEvent.equals("DatasetKeyText"))
        page.setDatasetKey(anEvent.getStringValue().replace("@", ""));
    if(anEvent.equals("PaintBackCheckBox"))
        page.setPaintBackground(anEvent.getBoolValue());

    // Handle AddButton
    if(anEvent.equals("AddButton"))
        page.addLayerNamed("Layer " + (page.getLayerCount() + 1));

    // Handle RemoveButton
    if(anEvent.equals("RemoveButton")) {
        int index = getViewSelIndex("LayersTable");
        page.removeLayer(index);
    }

    // Handle MergeButton
    if(anEvent.equals("MergeButton")) {
        
        // Get selected layer and index
        SGPageLayer layer = page.getSelectedLayer();
        int index = page.getSelectedLayerIndex();
        
        // If index is less than layer count
        if(index<page.getLayerCount()) {
            SGPageLayer resultingLayer = page.getLayer(index - 1);
            resultingLayer.addChildren(layer.getChildren());
            layer.removeChildren();
            page.removeLayer(layer);
        }
    }
    
    // Handle RenameButton
    if(anEvent.equals("RenameButton")) {
        int srow = getViewSelIndex("LayersTable");
        SGPageLayer layer = page.getLayer(srow);
        DialogBox dbox = new DialogBox("Rename Layer"); dbox.setQuestionMessage("Layer Name:");
        String newName = dbox.showInputDialog(getUI(), layer.getName());
        if(newName!=null && newName.length()>0)
            layer.setName(newName);
    }
    
    // Handle LayersTable
    if(anEvent.equals("LayersTable")) {
        
        // Handle MouseClick event - have page select new table row
        int row = _layersTable.getSelIndex(); if(row<0) return;
        SGPageLayer layer = page.getLayer(row);
        page.selectLayer(layer);
        
        // If column one was selected, cycle through layer states
        int col = _layersTable.getSelCol();
        if(anEvent.isMouseClick() && col==1) {
            int state = layer.getLayerState();
            if(state== SGPageLayer.StateVisible) layer.setLayerState(SGPageLayer.StateInvisible);
            else if(state== SGPageLayer.StateInvisible) layer.setLayerState(SGPageLayer.StateLocked);
            else layer.setLayerState(SGPageLayer.StateVisible);
            _layersTable.updateItems();
        }
    }

    // Handle AllVisibleButton
    if(anEvent.equals("AllVisibleButton"))
        for(int i=0, iMax=page.getLayerCount(); i<iMax; i++)
            page.getLayer(i).setLayerState(SGPageLayer.StateVisible);
    
    // Handle AllVisibleButton
    if(anEvent.equals("AllInvisibleButton"))
        for(int i=0, iMax=page.getLayerCount(); i<iMax; i++)
            page.getLayer(i).setLayerState(SGPageLayer.StateInvisible);
    
    // Handle AllLockedButton
    if(anEvent.equals("AllLockedButton"))
        for(int i=0, iMax=page.getLayerCount(); i<iMax; i++)
            page.getLayer(i).setLayerState(SGPageLayer.StateLocked);
}

/**
 * Configure LayersTable: Set image for second column.
 */
public void configureLayersTable(ListCell <SGPageLayer> aCell)
{
    // Get page, cell row/col, page layer
    //RMPage page = getSelectedShape(); if(page==null) return;
    //int row = aCell.getRow(), col = aCell.getCol(); if(row<0 || row>=page.getLayerCount()) return;
    SGPageLayer layer = aCell.getItem(); if(layer==null) return; //page.getLayer(row);
    int col = aCell.getCol();

    // Handle column 0 (layer name)
    if(col==0) {
        aCell.setText(layer.getName());
    }
    
    // Handle column 1 (layer state image)
    if(col==1) {
        int state = layer!=null? layer.getLayerState() : -1;
        if(state== SGPageLayer.StateVisible) aCell.setImage(_visibleIcon);
        else if(state== SGPageLayer.StateInvisible) aCell.setImage(_invisibleIcon);
        else if(state== SGPageLayer.StateLocked) aCell.setImage(_lockedIcon);
        aCell.setText(null);
    }
}

/**
 * Returns the shape class that this tool is responsible for.
 */
public Class getShapeClass()  { return SGPage.class; }

/**
 * Returns the name to be used for this tool in the inspector window title.
 */
public String getWindowTitle()  { return "Page Inspector"; }

/**
 * Overrides tool method to declare that pages have no handles.
 */
public int getHandleCount(T aShape)  { return 0; }

}
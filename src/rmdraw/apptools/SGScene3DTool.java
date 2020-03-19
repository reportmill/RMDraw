/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.apptools;
import rmdraw.app.Tool;
import rmdraw.gfx3d.*;
import rmdraw.scene.*;
import snap.view.ViewEvent;

/**
 * Tool for visual editing RMScene3D.
 */
public class SGScene3DTool<T extends SGScene3D> extends Tool<T> {
    
    // The Trackball control for rotating selected scene3d
    Trackball  _trackball;
    
/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Get Trackball
    _trackball = getView("Trackball", Trackball.class);
    
    // Initialize RenderingComboBox
    setViewItems("RenderingComboBox", new String[] { "Real 3D", "Pseudo 3D" });
}

/**
 * Updates UI panel from currently selected scene3d.
 */
public void resetUI()
{
    // Get the selected scene
    SGScene3D scene = getSelView(); if(scene==null) return;
    
    // Reset Rendering radio buttons
    setViewSelIndex("RenderingComboBox", scene.isPseudo3D()? 1 : 0);
    
    // Reset YawSpinner, PitchSpinner, RollSpinner
    setViewValue("YawSpinner", Math.round(scene.getYaw()));
    setViewValue("PitchSpinner", Math.round(scene.getPitch()));
    setViewValue("RollSpinner", Math.round(scene.getRoll3D()));
    
    // Reset scene control
    _trackball.syncFrom(scene.getCamera());
    
    // Reset Depth slider/text
    setViewValue("DepthSlider", scene.getDepth());
    setViewValue("DepthText", scene.getDepth());
    
    // Reset Field of view slider/text
    setViewValue("FOVSlider", scene.getFocalLength()/72);
    setViewValue("FOVText", scene.getFocalLength()/72);
}

/**
 * Updates currently selected scene 3d from UI panel controls.
 */
public void respondUI(ViewEvent anEvent)
{
    // Get the currently selected scene3d
    SGScene3D scene = getSelView(); if(scene==null) return;
    
    // Handle RenderingComboBox
    if(anEvent.equals("RenderingComboBox"))
        setPseudo3D(scene.getCamera(), anEvent.getSelIndex()==1);
    
    // Handle YawSpinner, PitchSpinner, RollSpinner
    if(anEvent.equals("YawSpinner"))
        scene.setYaw(anEvent.getFloatValue());
    if(anEvent.equals("PitchSpinner"))
        scene.setPitch(anEvent.getFloatValue());
    if(anEvent.equals("RollSpinner"))
        scene.setRoll3D(anEvent.getFloatValue());

    // Handle Trackball
    if(anEvent.equals("Trackball"))
        _trackball.syncTo(scene.getCamera());
    
    // Handle DepthSlider and DepthText
    if(anEvent.equals("DepthSlider") || anEvent.equals("DepthText"))
        scene.setDepth(anEvent.equals("DepthSlider")? anEvent.getIntValue() : anEvent.getFloatValue());

    // Handle FOVSlider or FOVText
    if(anEvent.equals("FOVSlider") || anEvent.equals("FOVText"))
        scene.setFocalLength(anEvent.equals("FOVSlider")? anEvent.getIntValue()*72 : anEvent.getFloatValue()*72);
}

/**
 * Returns the class that this tool is responsible for.
 */
public Class getViewClass()  { return SGScene3D.class; }

/**
 * Returns the name of this tool for the inspector window.
 */
public String getWindowTitle()  { return "Scene3D Inspector"; }

/**
 * Overridden to make scene3d super-selectable.
 */
public boolean isSuperSelectable(SGView aShape)  { return true; }

/**
 * Overridden to make scene3d not ungroupable.
 */
public boolean isUngroupable(SGView aShape)  { return false; }

/**
 * Event handler for editing.
 */    
public void mousePressed(T aView, ViewEvent anEvent)
{
    // If shape isn't super selected, just return
    if(!isSuperSelected(aView)) return;
    
    // Forward mouse pressed to scene and consume event
    aView.processEvent(createSceneEvent(aView, anEvent));
    anEvent.consume();
}

/**
 * Event handler for editing.
 */
public void mouseDragged(T aView, ViewEvent anEvent)
{
    // Forward mouse pressed to scene and consume event
    aView.processEvent(createSceneEvent(aView, anEvent));
    anEvent.consume();
}

/**
 * Event handler for editing.
 */
public void mouseReleased(T aView, ViewEvent anEvent)
{
    // Forward mouse pressed to scene and consume event
    aView.processEvent(createSceneEvent(aView, anEvent));
    anEvent.consume();
}

/**
 * Sets Psuedo3D with some good settings.
 */
private void setPseudo3D(Camera aCam, boolean isPseudo3D)
{
    // Set defaults for pseudo 3d
    aCam.setPseudo3D(isPseudo3D);
    if(isPseudo3D) {
        aCam.setPseudoSkewX(.3f); aCam.setPseudoSkewY(-.25f); aCam.setDepth(20); aCam.setFocalLength(60*72); }
    
    // Set defaults for true 3d
    else { aCam.setYaw(23); aCam.setPitch(12); aCam.setDepth(100); aCam.setFocalLength(8*72); }    
}

}
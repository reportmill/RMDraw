/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.apptools;
import rmdraw.scene.*;

/**
 * Tool for RMSpringShape.
 */
public class RMSpringShapeTool <T extends SGSpringsView> extends RMParentShapeTool <T> {

/**
 * Override to return shape class.
 */
public Class <T> getViewClass()  { return (Class<T>) SGSpringsView.class; }

/**
 * Returns whether a given shape is super-selectable.
 */
public boolean isSuperSelectable(SGView aShape)  { return true; }

/**
 * Returns whether a given shape accepts children.
 */
public boolean getAcceptsChildren(SGView aShape)  { return true; }

}

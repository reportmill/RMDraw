/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.apptools;
import rmdraw.scene.*;

/**
 * Tool for SGSpringsView.
 */
public class SGSpringsTool<T extends SGSpringsView> extends SGParentTool<T> {

    /**
     * Override to return view class.
     */
    public Class <T> getViewClass()  { return (Class<T>) SGSpringsView.class; }

    /**
     * Returns whether a given view is super-selectable.
     */
    public boolean isSuperSelectable(SGView aView)  { return true; }

    /**
     * Returns whether a given view accepts children.
     */
    public boolean getAcceptsChildren(SGView aView)  { return true; }

}

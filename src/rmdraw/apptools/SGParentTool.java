/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.apptools;
import rmdraw.app.Tool;
import rmdraw.scene.*;

/**
 * A tool class for SGParent.
 */
public class SGParentTool<T extends SGParent> extends Tool<T> {

    /**
     * Override to return view class.
     */
    public Class<T> getViewClass()  { return (Class<T>) SGParent.class; }

    /**
     * Returns the string to be used for the inspector window title.
     */
    public String getWindowTitle()  { return "Group Inspector"; }

}
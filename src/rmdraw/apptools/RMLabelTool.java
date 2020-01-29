/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.apptools;
import rmdraw.shape.*;
import snap.view.*;

public class RMLabelTool <T extends RMLabel> extends RMTool <T> {

public Class getShapeClass()  { return RMLabel.class; }

public String getWindowTitle()  { return "Label Inspector"; }

public View createUI()  { return new Label(); }

/**
 * Overrides tool method to declare that labels have no handles.
 */
public int getHandleCount(T aShape)  { return 0; }

}
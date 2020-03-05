/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.gfx;
import java.util.*;
import snap.gfx.*;
import snap.view.ViewOwner;

/**
 * Provides a tool for editing Snap Effects.
 */
public class EffectTool extends ViewOwner {

    // The Styler used to get/set paint attributes
    private Styler _styler;

    // Map of tool instances by shape class
    private Map<Class,EffectTool>  _tools = new Hashtable();
    
    // List of known effects
    static Effect  _effects[] = { new ShadowEffect(), new ReflectEffect(), new BlurEffect(), new EmbossEffect() };
    
    /**
     * Creates EffectTool.
     */
    public EffectTool()
    {
        super();
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
     * Returns the number of known effects.
     */
    public int getEffectCount()  { return _effects.length; }

    /**
     * Returns an individual effect at given index.
     */
    public Effect getEffect(int anIndex)  { return _effects[anIndex]; }

    /**
     * Returns the currently selected shape's effect.
     */
    public Effect getEffect()
    {
        return getStyler().getEffect();
    }

    /**
     * Iterate over editor selected shapes and set fill.
     */
    public void setEffect(Effect anEffect)
    {
        Styler styler = getStyler();
        styler.setEffect(anEffect);
    }

    /**
     * Returns the specific tool for a given shape.
     */
    public EffectTool getTool(Object anObj)
    {
        // Get tool from tools map - just return if present
        Class cls = anObj instanceof Class? (Class)anObj : anObj.getClass();
        EffectTool tool = _tools.get(cls);
        if (tool==null) {
            _tools.put(cls, tool=getToolImpl(cls));
            tool.setStyler(getStyler());
        }
        return tool;
    }

    /**
     * Returns the specific tool for a given effect.
     */
    private static EffectTool getToolImpl(Class aClass)
    {
        if (aClass==ShadowEffect.class) return new ShadowEffectTool();
        if (aClass==ReflectEffect.class) return new ReflectEffectTool();
        if (aClass==BlurEffect.class) return new BlurEffectTool();
        if (aClass==EmbossEffect.class) return new EmbossEffectTool();
        System.err.println("EffectTool.getToolImpl: Can't find tool for: " + aClass);
        return new EffectTool();
    }
}
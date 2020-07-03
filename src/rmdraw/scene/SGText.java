/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.scene;
import java.util.*;
import java.util.List;
import snap.geom.*;
import snap.gfx.*;
import snap.text.*;
import snap.util.*;

/**
 * This class is an SGView subclass for handling rich text. Text is probably the most common and useful element in a
 * ReportMill template. You might use this class to programmatically build or modify a template, like this:
 * <p><blockquote><pre>
 *   RichText rtext = new RichText("Hello world!", RMFont.getFont("Arial", 12), RMColor.red);
 *   RMText text = new RMText(rtext);
 *   template.getPage(0).addChild(text);
 *   text.setXY(36, 36);
 *   text.setSizeToFit();
 * </pre></blockquote>
 */
public class SGText extends SGRect {

    // The RichText to be displayed
    private RichText _rtext;

    // The text margin (if different than default)
    private Insets _margin = getMarginDefault();
    
    // Vertical alignment of text
    private VPos _alignY = VPos.TOP;
    
    // Specifies how text should handle overflow during RPG (ignore it, shrink it or paginate it)
    private byte  _wraps;
    
    // Whether to fit text on layout
    private boolean  _fitText;
    
    // Whether text should wrap around other views that cause wrap
    private boolean  _performsWrap = false;

    // Whether text should eliminate empty lines during RPG
    private boolean  _coalesceNewlines;
    
    // Whether text should draw box around itself even if there's no stroke
    private boolean _drawSelRect;
    
    // PDF option: Whether text box is editable in PDF
    private boolean  _editable;
    
    // PDF option: Whether text is multiline when editable in PDF
    private boolean  _multiline;

    // The linked text view for rendering overflow, if there is one
    private SGLinkedText _linkedText;
    
    // A text box to manage RichText in view bounds
    private TextBox  _textBox;

    // The view that provides the path for this text to wrap text to
    private SGView _pathView;

    // The default text margin (top=1, left=2, bottom=0, right=2)
    static Insets          _marginDefault = new Insets(1, 2, 0, 2);
    
    // A listener to handle rich text changes
    PropChangeListener     _richTextLsnr = pc -> richTextDidPropChange(pc);
    
    // Constants for overflow behavior during RPG
    public static final byte WRAP_NONE = 0;
    public static final byte WRAP_BASIC = 1;
    public static final byte WRAP_SCALE = 2;
    
    // Constants for properties
    public static final String Editable_Prop = "Editable";
    public static final String Multiline_Prop = "Multiline";
    
    /**
     * Creates an empty text instance.
     */
    public SGText() { }

    /**
     * Creates a text instance initialized with given RichText.
     */
    public SGText(RichText aRichText)  { setRichText(aRichText); }

    /**
     * Creates a text instance initialized with the given plain text String.
     */
    public SGText(String plainText)  { getRichText().addChars(plainText); }

    /**
     * Returns the RichText.
     */
    public RichText getRichText()
    {
        if(_rtext!=null) return _rtext;
        _rtext = new RichText();
        _rtext.addPropChangeListener(_richTextLsnr);
        return _rtext;
    }

    /**
     * Sets the RichText associated with this RMText.
     */
    public void setRichText(RichText aRT)
    {
        // If value already set, just return
        if(aRT==_rtext) return;

        // Stop listening to last RichText and start listening to new RichText
        if(_rtext!=null) _rtext.removePropChangeListener(_richTextLsnr);
        if(aRT!=null) aRT.addPropChangeListener(_richTextLsnr);

        // Set value and fire property change, and reset cached HeightToFit
        firePropChange("XString", _rtext, _rtext = aRT);
        _textBox = null;
        revalidate(); repaint();
    }

    /**
     * Returns the length, in characters, of the XString associated with this RMText.
     */
    public int length()  { return getRichText().length(); }

    /**
     * Returns the text associated with this RMText as a plain String.
     */
    public String getText()  { return getRichText().getString(); }

    /**
     * Replaces the current text associated with this RMText with the given String.
     */
    public void setText(String aString)  { getRichText().replaceChars(aString, 0, length()); }

    /**
     * Returns the first character index visible in this text.
     */
    public int getVisibleStart()  { return 0; }

    /**
     * Returns the last character index visible in this text.
     */
    public int getVisibleEnd()  { return getTextBox().getEnd(); }

    /**
     * Returns whether all characters can be visibly rendered in text bounds.
     */
    public boolean isAllTextVisible()  { return !getTextBox().isTextOutOfBounds(); }

    /**
     * Returns the font for char 0.
     */
    public Font getFont()
    {
        return getRichText().getFontAt(0);
    }

    /**
     * Sets the font for all characters.
     */
    public void setFont(Font aFont)
    {
        getRichText().setStyleValue(aFont);
    }

    /**
     * Returns the format for char 0.
     */
    public TextFormat getFormat()
    {
        return getRichText().getRunAt(0).getFormat();
    }

    /**
     * Sets the format for all characters.
     */
    public void setFormat(TextFormat aFormat)
    {
        getRichText().setStyleValue(aFormat);
    }

    /**
     * Returns the color of the first character of the xstring associated with this RMText.
     */
    public Color getTextColor()  { return getRichText().getRunAt(0).getColor(); }

    /**
     * Sets the color of the characters in the XString associated with this RMText.
     */
    public void setTextColor(Color aColor)
    {
        getRichText().setStyleValue(aColor);
    }

    /**
     * Returns the border for char 0.
     */
    public Border getTextBorder()
    {
        return getRichText().getRunAt(0).getBorder();
    }

    /**
     * Sets the border for all characters.
     */
    public void setTextBorder(Border aBorder)
    {
        getRichText().setStyleValue(TextStyle.BORDER_KEY,aBorder,0,length());
    }

    /**
     * Returns the alignment for char 0.
     */
    public HPos getAlignX()
    {
        return getRichText().getLineStyleAt(0).getAlign();
    }

    /**
     * Sets the align for all chars.
     */
    public void setAlignX(HPos anAlignX)
    {
        getRichText().setAlignX(anAlignX);
    }

    /**
     * Returns the vertical alignment.
     */
    public VPos getAlignY()  { return _alignY; }

    /**
     * Sets the vertical alignment.
     */
    public void setAlignY(VPos anAlignY)
    {
        firePropChange("AlignmentY", _alignY, _alignY = anAlignY);
        revalidate(); repaint();
    }

    /**
     * Returns the wrapping behavior for over-filled rpgCloned text (NONE, WRAP, SHRINK).
     */
    public byte getWraps()  { return _wraps; }

    /**
     * Sets the wrapping behavior for over-filled rpgCloned text (NONE, WRAP, SHRINK).
     */
    public void setWraps(byte aValue)  { _wraps = aValue; }

    /**
     * Returns whether text should wrap around other views that cause wrap.
     */
    public boolean getPerformsWrap()  { return _performsWrap; }

    /**
     * Sets whether text should wrap around other views that cause wrap.
     */
    public void setPerformsWrap(boolean aFlag)  { _performsWrap = aFlag; }

    /**
     * Returns whether text should coalesce consecutive newlines in rpgClone.
     */
    public boolean getCoalesceNewlines()  { return _coalesceNewlines; }

    /**
     * Sets whether text should coalesce consecutive newlines in rpgClone.
     */
    public void setCoalesceNewlines(boolean aFlag)  { _coalesceNewlines = aFlag; }

    /**
     * Returns whether text should always draw at least a light gray border (useful when editing).
     */
    public boolean getDrawsSelectionRect()  { return _drawSelRect; }

    /**
     * Sets whether text should always draw at least a light-gray border (useful when editing).
     */
    public void setDrawsSelectionRect(boolean aValue)  { _drawSelRect = aValue; }

    /**
     * Returns whether text box is editable in PDF.
     */
    public boolean isEditable()  { return _editable; }

    /**
     * Sets whether text box is editable in PDF.
     */
    public void setEditable(boolean aValue)
    {
        firePropChange(Editable_Prop, _editable, _editable = aValue);
    }

    /**
     * Returns whether text is multiline when editable in PDF.
     */
    public boolean isMultiline()  { return _multiline; }

    /**
     * Sets whether text is multiline when editable in PDF.
     */
    public void setMultiline(boolean aValue)
    {
        firePropChange(Multiline_Prop, _multiline, _multiline = aValue);
    }

    /**
     * Returns margin.
     */
    public Insets getMargin()  { return _margin; }

    /**
     * Sets margin.
     */
    public void setMargin(Insets aMargin)
    {
        if(_margin.equals(aMargin)) return;
        firePropChange("Margin", _margin, _margin = aMargin);
        revalidate(); repaint();
    }

    /**
     * Returns the default margin of the text (top=1, left=2, right=2, bottom=0).
     */
    public Insets getMarginDefault()  { return _marginDefault; }

    /**
     * Returns the margin as a string.
     */
    public String getMarginString()
    {
        return getMarginTop() + ", " + getMarginLeft() + ", " + getMarginBottom() + ", " + getMarginRight();
    }

    /**
     * Sets the margin as a string.
     */
    public void setMarginString(String aString)
    {
        // If given string is empty, set default margins
        if(aString==null || aString.trim().length()==0) { setMargin(getMarginDefault()); return; }

        // Split the string by commas or spaces and get the parts
        String parts[] = aString.indexOf(",")>0? aString.split(",") : aString.split(" ");
        String p1 = parts[0];
        String p2 = parts[Math.min(1, parts.length-1)];
        String p3 = parts[Math.min(2, parts.length-1)];
        String p4 = parts[Math.min(3, parts.length-1)];

        // Set margin from parts
        setMargin(new Insets(SnapUtils.intValue(p1),SnapUtils.intValue(p4),SnapUtils.intValue(p3),SnapUtils.intValue(p2)));
    }

    /**
     * Returns the left margin of the text (default to 2).
     */
    public int getMarginLeft()  { return (int)Math.round(getMargin().getLeft()); }

    /**
     * Returns the right margin of the text (defaults to 2).
     */
    public int getMarginRight()  { return (int)Math.round(getMargin().getRight()); }

    /**
     * Returns the top margin of the text (defaults to 1).
     */
    public int getMarginTop()  { return (int)Math.round(getMargin().getTop()); }

    /**
     * Returns the bottom margin of the text (defaults to 0).
     */
    public int getMarginBottom()  { return (int)Math.round(getMargin().getBottom()); }

    /**
     * Override to revalidate.
     */
    public void setWidth(double aValue)  { super.setWidth(aValue); revalidate(); }

    /**
     * Override to revalidate.
     */
    public void setHeight(double aValue)  { super.setHeight(aValue); revalidate(); }

    /**
     * Overrides to get clip path.
     */
    public Shape getPath()
    {
        // If text doesn't perform wrap or parent is null, return normal path in bounds
        if(!getPerformsWrap() || getParent()==null)
            return getPathView()!=null? getPathView().getPath().copyFor(getBoundsLocal()) : super.getPath();

        // Get peers who cause wrap (if none, just return super path in bounds)
        List peersWhoCauseWrap = getPeersWhoCauseWrap();
        if(peersWhoCauseWrap==null)
            return getPathView()!=null? getPathView().getPath().copyFor(getBoundsLocal()) : super.getPath();

        // Add this text to list
        peersWhoCauseWrap.add(0, this);

        // Get the path minus the neighbors, convert back to this view, reset bounds to this view
        _performsWrap = false;
        Shape path = SGViewUtils.getSubtractedPath(peersWhoCauseWrap, -3);  // INSET NAILED TO -3
        _performsWrap = true;
        path = parentToLocal(path);
        path = path.copyFor(getBoundsLocal());
        return path;
    }

    /**
     * Returns the subset of children that cause wrap.
     */
    private List <SGView> getPeersWhoCauseWrap()
    {
        // Iterate over children and add any that intersect frame
        List list = null;
        for (int i=0, iMax=getParent().getChildCount(); i<iMax; i++) { SGView child = getParent().getChild(i);
            if (child!=this && child.getFrame().intersects(getFrame())) {
                if (list==null)
                    list = new ArrayList(); list.add(child); }
        }
        return list;
    }

    /**
     * This notification method is called when any peer is changed.
     */
    public void peerDidChange(SGView aView)
    {
        // If this text respects neighbors and view intersects it, register for redraw
        if (getPerformsWrap() && aView.getFrame().intersectsRect(getFrame())) {
            revalidate(); repaint(); }
    }

    /**
     * Returns the view that provides the path for this text to wrap text to.
     */
    public SGView getPathView()  { return _pathView; }

    /**
     * Sets the view that provides the path for this text to wrap text to.
     */
    public void setPathView(SGView aView)
    {
        if (SnapUtils.equals(aView, _pathView)) return;
        firePropChange("PathView", _pathView, _pathView = aView);
        revalidate(); repaint();
    }

    /**
     * Overrides rectangle implementation to potentially clear path view.
     */
    public void setRadius(float aValue)  { super.setRadius(aValue); setPathView(null); }

    /**
     * Returns the linked text for this text (if any).
     */
    public SGLinkedText getLinkedText()  { return _linkedText; }

    /**
     * Sets the linked text for this text (if any).
     */
    public void setLinkedText(SGLinkedText aLinkedText)
    {
        // Set linked text, and if non-null, set its previous text to this text
        _linkedText = aLinkedText;
        if(_linkedText!=null)
            _linkedText.setPreviousText(this);
        revalidate(); repaint();
    }

    /**
     * Returns a text layout.
     */
    public TextBox getTextBox()
    {
        // If already set, just return
        if (_textBox!=null) return _textBox;

        // Create, configure, update and return
        _textBox = new TextBox();
        _textBox.setWrapLines(true);
        updateTextBox();
        return _textBox;
    }

    /**
     * Updates the text box.
     */
    protected void updateTextBox()
    {
        // Update RichText
        _textBox.setRichText(getRichText());

        // Update bounds
        Insets pad = getMargin();
        double pl = pad.left, pr = pad.right, pt = pad.top, pb = pad.bottom;
        double w = getWidth() - pl - pr; if (w<0) w = 0;
        double h = getHeight() - pt - pb; if (h<0) h = 0;
        _textBox.setBounds(pl, pt, w, h);

        // Update Start, Linked, Align
        _textBox.setStart(getVisibleStart());
        _textBox.setLinked(getLinkedText()!=null);
        _textBox.setAlignY(getAlignY());
        _textBox.setBoundsPath(!(getPath() instanceof Rect) || getPerformsWrap()? getPath() : null);
        _textBox.setHyphenate(TextEditor.isHyphenating());

        // Update FontScale, ScaleTextToFit
        _textBox.setFontScale(1);
        if (_fitText) _textBox.scaleTextToFit();
    }

    /**
     * Returns whether to fit text to box.
     */
    public boolean isFitText()  { return _fitText; }

    /**
     * Sets whether to fit text to box.
     */
    public void setFitText(boolean aValue)
    {
        _fitText = aValue;
    }

    /**
     * Override to compute from RMTextLayout.
     */
    protected double getPrefWidthImpl(double aHeight)
    {
        // If font scaling, return current size
        if(_wraps==WRAP_SCALE) return getWidth();
        if(length()==0) return 0; // Zero instead of getMarginLeft() + getMarginRight() so empty texts are hidden

        // Get text box width (from first visible char) and return that plus margin
        double pw = getRichText().getPrefWidth(getVisibleStart());
        return Math.ceil(getMarginLeft() + pw + getMarginRight());
    }

    /**
     * Override to compute from RMTextLayout.
     */
    protected double getPrefHeightImpl(double aWidth)
    {
        if(_wraps==WRAP_SCALE) return getHeight();
        if(length()==0) return 0; // Zero instead of getMarginTop()+getMarginBottom() so empty texts are hidden
        double ph = getTextBox().getPrefHeight(getTextBox().getWidth());
        return Math.ceil(getMarginTop() + ph + getMarginBottom());
    }

    /**
     * Creates a view suitable for the "remainder" portion of a divideView call (just a clone by default).
     */
    protected SGView createDivideViewRemainder(byte anEdge)  { return anEdge==0? new SGLinkedText(this) : clone(); }

    /** Editor method - indicates that this view can be super selected. */
    public boolean superSelectable()  { return true; }

    /** Editor method. */
    public boolean isStructured()  { return false; }

    /**
     * Paints a text.
     */
    protected void paintView(Painter aPntr)
    {
        // Paint normal background
        super.paintView(aPntr);

        // If SuperSelected, just return (TextTool will do painting instead)
        if(SceneGraph.isSuperSelected(this))
            return;

        // Clip to view bounds (cache clip)
        aPntr.save();
        aPntr.clip(getBoundsLocal());

        // Paint TextBox
        getTextBox().paint(aPntr);

        // Restore
        aPntr.restore();
    }

    /**
     * Override to catch XString and TextEditor changes.
     */
    protected void richTextDidPropChange(PropChange aPC)
    {
        _pcs.fireDeepChange(this, aPC);
        repaint();
    }

    /**
     * Override to do home-brew layout.
     */
    public void revalidate()
    {
        if(_textBox!=null) updateTextBox();
        if(getLinkedText()!=null) { getLinkedText().revalidate(); getLinkedText().repaint(); }
    }

    /**
     * Standard clone implementation.
     */
    public SGText clone()
    {
        // Get normal view clone, clone XString, clear layout and return
        SGText clone = (SGText)super.clone();
        clone._rtext = null; clone._textBox = null;
        clone._richTextLsnr = pc -> richTextDidPropChange(pc);
        if (_rtext!=null) clone.setRichText(_rtext.clone());
        return clone;
    }

    /**
     * Override to support margin copy.
     */
    public void copyView(SGView aView)
    {
        super.copyView(aView);
        SGText other = aView instanceof SGText ? (SGText) aView : null; if (other==null) return;
        setMargin(other.getMargin());
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic attributes and reset element name to text
        XMLElement e = super.toXML(anArchiver); e.setName("text");

        // Archive Margin, AlignmentY
        if (getMargin()!=getMarginDefault()) e.add("margin", getMarginString());
        if (_alignY!=VPos.TOP) e.add("valign", getAlignY().toString().toLowerCase());

        // Archive Wraps, PerformsWrap
        if (_wraps!=0) e.add("wrap", _wraps==WRAP_BASIC? "wrap" : "shrink");
        if (_performsWrap) e.add("WrapAround", true);

        // Archive CoalesceNewlines, DrawsSelectionRect
        if (_coalesceNewlines) e.add("coalesce-newlines", true);
        if (_drawSelRect) e.add("draw-border", true);

        // Archive xstring
        if (!(this instanceof SGLinkedText)) {

            // Get the xml element for the RichText
            XMLElement xse = anArchiver.toXML(getRichText());

            // Add individual child elements to this text's xml element
            for(int i=0, iMax=xse.size(); i<iMax; i++)
                e.add(xse.get(i));
        }

        // If linked text present, archive reference to it (it should be archived as normal part of view hierarchy)
        if(getLinkedText()!=null)
            e.add("linked-text", anArchiver.getReference(getLinkedText()));

        // If there is a path view, archive path view
        if (getPathView()!=null) {

            // Get path view and an element (and add element to master element)
            SGView pathView = getPathView();
            XMLElement pathViewElement = new XMLElement("path-shape");
            e.add(pathViewElement);

            // Archive path view to path-shape element
            XMLElement pathViewElementZero = anArchiver.toXML(pathView);
            pathViewElement.add(pathViewElementZero);
        }

        // Archive PDF options
        if (isEditable()) e.add(Editable_Prop, true);
        if (isMultiline()) e.add(Multiline_Prop, true);

        // Return element for this view
        return e;
    }

    /**
     * XML unarchival.
     */
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic attributes
        super.fromXML(anArchiver, anElement);

        // Unarchive Margin, AlignmentY
        if (anElement.hasAttribute("margin"))
            setMarginString(anElement.getAttributeValue("margin"));
        if (anElement.hasAttribute("valign"))
            setAlignY(VPos.get(anElement.getAttributeValue("valign")));

        // Unarchive Wraps, PerformsWrap
        String wrap = anElement.getAttributeValue("wrap", "none");
        if (wrap.equals("wrap")) setWraps(WRAP_BASIC); else if(wrap.equals("shrink")) setWraps(WRAP_SCALE);
        setPerformsWrap(anElement.getAttributeBoolValue("WrapAround"));

        // Unarchive CoalesceNewlines, DrawsSelectionRect
        setCoalesceNewlines(anElement.getAttributeBoolValue("coalesce-newlines"));
        if (anElement.getAttributeBoolValue("draw-border")) setDrawsSelectionRect(true);

        // Unarchive RichText
        if (!(this instanceof SGLinkedText))
            getRichText().fromXML(anArchiver, anElement);

        // Register for finish call
        anArchiver.getReference(anElement);

        // Unarchive path-shape if present
        if (anElement.get("path-shape")!=null) {

            // Get the dedicated path-shape element and its first child (the actual path-shape element)
            XMLElement pathViewEmt = anElement.get("path-shape");
            XMLElement pathViewEmt0 = pathViewEmt.get(0);

            // Unarchive the path view and set
            SGView pathView = (SGView)anArchiver.fromXML(pathViewEmt0, null);
            setPathView(pathView);
        }

        // Unarchive PDF options
        if (anElement.hasAttribute(Editable_Prop)) setEditable(anElement.getAttributeBoolValue(Editable_Prop));
        if (anElement.hasAttribute(Multiline_Prop)) setMultiline(anElement.getAttributeBoolValue(Multiline_Prop));

        // Return this view
        return this;
    }

    /**
     * XML reference unarchival - to unarchive linked text.
     */
    public void fromXMLFinish(XMLArchiver anArchiver, XMLElement anElement)
    {
        // If linked-text, get referenced linked text and set
        if (!anElement.hasAttribute("linked-text")) return;
        SGLinkedText linkedText = (SGLinkedText)anArchiver.getReference("linked-text", anElement);
        setLinkedText(linkedText);
    }

    /**
     * Standard toSring implementation.
     */
    public String toString()
    {
        String string = super.toString();
        string = string.substring(0, string.length() - 1);
        return string + ", \"" + getRichText() + "\"]";
    }
}
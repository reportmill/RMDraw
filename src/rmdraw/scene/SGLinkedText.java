/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.scene;
import snap.gfx.Font;
import snap.text.RichText;
import snap.util.*;

/**
 * This class is a shape used to render text that didn't fit in a referenced text shape.
 */
public class SGLinkedText extends SGText {

    // Points to previous text
    SGText _previousText;

    /**
     * Creates a new linked text.
     */
    public SGLinkedText()
    {
    }

    /**
     * Creates a new overfloat text for the given text shape.
     */
    public SGLinkedText(SGText aText)
    {
        // Copy basic attributes of previous text and set linked text
        copyView(aText);
        aText.setLinkedText(this);
    }

    /**
     * Returns the text that this text is linked from.
     */
    public SGText getPreviousText()
    {
        return _previousText;
    }

    /**
     * Sets the text that this text is linked from.
     */
    public void setPreviousText(SGText aText)
    {
        _previousText = aText;
    }

    /**
     * Returns the same xstring as previoust text.
     */
    @Override
    public RichText getRichText()
    {
        return getPreviousText().getRichText();
    }

    /**
     * Returns the font for char 0 of the start text.
     */
    public Font getFont()
    {
        return getPreviousText().getFont();
    }

    /**
     * Overrides text implementation to return index where previous text left off.
     */
    public int getVisibleStart()
    {
        return getPreviousText() != null ? getPreviousText().getVisibleEnd() : 0;
    }

    /**
     * Overrides shape method to rewire linked text linked list.
     */
    public void setParent(SGParent aShape)
    {
        // Do normal set parent
        super.setParent(aShape);

        // If removing from share hierarchy, rewire text chain
        if (aShape == null) {
            _previousText.setLinkedText(getLinkedText());
            _previousText.repaint();
        }
    }

    /**
     * XML archival.
     */
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic shape attributes and reset element name
        XMLElement e = super.toXML(anArchiver);
        e.setName("linked-text");

        // Add xref id (someday this may happen automatically, just by having source text reference us)
        e.add("xref", anArchiver.getReference(this, true));

        // Return element
        return e;
    }
}
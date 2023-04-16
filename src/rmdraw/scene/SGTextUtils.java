/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.scene;
import snap.geom.Path2D;
import snap.geom.Rect;
import snap.geom.Shape;
import snap.gfx.*;
import snap.text.RichText;
import snap.text.TextBox;
import snap.text.TextBoxLine;
import snap.text.TextBoxRun;

/**
 * Utility methods for some esoteric text functionality.
 */
public class SGTextUtils {

    /**
     * Returns a path for all text chars.
     */
    public static Shape getTextPath(SGText aText)
    {
        // Create path and establish bounds of text
        Path2D path = new Path2D();
        path.moveTo(0, 0);
        path.moveTo(aText.getWidth(), aText.getHeight());

        // Iterate over text runs
        TextBox tbox = aText.getTextBox();
        for (TextBoxLine line : tbox.getLines())
            for (TextBoxRun run : line.getRuns()) { //if(run.length()==0 || run.isTab()) continue;
                String str = run.getString();
                double cspace = run.getStyle().getCharSpacing();
                path.appendShape(run.getFont().getOutline(str, run.getX(), line.getBaseline(), cspace));
            }

        // Return path
        return path;
    }

    /**
     * Returns an SGPolygon with the glyph path for the chars in this text. Assumes all runs have same visual attrs.
     */
    public static SGPolygon getTextPathView(SGText aText)
    {
        // Create polygon for text path with attributes from text shape
        SGPolygon polygon = new SGPolygon(getTextPath(aText));
        polygon.copyView(aText);

        // Set polygon color to run or outline color and stroke and return
        polygon.setFillColor(aText.getTextColor());
        polygon.setBorder(aText.getTextBorder());
        return polygon;
    }

    /**
     * Returns a group shape with a text shape for each individual character in this text shape.
     */
    public static SGView getTextCharsView(SGText aText)
    {
        // Get view for chars
        SGParent charsView = new SGSpringsView();
        charsView.copyView(aText);

        // Iterate over runs
        TextBox tbox = aText.getTextBox();
        for (TextBoxLine line : tbox.getLines())
            for (TextBoxRun run : line.getRuns()) { //if(run.length()==0 || run.isTab()) continue;

                // Get run font and run bounds
                Font font = run.getFont();
                Rect runBounds = new Rect(run.getX(), line.getY(), 0, line.getHeight()); // run y/height instead?

                // Iterate over run chars
                for (int i = 0, iMax = run.length(); i < iMax; i++) {
                    char c = run.charAt(i);

                    // Get char advance (just continue if zero)
                    double advance = font.charAdvance(c);
                    if (advance <= 0) continue;

                    // If non-space character, create glyph view
                    if (c != ' ') {
                        Rect glyphBounds = font.getCharBounds(c);
                        RichText rtext = aText.getRichText().copyForRange(run.getStart() + i, run.getStart() + i + 1);
                        SGText glyph = new SGText(rtext);
                        glyph.setAutosizing("~-~,~-~");

                        charsView.addChild(glyph);
                        runBounds.width = Math.ceil(Math.max(advance, glyphBounds.getMaxX()));
                        glyph.setFrame(getBoundsFromTextBounds(aText, runBounds));
                    }

                    // Increase bounds by advance
                    runBounds.x += advance;
                }
            }

        // Return chars view
        return charsView;
    }

    /**
     * Returns bounds from given text bounds, adjusted to account for text margins.
     */
    private static Rect getBoundsFromTextBounds(SGText aText, Rect aRect)
    {
        double rx = aRect.getX(), ry = aRect.getY(), rw = aRect.getWidth(), rh = aRect.getHeight();
        rx -= aText.getMarginLeft();
        rw += (aText.getMarginLeft() + aText.getMarginRight());
        ry -= aText.getMarginTop();
        rh += (aText.getMarginTop() + aText.getMarginBottom());
        return new Rect(rx, ry, rw, rh);
    }
}
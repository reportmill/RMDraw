package rmdraw.apptools;

import rmdraw.app.Editor;
import rmdraw.scene.*;
import snap.geom.Insets;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.Font;

/**
 * A class to hold misc TextTool methods.
 */
public class TextToolUtils {

    /**
     * Returns a rect suitable for the default bounds of a given text at a given point. This takes into account the font
     * and margins of the given text.
     */
    public static Rect getDefaultBounds(SGText aText, Point aPoint)
    {
        // Get text font (or default font, if not available) and margin
        Font font = aText.getFont();
        if(font==null) font = Font.getDefaultFont();
        Insets margin = aText.getMargin();

        // Default width is a standard char plus margin, default height is font line height plus margin
        double w = Math.round(font.charAdvance('x') + margin.getWidth());
        double h = Math.round(font.getLineHeight() + margin.getHeight());

        // Get bounds x/y from given (cursor) point and size
        double x = Math.round(aPoint.x - w/2) + 1;
        double y = Math.round(aPoint.y - h/2) - 1;

        // Return integral bounds rect
        Rect rect = new Rect(x, y, w, h); rect.snap();
        return rect;
    }

    /**
     * Returns whether text tool should convert to text.
     */
    public static boolean shouldConvertToText(SGView aShape)
    {
        if(aShape.isLocked()) return false;
        return aShape.getClass()== SGRect.class || aShape instanceof SGOval ||
                aShape instanceof SGPolygon;
    }

    /**
     * Converts a shape to a text shape.
     */
    public static void convertToText(Editor anEditor, SGView aShape, String aString)
    {
        // If shape is null, just return
        if(aShape==null) return;

        // Get text shape for given shape (if given shape is text, just use it)
        SGText text = aShape instanceof SGText ? (SGText)aShape : new SGText();

        // Copy attributes of given shape
        if(text!=aShape)
            text.copyView(aShape);

        // Copy path of given shape
        if(text!=aShape)
            text.setPathView(aShape);

        // Swap this shape in for original
        if(text!=aShape) {
            aShape.getParent().addChild(text, aShape.indexOf());
            aShape.getParent().removeChild(aShape);
        }

        // Install a bogus string for testing
        if(aString!=null && aString.equals("test"))
            aString = TextToolUtils.getTestString();

        // If aString is non-null, install in text
        if(aString!=null)
            text.setText(aString);

        // Select new shape
        anEditor.setSuperSelView(text);
    }

    /**
     * Returns a test string.
     */
    public static String getTestString()
    {
        return "Leo vitae diam est luctus, ornare massa mauris urna, vitae sodales et ut facilisis dignissim, " +
                "imperdiet in diam, quis que ad ipiscing nec posuere feugiat ante velit. Viva mus leo quisque. Neque mi vitae, " +
                "nulla cras diam fusce lacus, nibh pellentesque libero. " +
                "Dolor at venenatis in, ac in quam purus diam mauris massa, dolor leo vehicula at commodo. Turpis condimentum " +
                "varius aliquet accumsan, sit nullam eget in turpis augue, vel tristique, fusce metus id consequat orci " +
                "penatibus. Ipsum vehicula euismod aliquet, pharetra. " +
                "Fusce lectus proin, neque cr as eget, integer quam facilisi a adipiscing posuere. Imper diet sem sapien. " +
                "Pretium natoque nibh, tristique odio eligendi odio molestie mas sa. Volutpat justo fringilla rut rum augue. " +
                "Lao reet ulla mcorper molestie.";
    }

}

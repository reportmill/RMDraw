package rmdraw.scene;
import snap.geom.Rect;
import snap.gfx.*;
import snap.util.XMLArchiver;
import snap.util.XMLElement;

/**
 * A class to hold stubs for legacy unarchival.
 */
public class RMArchivers {

    /**
     * An RMFill stub to unarchive legacy RMFill.
     */
    public static class RMFillStub implements Paint, XMLArchiver.Archivable {

        /**
         * Bogus paint methods.
         */
        public boolean isAbsolute()
        {
            return false;
        }

        public boolean isOpaque()
        {
            return false;
        }

        public Paint copyForRect(Rect aRect)
        {
            return null;
        }

        public Color getColor()
        {
            return null;
        }

        public Paint copyForColor(Color aColor)
        {
            return null;
        }

        /**
         * Implement toXML for interface.
         */
        public XMLElement toXML(XMLArchiver anArchive)
        {
            return null;
        }

        /**
         * XML unarchival.
         */
        public Paint fromXML(XMLArchiver anArchiver, XMLElement anElement)
        {
            String colorStr = anElement.getAttributeValue("color");
            Color color = colorStr != null ? new Color(colorStr) : Color.BLACK;
            return color;
        }
    }

    /**
     * An RMImageFill stub to unarchive legacy RMImageFill.
     */
    public static class RMImageFillStub extends RMFillStub {

        /**
         * XML unarchival.
         */
        public Paint fromXML(XMLArchiver anArchiver, XMLElement anElement)
        {
            // Unarchive ImageName: get resource bytes, page and set ImageRef
            Image img = null;
            String iname = anElement.getAttributeValue("resource");
            if (iname != null) {
                byte bytes[] = anArchiver.getResource(iname);
                img = Image.get(bytes);
            }
            if (img == null)
                img = ImageUtils.getEmptyImage();

            // Unarchive Tile, legacy FillStyle (Stretch=0, Tile=1, Fit=2, FitIfNeeded=3)
            boolean tiled = anElement.getAttributeBooleanValue("Tile", false);
            if (anElement.hasAttribute("fillstyle")) tiled = anElement.getAttributeIntValue("fillstyle") == 1;

            // Unarchive X, Y
            double x = 0, y = 0;
            if (anElement.hasAttribute("x")) x = anElement.getAttributeFloatValue("x");
            if (anElement.hasAttribute("y")) y = anElement.getAttributeFloatValue("y");

            // Unarchive ScaleX, ScaleY
            double sx = anElement.getAttributeFloatValue("scale-x", 1);
            double sy = anElement.getAttributeFloatValue("scale-y", 1);

            // Create ImagePaint and return
            ImagePaint paint;
            if (tiled)
                paint = new ImagePaint(img, new Rect(x, y, img.getWidth() * sx, img.getHeight() * sx), true);
            else paint = new ImagePaint(img, new Rect(0, 0, sx, sy), false);
            return paint;
        }
    }

    /**
     * A Border subclass that paints a border for a stroke and color.
     */
    public static class RMStrokeStub extends Borders.LineBorder {

        /**
         * XML archival.
         */
        public XMLElement toXML(XMLArchiver anArchiver)
        {
            return null;
        }

        /**
         * XML unarchival.
         */
        public Borders.LineBorder fromXML(XMLArchiver anArchiver, XMLElement anElement)
        {
            // Unarchive Color
            String colorStr = anElement.getAttributeValue("color");
            Color color = colorStr != null ? new Color(colorStr) : Color.BLACK;

            // Unarchive Width, DashArray, DashPhase
            double width = 1, dashArray[] = null, dashPhase = 0;
            if (anElement.hasAttribute("width"))
                width = anElement.getAttributeFloatValue("width", 1);
            else if (anElement.hasAttribute("linewidth"))
                width = anElement.getAttributeFloatValue("linewidth", 1);
            if (anElement.hasAttribute("dash-array"))
                dashArray = Stroke.getDashArray(anElement.getAttributeValue("dash-array"));
            if (anElement.hasAttribute("dash-phase"))
                dashPhase = anElement.getAttributeFloatValue("dash-phase");
            Stroke stroke = new Stroke(width, dashArray, dashPhase);
            return new Borders.LineBorder(color, stroke);
        }
    }
}

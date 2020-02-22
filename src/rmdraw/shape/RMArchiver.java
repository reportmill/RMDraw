/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.shape;
import rmdraw.base.*;
import rmdraw.graphics.*;
import java.util.*;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.RichText;
import snap.gfx.TextLineStyle;
import snap.util.*;
import snap.web.WebURL;

/**
 * This class handles RM document archival.
 */
public class RMArchiver extends XMLArchiver {

    // The shared class map
    private static Map <String, Class> _classMapRM;

    /**
     * Creates RMArchiver.
     */
    public RMArchiver()  { }

    /**
     * Returns a parent shape for source.
     */
    public RMDocument getDoc(Object aSource)  { return getDoc(aSource, null); }

    /**
     * Creates a document.
     */
    public RMDocument getDoc(Object aSource, RMDocument aBaseDoc)
    {
        // If source is a document, just return it
        if(aSource instanceof RMDocument) return (RMDocument)aSource;

        // Get URL and/or bytes (complain if not found)
        WebURL url = WebURL.getURL(aSource);
        byte bytes[] = url!=null? url.getBytes() : SnapUtils.getBytes(aSource);
        if(bytes==null)
            throw new RuntimeException("RMArchiver.getDoc: Cannot read source: " + (url!=null? url : aSource));

        // If PDF, return PDF Doc
        //if(RMPDFData.canRead(bytes)) return RMPDFShape.getDocPDF(url!=null? url : bytes, aBaseDoc);

        // Create archiver, read, set source and return
        setRootObject(aBaseDoc);

        RMDocument doc = (RMDocument)readObject(url!=null? url : bytes);

        // Set Source URL and return
        doc.setSourceURL(getSourceURL());
        return doc;
    }

    /**
     * Creates the class map.
     */
    protected Map <String, Class> createClassMap()
    {
        return getClassMapShared();
    }

    /**
     * Creates the class map.
     */
    public static Map <String, Class> getClassMapShared()
    {
        // If already set, just return
        if (_classMapRM!=null) return _classMapRM;

        // Create class map and add classes
        Map cmap = new HashMap();

        // Shape classes
        cmap.put("arrow-head", RMLineShape.ArrowHead.class);
        cmap.put("document", RMDocument.class);
        cmap.put("flow-shape", RMParentShape.class);
        cmap.put("image-shape", RMImageShape.class);
        cmap.put("line", RMLineShape.class);
        cmap.put("oval", RMOvalShape.class);
        cmap.put("page", RMPage.class);
        cmap.put("polygon", RMPolygonShape.class);
        cmap.put("rect", RMRectShape.class);
        cmap.put("shape", RMParentShape.class);
        cmap.put("spring-shape", RMSpringShape.class);
        cmap.put("text", RMTextShape.class);
        cmap.put("linked-text", RMLinkedText.class);
        cmap.put("scene3d", RMScene3D.class);

        // Graphics
        cmap.put("color", Color.class);
        cmap.put("font", Font.class);
        cmap.put("format", TextFormatStub.class);
        cmap.put("pgraph", TextLineStyle.class); // Was RMParagraph
        cmap.put("xstring", RichText.class); // Was RMXString

        // Strokes
        cmap.put("stroke", RMStroke.class); cmap.put("double-stroke", RMStroke.class);
        cmap.put("border-stroke", "rmdraw.graphics.RMBorderStroke");

        // Fills
        cmap.put("fill", RMFill.class);
        cmap.put("gradient-fill", RMGradientFill.class);
        cmap.put("radial-fill", RMGradientFill.class);
        cmap.put("image-fill", RMImageFill.class);
        cmap.put("contour-fill", "rmdraw.graphics.RMContourFill");

        // Effects
        cmap.put("blur-effect", "snap.gfx.BlurEffect");
        cmap.put("shadow-effect", "snap.gfx.ShadowEffect");
        cmap.put("reflection-effect", "snap.gfx.ReflectEffect");
        cmap.put("emboss-effect", "snap.gfx.EmbossEffect");

        // Sorts, Grouping
        cmap.put("sort", "rmdraw.base.RMSort");
        cmap.put("top-n-sort", "rmdraw.base.RMTopNSort");
        cmap.put("value-sort", "rmdraw.base.RMValueSort");
        cmap.put("grouper", RMGrouper.class);
        cmap.put("grouping", RMGrouping.class);

        // Return classmap
        return _classMapRM = cmap;
    }

    /**
     * A class to unarchive formats as proper subclass based on type attribute.
     */
    public static class TextFormatStub implements Archivable {

        /** Implement toXML for interface. */
        public XMLElement toXML(XMLArchiver anArchive)  { return null; }

        /** Implement fromXML to return proper format based on type attribute. */
        public Object fromXML(XMLArchiver anArchiver, XMLElement anElmnt)
        {
            String type = anElmnt.getAttributeValue("type","");
            if (type.equals("number"))
                return anArchiver.fromXML(anElmnt, RMNumberFormat.class,null);
            if (type.equals("date"))
                return anArchiver.fromXML(anElmnt, RMDateFormat.class, null);
            if (type.length()>0)
                System.err.println("TextFormatStub: Unknown format type " + type);
            return null;
        }
    }
}
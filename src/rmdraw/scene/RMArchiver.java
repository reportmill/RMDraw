/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.scene;
import java.util.*;

import snap.gfx.*;
import snap.text.RichText;
import snap.text.TextLineStyle;
import snap.util.*;
import snap.web.WebURL;

/**
 * This class handles RM document archival.
 */
public class RMArchiver extends XMLArchiver {

    // The shared class map
    private static Map<String, Class> _classMapRM;

    /**
     * Creates RMArchiver.
     */
    public RMArchiver()
    {
    }

    /**
     * Returns a parent shape for source.
     */
    public SGDoc getDocFromSource(Object aSource)
    {
        // If source is a document, just return it
        if (aSource instanceof SGDoc) return (SGDoc) aSource;

        // Get URL and/or bytes (complain if not found)
        WebURL url = WebURL.getURL(aSource);
        byte bytes[] = url != null ? url.getBytes() : SnapUtils.getBytes(aSource);
        if (bytes == null)
            throw new RuntimeException("RMArchiver.getDoc: Cannot read source: " + (url != null ? url : aSource));

        // If PDF, return PDF Doc
        //if(RMPDFData.canRead(bytes)) return RMPDFShape.getDocPDF(url!=null? url : bytes, aBaseDoc);

        // Read document from source
        SGDoc doc = (SGDoc) readFromXMLSource(url != null ? url : bytes);

        // Set Source URL and return
        doc.setSourceURL(getSourceURL());
        return doc;
    }

    /**
     * Creates the class map.
     */
    protected Map<String, Class> createClassMap()
    {
        return getClassMapShared();
    }

    /**
     * Creates the class map.
     */
    public static Map<String, Class> getClassMapShared()
    {
        // If already set, just return
        if (_classMapRM != null) return _classMapRM;

        // Create class map and add classes
        Map cmap = new HashMap();

        // Shape classes
        cmap.put("arrow-head", SGLine.ArrowHead.class);
        cmap.put("document", SGDoc.class);
        cmap.put("flow-shape", SGParent.class);
        cmap.put("image-shape", SGImage.class);
        cmap.put("line", SGLine.class);
        cmap.put("oval", SGOval.class);
        cmap.put("page", SGPage.class);
        cmap.put("polygon", SGPolygon.class);
        cmap.put("rect", SGRect.class);
        cmap.put("shape", SGParent.class);
        cmap.put("spring-shape", SGSpringsView.class);
        cmap.put("text", SGText.class);
        cmap.put("linked-text", SGLinkedText.class);
        cmap.put("scene3d", SGScene3D.class);

        // Graphics
        cmap.put("color", Color.class);
        cmap.put("font", Font.class);
        cmap.put("pgraph", TextLineStyle.class); // Was RMParagraph
        cmap.put("xstring", RichText.class); // Was RMXString

        // Strokes
        cmap.put("stroke", RMArchivers.RMStrokeStub.class);
        cmap.put("double-stroke", RMArchivers.RMStrokeStub.class);
        cmap.put("border-stroke", Borders.EdgeBorder.class);

        // Fills
        cmap.put("fill", RMArchivers.RMFillStub.class);
        cmap.put("gradient-fill", GradientPaint.class);
        cmap.put("radial-fill", GradientPaint.class);
        cmap.put("image-fill", RMArchivers.RMImageFillStub.class);

        // Effects
        cmap.put("blur-effect", BlurEffect.class);
        cmap.put("shadow-effect", ShadowEffect.class);
        cmap.put("reflection-effect", ReflectEffect.class);
        cmap.put("emboss-effect", EmbossEffect.class);

        // Return classmap
        return _classMapRM = cmap;
    }
}
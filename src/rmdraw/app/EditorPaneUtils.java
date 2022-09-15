/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.app;
import rmdraw.scene.*;

import java.io.File;

import snap.util.*;

/**
 * Some utility methods for EditorPane.
 */
public class EditorPaneUtils {

    /**
     * Generate report, save as HTML in temp file and open.
     */
    public static void previewHTML(EditorPane anEP)
    {
        SGDoc doc = anEP.getDoc();
        doc.write(SnapUtils.getTempDir() + "RMHTMLFile.html");
        FileUtils.openFile(SnapUtils.getTempDir() + "RMHTMLFile.html");
    }

    /**
     * Generate report, save as JPG in temp file and open.
     */
    public static void previewJPG(EditorPane anEP)
    {
        SGDoc doc = anEP.getDoc();
        doc.write(SnapUtils.getTempDir() + "RMJPGFile.jpg");
        FileUtils.openFile(SnapUtils.getTempDir() + "RMJPGFile.jpg");
    }

    /**
     * Generate report, save as PNG in temp file and open.
     */
    public static void previewPNG(EditorPane anEP)
    {
        SGDoc doc = anEP.getDoc();
        doc.write(SnapUtils.getTempDir() + "RMPNGFile.png");
        FileUtils.openFile(SnapUtils.getTempDir() + "RMPNGFile.png");
    }

    /**
     * Preview RTF.
     */
    public static void previewRTF(EditorPane anEP)
    {
        SGDoc doc = anEP.getDoc();
        doc.write(SnapUtils.getTempDir() + "RMRTFFile.rtf");
        FileUtils.openFile(SnapUtils.getTempDir() + "RMRTFFile.rtf");
    }

    /**
     * Preview XML.
     */
    public static void previewXML(EditorPane anEP)
    {
        Editor editor = anEP.getEditor();
        XMLElement xml = new RMArchiver().writeToXML(editor.getDoc());
        File file = FileUtils.getTempFile("RMXMLFile.xml");
        try {
            FileUtils.writeBytes(file, xml.getBytes());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        FileUtils.openFile(file);
    }

}
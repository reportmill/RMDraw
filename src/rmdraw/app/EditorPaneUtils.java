/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.app;
import rmdraw.shape.*;
import java.io.File;
import snap.util.*;
import snap.view.*;
import snap.viewx.*;

/**
 * Some utility methods for EditorPane.
 */
public class EditorPaneUtils {

/**
 * Generates report from editor.
 */
public static RMDocument generateReport(EditorPane anEP, boolean doPaginate)
{
    // Get editor - if editing, flush changes, otherwise, set Editing
    Editor editor = anEP.getEditor();
    if(anEP.isEditing())
        editor.flushEditingChanges();
    else anEP.setEditing(true);
    
    // Get document and return report
    RMDocument document = anEP.getDoc();
    return document.generateReport(editor.getDataSourceDataset(), doPaginate);
}

/**
 * Generate report, save as HTML in temp file and open.
 */
public static void previewHTML(EditorPane anEP)
{
    RMDocument doc = anEP.getDoc();
    doc.write(SnapUtils.getTempDir() + "RMHTMLFile.html");
    FileUtils.openFile(SnapUtils.getTempDir() + "RMHTMLFile.html");
}

/**
 * Generate report, save as JPG in temp file and open.
 */
public static void previewJPG(EditorPane anEP)
{
    RMDocument doc = anEP.getDoc();
    doc.write(SnapUtils.getTempDir() + "RMJPGFile.jpg");
    FileUtils.openFile(SnapUtils.getTempDir() + "RMJPGFile.jpg");
}

/**
 * Generate report, save as PNG in temp file and open.
 */
public static void previewPNG(EditorPane anEP)
{
    RMDocument doc = anEP.getDoc();
    doc.write(SnapUtils.getTempDir() + "RMPNGFile.png");
    FileUtils.openFile(SnapUtils.getTempDir() + "RMPNGFile.png");
}

/**
 * Preview RTF.
 */
public static void previewRTF(EditorPane anEP)
{
    RMDocument doc = anEP.getDoc();
    doc.write(SnapUtils.getTempDir() + "RMRTFFile.rtf");
    FileUtils.openFile(SnapUtils.getTempDir() + "RMRTFFile.rtf");
}

/**
 * Preview XML.
 */
public static void previewXML(EditorPane anEP)
{
    Editor editor = anEP.getEditor();
    XMLElement xml = new RMArchiver().writeObject(editor.getDoc());
    File file = FileUtils.getTempFile("RMXMLFile.xml");
    try { FileUtils.writeBytes(file, xml.getBytes()); }
    catch(Exception e) { throw new RuntimeException(e); }
    FileUtils.openFile(file);
}

}
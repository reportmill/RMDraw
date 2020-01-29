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
 * Preview PDF.
 */
public static void previewPDF(EditorPane anEP)
{
    // Get filename (if alt key is pressed, change to current doc plus .pdf)
    String filename = SnapUtils.getTempDir() + "RMPDFFile.pdf";
    if(ViewUtils.isAltDown() && anEP.getDoc().getFilename()!=null)
        filename = FilePathUtils.getSimple(anEP.getDoc().getFilename()) + ".pdf";
    
    // Get report, write report and open file
    RMDocument report = generateReport(anEP, true);
    report.writePDF(filename);
    FileUtils.openFile(filename);
}

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
    RMDocument report = generateReport(anEP, !ViewUtils.isAltDown());
    report.write(SnapUtils.getTempDir() + "RMHTMLFile.html");
    FileUtils.openFile(SnapUtils.getTempDir() + "RMHTMLFile.html");
}

/**
 * Generate report, save as CSV in temp file and open.
 */
public static void previewCSV(EditorPane anEP)
{
    RMDocument report = generateReport(anEP, false);
    report.write(SnapUtils.getTempDir() + "RMCSVFile.csv");
    FileUtils.openFile(SnapUtils.getTempDir() + "RMCSVFile.csv");
}

/**
 * Generate report, save as JPG in temp file and open.
 */
public static void previewJPG(EditorPane anEP)
{
    RMDocument report = generateReport(anEP, false);
    report.write(SnapUtils.getTempDir() + "RMJPGFile.jpg");
    FileUtils.openFile(SnapUtils.getTempDir() + "RMJPGFile.jpg");
}

/**
 * Generate report, save as PNG in temp file and open.
 */
public static void previewPNG(EditorPane anEP)
{
    RMDocument report = generateReport(anEP, false);
    report.write(SnapUtils.getTempDir() + "RMPNGFile.png");
    FileUtils.openFile(SnapUtils.getTempDir() + "RMPNGFile.png");
}

/**
 * Preview XLS.
 */
public static void previewXLS(EditorPane anEP)
{
    // Get report, write report and open file (in handler, in case POI jar is missing)
    try {
        RMDocument report = generateReport(anEP, false);
        report.write(SnapUtils.getTempDir() + "RMXLSFile.xls");
        FileUtils.openFile(SnapUtils.getTempDir() + "RMXLSFile.xls");
    }
    
    // Catch exception - handle case where poi jar is missing    
    catch(Throwable t) {
        
        // print it out (in case it's something other than a missing jar)
        t.printStackTrace();
        
        // Run option dialog to ask user if they want to see Excel doc
        String msg = "ReportMill needs the OpenSource POI jar in order to generate Excel. Click Open to see " +
            "the support document on the subject.";
        DialogBox dbox = new DialogBox("Excel POI Jar Missing");
        dbox.setWarningMessage(StringUtils.wrap(msg, 50)); dbox.setOptions("Open", "Cancel");
        int answer = dbox.showOptionDialog(anEP.getEditor(), "Open");
        
        // If user answered "open", open poi doc url
        if(answer==0)
            URLUtils.openURL("http://reportmill.com/support/Excel.html");
    }
}

/**
 * Preview RTF.
 */
public static void previewRTF(EditorPane anEP)
{
    // Get report, write report and open file
    RMDocument report = generateReport(anEP, true);
    report.write(SnapUtils.getTempDir() + "RMRTFFile.rtf");
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

/**
 * Save document as PDF to given path.
 */
public static void saveAsPDF(EditorPane anEP)
{
    Editor editor = anEP.getEditor();
    String path = FilePanel.showOpenPanel(editor, "PDF file (.pdf)", "pdf"); if(path==null) return;
    editor.flushEditingChanges();
    editor.getDoc().writePDF(path);
}

}
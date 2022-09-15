/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.app;
import snap.gfx.GFXEnv;
import snap.util.*;
import snap.view.WindowView;
import snap.viewx.DialogBox;
import snap.viewx.ExceptionReporter;

/**
 * This is the main class for the RMDraw app.
 */
public class App {

    // Whether app is in process of quiting
    static boolean _quiting;

    /**
     * This is the static main method, called by Java when launching with com.reportmill.App.
     */
    public static void main(String args[])
    {
        new App(args);
    }

    /**
     * Creates a new app instance.
     */
    public App(String args[])
    {
        // Install Exception reporter
        ExceptionReporter er = new ExceptionReporter("ReportMill");
        er.setToAddress("support@reportmill.com");
        er.setInfo("RMDraw Version " + "1.0" + ", Build Date: " + "Unknown");
        Thread.setDefaultUncaughtExceptionHandler(er);

        // Run welcome panel
        Welcome.getShared().runWelcome();
    }

    /**
     * Quits the app (can be invoked by anyone).
     */
    public static void quitApp()
    {
        // Get open editor panes
        if (_quiting) return;
        _quiting = true;
        EditorPane epanes[] = WindowView.getOpenWindowOwners(EditorPane.class);

        // Iterate over open Editors to see if any have unsaved changes
        int answer = 0;
        for (int i = 0, iMax = epanes.length; i < iMax && iMax > 1; i++) {
            EditorPane epane = epanes[i];

            // Turn off editor preview
            epane.setEditing(true);

            // If editor has undos, run Review Unsaved panel and break
            if (epane.getEditor().undoerHasUndos()) {
                DialogBox dbox = new DialogBox("Review Unsaved Documents");
                dbox.setWarningMessage("There are unsaved documents");
                dbox.setOptions("Review Unsaved", "Quit Anyway", "Cancel");
                answer = dbox.showOptionDialog(epane.getEditor(), "Review Unsaved");
                break;
            }
        }

        // If user hit Cancel, just go away
        if (answer == 2) {
            _quiting = false;
            return;
        }

        // Disable welcome panel
        boolean old = Welcome.getShared().isEnabled();
        Welcome.getShared().setEnabled(false);

        // If Review Unsaved, iterate through _editors to see if they should be saved or if user wants to cancel instead
        if (answer == 0)
            for (EditorPane epane : epanes)
                if (!epane.close()) {
                    Welcome.getShared().setEnabled(old);
                    _quiting = false;
                    return;
                }

        // Flush Properties to registry and exit
        try {
            Prefs.get().flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        GFXEnv.getEnv().exit(0);
    }
}
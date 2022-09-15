/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.app;
import snap.util.SnapUtils;
import snap.view.*;
import snap.viewx.*;

/**
 * This class provides the welcome panel for RM.
 */
public class Welcome extends ViewOwner {

    // Whether welcome panel is enabled
    boolean _enabled;

    // A preloaded editor to speed up first open
    EditorPane _preloadEdPane;

    // Shared welcome panel
    static Welcome _shared;

    /**
     * Creates a new Welcome.
     */
    public Welcome()
    {
        _shared = this;
    }

    /**
     * Returns the shared instance of the welcome panel.
     */
    public static Welcome getShared()
    {
        return _shared != null ? _shared : (_shared = new Welcome());
    }

    /**
     * Returns whether welcome panel is enabled.
     */
    public boolean isEnabled()
    {
        return _enabled;
    }

    /**
     * Sets whether welcome panel is enabled.
     */
    public void setEnabled(boolean aValue)
    {
        _enabled = aValue;
    }

    /**
     * Brings up the welcome panel.
     */
    public void runWelcome()
    {
        // Set enabled, since we were run explicitly
        setEnabled(true);

        // Make welcome panel visible
        getWindow().setVisible(true);

        // Preload an editor pane while use ponders the welcome panel
        runLater(() -> _preloadEdPane = newEditorPane());
    }

    /**
     * Close welcome.
     */
    public void close()
    {
        getWindow().setVisible(false);
    }

    /**
     * Initializes the UI panel.
     */
    protected void initUI()
    {
        // Reset BuildLabel, JavaLabel, LicenseLabel
        setViewText("BuildLabel", "Build: " + "Unknown");
        setViewText("JavaLabel", "Java: " + System.getProperty("java.runtime.version"));
        setViewText("LicenseLabel", "Free License for everyone!");

        //
        getView("RecentFilesButton", Button.class).setImage(ComboBox.getArrowImage());

        // Configure Window: Image, Add WindowListener to indicate app should exit when close button clicked
        getWindow().setType(WindowView.TYPE_UTILITY);
        enableEvents(getWindow(), WinClose);

        // Register for Escape action
        addKeyActionHandler("QuitButton", "ESCAPE");
    }

    /**
     * Respond to UI panel controls.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Handle NewButton
        if (anEvent.equals("NewButton")) {

            // Get new editor pane
            EditorPane epane = newEditorPane().newDocument();

            // Make editor window visible, show doc inspector, and order front after delay to get focus back from inspector
            epane.setWindowVisible(true);
            epane.getInspectorPanel().showDocumentInspector();
            EditorPane ep = epane;
            runLater(() -> ep.getWindow().toFront());
            close();  // Close welcome panel
            runLater(() -> ep.getTopToolBar().startSamplesButtonAnim());
        }

        // Handle OpenButton
        if (anEvent.equals("OpenButton")) {
            String path = null;
            if (anEvent.isAltDown()) {
                DialogBox dbox = new DialogBox("Enter Document URL");
                dbox.setMessage("Enter Document URL");
                path = dbox.showInputDialog(getUI(), "http://localhost:8080/Movies.rpt");
            }
            open(path);
        }

        // Handle RecentFilesButton
        if (anEvent.equals("RecentFilesButton"))
            RecentFiles.showPathsMenu(anEvent.getView(), "RecentDocuments", str -> open(str));

        // Handle FinishButton
        if (anEvent.equals("QuitButton") || anEvent.isWinClose())
            App.quitApp();
    }

    /**
     * Opens a document.  If pathName is null, the open panel will be run.
     */
    public void open(String aPath)
    {
        // Get the new editor pane that will open the document
        EditorPane epane = newEditorPane();

        // if no pathname, have editor run open panel
        epane = aPath == null ? epane.open(getView("OpenButton")) : epane.open(aPath);

        // If no document opened, just return
        if (epane == null) return;

        // Make editor window visible, show doc inspector, and order front after delay to get focus back from inspector
        epane.setWindowVisible(true);
        epane.getInspectorPanel().showDocumentInspector();
        EditorPane ep = epane;
        runLater(() -> ep.getWindow().toFront());
        close();  // Close welcome panel
    }

    /**
     * Creates a new editor for new or opened documents.
     */
    public EditorPane newEditorPane()
    {
        // Use/clear cached version if set
        if (_preloadEdPane != null) {
            EditorPane ep = _preloadEdPane;
            _preloadEdPane = null;
            return ep;
        }

        // Otherwise, return new pane with UI loaded
        EditorPane ep = new EditorPane();
        if (SnapUtils.isTeaVM) ep.getUI().getWindow().setMaximized(true);
        return ep;
    }

}
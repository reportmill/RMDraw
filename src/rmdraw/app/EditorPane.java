/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.app;
import rmdraw.scene.*;
import java.util.*;
import snap.gfx.*;
import snap.props.PropChange;
import snap.view.*;
import snap.viewx.*;
import snap.web.*;
import snap.util.*;

/**
 * This class is a container for an Editor in an enclosing ScrollView with tool bars for editing.
 */
public class EditorPane extends ViewerPane {

    // The menu bar owner
    EditorPaneMenuBar _menuBar;

    // The shared editor inspector
    InspectorPanel _inspPanel = createInspectorPanel();

    // The shared attributes inspector (go ahead and create to get RMColorPanel created)
    AttributesPanel _attrsPanel = createAttributesPanel();

    // The image for a window frame icon
    private static Image _frameImg;

    /**
     * Constructor.
     */
    public EditorPane()
    {
        super();
    }

    /**
     * Returns the viewer as an editor.
     */
    public Editor getEditor()
    {
        return (Editor) getViewer();
    }

    /**
     * Overridden to return an Editor.
     */
    protected Viewer createViewer()
    {
        return new Editor();
    }

    /**
     * Override to return as EditorPaneToolBar.
     */
    public EditorPaneToolBar getTopToolBar()
    {
        return (EditorPaneToolBar) super.getTopToolBar();
    }

    /**
     * Creates the top tool bar.
     */
    protected ViewOwner createTopToolBar()
    {
        return new EditorPaneToolBar(this);
    }

    /**
     * Returns the SwingOwner for the menu bar.
     */
    public EditorPaneMenuBar getMenuBar()
    {
        return _menuBar != null ? _menuBar : (_menuBar = createMenuBar());
    }

    /**
     * Creates the EditorPaneMenuBar for the menu bar.
     */
    protected EditorPaneMenuBar createMenuBar()
    {
        return new EditorPaneMenuBar(this);
    }

    /**
     * Returns whether editing.
     */
    public boolean isEditing()
    {
        return false;
    }

    /**
     * Sets whether editing.
     */
    public void setEditing(boolean aFlag)
    {
        if (aFlag) return;
        throw new RuntimeException("EditorPane.setEditing: Not implemented");
    }

    /**
     * Initializes the UI.
     */
    protected View createUI()
    {
        // Create normal ViewerPane BorderView UI
        BorderView borderView = (BorderView) super.createUI();

        // Get InspectorPanel
        InspectorPanel inspPanel = getInspectorPanel();
        View inspPanelUI = inspPanel.getUI();
        inspPanelUI.setGrowHeight(true);

        // Get AttributesPanel
        AttributesPanel attrPanel = getAttributesPanel();
        attrPanel.getUI();

        // Create ColView to hold them
        ColView colView = new ColView();
        colView.setFillWidth(true);
        colView.setBorder(Color.LIGHTGRAY, 1);
        colView.addChild(inspPanelUI);

        // Add ColView to BorderView
        borderView.setRight(colView);

        // Install AttributesPanel
        ParentView rbox = getRulerBox();
        attrPanel.getDrawer().showTabButton(rbox);

        // Wrap MenuBar View around UI
        return createMenuBarUI(borderView);
    }

    /**
     * Create ColView holding MenuBar and EditorPane UI (with key listener so MenuBar catches shortcut keys).
     */
    protected View createMenuBarUI(View contentView)
    {
        EditorPaneMenuBar menuBar = getMenuBar();
        return MenuBar.createMenuBarView(menuBar.getUI(), contentView);
    }

    /**
     * Override to configure Window.
     */
    protected void initUI()
    {
        // Do normal version
        super.initUI();

        // Enable Events for editor
        enableEvents(getEditor(), MousePress, MouseRelease);

        // Listen for Editor PropChanges
        getEditor().addPropChangeListener(pc -> editorDidPropChange(pc));

        // Configure Window ClassName, Image and enable window events
        WindowView win = getWindow();
        win.setImage(getFrameIcon());
        enableEvents(win, WinClose);
    }

    /**
     * Updates the editor's UI panels.
     */
    protected void resetUI()
    {
        // Do normal update
        super.resetUI();

        // If title has changed, update window title
        if (isWindowVisible()) {
            String title = getWindowTitle();
            WindowView win = getWindow();
            if (!SnapUtils.equals(title, win.getTitle())) {
                win.setTitle(title);
                win.setDocURL(getSourceURL());
            }
        }

        // Reset MenuBar, InspectorPanel and AttributesPanel
        if (!ViewUtils.isMouseDown()) getMenuBar().resetLater();
        if (getInspectorPanel().isResetWithEditor()) getInspectorPanel().resetLater();
        if (getAttributesPanel().isVisible() && !ViewUtils.isMouseDown()) getAttributesPanel().resetLater();
    }

    /**
     * Handles changes to the editor's UI controls.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Forward on to menu bar
        getMenuBar().dispatchEventToOwner(anEvent);

        // Do normal version
        super.respondUI(anEvent);

        // Handle PopupTrigger
        if (anEvent.isPopupTrigger() && !anEvent.isConsumed())
            runPopupMenu(anEvent);

            // Handle WinClosing
        else if (anEvent.isWinClose()) {
            close();
            anEvent.consume();
        }
    }

    /**
     * Returns the inspector panel (shared).
     */
    public InspectorPanel getInspectorPanel()
    {
        return _inspPanel;
    }

    /**
     * Creates the InspectorPanel.
     */
    protected InspectorPanel createInspectorPanel()
    {
        return new InspectorPanel(this);
    }

    /**
     * Returns the attributes panel (shared).
     */
    public AttributesPanel getAttributesPanel()
    {
        return _attrsPanel;
    }

    /**
     * Creates the AttributesPanel.
     */
    protected AttributesPanel createAttributesPanel()
    {
        return new AttributesPanel(this);
    }

    /**
     * Shows the AttributesPanel Drawer.
     */
    public void showAttributesDrawer()
    {
        getAttributesPanel().showDrawer();
    }

    /**
     * Hides the AttributesPanel Drawer.
     */
    public void hideAttributesDrawer()
    {
        getAttributesPanel().hideDrawer();
    }

    /**
     * Returns extension for editor document.
     */
    public String[] getFileExtensions()
    {
        return new String[]{".rpt", ".pdf"};
    }

    /**
     * Returns the description for the editor document for use in open/save panels.
     */
    public String getFileDescription()
    {
        return "ReportMill files (.rpt, .pdf)";
    }

    /**
     * Returns the window title.
     */
    public String getWindowTitle()
    {
        // Get window title: Basic filename + optional "Doc edited asterisk + optional "Doc Scaled"
        String title = getSourceURL() != null ? getSourceURL().getPath() : null;
        if (title == null) title = "Untitled";

        // If has undos, add asterisk. If zoomed, add ZoomFactor
        if (getEditor().getUndoer() != null && getEditor().getUndoer().hasUndos()) title = "* " + title;
        if (!MathUtils.equals(getEditor().getZoomFactor(), 1f))
            title += " @ " + Math.round(getEditor().getZoomFactor() * 100) + "%";

        // If previewing, add "(Previewing)" and return
        if (getEditor().isPreview()) title += " (Previewing)";
        return title;
    }

    /**
     * Creates a new default editor pane.
     */
    public EditorPane newDocument()
    {
        return open(new SGDoc(612, 792));
    }

    /**
     * Creates a new editor window from an open panel.
     */
    public EditorPane open(View aView)
    {
        // Get path from open panel for supported file extensions
        String path = FilePanel.showOpenPanel(aView, getFileDescription(), getFileExtensions());
        return open(path);
    }

    /**
     * Creates a new editor window by opening the document from the given source.
     */
    public EditorPane open(Object aSource)
    {
        // If document source is null, just return null
        if (aSource == null) return null;

        // Get Source URL
        WebURL url = WebURL.getURL(aSource);

        // If source is already opened, return editor pane
        if (!SnapUtils.equals(url, getSourceURL())) {
            EditorPane epanes[] = WindowView.getOpenWindowOwners(EditorPane.class);
            for (EditorPane epane : epanes)
                if (SnapUtils.equals(url, epane.getSourceURL()))
                    return epane;
        }

        // Load document
        SGDoc doc = null;
        try {
            doc = SGDoc.getDocFromSource(aSource);
        }

        // If there was an XML parse error loading aSource, show error dialog
        catch (Exception e) {
            e.printStackTrace();
            String msg = StringUtils.wrap("Error reading file:\n" + e.getMessage(), 40);
            runLater(() -> {
                DialogBox dbox = new DialogBox("Error Reading File");
                dbox.setErrorMessage(msg);
                dbox.showMessageDialog(getUI());
            });
        }

        // If no document, just return null
        if (doc == null) return null;

        // Set document
        getViewer().setDoc(doc);

        // If source is URL, add to recent files
        if (url != null)
            RecentFiles.addURL(url);

        // Return the editor
        return this;
    }

    /**
     * Saves the current editor document, running the save panel.
     */
    public void saveAs()
    {
        // Make sure editor isn't previewing
        setEditing(true);

        // Get extensions - if there is an existing extension, make sure it's first in the exts array
        String exts[] = getFileExtensions();
        if (getSourceURL() != null && FilePathUtils.getExtension(getSourceURL().getPath()) != null) {
            List ex = new ArrayList(Arrays.asList(exts));
            ex.add(0, FilePathUtils.getExtension(getSourceURL().getPath()));
            exts = (String[]) ex.toArray(new String[0]);
        }

        // Run save panel, set Document.Source to path and re-save (or just return if cancelled)
        String path = FilePanel.showSavePanel(getUI(), getFileDescription(), exts);
        if (path == null) return;
        getDoc().setSourceURL(WebURL.getURL(path));
        save();
    }

    /**
     * Saves the current editor document, running the save panel if needed.
     */
    public void save()
    {
        // If can't save to current source, do SaveAs instead
        WebURL url = getSourceURL();
        if (url == null) {
            saveAs();
            return;
        }

        // Make sure editor isn't previewing and has focus (to commit any inspector textfield changes)
        setEditing(true);
        getEditor().requestFocus();

        // Do actual save - if exception, print stack trace and set error string
        try {
            saveImpl();
        }
        catch (Throwable e) {
            e.printStackTrace();
            String msg = "The file " + url.getPath() + " could not be saved (" + e + ").";
            DialogBox dbox = new DialogBox("Error on Save");
            dbox.setErrorMessage(msg);
            dbox.showMessageDialog(getUI());
            return;
        }

        // Add URL to RecentFiles, clear undoer and reset UI
        RecentFiles.addURL(url);
        getDoc().getUndoer().reset();
        resetLater();
    }

    /**
     * The real save method.
     */
    protected void saveImpl()
    {
        WebURL url = getSourceURL();
        WebFile file = url.getFile();
        if (file == null) file = url.createFile(false);
        file.setBytes(getDoc().getBytes());
        file.save();
    }

    /**
     * Reloads the current editor document from the last saved version.
     */
    public void revert()
    {
        // Get filename (just return if null)
        WebURL surl = getSourceURL();
        if (surl == null) return;

        // Run option panel for revert confirmation (just return if denied)
        String msg = "Revert to saved version of " + surl.getFilename() + "?";
        DialogBox dbox = new DialogBox("Revert to Saved");
        dbox.setQuestionMessage(msg);
        if (!dbox.showConfirmDialog(getUI())) return;

        // Re-open filename
        getSourceURL().getFile().reload();
        open(getSourceURL());
    }

    /**
     * Closes this editor pane
     */
    public boolean close()
    {
        // Make sure editor isn't previewing
        setEditing(true);

        // If unsaved changes, run panel to request save
        if (getEditor().undoerHasUndos()) {
            String fname = getSourceURL() == null ? "untitled document" : getSourceURL().getFilename();
            String msg = "Save changes to " + fname + "?", options[] = {"Save", "Don't Save", "Cancel"};
            DialogBox dbox = new DialogBox("Unsaved Changes");
            dbox.setWarningMessage(msg);
            dbox.setOptions(options);
            switch (dbox.showOptionDialog(getUI(), "Save")) {
                case 0:
                    save();
                case 1:
                    break;
                default:
                    return false;
            }
        }

        // Do real close (run later because Java 8 on MacOS sometimes leaves a zombie window after above dialog)
        runLater(() -> closeQuick());
        return true;
    }

    /**
     * Closes window without checking for save.
     */
    protected void closeQuick()
    {
        // Hide window
        getWindow().hide();

        // If another open editor is available focus on it, otherwise run WelcomePanel
        EditorPane epane = WindowView.getOpenWindowOwner(EditorPane.class);
        if (epane != null)
            epane.getEditor().requestFocus();
        else if (Welcome.getShared().isEnabled())
            Welcome.getShared().runWelcome();
    }

    /**
     * Called when the app is about to exit to gracefully handle any open documents.
     */
    public void quit()
    {
        App.quitApp();
    }

    /**
     * Returns a popup menu for the editor.
     */
    public void runPopupMenu(ViewEvent anEvent)
    {
        // Get selected view (just return if page is selected)
        Menu pmenu = new Menu();
        SGView view = getEditor().getSelOrSuperSelView();
        if (view instanceof SGPage) return;

        // If SGText, get copy of Format menu
        if (view instanceof SGText) {
            SGText text = (SGText) view;

            // Get editor pane format menu and add menu items to popup
            Menu formatMenu = getMenuBar().getView("FormatMenu", Menu.class);
            Menu formatMenuCopy = (Menu) formatMenu.clone();
            for (MenuItem m : formatMenuCopy.getItems()) pmenu.addItem(m);

            // If structured tablerow, add AddColumnMenuItem and SplitColumnMenuItem
            //if(text.isStructured()) { MenuItem mi;
            //    mi = new MenuItem(); mi.setText("Add Column"); mi.setName("AddColumnMenuItem"); pmenu.addItem(mi);
            //    mi = new MenuItem(); mi.setText("Split Column"); mi.setName("SplitColumnMenuItem"); pmenu.addItem(mi);
            //}
        }

        // Get copy of views menu and add menu items to popup
        Menu viewsMenu = getMenuBar().getView("ShapesMenu", Menu.class);
        Menu viewsMenuCopy = (Menu) viewsMenu.clone();
        for (MenuItem m : viewsMenuCopy.getItems()) pmenu.addItem(m);

        // Initialize popup menu items to send Events to menu bar
        pmenu.setOwner(getMenuBar());
        pmenu.show(getEditor(), anEvent.getX(), anEvent.getY());
        anEvent.consume();
    }

    /**
     * Called when Editor has prop change.
     */
    private void editorDidPropChange(PropChange aPC)
    {
        String pname = aPC.getPropName();
        switch (pname) {
            case Editor.SelViews_Prop:
            case Editor.SuperSelView_Prop:
                resetLater();
                break;
        }
    }

    /**
     * Returns the icon for the editor window frame.
     */
    private static Image getFrameIcon()
    {
        return _frameImg != null ? _frameImg : (_frameImg = Image.get(EditorPane.class, "ReportMill16x16.png"));
    }

    /**
     * A class for any editor pane support panes.
     */
    public static class SupportPane extends ViewOwner {

        // The editor pane
        EditorPane _editorPane;

        /**
         * Creates a new SupportPane with given editor pane.
         */
        public SupportPane(EditorPane anEP)
        {
            _editorPane = anEP;
        }

        /**
         * Returns the EditorPane.
         */
        public EditorPane getEditorPane()
        {
            return _editorPane;
        }

        /**
         * Sets the EditorPane.
         */
        public void setEditorPane(EditorPane anEP)
        {
            _editorPane = anEP;
        }

        /**
         * Returns the editor.
         */
        public Editor getEditor()
        {
            return _editorPane.getEditor();
        }

        /**
         * Returns the title.
         */
        public String getWindowTitle()
        {
            return "Inspector";
        }
    }
}
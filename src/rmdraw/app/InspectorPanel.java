/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.app;
import rmdraw.editors.PlacerTool;
import rmdraw.editors.StylerPane;
import rmdraw.scene.*;
import snap.geom.Polygon;
import snap.gfx.*;
import snap.util.SnapUtils;
import snap.view.*;

/**
 * This class is responsible for the UI associated with the inspector window.
 */
public class InspectorPanel extends EditorPane.SupportPane {
    
    // The selection path view
    private ChildView  _selPathView;
    
    // The Title label
    private Label  _titleLabel;
    
    // The view SpecificButton
    private ToggleButton _specificBtn;
    
    // The ScrollView that holds UI for child inspectors
    private ScrollView  _inspBox;
    
    // The child inspector current installed in inspector panel
    private ViewOwner  _childInspector;
    
    // The inspector for paint/fill attributes
    private StylerPane  _stylerPane;
    
    // The inspector for view placement attributes (location, size, roll, scale, skew, autosizing)
    private PlacerTool _placementInsp;
    
    // The inspector for view general attributes (name, url, text wrap around)
    private ShapeGeneral  _generalInsp;
    
    // The inspector for view hierarchy
    private ShapeTree  _viewTree;
    
    // The inspector for Undo
    private UndoInspector  _undoInspector;
    
    // The inspector for XML datasource
    //private DataSourcePanel  _dataSource;
    
    // Used for managing selection path
    private SGView _deepView;
    
    // Used for managing selection path
    private SGView _selView;

    /**
     * Creates a new InspectorPanel for EditorPane.
     */
    public InspectorPanel(EditorPane anEP)  { super(anEP); }

    /**
     * Initializes UI panel for the inspector.
     */
    public void initUI()
    {
        // Create StylerPane
        Editor editor = getEditor();
        EditorStyler styler = editor.getStyler();
        _stylerPane = new StylerPane(styler);

        // Create other inspectors
        EditorPane epane = getEditorPane();
        _placementInsp = new PlacerTool(editor.getPlacer());
        _generalInsp = new ShapeGeneral(epane);
        _viewTree = new ShapeTree(epane);

        // Get/configure TitleLabel
        _titleLabel = getView("TitleLabel", Label.class);
        _titleLabel.setTextFill(Color.GRAY);

        // Get SelPathView and InspectorPanel
        _selPathView = getView("SelPathView", ChildView.class);
        enableEvents(_selPathView, MouseRelease);
        _specificBtn = getView("SpecificButton", ToggleButton.class);

        // Get/configure ContentBox
        _inspBox = getView("ContentBox", ScrollView.class);
        _inspBox.setBorder(null);
        _inspBox.setBarSize(12);
        _inspBox.setFillWidth(true);

        // Create the Action that redispatches the event and add the action to the action map
        addKeyActionHandler("UndoAction", "meta Z");
    }

    /**
     * Refreshes the inspector for the current editor selection.
     */
    public void resetUI()
    {
        // Get editor (and just return if null) and tool for selected views
        Editor editor = getEditor(); if (editor==null) return;
        Tool tool = editor.getToolForViews(editor.getSelOrSuperSelViews());

        // If ViewSpecificButton is selected, instal inspector for current selection
        if (getViewBoolValue("SpecificButton"))
            setInspector(tool);

        // If FillsButton is selected, install fill inspector
        if (getViewBoolValue("FillsButton"))
            setInspector(_stylerPane);

        // Get the inspector (owner)
        ViewOwner owner = getInspector();

        // Get inspector title from owner and set
        String title = getInspectorTitle(owner, tool);
        _titleLabel.setText(title);

        // If owner non-null, tell it to reset
        if (owner!=null)
            owner.resetLater();

        // Reset the selection path view
        resetSelPathView();

        // Get image for current tool and set in ViewSpecificButton
        Image timage = tool.getImage();
        getView("SpecificButton", ButtonBase.class).setImage(timage);
    }

    /**
     * Handles changes to the inspector UI controls.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Handle PlacementButton
        if (anEvent.equals("PlacementButton"))
            setInspector(_placementInsp);

        // Handle GeneralButton
        if (anEvent.equals("GeneralButton"))
            setInspector(_generalInsp);

        // Handle UndoAction
        if (anEvent.equals("UndoAction"))
            getEditor().undo();

        // Handle SelPath
        if (anEvent.getName().startsWith("SelPath"))
            popSelection(SnapUtils.intValue(anEvent.getName()));

        // Handle SelPathView
        if (anEvent.equals("SelPathView") && anEvent.isMouseRelease())
            setVisible(9);
    }

    /**
     * Returns whether the inspector is visible.
     */
    public boolean isVisible()  { return isUISet() && getUI().isShowing(); }

    /**
     * Sets whether the inspector is visible.
     */
    public void setVisible(boolean aValue)
    {
        // If requested visible and inspector is not visible, make visible
        if (aValue && !isVisible())
            setVisible(-1);
    }

    /**
     * Sets the inspector to be visible, showing the specific sub-inspector at the given index.
     */
    public void setVisible(int anIndex)
    {
        // If index 0, 1 or 3, set appropriate toggle button true
        if (anIndex==0) setViewValue("SpecificButton", true);
        if (anIndex==1) setViewValue("FillsButton", true);
        if (anIndex==3) setViewValue("GeneralButton", true);

        // If index is 6, show _undoInspector
        if (anIndex==6) {
            setInspector(_undoInspector!=null? _undoInspector : (_undoInspector = new UndoInspector(getEditorPane())));
            _specificBtn.getToggleGroup().setSelected(null); //setViewValue("OffscreenButton", true);
        }

        // If index is 7, show DataSource Inspector
        //if (anIndex==7) {
        //    setInspector(_dataSource!=null? _dataSource : (_dataSource = new DataSourcePanel(getEditorPane())));
        //    _specificBtn.getToggleGroup().setSelected(null); //setViewValue("OffscreenButton", true);
        //}

        // If index is 9, show ViewTree Inspector
        if (anIndex==9) {
            setInspector(_viewTree);
            _specificBtn.getToggleGroup().setSelected(null);
        }
    }

    /**
     * Returns whether inspector should update when editor does.
     */
    public boolean isResetWithEditor()
    {
        if (!isVisible()) return false;
        if (!ViewUtils.isMouseDrag()) return true;
        return getInspector()== _placementInsp;
    }

    /**
     * Returns whether the inspector is showing the datasource inspector.
     */
    public boolean isShowingDataSource()
    {
        return false; //isUISet() && _dataSource!=null && _dataSource.getUI().isShowing();
    }

    /**
     * Returns the inspector (owner) of the inspector pane.
     */
    protected ViewOwner getInspector()  { return _childInspector; }

    /**
     * Sets the inspector in the inspector pane.
     */
    protected void setInspector(ViewOwner anOwner)
    {
        // Set new inspector
        _childInspector = anOwner;

        // Get content and it grows height
        View content = anOwner.getUI();
        boolean contentGrowHeight = content.isGrowHeight();

        // Set content and whether Inspector ScrollView sizes or scrolls content vertically
        _inspBox.setContent(content);
        _inspBox.setFillHeight(contentGrowHeight);
    }

    /**
     * Updates the selection path view.
     */
    public void resetSelPathView()
    {
        // Get main editor, Selected/SuperSelected view and view that should be selected in selection path
        Editor editor = getEditor();
        SGView selView = editor.getSelOrSuperSelView();
        SGView view = _deepView!=null && _deepView.isAncestor(selView)? _deepView : selView;

        // If the selView has changed because of external forces, reset selectionPath to point to it
        if (selView != _selView)
            view = selView;

        // Set new DeepView to be view
        _deepView = view;
        _selView = selView;

        // Remove current buttons
        for (int i=_selPathView.getChildCount()-1; i>=0; i--) {
            View button = _selPathView.removeChild(i);
            if (button instanceof ToggleButton)
                getToggleGroup("SelPathGroup").remove((ToggleButton)button);
        }

        // Add buttons for DeepView and its ancestors
        for (SGView vue = _deepView; vue!=null; vue=vue.getParent()) {

            // Create new button and configure action
            ToggleButton button = new ToggleButton();
            button.setName("SelPath " + vue.getAncestorCount());
            button.setPrefSize(40,40);
            button.setMinSize(40,40);
            button.setShowArea(false);

            // Set button images
            Image img = editor.getToolForView(vue).getImage();
            button.setImage(img);
            button.setToolTip(vue.getClass().getSimpleName());
            if (vue==selView)
                button.setSelected(true);  // Whether selected

            // Add button to selection path panel and button group
            _selPathView.addChild(button, 0); button.setOwner(this);
            getToggleGroup("SelPathGroup").add(button);
            if (vue!= _deepView)
                _selPathView.addChild(new Sep(), 1);
        }
    }

    /**
     * Changes the selection path selection to the level of the string index in the action event.
     */
    public void popSelection(int selIndex)
    {
        // Get main editor (just return if editor or deepest view is null)
        Editor editor = getEditor(); if (editor==null || _deepView ==null) return;

        // If user selected descendant of current selected view, select on down to it
        if (selIndex > editor.getSelOrSuperSelView().getAncestorCount()) {

            // Get current deepest view
            SGView view = _deepView;

            // Find view that was clicked on
            while (selIndex != view.getAncestorCount())
                view = view.getParent();

            // If view parent's childrenSuperSelectImmediately, superSelect view
            if (view.getParent().childrenSuperSelectImmediately())
                editor.setSuperSelView(view);

            // If view shouldn't superSelect, just select it
            else editor.setSelView(view);
        }

        // If user selected ancestor of current view, pop selection up to it
        else while (selIndex != editor.getSelOrSuperSelView().getAncestorCount())
            editor.popSelection();

        // Set selected view to new editor selected view
        _selView = editor.getSelOrSuperSelView();

        // Make sure view specific inspector is selected
        if (!getViewBoolValue("SpecificButton"))
            getView("SpecificButton", ToggleButton.class).fire();
    }

    /**
     * Makes the inspector panel show the document inspector.
     */
    public void showDocumentInspector()
    {
        setVisible(0);
        resetSelPathView();
        popSelection(0);
    }

    /**
     * Returns an inspector for ViewOwner and tool.
     */
    private String getInspectorTitle(ViewOwner owner, Tool tool)
    {
        // If Tool, just return title
        if (owner instanceof Tool)
            return ((Tool)owner).getWindowTitle();

            // If SupportPane
        else if (owner instanceof EditorPane.SupportPane) {
            String title = ((EditorPane.SupportPane)owner).getWindowTitle();
            String cname = tool.getShapeClass().getSimpleName();
            String shpName = cname.replace("RM", "").replace("Shape", "");
            title += " (" + shpName + ')';
            return title;
        }

        // Just return generic title
        return "Inspector";
    }

    /** View to render SelectionPath separator. */
    private static class Sep extends View {
        protected double getPrefWidthImpl(double aH)  { return 5; }
        protected double getPrefHeightImpl(double aW)  { return 40; }
        protected void paintFront(Painter aPntr)  { aPntr.setColor(Color.DARKGRAY); aPntr.fill(_arrow); }
        static Polygon _arrow = new Polygon(0, 15, 5, 20, 0, 25);
    }
}
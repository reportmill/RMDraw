package rmdraw.app;
import rmdraw.scene.*;
import java.util.*;
import snap.geom.*;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;
import snap.viewx.DialogBox;
import snap.viewx.DialogSheet;
import snap.web.WebResponse;
import snap.web.WebURL;

/**
 * A class to show samples.
 */
public class SamplesPane extends ViewOwner {

    // The editor pane
    private EditorPane _epane;

    // The shared document names
    private static String _docNames[];

    // The shared document images
    private static Image _docImages[];

    // The selected index
    private int _selIndex;

    // The dialog box
    private DialogSheet _dbox;

    // Constants
    private static final String SAMPLES_ROOT = "https://reportmill.com/rmsamples/";
    private static final Effect SHADOW = new ShadowEffect();
    private static final Effect SHADOW_SEL = new ShadowEffect(10, Color.get("#038ec3"), 0, 0);

    /**
     * Shows the samples pane.
     */
    public void showSamples(EditorPane anEP)
    {
        _epane = anEP;
        ChildView aView = (ChildView) anEP.getUI();

        _dbox = new DialogSheet();
        _dbox.setContent(getUI());
        _dbox.showConfirmDialog(aView);
        _dbox.addPropChangeListener(pc -> dialogBoxClosed(), DialogBox.Showing_Prop);
    }

    /**
     * Called when dialog box closed.
     */
    void dialogBoxClosed()
    {
        if (_dbox.isCancelled()) return;
        if (_selIndex == 0 && _epane.getDoc().getPage(0).getChildCount() == 0) return;
        _epane.getEditor().setDoc(getDoc(_selIndex));
        _epane.getEditor().requestFocus();
    }

    /**
     * Creates UI.
     */
    protected View createUI()
    {
        // Create main ColView to hold RowViews for samples
        ColView colView = new ColView();
        colView.setName("ItemColView");
        colView.setSpacing(25);
        colView.setPadding(25, 15, 20, 15);
        colView.setAlign(Pos.TOP_CENTER);
        colView.setFillWidth(true);
        colView.setFill(new Color(.97, .97, 1d));
        colView.setBorder(Color.GRAY, 1);
        colView.setPrefWidth(557);

        // Add loading label
        Label loadLabel = new Label("Loading...");
        loadLabel.setFont(Font.Arial16.deriveFont(32).getBold());
        loadLabel.setTextFill(Color.GRAY);
        colView.addChild(loadLabel);

        // Create ScrollView
        ScrollView scroll = new ScrollView(colView);
        scroll.setPrefHeight(420);
        scroll.setShowHBar(false);
        scroll.setShowVBar(true);

        // Create "Select template" label
        Label selectLabel = new Label("Select a template:");
        selectLabel.setFont(Font.Arial16.deriveFont(20).getBold());

        // Create HeaderRow to hold SelectLabel
        RowView headerRow = new RowView();
        headerRow.addChild(selectLabel);

        // Create top level col view to hold HeaderRow and ColView
        ColView boxView = new ColView();
        boxView.setSpacing(8);
        boxView.setFillWidth(true);
        boxView.setChildren(headerRow, scroll);
        return boxView;
    }

    /**
     * Initialize UI.
     */
    protected void initUI()
    {
        if (_docNames == null)
            loadIndexFile();
        else buildUI();
    }

    /**
     * Starts loading.
     */
    private void loadIndexFile()
    {
        WebURL url = WebURL.getURL(SAMPLES_ROOT + "index.txt");
        url.getResponseAndCall(resp -> indexFileLoaded(resp));
    }

    /**
     * Loads content.
     */
    private void indexFileLoaded(WebResponse aResp)
    {
        // If response is bogus, report it
        if (aResp.getCode() != WebResponse.OK) {
            runLater(() -> indexFileLoadFailed(aResp));
            return;
        }

        // Get text and break into lines
        String text = aResp.getText();
        String lines[] = text.split("\\s*\n\\s*");

        // Get names list from lines
        List<String> docNamesList = new ArrayList();
        for (String line : lines) {
            line = line.trim();
            if (line.length() > 0)
                docNamesList.add(line);
        }

        // Get DocNames from list
        _docNames = docNamesList.toArray(new String[0]);
        _docImages = new Image[_docNames.length];

        // Rebuild UI
        runLater(() -> buildUI());
    }

    /**
     * Loads failure condition.
     */
    private void indexFileLoadFailed(WebResponse aResp)
    {
        // Get error string and TextArea
        String str = "Failed to load index file.\n" + "Response code: " + aResp.getCodeString() + "\n" +
                "Exception: " + aResp.getException();
        TextArea textArea = new TextArea();
        textArea.setText(str);

        // Add to ColView
        ColView colView = getView("ItemColView", ColView.class);
        colView.setAlign(Pos.CENTER);
        colView.addChild(textArea);
    }

    /**
     * Loads samples.
     */
    private void buildUI()
    {
        // Get ItemColView and remove children
        ColView colView = getView("ItemColView", ColView.class);
        colView.removeChildren();

        // Create RowViews
        RowView rowView = null;
        for (int i = 0; i < _docNames.length; i++) {
            String name = _docNames[i];

            // Create/add new RowView for every three samples
            if (i % 3 == 0) {
                rowView = new RowView();
                rowView.setAlign(Pos.CENTER);
                colView.addChild(rowView);
            }

            // Create ImageViewX for sample
            ImageView iview = new ImageView();
            iview.setPrefSize(getDocSize(i));
            iview.setFill(Color.WHITE);
            iview.setName("ImageView" + i);
            iview.setEffect(i == 0 ? SHADOW_SEL : SHADOW);

            // Create label for sample
            Label label = new Label(name);
            label.setFont(Font.Arial13);
            label.setPadding(3, 4, 3, 4);
            label.setLeanY(VPos.BOTTOM);
            if (i == 0) {
                label.setFill(Color.BLUE);
                label.setTextFill(Color.WHITE);
            }

            // Create/add ItemBox for Sample and add ImageView + Label
            ColView ibox = new ColView();
            ibox.setPrefSize(175, 175);
            ibox.setAlign(Pos.TOP_CENTER);
            ibox.setChildren(iview, label);
            ibox.setPadding(0, 0, 8, 0);
            ibox.setName("ItemBox" + i);
            ibox.addEventHandler(e -> itemBoxWasPressed(ibox, e), MousePress);
            rowView.addChild(ibox);
        }

        // Make sure all row views and image boxes are owned by ui
        for (View child : colView.getChildren())
            child.setOwner(this);

        // Load images
        loadImagesInBackground();
    }

    /**
     * Called when template ItemBox is clicked.
     */
    private void itemBoxWasPressed(ColView anItemBox, ViewEvent anEvent)
    {
        // Get name and index of pressed ItemBox
        String name = anItemBox.getName();
        int index = StringUtils.intValue(name);

        // Set attributes of current selection back to normal
        ColView oldItemBox = getView("ItemBox" + _selIndex, ColView.class);
        oldItemBox.getChild(0).setEffect(SHADOW);
        Label oldLabel = (Label) oldItemBox.getChild(1);
        oldLabel.setFill(null);
        oldLabel.setTextFill(null);

        // Set attributes of new selection to selected effect
        anItemBox.getChild(0).setEffect(SHADOW_SEL);
        Label newLabel = (Label) anItemBox.getChild(1);
        newLabel.setFill(Color.BLUE);
        newLabel.setTextFill(Color.WHITE);

        // Set new index
        _selIndex = index;

        // If double-click, confirm dialog box
        if (anEvent.getClickCount() > 1) _dbox.confirm();
    }

    /**
     * Returns the number of docs.
     */
    private static int getDocCount()
    {
        return _docNames.length;
    }

    /**
     * Returns the doc names.
     */
    private static String[] getDocNames()
    {
        return _docNames;
    }

    /**
     * Returns the doc name at index.
     */
    private static String getDocName(int anIndex)
    {
        return _docNames[anIndex];
    }

    /**
     * Returns the doc at given index.
     */
    private static SGDoc getDoc(int anIndex)
    {
        // Get document name, URL string and URL
        String name = getDocName(anIndex);
        String urls = SAMPLES_ROOT + name + '/' + name + ".rpt";
        WebURL url = WebURL.getURL(urls);

        // Get bytes (complain if not found)
        byte bytes[] = url.getBytes();
        if (bytes == null) {
            System.err.println("SamplesPane.getDoc: Couldn't load " + url);
            return null;
        }

        // Return document
        SGDoc doc = SGDoc.getDocFromSource(bytes);
        return doc;
    }

    /**
     * Returns the doc thumnail image at given index.
     */
    private Image getDocImage(int anIndex)
    {
        // If image already set, just return
        Image img = _docImages[anIndex];
        if (img != null) return img;

        // Get image name, URL string, and URL
        String name = getDocName(anIndex);
        String urls = SAMPLES_ROOT + name + '/' + name + ".png";
        WebURL imgURL = WebURL.getURL(urls);

        // Create Image. Then make sure image is loaded by requesting Image.Native.
        img = _docImages[anIndex] = Image.get(imgURL);
        img.getNative();
        return img;
    }

    /**
     * Returns size of doc at given index.
     */
    private static Size getDocSize(int anIndex)
    {
        return new Size(102, 132);
    }

    /**
     * Loads the thumbnail image for each sample in background thread.
     */
    private void loadImagesInBackground()
    {
        new Thread(() -> loadImages()).start();
    }

    /**
     * Loads the thumbnail image for each sample in background thread.
     */
    private void loadImages()
    {
        // Iterate over sample names and load/set images
        for (int i = 0; i < getDocCount(); i++) {
            int index = i;
            Image img = getDocImage(i);
            runLater(() -> setImage(img, index));
        }
    }

    /**
     * Called after an image is loaded to set in ImageView in app thread.
     */
    private void setImage(Image anImg, int anIndex)
    {
        String name = "ImageView" + anIndex;
        ImageView iview = getView(name, ImageView.class);
        iview.setImage(anImg);
    }

    /**
     * Creates images (only needed when updating).
     */
    private static void createImages()
    {
        for (int i = 0, iMax = getDocCount(); i < iMax; i++) {
            SGDoc doc = getDoc(i);
            if (doc == null) continue;
            doc.getPage(0).setPaintBackground(false);
            Size size = getDocSize(i);
            int index = i;
            Image img = createImage(doc.getPage(0), size.width, size.height);
            byte bytes[] = img.getBytesPNG();
            new java.io.File("/tmp/gallery").mkdir();
            String fname = "/tmp/gallery/" + getDocName(i) + ".png";
            SnapUtils.writeBytes(bytes, fname);
        }
    }

    /**
     * Returns an image for the given shape, with given background color (null for clear) and scale.
     */
    private static Image createImage(SGView aShape, double aW, double aH)
    {
        // Create new image
        int w = (int) Math.round(aW), h = (int) Math.round(aH);
        Image img = Image.get(w, h, false);

        // Create painter and configure
        Painter pntr = img.getPainter();
        pntr.setImageQuality(1);

        // Fill background
        pntr.setColor(Color.WHITE);
        pntr.fillRect(0, 0, w, h);
        pntr.setColor(Color.GRAY);
        pntr.drawRect(.5, .5, w - 1, h - 1);

        // Paint shape and return image
        SGViewUtils.layoutDeep(aShape);
        SGViewUtils.paintView(aShape, pntr, new Rect(0, 0, w, h), 1d / 6);
        pntr.flush();
        return img;
    }
}
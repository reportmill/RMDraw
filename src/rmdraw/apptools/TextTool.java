/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.apptools;
import rmdraw.app.*;
import rmdraw.scene.*;
import java.util.List;
import snap.geom.*;
import snap.gfx.*;
import snap.props.PropChange;
import snap.props.PropChangeListener;
import snap.text.*;
import snap.view.*;

/**
 * This class provides UI editing for text shapes.
 */
public class TextTool<T extends SGText> extends Tool<T> {
    
    // The TextArea in the inspector panel
    private TextArea  _textArea;

    // The TextArea used to paint text in editor when super-selected
    private TextEditor _textEdtr;
    
    // The shape hit by text tool on mouse down
    private SGView _downShape;
    
    // Whether editor should resize RMText whenever text changes
    private boolean  _updatingSize = false;
    
    // The minimum height of the RMText when editor text editor is updating size
    private double  _updatingMinHeight = 0;

    // A Listener for TextEditor Selection PropChange
    private PropChangeListener _textEditorSelChangeLsnr = pc -> textEditorSelChanged();

    /**
     * Returns the TextEditor used to edit text in Editor view.
     */
    public TextEditor getTextEditor()
    {
        getUI(); // I don't like this

        // Get selected text (just return if null)
        SGText text = getSelView();
        if (text==null || !isSuperSelected(text))
            return null;

        // Update TextEditor
        TextBox tbox = text.getTextBox();
        _textEdtr.setTextBox(tbox);
        return _textEdtr;
    }

    /**
     * Returns whether TextEditor used to edit text in Editor view is active.
     */
    public boolean isTextEditorActive()
    {
        return getTextEditor()!=null;
    }

    /**
     * Initialize UI panel.
     */
    protected void initUI()
    {
        // Get the TextView and register to update selection
        TextView textView = getView("TextView", TextView.class);
        textView.getScrollView().setBarSize(12);
        _textArea = textView.getTextArea();
        _textArea.addPropChangeListener(pc -> textAreaPropChanged(pc));

        // Create TextEditor
        _textEdtr = new TextShapeTextEditor();
    }

    /**
     * Refresh UI from currently selected text shape.
     */
    public void resetUI()
    {
        // Get editor and currently selected text
        Editor editor = getEditor();
        SGText text = getSelView(); if(text==null) return;
        TextToolStyler styler = (TextToolStyler)getStyler(text);

        // Get paragraph from text
        int selStart = 0; if(_textArea.isFocused()) selStart = _textArea.getSelStart();
        TextLineStyle pgraph = text.getRichText().getLineStyleAt(selStart);

        // If editor is text editing, get paragraph from text editor instead
        TextEditor ted = getTextEditor();
        if (ted!=null) {
            pgraph = ted.getSelLineStyle();
            ted.setActive(editor.isFocused() && isSuperSelected(text));
        }

        // Update AlignLeftButton, AlignCenterButton, AlignRightButton, AlignFullButton
        setViewValue("AlignLeftButton", pgraph.getAlign()==HPos.LEFT);
        setViewValue("AlignCenterButton", pgraph.getAlign()==HPos.CENTER);
        setViewValue("AlignRightButton", pgraph.getAlign()==HPos.RIGHT);
        setViewValue("AlignFullButton", pgraph.isJustify());

        // Update AlignTopButton, AlignMiddleButton, AlignBottomButton
        setViewValue("AlignTopButton", text.getAlignY()== VPos.TOP);
        setViewValue("AlignMiddleButton", text.getAlignY()==VPos.CENTER);
        setViewValue("AlignBottomButton", text.getAlignY()==VPos.BOTTOM);

        // Set TextView RichText and selection
        _textArea.setRichText(text.getRichText());
        if (ted!=null)
            _textArea.setSel(ted.getSelStart(), ted.getSelEnd());

        // Get text's background color and set in TextArea if found
        Color color = null;
        for (SGView shape = text; color==null && shape!=null;) {
            if (shape.getFill()==null) shape = shape.getParent();
            else color = shape.getFill().getColor();
        }
        _textArea.setFill(color==null ? Color.WHITE : color);

        // Get xstring font size and scale up to 12pt if any string run is smaller
        double fsize = 12;
        for (RichTextLine line : text.getRichText().getLines())
            for (RichTextRun run : line.getRuns())
                fsize = Math.min(fsize, run.getFont().getSize());
        _textArea.setFontScale(fsize<12? 12/fsize : 1);

        // Update MarginText, RoundingThumb, RoundingText
        setViewValue("MarginText", text.getMarginString());
        setViewValue("RoundingThumb", text.getRadius());
        setViewValue("RoundingText", text.getRadius());

        // Update ShowBorderCheckBox, CoalesceNewlinesCheckBox, PerformWrapCheckBox
        setViewValue("ShowBorderCheckBox", text.getDrawsSelectionRect());
        setViewValue("CoalesceNewlinesCheckBox", text.getCoalesceNewlines());
        setViewValue("PerformWrapCheckBox", text.getPerformsWrap());

        // Update PaginateRadio, ShrinkRadio, GrowRadio
        setViewValue("PaginateRadio", text.getWraps()== SGText.WRAP_BASIC);
        setViewValue("ShrinkRadio", text.getWraps()== SGText.WRAP_SCALE);
        setViewValue("GrowRadio", text.getWraps()== SGText.WRAP_NONE);

        // Update CharSpacingSpinner, LineSpacingSpinner, LineGapSpinner
        setViewValue("CharSpacingSpinner", styler.getCharSpacing());
        setViewValue("LineSpacingSpinner", styler.getLineSpacing());
        setViewValue("LineGapSpinner", styler.getLineGap());

        // Update PDF options: EditableCheckBox, MultilineCheckBox
        setViewValue("EditableCheckBox", text.isEditable());
        setViewValue("MultilineCheckBox", text.isEditable() && text.isMultiline());
        setViewEnabled("MultilineCheckBox", text.isEditable());
    }

    /**
     * Handles changes from UI panel controls.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Get editor, currently selected text shape and text shapes (just return if null)
        Editor editor = getEditor();
        SGText text = getSelView(); if(text==null) return;
        List <SGText> texts = (List) getSelViews();

        // Register repaint for texts
        for(SGView txt : texts) txt.repaint(); //texts.forEach(i -> i.repaint());

        // Handle AlignLeftButton, AlignCenterButton, AlignRightButton, AlignFullButton, AlignTopButton, AlignMiddleButton
        if (anEvent.equals("AlignLeftButton")) editor.getStyler().setAlignX(HPos.LEFT);
        if (anEvent.equals("AlignCenterButton")) editor.getStyler().setAlignX(HPos.CENTER);
        if (anEvent.equals("AlignRightButton")) editor.getStyler().setAlignX(HPos.RIGHT);
        if (anEvent.equals("AlignFullButton")) editor.getStyler().setJustify(true);
        if (anEvent.equals("AlignTopButton")) for(SGText txt : texts) txt.setAlignY(VPos.TOP);
        if (anEvent.equals("AlignMiddleButton")) for(SGText txt : texts) txt.setAlignY(VPos.CENTER);
        if (anEvent.equals("AlignBottomButton")) for(SGText txt : texts) txt.setAlignY(VPos.BOTTOM);

        // If RoundingThumb or RoundingText, make sure shapes have stroke
        if (anEvent.equals("RoundingThumb") || anEvent.equals("RoundingText"))
            for(SGText t : texts) t.setBorder(Border.blackBorder());

        // Handle MarginText, RoundingThumb, RoundingText
        if (anEvent.equals("MarginText")) for(SGText txt : texts)
            txt.setMarginString(anEvent.getStringValue());
        if (anEvent.equals("RoundingThumb")) for(SGText txt : texts)
            txt.setRadius(anEvent.getFloatValue());
        if (anEvent.equals("RoundingText")) for(SGText txt : texts)
            txt.setRadius(anEvent.getFloatValue());

        // Handle ShowBorderCheckBox, CoalesceNewlinesCheckBox, PerformWrapCheckBox
        if (anEvent.equals("ShowBorderCheckBox"))
            for (SGText txt : texts) txt.setDrawsSelectionRect(anEvent.getBoolValue());
        if (anEvent.equals("CoalesceNewlinesCheckBox"))
            for (SGText txt : texts) txt.setCoalesceNewlines(anEvent.getBoolValue());
        if (anEvent.equals("PerformWrapCheckBox"))
            for (SGText txt : texts) txt.setPerformsWrap(anEvent.getBoolValue());

        // Handle PaginateRadio, ShrinkRadio, GrowRadio
        if (anEvent.equals("PaginateRadio")) for(SGText txt : texts) txt.setWraps(SGText.WRAP_BASIC);
        if (anEvent.equals("ShrinkRadio")) for(SGText txt : texts) txt.setWraps(SGText.WRAP_SCALE);
        if (anEvent.equals("GrowRadio")) for(SGText txt : texts) txt.setWraps(SGText.WRAP_NONE);

        // Handle CharSpacingSpinner, LineSpacingSpinner, LineSpacingSingleButton, LineSpacingDoubleButton, LineGapSpinner
        if (anEvent.equals("CharSpacingSpinner")) setCharSpacing(anEvent.getFloatValue());
        if (anEvent.equals("LineSpacingSpinner")) setLineSpacing(anEvent.getFloatValue());
        if (anEvent.equals("LineSpacingSingleButton")) setLineSpacing(1);
        if (anEvent.equals("LineSpacingDoubleButton")) setLineSpacing(2);
        if (anEvent.equals("LineGapSpinner")) setLineGap(anEvent.getFloatValue());

        // Handle LineHeightMinSpinner, LineHeightMaxSpinner
        //if(anEvent.equals("LineHeightMinSpinner")) setLineHeightMin(editor, Math.max(anEvent.getFloatValue(), 0));
        //if(anEvent.equals("LineHeightMaxSpinner")) {
        //    float val = anEvent.getFloatValue(); if(val>=999) val = Float.MAX_VALUE; setLineHeightMax(editor, val); }

        // Handle MakeMinWidthMenuItem, MakeMinHeightMenuItem
        if (anEvent.equals("MakeMinWidthMenuItem"))
            for (SGText txt : texts) txt.setWidth(txt.getBestWidth());
        if (anEvent.equals("MakeMinHeightMenuItem"))
            for (SGText txt : texts) txt.setHeight(txt.getBestHeight());

        // Handle TurnToPathMenuItem
        if (anEvent.equals("TurnToPathMenuItem"))
            for (int i=0; i<texts.size(); i++) {
                SGText text1 = texts.get(i);
                SGView textPathShape = SGTextUtils.getTextPathView(text1);
                SGParent parent = text1.getParent();
                parent.addChild(textPathShape, text1.indexOf());
                parent.removeChild(text1);
                editor.setSelView(textPathShape);
            }

        // Handle TurnToCharsShapeMenuItem
        if (anEvent.equals("TurnToCharsShapeMenuItem"))
            for (int i=0; i<texts.size(); i++) {
                SGText text1 = texts.get(i);
                SGView textCharsShape = SGTextUtils.getTextCharsView(text1);
                SGParent parent = text1.getParent();
                parent.addChild(textCharsShape, text1.indexOf());
                parent.removeChild(text1);
                editor.setSelView(textCharsShape);
            }

        // Handle LinkedTextMenuItem
        if (anEvent.equals("LinkedTextMenuItem")) {

            // Get linked text identical to original text and add to text's parent
            SGLinkedText linkedText = new SGLinkedText(text);
            text.getParent().addChild(linkedText);

            // Shift linked text down if there's room, otherwise right, otherwise just offset by quarter inch
            if (text.getFrameMaxY() + 18 + text.getFrame().height*.75 < text.getParent().getHeight())
                linkedText.offsetXY(0, text.getHeight() + 18);
            else if (text.getFrameMaxX() + 18 + text.getFrame().width*.75 < text.getParent().getWidth())
                linkedText.offsetXY(text.getWidth() + 18, 0);
            else linkedText.offsetXY(18, 18);

            // Select and repaint new linked text
            editor.setSelView(linkedText);
            linkedText.repaint();
        }

        // Update PDF options: EditableCheckBox, MultilineCheckBox
        if (anEvent.equals("EditableCheckBox")) text.setEditable(anEvent.getBoolValue());
        if (anEvent.equals("MultilineCheckBox")) text.setMultiline(anEvent.getBoolValue());
    }

    /**
     * Paints selected shape indicator, like handles (and maybe a text linking indicator).
     */
    public void paintHandles(T aView, Painter aPntr, boolean isSuperSelected)
    {
        // Paint bounds rect (maybe)
        paintBoundsRect(aView, aPntr);

        // Paint SuperSelected
        if (isSuperSelected)
            paintTextEditor(aView, aPntr);

        // Otherwise, do normal paint
        else super.paintHandles(aView, aPntr, isSuperSelected);

        // Call paintTextLinkIndicator
        if (isPaintingTextLinkIndicator(aView))
            paintTextLinkIndicator(aView, aPntr);
    }

    /**
     * Returns whether to show bounds rect.
     */
    protected boolean isShowBoundsRect(SGText aText)
    {
        Editor editor = getEditor();
        if (aText.getBorder()!=null) return false; // If text draws it's own stroke, return false
        if (!editor.isEditing()) return false; // If editor is previewing, return false
        if (editor.isSelected(aText) || editor.isSuperSelected(aText)) return true; // If selected, return true
        if (aText.length()==0) return true; // If text is zero length, return true
        if (aText.getDrawsSelectionRect()) return true; // If text explicitly draws selection rect, return true
        return false; // Otherwise, return false
    }

    /**
     * Paint bounds rect (maybe): Set color (red if selected, light gray otherwise), get bounds path and draw.
     */
    public void paintBoundsRect(SGText aText, Painter aPntr)
    {
        // If not ShowBoundsRect, just return
        if (!isShowBoundsRect(aText)) return;

        // Save gstate, set color/stroke
        aPntr.save();
        aPntr.setColor(getEditor().isSuperSelected(aText)? new Color(.9f, .4f, .4f) : Color.LIGHTGRAY);
        aPntr.setStroke(Stroke.Stroke1.copyForDashes(3, 2));

        // Get bounds path
        Shape path = aText.getPath().copyFor(aText.getBoundsLocal());
        path = getEditor().convertFromSceneView(path, aText);

        // Draw bounds rect with no Antialiasing
        aPntr.setAntialiasing(false);
        aPntr.draw(path);
        aPntr.setAntialiasing(true);
        aPntr.restore();
    }

    /**
     * Override to paint text when editing.
     */
    protected void paintTextEditor(T aText, Painter aPntr)
    {
        // Save gstate
        aPntr.save();

        // Transform to Text coords
        Transform xfm = aText.getLocalToParent(null);
        aPntr.transform(xfm);

        // Clip to bounds
        aPntr.clip(aText.getBoundsLocal());

        // Have TextEditor paint active text
        TextEditor ted = getTextEditor();
        ted.paintActiveText(aPntr);

        // Restore gstate
        aPntr.restore();
    }

    /**
     * Returns whether to paint text link indicator.
     */
    protected boolean isPaintingTextLinkIndicator(SGText aText)
    {
        // If there is a linked text, return true
        if(aText.getLinkedText()!=null) return true;

        // If height is less than half-inch, return false
        if(aText.getHeight()<36) return false;

        // If all text visible, return false
        if(aText.isAllTextVisible()) return false;

        // Return true
        return true;
    }

    /**
     * Paints the text link indicator.
     */
    private void paintTextLinkIndicator(SGText aText, Painter aPntr)
    {
        // Turn off anti-aliasing
        aPntr.setAntialiasing(false);

        // Get overflow indicator box center point in editor coords
        Point point = getEditor().convertFromSceneView(aText.getWidth()-15, aText.getHeight(), aText);

        // Get overflow indicator box rect in editor coords
        Rect rect = new Rect(point.x - 5, point.y - 5, 10, 10);

        // Draw white background, black frame, and plus sign and turn off aliasing
        aPntr.setColor(aText.getLinkedText()==null? Color.WHITE : new Color(90, 200, 255)); aPntr.fill(rect);
        aPntr.setColor(aText.getLinkedText()==null? Color.BLACK : Color.GRAY);
        aPntr.setStroke(Stroke.Stroke1); aPntr.draw(rect);
        aPntr.setColor(aText.getLinkedText()==null? Color.BLACK : Color.WHITE);
        aPntr.setStroke(new Stroke(1)); //, BasicStroke.CAP_BUTT, 0));
        aPntr.drawLine(rect.getMidX(), rect.y + 2, rect.getMidX(), rect.getMaxY() - 2);
        aPntr.drawLine(rect.x + 2, rect.getMidY(), rect.getMaxX() - 2, rect.getMidY());

        // Turn on antialiasing
        aPntr.setAntialiasing(true);
    }

    /**
     * Overrides standard tool method to deselect any currently editing text.
     */
    public void activateTool()
    {
        if (getEditor().getSuperSelView() instanceof SGText)
            getEditor().setSuperSelView(getEditor().getSuperSelView().getParent());
    }

    /**
     * Override to return TextToolCopyPaster to handle text copy/paste.
     */
    protected CopyPaster getCopyPaster()
    {
        if (isTextEditorActive())
            return new TextEditorCopyPaster();
        return super.getCopyPaster();
    }

    /**
     * Event handling - overridden to install text cursor.
     */
    public void mouseMoved(ViewEvent anEvent)  { getEditor().setCursor(Cursor.TEXT); }

    /**
     * Event handling - overridden to install text cursor.
     */
    public void mouseMoved(T aView, ViewEvent anEvent)
    {
        if (getEditor().getViewAtPoint(anEvent.getPoint()) instanceof SGText) {
            getEditor().setCursor(Cursor.TEXT); anEvent.consume(); }
    }

    /**
     * Handles mouse pressed for text tool. Special support to super select any text hit by tool mouse pressed.
     */
    public void mousePressed(ViewEvent anEvent)
    {
        // Register all selectedShapes dirty because their handles will probably need to be wiped out
        for (SGView shp : getEditor().getSelViews()) shp.repaint();

        // Get shape hit by down point
        _downShape = getEditor().getViewAtPoint(anEvent.getX(),anEvent.getY());

        // Get _downPoint from editor
        _downPoint = getEditorEvents().getEventPointInView(true);

        // Create default text instance and set initial bounds to reasonable value
        SGText tshape = new SGText(); _newView = tshape;
        Rect defaultBounds = TextToolUtils.getDefaultBounds(tshape, _downPoint);
        _newView.setFrame(defaultBounds);

        // Add shape to superSelectedShape (within an undo grouping) and superSelect
        setUndoTitle("Add Text");
        getEditor().getSuperSelParentView().addChild(_newView);
        getEditor().setSuperSelView(_newView);
        _updatingSize = true;
    }

    /**
     * Handles mouse dragged for tool. If user doesn't really drag, then default text box should align the base line
     * of the text about the pressed point. If they do really drag, then text box should be the rect they drag out.
     */
    public void mouseDragged(ViewEvent anEvent)
    {
        // If shape wasn't created in mouse down, just return
        if (_newView ==null) return;

        // Set shape to repaint
        _newView.repaint();

        // Get event point in shape parent coords
        Point point = getEditorEvents().getEventPointInView(true);
        point = _newView.localToParent(point);

        // Get text default bounds and effective down point
        SGText tshape = (SGText) _newView;
        Rect defaultBounds = TextToolUtils.getDefaultBounds(tshape, _downPoint);
        Point downPoint = defaultBounds.getPoint(Pos.TOP_LEFT);

        // Get new bounds rect from default bounds and drag point (make sure min height is default height)
        Rect rect = Rect.get(downPoint, point);
        rect.width = Math.max(rect.width, defaultBounds.width);
        rect.height = Math.max(rect.height, defaultBounds.height);

        // Set UpdatingMinHeight to drag rect height, but if text rect unreasonably thin, reset to 0
        _updatingMinHeight = rect.height;
        if (rect.width<=30)
            _updatingMinHeight = 0;

        // Set new shape bounds
        _newView.setFrame(rect);
    }

    /**
     * Event handling for text tool mouse loop.
     */
    public void mouseReleased(ViewEvent e)
    {
        // Get event point in shape parent coords
        Point upPoint = getEditorEvents().getEventPointInView(true);
        upPoint = _newView.localToParent(upPoint);

        // If upRect is really small, see if the user meant to convert a shape to text instead
        if (Math.abs(_downPoint.x - upPoint.x)<=3 && Math.abs(_downPoint.y - upPoint.y)<=3) {

            // If hit shape is text, just super-select that text and return
            if (_downShape instanceof SGText) {
                _newView.removeFromParent();
                getEditor().setSuperSelView(_downShape);
            }

            // If hit shape is Rectangle, Oval or Polygon, swap for RMText and return
            else if (TextToolUtils.shouldConvertToText(_downShape)) {
                _newView.removeFromParent();
                TextToolUtils.convertToText(getEditor(), _downShape, null);
            }
        }

        // Set Editor.CurrentTool to SelectTool and reset new view
        getEditor().setCurrentToolToSelectTool();
        _newView = null;
    }

    /**
     * Event handling for shape editing (just forwards to text editor).
     */
    public void processEvent(T aText, ViewEvent anEvent)
    {
        if (anEvent.isMouseEvent())
            processMouseEvent(aText, anEvent);
        else if (anEvent.isKeyEvent())
            processKeyEvent(aText, anEvent);
    }

    /**
     * Event handling for shape editing (just forwards to text editor).
     */
    protected void processMouseEvent(T aText, ViewEvent anEvent)
    {
        // If shape isn't super selected, just return
        if (!isSuperSelected(aText)) return;

        // If mouse event, convert event to text shape coords and consume
        if (anEvent.isMouseEvent()) { anEvent.consume();
            Point pnt = getEditor().convertToSceneView(anEvent.getX(), anEvent.getY(), aText);
            anEvent = anEvent.copyForPoint(pnt.getX(), pnt.getY());
        }

        // Forward on to editor
        getTextEditor().processEvent(anEvent);
        aText.repaint();
    }

   /**
     * Override to forward to TextEditor.
     */
    protected void processKeyEvent(T aView, ViewEvent anEvent)
    {
        getTextEditor().processEvent(anEvent);
        aView.repaint();
    }

    /**
     * Editor method - installs this text in Editor's text editor.
     */
    public void didBecomeSuperSel(T aText)
    {
        // Start listening to changes to TextEditor Selection change
        TextEditor ted = getTextEditor();
        ted.addPropChangeListener(_textEditorSelChangeLsnr, TextEditor.Selection_Prop);
    }

    /**
     * Editor method - uninstalls this text from Editor's text editor and removes new text if empty.
     */
    public void willLoseSuperSel(T aText)
    {
        // If text editor was really just an insertion point and ending text length is zero, remove text
        if (_updatingSize && aText.length()==0 && getEditor().getSelectTool().getDragMode()==SelectTool.DragMode.None)
            aText.removeFromParent();

        // Stop listening to changes to TextShape RichText
        TextEditor ted = getTextEditor();
        ted.removePropChangeListener(_textEditorSelChangeLsnr, TextEditor.Selection_Prop);
        ted.setActive(false);
        _updatingSize = false; _updatingMinHeight = 0;
    }

    /**
     * Handle changes to Selected TextShape.RichText
     */
    protected void textAreaPropChanged(PropChange aPC)
    {
        // If in resetUI, just return
        if (isSendEventDisabled()) return;

        // Handle Selection change
        if (aPC.getPropertyName()==TextArea.Selection_Prop) {

            // Make sure TextShape is super-selected
            Editor editor = getEditor();
            SGText textShape = getSelView();
            if (textShape!=null && textShape!=editor.getSuperSelView())
                editor.setSuperSelView(textShape);

            // Make sure TextEditor has same selection
            TextEditor ted = getTextEditor();
            if (ted!=null)
                ted.setSel(_textArea.getSelStart(), _textArea.getSelEnd());
        }

        // Handle Focus change
        else if (aPC.getPropertyName()==TextArea.Focused_Prop) {
            TextEditor ted = getTextEditor();
            if (ted!=null && _textArea.isFocused())
                ted.setActive(false);
        }

        // If updating size, reset text width & height to accommodate text
        else if (_updatingSize) {

            // Get TextView
            SGText text = getSelView(); if (text==null) return;

            // Get preferred text shape width
            double maxWidth = _updatingMinHeight==0 ? text.getParent().getWidth() - text.getX() : text.getWidth();
            double prefWidth = text.getPrefWidth(); if (prefWidth>maxWidth) prefWidth = maxWidth;

            // If width gets updated, get & set desired width (make sure it doesn't go beyond page border)
            if (_updatingMinHeight==0)
                text.setWidth(prefWidth);

            // If PrefHeight or current height is greater than UpdatingMinHeight (which won't be zero if user drew a
            //  text box to enter text), set Height to PrefHeight
            double prefHeight = text.getPrefHeight();
            if (prefHeight>_updatingMinHeight || text.getHeight()>_updatingMinHeight)
                text.setHeight(Math.max(prefHeight, _updatingMinHeight));
        }
    }

    /**
     * Called when TextArea changes selection.
     */
    private void textEditorSelChanged()
    {
        // Get TextEditor and update sel from TextArea
        SGText textShape = getSelView(); if (textShape==null) return;
        TextEditor textEd = getTextEditor();
        if (textEd!=null)
            _textArea.setSel(textEd.getSelStart(), textEd.getSelEnd());

        // Repaint TextShape and ResetUI on MouseUp
        textShape.repaint();
        ViewUtils.runOnMouseUp(() -> getEditorPane().resetLater());
    }

    /**
     * Event hook during selection.
     */
    public boolean mousePressedSelection(ViewEvent anEvent)
    {
        // Iterator over selected shapes and see if any has an overflow indicator box that was hit
        List shapes = getEditor().getSelOrSuperSelViews();
        for (int i=0, iMax=shapes.size(); i<iMax; i++) { SGText text = (SGText)shapes.get(i);

            // If no linked text and not painting text indicator, just continue
            if (text.getLinkedText()==null && !isPaintingTextLinkIndicator(text)) continue;

            // Get point in text coords
            Point point = getEditor().convertToSceneView(anEvent.getX(), anEvent.getY(), text);

            // If pressed was in overflow indicator box, add linked text (or select existing one)
            if (point.x>=text.getWidth()-20 && point.x<=text.getWidth()-10 && point.y>=text.getHeight()-5) {
                if (text.getLinkedText()==null) sendEvent("LinkedTextMenuItem");   // If not linked text, add it
                else getEditor().setSelView(text.getLinkedText());          // Otherwise, select it
                return true;    // Return true so SelectTool goes to DragModeNone
            }
        }

        // Return false is mouse point wasn't in overflow indicator box
        return false;
    }

    /**
     * Overrides tool tooltip method to return text string if some chars aren't visible.
     */
    public String getToolTip(T aView, ViewEvent anEvent)
    {
        // If all text is visible and greater than 8 pt, return null
        if (aView.isAllTextVisible() && aView.getFont().getSize()>=8) return null;

        // Get text string (just return if empty), trim to 64 chars or less and return
        String string = aView.getText(); if (string==null || string.length()==0) return null;
        if (string.length()>64) string = string.substring(0,64) + "...";
        return string;
    }

    /**
     * Overrides Tool implementation to accept KeysPanel drags.
     */
    public boolean acceptsDrag(T aView, ViewEvent anEvent)
    {
        // If KeysPanel is dragging, return true
        Clipboard cb = anEvent.getClipboard();
        if(cb.hasString())
            return true;

        // Otherwise, return normal
        return super.acceptsDrag(aView, anEvent);
    }

    /**
     * Override normal implementation to handle KeysPanel drop.
     */
    public void dragDrop(T aView, ViewEvent anEvent)
    {
        // If a keys panel drop, add key to text
        Clipboard cb = anEvent.getClipboard();
        if(cb.hasString()) {
            String string = anEvent.getClipboard().getString();
            SGText text = aView;
            if (text.length()==0)
                text.setText(string);
            else text.getRichText().addChars(" " + string);
        }

        // Otherwise, do normal drop
        else super.dragDrop(aView, anEvent);
    }

    /** Sets the character spacing for the currently selected shapes. */
    private void setCharSpacing(float aValue)
    {
        setUndoTitle("Char Spacing Change");
        for (TextToolStyler styler : getSelOrSuperSelStylers())
            styler.setCharSpacing(aValue);
    }

    /** Sets the line spacing for all chars (or all selected chars, if editing). */
    private void setLineSpacing(float aHeight)
    {
        setUndoTitle("Line Spacing Change");
        for (TextToolStyler styler : getSelOrSuperSelStylers())
            styler.setLineSpacing(aHeight);
    }

    /** Sets the line gap for all chars (or all selected chars, if editing). */
    private void setLineGap(float aHeight)
    {
        setUndoTitle("Line Gap Change");
        for (TextToolStyler styler : getSelOrSuperSelStylers())
            styler.setLineGap(aHeight);
    }

    /** Set min line height for all chars (or all selected chars, if editing). */
    //private void setLineHeightMin(float aHeight) { setUndoTitle("Min Line Height Change");
    //    for (ToolStylerText styler : getSelOrSuperSelStylers()) styler.setLineHeightMin(aHeight); }
    /** Set max line height for all chars (or all selected chars, if editing). */
    //private void setLineHeightMax(float aHeight) { setUndoTitle("Max Line Height Change");
    //    for (ToolStylerText styler : getSelOrSuperSelStylers()) styler.setLineHeightMax(aHeight); }

    /**
     * Returns the ToolStylerText objects for editor selected shapes.
     */
    private TextToolStyler[] getSelOrSuperSelStylers()
    {
        List<SGView> shapes = getEditor().getSelOrSuperSelViews();
        TextToolStyler stylers[] = new TextToolStyler[shapes.size()];
        for (int i=0, iMax=shapes.size(); i<iMax; i++) stylers[i] = (TextToolStyler)getStyler(shapes.get(i));
        return stylers;
    }

    /**
     * Returns the shape class that this tool edits.
     */
    public Class getViewClass()  { return SGText.class; }

    /**
     * Returns the name of this tool to be displayed by inspector.
     */
    public String getWindowTitle()  { return "Text Inspector"; }

    /**
     * Returns the image used to represent shapes that this tool represents.
     */
    protected Image getImageImpl()  { return Image.get(TextTool.class, "TextTool.png"); }

    /**
     * A TextEditor subclass to repaint super-selected SGTextView when text selection changes.
     */
    private class TextShapeTextEditor extends TextEditor {
        @Override
        protected void repaintSel()
        {
            super.repaintSel();
            SGText text = getSelView();
            if (text!=null)
                text.repaint();
        }
    }

    /**
     * A CopyPaster for TextTool.
     */
    private class TextEditorCopyPaster implements CopyPaster {

        /** Override to forward to TextEditor. */
        public void cut()  { getTextEditor().cut(); }

        /** Override to forward to TextEditor. */
        public void copy()  { getTextEditor().copy(); }

        /** Override to forward to TextEditor. */
        public void paste()  { getTextEditor().paste(); }

        /** Override to forward to TextEditor. */
        public void delete()  { getTextEditor().delete(); }

        /** Override to forward to TextEditor. */
        public void selectAll()  { getTextEditor().selectAll(); }
    }
}
/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package rmdraw.app;
import rmdraw.shape.*;
import java.util.*;
import snap.geom.Point;
import snap.geom.Size;
import snap.gfx.Image;
import snap.util.ListUtils;
import snap.view.ViewUtils;

/**
 * Handles useful methods to help editor.
 */
public class EditorUtils {

/**
 * Groups the given shape list to the given group shape.
 * If given shapes list is null, use editor selected shapes.
 * If given group shape is null, create new generic group shape.
 */
public static void groupShapes(Editor anEditor, List <RMShape> theShapes, RMParentShape aGroupShape)
{
    // If shapes not provided, use editor selected shapes
    if(theShapes==null)
        theShapes = anEditor.getSelectedShapes();
    
    // If there are less than 2 selected shapes play a beep (the user really should know better)
    if(theShapes.size()==0) { anEditor.beep(); return; }
    
    // Set undo title
    anEditor.undoerSetUndoTitle("Group");

    // Get copy of shapes, sorted by their original index in parent
    List <RMShape> shapes = RMShapeUtils.getShapesSortedByIndex(theShapes);
    
    // Get parent
    RMParentShape parent = shapes.get(0).getParent();
    
    // If no group shape, create one
    if(aGroupShape==null) {
        aGroupShape = new RMSpringShape();
        aGroupShape.setBounds(RMShapeUtils.getBoundsOfChildren(parent, shapes));
    }

    // Add groupShape to the current parent (with no transform)
    parent.addChild(aGroupShape);

    // Iterate over children and group to GroupShape
    for(RMShape child : shapes)
        groupShape(child, aGroupShape);
    
    // Select group shape
    anEditor.setSelectedShape(aGroupShape);
}

/**
 * Adds child shape to group shape.
 */
private static void groupShape(RMShape child, RMParentShape gshape)
{
    // Get center point in parent coords and store as child x/y
    RMParentShape parent = child.getParent();
    Point cp = child.localToParent(child.getWidth()/2, child.getHeight()/2);
    child.setXY(cp.x, cp.y);
    
    // Move child to GroupShape
    parent.removeChild(child);
    gshape.addChild(child);
        
    // Undo transforms of group shape
    child.setRoll(child.getRoll() - gshape.getRoll());
    child.setScaleX(child.getScaleX()/gshape.getScaleX()); child.setScaleY(child.getScaleY()/gshape.getScaleY());
    child.setSkewX(child.getSkewX() - gshape.getSkewX()); child.setSkewY(child.getSkewY() - gshape.getSkewY());
    
    // Reset center point: Get old center point in GroupShape coords and offset child by new center in GroupShape coords
    cp = gshape.parentToLocal(cp.x, cp.y);
    Point cp2 = child.localToParent(child.getWidth()/2, child.getHeight()/2);
    child.offsetXY(cp.x - cp2.x, cp.y - cp2.y);
}

/**
 * Ungroups any currently selected group shapes.
 */
public static void ungroupShapes(Editor anEditor)
{
    // Get currently super selected shape and create list to hold ungrouped shapes
    List <RMShape> ungroupedShapes = new Vector();
    
    // Register undo title for ungrouping
    anEditor.undoerSetUndoTitle("Ungroup");

    // See if any of the selected shapes can be ungrouped
    for(RMShape shape : anEditor.getSelectedShapes()) {
        
        // If shape cann't be ungrouped, skip
        if(!anEditor.getToolForView(shape).isUngroupable(shape)) continue;
        RMParentShape groupShape = (RMParentShape)shape;
        RMParentShape parent = groupShape.getParent();
            
        // Iterate over children and ungroup from GroupShape
        for(RMShape child : groupShape.getChildArray()) {
            ungroupShape(child);
            ungroupedShapes.add(child);
        }

        // Remove groupShape from parent
        parent.removeChild(groupShape);
    }

    // If were some ungroupedShapes, select them (set selected objects for undo/redo)
    if(ungroupedShapes.size()>0)
        anEditor.setSelectedShapes(ungroupedShapes);

    // If no ungroupedShapes, beep at silly user
    else anEditor.beep();
}

/**
 * Transforms given shape to world coords.
 */
private static void ungroupShape(RMShape child)
{
    // Get center point in parent coords and store as child x/y
    RMParentShape gshape = child.getParent(), parent = gshape.getParent();
    Point cp = child.localToParent(child.getWidth()/2, child.getHeight()/2, parent);
    child.setXY(cp.x, cp.y);
    
    // Coalesce transforms up the parent chain
    child.setRoll(child.getRoll() + gshape.getRoll());
    child.setScaleX(child.getScaleX() * gshape.getScaleX()); child.setScaleY(child.getScaleY() * gshape.getScaleY());
    child.setSkewX(child.getSkewX() + gshape.getSkewX()); child.setSkewY(child.getSkewY() + gshape.getSkewY());

    // Remove from group shape & add to group shape parent
    gshape.removeChild(child);
    parent.addChild(child);
    
    // Reset center point: Get new center in parent coords and offset child by change
    Point cp2 = child.localToParent(child.getWidth()/2, child.getHeight()/2);
    child.offsetXY(cp.x - cp2.x, cp.y - cp2.y);
}

/**
 * Orders all currently selected shapes to the front.
 */
public static void bringToFront(Editor anEditor)
{
    RMParentShape parent = anEditor.getSuperSelectedParentShape();
    if(parent==null || anEditor.getSelectedShapeCount()==0) { anEditor.beep(); return; }
    anEditor.undoerSetUndoTitle("Bring to Front");
    parent.bringShapesToFront(anEditor.getSelectedShapes());
}

/**
 * Orders all currently selected shapes to the back.
 */
public static void sendToBack(Editor anEditor)
{
    RMParentShape parent = anEditor.getSuperSelectedParentShape();
    if(parent==null || anEditor.getSelectedShapeCount()==0) { anEditor.beep(); return; }
    anEditor.undoerSetUndoTitle("Send to Back");
    parent.sendShapesToBack(anEditor.getSelectedShapes());
}

/**
 * Arranges currently selected shapes in a row relative to their top.
 */
public static void makeRowTop(Editor anEditor)
{
    if(anEditor.getSelectedShapeCount()==0) { anEditor.beep(); return; }
    anEditor.undoerSetUndoTitle("Make Row Top");
    double minY = anEditor.getSelectedShape().getFrameY();
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setFrameY(minY);
}

/**
 * Arranges currently selected shapes in a row relative to their center.
 */
public static void makeRowCenter(Editor anEditor)
{
    if(anEditor.getSelectedShapeCount()==0) { anEditor.beep(); return; }
    anEditor.undoerSetUndoTitle("Make Row Center");
    double midY = anEditor.getSelectedShape().getFrame().getMidY();
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setFrameY(midY - shape.getHeight()/2);
}

/**
 * Arranges currently selected shapes in a row relative to their bottom.
 */
public static void makeRowBottom(Editor anEditor)
{
    if(anEditor.getSelectedShapeCount()==0) { anEditor.beep(); return; }
    anEditor.undoerSetUndoTitle("Make Row Bottom");
    double maxY = anEditor.getSelectedShape().getFrameMaxY();
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setFrameY(maxY - shape.getHeight());
}

/**
 * Arranges currently selected shapes in a column relative to their left border.
 */
public static void makeColumnLeft(Editor anEditor)
{
    if(anEditor.getSelectedShapeCount()==0) { anEditor.beep(); return; }
    anEditor.undoerSetUndoTitle("Make Column Left");
    double minX = anEditor.getSelectedShape().getFrameX();
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setFrameX(minX);
}

/**
 * Arranges currently selected shapes in a column relative to their center.
 */
public static void makeColumnCenter(Editor anEditor)
{
    if(anEditor.getSelectedShapeCount()==0) { anEditor.beep(); return; }
    anEditor.undoerSetUndoTitle("Make Column Center");
    double midX = anEditor.getSelectedShape().getFrame().getMidX();
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setFrameX(midX - shape.getWidth()/2);
}

/**
 * Arranges currently selected shapes in a column relative to their right border.
 */
public static void makeColumnRight(Editor anEditor)
{
    if(anEditor.getSelectedShapeCount()==0) { anEditor.beep(); return; }
    anEditor.undoerSetUndoTitle("Make Column Right");
    double maxX = anEditor.getSelectedShape().getFrameMaxX();    
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setFrameX(maxX - shape.getWidth());
}

/**
 * Makes currently selected shapes all have the same width and height as the first selected shape.
 */
public static void makeSameSize(Editor anEditor)
{
    if(anEditor.getSelectedShapeCount()==0) { anEditor.beep(); return; }
    anEditor.undoerSetUndoTitle("Make Same Size");
    Size size = anEditor.getSelectedShape().getSize();
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setSize(size.getWidth(), size.getHeight());
}

/**
 * Makes currently selected shapes all have the same width as the first selected shape.
 */
public static void makeSameWidth(Editor anEditor)
{
    // If no shapes, beep and return
    if(anEditor.getSelectedShapeCount()==0) { anEditor.beep(); return; }
    
    // Register undo title
    anEditor.undoerSetUndoTitle("Make Same Width");
    
    // Get first selected shape width
    double width = anEditor.getSelectedShape().getWidth();
    
    // Iterate over selected shapes and set width
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setWidth(width);
}

/**
 * Makes currently selected shapes all have the same height as the first selected shape.
 */
public static void makeSameHeight(Editor anEditor)
{
    // If no shapes, beep and return
    if(anEditor.getSelectedShapeCount()==0) { anEditor.beep(); return; }
    
    // Register undo title
    anEditor.undoerSetUndoTitle("Make Same Height");
    
    // Get first selected shape height
    double height = anEditor.getSelectedShape().getHeight();
    
    // Iterate over selected shapes and set height
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setHeight(height);
}

/**
 * Makes currently selected shapes size to fit content.
 */
public static void setSizeToFit(Editor anEditor)
{
    // If no shapes, beep and return
    if(anEditor.getSelectedShapeCount()==0) { anEditor.beep(); return; }
    
    // Register undo title
    anEditor.undoerSetUndoTitle("Size to Fit");
    
    // Iterate over shapes and size to fit
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setBestSize();
}

/**
 * Arranges currently selected shapes such that they have the same horizontal distance between them.
 */
public static void equallySpaceRow(Editor anEditor)
{
    // If no selected shapes, beep and return
    if(anEditor.getSelectedShapeCount()==0) { anEditor.beep(); return; }
    
    // Get selectedShapes sorted by minXInParentBounds
    List <RMShape> shapes = RMShapeUtils.getShapesSortedByFrameX(anEditor.getSelectedShapes());
    float spaceBetweenShapes = 0;

    // Calculate average space between shapes
    for(int i=1, iMax=shapes.size(); i<iMax; i++)
        spaceBetweenShapes += shapes.get(i).getFrameX() - shapes.get(i-1).getFrameMaxX();
    if(shapes.size()>1)
        spaceBetweenShapes = spaceBetweenShapes/(shapes.size()-1);
    
    // Reset average space between shapes
    anEditor.undoerSetUndoTitle("Equally Space Row");
    for(int i=1, iMax=shapes.size(); i<iMax; i++) {
        RMShape shape = shapes.get(i);
        RMShape lastShape = shapes.get(i-1);
        double tx = lastShape.getFrameMaxX() + spaceBetweenShapes;
        shape.setFrameX(tx);
    }
}

/**
 * Arranges currently selected shapes such that they have the same vertical distance between them.
 */
public static void equallySpaceColumn(Editor anEditor)
{
    // If no selected shapes, beep and return
    if(anEditor.getSelectedShapeCount()==0) { anEditor.beep(); return; }
    
    // Get selectedShapes sorted by minXInParentBounds
    List <RMShape> shapes = RMShapeUtils.getShapesSortedByFrameY(anEditor.getSelectedShapes());
    float spaceBetweenShapes = 0;

    // Calculate average space between shapes
    for(int i=1, iMax=shapes.size(); i<iMax; i++)
        spaceBetweenShapes += shapes.get(i).getFrameY() - shapes.get(i-1).getFrameMaxY();
    if(shapes.size()>1)
        spaceBetweenShapes = spaceBetweenShapes/(shapes.size()-1);

    // Reset average space between shapes
    anEditor.undoerSetUndoTitle("Equally Space Column");
    for(int i=1, iMax=shapes.size(); i<iMax; i++) {
        RMShape shape = shapes.get(i);
        RMShape lastShape = shapes.get(i-1);
        double ty = lastShape.getFrameMaxY() + spaceBetweenShapes;
        shape.setFrameY(ty);
    }
}

/**
 * Adds the selected shapes to a Scene3D Shape.
 */
public static void groupInScene3D(Editor anEditor)
{
    // If no shapes, beep and return
    if(anEditor.getSelectedShapeCount()==0) { anEditor.beep(); return; }
    
    // Get selected shapes
    List <RMShape> selectedShapes = ListUtils.clone(anEditor.getSelectedShapes());
    
    // Get parent
    RMParentShape parent = anEditor.getSelectedShape(0).getParent();
    
    // Get new Scene3D to group selected shapes in
    RMScene3D groupShape = new RMScene3D();
    
    // Set scene3D to combined bounds of children
    groupShape.setFrame(RMShapeUtils.getBoundsOfChildren(parent, selectedShapes));

    // Set undo title
    anEditor.undoerSetUndoTitle("Group in Scene3D");
    
    // Iterate over children and add to group shape
    for(int i=0, iMax=selectedShapes.size(); i<iMax; i++) {
        RMShape shape = selectedShapes.get(i);
        groupShape.addShapeRM(shape);
        shape.removeFromParent();
        shape.setXY(shape.x() - groupShape.x(), shape.y() - groupShape.y());
    }
    
    // Add group shape to original parent
    parent.addChild(groupShape);
    
    // Select new shape
    anEditor.setSelectedShape(groupShape);
}

/**
 * Create new shape by coalescing the outer perimeters of the currently selected shapes.
 */
public static void combinePaths(Editor anEditor)
{
    // If shapes less than 2, just beep and return
    if(anEditor.getSelectedShapeCount()<2) { anEditor.beep(); return; }
    
    // Get selected shapes and create CombinedShape
    List <RMShape> selectedShapes = ListUtils.clone(anEditor.getSelectedShapes());
    RMPolygonShape combinedShape = RMShapeUtils.getCombinedPathsShape(selectedShapes);
    
    // Remove original children and replace with CombinedShape
    anEditor.undoerSetUndoTitle("Add Paths");
    RMParentShape parent = anEditor.getSuperSelectedParentShape();
    for(RMShape shape : selectedShapes) parent.removeChild(shape);
    parent.addChild(combinedShape);
    
    // Select CombinedShape
    anEditor.setSelectedShape(combinedShape);
}

/**
 * Create new shape by coalescing the outer perimeters of the currently selected shapes.
 */
public static void subtractPaths(Editor anEditor)
{
    // If shapes less than 2, just beep and return
    if(anEditor.getSelectedShapeCount()<2) { anEditor.beep(); return; }
    
    // Get selected shapes and create SubtractedShape
    List <RMShape> selectedShapes = ListUtils.clone(anEditor.getSelectedShapes());
    RMPolygonShape subtractedShape = RMShapeUtils.getSubtractedPathsShape(selectedShapes, 0);
    
    // Remove original children and replace with SubtractedShape
    anEditor.undoerSetUndoTitle("Subtract Paths");
    RMParentShape parent = anEditor.getSuperSelectedParentShape();
    for(RMShape shape : selectedShapes) parent.removeChild(shape);
    parent.addChild(subtractedShape);
    
    // Select SubtractedShape
    anEditor.setSelectedShape(subtractedShape);
}

/**
 * Converts currently selected shape to image.
 */
public static void convertToImage(Editor anEditor)
{
    // Get currently selected shape (if shape is null, just return)
    RMShape shape = anEditor.getSelectedShape(); if(shape==null) return;
    
    // Get image for shape, get PNG bytes for image and create new RMImageShape for bytes
    Image image = RMShapeUtils.createImage(shape, null);
    byte imageBytes[] = image.getBytesPNG();
    RMImageShape imageShape = new RMImageShape(imageBytes);
    
    // Set ImageShape XY and add to parent
    imageShape.setXY(shape.getX() + shape.getBoundsMarked().getX(), shape.getY() + shape.getBoundsMarked().getY());
    shape.getParent().addChild(imageShape, shape.indexOf());
    
    // Replace old selectedShape with image and remove original shape
    anEditor.setSelectedShape(imageShape);
    shape.removeFromParent();
}

/**
 * Moves all the currently selected shapes one point to the right.
 */
public static void moveRightOnePoint(Editor anEditor)
{
    anEditor.undoerSetUndoTitle("Move Right One Point");
    RMDocument doc = anEditor.getDoc();
    double offset = doc.isSnapGrid()? doc.getGridSpacing() : 1;
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setFrameX(shape.getFrameX() + offset);
}

/**
 * Moves all the currently selected shapes one point to the left.
 */
public static void moveLeftOnePoint(Editor anEditor)
{
    anEditor.undoerSetUndoTitle("Move Left One Point");
    RMDocument doc = anEditor.getDoc();
    double offset = doc.isSnapGrid()? doc.getGridSpacing() : 1;
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setFrameX(shape.getFrameX() - offset);
}

/**
 * Moves all the currently selected shapes one point up.
 */
public static void moveUpOnePoint(Editor anEditor)
{
    anEditor.undoerSetUndoTitle("Move Up One Point");
    RMDocument doc = anEditor.getDoc();
    double offset = doc.isSnapGrid()? doc.getGridSpacing() : 1;
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setFrameY(shape.getFrameY() - offset);
}

/**
 * Moves all the currently selected shapes one point down.
 */
public static void moveDownOnePoint(Editor anEditor)
{
    anEditor.undoerSetUndoTitle("Move Down One Point");
    RMDocument doc = anEditor.getDoc();
    double offset = doc.isSnapGrid()? doc.getGridSpacing() : 1;
    for(RMShape shape : anEditor.getSelectedShapes())
        shape.setFrameY(shape.getFrameY() + offset);
}

/**
 * Moves all the currently selected shapes to a new page layer.
 */
public static void moveToNewLayer(Editor anEditor)
{
    RMDocument doc = anEditor.getDoc();
    if(anEditor.getSelectedShapeCount()==0 || doc==null) { anEditor.beep(); return; }
    doc.getSelPage().moveToNewLayer(anEditor.getSelectedShapes());
}

/**
 * Splits the selected shape in half on the horizontal axis.
 */
public static void splitHorizontal(Editor editor)
{
    editor.undoerSetUndoTitle("Split Column");
    RMShape shape = editor.getSuperSelectedShape();
    RMParentShape parent = shape.getParent();
    shape.repaint();
    shape = shape.divideShapeFromLeft(shape.getWidth()/2);
    parent.addChild(shape);
    editor.setSuperSelectedShape(shape);
}

/**
 * Adds an image placeholder to the given editor.
 */
public static void addImagePlaceholder(Editor anEditor)
{
    // Create image shape
    RMImageShape imageShape = new RMImageShape(null);
    
    // Get parent and move image shape to center
    RMParentShape parent = anEditor.firstSuperSelectedShapeThatAcceptsChildren();
    imageShape.setFrame((int)(parent.getWidth()/2 - 24), (int)(parent.getHeight()/2 - 24), 48, 48);

    // Set image in image shape and add imageShape to mainShape
    anEditor.undoerSetUndoTitle("Add Image");
    parent.addChild(imageShape);

    // Select imageShape, set selectTool and redisplay
    anEditor.setSelectedShape(imageShape);
    anEditor.setCurrentToolToSelectTool();
    anEditor.repaint();
}

/**
 * Check spelling for given editor.
 */
public static void checkSpelling(Editor anEditor)
{
    //new EditorSpellCheck(anEditor).show(anEditor);
    ViewUtils.beep();
}

///**
// * A SpellCheckPanel for Editor.
// */
//private static class EditorSpellCheck extends SpellCheckPanel {
//
//    // The Editor
//    private Editor _editor;
//
//    // The TextShape being edited
//    private RMTextShape _workingText;
//
//    /** Create EditorSpellCheck. */
//    EditorSpellCheck(Editor anEditor)  { _editor = anEditor; }
//
//    @Override
//    protected String getText()
//    {
//        // Get editor and selected shape
//        RMShape shape = _editor.getSelectedOrSuperSelectedShape();
//
//        // If shape has changed do the right thing
//        if(shape!=_workingText) {
//
//            // If new shape is text, make it the working text
//            if(shape instanceof RMTextShape)
//                _workingText = (RMTextShape)shape;
//
//            // If new shape isn't text, but is on same page as previous workingText, select previous working text
//            else if(_workingText!=null && shape.getPageShape()==_workingText.getPageShape()) {
//            }
//
//            // Otherwise, set workingText to null
//            else _workingText = null;
//        }
//
//        // Make sure working text is superselected
//        if(_workingText!=null && _workingText!=_editor.getSuperSelectedShape()) {
//            _editor.setSuperSelectedShape(_workingText);
//            _editor.getTextEditor().setSel(0);
//        }
//
//        // Return working text
//        return _workingText!=null ? _workingText.getText() : null;
//    }
//
//    @Override
//    protected TextEditor getTextEditor()
//    {
//        return _editor.getTextEditor();
//    }
//}

}
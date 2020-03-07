package rmdraw.app;

import snap.view.ViewEvent;

/**
 * Interface for anything that can handle standard drag/drop operations.
 *
 * The full set of operations is dragEnter, dragOver, dragExit, dragDrop.
 */
public interface DragDropper {

    /**
     * Called when drag enters view.
     */
    void dragEnter(ViewEvent anEvent);

    /**
     * Called when drag moves while over view.
     */
    void dragOver(ViewEvent anEvent);

    /**
     * Called when drag leaves view.
     */
    void dragExit(ViewEvent anEvent);

    /**
     * Called when drag released while over view.
     */
    void dragDrop(ViewEvent anEvent);

    /**
     * Dispatches an event to given dropper.
     */
    public static void dispatchDragEvent(DragDropper aDragDropper, ViewEvent anEvent)
    {
        switch(anEvent.getType()) {
            case DragEnter: aDragDropper.dragEnter(anEvent); break;
            case DragOver: aDragDropper.dragOver(anEvent); break;
            case DragExit: aDragDropper.dragExit(anEvent); break;
            case DragDrop: aDragDropper.dragDrop(anEvent); break;
            default: throw new RuntimeException("DragDropper.dispatchDragEvent: Unknown event type: " + anEvent.getType());
            //case DragActionChanged: anEvent.acceptDrag(DnDConstants.ACTION_COPY);
        }
    }
}

package rmdraw.app;

/**
 * Interface for anything that can handle standard copy/paste operations.
 *
 * The full set of operations is cut, copy, paste, delete, select all.
 */
public interface CopyPaster {

    /**
     * Standard copy to clipboard.
     */
    void copy();

    /**
     * Standard paste from clipboard.
     */
    void paste();

    /**
     * Standard cut.
     */
    default void cut()
    {
        copy();
        delete();
    }

    /**
     * Standard delete.
     */
    void delete();

    /**
     * Standard select all.
     */
    void selectAll();
}

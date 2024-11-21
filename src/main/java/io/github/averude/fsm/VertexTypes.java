package io.github.averude.fsm;

import java.util.NoSuchElementException;

/**
 * Enumeration defining the types of vertices that can exist within a finite state machine graph.
 * Each vertex type determines how transitions (edges) are handled when interacting with the related nodes.
 */
public enum VertexTypes {
    /**
     * Represents a basic vertex type.
     * In this mode, transitions explicitly defined between nodes are recognized.
     * Attempting to follow a transition (edge) that is not defined throws a {@link NoSuchElementException}.
     */
    BASIC,

    /**
     * Represents a looped vertex type.
     * This type adds a unique behavior where if a specified transition (edge) does not exist,
     * the node resolves to loop back to itself. This effectively implements a self-loop for
     * any undefined transition, preventing {@link NoSuchElementException}.
     */
    LOOPED
}

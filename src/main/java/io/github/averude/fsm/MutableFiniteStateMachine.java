package io.github.averude.fsm;

import java.util.NoSuchElementException;

/**
 * An interface representing a mutable finite state machine (FSM) which extends the capabilities of a regular finite state machine
 * by allowing modification of its states and transitions. States are represented as vertices, and transitions as directed edges
 * between these vertices.
 * <p>
 * Implementations of this interface are expected to support dynamic changes including addition, modification, and removal
 * of both states and transitions. This interface provides methods to manipulate the graph structure, thus enabling
 * the representation and manipulation of complex state-driven processes.
 * <p>
 * Error handling is thoroughly enforced, with various exceptions thrown to handle invalid operations such as adding
 * duplicate states, inserting edges where vertices do not exist, or removing non-existent components.
 */
public interface MutableFiniteStateMachine<E, V> extends FiniteStateMachine<E, V> {
    /**
     * Adds a vertex representing a state to the finite state machine graph with a default vertex type.
     * If the vertex already exists in the graph, this method will throw an {@link IllegalArgumentException}.
     *
     * @param vertex the vertex to add to the graph; must not be null.
     * @throws NullPointerException     if the vertex parameter is null.
     * @throws IllegalArgumentException if the vertex already exists in the graph.
     */
    void addVertex(V vertex);

    /**
     * Adds a vertex representing a state to the finite state machine graph with a specified vertex type.
     * If the vertex already exists in the graph, this method will throw an {@link IllegalArgumentException}.
     *
     * @param vertex     the vertex to add to the graph; must not be null.
     * @param vertexType the type to assign to the vertex; must not be null.
     * @throws NullPointerException     if either the vertex or vertexType parameter is null.
     * @throws IllegalArgumentException if the vertex already exists in the graph.
     */
    void addVertex(V vertex, VertexTypes vertexType);

    /**
     * Removes a vertex from the finite state machine graph, along with any edges (transitions) connected to it.
     * This method will throw a {@link NoSuchElementException} if the vertex does not exist in the graph.
     *
     * @param vertex the vertex to remove from the graph; must not be null.
     * @throws NullPointerException   if the vertex is null.
     * @throws NoSuchElementException if the vertex does not exist in the graph. This exception is thrown to prevent further
     *                                invalid operations and to notify the user about the absence of the specified vertex.
     */
    void removeVertex(V vertex);

    /**
     * Adds a single transition (directed edge) between two vertices in the finite state machine graph.
     * This method checks if a similar transition already exists, in which case an {@link IllegalArgumentException} is thrown.
     *
     * @param from the starting vertex of the transition; must not be null.
     * @param to   the ending vertex of the transition; must not be null.
     * @param edge the edge representing the transition; must not be null.
     * @throws NullPointerException     if any of the parameters are null.
     * @throws IllegalArgumentException if the transition already exists between the given vertices.
     * @throws NoSuchElementException   if either 'from' or 'to' vertices do not exist in the graph.
     */
    void addTransition(V from, V to, E edge);

    /**
     * Adds multiple transitions (directed edges) between two vertices using a collection of edges
     * in the finite state machine graph.
     * Each transition is checked individually, and an {@link IllegalArgumentException} is thrown
     * if any identical transition already exists.
     *
     * @param from  the starting vertex for all transitions; must not be null.
     * @param to    the ending vertex for all transitions; must not be null.
     * @param edges the iterable collection of edges representing multiple transitions; must not be null.
     * @throws NullPointerException     if any of the parameters are null.
     * @throws IllegalArgumentException if any transition already exists between 'from' and 'to' using any edge from 'edges'.
     * @throws NoSuchElementException   if either 'from' or 'to' vertices do not exist in the graph.
     */
    void addTransitions(V from, V to, Iterable<E> edges);

    /**
     * Removes a specified transition (directed edge) between two vertices in the finite state machine graph.
     * Throws a {@link NoSuchElementException} if the transition does not exist.
     *
     * @param from the starting vertex of the transition; must not be null.
     * @param to   the ending vertex of the transition; must not be null.
     * @param edge the edge representing the transition that should be removed; must not be null.
     * @throws NullPointerException   if any of the parameters are null.
     * @throws NoSuchElementException if the specified transition does not exist, or if 'from' or 'to'
     *                                vertices do not exist in the graph.
     */
    void removeTransition(V from, V to, E edge);

    /**
     * Clears all vertices and transitions from the finite state machine graph.
     * After this call, the graph will be empty.
     */
    void clear();
}

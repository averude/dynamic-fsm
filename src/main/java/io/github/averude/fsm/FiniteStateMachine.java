package io.github.averude.fsm;

import java.util.NoSuchElementException;

/**
 * An interface representing a finite state machine (FSM) which models a collection of states
 * and transitions between these states.
 * States are represented by vertices and transitions by directed edges between these vertices.
 * This interface allows querying the graph structure and navigating through the states according to defined transitions.
 * <p>
 * Implementations of this interface should ensure accurate maintenance of state transitions and should manage
 * vertices' presence and connectivity in a coherent and reliable manner.
 * <p>
 * The interface extends Iterable<V> to allow iterating over vertices.
 */
public interface FiniteStateMachine<E, V> extends Iterable<V> {
    /**
     * Checks whether a given vertex exists in the finite state machine graph.
     *
     * @param vertex the vertex to check in the graph; must not be null.
     * @return true if the vertex exists, false otherwise.
     * @throws NullPointerException if the vertex parameter is null.
     */
    boolean hasVertex(V vertex);

    /**
     * Retrieves the type of given vertex in the finite state machine graph.
     * This method will throw a {@link NoSuchElementException} if the vertex does not exist in the graph.
     *
     * @param vertex the vertex whose type is to be retrieved; must not be null.
     * @return the {@link VertexTypes} enum value representing the type of the vertex.
     * @throws NullPointerException   if the vertex is null.
     * @throws NoSuchElementException if the vertex does not exist in the graph.
     */
    VertexTypes getVertexType(V vertex);

    /**
     * Checks if a specific transition (directed edge) exists between two vertices in the graph.
     *
     * @param from the starting vertex of the transition; must not be null.
     * @param to   the ending vertex of the transition; must not be null.
     * @param edge the edge representing the transition; must not be null.
     * @return true if the transition exists, false otherwise.
     * @throws NullPointerException   if any of the parameters are null.
     * @throws NoSuchElementException if either 'from' or 'to' vertices do not exist in the graph.
     */
    boolean hasTransition(V from, V to, E edge);

    /**
     * Checks if any transition exists between two vertices, ignoring the specific edges.
     *
     * @param from the starting vertex of transitions; must not be null.
     * @param to   the ending vertex of transitions; must not be null.
     * @return true if any transition exists between the specified vertices, false otherwise.
     * @throws NullPointerException   if either 'from' or 'to' is null.
     * @throws NoSuchElementException if either 'from' or 'to' vertices do not exist in the graph.
     */
    boolean hasAnyTransition(V from, V to);

    /**
     * Traverses the graph starting from a given vertex and following a sequence of edges provided.
     * Each vertex type may define specific behavior for how to process transitions based on the edges.
     * Refer to the documentation of {@link VertexTypes} for details on how each type handles transitions.
     *
     * @param from  the starting vertex from where the traversal begins; must not be null.
     * @param edges the iterable collection of edges to follow in order; must not be null.
     * @return final vertex state after traversal is complete.
     * @throws NullPointerException   if either 'from' or 'edges' is null.
     * @throws NoSuchElementException if the starting vertex does not exist
     *                                or if any transition defined by 'edges' does not exist.
     * @throws IllegalStateException  if transition behavior is invalid based on vertex type processing rules.
     */
    V traverse(V from, Iterable<E> edges);

    /**
     * Returns the number of vertices currently in the finite state machine graph.
     *
     * @return the number of vertices in the graph.
     */
    int size();
}

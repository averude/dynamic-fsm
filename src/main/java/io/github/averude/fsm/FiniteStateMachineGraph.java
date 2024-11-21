package io.github.averude.fsm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A class that implements a mutable finite state machine (FSM) graph data structure.
 * This graph efficiently manages states as vertices and state transitions as directed edges between these vertices.
 * Each vertex and edge can be dynamically added or removed from the FSM, giving flexibility in graph manipulation.
 * This implementation also supports assigning distinct types to vertices, enhancing behavioral categorization during processing.
 * <p>
 * Important: The type of edges (E) needs to implement {@code equals} and {@code hashCode} methods correctly.
 * These implementations are critical for ensuring proper functionality of edge-based operations,
 * such as detecting duplicate edges and managing collections of edges efficiently.
 * <p>
 * The graph is not thread-safe; external synchronization is necessary for concurrent modifications.
 * Special attention should be given to maintaining graph integrity under modifications, especially in dense graphs
 * where each vertex might be connected to many others, increasing the complexity of operations such as vertex removal.
 * <p>
 * Performance Considerations:
 * - The typical operations such as adding and removing vertices and edges are efficient. However, the performance may degrade
 * under worse-case scenarios such as with dense graphs where a vertex might have connections with a large portion of the graph.
 * Specifically, the {@code removeVertex(V vertex)} method can become particularly costly in terms of time complexity,
 * potentially reaching up to O(n^2) in dense graphs where `n` is the number of vertices. This worse-case scenario occurs
 * because removing a vertex requires inspecting and possibly updating the adjacency information of each other vertex
 * in the graph.
 * <p>
 *
 * @param <E> the type of edges representing transitions between vertices; must implement {@code equals}
 *            and {@code hashCode} methods correctly.
 * @param <V> the type of vertices representing states in the finite state machine.
 */
public class FiniteStateMachineGraph<E, V> implements MutableFiniteStateMachine<E, V> {
    private static final NodeFactory NODE_FACTORY = new NodeFactory();

    private final VertexTypes defaultVertexType;
    private final Map<V, Node<E, V>> nodesMap = new HashMap<>();

    /**
     * Constructs a new FiniteStateMachineGraph instance with 'BASIC' as the default vertex type.
     * All vertices added to the FSM without a specified type will assume this default type.
     */
    public FiniteStateMachineGraph() {
        this(VertexTypes.BASIC);
    }

    /**
     * Constructs a new FiniteStateMachineGraph instance with a specified default vertex type.
     * All vertices added to the FSM without a specified type will assume this default type.
     *
     * @param defaultVertexType the default vertex type for vertices added without a specified type.
     */
    public FiniteStateMachineGraph(VertexTypes defaultVertexType) {
        this.defaultVertexType = defaultVertexType;
    }

    /**
     * Adds a vertex representing a state to the finite state machine graph with a default vertex type.
     * If the vertex already exists in the graph, this method will throw an {@link IllegalArgumentException}.
     *
     * @param vertex the vertex to add to the graph; must not be null.
     * @throws NullPointerException     if the vertex parameter is null.
     * @throws IllegalArgumentException if the vertex already exists in the graph.
     */
    @Override
    public void addVertex(V vertex) {
        addVertex(vertex, defaultVertexType);
    }

    /**
     * Adds a vertex representing a state to the finite state machine graph with a specified vertex type.
     * If the vertex already exists in the graph, this method will throw an {@link IllegalArgumentException}.
     *
     * @param vertex     the vertex to add to the graph; must not be null.
     * @param vertexType the type to assign to the vertex; must not be null.
     * @throws NullPointerException     if either the vertex or vertexType parameter is null.
     * @throws IllegalArgumentException if the vertex already exists in the graph.
     */
    @Override
    public void addVertex(V vertex, VertexTypes vertexType) {
        Objects.requireNonNull(vertex, "vertex cannot be null");
        Objects.requireNonNull(vertexType, "vertex type cannot be null");

        if (nodesMap.containsKey(vertex)) {
            throw new IllegalArgumentException("Vertex " + vertex + " is already added to the graph");
        }

        Node<E, V> node = NODE_FACTORY.createNode(vertex, vertexType);
        nodesMap.put(vertex, node);
    }

    /**
     * Removes a vertex from the finite state machine graph, along with any edges (transitions) connected to it.
     * This method will throw a {@link NoSuchElementException} if the vertex does not exist in the graph.
     *
     * @param vertex the vertex to remove from the graph; must not be null.
     * @throws NullPointerException   if the vertex is null.
     * @throws NoSuchElementException if the vertex does not exist in the graph. This exception is thrown to prevent further
     *                                invalid operations and to notify the user about the absence of the specified vertex.
     */
    @Override
    public void removeVertex(V vertex) {
        Node<E, V> node = getNode(vertex);

        // cleanup of edges which point to this vertex
        for (Map.Entry<V, Node<E, V>> entry : nodesMap.entrySet()) {
            if (!Objects.equals(entry.getKey(), vertex) && entry.getValue().getChildCount() != 0) {
                Node<E, V> evNode = entry.getValue();
                evNode.removeChild(node);
            }
        }

        // remove from map
        nodesMap.remove(vertex);
    }

    /**
     * Checks whether a given vertex exists in the finite state machine graph.
     *
     * @param vertex the vertex to check in the graph; must not be null.
     * @return true if the vertex exists, false otherwise.
     * @throws NullPointerException if the vertex parameter is null.
     */
    @Override
    public boolean hasVertex(V vertex) {
        Objects.requireNonNull(vertex);
        return nodesMap.containsKey(vertex);
    }

    /**
     * Retrieves the type of given vertex in the finite state machine graph.
     * This method will throw a {@link NoSuchElementException} if the vertex does not exist in the graph.
     *
     * @param vertex the vertex whose type is to be retrieved; must not be null.
     * @return the {@link VertexTypes} enum value representing the type of the vertex.
     * @throws NullPointerException   if the vertex is null.
     * @throws NoSuchElementException if the vertex does not exist in the graph.
     */
    @Override
    public VertexTypes getVertexType(V vertex) {
        return getNode(vertex).getType();
    }

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
    @Override
    public void addTransition(V from, V to, E edge) {
        validateParameters(from, to, edge);

        Node<E, V> fromNode = getNode(from);
        if (fromNode.hasChild(edge)) {
            throw new IllegalArgumentException("Transition already exists from: [%s] on edge: [%s]".formatted(from, edge));
        }

        Node<E, V> toNode = getNode(to);
        fromNode.addChild(edge, toNode);
    }

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
    @Override
    public void addTransitions(V from, V to, Iterable<E> edges) {
        validateParameters(from, to);
        Objects.requireNonNull(edges, "edges cannot be null");

        Node<E, V> fromNode = getNode(from);
        Node<E, V> toNode = getNode(to);

        for (E edge : edges) {
            if (fromNode.hasChild(edge)) {
                throw new IllegalArgumentException("Transition already exists from: [%s] on edge: [%s]".formatted(from, edge));
            }

            fromNode.addChild(edge, toNode);
        }
    }

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
    @Override
    public boolean hasTransition(V from, V to, E edge) {
        validateParameters(from, to, edge);

        Node<E, V> fromNode = getNode(from);
        Node<E, V> toNode = getNode(to);

        return fromNode.hasChild(edge) && fromNode.getChild(edge).equals(toNode);
    }

    /**
     * Checks if any transition exists between two vertices, ignoring the specific edges.
     *
     * @param from the starting vertex of transitions; must not be null.
     * @param to   the ending vertex of transitions; must not be null.
     * @return true if any transition exists between the specified vertices, false otherwise.
     * @throws NullPointerException   if either 'from' or 'to' is null.
     * @throws NoSuchElementException if either 'from' or 'to' vertices do not exist in the graph.
     */
    @Override
    public boolean hasAnyTransition(V from, V to) {
        validateParameters(from, to);

        Node<E, V> fromNode = getNode(from);
        Node<E, V> toNode = getNode(to);

        return fromNode.getChildren().contains(toNode);
    }

    /**
     * Removes a specified transition (directed edge) between two vertices in the finite state machine graph.
     * Throws a {@link NoSuchElementException} if the transition does not exist.
     *
     * @param from the starting vertex of the transition; must not be null.
     * @param to   the ending vertex of the transition; must not be null.
     * @param edge the edge representing the transition that should be removed; must not be null.
     * @throws NullPointerException   if any of the parameters are null.
     * @throws NoSuchElementException if the specified transition does not exist,
     *                                or if 'from' or 'to' vertices do not exist in the graph.
     */
    @Override
    public void removeTransition(V from, V to, E edge) {
        if (!hasTransition(from, to, edge)) {
            throw new NoSuchElementException("Transition not found from [%s] to [%s] on edge [%s]".formatted(from, to, edge));
        }

        Node<E, V> fromNode = getNode(from);
        fromNode.removeChild(edge);
    }

    private void validateParameters(V from, V to, E edge) {
        Objects.requireNonNull(from, "from vertex cannot be null");
        Objects.requireNonNull(to, "to vertex cannot be null");
        Objects.requireNonNull(edge, "edge cannot be null");
    }

    private void validateParameters(V from, V to) {
        Objects.requireNonNull(from, "from vertex cannot be null");
        Objects.requireNonNull(to, "to vertex cannot be null");
    }

    private Node<E, V> getNode(V vertex) {
        Node<E, V> node = nodesMap.get(vertex);
        if (node == null) {
            throw new NoSuchElementException("Graph does not contain the vertex: [%s]".formatted(vertex));
        }
        return node;
    }

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
    @Override
    public V traverse(V from, Iterable<E> edges) {
        Node<E, V> startNode = getNode(from);
        return traverse(startNode, edges);
    }

    /**
     * Internal method to traverse through the graph using a starting node and a sequence of edges.
     * Handles the transition based on the node type which might alter the behavior dynamically during traversal.
     *
     * @param start the node from which traversal begins; must not be null.
     * @param edges the iterable collection of edges to traverse; must not be null.
     * @return final vertex state reached after the traversal.
     * @throws NullPointerException   if any of the parameters are null.
     * @throws NoSuchElementException if an edge does not point to any next node.
     * @throws IllegalStateException  if any transition behavior is inconsistent due to node type.
     */
    private V traverse(Node<E, V> start, Iterable<E> edges) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(edges);

        Node<E, V> current = start;
        for (E edge : edges) {
            current = current.getChild(edge);
        }
        return current.getValue();
    }

    /**
     * Returns the number of vertices currently in the finite state machine graph.
     *
     * @return the number of vertices in the graph.
     */
    @Override
    public int size() {
        return nodesMap.size();
    }

    /**
     * Clears all vertices and transitions from the finite state machine graph.
     * After this call, the graph will be empty.
     */
    @Override
    public void clear() {
        nodesMap.clear();
    }

    /**
     * Provides an iterator over the vertices in the finite state machine graph.
     * This iterator allows for traversing each vertex in the graph. The standard {@code remove()} operation
     * is not supported by this iterator and will throw an {@code UnsupportedOperationException} if called.
     * <p>
     * The iterator fails fast on concurrent modification. It throws a {@code ConcurrentModificationException}
     * when it detects that the graph has been modified during iteration outside of this iterator's own remove method.
     *
     * @return an Iterator over the vertices of the graph.
     * @throws NoSuchElementException if there are no more elements to iterate and {@code next()} is called.
     */
    @Override
    public Iterator<V> iterator() {
        return new Iterator<>() {
            private final Iterator<V> mapIterator = nodesMap.keySet().iterator();

            @Override
            public boolean hasNext() {
                return mapIterator.hasNext();
            }

            @Override
            public V next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No more elements in the Graph");
                }
                return mapIterator.next();
            }
        };
    }
}
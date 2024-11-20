package io.github.averude.fsm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public class FiniteStateMachineGraph<E, V> implements MutableFiniteStateMachine<E, V> {
    private static final NodeFactory NODE_FACTORY = new NodeFactory();

    private final VertexTypes defaultVertexType;
    private final Map<V, Node<E, V>> nodesMap = new HashMap<>();

    public FiniteStateMachineGraph() {
        this(VertexTypes.BASIC);
    }

    public FiniteStateMachineGraph(VertexTypes defaultVertexType) {
        this.defaultVertexType = defaultVertexType;
    }

    @Override
    public void addVertex(V vertex) {
        addVertex(vertex, defaultVertexType);
    }

    @Override
    public void addVertex(V vertex, VertexTypes vertexType) {
        Objects.requireNonNull(vertex, "vertex cannot be null");
        Objects.requireNonNull(vertexType, "vertex type cannot be null");
        Node<E, V> node = NODE_FACTORY.createNode(vertex, vertexType);
        nodesMap.put(vertex, node);
    }

    @Override
    public void removeVertex(V vertex) {
        Node<E, V> node = getNode(vertex);

        // cleanup of edges which point to this vertex
        for (Map.Entry<V, Node<E, V>> entry : nodesMap.entrySet()) {
            if (!Objects.equals(entry.getKey(), vertex)
                    && entry.getValue().getChildCount() != 0) {
                Node<E, V> evNode = entry.getValue();
                evNode.removeChild(node);
            }
        }

        // remove from map
        nodesMap.remove(vertex);
    }

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

    @Override
    public boolean hasTransition(V from, V to, E edge) {
        validateParameters(from, to, edge);

        Node<E, V> fromNode = getNode(from);
        Node<E, V> toNode = getNode(to);

        return fromNode.hasChild(edge) && fromNode.getChild(edge).equals(toNode);
    }

    @Override
    public boolean hasAnyTransition(V from, V to) {
        validateParameters(from, to);

        Node<E, V> fromNode = getNode(from);
        Node<E, V> toNode = getNode(to);

        return fromNode.getChildren().contains(toNode);
    }

    @Override
    public boolean hasVertex(V vertex) {
        Objects.requireNonNull(vertex);
        return nodesMap.containsKey(vertex);
    }

    @Override
    public void removeTransition(V from, V to, E edge) {
        if (!hasTransition(from, to, edge)) {
            throw new NoSuchElementException("Transition not found from [%s] to [%s] on edge [%s]".formatted(from, to, edge));
        }

        Node<E, V> fromNode = getNode(from);
        fromNode.removeChild(edge);
    }

    @Override
    public VertexTypes getVertexType(V vertex) {
        return getNode(vertex).getType();
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
            throw new NoSuchElementException("FSM tree does not contain the vertex: [%s]".formatted(vertex));
        }
        return node;
    }

    @Override
    public V traverse(V from, Iterable<E> edges) {
        Node<E, V> startNode = getNode(from);
        return traverse(startNode, edges);
    }

    private V traverse(Node<E, V> start, Iterable<E> edges) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(edges);

        Node<E, V> current = start;
        for (E edge : edges) {
            current = current.getChild(edge);
        }
        return current.getValue();
    }

    @Override
    public int size() {
        return nodesMap.size();
    }

    @Override
    public void clear() {
        nodesMap.clear();
    }

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
                    throw new NoSuchElementException("No more elements in the FSMTree");
                }
                return mapIterator.next();
            }

            @Override
            public void remove() {
                mapIterator.remove();
            }
        };
    }
}
package io.github.averude.fsm;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public class FSMTree<E, V> implements MutableFiniteStateMachine<E, V> {
    private static final NodeFactory NODE_FACTORY = new NodeFactory();
    private final VertexTypes defaultVertexType;
    private final Node<E, V> root;
    private final Map<V, Node<E, V>> nodesMap = new HashMap<>();

    public FSMTree(V vertex) {
        this(vertex, VertexTypes.BASIC);
    }

    public FSMTree(V vertex, VertexTypes defaultVertexType) {
        this.defaultVertexType = defaultVertexType;
        this.root = NODE_FACTORY.createNode(vertex, defaultVertexType);
        nodesMap.put(vertex, root);
    }

    @Override
    public void addTransition(V from, V to, E edge) {
        addTransition(from, to, edge, defaultVertexType);
    }

    @Override
    public void addTransition(V from, V to, E edge, VertexTypes vertexType) {
        validateParameters(from, to, edge);
        Objects.requireNonNull(vertexType, "vertex type cannot be null");

        Node<E, V> fromNode = getNode(from);
        if (fromNode.hasChild(edge)) {
            throw new IllegalArgumentException("Transition already exists from: [%s] on edge: [%s]".formatted(from, edge));
        }

        Node<E, V> toNode = nodesMap.computeIfAbsent(to, v -> NODE_FACTORY.createNode(v, vertexType));
        fromNode.addChild(edge, toNode);
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
        Node<E, V> toNode = getNode(to);

        fromNode.removeChild(edge);

        if (toNode.getChildCount() == 0 && hasNoEdgesLeftTo(toNode)) {
            nodesMap.remove(to);
        }
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

    private boolean hasNoEdgesLeftTo(Node<E, V> toNode) {
        return nodesMap.values()
                .stream()
                .filter(node -> !node.equals(toNode))
                .noneMatch(node -> node.getChildren().contains(toNode));
    }

    @Override
    public V traverse(Iterable<E> edges) {
        return traverse(root, edges);
    }

    @Override
    public V traverse(V vertex, Iterable<E> edges) {
        Node<E, V> startNode = getNode(vertex);
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
}
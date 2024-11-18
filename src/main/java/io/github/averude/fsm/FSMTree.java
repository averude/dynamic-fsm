package io.github.averude.fsm;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public class FSMTree<E, V> {
    private static final NodeFactory NODE_FACTORY = new NodeFactory();
    private final NodeTypes nodeType;
    private final Node<E, V> root;
    private final Map<V, Node<E, V>> nodesMap = new HashMap<>();

    public FSMTree(V vertex) {
        this(vertex, NodeTypes.DEFAULT);
    }

    public FSMTree(V vertex, NodeTypes nodeType) {
        this.nodeType = nodeType;
        this.root = NODE_FACTORY.createNode(vertex, nodeType);
        nodesMap.put(vertex, root);
    }

    public void addTransition(V from, V to, E edge) {
        addTransition(from, to, edge, nodeType);
    }

    public void addTransition(V from, V to, E edge, NodeTypes nodeType) {
        validateParameters(from, to, edge);
        Objects.requireNonNull(nodeType);

        Node<E, V> fromNode = getNode(from);
        if (fromNode.hasChild(edge)) {
            throw new IllegalArgumentException("Transition already exists from: [%s] on edge: [%s]".formatted(from, edge));
        }

        Node<E, V> toNode = nodesMap.computeIfAbsent(to, v -> NODE_FACTORY.createNode(v, nodeType));
        fromNode.addChild(edge, toNode);
    }

    public boolean hasTransition(V from, V to, E edge) {
        validateParameters(from, to, edge);

        Node<E, V> fromNode = getNode(from);
        Node<E, V> toNode = getNode(to);

        return fromNode.hasChild(edge) && fromNode.getChild(edge) == toNode;
    }

    public boolean hasAnyTransition(V from, V to) {
        validateParameters(from, to);

        Node<E, V> fromNode = getNode(from);
        Node<E, V> toNode = getNode(to);

        return fromNode.getChildren().contains(toNode);
    }

    public boolean hasVertex(V vertex) {
        Objects.requireNonNull(vertex);
        return nodesMap.containsKey(vertex);
    }

    public void removeTransition(V from, V to, E edge) {
        validateParameters(from, to, edge);

        Node<E, V> fromNode = getNode(from);
        Node<E, V> toNode = getNode(to);

        if (!fromNode.hasChild(edge) || fromNode.getChild(edge) != toNode) {
            throw new NoSuchElementException("Transition not found from [%s] to [%s] on edge [%s]".formatted(from, to, edge));
        }

        fromNode.removeChild(edge);

        if (toNode.getChildCount() == 0 && hasNoEdgesLeftTo(toNode)) {
            nodesMap.remove(to);
        }
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
                .filter(node -> node != toNode)
                .noneMatch(node -> node.getChildren().contains(toNode));
    }

    public V traverse(Iterable<E> edges) {
        return traverse(root, edges);
    }

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

    public int size() {
        return nodesMap.size();
    }
}
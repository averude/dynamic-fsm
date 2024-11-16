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
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        Objects.requireNonNull(edge);

        Node<E, V> fromNode = nodesMap.get(from);
        if (fromNode == null) {
            throw new NoSuchElementException("FSM tree does not contain the vertex " + from);
        }

        if (fromNode.hasChild(edge)) {
            throw new IllegalArgumentException("FSM tree already contains the edge " + edge + " from " + from);
        }

        Node<E, V> toNode = nodesMap.get(to);
        if (toNode == null) {
            toNode = NODE_FACTORY.createNode(to, nodeType);
            nodesMap.put(to, toNode);
        }

        fromNode.addChild(edge, toNode);
    }

    public V traverse(Iterable<E> edges) {
        return traverse(root, edges);
    }

    public V traverse(V vertex, Iterable<E> edges) {
        Node<E, V> current = nodesMap.get(vertex);
        if (current == null) {
            throw new NoSuchElementException("FSM tree does not contain the vertex " + vertex);
        }

        return traverse(current, edges);
    }

    private V traverse(Node<E, V> start, Iterable<E> edges) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(edges);

        Node<E, V> current = start;

        for (E edge : edges) {
            current = current.getNext(edge);
        }

        return current.getValue();
    }

    public int size() {
        return nodesMap.size();
    }
}
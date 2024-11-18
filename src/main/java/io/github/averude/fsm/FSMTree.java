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
        Objects.requireNonNull(nodeType);

        Node<E, V> fromNode = nodesMap.get(from);
        if (fromNode == null) {
            throw new NoSuchElementException("FSM tree does not contain the vertex: [%s]".formatted(from));
        }

        if (fromNode.hasChild(edge)) {
            throw new IllegalArgumentException("FSM tree already contains the edge: [%s] from: [%s]".formatted(edge, from));
        }

        Node<E, V> toNode = nodesMap.get(to);
        if (toNode == null) {
            toNode = NODE_FACTORY.createNode(to, nodeType);
            nodesMap.put(to, toNode);
        }

        fromNode.addChild(edge, toNode);
    }

    public boolean hasTransition(V from, V to, E edge) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        Objects.requireNonNull(edge);

        // check if from and to exist
        Node<E, V> fromNode = nodesMap.get(from);
        if (fromNode == null) {
            throw new NoSuchElementException("FSM tree does not contain the vertex: [%s]".formatted(from));
        }

        Node<E, V> toNode = nodesMap.get(to);
        if (toNode == null) {
            throw new NoSuchElementException("FSM tree does not contain the vertex: [%s]".formatted(to));
        }

        // check if `from` has edge and it points to `to`
        if (!fromNode.hasChild(edge)) {
            return false;
        }

        // check if child node of `from` is actually `to` node
        return fromNode.getChild(edge) == toNode;
    }

    public boolean hasAnyTransition(V from, V to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        // check if from and to exist
        Node<E, V> fromNode = nodesMap.get(from);
        if (fromNode == null) {
            throw new NoSuchElementException("FSM tree does not contain the vertex: [%s]".formatted(from));
        }

        Node<E, V> toNode = nodesMap.get(to);
        if (toNode == null) {
            throw new NoSuchElementException("FSM tree does not contain the vertex: [%s]".formatted(to));
        }

        return fromNode.getChildren().stream().anyMatch(child -> child == toNode);
    }

    public boolean hasVertex(V vertex) {
        Objects.requireNonNull(vertex);

        return nodesMap.containsKey(vertex);
    }

    public void removeTransition(V from, V to, E edge) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        Objects.requireNonNull(edge);

        Node<E, V> fromNode = nodesMap.get(from);
        if (fromNode == null) {
            throw new NoSuchElementException("FSM tree does not contain the vertex: [%s]".formatted(from));
        }

        Node<E, V> toNode = nodesMap.get(to);
        if (toNode == null) {
            throw new NoSuchElementException("FSM tree does not contain the vertex: [%s]".formatted(to));
        }

        if (!fromNode.hasChild(edge) || fromNode.getChild(edge) != toNode) {
            throw new NoSuchElementException("FSM tree does not contain the edge [%s] from [%s] to [%s]".formatted(edge, from, to));
        }

        fromNode.removeChild(edge);
        // If toNode has no children and other nodes has no edge to this node then remove it from nodesMap
        if (toNode.getChildCount() == 0 && hasNoEdgesLeftTo(toNode)) {
            nodesMap.remove(to);
        }
    }

    // Complexity of operation is O(N) where N is number of edges (transitions) in graph
    private boolean hasNoEdgesLeftTo(Node<E, V> toNode) {
        return nodesMap.values()
                .stream()
                .filter(node -> node != toNode) // exclude the target node
                .noneMatch(node -> node.getChildren().stream()
                        .anyMatch(child -> child == toNode));
    }

    public V traverse(Iterable<E> edges) {
        return traverse(root, edges);
    }

    public V traverse(V vertex, Iterable<E> edges) {
        Node<E, V> current = nodesMap.get(vertex);
        if (current == null) {
            throw new NoSuchElementException("FSM tree does not contain the vertex: [%s]".formatted(vertex));
        }

        return traverse(current, edges);
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
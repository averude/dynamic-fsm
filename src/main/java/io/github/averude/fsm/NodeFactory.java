package io.github.averude.fsm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class NodeFactory {
    public <K, V> Node<K, V> createNode(V value, VertexTypes type) {
        return switch (type) {
            case BASIC -> new DefaultNode<>(value);
            case LOOPED -> new LoopedNode<>(value);
        };
    }

    abstract static class AbstractNode<K, V> implements Node<K, V> {
        protected V value;
        protected final Map<K, Node<K, V>> children = new HashMap<>();

        @Override
        public void addChild(K key, Node<K, V> child) {
            children.put(key, child);
        }

        @Override
        public void removeChild(K key) {
            children.remove(key);
        }

        @Override
        public int getChildCount() {
            return children.size();
        }

        @Override
        public Collection<Node<K, V>> getChildren() {
            return children.values();
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public boolean hasChild(K key) {
            return children.containsKey(key);
        }
    }

    static class DefaultNode<K, V> extends AbstractNode<K, V> {

        DefaultNode(V value) {
            this.value = value;
        }

        @Override
        public Node<K, V> getChild(K key) {
            if (children.containsKey(key)) {
                return children.get(key);
            } else {
                throw new NoSuchElementException("Node does not contain the key: [%s]".formatted(key));
            }
        }

        @Override
        public VertexTypes getType() {
            return VertexTypes.BASIC;
        }
    }

    static class LoopedNode<K, V> extends AbstractNode<K, V> {

        LoopedNode(V value) {
            this.value = value;
        }

        @Override
        public Node<K, V> getChild(K key) {
            return children.getOrDefault(key, this);
        }

        @Override
        public VertexTypes getType() {
            return VertexTypes.LOOPED;
        }
    }
}

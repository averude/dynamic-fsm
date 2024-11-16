package io.github.averude.fsm;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class NodeFactory {
    public <K, V> Node<K, V> createNode(V value, NodeTypes type) {
        return switch (type) {
            case DEFAULT -> new DefaultNode<>(value);
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
        public Node<K, V> getNext(K key) {
            if (children.containsKey(key)) {
                return children.get(key);
            } else {
                throw new NoSuchElementException("Node does not contain the key " + key);
            }
        }
    }

    static class LoopedNode<K, V> extends AbstractNode<K, V> {

        LoopedNode(V value) {
            this.value = value;
        }

        @Override
        public Node<K, V> getNext(K key) {
            return children.getOrDefault(key, this);
        }
    }
}

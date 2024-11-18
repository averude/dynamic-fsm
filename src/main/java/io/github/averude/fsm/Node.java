package io.github.averude.fsm;

import java.util.Collection;

public interface Node<K, V> {
    void addChild(K key, Node<K, V> child);

    void removeChild(K key);

    int getChildCount();

    Collection<Node<K, V>> getChildren();

    Node<K, V> getChild(K key);

    V getValue();

    boolean hasChild(K key);
}
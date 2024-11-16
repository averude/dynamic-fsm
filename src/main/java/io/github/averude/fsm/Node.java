package io.github.averude.fsm;

public interface Node<K, V> {
    void addChild(K key, Node<K, V> child);

    Node<K, V> getNext(K key);

    V getValue();

    boolean hasChild(K key);
}
package io.github.averude.fsm;

public interface MutableFiniteStateMachine<E, V> extends FiniteStateMachine<E, V> {
    void addVertex(V vertex);

    void addVertex(V vertex, VertexTypes vertexType);

    void removeVertex(V vertex);

    void addTransition(V from, V to, E edge);

    void addTransitions(V from, V to, Iterable<E> edges);

    void removeTransition(V from, V to, E edge);

    void clear();
}

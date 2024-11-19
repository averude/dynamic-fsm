package io.github.averude.fsm;

public interface FiniteStateMachine<E, V> {
    boolean hasTransition(V from, V to, E edge);

    boolean hasAnyTransition(V from, V to);

    boolean hasVertex(V vertex);

    VertexTypes getVertexType(V vertex);

    V traverse(Iterable<E> edges);

    V traverse(V vertex, Iterable<E> edges);

    int size();
}

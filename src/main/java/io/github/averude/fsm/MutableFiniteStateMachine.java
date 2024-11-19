package io.github.averude.fsm;

public interface MutableFiniteStateMachine<E, V> extends FiniteStateMachine<E, V> {
    void addTransition(V from, V to, E edge);

    void addTransition(V from, V to, E edge, VertexTypes vertexType);

    void removeTransition(V from, V to, E edge);
}

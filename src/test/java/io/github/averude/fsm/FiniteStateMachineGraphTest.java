package io.github.averude.fsm;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiniteStateMachineGraphTest {
    private static final String START = "Start";
    private static final String END = "End";

    @Test
    void simpleFSM() {
        FiniteStateMachineGraph<Integer, String> finiteStateMachineGraph = new FiniteStateMachineGraph<>();

        finiteStateMachineGraph.addVertex(START);
        finiteStateMachineGraph.addVertex(END);
        finiteStateMachineGraph.addTransition(START, END, 1);

        String result = finiteStateMachineGraph.traverse(START, List.of(1));
        assertEquals(END, result);
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(START));
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(END));
        assertTrue(finiteStateMachineGraph.hasTransition(START, END, 1));
        assertTrue(finiteStateMachineGraph.hasAnyTransition(START, END));
        assertEquals(2, finiteStateMachineGraph.size());
    }

    @Test
    void simpleLoopedFSM() {
        FiniteStateMachineGraph<Integer, String> finiteStateMachineGraph = new FiniteStateMachineGraph<>(VertexTypes.LOOPED);

        finiteStateMachineGraph.addVertex(START);

        String result = finiteStateMachineGraph.traverse(START, List.of(1));
        assertEquals(START, result);
        assertFalse(finiteStateMachineGraph.hasTransition(START, START, 1));
        assertEquals(VertexTypes.LOOPED, finiteStateMachineGraph.getVertexType(START));
        assertEquals(1, finiteStateMachineGraph.size());
    }

    @Test
    void simpleTwoDirectionFSM() {
        FiniteStateMachineGraph<Integer, String> finiteStateMachineGraph = new FiniteStateMachineGraph<>();

        finiteStateMachineGraph.addVertex(START);
        finiteStateMachineGraph.addVertex(END);
        finiteStateMachineGraph.addTransition(START, END, 1);
        finiteStateMachineGraph.addTransition(END, START, 1);

        String result = finiteStateMachineGraph.traverse(START, List.of(1, 1));
        assertEquals(START, result);
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(START));
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(END));
        assertTrue(finiteStateMachineGraph.hasTransition(START, END, 1));
        assertTrue(finiteStateMachineGraph.hasAnyTransition(START, END));
        assertTrue(finiteStateMachineGraph.hasTransition(END, START, 1));
        assertTrue(finiteStateMachineGraph.hasAnyTransition(END, START));
        assertEquals(2, finiteStateMachineGraph.size());
    }

    @Test
    void simpleSelfPointingNodeFSM() {
        FiniteStateMachineGraph<Integer, String> finiteStateMachineGraph = new FiniteStateMachineGraph<>();

        finiteStateMachineGraph.addVertex(START);
        finiteStateMachineGraph.addVertex(END);
        finiteStateMachineGraph.addTransition(START, END, 1);
        finiteStateMachineGraph.addTransition(END, END, 2);

        String result = finiteStateMachineGraph.traverse(START, List.of(1, 2));
        assertEquals(END, result);
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(START));
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(END));
        assertTrue(finiteStateMachineGraph.hasTransition(START, END, 1));
        assertTrue(finiteStateMachineGraph.hasAnyTransition(START, END));
        assertFalse(finiteStateMachineGraph.hasTransition(END, START, 2));
        assertFalse(finiteStateMachineGraph.hasAnyTransition(END, START));
        assertTrue(finiteStateMachineGraph.hasTransition(END, END, 2));
        assertTrue(finiteStateMachineGraph.hasAnyTransition(END, END));
        assertEquals(2, finiteStateMachineGraph.size());
    }

    @Test
    void addSameVertexTwice_throwsIllegalArgumentException() {
        FiniteStateMachineGraph<Integer, String> finiteStateMachineGraph = new FiniteStateMachineGraph<>();

        finiteStateMachineGraph.addVertex(START);
        assertThrows(IllegalArgumentException.class, () ->finiteStateMachineGraph.addVertex(START));
    }

    @Test
    void sameEdgeToVertexTwice_throwsIllegalArgumentException() {
        FiniteStateMachineGraph<Integer, String> finiteStateMachineGraph = new FiniteStateMachineGraph<>();

        finiteStateMachineGraph.addVertex(START);
        finiteStateMachineGraph.addVertex(END);
        finiteStateMachineGraph.addTransition(START, END, 1);

        assertThrows(IllegalArgumentException.class, () -> finiteStateMachineGraph.addTransition(START, END, 1));
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(START));
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(END));
        assertTrue(finiteStateMachineGraph.hasTransition(START, END, 1));
        assertTrue(finiteStateMachineGraph.hasAnyTransition(START, END));
        assertEquals(2, finiteStateMachineGraph.size());
    }

    @Test
    void longChainOfTransitions() {
        String play = "Play";
        String pause = "Pause";
        String stop = "Stop";

        FiniteStateMachineGraph<Integer, String> finiteStateMachineGraph = new FiniteStateMachineGraph<>();
        finiteStateMachineGraph.addVertex(stop);
        finiteStateMachineGraph.addVertex(play);
        finiteStateMachineGraph.addVertex(pause);
        finiteStateMachineGraph.addTransition(stop, play, 1);
        finiteStateMachineGraph.addTransition(play, pause, 1);
        finiteStateMachineGraph.addTransition(play, stop, 2);
        finiteStateMachineGraph.addTransition(pause, play, 1);
        finiteStateMachineGraph.addTransition(pause, stop, 2);

        // Play, Stop, Play, Pause, Stop, Play, Pause, Play
        String result = finiteStateMachineGraph.traverse(stop, List.of(1, 2, 1, 1, 2, 1, 1, 1));

        assertEquals(play, result);
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(stop));
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(play));
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(pause));
        assertTrue(finiteStateMachineGraph.hasTransition(stop, play, 1));
        assertTrue(finiteStateMachineGraph.hasTransition(play, pause, 1));
        assertTrue(finiteStateMachineGraph.hasTransition(play, stop, 2));
        assertTrue(finiteStateMachineGraph.hasTransition(pause, play, 1));
        assertTrue(finiteStateMachineGraph.hasTransition(pause, stop, 2));
        assertTrue(finiteStateMachineGraph.hasAnyTransition(stop, play));
        assertTrue(finiteStateMachineGraph.hasAnyTransition(play, pause));
        assertTrue(finiteStateMachineGraph.hasAnyTransition(play, stop));
        assertTrue(finiteStateMachineGraph.hasAnyTransition(pause, play));
        assertTrue(finiteStateMachineGraph.hasAnyTransition(pause, stop));
        assertEquals(3, finiteStateMachineGraph.size());
    }

    @Test
    void transitionFromVertex() {
        String intermediate = "Intermediate";

        FiniteStateMachineGraph<Integer, String> finiteStateMachineGraph = new FiniteStateMachineGraph<>();

        finiteStateMachineGraph.addVertex(START);
        finiteStateMachineGraph.addVertex(END);
        finiteStateMachineGraph.addVertex(intermediate);
        finiteStateMachineGraph.addTransition(START, END, 1);
        finiteStateMachineGraph.addTransition(END, START, 1);
        finiteStateMachineGraph.addTransition(END, intermediate, 2);

        String result = finiteStateMachineGraph.traverse(END, List.of(2));

        assertEquals(intermediate, result);
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(START));
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(END));
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(intermediate));
        assertTrue(finiteStateMachineGraph.hasTransition(START, END, 1));
        assertTrue(finiteStateMachineGraph.hasTransition(END, START, 1));
        assertTrue(finiteStateMachineGraph.hasTransition(END, intermediate, 2));
        assertTrue(finiteStateMachineGraph.hasAnyTransition(START, END));
        assertTrue(finiteStateMachineGraph.hasAnyTransition(END, START));
        assertTrue(finiteStateMachineGraph.hasAnyTransition(END, intermediate));
        assertEquals(3, finiteStateMachineGraph.size());
    }

    @Test
    void invalidEdgeForBasicNodeType() {
        FiniteStateMachineGraph<Integer, String> finiteStateMachineGraph = new FiniteStateMachineGraph<>();

        finiteStateMachineGraph.addVertex(START);
        finiteStateMachineGraph.addVertex(END);
        finiteStateMachineGraph.addTransition(START, END, 1);

        assertThrows(NoSuchElementException.class, () -> finiteStateMachineGraph.traverse(START, List.of(10)));
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(START));
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(END));
        assertTrue(finiteStateMachineGraph.hasTransition(START, END, 1));
        assertTrue(finiteStateMachineGraph.hasAnyTransition(START, END));
        assertEquals(2, finiteStateMachineGraph.size());
    }

    @Test
    void invalidEdgeForLoopedNodeType() {
        FiniteStateMachineGraph<Integer, String> finiteStateMachineGraph = new FiniteStateMachineGraph<>();

        finiteStateMachineGraph.addVertex(START);
        finiteStateMachineGraph.addVertex(END, VertexTypes.LOOPED);
        finiteStateMachineGraph.addTransition(START, END, 1);

        String result = finiteStateMachineGraph.traverse(START, List.of(1, 10));
        assertEquals(END, result);
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(START));
        assertEquals(VertexTypes.LOOPED, finiteStateMachineGraph.getVertexType(END));
        assertTrue(finiteStateMachineGraph.hasTransition(START, END, 1));
        assertTrue(finiteStateMachineGraph.hasAnyTransition(START, END));
        assertEquals(2, finiteStateMachineGraph.size());
    }

    @Test
    void invalidVertex() {
        FiniteStateMachineGraph<Integer, String> finiteStateMachineGraph = new FiniteStateMachineGraph<>();

        finiteStateMachineGraph.addVertex(START);
        finiteStateMachineGraph.addVertex(END);
        finiteStateMachineGraph.addTransition(START, END, 1);

        assertThrows(NoSuchElementException.class, () -> finiteStateMachineGraph.addTransition("Invalid", END, 1));
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(START));
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(END));
        assertTrue(finiteStateMachineGraph.hasTransition(START, END, 1));
        assertTrue(finiteStateMachineGraph.hasAnyTransition(START, END));
        assertEquals(2, finiteStateMachineGraph.size());
    }

    @Test
    void removeVertex() {
        String running = "running";
        FiniteStateMachineGraph<Integer, String> finiteStateMachineGraph = new FiniteStateMachineGraph<>();

        finiteStateMachineGraph.addVertex(START);
        finiteStateMachineGraph.addVertex(running);
        finiteStateMachineGraph.addVertex(END);
        finiteStateMachineGraph.addTransition(START, END, 1);
        finiteStateMachineGraph.addTransition(START, running, 2);
        finiteStateMachineGraph.addTransition(running, running, 2);
        finiteStateMachineGraph.addTransition(running, END, 3);
        finiteStateMachineGraph.addTransition(START, END, 10);

        String result = finiteStateMachineGraph.traverse(START, List.of(2, 2, 2, 3));
        assertEquals(END, result);

        finiteStateMachineGraph.removeVertex(running);

        assertFalse(finiteStateMachineGraph.hasVertex(running));
        assertEquals(2, finiteStateMachineGraph.size());
        assertThrows(NoSuchElementException.class, () -> finiteStateMachineGraph.traverse(END, List.of(2, 2, 2, 3)));
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(START));
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(END));
        assertTrue(finiteStateMachineGraph.hasTransition(START, END, 1));
        assertTrue(finiteStateMachineGraph.hasTransition(START, END, 10));
        assertTrue(finiteStateMachineGraph.hasAnyTransition(START, END));
        assertThrows(NoSuchElementException.class, () -> finiteStateMachineGraph.hasTransition(START, running, 2));
        assertThrows(NoSuchElementException.class, () -> finiteStateMachineGraph.hasTransition(running, running, 2));
        assertThrows(NoSuchElementException.class, () -> finiteStateMachineGraph.hasTransition(running, END, 3));

        // if edge survived, then exception will be thrown
        finiteStateMachineGraph.addTransition(START, END, 2);
        assertEquals(END, finiteStateMachineGraph.traverse(START, List.of(2)));
    }

    @Test
    void removeTransition() {
        FiniteStateMachineGraph<Integer, String> finiteStateMachineGraph = new FiniteStateMachineGraph<>();

        finiteStateMachineGraph.addVertex(START);
        finiteStateMachineGraph.addVertex(END);
        finiteStateMachineGraph.addTransition(START, END, 1);
        finiteStateMachineGraph.addTransition(START, END, 10);

        String result = finiteStateMachineGraph.traverse(START, List.of(10));
        assertEquals(END, result);

        finiteStateMachineGraph.removeTransition(START, END, 10);

        assertTrue(finiteStateMachineGraph.hasAnyTransition(START, END));
        assertTrue(finiteStateMachineGraph.hasTransition(START, END, 1));
        assertFalse(finiteStateMachineGraph.hasTransition(START, END, 10));
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(START));
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(END));
        assertEquals(2, finiteStateMachineGraph.size());
        assertTrue(finiteStateMachineGraph.hasVertex(END));
        assertThrows(NoSuchElementException.class, () -> finiteStateMachineGraph.traverse(START, List.of(10)));
    }

    @Test
    void removeTransitionButWithDifferentToVertex() {
        FiniteStateMachineGraph<Integer, String> finiteStateMachineGraph = new FiniteStateMachineGraph<>();

        finiteStateMachineGraph.addVertex(START);
        finiteStateMachineGraph.addVertex(END);
        finiteStateMachineGraph.addTransition(START, END, 1);
        finiteStateMachineGraph.addTransition(START, END, 10);

        String result = finiteStateMachineGraph.traverse(START, List.of(10));
        assertEquals(END, result);

        assertThrows(NoSuchElementException.class, () -> finiteStateMachineGraph.removeTransition(START, START, 10));

        assertTrue(finiteStateMachineGraph.hasTransition(START, END, 1));
        assertTrue(finiteStateMachineGraph.hasTransition(START, END, 10));
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(START));
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(END));
        assertEquals(2, finiteStateMachineGraph.size());
        assertTrue(finiteStateMachineGraph.hasVertex(END));
        assertEquals(END, finiteStateMachineGraph.traverse(START, List.of(10)));
    }

    @Test
    void clearFSM() {
        FiniteStateMachineGraph<Integer, String> finiteStateMachineGraph = new FiniteStateMachineGraph<>();

        finiteStateMachineGraph.addVertex(START);
        finiteStateMachineGraph.addVertex(END);
        finiteStateMachineGraph.addTransition(START, END, 1);

        String result = finiteStateMachineGraph.traverse(START, List.of(1));
        assertEquals(END, result);
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(START));
        assertEquals(VertexTypes.BASIC, finiteStateMachineGraph.getVertexType(END));
        assertTrue(finiteStateMachineGraph.hasTransition(START, END, 1));
        assertTrue(finiteStateMachineGraph.hasAnyTransition(START, END));
        assertEquals(2, finiteStateMachineGraph.size());

        finiteStateMachineGraph.clear();

        assertEquals(0, finiteStateMachineGraph.size());
        assertFalse(finiteStateMachineGraph.hasVertex(START));
        assertFalse(finiteStateMachineGraph.hasVertex(END));
    }

    @Test
    void iterateOverValues() {
        FiniteStateMachineGraph<Integer, String> finiteStateMachineGraph = new FiniteStateMachineGraph<>();

        finiteStateMachineGraph.addVertex(START);
        finiteStateMachineGraph.addVertex(END);
        finiteStateMachineGraph.addTransitions(START, END, List.of(1, 2, 3, 4, 5));

        int valuesCount = 0;
        Set<String> graphValues = new HashSet<>();
        for (String value : finiteStateMachineGraph) {
            assertTrue(graphValues.add(value));
            valuesCount++;
        }
        assertEquals(valuesCount, finiteStateMachineGraph.size());
        assertEquals(graphValues, Set.of(START, END));
    }
}
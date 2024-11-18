package io.github.averude.fsm;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FSMTreeTest {
    private static final String START = "Start";
    private static final String END = "End";

    @Test
    void simpleFSM() {
        FSMTree<Integer, String> fsmTree = new FSMTree<>(START);
        fsmTree.addTransition(START, END, 1);

        String result = fsmTree.traverse(List.of(1));
        assertEquals(END, result);
        assertTrue(fsmTree.hasTransition(START, END, 1));
        assertTrue(fsmTree.hasAnyTransition(START, END));
        assertEquals(2, fsmTree.size());
    }

    @Test
    void simpleLoopedFSM() {
        FSMTree<Integer, String> fsmTree = new FSMTree<>(START, NodeTypes.LOOPED);

        String result = fsmTree.traverse(List.of(1));
        assertEquals(START, result);
        assertFalse(fsmTree.hasTransition(START, START, 1));
        assertEquals(1, fsmTree.size());
    }

    @Test
    void simpleTwoDirectionFSM() {
        FSMTree<Integer, String> fsmTree = new FSMTree<>(START);
        fsmTree.addTransition(START, END, 1);
        fsmTree.addTransition(END, START, 1);

        String result = fsmTree.traverse(List.of(1, 1));
        assertEquals(START, result);
        assertTrue(fsmTree.hasTransition(START, END, 1));
        assertTrue(fsmTree.hasAnyTransition(START, END));
        assertTrue(fsmTree.hasTransition(END, START, 1));
        assertTrue(fsmTree.hasAnyTransition(END, START));
        assertEquals(2, fsmTree.size());
    }

    @Test
    void simpleSelfPointingNodeFSM() {
        FSMTree<Integer, String> fsmTree = new FSMTree<>(START);
        fsmTree.addTransition(START, END, 1);
        fsmTree.addTransition(END, END, 2);

        String result = fsmTree.traverse(List.of(1, 2));
        assertEquals(END, result);
        assertTrue(fsmTree.hasTransition(START, END, 1));
        assertTrue(fsmTree.hasAnyTransition(START, END));
        assertFalse(fsmTree.hasTransition(END, START, 2));
        assertFalse(fsmTree.hasAnyTransition(END, START));
        assertTrue(fsmTree.hasTransition(END, END, 2));
        assertTrue(fsmTree.hasAnyTransition(END, END));
        assertEquals(2, fsmTree.size());
    }

    @Test
    void sameEdgeToVertexTwice_throwsIllegalArgumentException() {
        FSMTree<Integer, String> fsmTree = new FSMTree<>(START);
        fsmTree.addTransition(START, END, 1);

        assertThrows(IllegalArgumentException.class, () -> fsmTree.addTransition(START, END, 1));
        assertTrue(fsmTree.hasTransition(START, END, 1));
        assertTrue(fsmTree.hasAnyTransition(START, END));
        assertEquals(2, fsmTree.size());
    }

    @Test
    void longChainOfTransitions() {
        String play = "Play";
        String pause = "Pause";
        String stop = "Stop";

        FSMTree<Integer, String> fsmTree = new FSMTree<>(stop);
        fsmTree.addTransition(stop, play, 1);
        fsmTree.addTransition(play, pause, 1);
        fsmTree.addTransition(play, stop, 2);
        fsmTree.addTransition(pause, play, 1);
        fsmTree.addTransition(pause, stop, 2);

        // Play, Stop, Play, Pause, Stop, Play, Pause, Play
        String result = fsmTree.traverse(List.of(1, 2, 1, 1, 2, 1, 1, 1));

        assertEquals(play, result);
        assertTrue(fsmTree.hasTransition(stop, play, 1));
        assertTrue(fsmTree.hasTransition(play, pause, 1));
        assertTrue(fsmTree.hasTransition(play, stop, 2));
        assertTrue(fsmTree.hasTransition(pause, play, 1));
        assertTrue(fsmTree.hasTransition(pause, stop, 2));
        assertTrue(fsmTree.hasAnyTransition(stop, play));
        assertTrue(fsmTree.hasAnyTransition(play, pause));
        assertTrue(fsmTree.hasAnyTransition(play, stop));
        assertTrue(fsmTree.hasAnyTransition(pause, play));
        assertTrue(fsmTree.hasAnyTransition(pause, stop));
        assertEquals(3, fsmTree.size());
    }

    @Test
    void transitionFromVertex() {
        String intermediate = "Intermediate";

        FSMTree<Integer, String> fsmTree = new FSMTree<>(START);
        fsmTree.addTransition(START, END, 1);
        fsmTree.addTransition(END, START, 1);
        fsmTree.addTransition(END, intermediate, 2);

        String result = fsmTree.traverse(END, List.of(2));

        assertEquals(intermediate, result);
        assertTrue(fsmTree.hasTransition(START, END, 1));
        assertTrue(fsmTree.hasTransition(END, START, 1));
        assertTrue(fsmTree.hasTransition(END, intermediate, 2));
        assertTrue(fsmTree.hasAnyTransition(START, END));
        assertTrue(fsmTree.hasAnyTransition(END, START));
        assertTrue(fsmTree.hasAnyTransition(END, intermediate));
        assertEquals(3, fsmTree.size());
    }

    @Test
    void invalidEdgeForSimpleNodeType() {
        FSMTree<Integer, String> fsmTree = new FSMTree<>(START);
        fsmTree.addTransition(START, END, 1);

        assertThrows(NoSuchElementException.class, () -> fsmTree.traverse(List.of(10)));
        assertTrue(fsmTree.hasTransition(START, END, 1));
        assertTrue(fsmTree.hasAnyTransition(START, END));
        assertEquals(2, fsmTree.size());
    }

    @Test
    void invalidEdgeForLoopedNodeType() {
        FSMTree<Integer, String> fsmTree = new FSMTree<>(START);
        fsmTree.addTransition(START, END, 1, NodeTypes.LOOPED);

        String result = fsmTree.traverse(List.of(1, 10));
        assertEquals(END, result);
        assertTrue(fsmTree.hasTransition(START, END, 1));
        assertTrue(fsmTree.hasAnyTransition(START, END));
        assertEquals(2, fsmTree.size());
    }

    @Test
    void invalidVertex() {
        FSMTree<Integer, String> fsmTree = new FSMTree<>(START);
        fsmTree.addTransition(START, END, 1);

        assertThrows(NoSuchElementException.class, () -> fsmTree.addTransition("Invalid", END, 1));
        assertTrue(fsmTree.hasTransition(START, END, 1));
        assertTrue(fsmTree.hasAnyTransition(START, END));
        assertEquals(2, fsmTree.size());
    }

    @Test
    void removeTransition() {
        FSMTree<Integer, String> fsmTree = new FSMTree<>(START);
        fsmTree.addTransition(START, END, 1);
        fsmTree.addTransition(START, END, 10);

        String result = fsmTree.traverse(List.of(10));
        assertEquals(END, result);

        fsmTree.removeTransition(START, END, 10);

        assertTrue(fsmTree.hasAnyTransition(START, END));
        assertTrue(fsmTree.hasTransition(START, END, 1));
        assertFalse(fsmTree.hasTransition(START, END, 10));
        assertEquals(2, fsmTree.size());
        assertTrue(fsmTree.hasVertex(END));
        assertThrows(NoSuchElementException.class, () -> fsmTree.traverse(List.of(10)));
    }

    @Test
    void removeTransitionButWithDifferentToVertex() {
        FSMTree<Integer, String> fsmTree = new FSMTree<>(START);
        fsmTree.addTransition(START, END, 1);
        fsmTree.addTransition(START, END, 10);

        String result = fsmTree.traverse(List.of(10));
        assertEquals(END, result);

        assertThrows(NoSuchElementException.class, () -> fsmTree.removeTransition(START, START, 10));

        assertTrue(fsmTree.hasTransition(START, END, 1));
        assertTrue(fsmTree.hasTransition(START, END, 10));
        assertEquals(2, fsmTree.size());
        assertTrue(fsmTree.hasVertex(END));
        assertEquals(END, fsmTree.traverse(List.of(10)));
    }

    @Test
    void removeTransitionAndVertex() {
        var someState = "someState";

        FSMTree<Integer, String> fsmTree = new FSMTree<>(START);
        fsmTree.addTransition(START, END, 1);
        fsmTree.addTransition(START, someState, 10);

        String result = fsmTree.traverse(List.of(10));
        assertEquals(someState, result);

        fsmTree.removeTransition(START, someState, 10);

        assertTrue(fsmTree.hasTransition(START, END, 1));
        assertEquals(2, fsmTree.size());
        assertThrows(NoSuchElementException.class, () -> fsmTree.hasTransition(START, someState, 10));
        assertFalse(fsmTree.hasVertex(someState));
        assertThrows(NoSuchElementException.class, () -> fsmTree.traverse(List.of(10)));
    }
}
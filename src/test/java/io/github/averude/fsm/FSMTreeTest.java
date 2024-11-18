package io.github.averude.fsm;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FSMTreeTest {
    String start = "Start";
    String end = "End";

    @Test
    void simpleFSM() {
        FSMTree<Integer, String> fsmTree = new FSMTree<>(start);
        fsmTree.addTransition(start, end, 1);

        String result = fsmTree.traverse(List.of(1));
        assertEquals(end, result);
    }

    @Test
    void simpleLoopedFSM() {
        FSMTree<Integer, String> fsmTree = new FSMTree<>(start, NodeTypes.LOOPED);

        String result = fsmTree.traverse(List.of(1));
        assertEquals(start, result);
    }

    @Test
    void simpleTwoDirectionFSM() {
        FSMTree<Integer, String> fsmTree = new FSMTree<>(start);
        fsmTree.addTransition(start, end, 1);
        fsmTree.addTransition(end, start, 1);

        String result = fsmTree.traverse(List.of(1, 1));
        assertEquals(start, result);
    }

    @Test
    void simpleSelfPointingNodeFSM() {
        FSMTree<Integer, String> fsmTree = new FSMTree<>(start);
        fsmTree.addTransition(start, end, 1);
        fsmTree.addTransition(end, end, 2);

        String result = fsmTree.traverse(List.of(1, 2));
        assertEquals(end, result);
    }

    @Test
    void sameEdgeToVertexTwice_throwsIllegalArgumentException() {
        FSMTree<Integer, String> fsmTree = new FSMTree<>(start);
        fsmTree.addTransition(start, end, 1);

        assertThrows(IllegalArgumentException.class, () -> fsmTree.addTransition(start, end, 1));
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
    }

    @Test
    void transitionFromVertex() {
        String intermediate = "Intermediate";

        FSMTree<Integer, String> fsmTree = new FSMTree<>(start);
        fsmTree.addTransition(start, end, 1);
        fsmTree.addTransition(end, start, 1);
        fsmTree.addTransition(end, intermediate, 2);

        String result = fsmTree.traverse(end, List.of(2));

        assertEquals(intermediate, result);
    }

    @Test
    void invalidEdgeForSimpleNodeType() {
        FSMTree<Integer, String> fsmTree = new FSMTree<>(start);
        fsmTree.addTransition(start, end, 1);

        assertThrows(NoSuchElementException.class, () -> fsmTree.traverse(List.of(10)));
    }

    @Test
    void invalidEdgeForLoopedNodeType() {
        FSMTree<Integer, String> fsmTree = new FSMTree<>(start);
        fsmTree.addTransition(start, end, 1, NodeTypes.LOOPED);

        String result = fsmTree.traverse(List.of(1, 10));
        assertEquals(end, result);
    }

    @Test
    void invalidVertex() {
        FSMTree<Integer, String> fsmTree = new FSMTree<>(start);
        fsmTree.addTransition(start, end, 1);

        assertThrows(NoSuchElementException.class, () -> fsmTree.addTransition("Invalid", end, 1));
    }

    @Test
    void removeTransition() {
        FSMTree<Integer, String> fsmTree = new FSMTree<>(start);
        fsmTree.addTransition(start, end, 1);
        fsmTree.addTransition(start, end, 10);

        String result = fsmTree.traverse(List.of(10));
        assertEquals(end, result);

        fsmTree.removeTransition(start, end, 10);

        assertTrue(fsmTree.hasAnyTransition(start, end));
        assertTrue(fsmTree.hasTransition(start, end, 1));
        assertFalse(fsmTree.hasTransition(start, end, 10));
        assertEquals(2, fsmTree.size());
        assertTrue(fsmTree.hasVertex(end));
        assertThrows(NoSuchElementException.class, () -> fsmTree.traverse(List.of(10)));
    }

    @Test
    void removeTransitionButWithDifferentToVertex() {
        FSMTree<Integer, String> fsmTree = new FSMTree<>(start);
        fsmTree.addTransition(start, end, 1);
        fsmTree.addTransition(start, end, 10);

        String result = fsmTree.traverse(List.of(10));
        assertEquals(end, result);

        assertThrows(NoSuchElementException.class, () -> fsmTree.removeTransition(start, start, 10));

        assertTrue(fsmTree.hasTransition(start, end, 1));
        assertTrue(fsmTree.hasTransition(start, end, 10));
        assertEquals(2, fsmTree.size());
        assertTrue(fsmTree.hasVertex(end));
        assertEquals(end, fsmTree.traverse(List.of(10)));
    }

    @Test
    void removeTransitionAndVertex() {
        var someState = "someState";

        FSMTree<Integer, String> fsmTree = new FSMTree<>(start);
        fsmTree.addTransition(start, end, 1);
        fsmTree.addTransition(start, someState, 10);

        String result = fsmTree.traverse(List.of(10));
        assertEquals(someState, result);

        fsmTree.removeTransition(start, someState, 10);

        assertTrue(fsmTree.hasTransition(start, end, 1));
        assertEquals(2, fsmTree.size());
        assertThrows(NoSuchElementException.class, () -> fsmTree.hasTransition(start, someState, 10));
        assertFalse(fsmTree.hasVertex(someState));
        assertThrows(NoSuchElementException.class, () -> fsmTree.traverse(List.of(10)));
    }
}
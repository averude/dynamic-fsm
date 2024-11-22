# Dynamic Finite State Machine Library

## Table of Contents
1. [Introduction](#introduction)
2. [Features](#features)
3. [Installation]($installation)
4. [Example](#example)

## Introduction

Dynamic Finite State Machine Library is a flexible and efficient Java library designed for creating and managing finite state machines. 
It allows you to dynamically construct state machines by defining states and transitions between those states, 
making it ideal for scenarios where states and transitions need to be modified at runtime.

A key feature of this library is its use of generic parameters for states (`V`) and transitions (`E`). 
This provides the flexibility to use any object types as states and transition triggers.

By leveraging the power of this library, developers can implement complex decision-making processes and state-dependent behavior with ease, 
making it an excellent tool for projects requiring dynamic state management.

## Features

The finite state machine (FSM) library provides a robust set of functionalities that help developers effectively model and manage state-driven systems. Below are the key features offered by this library:

1. **State and Transition Management**: Create and manage states as well as the transitions between them. The library supports the addition, removal, and validation of transitions.

2. **Flexible Edge Definitions**: Define transitions using customizable edge objects, which provide the flexibility to encode different kinds of state changes.

3. **Vertex Type Flexibility**: Support for different types of vertices (Basic and Looped) allows for the modeling of complex behaviors within the state machine.

4. **Traversal Operations**: Traverse through the state machine using a sequence of edges starting from a specific vertex or the initial state, making it useful for executing state transitions based on events.

5. **Dynamic State Machine Modifiability**: Modify the state machine on-the-fly with operations to add or remove transitions, aiding in dynamic and adaptive system behavior.

6. **State Querying**: Efficient querying capabilities to check the existence of specific vertices and transitions, facilitating easy checks and validations within system flows.

7. **Loops and Recursive States**: Special handling for looped nodes allows for easy modeling of recursive states or self-transitions.

With these features, the finite state machine library provides a powerful yet flexible way to model complex state-dependent behaviors in applications. Whether you are building simple sequences or intricate state-dependent interactions, this library helps streamline state management and operations.

## Installation

To add the Dynamic FSM library to your project, use the following dependency declarations:

### Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.averude</groupId>
    <artifactId>dynamic-fsm</artifactId>
    <version>0.0.1</version>
</dependency>
```

### Gradle

For Gradle, include the following implementation in your `build.gradle`:

```groovy
implementation 'io.github.averude:dynamic-fsm:0.0.1'
```

## Example

Assuming we have a music player state machine, which has 3 states: _stopped_, _playing_ and _paused_.
Commands for changing the state are: _play_, _pause_ and _stop_.
The transition between the states would be:
- From _stopped_ to _playing_: _play_ command
- From _playing_ to _stopped_: _stop_ command
- From _playing_ to _paused_: _pause_ command
- From _paused_ to _playing_: _play_ command
- From _paused_ to _stopped_: _stop_ command

With help of **dynamic-fsm** library we can get the result state after the list of commands:

```java
import io.github.averude.fsm.FiniteStateMachineGraph;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

interface State {
    String getStateDescription();
}

class StoppedState implements State {
    @Override
    public String getStateDescription() {
        return "The music is stopped";
    }
}

class PlayingState implements State {
    @Override
    public String getStateDescription() {
        return "The music is playing";
    }
}

class PausedState implements State {
    @Override
    public String getStateDescription() {
        return "The music is paused";
    }
}

public class FSMUsageExampleTest {
    @Test
    void name() {
        var stateMachine = new FiniteStateMachineGraph<String, State>();

        var playCommand = "Play";
        var pauseCommand = "Pause";
        var stopCommand = "Stop";

        var stoppedState = new StoppedState();
        var playingState = new PlayingState();
        var pausedState = new PausedState();

        stateMachine.addVertex(stoppedState);
        stateMachine.addVertex(playingState);
        stateMachine.addVertex(pausedState);

        stateMachine.addTransition(stoppedState, playingState, playCommand);
        stateMachine.addTransition(playingState, pausedState, pauseCommand);
        stateMachine.addTransition(playingState, stoppedState, stopCommand);
        stateMachine.addTransition(pausedState, playingState, playCommand);
        stateMachine.addTransition(pausedState, stoppedState, stopCommand);

        State stateAfterTraverse = stateMachine.traverse(
                stoppedState,
                List.of(playCommand, pauseCommand, playCommand, stopCommand)
        );

        assertEquals("The music is stopped", stateAfterTraverse.getStateDescription());
    }
}

```
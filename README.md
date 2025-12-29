# How to Run the Code

This project uses Gradle as the build system. Follow the steps below to compile and run the code.

## Prerequisites

- Ensure you have [Gradle](https://gradle.org/install/) installed on your system.
- Navigate to the root directory of the project using your terminal.

## Building the Project

To compile the project, use the following command:
```bash
gradle clean
```
Secondly,
```bash
gradle build
```
Thirdly,
```bash
gradle run
```

# Design Patterns Implemented

## Strategy Pattern

The Strategy Pattern is implemented in this project to define a family of algorithms, encapsulate each one, and make them interchangeable. This pattern involves the following participants:

1. **Strategy**:
    - `GhostChaseStrategy.class`
2. **ConcreteStrategy**:
    - `BlinkyChaseStrategy.class`
    - `PinkyChaseStrategy.class`
    - `InkyChaseStrategy.class`
    - `ClydeChaseStrategy.class`
3. **Context**:
    - `GhostImpl.class`

These classes work together to allow each ghost character to use a unique chase strategy, enabling dynamic changes in behavior based on the context in the game.



## State Pattern

The State Pattern is used to allow an object to alter its behavior when its internal state changes, making it appear as if the object changes its class. This pattern involves the following participants:

1. **State**:
    - `GhostState.class`
2. **ConcreteState**:
    - `NormalState.class`
    - `FrightenedState.class`
3. **Context**:
    - `GhostImpl.class`

With this pattern, each ghost can switch between different states, such as "Normal" or "Frightened," which changes their behavior dynamically in response to game events.




## Decorator Pattern

The Decorator Pattern is used to add additional responsibilities to an object dynamically. This pattern involves the following participants:

1. **Component**:
    - `Component` interface
2. **ConcreteComponent**:
    - `GhostImpl.class` (implements `Component`)
3. **Decorator**:
    - `GhostDecorator.class` abstract class
4. **ConcreteDecorator**:
    - `FrightenedGhostDecorator.class`

These classes work together to allow ghosts to be decorated with additional behaviors, such as becoming "Frightened," without altering the core `GhostImpl` class directly.



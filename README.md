# PacmanFX

<p align="center">
  <a href="https://github.com/gss10282023/game123/actions/workflows/ci.yml"><img alt="CI" src="https://github.com/gss10282023/game123/actions/workflows/ci.yml/badge.svg" /></a>
  <a href="LICENSE"><img alt="License" src="https://img.shields.io/github/license/gss10282023/game123" /></a>
  <a href="https://github.com/gss10282023/game123/issues"><img alt="Issues" src="https://img.shields.io/github/issues/gss10282023/game123" /></a>
  <a href="https://github.com/gss10282023/game123/commits"><img alt="Last commit" src="https://img.shields.io/github/last-commit/gss10282023/game123" /></a>
  <img alt="Java" src="https://img.shields.io/badge/Java-17%2B-007396?logo=java&logoColor=white" />
  <img alt="JavaFX" src="https://img.shields.io/badge/JavaFX-17.0.2-2c2255" />
  <img alt="Gradle" src="https://img.shields.io/badge/Gradle-wrapper-02303A?logo=gradle&logoColor=white" />
</p>

A Pac-Man-inspired arcade game built with JavaFX, with a codebase structured to showcase classic OO design patterns.

<p align="center">
  <img src="docs/assets/demo.gif" alt="Demo" />
</p>


## What it is

- Playable JavaFX Pacman clone (keyboard-controlled)
- Data-driven gameplay via `src/main/resources/config.json` and map files under `src/main/resources/`
- A compact reference implementation of Strategy/State/Decorator patterns in a real gameplay loop

## Who it's for

- Want to play quickly: follow the “Quick start” section
- Want to learn: jump to “Architecture & patterns”
- Want to extend it: tweak `config.json`, swap maps, or add new ghost behaviors

## Demo

If your Markdown viewer does not render GIFs, see `docs/assets/demo-strip.png` or `docs/assets/screenshot-1.png`.

## Tech stack

- Java 17 + JavaFX 17.0.2 (via `org.openjfx.javafxplugin`)
- Gradle wrapper (`./gradlew`)
- JSON config parsing via `json-simple`
- Unit tests via JUnit 5 (`./gradlew test`)

## Quick start

### Prerequisites

- JDK 17+

### Run

```bash
./gradlew run
```

### Build

```bash
./gradlew build
```

### Create a distributable

```bash
./gradlew installDist
```

### Use a custom config

```bash
./gradlew run --args="--config=/path/to/config.json"
```

## Controls

- Arrow keys: move

## Configuration

The game reads a JSON config from either filesystem path or classpath resource. By default it loads `/config.json` on the classpath (`src/main/resources/config.json`).

Top-level fields (see `src/main/resources/config.json`):

- `map`: map file path (filesystem path or classpath resource)
- `numLives`: starting lives
- `levels[]`: per-level tuning
  - `pacmanSpeed` (pixels per tick)
  - `ghostSpeed.{chase,scatter,frightened}` (pixels per tick)
  - `modeLengths.{chase,scatter,frightened}` (ticks; the game loop runs ~every 34ms in `pacman.view.GameWindow`)

## Map format

Map files are plain-text grids; each character maps to a tile/entity (see `pacman.model.factories.RenderableType`):

- `0`: empty
- `1..6`: walls (horizontal/vertical/corners)
- `7`: pellet
- `z`: power pellet
- `p`: Pacman spawn
- `b`,`s`,`i`,`c`: ghost spawns (Blinky/Pinky/Inky/Clyde)

Each map cell is a 16×16 tile (`MazeCreator.RESIZING_FACTOR`).

## Architecture & patterns

This project is intentionally organized around a few core patterns to keep game logic extensible:

### Strategy (ghost AI)

- Strategy: `pacman.model.strategy.GhostChaseStrategy`
- Implementations: `BlinkyChaseStrategy`, `PinkyChaseStrategy`, `InkyChaseStrategy`, `ClydeChaseStrategy`
- Context: `pacman.model.entity.dynamic.ghost.GhostImpl`

### State (ghost modes)

- State: `pacman.model.state.GhostState`
- Implementations: `NormalState`, `FrightenedState`
- Context: `pacman.model.entity.dynamic.ghost.GhostImpl`

### Decorator (dynamic behavior)

- Component: `pacman.model.decorator.Component`
- Base implementation: `pacman.model.entity.dynamic.ghost.GhostImpl`
- Decorators: `pacman.model.decorator.GhostDecorator`, `FrightenedGhostDecorator`

### Factory/Registry (map parsing)

- Map loader: `pacman.model.maze.MazeCreator`
- Registry: `pacman.model.factories.RenderableFactoryRegistryImpl`
- Factories: `pacman.model.factories.WallFactory`, `pacman.model.factories.PelletFactory`, `pacman.model.factories.PacmanFactory`, `pacman.model.factories.GhostFactory`

### Command (input handling)

- Commands: `pacman.view.keyboard.command.Move*Command`
- Invoker: `pacman.model.entity.dynamic.player.MovementInvoker`
- Handler: `pacman.view.keyboard.KeyboardInputHandler`

### Observer (UI updates)

- Subjects: `pacman.model.engine.GameEngineImpl`, `pacman.model.level.LevelImpl`
- Observer: `pacman.view.display.DisplayManager`

## Development

- Run tests: `./gradlew test`
- CI: GitHub Actions workflow in `.github/workflows/ci.yml` (Ubuntu + Windows, JDK 17)

## License

MIT (see `LICENSE`).

## Third-party notices

See `THIRD_PARTY_NOTICES.md`.

## Trademark

See `TRADEMARKS.md`.

## Contributing

See `CONTRIBUTING.md`.

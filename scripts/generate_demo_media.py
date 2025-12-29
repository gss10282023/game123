from __future__ import annotations

from collections import deque
from dataclasses import dataclass
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
RESOURCES_DIR = ROOT / "src" / "main" / "resources"
MAZE_DIR = RESOURCES_DIR / "maze"
DOCS_ASSETS_DIR = ROOT / "docs" / "assets"

TILE_SIZE = 16
PACMAN_OFFSET = (4, -4)
GHOST_OFFSET = (4, -4)


@dataclass(frozen=True)
class Pos:
    x: int
    y: int

    def __add__(self, other: "Pos") -> "Pos":
        return Pos(self.x + other.x, self.y + other.y)


WALL_TILES: dict[str, str] = {
    "1": "walls/horizontal.png",
    "2": "walls/vertical.png",
    "3": "walls/upLeft.png",
    "4": "walls/upRight.png",
    "5": "walls/downLeft.png",
    "6": "walls/downRight.png",
}


def load_image(rel_path: str) -> Image.Image:
    path = MAZE_DIR / rel_path
    return Image.open(path).convert("RGBA")


def read_map() -> list[str]:
    map_path = RESOURCES_DIR / "new-map.txt"
    lines = map_path.read_text(encoding="utf-8").splitlines()
    if not lines:
        raise RuntimeError(f"Map file is empty: {map_path}")

    width = len(lines[0])
    if any(len(line) != width for line in lines):
        raise RuntimeError("Map file has inconsistent line widths.")

    return lines


def is_wall(tile: str) -> bool:
    return tile in WALL_TILES


def find_first(lines: list[str], target: str) -> Pos | None:
    for y, line in enumerate(lines):
        for x, ch in enumerate(line):
            if ch == target:
                return Pos(x, y)
    return None


def find_all(lines: list[str], targets: set[str]) -> dict[str, list[Pos]]:
    out = {t: [] for t in targets}
    for y, line in enumerate(lines):
        for x, ch in enumerate(line):
            if ch in targets:
                out[ch].append(Pos(x, y))
    return out


def neighbors(p: Pos) -> list[Pos]:
    return [p + Pos(1, 0), p + Pos(-1, 0), p + Pos(0, 1), p + Pos(0, -1)]


def in_bounds(lines: list[str], p: Pos) -> bool:
    return 0 <= p.y < len(lines) and 0 <= p.x < len(lines[0])


def walkable(lines: list[str], p: Pos) -> bool:
    if not in_bounds(lines, p):
        return False
    return not is_wall(lines[p.y][p.x])


def bfs_path(lines: list[str], start: Pos, goal: Pos) -> list[Pos]:
    if start == goal:
        return [start]

    q: deque[Pos] = deque([start])
    prev: dict[Pos, Pos] = {}
    seen = {start}

    while q:
        cur = q.popleft()
        for nxt in neighbors(cur):
            if nxt in seen:
                continue
            if not walkable(lines, nxt):
                continue
            seen.add(nxt)
            prev[nxt] = cur
            if nxt == goal:
                q.clear()
                break
            q.append(nxt)

    if goal not in prev:
        return [start]

    path = [goal]
    cur = goal
    while cur != start:
        cur = prev[cur]
        path.append(cur)
    path.reverse()
    return path


def tile_to_px(p: Pos) -> tuple[int, int]:
    return p.x * TILE_SIZE, p.y * TILE_SIZE


def draw_base_board(lines: list[str]) -> Image.Image:
    width_px = len(lines[0]) * TILE_SIZE
    height_px = len(lines) * TILE_SIZE
    canvas = Image.new("RGBA", (width_px, height_px), (0, 0, 0, 255))

    wall_cache: dict[str, Image.Image] = {k: load_image(v) for k, v in WALL_TILES.items()}
    pellet = load_image("pellet.png")

    for y, line in enumerate(lines):
        for x, ch in enumerate(line):
            px, py = x * TILE_SIZE, y * TILE_SIZE
            if ch in wall_cache:
                canvas.alpha_composite(wall_cache[ch], dest=(px, py))
            elif ch == "7":
                canvas.alpha_composite(pellet, dest=(px, py))
            elif ch == "z":
                power = pellet.resize((pellet.width * 2, pellet.height * 2), resample=Image.NEAREST)
                canvas.alpha_composite(power, dest=(px - 8, py - 8))

    return canvas


def overlay_hud(frame: Image.Image, score: int, lives: int) -> None:
    draw = ImageDraw.Draw(frame)
    font_path = MAZE_DIR / "PressStart2P-Regular.ttf"
    try:
        font = ImageFont.truetype(str(font_path), 12)
    except Exception:
        font = ImageFont.load_default()

    draw.text((12, 10), f"SCORE {score}", fill=(255, 255, 255, 255), font=font)
    draw.text((frame.width - 160, 10), "PACMANFX", fill=(255, 255, 255, 255), font=font)
    draw.text((12, frame.height - 26), f"LIVES {lives}", fill=(255, 255, 255, 255), font=font)


def direction_from(a: Pos, b: Pos) -> str:
    dx, dy = b.x - a.x, b.y - a.y
    if abs(dx) > abs(dy):
        return "right" if dx > 0 else "left"
    return "down" if dy > 0 else "up"


def pacman_sprite(direction: str, closed: bool) -> Image.Image:
    if closed:
        return load_image("pacman/playerClosed.png")
    mapping = {
        "right": "pacman/playerRight.png",
        "left": "pacman/playerLeft.png",
        "up": "pacman/playerUp.png",
        "down": "pacman/playerDown.png",
    }
    return load_image(mapping[direction])


def main() -> None:
    DOCS_ASSETS_DIR.mkdir(parents=True, exist_ok=True)

    lines = read_map()
    base = draw_base_board(lines)

    start = find_first(lines, "p")
    if start is None:
        raise RuntimeError("Map has no 'p' (Pacman start) tile.")

    # Choose a rightward goal on the same row to make a simple, readable loop.
    goal = None
    for x in range(len(lines[0]) - 2, start.x, -1):
        candidate = Pos(x, start.y)
        if walkable(lines, candidate):
            goal = candidate
            break
    if goal is None:
        goal = start

    path_out = bfs_path(lines, start, goal)
    path_back = list(reversed(path_out))
    path = path_out + path_back[1:]

    ghosts = {
        "b": load_image("ghosts/blinky.png"),
        "s": load_image("ghosts/pinky.png"),
        "i": load_image("ghosts/inky.png"),
        "c": load_image("ghosts/clyde.png"),
    }
    ghost_positions = find_all(lines, set(ghosts.keys()))

    frames: list[Image.Image] = []
    score = 0
    for i, pos in enumerate(path):
        frame = base.copy()

        # Ghosts (static, based on map).
        for ghost_key, sprite in ghosts.items():
            for gpos in ghost_positions.get(ghost_key, []):
                gx, gy = tile_to_px(gpos)
                frame.alpha_composite(sprite, dest=(gx + GHOST_OFFSET[0], gy + GHOST_OFFSET[1]))

        # Pacman movement + mouth animation.
        nxt = path[(i + 1) % len(path)]
        direction = direction_from(pos, nxt)
        closed = (i // 2) % 2 == 1
        pac = pacman_sprite(direction, closed)
        px, py = tile_to_px(pos)
        frame.alpha_composite(pac, dest=(px + PACMAN_OFFSET[0], py + PACMAN_OFFSET[1]))

        # Simple score ramp for HUD flavor.
        if i % 3 == 0:
            score += 10
        overlay_hud(frame, score=score, lives=3)

        frames.append(frame)

    # Screenshot (first frame)
    screenshot_path = DOCS_ASSETS_DIR / "screenshot-1.png"
    frames[0].save(screenshot_path, format="PNG", optimize=True)

    # PNG preview strip (useful for tools that can't render GIFs)
    strip_indices = [0, len(frames) // 3, (len(frames) * 2) // 3]
    strip_path = DOCS_ASSETS_DIR / "demo-strip.png"
    strip = Image.new("RGBA", (frames[0].width * len(strip_indices), frames[0].height), (0, 0, 0, 255))
    for i, idx in enumerate(strip_indices):
        strip.alpha_composite(frames[idx], dest=(frames[0].width * i, 0))
    strip.save(strip_path, format="PNG", optimize=True)

    # GIF (looping)
    gif_path = DOCS_ASSETS_DIR / "demo.gif"
    gif_frames = []
    for fr in frames:
        pal = fr.convert("P", palette=Image.ADAPTIVE, colors=256)
        gif_frames.append(pal)
    gif_frames[0].save(
        gif_path,
        save_all=True,
        append_images=gif_frames[1:],
        duration=90,
        loop=0,
        optimize=True,
        disposal=2,
    )

    print(f"Wrote {screenshot_path.relative_to(ROOT)}")
    print(f"Wrote {strip_path.relative_to(ROOT)}")
    print(f"Wrote {gif_path.relative_to(ROOT)}")


if __name__ == "__main__":
    main()

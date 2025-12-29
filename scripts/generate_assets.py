from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
ASSETS_DIR = ROOT / "src" / "main" / "resources" / "maze"


@dataclass(frozen=True)
class Rgba:
    r: int
    g: int
    b: int
    a: int = 255

    def as_tuple(self) -> tuple[int, int, int, int]:
        return self.r, self.g, self.b, self.a


PACMAN_YELLOW = Rgba(255, 210, 0)
PACMAN_EYE = Rgba(20, 20, 20)
GHOST_PUPIL = Rgba(60, 120, 255)
WALL_BLUE = Rgba(30, 115, 255)
WALL_BLUE_DARK = Rgba(20, 80, 200)


def ensure_parent(path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)


def save_png(img: Image.Image, path: Path) -> None:
    ensure_parent(path)
    img.save(path, format="PNG", optimize=True)


def transparent_canvas(width: int, height: int) -> Image.Image:
    return Image.new("RGBA", (width, height), (0, 0, 0, 0))


def draw_pacman(size: tuple[int, int], direction: str, closed: bool) -> Image.Image:
    img = transparent_canvas(*size)
    draw = ImageDraw.Draw(img)
    bbox = (0, 0, size[0] - 1, size[1] - 1)

    if closed:
        draw.ellipse(bbox, fill=PACMAN_YELLOW.as_tuple())
    else:
        direction_to_angle = {"right": 0, "up": 90, "left": 180, "down": 270}
        center = direction_to_angle[direction]
        mouth_half_angle = 30
        start = (center + mouth_half_angle) % 360
        end = (center - mouth_half_angle) % 360
        draw.pieslice(bbox, start=start, end=end, fill=PACMAN_YELLOW.as_tuple())

        # Small eye (kept subtle; avoids exact Pac-Man likeness).
        eye_x = int(size[0] * 0.62)
        eye_y = int(size[1] * 0.28)
        r = max(1, min(size) // 14)
        draw.ellipse((eye_x - r, eye_y - r, eye_x + r, eye_y + r), fill=PACMAN_EYE.as_tuple())

    return img


def draw_ghost(size: tuple[int, int], body: Rgba, frightened: bool = False) -> Image.Image:
    w, h = size
    img = transparent_canvas(w, h)
    draw = ImageDraw.Draw(img)

    body_bbox = (2, 2, w - 3, h - 3)
    radius = max(6, min(w, h) // 3)
    draw.rounded_rectangle(body_bbox, radius=radius, fill=body.as_tuple())

    # Bottom "fringe"
    fringe_y = h - 6
    for i in range(4):
        x0 = 2 + i * (w - 4) // 4
        x1 = 2 + (i + 1) * (w - 4) // 4
        draw.rectangle((x0, fringe_y, x1, h - 3), fill=body.as_tuple())

    # Eyes
    eye_w = max(4, w // 6)
    eye_h = max(6, h // 5)
    left_eye = (w * 3 // 10 - eye_w // 2, h * 2 // 5 - eye_h // 2)
    right_eye = (w * 7 // 10 - eye_w // 2, h * 2 // 5 - eye_h // 2)

    for (ex, ey) in (left_eye, right_eye):
        draw.ellipse((ex, ey, ex + eye_w, ey + eye_h), fill=(255, 255, 255, 255))
        if not frightened:
            pr = max(1, eye_w // 4)
            px = ex + eye_w * 2 // 3 - pr
            py = ey + eye_h // 2 - pr
            draw.ellipse((px, py, px + pr * 2, py + pr * 2), fill=GHOST_PUPIL.as_tuple())

    return img


def draw_pellet(size: tuple[int, int]) -> Image.Image:
    img = transparent_canvas(*size)
    draw = ImageDraw.Draw(img)
    w, h = size
    r = max(2, min(w, h) // 8)
    cx, cy = w // 2, h // 2
    draw.ellipse((cx - r, cy - r, cx + r, cy + r), fill=(245, 245, 245, 255))
    return img


def draw_wall_tile(kind: str) -> Image.Image:
    img = transparent_canvas(16, 16)
    draw = ImageDraw.Draw(img)

    thickness = 4
    mid = 8

    def h_line(x0: int, x1: int, y: int) -> None:
        draw.rectangle((x0, y - thickness // 2, x1, y + thickness // 2), fill=WALL_BLUE.as_tuple())
        draw.rectangle((x0, y - thickness // 2, x1, y - thickness // 2 + 1), fill=WALL_BLUE_DARK.as_tuple())

    def v_line(x: int, y0: int, y1: int) -> None:
        draw.rectangle((x - thickness // 2, y0, x + thickness // 2, y1), fill=WALL_BLUE.as_tuple())
        draw.rectangle((x - thickness // 2, y0, x - thickness // 2 + 1, y1), fill=WALL_BLUE_DARK.as_tuple())

    if kind == "horizontal":
        h_line(0, 15, mid)
    elif kind == "vertical":
        v_line(mid, 0, 15)
    elif kind == "up_left":
        v_line(mid, 0, mid)
        h_line(0, mid, mid)
    elif kind == "up_right":
        v_line(mid, 0, mid)
        h_line(mid, 15, mid)
    elif kind == "down_left":
        v_line(mid, mid, 15)
        h_line(0, mid, mid)
    elif kind == "down_right":
        v_line(mid, mid, 15)
        h_line(mid, 15, mid)
    else:
        raise ValueError(f"Unknown wall kind: {kind}")

    return img


def main() -> None:
    # Pacman
    save_png(draw_pacman((24, 26), "left", closed=False), ASSETS_DIR / "pacman" / "playerLeft.png")
    save_png(draw_pacman((24, 26), "right", closed=False), ASSETS_DIR / "pacman" / "playerRight.png")
    save_png(draw_pacman((26, 24), "up", closed=False), ASSETS_DIR / "pacman" / "playerUp.png")
    save_png(draw_pacman((26, 24), "down", closed=False), ASSETS_DIR / "pacman" / "playerDown.png")
    save_png(draw_pacman((24, 26), "right", closed=True), ASSETS_DIR / "pacman" / "playerClosed.png")

    # Ghosts
    save_png(draw_ghost((28, 28), Rgba(235, 60, 60)), ASSETS_DIR / "ghosts" / "blinky.png")
    save_png(draw_ghost((28, 28), Rgba(255, 160, 200)), ASSETS_DIR / "ghosts" / "pinky.png")
    save_png(draw_ghost((28, 28), Rgba(65, 230, 255)), ASSETS_DIR / "ghosts" / "inky.png")
    save_png(draw_ghost((28, 28), Rgba(255, 170, 60)), ASSETS_DIR / "ghosts" / "clyde.png")
    save_png(draw_ghost((28, 28), Rgba(40, 90, 255), frightened=True), ASSETS_DIR / "ghosts" / "frightened.png")

    # Pellet
    save_png(draw_pellet((16, 16)), ASSETS_DIR / "pellet.png")

    # Walls
    save_png(draw_wall_tile("horizontal"), ASSETS_DIR / "walls" / "horizontal.png")
    save_png(draw_wall_tile("vertical"), ASSETS_DIR / "walls" / "vertical.png")
    save_png(draw_wall_tile("up_left"), ASSETS_DIR / "walls" / "upLeft.png")
    save_png(draw_wall_tile("up_right"), ASSETS_DIR / "walls" / "upRight.png")
    save_png(draw_wall_tile("down_left"), ASSETS_DIR / "walls" / "downLeft.png")
    save_png(draw_wall_tile("down_right"), ASSETS_DIR / "walls" / "downRight.png")

    # Unused but shipped asset: keep licensing clean.
    save_png(Image.new("RGBA", (16, 2), WALL_BLUE.as_tuple()), ASSETS_DIR / "walls" / "horizontal-1.png")

    print(f"Generated assets into: {ASSETS_DIR}")


if __name__ == "__main__":
    main()


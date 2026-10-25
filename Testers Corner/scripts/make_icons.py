"""Generate the Testers’ Corner icons (16, 48, 128 px) for the theme (repo root) and the New Tab page.

Usage:  python3 scripts/make_icons.py      (needs Pillow: pip install pillow)
Built by Santhosh Tuppad.
"""
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
SIZES = (16, 48, 128)
S = 1024  # draw large, then downscale for smooth edges

TILE = (17, 21, 29, 255)
BORDER = (47, 56, 70, 255)
TEAL = (79, 209, 197, 255)
INK = (6, 35, 32, 255)


def draw_icon() -> Image.Image:
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    # Rounded tile
    d.rounded_rectangle((32, 32, S - 32, S - 32), radius=220, fill=TILE, outline=BORDER, width=20)

    # "Start" play triangle, optically centred
    cx, cy, r = S * 0.53, S * 0.5, S * 0.30
    tri = [(cx - r * 0.75, cy - r), (cx - r * 0.75, cy + r), (cx + r * 0.95, cy)]
    d.polygon(tri, fill=TEAL)

    # "Test" check mark inside the triangle
    w = int(S * 0.045)
    check = [(cx - r * 0.42, cy + r * 0.02), (cx - r * 0.12, cy + r * 0.30), (cx + r * 0.38, cy - r * 0.26)]
    d.line(check, fill=INK, width=w, joint="curve")
    for x, y in (check[0], check[-1]):
        d.ellipse((x - w / 2, y - w / 2, x + w / 2, y + w / 2), fill=INK)
    return img


def main() -> None:
    big = draw_icon()
    for out in (ROOT / "icons", ROOT / "new-tab" / "icons"):
        out.mkdir(parents=True, exist_ok=True)
        for size in SIZES:
            big.resize((size, size), Image.LANCZOS).save(out / f"icon-{size}.png")
    print("Icons written to icons/ and new-tab/icons/")


if __name__ == "__main__":
    main()

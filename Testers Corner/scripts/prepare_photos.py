"""Prepare portrait photos for the people row so they all look alike.

Each photo is turned upright, cropped to a square (keeping the upper part, where faces
usually are), resized to 400x400, and brought to the same brightness and contrast.
The results go to new-tab/people/<slug>.jpg and new-tab/people.js is updated to use them.

Usage:  python3 scripts/prepare_photos.py <folder-with-original-photos>
        (needs Pillow: pip install pillow)

Name the originals by slug, e.g. james-bach.jpg, cem-kaner.png. Slugs are listed in
new-tab/people.js. Only use photos you have permission to share.

Optional framing: so every face fills its circle the same way, add framing.json to the
folder with each head's centre and height in pixels (top of head to chin) in the original:
    {"james-bach": {"x": 255, "y": 222, "head": 415}}
Photos without an entry get a simple square crop. Where a photo is too small for the
framing, a blurred copy of its own background fills the gap.
Built by Santhosh Tuppad.
"""
import json
import re
import sys
from pathlib import Path

from PIL import Image, ImageEnhance, ImageFilter, ImageOps, ImageStat

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "new-tab" / "people"
PEOPLE_JS = ROOT / "new-tab" / "people.js"
SIZE = 400
TARGET_BRIGHTNESS = 118  # mean luminance, 0-255
HEAD_FILL = 0.85  # head height as a share of the frame
HEAD_CENTRE = 0.46  # where the head centre sits, from the top of the frame
EXTENSIONS = (".jpg", ".jpeg", ".png", ".webp")


def square_crop(img: Image.Image) -> Image.Image:
    w, h = img.size
    side = min(w, h)
    left = (w - side) // 2
    top = int((h - side) * 0.2)  # portraits: keep the head, not the middle of the torso
    return img.crop((left, top, left + side, top + side))


def framed_crop(img: Image.Image, x: float, y: float, head: float) -> Image.Image:
    side = round(head / HEAD_FILL)
    left, top = round(x - side / 2), round(y - side * HEAD_CENTRE)
    # Background: the photo itself, scaled to cover the frame and blurred, for any gaps.
    scale = max(side / img.width, side / img.height)
    bg = img.resize((round(img.width * scale) + 1, round(img.height * scale) + 1), Image.LANCZOS)
    bg = bg.crop((0, 0, side, side)).filter(ImageFilter.GaussianBlur(side / 25))
    box = (max(left, 0), max(top, 0), min(left + side, img.width), min(top + side, img.height))
    bg.paste(img.crop(box), (box[0] - left, box[1] - top))
    return bg


def prepare(src: Path, frame: dict | None = None) -> Image.Image:
    img = ImageOps.exif_transpose(Image.open(src)).convert("RGB")
    img = framed_crop(img, frame["x"], frame["y"], frame["head"]) if frame else square_crop(img)
    img = img.resize((SIZE, SIZE), Image.LANCZOS)
    img = ImageOps.autocontrast(img, cutoff=1)
    mean = ImageStat.Stat(img.convert("L")).mean[0] or 1
    img = ImageEnhance.Brightness(img).enhance(max(0.7, min(1.4, TARGET_BRIGHTNESS / mean)))
    return img.filter(ImageFilter.UnsharpMask(radius=1.2, percent=60, threshold=2))


def main() -> None:
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    src_dir = Path(sys.argv[1]).expanduser()
    js = PEOPLE_JS.read_text(encoding="utf-8")
    slugs = re.findall(r'slug: "([a-z0-9-]+)"', js)
    framing_file = src_dir / "framing.json"
    framing = json.loads(framing_file.read_text()) if framing_file.exists() else {}
    OUT.mkdir(parents=True, exist_ok=True)

    for slug in slugs:
        src = next((p for p in src_dir.iterdir() if p.stem.lower() == slug and p.suffix.lower() in EXTENSIONS), None)
        if not src:
            print(f"  skip  {slug}: no {slug}.jpg/.png/.webp in {src_dir}")
            continue
        prepare(src, framing.get(slug)).save(OUT / f"{slug}.jpg", "JPEG", quality=86, optimize=True, progressive=True)
        js = re.sub(rf'(slug: "{slug}",[\s\S]*?photo: )(null|"[^"]*")', rf'\1"people/{slug}.jpg"', js, count=1)
        print(f"  done  {slug}: {src.name} -> new-tab/people/{slug}.jpg")

    PEOPLE_JS.write_text(js, encoding="utf-8")
    print("Updated new-tab/people.js. Reload the extension in chrome://extensions to see the photos.")


if __name__ == "__main__":
    main()

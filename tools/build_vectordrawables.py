#!/usr/bin/env python3
"""Download Lucide SVG icons and convert them to Android VectorDrawable XML.

Sources: https://github.com/lucide-icons/lucide (ISC licence — see
docs/icon-system/LICENSE-lucide.txt).
Every glyph is kept byte-for-byte from upstream where possible; only the
SVG container is translated into Android's <vector>/<path> vocabulary.
"""
import os
import re
import sys
import urllib.request
import xml.etree.ElementTree as ET

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RAW = "https://raw.githubusercontent.com/lucide-icons/lucide/main/icons/{}.svg"
SVG_DIR = os.path.join(ROOT, "docs", "icon-system", "lucide-svg")
VD_DIR = os.path.join(ROOT, "res", "drawable")

# Our semantic name -> upstream Lucide icon file name.
MAP = {
    "home": "house",
    "folder": "folder",
    "tools": "wrench",
    "settings": "settings-2",
    "doc": "file-text",
    "book": "book-open",
    "merge": "combine",
    "scissors": "scissors",
    "compress": "minimize-2",
    "scan": "scan-line",
    "image": "image",
    "storage": "hard-drive",
    "camera": "camera",
    "pen": "pen-line",
    "highlighter": "highlighter",
    "signature": "signature",
    "palette": "palette",
    "back": "chevron-left",
    "chevron": "chevron-right",
    "star": "star",
    "moon": "moon",
    "sun": "sun",
    "hash": "hash",
    "rotate": "rotate-cw",
    "trash": "trash",
    "search": "search",
    "zoomin": "zoom-in",
    "zoomout": "zoom-out",
    "plus": "plus",
    "minus": "minus",
    "close": "x",
    "check": "check",
    "alert": "triangle-exclamation-point",
    "info": "info",
    "shield": "shield",
    "bolt": "zap",
    "share": "share-2",
    "more": "ellipsis",
    "grid": "grid-2x2",
    "up": "arrow-up",
    "down": "arrow-down",
    "select": "square-check",
    "unselect": "square",
}

# Aliases that reuse an existing glyph but keep their own semantic resource.
ALIAS = {
    "files": "folder",
    "link": "merge",
    "edit": "pen",
    "prev": "back",
    "next": "chevron",
    "zoom_in": "zoomin",
    "zoom_out": "zoomout",
    "warning": "alert",
}

WHITE = "#FFFFFF"
NONE = "@android:color/transparent"

SVG_NS = "{http://www.w3.org/2000/svg}"


def download(name):
    os.makedirs(SVG_DIR, exist_ok=True)
    path = os.path.join(SVG_DIR, name + ".svg")
    if not os.path.isfile(path):
        url = RAW.format(name)
        with urllib.request.urlopen(url, timeout=25) as resp:
            data = resp.read()
        with open(path, "wb") as fh:
            fh.write(data)
    return path


def num(v):
    if isinstance(v, (int, float)):
        v = str(v)
    v = (v or "0").strip()
    if re.fullmatch(r"[-+]?\d*\.?\d+", v):
        f = float(v)
        return str(int(f)) if f == int(f) else str(f)
    return v


def circle_path(el):
    cx, cy, r = num(el.get("cx")), num(el.get("cy")), num(el.get("r"))
    return f"M{cx},{cy} m-{r},0 a{r},{r} 0 1,0 {float(r)*2},0 a{r},{r} 0 1,0 {-float(r)*2},0"


def line_path(el):
    return f"M{num(el.get('x1'))},{num(el.get('y1'))} L{num(el.get('x2'))},{num(el.get('y2'))}"


def rect_path(el):
    x, y = float(el.get("x", 0)), float(el.get("y", 0))
    w, h = float(el.get("width", 0)), float(el.get("height", 0))
    rx = float(el.get("rx", el.get("ry", 0)))
    ry = float(el.get("ry", rx))
    if rx <= 0 or ry <= 0:
        return f"M{num(x)},{num(y)} h{num(w)} v{num(h)} h{-w:g} Z"
    return (f"M{num(x+rx)},{num(y)} h{num(w-2*rx)} a{num(rx)},{num(ry)} 0 0,1 {num(rx)},{num(ry)} "
            f"v{num(h-2*ry)} a{num(rx)},{num(ry)} 0 0,1 {num(-rx)},{num(ry)} "
            f"h{num(-(w-2*rx))} a{num(rx)},{num(ry)} 0 0,1 {num(-rx)},{num(-ry)} "
            f"v{num(-(h-2*ry))} a{num(rx)},{num(ry)} 0 0,1 {num(rx)},{num(-ry)} Z")


def points_path(el, close):
    pts = re.findall(r"[-+]?\d*\.?\d+", el.get("points", ""))
    if len(pts) < 4:
        return ""
    out = "M" + ",".join(num(p) for p in pts[:2])
    for i in range(2, len(pts) - 1, 2):
        out += " L" + num(pts[i]) + "," + num(pts[i + 1])
    return out + (" Z" if close else "")


def element_to_vector(el, stroke_w):
    tag = el.tag.replace(SVG_NS, "")
    if tag == "g":
        return []
    fill = el.get("fill", "none")
    stroke = el.get("stroke", "currentColor")
    is_filled = fill not in ("none", "transparent", "") and stroke == "none"
    stroke_width = num(el.get("stroke-width", str(stroke_w)))
    out = []
    if tag == "path":
        data = el.get("d", "").strip()
    elif tag == "circle":
        data = circle_path(el)
    elif tag == "line":
        data = line_path(el)
    elif tag == "rect":
        data = rect_path(el)
    elif tag == "polyline":
        data = points_path(el, False)
    elif tag == "polygon":
        data = points_path(el, True)
    else:
        return []
    if not data:
        return []
    if is_filled:
        out.append(f'    <path android:pathData="{data}" android:fillColor="{WHITE}"/>')
    else:
        out.append(
            f'    <path android:pathData="{data}" android:fillColor="{NONE}" '
            f'android:strokeColor="{WHITE}" android:strokeWidth="{stroke_width}" '
            f'android:strokeLineCap="round" android:strokeLineJoin="round"/>'
        )
    return out


def convert(src, dest, name):
    tree = ET.parse(src)
    root = tree.getroot()
    body = []
    # Flatten groups: upstream groups only carry inherited stroke attributes.
    for el in root.iter():
        if el is root:
            continue
        body.extend(element_to_vector(el, 2))
    if not body:
        raise RuntimeError(f"no drawable elements found in {src}")
    xml = (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        f'<!-- {name}: converted from Lucide (ISC). Do not edit by hand;\n'
        '     regenerate with tools/build_vectordrawables.py -->\n'
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        '    android:width="24dp" android:height="24dp"\n'
        '    android:viewportWidth="24" android:viewportHeight="24">\n'
        + "\n".join(body) + "\n</vector>\n"
    )
    with open(dest, "w", encoding="utf-8") as fh:
        fh.write(xml)
    return len(body)


def main():
    os.makedirs(VD_DIR, exist_ok=True)
    all_names = dict(MAP)
    all_names.update({k: MAP[v] for k, v in ALIAS.items()})
    ok, fail = [], []
    for semantic, upstream in sorted(all_names.items()):
        try:
            svg = download(upstream)
            total = convert(svg, os.path.join(VD_DIR, "ic_" + semantic + ".xml"), upstream)
            ok.append((semantic, upstream, total))
        except Exception as exc:  # noqa: BLE001 - report every failure
            fail.append((semantic, upstream, str(exc)))
    for semantic, upstream, n in ok:
        print(f"OK   {semantic:12} <- {upstream:16} ({n} paths)")
    for semantic, upstream, err in fail:
        print(f"FAIL {semantic:12} <- {upstream:16} {err}", file=sys.stderr)
    print(f"\n{len(ok)} converted, {len(fail)} failed")
    return 1 if fail else 0


if __name__ == "__main__":
    sys.exit(main())

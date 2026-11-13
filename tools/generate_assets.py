"""Generates every texture (PNG) and sound (OGG) used by Pixel Stats.
Run:  python3 tools/generate_assets.py   (needs: pillow numpy soundfile)
All art/sounds are procedurally created here -> 100% original, no copyright issues."""
import math, os, random
import numpy as np, soundfile as sf
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
GUI = os.path.join(ROOT, "assets", "pixelstats", "textures", "gui")
SND = os.path.join(ROOT, "assets", "pixelstats", "sounds")
os.makedirs(GUI, exist_ok=True); os.makedirs(SND, exist_ok=True)

# ------------------------------------------------------------------ textures
def img(w, h): return Image.new("RGBA", (w, h), (0, 0, 0, 0))
def save(im, name, folder=GUI): im.save(os.path.join(folder, name))

def outline(im, col=(22, 16, 30, 255)):
    px = im.load(); w, h = im.size; add = []
    for y in range(h):
        for x in range(w):
            if px[x, y][3] == 0:
                for dx, dy in ((1,0),(-1,0),(0,1),(0,-1)):
                    nx, ny = x+dx, y+dy
                    if 0 <= nx < w and 0 <= ny < h and px[nx, ny][3] > 0:
                        add.append((x, y)); break
    for p in add: px[p] = col
    return im

def panel():  # 32x32, 9-slice border 8
    im = img(32, 32); d = ImageDraw.Draw(im)
    d.rectangle([0,0,31,31], fill=(12,9,20,255))                    # outer black
    d.rectangle([1,1,30,30], fill=(226,182,74,255))                 # gold
    d.rectangle([2,2,29,29], fill=(122,84,34,255))                  # dark gold
    d.rectangle([3,3,28,28], fill=(70,74,128,255))                  # light bevel
    d.rectangle([4,4,27,27], fill=(18,19,40,255))                   # shadow
    d.rectangle([5,5,26,26], fill=(30,33,62,255))                   # body
    for (cx, cy) in ((5,5),(26,5),(5,26),(26,26)):                  # corner diamonds
        for dx, dy in ((0,0),(1,0),(-1,0),(0,1),(0,-1)):
            im.putpixel((cx+dx, cy+dy), (255,226,140,255))
    for (x, y) in ((0,0),(31,0),(0,31),(31,31)): im.putpixel((x,y),(0,0,0,0))   # notched corners
    return im

def row(hover=False):  # 16x16, 9-slice border 3
    im = img(16, 16); d = ImageDraw.Draw(im)
    edge = (236,196,96,255) if hover else (12,10,22,255)
    body = (54,60,112,255) if hover else (38,42,80,255)
    d.rectangle([0,0,15,15], fill=edge)
    d.rectangle([1,1,14,14], fill=(22,24,50,255))
    d.line([(1,1),(14,1)], fill=(92,100,170,255)); d.line([(1,1),(1,14)], fill=(92,100,170,255))
    d.rectangle([2,2,13,13], fill=body)
    for (x, y) in ((0,0),(15,0),(0,15),(15,15)): im.putpixel((x,y),(0,0,0,0))
    return im

def banner():  # 128x24
    im = img(128, 24); d = ImageDraw.Draw(im)
    gold, dg, red, dr, lr = (236,196,96,255), (130,90,36,255), (150,38,52,255), (96,24,36,255), (205,70,82,255)
    d.polygon([(0,8),(14,8),(14,22),(0,22),(6,15)], fill=dr, outline=dg)      # left tail
    d.polygon([(127,8),(113,8),(113,22),(127,22),(121,15)], fill=dr, outline=dg)
    d.rectangle([10,2,117,18], fill=gold)
    d.rectangle([11,3,116,17], fill=dg)
    d.rectangle([12,4,115,16], fill=red)
    d.line([(12,5),(115,5)], fill=lr)
    d.line([(12,16),(115,16)], fill=dr)
    for x in (16, 111):                                                      # little gems
        d.rectangle([x-1,9,x+1,11], fill=(120,220,255,255)); im.putpixel((x,10),(255,255,255,255))
    return outline(im, (14,10,18,255))

def button():  # 20x80 : normal / hover / pressed / disabled
    im = img(20, 80); d = ImageDraw.Draw(im)
    states = [((84,176,92),(140,230,150),(44,110,56),(255,255,255)),
              ((112,214,122),(176,255,184),(60,138,72),(255,255,255)),
              ((58,132,70),(44,104,56),(90,170,100),(230,230,230)),
              ((78,78,92),(110,110,124),(52,52,62),(150,150,160))]
    for i, (face, hi, lo, plus) in enumerate(states):
        y0 = i*20
        d.rectangle([0,y0,19,y0+19], fill=(12,10,22,255))
        d.rectangle([1,y0+1,18,y0+18], fill=lo+(255,))
        d.rectangle([1,y0+1,17,y0+17], fill=hi+(255,))
        d.rectangle([2,y0+2,17,y0+17], fill=face+(255,))
        off = 1 if i == 2 else 0
        d.rectangle([5,y0+9+off,14,y0+10+off], fill=plus+(255,))
        d.rectangle([9,y0+5+off,10,y0+14+off], fill=plus+(255,))
        for (x, y) in ((0,0),(19,0),(0,19),(19,19)): im.putpixel((x,y0+y),(0,0,0,0))
    return im

def icon_str():
    im = img(16,16); d = ImageDraw.Draw(im)
    d.line([(12,3),(6,9)], fill=(214,222,236,255), width=3)
    d.line([(11,4),(5,10)], fill=(150,160,182,255), width=1)
    d.line([(3,8),(8,13)], fill=(236,190,70,255), width=2)
    d.line([(5,10),(2,13)], fill=(138,84,40,255), width=2)
    d.point((13,2), fill=(255,255,255,255)); d.point((12,3), fill=(255,255,255,255))
    return outline(im)

def icon_dex():
    im = img(16,16); d = ImageDraw.Draw(im)
    d.line([(3,13),(12,4)], fill=(176,124,70,255), width=1)               # shaft
    d.polygon([(14,1),(9,4),(11,6)], fill=(210,218,232,255))              # head
    d.line([(2,10),(5,13)], fill=(110,230,110,255), width=1)              # fletching
    d.line([(2,12),(4,14)], fill=(70,180,80,255), width=1)
    d.line([(4,10),(6,12)], fill=(70,180,80,255), width=1)
    return outline(im)

def icon_con():
    im = img(16,16); d = ImageDraw.Draw(im)
    d.polygon([(2,2),(13,2),(13,8),(8,14),(7,14),(2,8)], fill=(224,150,52,255))
    d.polygon([(4,4),(11,4),(11,8),(8,12),(7,12),(4,8)], fill=(255,196,96,255))
    d.line([(7,5),(7,11)], fill=(168,96,34,255)); d.line([(8,5),(8,11)], fill=(168,96,34,255))
    d.line([(5,7),(10,7)], fill=(168,96,34,255))
    d.point((4,4), fill=(255,236,170,255))
    return outline(im)

def icon_int():
    im = img(16,16); d = ImageDraw.Draw(im)
    d.ellipse([2,3,12,13], fill=(70,126,240,255))
    d.ellipse([3,4,10,10], fill=(110,166,255,255))
    for p in ((5,5),(6,5),(5,6)): d.point(p, fill=(255,255,255,255))
    for p in ((13,1),(13,2),(13,3),(12,2),(14,2)): d.point(p, fill=(255,240,160,255))
    return outline(im)

def icon_luc():
    im = img(16,16); d = ImageDraw.Draw(im)
    g, lg = (62,178,78,255), (130,230,120,255)
    for box in ((5,1,10,6),(5,8,10,13),(1,5,6,10),(9,5,14,10)): d.ellipse(box, fill=g)
    d.rectangle([6,6,9,9], fill=lg)
    d.line([(8,10),(11,14)], fill=(40,120,50,255), width=1)
    d.point((6,3), fill=lg); d.point((3,7), fill=lg)
    return outline(im)

def icon_xp():
    im = img(16,16); d = ImageDraw.Draw(im)
    d.ellipse([2,2,13,13], fill=(150,230,40,255))
    d.ellipse([4,4,10,10], fill=(210,255,90,255))
    for p in ((5,5),(6,5),(5,6)): d.point(p, fill=(255,255,255,255))
    return outline(im)

def logo():
    im = Image.new("RGBA", (128,128), (30,33,62,255)); d = ImageDraw.Draw(im)
    d.rectangle([0,0,127,127], outline=(12,9,20,255)); d.rectangle([2,2,125,125], outline=(226,182,74,255), width=2)
    d.rectangle([5,5,122,122], outline=(122,84,34,255))
    for k, (f, pos) in enumerate(((icon_dex,(14,14)),(icon_str,(70,14)),(icon_con,(14,70)),(icon_int,(70,70)))):
        im.alpha_composite(f().resize((44,44), Image.NEAREST), pos)
    im.alpha_composite(icon_luc().resize((32,32), Image.NEAREST), (48,48))
    return im

save(panel(), "panel.png"); save(row(), "row.png"); save(row(True), "row_hover.png")
save(banner(), "banner.png"); save(button(), "button.png")
for n, f in (("str",icon_str),("dex",icon_dex),("con",icon_con),("int",icon_int),("luc",icon_luc),("xp",icon_xp)):
    save(f(), f"icon_{n}.png")
save(logo(), "logo.png", ROOT)

# -------------------------------------------------------------------- sounds
SR = 44100
def t(dur): return np.linspace(0, dur, int(SR*dur), endpoint=False)
def square(f, dur, duty=0.5): return np.where((t(dur)*f) % 1.0 < duty, 1.0, -1.0)
def tri(f, dur): return 2*np.abs(2*((t(dur)*f) % 1.0) - 1) - 1
def sine(f, dur): return np.sin(2*math.pi*f*t(dur))
def env(x, atk=0.004, rel=0.6):
    n = len(x); a = max(1, int(atk*SR)); e = np.ones(n)
    e[:a] = np.linspace(0, 1, a)
    e[a:] = np.exp(-np.linspace(0, 6*(1.0/max(rel,0.05)), n-a))
    return x*e
def seq(notes, dur, wave=square, vol=0.5, rel=0.5, **kw):
    return np.concatenate([env(wave(f, dur, **kw)*vol, rel=rel) for f in notes])
def write(name, x, peak=0.8):
    x = np.asarray(x, dtype=np.float32); x = x / max(1e-6, np.max(np.abs(x))) * peak
    x = x * np.linspace(1, 1, len(x)); x[-200:] *= np.linspace(1, 0, 200)
    sf.write(os.path.join(SND, name + ".ogg"), x, SR, format="OGG", subtype="VORBIS")

n = lambda semi: 440.0 * 2 ** ((semi - 9) / 12)         # semitone from C4 -> Hz  (A4 = 440)
C5, E5, G5, B5, C6, E6, G6 = n(12), n(16), n(19), n(23), n(24), n(28), n(31)

write("ui_open",    np.concatenate([seq([C5,E5,G5,C6], 0.055, square, 0.45, 0.25, duty=0.25), env(tri(C6*2,0.18)*0.35, rel=0.25)]))
write("ui_close",   seq([G5,E5,C5], 0.06, tri, 0.6, 0.3))
write("ui_hover",   env(square(1500, 0.03, 0.125)*0.3, atk=0.002, rel=0.08))
write("ui_click",   np.concatenate([env(square(660,0.035,0.5)*0.5, rel=0.1), env(square(990,0.05,0.5)*0.5, rel=0.12)]))
shimmer = env((sine(E6,0.40)+0.5*sine(E6*2,0.40)+0.25*sine(E6*3,0.40))*0.5*(1+0.15*sine(9,0.40)), rel=0.5)
write("ui_upgrade", np.concatenate([seq([E5,G5,B5], 0.06, square, 0.40, 0.3, duty=0.25), shimmer]))
write("ui_deny",    np.concatenate([env(square(150,0.09,0.5)*0.6, rel=0.2), np.zeros(int(0.02*SR)), env(square(110,0.13,0.5)*0.6, rel=0.2)]))

rng = np.random.default_rng(7)                       # dodge = airy whoosh + falling pitch
dur = 0.30; nz = rng.standard_normal(int(SR*dur)); y = np.zeros_like(nz); lp = 0.0
for i in range(len(nz)):
    k = 0.04 + 0.5*math.sin(math.pi*i/len(nz))       # filter opens then closes
    lp += k*(nz[i]-lp); y[i] = nz[i]*0.0 + (nz[i]-lp)*0.7 + lp*0.3
sw = t(dur); swish = np.sin(2*math.pi*(900*sw - 1400*sw**2/2))
shape = np.sin(math.pi*np.linspace(0,1,len(y)))**1.5
write("dodge", (y*0.8 + swish*0.25)*shape)
print("assets generated")

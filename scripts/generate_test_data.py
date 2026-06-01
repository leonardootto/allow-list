#!/usr/bin/env python3
import json
import os

OUTPUT_DIR = "src/main/resources/lists"
LIST_COUNT = 20
PVS_PER_LIST = 500_000

os.makedirs(OUTPUT_DIR, exist_ok=True)

for i in range(LIST_COUNT):
    name = "merchants" if i == 0 else f"merchants-{i + 1}"
    start = i * PVS_PER_LIST + 1
    pvs = list(range(start, start + PVS_PER_LIST))
    path = os.path.join(OUTPUT_DIR, f"{name}.json")
    with open(path, "w") as f:
        json.dump({"pvs": pvs}, f)
    print(f"Generated {path} ({len(pvs)} PVs, range {start}–{start + PVS_PER_LIST - 1})")

print(f"\nDone: {LIST_COUNT} lists × {PVS_PER_LIST:,} PVs = {LIST_COUNT * PVS_PER_LIST:,} total PVs")

#!/usr/bin/env python3
import json
import re
import sys
from pathlib import Path

BENCH_FILE = Path("results/benchmark-results.json")
K6_FILE    = Path("results/summary.json")
PRES_FILE  = Path("presentation/index.html")

for p in (BENCH_FILE, K6_FILE, PRES_FILE):
    if not p.exists():
        print(f"Error: {p} not found", file=sys.stderr)
        sys.exit(1)

bench = json.loads(BENCH_FILE.read_text())
k6    = json.loads(K6_FILE.read_text())

results = {"benchmark": bench, "k6": k6}

block = (
    "// RESULTS_START\n"
    f"const RESULTS = {json.dumps(results, indent=2)};\n"
    "// RESULTS_END"
)

html = PRES_FILE.read_text()
html, n = re.subn(
    r"// RESULTS_START.*?// RESULTS_END",
    block,
    html,
    flags=re.DOTALL,
)

if n == 0:
    print("Error: RESULTS_START/END markers not found in presentation", file=sys.stderr)
    sys.exit(1)

PRES_FILE.write_text(html)
print(f"Presentation updated ({n} replacement(s))")
print(f"  benchmark p99 : {bench['p99_us']} μs")
print(f"  k6 p99        : {k6['p99_ms']} ms")

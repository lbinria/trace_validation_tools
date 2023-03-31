import os
import ndjson
import argparse
from functools import reduce
from itertools import groupby

def read_trace(filename):
    with open(filename) as f:
        return ndjson.load(f)

def run(files):
    all_paths = reduce(lambda a, b: a + b, ([f] if os.path.isfile(f) else [path for path in os.listdir(f) if path.endswith('.ndjson')] for f in files))
    # Open trace files and concatenate events
    merged_trace = reduce(lambda a, b: a + b, (read_trace(path) for path in all_paths), [])
    # Dump
    return ndjson.dumps(merged_trace)

if __name__ == "__main__":
    # Read program args
    parser = argparse.ArgumentParser(description="")
    parser.add_argument('files', type=str, nargs="+", help="Trace files to merge")
    args = parser.parse_args()
    # Print output
    print(run(args.files))
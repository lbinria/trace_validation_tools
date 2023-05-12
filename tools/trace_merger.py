import os
import ndjson
import argparse
from functools import reduce
from itertools import groupby

def read_trace(filename):
    with open(filename) as f:
        return ndjson.load(f)

def run(files, config=None, sort=False):
    all_paths = reduce(lambda a, b: a + b, ([f] if os.path.isfile(f) else [os.path.join(f, filename) for filename in os.listdir(f) if filename.endswith('.ndjson')] for f in files))
    # Open trace files and concatenate events
    merged_trace = reduce(lambda a, b: a + b, (read_trace(path) for path in all_paths), [])

    if sort:
        merged_trace = list(sorted(merged_trace, key=lambda x: x['clock']))

    # Append config line at beginning of the trace file
    if config:
        merged_trace = [read_trace(config)[0]] + merged_trace

    # Dump
    return ndjson.dumps(merged_trace)

if __name__ == "__main__":
    # Read program args
    parser = argparse.ArgumentParser(description="")
    parser.add_argument('files', type=str, nargs="+", help="Trace files to merge")
    parser.add_argument('--sort', type=bool, required=False, default=False, help="Sort by clock")
    parser.add_argument('--config', type=str, required=False, default=None, help="Config file")
    args = parser.parse_args()
    # Print output
    print(run(args.files, config=args.config, sort=args.sort))
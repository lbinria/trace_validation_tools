import ndjson
import argparse
from itertools import groupby

def read_trace(filename):
    with open(filename) as f:
        return ndjson.load(f)

def run(input):
    json_trace = read_trace(input)

    # Group by clock and sender
    json_trace_grouped = [(k, list(g)) for k, g in groupby(json_trace, lambda x: (x['clock'], x['sender']))]
    merged_trace = [list(g) for _, g in sorted(json_trace_grouped, key=lambda x: x[0][0])]
    # Convert into ndjson format expected by TLA+ trace specification
    converted_trace = [{event['var']: {'op' : event['op'], 'args' : event['args']} for event in mt} for mt in merged_trace]
    # Add config at first line
    json_trace_result = [{"__config": {}}] + converted_trace
    # Dump
    return ndjson.dumps(json_trace_result)

if __name__ == "__main__":
    # Read program args
    parser = argparse.ArgumentParser(description="")
    parser.add_argument('input', type=str, help="Trace file to convert.")
    args = parser.parse_args()
    # Print output
    print(run(args.input))
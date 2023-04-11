import ndjson
import argparse
from itertools import groupby, chain
from functools import reduce

def read_trace(filename):
    with open(filename) as f:
        return ndjson.load(f)

def merge_dict(d1, d2):
    return d1 | d2

def map_action(a):
    return { 'op': a['op'], 'path': a['path'], 'args': a['args'] }

def run(input):
    json_trace = read_trace(input)

    # Group by clock and sender
    json_trace_grouped = [(k, list(g)) for k, g in groupby(json_trace, lambda x: (x['clock'], x['sender']))]
    merged_trace = [list(g) for _, g in sorted(json_trace_grouped, key=lambda x: x[0][0])]
    # Convert into ndjson format expected by TLA+ trace specification
#     converted_trace = [{event['var']: {'op' : event['op'], 'args' : event['args']} for event in mt} for mt in merged_trace]
#     converted_trace = [reduce(lambda acc, event: merge_dict(acc, {event['var']: {'op' : event['op'], 'args' : event['args']}}), mt, {}) for mt in merged_trace]

    converted_trace = [{var: list(chain(*[map(map_action, actions)])) for var, actions in groupby(mt, lambda x: x['var'])} for mt in merged_trace]

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
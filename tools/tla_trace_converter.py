import ndjson
import argparse
from itertools import groupby, chain
from functools import reduce

def read_trace(filename):
    with open(filename) as f:
        return ndjson.load(f)

def merge_dict(d1, d2):
    return d1 | d2

def map_actions(actions):
    return list(chain(*[map(map_action, actions)]))

def sort_and_group(l, key):
    # Group by need pre-sorted
    l.sort(key=key)
    return groupby(l, key)

def map_action(a):
#     print(f"Action {a}")
    return { 'op': a['op'], 'path': a['path'], 'args': a['args'] }

def run(input):
    json_trace = read_trace(input)

    # Group by clock and sender
    # TODO sort clock / sender
    json_trace_grouped = [(k, list(g)) for k, g in groupby(json_trace, lambda x: (x['clock'], x['sender']))]
    merged_trace = [list(g) for _, g in sorted(json_trace_grouped, key=lambda x: x[0][0])]
    # Convert into ndjson format expected by TLA+ trace specification
#     converted_trace = [{event['var']: {'op' : event['op'], 'args' : event['args']} for event in mt} for mt in merged_trace]
    converted_trace = [{var: map_actions(actions) for var, actions in sort_and_group(mt, lambda x: x['var'])} for mt in merged_trace]

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
import argparse
import trace_merger
import trace_mapper
import tla_trace_converter

# Read program args
parser = argparse.ArgumentParser(description="")
parser.add_argument('--files', type=str, nargs="+", help="Trace files to merge and convert")
parser.add_argument('--map', type=str, help="Mapping file")
parser.add_argument('--validate', type=bool, action=argparse.BooleanOptionalAction, help="Should validate input and output ?")
parser.add_argument('--verbose', type=bool, action=argparse.BooleanOptionalAction, help="Print step ?")
args = parser.parse_args()

if args.verbose:
    print("# Merge events.")

merged_events = trace_merger.run(args.files)

with open("trace-merged.ndjson", "w") as f:
    f.write(merged_events)

if args.verbose:
    print("# Map events.")

mapped_events = trace_mapper.run("trace-merged.ndjson", args.map, args.validate)

with open("trace-mapped.ndjson", "w") as f:
    f.write(mapped_events)

if args.verbose:
    print("# Convert to TLA+ format (ndjson).")

converted_events = tla_trace_converter.run("trace-mapped.ndjson")

with open("trace-tla.ndjson", "w") as f:
    f.write(converted_events)
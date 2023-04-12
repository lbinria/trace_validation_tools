# About this

This project contains some tools to make trace validation (especially with the model checker TLA+).

It can be split into two parts:

 - An instrumentation, that aims to trace some events about some state changes of program variables. (instrumentation directory).
 - Some python scripts, that aims to work on trace files: merging, conversions or re-structuration. (tools directory).

# Prerequisite

- Java >= 17.0.6
- Apache maven >= 3.6.3
- Python >= 3.9.12

# Install tools


Just run the following command: `sh build_tools.sh` at the root directory. It will build tools python package and install it locally.

# Use python trace validations tools package

```python
from trace_validation_tools import *
```

# Trace validation tools modules 

 - trace_merger: merge multiple trace files into a single one.
 - tla_trace_converter: convert a (ndjson) trace file into another trace file with a specific format that is readable by a TLA+ trace specification.
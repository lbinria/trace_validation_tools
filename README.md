# What is trace validation ?

If you describe a system or a program with a formal specification, particularly a TLA+ formalism, you may have to your implementation against the specification. 

To do that, you can use our `trace_validation_tools`. First, these
tools provide primitives allowing one to log some selected events and
variables during the execution.  Then, thanks to a trace validation
specification, that is a refinement of the formal specification of the
system, one can check if an execution of the system matches with a
possible behavior w.r.t. the specification.

# How to perform a trace validation ?

- Writing the trace specification on top of your own spec (see part [useful tools](#useful-tools) and [templates](#templates) below)
- Use the primitives provided by the library to log events and variable changes in your system
- Check the trace(s) produced by the system against the specification:
    * compile and execute the implementation (containing the tracing primitives)
    * merge the produced trace files (see [useful tools](#useful-tools) and [scripts/trace_merger.py](scripts/trace_merger.py))
    * execute TLC on the trace specification and the generated trace files (see  [useful tools](#useful-tools) and [scripts/tla_trace_validation.py](scripts/trace_merger.py))

# About this repository

This repository contains:

- An instrumentation library which can be used to trace events and state changes of a
- Scripts to manipulate and check trace files
- Templates that aim to simplify the writing of trace specifications

# Prerequisite

- Java >= 17
- Apache maven >= 3.6.3
- Python >= 3.9.12
- TLA+ >= 1.8.0 (The Clarke release)

### Install TLA+ tools

Create a directory and add the latest versions `tla2tools.jar` and
`CommunityModules-deps.jar`. You do this either by installing the TLA+ toolbox
from https://github.com/tlaplus/tlaplus/releases/tag/v1.8.0, section
"Assets", or just the download the jars from
https://github.com/tlaplus/tlaplus/releases/tag/v1.8.0
and https://github.com/tlaplus/CommunityModules/releases.

The `tla_dir` variable in the script `tla_trace_validation.py` (see
[useful tools](#useful-tools)) should be set to the value of your toolbox path.

# Install trace validation instrumentation

There are two ways to use the instrumentation library in your
projects. Either get the source, compile, package and install it, or
get the package directly from the github maven registry.

## 1. Install package from sources

 - Clone the repository: `git clone https://github.com/lbinria/trace_validation_tools.git`
 - Package and install `mvn install`

## 2. Install package from github maven registry

Add the file [scripts/settings.xml](scripts/settings.xml) to your
`.m2` directory.

# How to use it 

Independently of the install method, in the `pom.xml` file of the
system you should trace add the following dependecy:

```xml 
<dependencies>
    <dependency>
        <groupId>org.lbee</groupId>
        <artifactId>instrumentation</artifactId>
        <version>1.1</version>
    </dependency>
</dependencies>
```

# Useful tools

Trace instrumentation tools gives some useful scripts and templates that you can copy and use in your projects. Scripts allow you to merge traces issued from different processes, run a trace validation on a trace file, etc. Templates are generic tla files that you need to fill to create a new trace specifications more easily.

## Scripts

You can find scripts in the `scripts` directory.
You can copy these scripts into your project to execute a trace validation pipeline for example (see https://github.com/lbinria/TicTac). 

### `trace_merger.py` 

This script enable to merge multiple trace files into a single one.

#### Use 

Give a list of trace files to merge.

`python trace_merger.py trace_1.ndjson trace_2.ndjson [...] --config system.ndjson.config  --sort True`

### `tla_trace_validation.py`

This script enable to execute TLC on a given trace specification.

#### Use 

`python tla_trace_validation.py myTraceSpec.tla trace.ndjson`

## Templates

You can find templates in the `templates` directory. You can copy these templates and adapt them accordingly to your base specification (see example: https://github.com/lbinria/TicTac).

# Tutorial

You can find a tutorial at https://github.com/lbinria/TicTac

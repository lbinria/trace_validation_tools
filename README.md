# What is trace validation ?

If you describe a system or a program with a formal specification, particularly a TLA+ formalism, you may have to check your implementation against the specification. 

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
    * execute TLC on the trace specification and the generated trace files (see [useful tools](#useful-tools) and [scripts/tla_trace_validation.py](scripts/tla_trace_validation.py))

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

Independently of the installation method, in the `pom.xml` file of the
system you should trace add the following dependency:

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

## Scripts

The Python script [scripts/trace_merger.py](scripts/trace_merger.py))
can be used to merge multiple trace files (from several concurrent
processes) passed as parameter into a single one. For example, two
trace files can be merged with

`python trace_merger.py trace_1.ndjson trace_2.ndjson --config system.ndjson.config --sort True`

The Python script
[scripts/tla_trace_validation.py](scripts/tla_trace_validation.py))
can be used to check using TLC a given trace w.r.t. a trace
specification. For example,

`python tla_trace_validation.py myTraceSpec.tla --trace trace.ndjson`

## Templates

[Templates](templates) in are generic `tla` files that can be adapted according to a
base specification. 

# Tutorial

You can find a simple example of using the library, the scripts and the
templates at https://github.com/lbinria/TicTac.

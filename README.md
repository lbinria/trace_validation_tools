# What is trace validation ?

If you describe a system or a program with a formal specification, particularly a TLA+ formalism, you may have to check your implementation against the specification. 

To do that for Java programs, you can use our `trace_validation_tools`. First, these
tools provide primitives allowing one to log some selected events and
variables during the execution.  Then, thanks to a trace validation
specification, that is a refinement of the formal specification of the
system, one can check if an execution of the system matches with a
possible behavior w.r.t. the specification.

# How to perform a trace validation ?

- Writing the trace specification on top of your own specification (see Section [templates](#templates))
- Use the primitives provided by the library to log events and variable changes in your system
- Check the trace(s) produced by the system against the specification:
    * compile and execute the implementation (containing the tracing primitives)
    * merge the produced trace files (see [scripts/trace_merger.py](scripts/trace_merger.py))
    * execute TLC on the trace specification and the generated trace files ([scripts/tla_trace_validation.py](scripts/tla_trace_validation.py))

# About this repository

This repository contains:

- A Java instrumentation library which can be used to trace events and state changes of a Java implementation
- Scripts to manipulate and check trace files
- Templates that aim to simplify the writing of trace specifications

# Prerequisite

- Java >= 17
- Apache maven >= 3.6.3
- Python >= 3.9.12
- TLA+ >= 1.8.0 (The Clarke release)

### Install TLA+ tools

Create a directory and add the latest versions of `tla2tools.jar` and
`CommunityModules-deps.jar`. You do this either by installing the TLA+ toolbox
from https://github.com/tlaplus/tlaplus/releases/tag/v1.8.0, section
"Assets", or just download the jars from
https://github.com/tlaplus/tlaplus/releases/tag/v1.8.0
and https://github.com/tlaplus/CommunityModules/releases.

The `tla_dir` variable in the script `tla_trace_validation.py` (see
[scripts](#scripts)) should be set to the value of your toolbox path.

# Install trace validation instrumentation

There are two ways to use the instrumentation library in your
projects:
1. Get the source, compile, package and install it:
 - Clone the repository: `git clone https://github.com/lbinria/trace_validation_tools.git`
 - Package and install: `mvn install`
2. Get the jar directly from the assets in the [Releases](releases) section.

# Use the library 

If you use `maven` to build your project then, depending on the installation method, in the `pom.xml` file of the system you should add the corrsesponding dependencies.

1. If the library was built and installed locally:
```xml 
<dependencies>
    <dependency>
        <groupId>org.lbee</groupId>
        <artifactId>instrumentation</artifactId>
        <version>1.3</version>
    </dependency>
</dependencies>
```
The version number depends on the version you intend to use. If installed from sources, the version is specified in the [pom.xml](pom.xml) file and a corresponding directory `org/lbee/instrumentation` is installed in the `.m2` `repository`.

2. If the jar is coppied locally to location `${jar.repository}`:
```xml 
<dependencies>
        <dependency>
            <groupId>org.lbee</groupId>
            <artifactId>instrumentation</artifactId>
            <version>1.3</version>
            <scope>system</scope>
            <systemPath>${jar.repository}/instrumentation-1.3.jar</systemPath>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.10.1</version>
        </dependency>
</dependencies>
```
The jar should be also specified in the classpath when executing the program.

# Scripts

The Python script [scripts/trace_merger.py](scripts/trace_merger.py)
can be used to merge multiple trace files (from several concurrent
processes) passed as parameter into a single one. 

Arguments:
- `files`: Trace files to merge (or directories containg `ndjson` files to be merged)
- `--config`: Config file (default=`conf.ndjson`)
- `--sort`: Sort by clock (default=`True`)
- `--remove_meta`: Remove clock and sender data (default=`True`)
- `--out`: Output file (default=`trace.ndjson`)

For example, two trace files can be merged with

`python trace_merger.py trace_1.ndjson trace_2.ndjson --config system.ndjson.config --sort True`

One can simply use

`python trace_merger.py`

to merge all `ndjson` files in the current directory into `trace.ndjson`.

The Python script
[scripts/tla_trace_validation.py](scripts/tla_trace_validation.py)
can be used to check using TLC a given trace w.r.t. a trace
specification. 

Arguments:
- `spec`: Specification file
- `--config`: Config file (default=`conf.ndjson`)
- `--trace`: Trace file (default=`trace.ndjson`)
- `--dfs`: use depth-first search (if not specified breadth-first search is used)

For example,

`python tla_trace_validation.py myTraceSpec.tla --trace trace.ndjson`

# Templates

In [Templates](templates) you can find generic `tla` files that can be adapted according to a base specification. 

# Examples

[TicTac](https://github.com/lbinria/TicTac) proposes several very simple examples of using the library, the scripts and the templates.

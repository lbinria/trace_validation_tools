# What is trace validation ?

If you describe a system or a program with a formal specification, particularly a TLA+ formalism, you may have to your implementation against the specification. 

To do that, you can use our `trace_validation_tools`. First, these
tools provide primitives allowing one to log some selected events and
variables during the execution.  Then, thanks to a trace validation
specification, that is a refinement of the formal specification of the
system, one can check if an execution of the system matches with a
possible behavior w.r.t. the specification.

# How to perform a trace validation ?

To make trace validation works, the only things to do are:

 - Writing the trace specification on top of your own spec (see part [useful tools](#useful-tools) and "templates" below)
 - Use this library in your system / program to log events and variable changes

# How to execute a trace validation ?

Once previous steps are made, you can execute a trace validation by following the next pipeline: 

 - execute your implementation
 - merge the produced trace files (see "useful tools" and "scripts/trace_merger.py" below)
 - execute TLC onto the trace specification and the generated trace files (see "usefuls tools" and "scripts/tla_trace_validation.py")

# About this repository

This repository contains tools to make and execute a trace validation with the model checker TLC (TLA+).

This repository contains:

- An instrumentation, that aims to trace some events about some state changes of system / program variables
- Some tools
   - scripts, that aims to work on trace files
   - templates, that aims to simplify the writing of trace specification

# Prerequisite

- Java >= 17
- Apache maven >= 3.6.3
- Python >= 3.9.12
- TLA+ >= 1.8.0 (The Clarke release)

## Install TLA+

 - Go to https://github.com/tlaplus/tlaplus/releases/tag/v1.8.0
 - Go to "Assets"
 - Download the toolbox (accordingly to your OS)
 - Unzip the toolbox somewhere
 - If you want to use the scripts (see "useful tools" part below), you need to configure the `tla_dir` variable in the script `tla_trace_validation.py` with value of your toolbox path 

# Install trace validation instrumentation

There are two ways to install instrumentation in your projects. Either get the source, compile, package and install or get the package directly from the github maven registry. 

# 1. Install package from sources

 - Clone the repository: `git clone https://github.com/lbinria/trace_validation_tools.git`
 - Package and install `mvn package`, `mvn install`


# 2. Install package from github maven registry

You should add github maven registry repository in maven settings:

In `~/.m2`, create a file named `settings.xml`. Put the following in this file: 

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <activeProfiles>
    <activeProfile>github</activeProfile>
  </activeProfiles>

  <profiles>
    <profile>
      <id>github</id>
      <repositories>
        <repository>
            <id>github</id>
            <name>GitHub lbinria Apache Maven Packages</name>
            <url>https://maven.pkg.github.com/lbinria/trace_validation_tools</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
        <repository>
          <id>central</id>
          <url>https://repo1.maven.org/maven2</url>
        </repository>
      </repositories>
    </profile>
  </profiles>

  <servers>
    <server>
      <id>github</id>
      <username>YOUR_USERNAME</username>
      <password>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</password>
    </server>
  </servers>

</settings>
```
# How to use it 

After installing, in your project, add the following to `pom.xml` file:

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

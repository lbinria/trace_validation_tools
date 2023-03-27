import os
import time
import signal
from subprocess import Popen
import sys

assert len(sys.argv) > 1, "Trace spec path was expected as argument."

trace_spec_path = sys.argv[1]

if len(sys.argv) > 2:
    trace_path = sys.argv[2]
    os.environ["TRACE_PATH"] = trace_path

tla_trace_validation_process = Popen([
    "/usr/lib/jvm/jdk-19/bin/java",
    "-XX:+UseParallelGC",
    "-cp",
    "/opt/TLAToolbox-1.8.0-nightly/toolbox/tla2tools.jar:/opt/TLAToolbox-1.8.0-nightly/toolbox/CommunityModules-deps.jar",
    "tlc2.TLC",
    trace_spec_path])

tla_trace_validation_process.wait()
tla_trace_validation_process.terminate()
import os
import time
import signal
from subprocess import Popen
import sys

# Get paths
tla_dir = "/opt/TLAToolbox-1.8.0-nightly/toolbox"

tla_jar = os.path.join(tla_dir, "tla2tools.jar")
community_modules_jar = os.path.join(tla_dir, "CommunityModules-deps.jar")
tla_cp = f"{tla_jar}:{community_modules_jar}"

assert len(sys.argv) > 1, "Trace spec path was expected as argument."
trace_spec_path = sys.argv[1]

if len(sys.argv) > 2:
    trace_path = sys.argv[2]
    os.environ["TRACE_PATH"] = trace_path

# Run TLA
tla_trace_validation_process = Popen([
    "java",
    "-XX:+UseParallelGC",
    "-cp",
    tla_cp,
    "tlc2.TLC",
    trace_spec_path])

tla_trace_validation_process.wait()
tla_trace_validation_process.terminate()
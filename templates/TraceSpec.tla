(* Generic module that aims to handle trace specifications *)
(* Just need to import it, and override some operators in order to have a trace spec that works *)
---- MODULE TraceSpec ----
EXTENDS TLC, Sequences, SequencesExt, Naturals, FiniteSets, Bags, Json, IOUtils, TVOperators

ASSUME TLCGet("config").mode = "bfs"

VARIABLES l


(* Operators to override *)
Vars == Print(<<"Trace spec isn't valid, you should override 'Vars'.">>, <<>>)
BaseInit == Print(<<"Trace spec isn't valid, you should override 'BaseInit'.">>, Nil)
TraceNext == Print(<<"Trace spec isn't valid, you should override 'TraceNext'.">>, Nil)
MapVariables(logline) == Print(<<"Trace spec isn't valid, you should override 'MapVariables'.">>, Nil)
\*ASSUME Vars /= <<>>
\*ASSUME TraceNext # Nil

(* Read trace *)
JsonTrace ==
    IF "TRACE_PATH" \in DOMAIN IOEnv THEN
        ndJsonDeserialize(IOEnv.TRACE_PATH)
    ELSE
        Print(<<"Failed to validate the trace. TRACE_PATH environnement variable was expected.">>, "")

(* Manage exceptions: assume that trace is free of any exception *)
ASSUME \A t \in ToSet(JsonTrace) : "event" \notin DOMAIN t \/ ("event" \in DOMAIN t /\ t.event /= "__exception")

(* Get trace skipping config line *)
Trace ==
    SubSeq(JsonTrace, 2, Len(JsonTrace))

logline ==
    Trace[l]



IsEvent(e) ==
    \* Equals FALSE if we get past the end of the log, causing model checking to stop.
    /\ l \in 1..Len(Trace)
    /\ IF "event" \in DOMAIN logline THEN logline.event = e ELSE TRUE
    /\ l' = l + 1
    /\ MapVariables(logline)

TraceInit ==
    /\ l = 1
    /\ BaseInit

TraceSpec ==
    \* Because of  [A]_v <=> A \/ v=v'  , the following formula is logically
     \* equivalent to the (canonical) Spec formual  Init /\ [][Next]_vars  .
     \* However, TLC's breadth-first algorithm does not explore successor
     \* states of a *seen* state.  Since one or more states may appear one or
     \* more times in the the trace, the  UNCHANGED vars  combined with the
     \*  TraceView  that includes  TLCGet("level")  is our workaround.
    TraceInit /\ [][TraceNext]_<<l, Vars>>

TraceAccepted ==
    LET d == TLCGet("stats").diameter IN
    IF d - 1 = Len(Trace) THEN TRUE
    ELSE Print(<<"Failed matching the trace to (a prefix of) a behavior:", Trace[d],
                    "TLA+ debugger breakpoint hit count " \o ToString(d+1)>>, FALSE)

TraceView ==
    \* A high-level state  s  can appear multiple times in a system trace.  Including the
     \* current level in TLC's view ensures that TLC will not stop model checking when  s
     \* appears the second time in the trace.  Put differently,  TraceView  causes TLC to
     \* consider  s_i  and s_j  , where  i  and  j  are the positions of  s  in the trace,
     \* to be different states.
    <<Vars, TLCGet("level")>>
====

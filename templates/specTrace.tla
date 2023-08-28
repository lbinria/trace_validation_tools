--------------------------- MODULE specTrace ---------------------------

EXTENDS TLC, Sequences, SequencesExt, Naturals, FiniteSets, Bags, Json, IOUtils, baseSpec, TVOperators, TraceSpec

(* Override CONSTANTS *)

(* Replace Nil constant *)
TraceNil == "null"

(* Get constant values from the first line of the trace *)
(* First line of the trace should contains the system's configuration *)

(* REPLACE / MODIFY COMMENT BELOW - with your own configuration parameters *)
(*
TraceC1 ==
    ToSet(JsonTrace[1].C1)

TraceC2 ==
    JsonTrace[1].C2

...
*)

(* REPLACE / MODIFY COMMENT BELOW *)
(* - replace / add one CASE by variable in your spec *)
(* - value should match with the init value of the variable in your spec *)
(*
(* Can be extracted from init *)
DefaultImpl(varName) ==
    CASE varName = "var1" -> 0..10
    []  varName = "var2" -> [a |-> 0, b |-> 0..10]
    ...
*)

(* REPLACE / MODIFY COMMENT BELOW *)
(*
MapVariablesImpl(t) ==
    /\
        IF "var1" \in DOMAIN t
        THEN var1' = MapVariable(var1, "var1", t)
        ELSE TRUE
    /\
        IF "var2" \in DOMAIN t
        THEN var2' = MapVariable(var2, "var2", t)
        ELSE TRUE
    ...
*)

(* REPLACE / MODIFY COMMENT BELOW *)
(* For each action of your spec, add the corresponding predicate *)
(*
IsAction1 ==
    /\ IsEvent("Action1")
    /\ Action1

IsAction2 ==
    /\ IsEvent("Action2")
    /\ Action2

...
*)

(* REPLACE / MODIFY COMMENT BELOW *)
(* Below make disjunction of all your predicates *)
(*
TraceNextImpl ==
    \/ Action1
    \/ Action2
    \/ ...
*)

(* REPLACE / MODIFY COMMENT BELOW ONLY IF YOU WANT TO MAKE ACTION COMPOSITION *)
ComposedNext == FALSE

(* NOTHING TO CHANGE BELOW *)
BaseSpec == Init /\ [][Next \/ ComposedNext]_vars
-----------------------------------------------------------------------------
=============================================================================
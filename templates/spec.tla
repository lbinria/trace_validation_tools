(* A spec example that do nothing *)
(* /!\ This example just serve to understand how adapt specTrace.tla to match with your own spec ! *)
--------------------------------- MODULE spec ---------------------------------
EXTENDS Naturals, FiniteSets, Sequences, TLC

\* A reserved value.
CONSTANTS Nil
\* Some constants
CONSTANTS C1, C2

----

\* Some variables
VARIABLE var1, var2

vars == <<var1, var2>>

Init ==
    /\ var1 \in 0..10
    /\ var2 = [a |-> 0, b |-> 0..10]

ActionA ==
    UNCHANGED vars

ActionB ==
    UNCHANGED vars

Next ==
    \/ ActionA
    \/ ActionB


Spec == Init /\ [][Next]_vars

===============================================================================
(* Generic module that aims to handle operators and applications *)
(* Just need to import it, and override some operators in order to have a trace spec that works *)
---- MODULE TVOperators ----
EXTENDS TLC, Sequences, SequencesExt, Naturals, FiniteSets, Bags, Json, IOUtils

(* Constant to override *)
CONSTANT Nil
(* Operators to override *)
Default(varName) ==  Print(<<"Trace spec isn't valid, you should override 'Default' operator.">>, Nil)
MapArgs(mapFunction, cur, default, op, args, eventName) == Print(<<"Trace spec isn't valid, you should override 'MapArgs' operator.">>, Nil)
MapArgsBase(mapFunction, cur, default, op, args, eventName) == args

(* Generic operators *)
Replace(cur, val) == val
AddElement(cur, val) == cur \cup {val}
AddElements(cur, vals) == cur \cup ToSet(vals)
RemoveElement(cur, val) == cur \ {val}
Clear(cur, val) == {}
AppendElement(cur, val) == Append(cur, val)
RemoveKey(cur, val) == [k \in DOMAIN cur |-> IF k = val THEN Nil ELSE cur[k]]
UpdateRec(cur, val) == [k \in DOMAIN cur |-> IF k \in DOMAIN val THEN val[k] ELSE cur[k]]
AddToBag(cur, val) ==
    IF val \in DOMAIN cur THEN
        [cur EXCEPT ![val] = cur[val] + 1]
    ELSE
        cur @@ (val :> 1)

RemoveFromBag(cur, val) ==
    IF val \in DOMAIN cur THEN
        [cur EXCEPT ![val] = cur[val] - 1]
    ELSE
        cur

Add(cur, val) == cur + val
Sub(cur, val) == cur - val

Unchanged(cur, val) == cur


Apply(var, default, op, args) ==
    CASE op = "Replace" -> Replace(var, args[1])
    []   op = "AddElement" -> AddElement(var, args[1])
    []   op = "AddElements" -> AddElements(var, args[1])
    []   op = "RemoveElement" -> RemoveElement(var, args[1])
    []   op = "AddToBag" -> AddToBag(var, args[1])
    []   op = "RemoveFromBag" -> RemoveFromBag(var, args[1])
    []   op = "Add" -> Add(var, args[1])
    []   op = "Clear" -> Clear(var, <<>>)
    []   op = "AppendElement" -> AppendElement(var, args[1])
    []   op = "RemoveKey" -> RemoveKey(var, args[1])
    []   op = "UpdateRec" -> UpdateRec(var, args[1])
    []   op = "Init" -> Replace(var, default)
    []   op = "InitWithValue" -> UpdateRec(default, args[1])
    []   op = "Unchanged" -> Unchanged(var, args[1])

RECURSIVE ExceptAtPath(_,_,_,_,_)
LOCAL ExceptAtPath(var, default, path, op, args) ==
    LET h == Head(path) IN
    IF Len(path) > 1 THEN
        [var EXCEPT ![h] = ExceptAtPath(var[h], default[h], Tail(path), op, args)]
    ELSE
        [var EXCEPT ![h] = Apply(@, default[h], op, args)]

RECURSIVE ApplyUpdates(_,_,_,_)
LOCAL ApplyUpdates(var, varName, updates, event) ==
    LET update == Head(updates) IN

    LET applied ==
        IF Len(update.path) > 0 THEN
            ExceptAtPath(var, Default(varName), update.path, update.op, update.args)
        ELSE
            LET mapArgs ==
                IF "map" \in DOMAIN update THEN
                    MapArgs(update.map, var, Default(varName), update.op, update.args, event)
                ELSE
                    update.args
            IN
            Apply(var, Default(varName), update.op, mapArgs)
    IN
    IF Len(updates) > 1 THEN
        ApplyUpdates(applied, varName, Tail(updates), event)
    ELSE
        applied

MapVariable(var, varName, logline) ==
    LET event == IF "desc" \in DOMAIN logline THEN logline.desc ELSE "" IN
    ApplyUpdates(var, varName, logline[varName], event)
====

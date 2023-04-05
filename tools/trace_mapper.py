import json 
import ndjson
from jsonschema import validate, RefResolver
from jsonschema.validators import Draft202012Validator
from jsonpath import JSONPath
import argparse
import re
import os

dir_path = os.path.dirname(os.path.realpath(__file__))

with open(f"{dir_path}/tla-definitions.schema.json") as f:
    tla_definitions = json.load(f)

schema_store = {
    tla_definitions.get('$id','tla-definitions.schema.json') : tla_definitions
}

resolver = RefResolver.from_schema(Draft202012Validator.META_SCHEMA, store=schema_store)


# Compile regex (used to find jpath expressions)
jpath_pattern = re.compile(r'\{\{(.*?)\}\}')

# Global variables
__current = None
__currentIdx = None
__src = None

# Some useful functions (can be extended by user)
def to_upper(str):
    return str.upper()

def to_lower(str):
    return str.lower()

def inc(n):
    return n+1 

def switch(val):
    def switch(template, source):
        match_case = next((c for c in template if c['case'] == val), None)
        # Assert default case is defined (can be checked by json schema too)
        assert match_case, f"missing default case on switch expression {template} on {source}."
        return map_value(source, match_case['value'])
        # return match_case['value']
    return switch

def foreach(val):
    def foreach(template, source):
        result = []
        
        for i, x in enumerate(val):
            global __current
            global __currentIdx
            __current = x 
            __currentIdx = i 
            result.append(map_element(source, template))
        return result

    return foreach

def _if(val):
    assert type(val) == bool, "If condition must be a bool."
    def _if(template, source):
        
        if (val):
            return map_element(source, template[0])
        else:
            return map_element(source, template[1])

    return _if 

def eval_expr(source, raw_expr):
    # print(source)
    expr = raw_expr
    values = []
    global __src
    __src = source

    # Search all jpath expressions
    for i, match in reversed(list(enumerate(jpath_pattern.finditer(raw_expr)))):
        # Get jpath expression
        jpath = match.group(0)
        jpath = jpath[2:len(jpath) - 2]
        # Find value in source
        res = JSONPath(jpath).parse(source)
        # 
        res = res[0] if len(res) == 1 else res
        # Append value to a variable (in order to eval later)
        values.append(res)
        # Replace jpath expression by the variable to eval later
        expr = expr[:match.start()] + f"values[{i}]" + expr[match.end():len(expr)]

    result = eval(expr, None, {'values': values, '__current': __current})
    # print(f"Eval {expr} = {result} with {values}")

    # Eval
    return result


def map_value(source, template):
    # Exec expression if needed
    if type(template) is str and template[0] == '@':
        return eval_expr(source, template[1:])
    else:
        return template

def map_element(source, template):
    # Map dictionary key / values
    if type(template) is dict:
        # t = [(map_value(source, k), map_element(source, v)) for k, v in template.items()]
        t = [(map_value(source, k), v) for k, v in template.items()]
        reduced_content = [k(v, source) for k, v in t if callable(k)]
        
        if reduced_content:
            if len(reduced_content) == 1:
                return reduced_content[0]
            else: 
                return reduced_content
        else:
            return {k:map_element(source, v) for k, v in t}
        
    # Map element of a list
    elif type(template) is list:
        return [map_element(source, v) for v in template]
    # Map primitive element
    else:
        return map_value(source, template)


def map_event(event):
    # Map var
    json_var_map = json_map[event['var']]
    target_var_name = json_var_map['name']
    
    # Map function
    json_op_map = json_var_map['functions'][event['op']]
    target_op_name = json_op_map['name']

    # If input schema is given validate args
    if shouldValidate and 'input_schema' in json_op_map:
        # print("Validate input")
        input_schema = json_op_map['input_schema']
        validate(instance=event['args'], schema=input_schema)

    # Map arguments
    target_args = map_element(event, json_op_map['map_args'])

    # If output schema is given validate target_args
    if shouldValidate and 'output_schema' in json_op_map:
        # print("Validate output")
        output_schema = json_op_map['output_schema']
        # validator = Draft202012Validator(output_schema, resolver=resolver)
        # validator.validate(target_args)
        validate(instance=target_args, schema=output_schema)

    return {'clock': event['clock'], 'sender': event['sender'], 'var': target_var_name, 'op': target_op_name, 'args': target_args}


def run(input, map, validate):

    global shouldValidate
    shouldValidate = validate
    
    # Open input / map files
    with open(input) as f:
        json_trace = ndjson.load(f)

    with open(map) as f:
        global json_map
        json_map = json.load(f)

    # Map each trace event
    json_mapped = [map_event(event) for event in json_trace]
    # Dump
    return ndjson.dumps(json_mapped)

if __name__ == "__main__":
    # Read program args
    parser = argparse.ArgumentParser(description="")
    parser.add_argument('input', type=str, help="Input (nd)json trace file")
    parser.add_argument('map', type=str, help="Mapping file")
    parser.add_argument('--validate', type=bool, action=argparse.BooleanOptionalAction, help="Should validate input and output ?")
    args = parser.parse_args()
    # Print output
    print(run(args.input, args.map, args.validate))
grammar ElementMatcher;

WS : [ \t\r\n]+ -> skip;
REGEX: '/' ~('/')+ '/';
STRING: '\'' ~('\'')* '\'';
NUMBER: ('0'..'9')+;
LOGICAL_OPERATOR: 'and' | 'or';

topExpression
    : parenthesizedExpression
    | expression LOGICAL_OPERATOR expression
    | hasExpression
    | isExpression
    | throwsExpression
    | subTypeExpression
    | overridesExpression
    | returnsExpression
    ;

expression
    : parenthesizedExpression
    | expression LOGICAL_OPERATOR expression
    | hasExpression
    | isExpression
    | throwsExpression
    | subTypeExpression
    | overridesExpression
    | returnsExpression
    | stringExpression
    | regexExpression
    ;

subExpression
    : stringExpression
    | regexExpression
    | parenthesizedExpression
    ;

parenthesizedExpression
    : '(' expression ')'
    ;

stringExpression
    : STRING
    ;

regexExpression
    : REGEX
    ;

throwsExpression
    : 'throws' subExpression
    | 'doesn\'t' 'throw' subExpression?
    ;

subTypeExpression
    : 'directly'? ('extends' | 'implements') subExpression
    | 'doesn\'t' 'directly'? ('extend' | 'implement') subExpression
    ;

overridesExpression
    : 'overrides' subExpression
    | 'doesn\'t' 'override' subExpression?
    ;

returnsExpression
    : 'returns' 'precisely'? subExpression
    | 'doesn\'t' 'return' 'precisely'? subExpression
    ;

hasExpression
    : hasExpression_basic
    | hasExpression_arguments
    | hasExpression_index
    ;

hasExpression_basic
    : 'has' (hasExpression_subExpr | hasExpression_match | hasExpression_attribute)
    | 'doesn\'t' 'have' (hasExpression_subExpr | hasExpression_match | hasExpression_attribute)
    ;

hasExpression_arguments
    : 'has' (('more' | 'less') 'than')? NUMBER 'arguments'
    | 'doesn\'t' 'have' (('more' | 'less') 'than')? NUMBER 'arguments'
    ;

hasExpression_index
    : 'has' 'index' (('larger' | 'less') 'than')? NUMBER
    | 'doesn\'t' 'have' 'index' (('larger' | 'less') 'than')? NUMBER
    ;

hasExpression_attribute
    : 'explicit'? 'attribute' (STRING | REGEX)? ('that' hasExpression_attribute_values)?
    ;

hasExpression_attribute_values
    : 'is' 'not'? 'equal' 'to' hasExpression_attribute_values_subExpr
    | 'is' ('greater' | 'less') 'than' NUMBER
    | 'has' (('more' | 'less') 'than')? NUMBER 'elements'
    | 'doesn\'t' 'have' (('more' | 'less') 'than')? NUMBER 'elements'
    | 'has' 'element' NUMBER?
    | 'has' 'element' NUMBER? 'that' hasExpression_attribute_values
    | 'doesn\'t' 'have' 'element' NUMBER?
    | 'doesn\'t' 'have' 'element' NUMBER? 'that' hasExpression_attribute_values
    | 'has' 'type' (STRING | REGEX)
    | 'doesn\'t' 'have' 'type' (STRING | REGEX)
    | 'has' hasExpression_attribute
    | 'doesn\'t' 'have' hasExpression_attribute
    | '(' hasExpression_attribute_values ')'
    | hasExpression_attribute_values LOGICAL_OPERATOR hasExpression_attribute_values
    ;

hasExpression_attribute_values_subExpr
    : STRING
    | REGEX
    | NUMBER
    ;

hasExpression_subExpr
    : ('argument' NUMBER?
        | 'typeParameter'
        | 'annotation'
        | 'declared'? 'method'
        | 'declared'? 'field'
        | 'declared'? 'innerClass'
        | 'direct'? 'outerClass'
        | 'direct'? 'superType'
        | 'type'
        )
        subExpression
    ;

hasExpression_match
    : ('name'
        | ('erased' | 'generic')? 'signature'
        | 'representation'
        | 'kind'
        | 'simpleName'
      ) (STRING | REGEX)
    ;

isExpression
    : 'is' 'not'? ( isExpression_subExpr | isExpression_kind | isExpression_package )
    | 'isn\'t' ( isExpression_subExpr | isExpression_kind | isExpression_package )
    ;

isExpression_subExpr
    : ('argument' NUMBER? 'of'
        | 'typeParameter' 'of'
        | 'annotated' 'by'
        | 'method' 'of'
        | 'field' 'of'
        | 'outerclass' 'of'
        | 'innerclass' 'of'
        | 'thrown' 'from'
        | 'direct'? 'superType' 'of'
        | 'overriden' 'by'
        | 'in' 'class'
        )
        subExpression
    ;

isExpression_kind
    : 'a' STRING | REGEX
    ;

isExpression_package
    : 'in' 'package' (STRING | REGEX)
    ;
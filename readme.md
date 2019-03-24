# expression-calculator
<p align="right">
<a href="https://docs.oracle.com/en/java/javase/12/index.html"><img src="https://img.shields.io/badge/Java_SE-12+-blue.svg" alt="Java requirement"></a>
</p>

A calculator that calculates expressions like `add(1, mult(2, 3)), or let(a, 5, add(a, a))`.

## Build
Install [Maven](https://maven.apache.org/) and execute
`mvn clean package -P default,withTests`.

## Usage
```
./exprcalculator.sh [options] "expression"
  Options:
    -v, -log
      Logging level. Possible values: OFF, ERROR, WARN, INFO, DEBUG
      Default: ERROR
    -p, -precision
      Precision: the number of digits to be used. Must not be negative. Use 0 for unlimited
      precision, but this will result in failures to calculate expressions which lead to irrational
      or repeating decimals (e.g. 'div(1, 3)')
      Default: precision=7 roundingMode=HALF_EVEN
```
One can also specify expression via the standard input stream:
```
echo "expression" | ./exprcalculator.sh
```
If you experience a slow startup (about a second) just know that this is caused by the initialization done by the logging library.
An insanity that I would have solved if it were a production-ready command-line tool.

## Expression systax
Syntax is pretty much obvious from the examples below, but some notes still might be helphul:
* An expression is either `<number>`, or `<any operator>(<operand1>, <operand2>, ...)`, or `<declared variable name>`.
* Birary operators: `add`, `sub`, `mult`, `div`:
`<operator>(<expression>, <expression>)`.
* Ternary operator `let` allows declaring variables and assign values for them:
`let(<variable name>, <variable value expression>, <expression where variable is used>)`.
Names of variables can not be the same as names of operators.

## Examples
```
./exprcalculator.sh "-3.14"
-3.14
```

The next calculation fails because `(-3.14)` is not an expression:
```
./exprcalculator.sh "(-3.14)"
Problem with ')' at index 6:
(-3.14)
      ^
```

```
./exprcalculator.sh "add(1, 2)"
3
```

Unlimited precision is specified with `-p 0`,
but `0.1` divided by `0.3` is a repeating decimal and can not be represented with unlimited precision:
```
./exprcalculator.sh -p 0 "div(0.1, 0.3)"
Problem with 'div' at index 0:
div(0.1, 0.3)
^
```

```
./exprcalculator.sh "add(1, mult(2, 3))"
7
```

```
./exprcalculator.sh "mult(add(2, 2), div(9, 3))"
12
```

```
./exprcalculator.sh "let(a, 5, add(a, a))"
10
```

```
./exprcalculator.sh "let(a, 5, let(b, mult(a, 10), add(b, a)))"
55
```

```
./exprcalculator.sh "let(a, let(b, 10, add(b, b)), let(b, 20, add(a, b)))"
40
```

```
./exprcalculator.sh "let(a, let(b, let(_var, 1, div(1, _var)), add(b, b)), let(b, 20, add(a, b)))"
22
```

```
./exprcalculator.sh "let(a, 1, add(let(b, 2, div(b, 1)), -1.0))"
1.0
```

This calculation fails because we try to define `var` twice within the same scope:
```
./exprcalculator.sh "let(var, 1, add(let(var, 2, div(var, 1)), 1))"
Problem with 'var' at index 20:
let(var, 1, add(let(var, 2, div(var, 1)), 1))
                    ^
```

All content is licensed under [![WTFPL logo](http://www.wtfpl.net/wp-content/uploads/2012/12/wtfpl-badge-2.png)](http://www.wtfpl.net/),
except where another license is explicitly specified.

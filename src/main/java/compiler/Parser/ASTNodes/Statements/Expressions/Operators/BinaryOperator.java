package compiler.Parser.ASTNodes.Statements.Expressions.Operators;

import compiler.Lexer.Symbol;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;


/**
 * >= < <= > == != + - * / % && ||
 */
public class BinaryOperator extends Operator {

    private final Symbol operator;

    public BinaryOperator(Symbol operator, int line, int column) {
        super(line, column);

        this.operator = operator;
    }


    public Symbol getSymbol() {
        return operator;
    }

    public String getOperator() {
        return operator.lexeme;
    }

    @Override
    public String toString() {
        return "BinaryOperator [operator=" + operator.lexeme + "]";
    }
    
    @Override
    public String prettyPrint(int indent) {
        return "  ".repeat(indent) + operator.lexeme;
    }


    /// Returns true if the operator can only be applied on booleans, false otherwise
    public boolean isBooleanOperator() {
        return switch (operator.lexeme) {
            case "&&", "||" -> true;
            default -> false;
        };
    }

    /// Returns true if the operator can be applied on booleans, numbers, strings, false otherwise
    public boolean isAnyTypeOperator() {
        return switch (operator.lexeme) {
            case "==", "!=" -> true;
            default -> false;
        };
    }

//    public boolean isStringOperator() {
//        return switch (operator.lexeme) {
//            case "+", "==", "!=" -> true;
//            default -> false;
//        };
//    }

    /// Returns true if the operator can only be applied to numbers, false otherwise
    public boolean isNumberOperator() {
        return switch (operator.lexeme) {
            case "<", "<=", ">", ">=", "+", "-", "/", "*", "%"-> true;
            default -> false;
        };
    }

    public boolean numberOperatorReturnsBoolean() {
        return switch (operator.lexeme) {
            case "<", "<=", ">", ">=" -> true;
            default -> false;
        };
    }


    @Override
    public <R, T> R accept(Visitor<R, T> v, T table) throws Exception{
        return v.visitBinaryOperator(this, table);
    }
}

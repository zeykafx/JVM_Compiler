package compiler.Parser.ASTNodes.Statements.Expressions.Expressions;

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.Types.Type;

public class ArrayExpression extends Expression {
    // ArrayExpression -> "array" "[" "intval" "]" "of" Type ";" .
    private final Expression sizeExpression;
    private final Type type;
//    private final Symbol identifier;


    public ArrayExpression(Expression sizeExpression, Type type) {
        this.sizeExpression = sizeExpression;
        this.type = type;
//        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return "ArrayExpression [sizeExpression=" + sizeExpression + ", type=" + type + "]";
    }
}

package compiler.Parser.ASTNodes.Statements.Expressions.Expressions;

import compiler.Parser.ASTNodes.Types.Type;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class ArrayExpression extends Expression {
    // a int[] = array[10] of int;

    // a[11]

    // ArrayExpression -> "array" "[" "intval" "]" "of" Type ";" .
    private final Expression sizeExpression;
    private final Type type;
//    private final Symbol identifier;


    public ArrayExpression(Expression sizeExpression, Type type) {
        this.sizeExpression = sizeExpression;
        this.type = type;
//        this.identifier = identifier;
    }


    public Expression getSizeExpression() {
        return sizeExpression;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ArrayExpression [sizeExpression=" + sizeExpression + ", type=" + type + "]";
    }
    
    @Override
    public String prettyPrint(int indent) {
        return "  ".repeat(indent) + "ArrayExpression: \n" + sizeExpression.prettyPrint(indent+1) + "\n" + "  ".repeat(indent+1) + " of " + type.prettyPrint(0);
    }

    @Override
    public SemType accept(Visitor<SemType> v, SymbolTable table) {
        return v.visitArrayExpression(this, table);
    }
}

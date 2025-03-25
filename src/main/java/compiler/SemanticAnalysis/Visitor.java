package compiler.SemanticAnalysis;

import compiler.Parser.ASTNodes.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.ArrayExpression;
import compiler.Parser.ASTNodes.Statements.Statements.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.*;
import compiler.Parser.ASTNodes.Types.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Operators.*;


public interface Visitor {

    void visitArrayAccess(ArrayAccess arrayAccess);
    void visitIdentifierAccess(IdentifierAccess identifierAccess);
    void visitRecordAccess(RecordAccess recordAccess);

    void visitAloneExpression(AloneExpression aloneExpression);
    void visitArrayExpression(ArrayExpression arrayExpression);
    void visitAssignment(Assignment assignment);
    void visitBinaryExpression(BinaryExpression binaryExpression);
    void visitIdentifier(Identifier identifier);
    void visitUnaryExpression(UnaryExpression unaryExpression);


//    void visitOperator(Operator operator);
    void visitBinaryOperator(BinaryOperator binaryOperator);
    void visitUnaryOperator(UnaryOperator unaryOperator);

//    void visitTerm(Term term);
    void visitConstValue(ConstVal constVal);
    void visitFunctionCall(FunctionCall functionCall);
    void visitNewRecord(NewRecord newRecord);
    void visitParamCall(ParamCall paramCall);
    void visitParenthesesTerm(ParenthesesTerm parenthesesTerm);

    void visitProgram(Program program);
    void visitBlock(Block block);

    void visitType(Type type);
    void visitNumType(NumType numType);

    void visitStatement(Statement statement);
    void visitForLoop(ForLoop forLoop);
    void visitFreeStatement(FreeStatement freeStatement);
    void visitFunctionDefinition(FunctionDefinition functionDefinition);
    void visitIfStatement(IfStatement  ifStatement);
    void visitParamDefinition(ParamDefinition paramDefinition);
    void visitRecordDefinition(RecordDefinition recordDefinition);
    void visitRecordFieldDefinition(RecordFieldDefinition recordFieldDefinition);
    void visitReturnStatement(ReturnStatement returnStatement);
    void visitVariableAssignment(VariableAssignment variableAssignment);
    void visitVariableDeclaration(VariableDeclaration variableDeclaration);
    void visitWhileLoop(WhileLoop whileLoop);
}

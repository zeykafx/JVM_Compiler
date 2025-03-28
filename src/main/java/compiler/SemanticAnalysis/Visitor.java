package compiler.SemanticAnalysis;

import compiler.Parser.ASTNodes.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.ArrayExpression;
import compiler.Parser.ASTNodes.Statements.Statements.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.*;
import compiler.Parser.ASTNodes.Types.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Operators.*;


public interface Visitor<T> {

    T visitArrayAccess(ArrayAccess arrayAccess);
    T visitIdentifierAccess(IdentifierAccess identifierAccess);
    T visitRecordAccess(RecordAccess recordAccess);

    T visitAloneExpression(AloneExpression aloneExpression);
    T visitArrayExpression(ArrayExpression arrayExpression);
    T visitAssignment(Assignment assignment);
    T visitBinaryExpression(BinaryExpression binaryExpression);
    T visitIdentifier(Identifier identifier);
    T visitUnaryExpression(UnaryExpression unaryExpression);


//    T visitOperator(Operator operator);
    T visitBinaryOperator(BinaryOperator binaryOperator);
    T visitUnaryOperator(UnaryOperator unaryOperator);

//    T visitTerm(Term term);
    T visitConstValue(ConstVal constVal);
    T visitFunctionCall(FunctionCall functionCall);
    T visitNewRecord(NewRecord newRecord);
    T visitParamCall(ParamCall paramCall);
    T visitParenthesesTerm(ParenthesesTerm parenthesesTerm);

    T visitProgram(Program program);
    T visitBlock(Block block);

    T visitType(Type type);
    T visitNumType(NumType numType);

    T visitStatement(Statement statement);
    T visitForLoop(ForLoop forLoop);
    T visitFreeStatement(FreeStatement freeStatement);
    T visitFunctionDefinition(FunctionDefinition functionDefinition);
    T visitIfStatement(IfStatement  ifStatement);
    T visitParamDefinition(ParamDefinition paramDefinition);
    T visitRecordDefinition(RecordDefinition recordDefinition);
    T visitRecordFieldDefinition(RecordFieldDefinition recordFieldDefinition);
    T visitReturnStatement(ReturnStatement returnStatement);
    T visitVariableAssignment(VariableAssignment variableAssignment);
    T visitVariableDeclaration(VariableDeclaration variableDeclaration);
    T visitWhileLoop(WhileLoop whileLoop);
}

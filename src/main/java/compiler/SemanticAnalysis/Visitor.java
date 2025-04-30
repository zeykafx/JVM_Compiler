package compiler.SemanticAnalysis;

import compiler.Parser.ASTNodes.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.ArrayExpression;
import compiler.Parser.ASTNodes.Statements.Statements.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.*;
import compiler.Parser.ASTNodes.Types.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Operators.*;


public interface Visitor<R, T> {

    R visitArrayAccess(ArrayAccess arrayAccess,T localTable) throws Exception;
    R visitIdentifierAccess(IdentifierAccess identifierAccess, T localTable) throws Exception;
    R visitRecordAccess(RecordAccess recordAccess, T localTable) throws Exception;

    R visitArrayExpression(ArrayExpression arrayExpression, T localTable) throws Exception;
    R visitBinaryExpression(BinaryExpression binaryExpression, T localTable) throws Exception;
    R visitUnaryExpression(UnaryExpression unaryExpression, T localTable) throws Exception;


//    R visitOperator(Operator operator, T localTable) throws Exception;
    R visitBinaryOperator(BinaryOperator binaryOperator, T localTable) throws Exception;
    R visitUnaryOperator(UnaryOperator unaryOperator, T localTable) throws Exception;

//    R visitTerm(Term term, T localTable) throws Exception;
    R visitConstValue(ConstVal constVal, T localTable) throws Exception;
    R visitFunctionCall(FunctionCall functionCall, T localTable) throws Exception;
    R visitRecordInstantiation(NewRecord newRecord, T localTable) throws Exception;
    R visitParamCall(ParamCall paramCall, T localTable) throws Exception;
    R visitParenthesesTerm(ParenthesesTerm parenthesesTerm, T localTable) throws Exception;

    R visitProgram(Program program, T localTable) throws Exception;
    R visitBlock(Block block, T localTable) throws Exception;

    R visitType(Type type, T localTable) throws Exception;
    R visitNumType(NumType numType, T localTable) throws Exception;

    R visitStatement(Statement statement, T localTable) throws Exception;
    R visitForLoop(ForLoop forLoop, T localTable) throws Exception;
    R visitFreeStatement(FreeStatement freeStatement, T localTable) throws Exception;
    R visitFunctionDefinition(FunctionDefinition functionDefinition, T localTable) throws Exception;
    R visitIfStatement(IfStatement  ifStatement, T localTable) throws Exception;
    R visitParamDefinition(ParamDefinition paramDefinition, T localTable) throws Exception;
    R visitRecordDefinition(RecordDefinition recordDefinition, T localTable) throws Exception;
    R visitRecordFieldDefinition(RecordFieldDefinition recordFieldDefinition, T localTable) throws Exception;
    R visitReturnStatement(ReturnStatement returnStatement, T localTable) throws Exception;
    R visitVariableAssignment(VariableAssignment variableAssignment, T localTable) throws Exception;
    R visitVariableDeclaration(VariableDeclaration variableDeclaration, T localTable) throws Exception;

    R visitWhileLoop(WhileLoop whileLoop, T localTable) throws Exception;
}

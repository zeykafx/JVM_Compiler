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

    T visitArrayAccess(ArrayAccess arrayAccess, SymbolTable localTable);
    T visitIdentifierAccess(IdentifierAccess identifierAccess, SymbolTable localTable);
    T visitRecordAccess(RecordAccess recordAccess, SymbolTable localTable);

    T visitAloneExpression(AloneExpression aloneExpression, SymbolTable localTable);
    T visitArrayExpression(ArrayExpression arrayExpression, SymbolTable localTable);
    T visitAssignment(Assignment assignment, SymbolTable localTable);
    T visitBinaryExpression(BinaryExpression binaryExpression, SymbolTable localTable);
    T visitIdentifier(Identifier identifier, SymbolTable localTable);
    T visitUnaryExpression(UnaryExpression unaryExpression, SymbolTable localTable);


//    T visitOperator(Operator operator, SymbolTable localTable);
    T visitBinaryOperator(BinaryOperator binaryOperator, SymbolTable localTable);
    T visitUnaryOperator(UnaryOperator unaryOperator, SymbolTable localTable);

//    T visitTerm(Term term, SymbolTable localTable);
    T visitConstValue(ConstVal constVal, SymbolTable localTable);
    T visitFunctionCall(FunctionCall functionCall, SymbolTable localTable);
    T visitNewRecord(NewRecord newRecord, SymbolTable localTable);
    T visitParamCall(ParamCall paramCall, SymbolTable localTable);
    T visitParenthesesTerm(ParenthesesTerm parenthesesTerm, SymbolTable localTable);

    T visitProgram(Program program, SymbolTable localTable);
    T visitBlock(Block block, SymbolTable localTable);

    T visitType(Type type, SymbolTable localTable);
    T visitNumType(NumType numType, SymbolTable localTable);

    T visitStatement(Statement statement, SymbolTable localTable);
    T visitForLoop(ForLoop forLoop, SymbolTable localTable);
    T visitFreeStatement(FreeStatement freeStatement, SymbolTable localTable);
    T visitFunctionDefinition(FunctionDefinition functionDefinition, SymbolTable localTable);
    T visitIfStatement(IfStatement  ifStatement, SymbolTable localTable);
    T visitParamDefinition(ParamDefinition paramDefinition, SymbolTable localTable);
    T visitRecordDefinition(RecordDefinition recordDefinition, SymbolTable localTable);
    T visitRecordFieldDefinition(RecordFieldDefinition recordFieldDefinition, SymbolTable localTable);
    T visitReturnStatement(ReturnStatement returnStatement, SymbolTable localTable);
    T visitVariableAssignment(VariableAssignment variableAssignment, SymbolTable localTable);
    T visitVariableDeclaration(VariableDeclaration variableDeclaration, SymbolTable localTable);

    T visitWhileLoop(WhileLoop whileLoop, SymbolTable localTable);
}

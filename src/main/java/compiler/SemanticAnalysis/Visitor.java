package compiler.SemanticAnalysis;

import compiler.Parser.ASTNodes.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.ArrayExpression;
import compiler.Parser.ASTNodes.Statements.Statements.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.*;
import compiler.Parser.ASTNodes.Types.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Operators.*;
import compiler.SemanticAnalysis.Errors.ReturnError;
import compiler.SemanticAnalysis.Errors.SemanticException;


public interface Visitor<T> {

    T visitArrayAccess(ArrayAccess arrayAccess, SymbolTable localTable) throws SemanticException;
    T visitIdentifierAccess(IdentifierAccess identifierAccess, SymbolTable localTable) throws SemanticException;
    T visitRecordAccess(RecordAccess recordAccess, SymbolTable localTable) throws SemanticException;

    T visitAloneExpression(AloneExpression aloneExpression, SymbolTable localTable) throws SemanticException;
    T visitArrayExpression(ArrayExpression arrayExpression, SymbolTable localTable) throws SemanticException;
    T visitAssignment(Assignment assignment, SymbolTable localTable) throws SemanticException;
    T visitBinaryExpression(BinaryExpression binaryExpression, SymbolTable localTable) throws SemanticException;
    T visitIdentifier(Identifier identifier, SymbolTable localTable) throws SemanticException;
    T visitUnaryExpression(UnaryExpression unaryExpression, SymbolTable localTable) throws SemanticException;


//    T visitOperator(Operator operator, SymbolTable localTable) throws SemanticException;
    T visitBinaryOperator(BinaryOperator binaryOperator, SymbolTable localTable) throws SemanticException;
    T visitUnaryOperator(UnaryOperator unaryOperator, SymbolTable localTable) throws SemanticException;

//    T visitTerm(Term term, SymbolTable localTable) throws SemanticException;
    T visitConstValue(ConstVal constVal, SymbolTable localTable) throws SemanticException;
    T visitFunctionCall(FunctionCall functionCall, SymbolTable localTable) throws SemanticException;
    T visitNewRecord(NewRecord newRecord, SymbolTable localTable) throws SemanticException;
    T visitParamCall(ParamCall paramCall, SymbolTable localTable) throws SemanticException;
    T visitParenthesesTerm(ParenthesesTerm parenthesesTerm, SymbolTable localTable) throws SemanticException;

    T visitProgram(Program program, SymbolTable localTable) throws SemanticException;
    T visitBlock(Block block, SymbolTable localTable) throws SemanticException;

    T visitType(Type type, SymbolTable localTable) throws SemanticException;
    T visitNumType(NumType numType, SymbolTable localTable) throws SemanticException;

    T visitStatement(Statement statement, SymbolTable localTable) throws SemanticException;
    T visitForLoop(ForLoop forLoop, SymbolTable localTable) throws SemanticException;
    T visitFreeStatement(FreeStatement freeStatement, SymbolTable localTable) throws SemanticException;
    T visitFunctionDefinition(FunctionDefinition functionDefinition, SymbolTable localTable) throws SemanticException;
    T visitIfStatement(IfStatement  ifStatement, SymbolTable localTable) throws SemanticException;
    T visitParamDefinition(ParamDefinition paramDefinition, SymbolTable localTable) throws SemanticException;
    T visitRecordDefinition(RecordDefinition recordDefinition, SymbolTable localTable) throws SemanticException;
    T visitRecordFieldDefinition(RecordFieldDefinition recordFieldDefinition, SymbolTable localTable) throws SemanticException;
    T visitReturnStatement(ReturnStatement returnStatement, SymbolTable localTable) throws SemanticException;
    T visitVariableAssignment(VariableAssignment variableAssignment, SymbolTable localTable) throws SemanticException;
    T visitVariableDeclaration(VariableDeclaration variableDeclaration, SymbolTable localTable) throws SemanticException;

    T visitWhileLoop(WhileLoop whileLoop, SymbolTable localTable) throws SemanticException;
}

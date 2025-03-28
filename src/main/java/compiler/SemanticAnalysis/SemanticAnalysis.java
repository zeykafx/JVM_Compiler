package compiler.SemanticAnalysis;

import compiler.Parser.ASTNodes.ASTNode;
import compiler.Parser.ASTNodes.Block;
import compiler.Parser.ASTNodes.Program;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.ArrayAccess;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.IdentifierAccess;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.RecordAccess;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Operators.BinaryOperator;
import compiler.Parser.ASTNodes.Statements.Expressions.Operators.UnaryOperator;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.*;
import compiler.Parser.ASTNodes.Statements.Statements.*;
import compiler.Parser.ASTNodes.Types.NumType;
import compiler.Parser.ASTNodes.Types.Type;

import java.util.ArrayList;

public class SemanticAnalysis implements Visitor<Type> {

	ASTNode rootNode;
	SymbolTable globalSymbolTable;

	public SemanticAnalysis(ASTNode rootNode) {
		this.rootNode = rootNode;
	}

	public void analyze() {
		rootNode.accept(this);
	}

	@Override
	public Type visitArrayAccess(ArrayAccess arrayAccess) {

	}

	@Override
	public Type visitIdentifierAccess(IdentifierAccess identifierAccess) {

	}

	@Override
	public Type visitRecordAccess(RecordAccess recordAccess) {

	}

	@Override
	public Type visitAloneExpression(AloneExpression aloneExpression) {

	}

	@Override
	public Type visitArrayExpression(ArrayExpression arrayExpression) {

	}

	@Override
	public Type visitAssignment(Assignment assignment) {

	}

	@Override
	public Type visitBinaryExpression(BinaryExpression binaryExpression) {

	}

	@Override
	public Type visitIdentifier(Identifier identifier) {

	}

	@Override
	public Type visitUnaryExpression(UnaryExpression unaryExpression) {

	}

	@Override
	public Type visitBinaryOperator(BinaryOperator binaryOperator) {

	}

	@Override
	public Type visitUnaryOperator(UnaryOperator unaryOperator) {

	}

	@Override
	public Type visitConstValue(ConstVal constVal) {

	}

	@Override
	public Type visitFunctionCall(FunctionCall functionCall) {

	}

	@Override
	public Type visitNewRecord(NewRecord newRecord) {

	}

	@Override
	public Type visitParamCall(ParamCall paramCall) {

	}

	@Override
	public Type visitParenthesesTerm(ParenthesesTerm parenthesesTerm) {

	}

	@Override
	public Type visitProgram(Program program) {

	}

	@Override
	public Type visitBlock(Block block) {

	}

	@Override
	public Type visitType(Type type) {

	}

	@Override
	public Type visitNumType(NumType numType) {

	}

	@Override
	public Type visitStatement(Statement statement) {

	}

	@Override
	public Type visitForLoop(ForLoop forLoop) {

	}

	@Override
	public Type visitFreeStatement(FreeStatement freeStatement) {

	}

	@Override
	public Type visitFunctionDefinition(FunctionDefinition functionDefinition) {

	}

	@Override
	public Type visitIfStatement(IfStatement ifStatement) {

	}

	@Override
	public Type visitParamDefinition(ParamDefinition paramDefinition) {

	}

	@Override
	public Type visitRecordDefinition(RecordDefinition recordDefinition) {

	}

	@Override
	public Type visitRecordFieldDefinition(RecordFieldDefinition recordFieldDefinition) {

	}

	@Override
	public Type visitReturnStatement(ReturnStatement returnStatement) {

	}

	@Override
	public Type visitVariableAssignment(VariableAssignment variableAssignment) {

	}

	@Override
	public Type visitVariableDeclaration(VariableDeclaration variableDeclaration) {

	}

	@Override
	public Type visitWhileLoop(WhileLoop whileLoop) {

	}
}

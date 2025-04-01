package compiler.SemanticAnalysis;

import compiler.Lexer.Symbol;
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
import compiler.SemanticAnalysis.Types.ArraySemType;
import compiler.SemanticAnalysis.Types.FunctionSemType;
import compiler.SemanticAnalysis.Types.RecordSemType;
import compiler.SemanticAnalysis.Types.SemType;

import java.util.ArrayList;
import java.util.HashMap;

public class SemanticAnalysis implements Visitor<SemType> {

	ASTNode rootNode;
	SymbolTable globalSymbolTable;


	SemType intType = new SemType("int");
	SemType stringType = new SemType("string");
	SemType floatType = new SemType("float");
	SemType boolType = new SemType("bool");
	SemType voidType = new SemType("void");
	SemType anyType = new SemType("any");
	SemType recType = new SemType("rec");

	public SemanticAnalysis() {
	}

	public void analyze(ASTNode rootNode) {
		this.rootNode = rootNode;
		globalSymbolTable = new SymbolTable(null);

		addPredefinedFunctions();

		rootNode.accept(this, globalSymbolTable);
	}

	/// add predefined functions to the symbol table
	private void addPredefinedFunctions() {
		// add predefined functions to the symbol table
		SemType[] paramTypesChr = {stringType};
		FunctionSemType chr = new FunctionSemType("int", paramTypesChr);
		globalSymbolTable.addSymbol("chr", chr);

		// len function definition for strings
		SemType[] paramTypesLenString = {stringType};
		FunctionSemType len = new FunctionSemType("len", paramTypesLenString);
		globalSymbolTable.addSymbol("len", len);

		// len function definition for arrays (of any SemType)
		ArraySemType arrayType = new ArraySemType(anyType);
		SemType[] paramTypesLenArray = {arrayType};
		FunctionSemType lenArray = new FunctionSemType("len", paramTypesLenArray);
		globalSymbolTable.addSymbol("len", lenArray);

		// floor(float) -> int
		SemType[] paramTypesFloor = {floatType};
		FunctionSemType floor = new FunctionSemType("int", paramTypesFloor);
		globalSymbolTable.addSymbol("floor", floor);

		// readInt() -> int
		SemType[] paramTypesReadInt = {};
		FunctionSemType readInt = new FunctionSemType("int", paramTypesReadInt);
		globalSymbolTable.addSymbol("readInt", readInt);

		// readFloat() -> float
		SemType[] paramTypesReadFloat = {};
		FunctionSemType readFloat = new FunctionSemType("float", paramTypesReadFloat);
		globalSymbolTable.addSymbol("readFloat", readFloat);

		// readString() -> string
		SemType[] paramTypesReadString = {};
		FunctionSemType readString = new FunctionSemType("string", paramTypesReadString);
		globalSymbolTable.addSymbol("readString", readString);

		// writeInt(int) -> void
		SemType[] paramTypesWriteInt = {intType};
		FunctionSemType writeInt = new FunctionSemType("void", paramTypesWriteInt);
		globalSymbolTable.addSymbol("writeInt", writeInt);

		// writeFloat(float) -> void
		SemType[] paramTypesWriteFloat = {floatType};
		FunctionSemType writeFloat = new FunctionSemType("void", paramTypesWriteFloat);
		globalSymbolTable.addSymbol("writeFloat", writeFloat);

		// write(any) -> void
		SemType[] paramTypesWrite = {anyType};
		FunctionSemType write = new FunctionSemType("void", paramTypesWrite);
		globalSymbolTable.addSymbol("write", write);

		// writeln(any) -> void
		SemType[] paramTypesWriteln = {anyType};
		FunctionSemType writeln = new FunctionSemType("void", paramTypesWriteln);
		globalSymbolTable.addSymbol("writeln", writeln);
	}

	@Override
	public SemType visitProgram(Program program, SymbolTable table) {
		// add constants to the symbol table
		ArrayList<VariableDeclaration> constants = program.getConstants();
		for (VariableDeclaration constant : constants) {
			// we don't need to check if the constant is defined as 'final', this is already done by the parser
			constant.accept(this, table);
		}

		ArrayList<RecordDefinition> records = program.getRecords();
		for (RecordDefinition record : records) {
			record.accept(this, table);
		}

		ArrayList<VariableDeclaration> globals = program.getGlobals();
		for (VariableDeclaration global : globals) {
			global.accept(this, table);
		}

		ArrayList<FunctionDefinition> functions = program.getFunctions();
		for (FunctionDefinition function : functions) {

			FunctionSemType functionSemType = (FunctionSemType) function.accept(this, table);


			// ! maybe we should deallocate functiontable at the end
		}


		return null;
	}

	@Override
	public SemType visitArrayAccess(ArrayAccess arrayAccess, SymbolTable table) {

	}

	@Override
	public SemType visitIdentifierAccess(IdentifierAccess identifierAccess, SymbolTable table) {

	}

	@Override
	public SemType visitRecordAccess(RecordAccess recordAccess, SymbolTable table) {

	}

	@Override
	public SemType visitAloneExpression(AloneExpression aloneExpression, SymbolTable table) {

	}

	@Override
	public SemType visitArrayExpression(ArrayExpression arrayExpression, SymbolTable table) {

	}

	@Override
	public SemType visitAssignment(Assignment assignment, SymbolTable table) {

	}

	@Override
	public SemType visitBinaryExpression(BinaryExpression binaryExpression, SymbolTable table) {
		// check if the left and right expressions are of the same SemType
	}

	@Override
	public SemType visitIdentifier(Identifier identifier, SymbolTable table) {

	}

	@Override
	public SemType visitUnaryExpression(UnaryExpression unaryExpression, SymbolTable table) {

	}

	@Override
	public SemType visitBinaryOperator(BinaryOperator binaryOperator, SymbolTable table) {

	}

	@Override
	public SemType visitUnaryOperator(UnaryOperator unaryOperator, SymbolTable table) {

	}

	@Override
	public SemType visitConstValue(ConstVal constVal, SymbolTable table) {

	}

	@Override
	public SemType visitFunctionCall(FunctionCall functionCall, SymbolTable table) {

	}

	@Override
	public SemType visitNewRecord(NewRecord newRecord, SymbolTable table) {

	}

	@Override
	public SemType visitParamCall(ParamCall paramCall, SymbolTable table) {

	}

	@Override
	public SemType visitParenthesesTerm(ParenthesesTerm parenthesesTerm, SymbolTable table) {

	}

	@Override
	public SemType visitBlock(Block block, SymbolTable table) {

	}

	@Override
	public SemType visitType(Type type, SymbolTable table) {

	}

	@Override
	public SemType visitNumType(NumType numType, SymbolTable table) {

	}

	@Override
	public SemType visitStatement(Statement statement, SymbolTable table) {

	}

	@Override
	public SemType visitForLoop(ForLoop forLoop, SymbolTable table) {

	}

	@Override
	public SemType visitFreeStatement(FreeStatement freeStatement, SymbolTable table) {

	}

	@Override
	public SemType visitFunctionDefinition(FunctionDefinition functionDefinition, SymbolTable table) {
//		Symbol name = functionDefinition.getIdentifier();

		// check types of parameters

		// check types of block

		// check type of return expression (?)

		// add function to the global symbol table
	}

	@Override
	public SemType visitIfStatement(IfStatement ifStatement, SymbolTable table) {

	}

	@Override
	public SemType visitParamDefinition(ParamDefinition paramDefinition, SymbolTable table) {

	}

	@Override
	public SemType visitRecordDefinition(RecordDefinition recordDefinition, SymbolTable table) {
		HashMap<String, SemType> fields = new HashMap<>();

		for (RecordFieldDefinition field : recordDefinition.getFields()) {
			SemType semType = field.accept(this, table);

			fields.put(field.getIdentifier().lexeme, semType);
		}

		RecordSemType recordSemType = new RecordSemType(fields);

		table.addSymbol(recordDefinition.getIdentifier().lexeme, recordSemType);

		return recordSemType;
	}

	@Override
	public SemType visitRecordFieldDefinition(RecordFieldDefinition recordFieldDefinition, SymbolTable table) {
		SemType semType;
		if (recordFieldDefinition.getType().isList) {
			SemType elementSemType = new SemType(recordFieldDefinition.getType().symbol.lexeme);
			semType = new ArraySemType(elementSemType);
		} else {
			semType = new SemType(recordFieldDefinition.getType().symbol.lexeme);
		}
		return semType;
	}

	@Override
	public SemType visitReturnStatement(ReturnStatement returnStatement, SymbolTable table) {

	}

	@Override
	public SemType visitVariableAssignment(VariableAssignment variableAssignment, SymbolTable table) {

	}

	@Override
	public SemType visitVariableDeclaration(VariableDeclaration variableDeclaration, SymbolTable table) {
		Symbol name = variableDeclaration.getName();
		Type type = variableDeclaration.getType();
		SemType semType = new SemType(type.symbol.lexeme, variableDeclaration.isConstant());

		table.addSymbol(name.lexeme, semType);

		return semType;
	}

	@Override
	public SemType visitWhileLoop(WhileLoop whileLoop, SymbolTable table) {

	}
}

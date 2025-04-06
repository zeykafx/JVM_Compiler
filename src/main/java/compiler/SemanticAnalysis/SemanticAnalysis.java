package compiler.SemanticAnalysis;

import compiler.Lexer.Symbol;
import compiler.Lexer.TokenTypes;
import compiler.Parser.ASTNodes.ASTNode;
import compiler.Parser.ASTNodes.Block;
import compiler.Parser.ASTNodes.Program;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.Access;
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
import compiler.SemanticAnalysis.Errors.*;
import compiler.SemanticAnalysis.Types.ArraySemType;
import compiler.SemanticAnalysis.Types.FunctionSemType;
import compiler.SemanticAnalysis.Types.RecordSemType;
import compiler.SemanticAnalysis.Types.SemType;

import java.util.ArrayList;
import java.util.Objects;
import java.util.TreeMap;

import static compiler.Lexer.TokenTypes.*;

public class SemanticAnalysis implements Visitor<SemType> {

	ASTNode rootNode;
	SymbolTable globalSymbolTable;


	SemType intType = new SemType("int");
	SemType floatType = new SemType("float");
	SemType numType = new SemType("num");
	SemType numOrBoolType = new SemType("numOrBool");
	SemType stringType = new SemType("string");
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

		try {
			rootNode.accept(this, globalSymbolTable);
		} catch (SemanticException e) {
			System.err.println("Syntax Error: " + e.getMessage());
			System.exit(2); // 2 for semantic analysis
		}
		catch (Exception e) {
			System.err.println("Unexpected error during semantic analysis of the program: " + e.getMessage());
			System.exit(2);

		}
	}

	/// add predefined functions to the symbol table
	private void addPredefinedFunctions() {
		// add predefined functions to the symbol table
		SemType[] paramTypesChr = {stringType};
		FunctionSemType chr = new FunctionSemType(intType, paramTypesChr);
		globalSymbolTable.addSymbol("chr", chr);

		// len function definition for strings
		SemType[] paramTypesLenString = {stringType};
		FunctionSemType len = new FunctionSemType(intType, paramTypesLenString);
		globalSymbolTable.addSymbol("len", len);

		// len function definition for arrays (of any SemType)
		ArraySemType arrayType = new ArraySemType(anyType);
		SemType[] paramTypesLenArray = {arrayType};
		FunctionSemType lenArray = new FunctionSemType(intType, paramTypesLenArray);
		globalSymbolTable.addSymbol("len", lenArray);

		// floor(float) -> int
		SemType[] paramTypesFloor = {floatType};
		FunctionSemType floor = new FunctionSemType(intType, paramTypesFloor);
		globalSymbolTable.addSymbol("floor", floor);

		// readInt() -> int
		SemType[] paramTypesReadInt = {};
		FunctionSemType readInt = new FunctionSemType(intType, paramTypesReadInt);
		globalSymbolTable.addSymbol("readInt", readInt);

		// readFloat() -> float
		SemType[] paramTypesReadFloat = {};
		FunctionSemType readFloat = new FunctionSemType(floatType, paramTypesReadFloat);
		globalSymbolTable.addSymbol("readFloat", readFloat);

		// readString() -> string
		SemType[] paramTypesReadString = {};
		FunctionSemType readString = new FunctionSemType(stringType, paramTypesReadString);
		globalSymbolTable.addSymbol("readString", readString);

		// writeInt(int) -> void
		SemType[] paramTypesWriteInt = {intType};
		FunctionSemType writeInt = new FunctionSemType(voidType, paramTypesWriteInt);
		globalSymbolTable.addSymbol("writeInt", writeInt);

		// writeFloat(float) -> void
		SemType[] paramTypesWriteFloat = {floatType};
		FunctionSemType writeFloat = new FunctionSemType(voidType, paramTypesWriteFloat);
		globalSymbolTable.addSymbol("writeFloat", writeFloat);

		// write(any) -> void
		SemType[] paramTypesWrite = {anyType};
		FunctionSemType write = new FunctionSemType(voidType, paramTypesWrite);
		globalSymbolTable.addSymbol("write", write);

		// writeln(any) -> void
		SemType[] paramTypesWriteln = {anyType};
		FunctionSemType writeln = new FunctionSemType(voidType, paramTypesWriteln);
		globalSymbolTable.addSymbol("writeln", writeln);
	}

	@Override
	public SemType visitProgram(Program program, SymbolTable table) throws SemanticException {
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

			function.accept(this, table);
		}

		return null;
	}

	@Override
	public SemType visitArrayAccess(ArrayAccess arrayAccess, SymbolTable table) throws SemanticException {
		// ArrayAccess -> "[" Expression "]"

		// check if the head is an array
		SemType headType = arrayAccess.getHeadAccess().accept(this, table);

		if (!(headType instanceof ArraySemType)) {
			throw new TypeError("The head of the array access at line " + arrayAccess.line + " is not an array");
		}

		// check if the index is an int
		SemType indexType = arrayAccess.getIndexExpression().accept(this, table);
		if (!indexType.equals(intType)) {
			throw new TypeError("The index of the array access at line " + arrayAccess.line + " is not an int");
		}

		return (ArraySemType) headType;
	}

	@Override
	public SemType visitIdentifierAccess(IdentifierAccess identifierAccess, SymbolTable table) throws SemanticException {
		// IdentifierAccess -> "identifier" AccessChain .
		// AccessChain -> Access AccessChain | .

		// check if the identifier is defined in the symbol table
		Symbol name = identifierAccess.getIdentifier();
		SemType semType = table.lookup(name.lexeme);
		if (semType == null) {
			// if the identifier is not found throw an error
			throw new ScopeError("Identifier " + name.lexeme + " not found in symbol table");
		}

		return semType;
	}

	@Override
	public SemType visitRecordAccess(RecordAccess recordAccess, SymbolTable table) throws SemanticException {
		// IdentifierAccess -> "identifier" AccessChain .
		// AccessChain -> Access AccessChain | .
		// Access -> "[" Expression "]" | "." "identifier" .

		// check if the identifier is defined in the symbol table
		Access headAccess = recordAccess.getHeadAccess();
		SemType headType = headAccess.accept(this, table);

		if (headType instanceof RecordSemType || headType instanceof ArraySemType) {
			RecordSemType recordSemType;
			if (headType instanceof ArraySemType arraySemType) {
				recordSemType = (RecordSemType) arraySemType.getElementSemType();
			} else {
				recordSemType = (RecordSemType) headType;
			}

			// check if the field is defined in the record
			Symbol field = recordAccess.getIdentifier();
			SemType fieldType = recordSemType.fields.get(field.lexeme);
			if (fieldType == null) {
				throw new SemanticException("Field "+ field.lexeme + " is not defined on record type " + recordAccess.getHeadAccess().toString());
			}

			// return type of accessed field
			return fieldType;
		} else {
			throw new TypeError("The head of the record access at line " + recordAccess.line + " is not a record");
		}

 	}


	@Override
	public SemType visitArrayExpression(ArrayExpression arrayExpression, SymbolTable table) throws SemanticException {
		// ArrayExpression -> "array" "[" "intval" "]" "of" Type ";" .

		SemType sizeExpressionSemType = arrayExpression.getSizeExpression().accept(this, table);
		// the size expression can be an int or a float
		if (!(sizeExpressionSemType.equals(intType) || sizeExpressionSemType.equals(floatType))) {
			throw new TypeError("Size expression of array creation at line " + arrayExpression.line + " is not an int or a float (variable or literal)");
		}

		SemType elemSemType = new SemType(arrayExpression.getType().symbol.lexeme);
		return new ArraySemType(elemSemType);
 	}

	@Override
	public SemType visitBinaryExpression(BinaryExpression binaryExpression, SymbolTable table) throws SemanticException {
		// check if the left and right expressions are of the same SemType

		SemType leftType = binaryExpression.getLeftTerm().accept(this, table);
		SemType rightType = binaryExpression.getRightTerm().accept(this, table);
		if (!leftType.equals(rightType)) {
			// Note: we don't do the implicit conversion of ints to floats here because the .equals method on SemType should do it (TODO: check that this is true)
			throw new TypeError("Types of elements in the binary expression at line "+binaryExpression.line+" do not match: " + leftType + " and " + rightType);
		}

		SemType termsType = leftType; // since both terms are equivalent in types, we can use the left term's type to do the checks

		SemType operatorSemType = binaryExpression.getOperator().accept(this, table);
		if (operatorSemType.equals(boolType)) {
			// this operator only accepts booleans -> "&&", "||"
			if (!termsType.equals(boolType)) {
				throw new TypeError(String.format("Type %s cannot be used in a boolean expression, line %d", termsType, binaryExpression.line));
			}

			return boolType;
		} else if (operatorSemType.equals(numOrBoolType)) {

			// here we have an operator that can accept both booleans and numbers -> "==", "!="
			// so if the type of the terms is not boolean or number, throw an error
			if (!(termsType.equals(boolType) || termsType.equals(numType))) {
				throw new TypeError(String.format("Type %s cannot be used in a with the %s operator", termsType, binaryExpression.getOperator().getSymbol().lexeme));
			}
//
//			if (termsType.equals(boolType)){
//				return boolType;
//			}

			return boolType;

 		} else if (operatorSemType.equals(numType)) {
			if (!termsType.equals(numType)) {
				throw new TypeError(String.format("Type %s cannot be used in a with the %s operator, expected int or float", termsType, binaryExpression.getOperator().getSymbol().lexeme));
			}
			if (binaryExpression.getOperator().numberOperatorReturnsBoolean()) {
				return boolType;
			}
			return termsType;
		}

		// shouldn't be here
		throw new SemanticException(String.format("Unexpected error in expression at line %s", binaryExpression.line));
	}

	@Override
	public SemType visitBinaryOperator(BinaryOperator binaryOperator, SymbolTable table) throws SemanticException {
		// return the type that the operator implies on the expression,
		// e.g., if the operator is + and the expression is int + int, then return int
		// if the operator is && then return a boolean
		// some boolean operators expect bools (&&, ||)
		// some other operators don't care (==, !=,...)
		if (binaryOperator.isBooleanOperator()) {
			return boolType;
		} else if (binaryOperator.isBooleanOrNumberOperator()) {
			return numOrBoolType;
		} else if (binaryOperator.isNumberOperator()) {
			return numType; // numtype means that whatever number type is fine
		}
		return null;
	}

	@Override
	public SemType visitUnaryExpression(UnaryExpression unaryExpression, SymbolTable table) throws SemanticException {
		// return the type of the expression with the unary operator applied

		SemType expressionSemType = unaryExpression.getTerm().accept(this, table);

		SemType operatorSemType = unaryExpression.getOperator().accept(this, table);
		if (operatorSemType.equals(boolType)) {
			if (!expressionSemType.equals(boolType)) {
				throw new SemanticException(String.format("Expected boolean with operator %s", unaryExpression.getOperator().getSymbol().lexeme));
			}

			return boolType;
		} else if (operatorSemType.equals(numType)) {
			if (!expressionSemType.equals(numType)) {
				throw new SemanticException(String.format("Expected int or float with operator %s", unaryExpression.getOperator().getSymbol().lexeme));
			}
			return numType;
		}

		// shouldn't be here
		throw new SemanticException(String.format("Unexpected error in expression at line %s", unaryExpression.line));
	}

	@Override
	public SemType visitUnaryOperator(UnaryOperator unaryOperator, SymbolTable table) throws SemanticException {
		if (unaryOperator.isBinaryOperator()) {
			return boolType;
		} else if (unaryOperator.isNumberOperator()) {
			return numType;
		}

		throw new SemanticException(String.format("Unexpected unary operator %s", unaryOperator.getSymbol().lexeme));
	}

	@Override
	public SemType visitConstValue(ConstVal constVal, SymbolTable table) throws SemanticException {
		// create SemType from this constant value
		String type = switch (constVal.getSymbol().type) {
			case INT_LITERAL -> "int";
			case FLOAT_LITERAL -> "float";
			case STRING_LITERAL -> "string";
			case BOOL_TRUE, BOOL_FALSE -> "bool";
			case REC -> "rec";
			default -> null;
		};

		return new SemType(type, true);
	}

	@Override
	public SemType visitFunctionCall(FunctionCall functionCall, SymbolTable table) throws SemanticException {
		// get the FunctionSemType of the function from the symbol table
		FunctionSemType functionSemType =(FunctionSemType) table.lookup(functionCall.getIdentifier().lexeme);
		if (functionSemType == null) {
			throw new ScopeError("function "+functionCall.getIdentifier().lexeme+" doesn't exist");
		}

		// for each argument/param, compared the present types to the types from the definition
		for (ParamCall paramCall : functionCall.getParameters()) {
			// get the SemType of the parameter
			SemType paramCallSemType = paramCall.accept(this, table);


			SemType[] functionDefParamsSemTypes = functionSemType.getParamSemTypes();
			// special case for functions with "any" for the type of their arguments
			if (functionDefParamsSemTypes.length == 1 && functionDefParamsSemTypes[0] == anyType) {
				break; // exit from the loop, we don't have anything to typecheck
			}


			if (paramCall.getParamIndex() > functionDefParamsSemTypes.length) {
				throw new TypeError("Too many arguments " + paramCall.getParamIndex() + " for function " + functionCall.getIdentifier().lexeme + " with " + functionDefParamsSemTypes.length + " arguments");
			}

			// get the argumentSemType
			SemType argSemType = functionDefParamsSemTypes[paramCall.getParamIndex()];


			if (!paramCallSemType.equals(argSemType)) {
				// the types didn't match, but if the expected type is a float and the given type is an int, we can convert it (here that means we keep going)

				// if we can't convert the type, we throw an error: here if it's not the convertable case, we throw
				if (!(argSemType.equals(floatType) && paramCallSemType.equals(intType))) { // TODO: maybe remove since we added equivalence in the .equals method
					throw new TypeError("Type of the argument index " + paramCall.getParamIndex() + " in the function call " + functionCall.getIdentifier().lexeme + " does not match the type of the argument " + argSemType);
				}

			}
		}

		// return the SemType of the return value (e.g., intType if the function returns an integer)
		return new SemType(functionSemType.type);
	}

	@Override
	public SemType visitNewRecord(NewRecord newRecord, SymbolTable table) throws SemanticException {
		// NewRecord means a record instance creation
		// example: p Product = Product(1, "Phone", 699);
		//                      ^^^^^^^^^^^^^^^^^^^^^^^^

		// check that the record type exists
		RecordSemType recordSemType = (RecordSemType) table.lookup(newRecord.getIdentifier().lexeme);
		if (recordSemType == null) {
			throw new ScopeError("Record type "+newRecord.getIdentifier().lexeme+" doesn't exist");
		}

		// check that the types of the record fields are correct and correspond to the definition

		for (ParamCall paramCall : newRecord.getTerms()) {
			// get the SemType
			SemType paramCallSemType = paramCall.accept(this, table);

			// get the fieldSemType
			// TODO: CHECK THIS BECAUSE IM NOT SURE AT ALL!!
			SemType fieldSemType = recordSemType.fields.values().toArray(new SemType[0])[paramCall.getParamIndex()];
			if (fieldSemType == null) {
				throw new TypeError("Field " + paramCall.getParamIndex() + " does not exist in record type " + newRecord.getIdentifier().lexeme);
			}

			// check that the types match
			if (!paramCallSemType.equals(fieldSemType)) {
				// the types didn't match, but if the expected type is a float and the given type is an int, we can convert it (here that means we keep going)

				// otherwise: throw
				if (!(fieldSemType.equals(floatType) && paramCallSemType.equals(intType))) {
					throw new TypeError("Type of the parameter index " + paramCall.getParamIndex() + " in the record " + newRecord.getIdentifier().lexeme + " does not match the type of the field " + fieldSemType);
				}
			}
		}

		// check that the number of parameters is correct
		if (newRecord.getTerms().size() != recordSemType.fields.size()) {
			throw new TypeError("Number of parameters in the record " + newRecord.getIdentifier().lexeme + " does not match the number of fields " + recordSemType.fields.size());
		}

		return recordSemType;
	}

	@Override
	public SemType visitParamCall(ParamCall paramCall, SymbolTable table) throws SemanticException {
		// ParamCall is one parameter assignment when creating a new record or when calling a function
		// example: p Product = Product(1, "Phone", 699);
		//                              ^  ^^^^^^^  ^^^
		//                              -> Each of these is a ParamCall

		// we just return the type of the expression
		return paramCall.getParamExpression().accept(this, table);
	}

	@Override
	public SemType visitParenthesesTerm(ParenthesesTerm parenthesesTerm, SymbolTable table) throws SemanticException {
		// return the SemType of the expression that is inside the parentheses
		return parenthesesTerm.getExpression().accept(this, table);
	}

	@Override
	public SemType visitType(Type type, SymbolTable table) throws SemanticException {
		if (type.isList) {
			return new ArraySemType(new SemType(type.symbol.lexeme));
		}
		return new SemType(type.symbol.lexeme);
	}

	@Override
	public SemType visitNumType(NumType numType, SymbolTable table) throws SemanticException {
		if (numType.isList) {
			return new ArraySemType(new SemType(numType.symbol.lexeme));
		}
		return new SemType(numType.symbol.lexeme);
	}

	@Override
	public SemType visitStatement(Statement statement, SymbolTable table) throws SemanticException {
		throw new SemanticException("this should never be called");
	}

	@Override
	public SemType visitForLoop(ForLoop forLoop, SymbolTable table) throws SemanticException {
		// ForCondition -> "identifier" "," Term "," Term "," Term .
		//
		// the increment shouldn't be 0
		// if the variable is an int, the start, step, and stop should all be ints
		// but if the variable is a float, start, step, and stop can be ints (they'll be converted to floats)
		// check all identifiers are Term

		// check if the variable is defined in the symbol table
		Symbol varSymbol = forLoop.getVariable();
		SemType varType = table.lookup(varSymbol.lexeme);
		if (varType == null) {
            // if the identifier is not found, throw an error
            throw new ScopeError("Identifier " + varSymbol.lexeme + " in for loop at line "+ varSymbol.line +" was not found in symbol table");
        }

		boolean loopVarIsInt = false;

        // check if the variable is a number
        if (!(varType.equals(intType) || varType.equals(floatType))) {
            throw new TypeError("Variable " + varSymbol.lexeme + " in for loop at line "+ varSymbol.line +" is not a number");
        }

		// if the loop var is an int, then this is the most restrictive case, the start, step, and stop vars must also all be ints
		if (varType.equals(intType)) {
			loopVarIsInt = true;
		}

        // check if the start, step, and stop are of the same type as the variable
        Symbol start = forLoop.getStart();
		typeCheckLoopFields(table,loopVarIsInt, start, "start");

		Symbol step = forLoop.getStep();
		typeCheckLoopFields(table,loopVarIsInt, step, "step");

		// check that the increment is not 0 (we only check if it is a literal, otherwise we can't check atp)
		if (step.type == INT_LITERAL && step.value == (Integer) 0) {
			throw new SemanticException("The step of the for loop at line " + step.line + " is 0, this loop will never make progress");
		}

		if (step.type == TokenTypes.FLOAT_LITERAL && step.value == (Float) 0.0f) {
			throw new SemanticException("The step of the for loop at line " + step.line + " is 0.0 (float), this loop will never make progress");
		}

		Symbol end = forLoop.getEnd();
		typeCheckLoopFields(table,loopVarIsInt, end, "end");

		// then visit the block
		forLoop.getBlock().accept(this, table);

		return null;
	}

	private void typeCheckLoopFields(SymbolTable table, boolean loopVarIsInt, Symbol fieldSymbol, String fieldName) throws TypeError {
		if (fieldSymbol.type == INT_LITERAL || fieldSymbol.type == TokenTypes.FLOAT_LITERAL) {

			// if the loop var is an int and the field (start, step, stop) number is a float, we throw an error
			if (loopVarIsInt && fieldSymbol.type == TokenTypes.FLOAT_LITERAL) {
				throw new TypeError("The "+fieldName+" number in the for loop at line" + fieldSymbol.line + ": '"+ fieldSymbol.lexeme +"' is not an integer but the loop variable is.");
			}
			// otherwise we're good

		} else if (fieldSymbol.type == TokenTypes.IDENTIFIER) {
        	SemType fieldType = table.lookup(fieldSymbol.lexeme);

			// if the loop var is an int and field var is a float, then we throw an error
			if (loopVarIsInt && fieldType.equals(floatType)) {
				throw new TypeError("The "+fieldName+" variable in the for loop at line" + fieldSymbol.line + ": '"+ fieldSymbol.lexeme +"' is not an integer but the loop variable is.");
			}
			// otherwise we're good (I think)
		} else {
			throw new TypeError("Type of the '"+fieldName+"' field in the for loop at line " + fieldSymbol.line + " is not valid, it should either be a variable identifier, int, or float");
		}
	}

	@Override
	public SemType visitWhileLoop(WhileLoop whileLoop, SymbolTable table) throws SemanticException {
		// WhileLoop -> "while" "(" Expression ")" Block .

		// check that the expression evaluates to a boolean
		SemType conditionType = whileLoop.getCondition().accept(this, table);
		if (!conditionType.equals(boolType)) {
			throw new MissingConditionError("The while loop's condition at line "+ whileLoop.line +" does not evaluate to a boolean.");
		}

		// then check the block
		whileLoop.getBlock().accept(this, table);

		return null;
	}

	@Override
	public SemType visitFreeStatement(FreeStatement freeStatement, SymbolTable table) throws SemanticException {
		// check if the identifier is defined in the symbol table
		IdentifierAccess identifierAccess = freeStatement.getIdentifierAccess();
		Symbol name = identifierAccess.getIdentifier();
		SemType semType = table.lookup(name.lexeme);
		if (semType == null) {
			// if the identifier is not found, throw an error
			throw new ScopeError("Identifier " + name.lexeme + " not found in symbol table");
		}
		return null;
	}

	@Override
	public SemType visitFunctionDefinition(FunctionDefinition functionDefinition, SymbolTable table) throws SemanticException {
		Symbol name = functionDefinition.getName();

		// create local table
		SymbolTable localTable = new SymbolTable(table, name.lexeme);

		// add parameters to the local symbol table
		ArrayList<SemType> paramTypes = new ArrayList<>();
		for (ParamDefinition param : functionDefinition.getParamDefinitions()){
			SemType paramSemType = param.accept(this, localTable);

			paramTypes.add(paramSemType);
		}

		// add function to the symbol table
		Type returnType = functionDefinition.getReturnType();

		SemType retSemType = getSemTypeFromASTNodeType(table, returnType);

		FunctionSemType semType = new FunctionSemType(retSemType, paramTypes.toArray(new SemType[0]));
		table.addSymbol(name.lexeme, semType);

		// check types of block
		Block block = functionDefinition.getBlock();
		block.accept(this, localTable);

		// check type of return expression (done in block)
		return null;
	}

	private SemType getSemTypeFromASTNodeType(SymbolTable table, Type returnType) {
		SemType retSemType;
		if (returnType.isList) {
			SemType elemSemType;
			if (returnType.symbol.type == RECORD) {
				elemSemType = table.lookup(returnType.symbol.lexeme);
			} else {
				elemSemType = new SemType(returnType.symbol.lexeme);
			}

			retSemType = new ArraySemType(elemSemType);
		} else if (returnType.symbol.type == RECORD) {
			retSemType = table.lookup(returnType.symbol.lexeme);
		} else {
			retSemType = new SemType(returnType.symbol.lexeme);
		}
		return retSemType;
	}

	@Override
	public SemType visitBlock(Block block, SymbolTable table) throws SemanticException {
		for (Statement stmt : block.getStatements()) {
			stmt.accept(this, table);
		}
		ReturnStatement returnStatement = (ReturnStatement) block.getReturnStatement();
		if (returnStatement != null) {
			returnStatement.accept(this, table);
		}
		return null;
	}

	@Override
	public SemType visitIfStatement(IfStatement ifStatement, SymbolTable table) throws SemanticException {
		// IfStmt -> "if" "(" Expression ")" Block ElseStmt .
		//ElseStmt -> "else" Block | .

		// first check that the condition is a boolean
		SemType conditionType = ifStatement.getCondition().accept(this, table);
		if (!conditionType.equals(boolType)) {
			throw new MissingConditionError("The if statement's condition at line " + ifStatement.line + " does not evaluate to a boolean.");
		}

		// then check the block
		ifStatement.getThenBlock().accept(this, table);

		// check the else block if it exists
		if (ifStatement.isElse()) {
			ifStatement.getElseBlock().accept(this, table);
		}

		return null;
	}

	@Override
	public SemType visitParamDefinition(ParamDefinition paramDefinition, SymbolTable table) throws SemanticException {

		// add param to local symbol table
		Symbol name = paramDefinition.getIdentifier();
		Type type = paramDefinition.getType();
		System.out.println(type);

		SemType semType = getSemTypeFromASTNodeType(table, type);

		table.addSymbol(name.lexeme, semType);
		return semType;
	}

	@Override
	public SemType visitRecordDefinition(RecordDefinition recordDefinition, SymbolTable table) throws SemanticException {
		TreeMap<String, SemType> fields = new TreeMap<>();

		for (RecordFieldDefinition field : recordDefinition.getFields()) {
			SemType semType = field.accept(this, table);

			fields.put(field.getIdentifier().lexeme, semType);
		}

		RecordSemType recordSemType = new RecordSemType(fields, recordDefinition.getIdentifier().lexeme);

		// check that the new record identifier does not shadow any other identifier in the table (this includes other record, but also predefined functions)
		SemType existingRecord = table.lookup(recordDefinition.getIdentifier().lexeme);
		if (existingRecord != null) { // IF IT'S NOT NULL, then throw an error
			throw new RecordError("Record " + recordDefinition.getIdentifier().lexeme + " already exists in the symbol table");
		}

		table.addSymbol(recordDefinition.getIdentifier().lexeme, recordSemType);

		return recordSemType;
	}

	@Override
	public SemType visitRecordFieldDefinition(RecordFieldDefinition recordFieldDefinition, SymbolTable table) throws SemanticException {
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
	public SemType visitReturnStatement(ReturnStatement returnStatement, SymbolTable table) throws SemanticException{
		String localFunctionName = table.getLocalFunctionName();
		FunctionSemType functionSemType = (FunctionSemType) table.lookup(localFunctionName);

		if (functionSemType == null) {
			// if localFunctionName is null => throw Error
			// couldn't find function
			throw new ScopeError("Function not found in symbol table");
		}

		SemType returnSemType;
		if (returnStatement.getExpression() == null) {
			returnSemType = voidType;
		} else {
			returnSemType = returnStatement.getExpression().accept(this, table);
		}

		if (!functionSemType.getRetType().equals(returnSemType)) {
			throw new TypeError("Return at line "+returnStatement.line+" type does not match function return type. Expected: " + functionSemType.getRetType() + ", found: " + returnSemType);
		}

		return null;
		/*  AS A BONUS : CHECK IF FUNCTION ALWAYS RETURNS A VALUE
		notre idée : ajouter un attribut "retNumber" à la localSymbolTable,
		TODO trouver l'algorithme qui permet de vérifier que la fonction retourne toujours une valeur

			fun f (a int) int { // <-- f(int) -> int, retNumber = 1
				if (qsdf) {
				   someExpres;
				   if {
					 return 1;
				   }
				}
				return 2;
			}

		 */
	}

	@Override
	public SemType visitVariableAssignment(VariableAssignment variableAssignment, SymbolTable table) throws SemanticException {
		// VariableAssignment -> IdentifierAccess "=" Expression .
		// IdentifierAccess -> "identifier" AccessChain .
		// AccessChain -> Access AccessChain | .
		// Access -> "[" Expression "]" | "." "identifier" .

		// first get the type of the variable
		SemType varType = variableAssignment.getAccess().accept(this, table);

		// then get the type of the expression
		SemType expressionType = variableAssignment.getExpression().accept(this, table);

		// and check that they match
		if (!varType.equals(expressionType)) {

			// the types didn't match, but if the expected type is a float and the given type is an int, we can convert it (here that means we keep going)

			// if we can't convert the type, we throw an error: here if it's not the convertable case, we throw
			if (!(varType.equals(floatType) && expressionType.equals(intType))) {
				throw new TypeError("Type of the variable " + variableAssignment.getAccess().toString() + " at line " + variableAssignment.line + " does not match the type of the expression " + variableAssignment.getExpression().toString());
			}

		}

		// then check that the variable is not a constant
		if (varType.isConstant) {
			throw new TypeError("Cannot assign to a constant variable " + variableAssignment.getAccess().toString() + " at line " + variableAssignment.line);
		}

		return null;
	}

	@Override
	public SemType visitVariableDeclaration(VariableDeclaration variableDeclaration, SymbolTable table) throws SemanticException {
		Symbol name = variableDeclaration.getName();
		Type type = variableDeclaration.getType();
		SemType semType = new SemType(type.symbol.lexeme, variableDeclaration.isConstant());

		table.addSymbol(name.lexeme, semType);

		return semType;
	}
}

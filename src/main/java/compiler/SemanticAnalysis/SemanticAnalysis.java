package compiler.SemanticAnalysis;

import compiler.Lexer.Symbol;
import compiler.Lexer.TokenTypes;
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
import compiler.SemanticAnalysis.Errors.*;
import compiler.SemanticAnalysis.Types.ArraySemType;
import compiler.SemanticAnalysis.Types.FunctionSemType;
import compiler.SemanticAnalysis.Types.RecordSemType;
import compiler.SemanticAnalysis.Types.SemType;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;

import static compiler.Lexer.TokenTypes.*;

public class SemanticAnalysis implements Visitor<SemType, SymbolTable> {

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

	public void analyze(ASTNode rootNode, boolean inTest) throws SemanticException {
		this.rootNode = rootNode;
		globalSymbolTable = new SymbolTable(null);

		addPredefinedFunctions();

		try {
			rootNode.accept(this, globalSymbolTable);
		} catch (SemanticException e) {
			if (inTest) {
				// if we are in test mode, we don't want to exit the program
				throw e;
			}
//			System.err.println("Semantic Analysis Error: " + e.getMessage());
			e.printStackTrace();

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
		SemType[] paramTypesChr = {intType};
		FunctionSemType chr = new FunctionSemType(stringType, paramTypesChr);
		globalSymbolTable.addSymbol("chr", chr);

		// EXTRA FUNCTION ADDED
		SemType[] paramTypesOrd = {stringType};
		FunctionSemType ord = new FunctionSemType(intType, paramTypesOrd);
		globalSymbolTable.addSymbol("ord", ord);

		// len function definition for strings
		SemType[] paramTypesLenString = {stringType};
		FunctionSemType len = new FunctionSemType(intType, paramTypesLenString);
		globalSymbolTable.addSymbol("len_string", len);

		// len function definition for arrays (of any SemType)
		ArraySemType arrayType = new ArraySemType(anyType);
		SemType[] paramTypesLenArray = {arrayType};
		FunctionSemType lenArray = new FunctionSemType(intType, paramTypesLenArray);
		globalSymbolTable.addSymbol("len_array", lenArray);

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
	public SemType visitProgram(Program program, SymbolTable table) throws Exception {
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
	public SemType visitArrayAccess(ArrayAccess arrayAccess, SymbolTable table) throws Exception {
		// ArrayAccess -> "[" Expression "]"

		// check if the head is an array
		SemType headType = arrayAccess.getHeadAccess().accept(this, table);

		// quick fix for strings access
		if (headType.equals(stringType)) {
			arrayAccess.semtype = intType; // intType because we index into a string and get a char

			return arrayAccess.semtype;
		}

		if (!(headType instanceof ArraySemType arraySemType)) {
			throw new TypeError("The head of the array access at line " + arrayAccess.line + " is not an array");
		}

		// check if the index is an int
		SemType indexType = arrayAccess.getIndexExpression().accept(this, table);
		if (!indexType.equals(intType)) {
			throw new TypeError("The index of the array access at line " + arrayAccess.line + " is not an int");
		}

		arrayAccess.semtype = arraySemType.getElementSemType();

		// return the type of the array element that we are accessing
		return arraySemType.getElementSemType();
	}

	@Override
	public SemType visitIdentifierAccess(IdentifierAccess identifierAccess, SymbolTable table) throws Exception {
		// IdentifierAccess -> "identifier" AccessChain .
		// AccessChain -> Access AccessChain | .

		// check if the identifier is defined in the symbol table
		Symbol name = identifierAccess.getIdentifier();
		SemType semType = table.lookup(name.lexeme);

		if (semType == null) {
			// if the identifier is not found throw an error
			throw new ScopeError("Identifier " + name.lexeme + " referenced at line " + name.line + " not found in symbol table");
		}

		identifierAccess.semtype = semType;

		return semType;
	}

	@Override
	public SemType visitRecordAccess(RecordAccess recordAccess, SymbolTable table) throws Exception {
		// IdentifierAccess -> "identifier" AccessChain .
		// AccessChain -> Access AccessChain | .
		// Access -> "[" Expression "]" | "." "identifier" .

		// check if the identifier is defined in the symbol table
		SemType headType = recordAccess.getHeadAccess().accept(this, table);

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

			recordAccess.semtype = fieldType;
			// return type of accessed field
			return fieldType;
		} else {
			throw new TypeError("The head of the record access at line " + recordAccess.line + " is not a record");
		}

 	}

	@Override
	public SemType visitArrayExpression(ArrayExpression arrayExpression, SymbolTable table) throws Exception {
		// ArrayExpression -> "array" "[" "intval" "]" "of" Type ";" .

		SemType sizeExpressionSemType = arrayExpression.getSizeExpression().accept(this, table);
		// the size expression can be an int or a float
		if (!(sizeExpressionSemType.equals(intType) || sizeExpressionSemType.equals(floatType))) {
			throw new TypeError("Size expression of array creation at line " + arrayExpression.line + " is not an int or a float (variable or literal)");
		}

		SemType elemSemType = getSemTypeFromASTNodeType(table, arrayExpression.getType());
		SemType retType = new ArraySemType(elemSemType);

		arrayExpression.semtype = retType;
		return retType;
 	}

	@Override
	public SemType visitBinaryExpression(BinaryExpression binaryExpression, SymbolTable table) throws Exception {
		// check if the left and right expressions are of the same SemType

		SemType leftType = binaryExpression.getLeftTerm().accept(this, table);
		SemType rightType = binaryExpression.getRightTerm().accept(this, table);


		binaryExpression.getLeftTerm().semtype = leftType;
		binaryExpression.getRightTerm().semtype = rightType;

		if (!leftType.equals(rightType) && !(leftType.equals(voidType) || rightType.equals(voidType))) {
			// check if the types are convertible
			// if either of the types is a float, then we can convert the other type to a float
			if (leftType.equals(floatType) && rightType.equals(intType)) {
				rightType = floatType;
//				binaryExpression.getRightTerm().semtype = floatType;
				binaryExpression.getRightTerm().semtype.toConvert = true;

			} else if (rightType.equals(floatType) && leftType.equals(intType)) {
				leftType = floatType;
//				binaryExpression.getLeftTerm().semtype = floatType;
				binaryExpression.getLeftTerm().semtype.toConvert = true;

			} else {
				// if the types are not convertible, throw an error
				throw new OperatorError("Types of elements in the binary expression at line "+binaryExpression.line+" do not match: " + leftType + " and " + rightType);
			}
//			throw new TypeError("Types of elements in the binary expression at line "+binaryExpression.line+" do not match: " + leftType + " and " + rightType);
		}

		SemType termsType;
		if (leftType.equals(numType)) {
			// if the one of the types is a float, then we use that type as the termsType
			// if both are ints, then we use int
			if (leftType.equals(floatType) || rightType.equals(floatType)) {
				termsType = floatType;
			} else {
				termsType = intType; // -> leftType = intType
			}
		} else {
			// if the types are not numbers, then we use the type of the left term
			termsType = leftType;
		}

		SemType operatorSemType = binaryExpression.getOperator().accept(this, table);
		if (operatorSemType.equals(boolType)) {
			// this operator only accepts booleans -> "&&", "||"
			if (!termsType.equals(boolType)) {
				throw new OperatorError(String.format("Type %s cannot be used in a boolean expression, line %d", termsType, binaryExpression.line));
			}
			binaryExpression.semtype = boolType;
			return boolType;
		} else if (operatorSemType.equals(anyType)) {

			// here we have an operator that can accept both booleans, numbers, strings, records -> "==", "!="
			// so return a boolType
			binaryExpression.semtype = boolType;
			return boolType;

 		} else if (operatorSemType.equals(numType)) {
			// check if it is a string too
			if (!termsType.equals(numType) && !termsType.equals(stringType)) {
				throw new OperatorError(String.format("Type %s cannot be used in a with the %s operator, expected int, float or string", termsType, binaryExpression.getOperator().getSymbol().lexeme));
			}

			if (binaryExpression.getOperator().numberOperatorReturnsBoolean()) {
				binaryExpression.semtype = boolType;
				return boolType;
			}
			binaryExpression.semtype = termsType;
			return termsType;
		}

		binaryExpression.semtype = termsType;

		// shouldn't be here
		throw new SemanticException(String.format("Unexpected error in expression at line %s", binaryExpression.line));
	}

	@Override
	public SemType visitBinaryOperator(BinaryOperator binaryOperator, SymbolTable table) throws Exception {
		// return the type that the operator implies on the expression,
		// e.g., if the operator is + and the expression is int + int, then return int
		// if the operator is && then return a boolean
		// some boolean operators expect bools (&&, ||)
		// some other operators don't care (==, !=,...)
		if (binaryOperator.isBooleanOperator()) {
			binaryOperator.semtype = boolType;
			return boolType;
		} else if (binaryOperator.isAnyTypeOperator()) {
			binaryOperator.semtype = anyType;
			return anyType;
		} else if (binaryOperator.isNumberOperator()) {
			binaryOperator.semtype = numType;
			return numType; // numtype means that whatever number type is fine
		}
		return null;
	}

	@Override
	public SemType visitUnaryExpression(UnaryExpression unaryExpression, SymbolTable table) throws Exception {
		// return the type of the expression with the unary operator applied

		SemType expressionSemType = unaryExpression.getTerm().accept(this, table);

		SemType operatorSemType = unaryExpression.getOperator().accept(this, table);
		if (operatorSemType.equals(boolType)) {

			if (!expressionSemType.equals(boolType)) {
				throw new OperatorError(String.format("Expected boolean with operator %s", unaryExpression.getOperator().getSymbol().lexeme));
			}
			unaryExpression.semtype = boolType;
			return boolType;

		} else if (operatorSemType.equals(numType)) {

			if (!expressionSemType.equals(numType)) {
				throw new OperatorError(String.format("Expected int or float with operator %s", unaryExpression.getOperator().getSymbol().lexeme));
			}

			if (expressionSemType.equals(intType)) {
				unaryExpression.semtype = intType;
			} else if (expressionSemType.equals(floatType)) {
				unaryExpression.semtype = floatType;
			} else {
				unaryExpression.semtype = numType;
			}
			return unaryExpression.semtype;
		}

		// shouldn't be here
		throw new SemanticException(String.format("Unexpected error in expression at line %s", unaryExpression.line));
	}

	@Override
	public SemType visitUnaryOperator(UnaryOperator unaryOperator, SymbolTable table) throws Exception {
		if (unaryOperator.isBooleanOperator()) {
			unaryOperator.semtype = boolType;
			return boolType;
		} else if (unaryOperator.isNumberOperator()) {
			unaryOperator.semtype = numType;
			return numType;
		}

		throw new SemanticException(String.format("Unexpected unary operator %s", unaryOperator.getSymbol().lexeme));
	}

	@Override
	public SemType visitConstValue(ConstVal constVal, SymbolTable table) throws Exception {
		Object objVal = constVal.getValue();

		// create SemType from this constant value
		String type = switch (constVal.getSymbol().type) {
			case INT_LITERAL -> "int";
			case FLOAT_LITERAL -> "float";
			case STRING_LITERAL -> "string";
			case BOOL_TRUE, BOOL_FALSE -> "bool";
			default -> null;
		};
		SemType semType = new SemType(type, true);
//
//		if (semType.equals(numType) || semType.equals(boolType)) {
//			constVal.canBeStaticallyEval = true;
//
//		}

		constVal.semtype = semType;
		return semType;
	}

	@Override
	public SemType visitFunctionCall(FunctionCall functionCall, SymbolTable table) throws Exception {
		String functionIdentifierInGlobalTable = getFunctionName(functionCall, table);

		// get the FunctionSemType of the function from the symbol table
		FunctionSemType functionSemType = (FunctionSemType) table.lookup(functionIdentifierInGlobalTable);
		if (functionSemType == null) {
			throw new ScopeError("function "+functionIdentifierInGlobalTable+" doesn't exist");
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


			// if the args don't match  and the function is not defined with "any" as the type of its arguments
			if (!argSemType.equals(paramCallSemType) && !argSemType.equals(anyType)) {
				// the types didn't match, but if the expected type is a float and the given type is an int, we can convert it (here that means we keep going)

				// if we can't convert the type, we throw an error: here if it's not the convertable case, we throw
				if (argSemType.equals(floatType) && paramCallSemType.equals(intType)) {
					functionCall.semtype = functionSemType;
					return functionSemType.getRetType();
				}

				throw new ArgumentError("Type of the argument index " + paramCall.getParamIndex() + " in the function call '" + functionCall.getIdentifier().lexeme + "' at line "+ functionCall.line +" does not match the type of the argument " + argSemType);
			}
			paramCall.semtype = paramCallSemType;
		}

		// check that the number of arguments is correct
		if (functionSemType.getParamSemTypes().length != functionCall.getParameters().size()) {
			// if we have a function with anyType as the argument type, we don't care about the number of arguments
			if (functionSemType.getParamSemTypes()[0].equals(anyType)) {
				functionCall.semtype = functionSemType;
				return functionSemType.getRetType();
			}

			throw new ArgumentError("Number of parameters in the function call " + functionCall.getIdentifier().lexeme + " at line "+functionCall.line+" does not match the number of arguments in the function definition: " + functionSemType.getParamSemTypes().length);
		}

		// return the SemType of the return value (e.g., intType if the function returns an integer)
		functionCall.semtype = functionSemType;
		return functionSemType.getRetType();
	}

	private String getFunctionName(FunctionCall functionCall, SymbolTable localTable) throws Exception {
		String functionIdentifierInGlobalTable = functionCall.getIdentifier().lexeme;

		if (functionIdentifierInGlobalTable.startsWith("len") && !functionCall.getParameters().isEmpty()) {
			// the len function has two definitions in the table, one for strings and the other for arrays, to avoid conflicts
			// we renamed the len functions len_string and len_array resp.
			// we now need to figure out which one we need to use
			SemType firstSemType = functionCall.getParameters().getFirst().accept(this, localTable);
			if (firstSemType.equals(stringType)) {
				functionIdentifierInGlobalTable += "_string";
			} else {
				functionIdentifierInGlobalTable += "_array";
			}
		}
		return functionIdentifierInGlobalTable;
	}

	@Override
	public SemType visitRecordInstantiation(NewRecord newRecord, SymbolTable table) throws Exception {
		// NewRecord means a record instance creation
		// example: p Product = Product(1, "Phone", 699);
		//                      ^^^^^^^^^^^^^^^^^^^^^^^^

		// check that the record type exists
		RecordSemType recordSemType = (RecordSemType) table.lookup(newRecord.getIdentifier().lexeme);
		if (recordSemType == null) {
			throw new ScopeError("Record type "+newRecord.getIdentifier().lexeme+" doesn't exist");
		}

		if (newRecord.getTerms().size() != recordSemType.fields.size()) {
			throw new ArgumentError("Number of parameters in the record " + newRecord.getIdentifier().lexeme + " does not match the number of fields " + recordSemType.fields.size());
		}

		// check that the types of the record fields are correct and correspond to the definition
		for (ParamCall paramCall : newRecord.getTerms()) {

			// get the SemType
			SemType paramCallSemType = paramCall.accept(this, table);

			// get the fieldSemType
			SemType fieldSemType = recordSemType.fields.values().toArray(new SemType[0])[paramCall.getParamIndex()];
			if (fieldSemType == null) {
				throw new SemanticException("Field " + paramCall.getParamIndex() + " does not exist in record type " + newRecord.getIdentifier().lexeme);
			}

			// check that the types match
			if (!paramCallSemType.equals(fieldSemType)) {
				// the types didn't match, but if the expected type is a float and the given type is an int, we can convert it (here that means we keep going)

				// otherwise: throw
				if (!(fieldSemType.equals(floatType) && paramCallSemType.equals(intType))) {
					throw new ArgumentError("Type of the parameter index " + paramCall.getParamIndex() + " in the record " + newRecord.getIdentifier().lexeme + " at line "+newRecord.line+" does not match the type of the field " + fieldSemType);
				}
			}
			paramCall.semtype = paramCallSemType;
		}

		newRecord.semtype = recordSemType;
		return recordSemType;
	}

	@Override
	public SemType visitParamCall(ParamCall paramCall, SymbolTable table) throws Exception {
		// ParamCall is one parameter assignment when creating a new record or when calling a function
		// example: p Product = Product(1, "Phone", 699);
		//                              ^  ^^^^^^^  ^^^
		//                              -> Each of these is a ParamCall

		// we just return the type of the expression
		SemType paramType = paramCall.getParamExpression().accept(this, table);
		paramCall.semtype = paramType;
		return paramType;
	}

	@Override
	public SemType visitParenthesesTerm(ParenthesesTerm parenthesesTerm, SymbolTable table) throws Exception {
		// return the SemType of the expression that is inside the parentheses
		SemType parenthesesSemType = parenthesesTerm.getExpression().accept(this, table);
		parenthesesTerm.semtype = parenthesesSemType;
		return parenthesesSemType;
	}

	@Override
	public SemType visitType(Type type, SymbolTable table) throws Exception {
		if (type.isList) {
			ArraySemType typeSemType = new ArraySemType(new SemType(type.symbol.lexeme));
			type.semtype = typeSemType;
			return typeSemType;
		}
		SemType typeSemType = new SemType(type.symbol.lexeme);
		type.semtype = typeSemType;
		return typeSemType;
	}

	@Override
	public SemType visitNumType(NumType numType, SymbolTable table) throws Exception {
		if (numType.isList) {
			SemType arraySemType = new ArraySemType(new SemType(numType.symbol.lexeme));
			numType.semtype = arraySemType;
			return arraySemType;
		}

		SemType semType = new SemType(numType.symbol.lexeme);
		numType.semtype = semType;
		return semType;
	}

	@Override
	public SemType visitStatement(Statement statement, SymbolTable table) throws Exception {
		throw new SemanticException("this should never be called");
	}

	@Override
	public SemType visitForLoop(ForLoop forLoop, SymbolTable table) throws Exception {
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
            throw new ScopeError("Identifier '" + varSymbol.lexeme + "' in for loop at line "+ varSymbol.line +" was not found in symbol table");
        }

		forLoop.semtype = varType;

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
		forLoop.startType = typeCheckLoopFields(table,loopVarIsInt, start, "start");;


		Symbol step = forLoop.getStep();
		forLoop.stepType = typeCheckLoopFields(table,loopVarIsInt, step, "step");

		// check that the increment is not 0 (we only check if it is a literal, otherwise we can't check atp)
		if (step.type == INT_LITERAL && step.value == (Integer) 0) {
			throw new SemanticException("The step of the for loop at line " + step.line + " is 0, this loop will never make progress");
		}

		if (step.type == TokenTypes.FLOAT_LITERAL && step.value == (Float) 0.0f) {
			throw new SemanticException("The step of the for loop at line " + step.line + " is 0.0 (float), this loop will never make progress");
		}

		Symbol end = forLoop.getEnd();
		forLoop.endType = typeCheckLoopFields(table,loopVarIsInt, end, "end");

		// then visit the block
		forLoop.getBlock().accept(this, table);

		return null;
	}

	private SemType typeCheckLoopFields(SymbolTable table, boolean loopVarIsInt, Symbol fieldSymbol, String fieldName) throws TypeError {
		if (fieldSymbol.type == INT_LITERAL || fieldSymbol.type == TokenTypes.FLOAT_LITERAL) {
			String typeName = fieldSymbol.type == INT_LITERAL ? "int" : "float";
			// if the loop var is an int and the field (start, step, stop) number is a float, we throw an error
			if (loopVarIsInt && fieldSymbol.type == TokenTypes.FLOAT_LITERAL) {
				throw new TypeError("The "+fieldName+" number in the for loop at line" + fieldSymbol.line + ": '"+ fieldSymbol.lexeme +"' is not an integer but the loop variable is.");
			}
			// otherwise we're good
			return new SemType(typeName);

		} else if (fieldSymbol.type == TokenTypes.IDENTIFIER) {
        	SemType fieldType = table.lookup(fieldSymbol.lexeme);

			// if the loop var is an int and field var is a float, then we throw an error
			if (loopVarIsInt && fieldType.type.equals("float")) {
				throw new TypeError("The "+fieldName+" variable in the for loop at line " + fieldSymbol.line + ": '"+ fieldSymbol.lexeme +"' is not an integer but the loop variable is.");
			}

			// otherwise we're good (I think)
			return fieldType;
		} else {
			throw new TypeError("Type of the '"+fieldName+"' field in the for loop at line " + fieldSymbol.line + " is not valid, it should either be a variable identifier, int, or float");
		}
	}

	@Override
	public SemType visitWhileLoop(WhileLoop whileLoop, SymbolTable table) throws Exception {
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
	public SemType visitFreeStatement(FreeStatement freeStatement, SymbolTable table) throws Exception {
		// check if the identifier is defined in the symbol table
		IdentifierAccess identifierAccess = freeStatement.getIdentifierAccess();
		Symbol name = identifierAccess.getIdentifier();
		SemType semType = table.lookup(name.lexeme);
		if (semType == null) {
			// if the identifier is not found, throw an error
			throw new ScopeError("Identifier " + name.lexeme + " not found in symbol table");
		}

		// remove from table
		table.removeSymbol(name.lexeme);

		return null;
	}

	@Override
	public SemType visitFunctionDefinition(FunctionDefinition functionDefinition, SymbolTable table) throws Exception {
		Symbol name = functionDefinition.getName();

		// create local table
		SymbolTable localTable = new SymbolTable(table, name.lexeme);

		// add parameters to the local symbol table
		ArrayList<SemType> paramTypes = new ArrayList<>();
		for (ParamDefinition param : functionDefinition.getParamDefinitions()){
			SemType paramSemType = param.accept(this, localTable);

			paramTypes.add(paramSemType);
		}

		functionDefinition.setParamTypes(paramTypes);
		// add function to the symbol table
		Type returnType = functionDefinition.getReturnType();

		SemType retSemType = getSemTypeFromASTNodeType(table, returnType);
		functionDefinition.setRetSemType(retSemType);

		FunctionSemType semType = new FunctionSemType(retSemType, paramTypes.toArray(new SemType[0]));
		table.addSymbol(name.lexeme, semType);

		// check types of block
		functionDefinition.getBlock().accept(this, localTable);

		
        // check that all paths in the function return a value (if the function isn't void)
        if (!retSemType.equals(voidType)) {
            if (!hasReturnInAllPaths(functionDefinition.getBlock())) {
                throw new ReturnError("Function '" + name.lexeme + "' has paths that don't return a value");
            }
        }
		
		functionDefinition.semtype = semType;
		return retSemType;
	}
	
	private SemType getSemTypeFromASTNodeType(SymbolTable table, Type type) {
		SemType retSemType;
		if (type.isList) {
			SemType elemSemType;
			if (type.symbol.type == RECORD) {
				elemSemType = table.lookup(type.symbol.lexeme);
			} else {
				elemSemType = new SemType(type.symbol.lexeme);
			}

			retSemType = new ArraySemType(elemSemType);
		} else if (type.symbol.type == RECORD) {
			retSemType = table.lookup(type.symbol.lexeme);
		} else {
			retSemType = new SemType(type.symbol.lexeme);
		}
		return retSemType;
	}

	private boolean hasReturnInAllPaths(Block block) {
        // return at end of (main) block 
        if (block.getReturnStatement() != null) {
            return true;
        }
        
        // check all statements for returns
        return checkStatementsForReturn(block.getStatements());
    }
    
    private boolean checkStatementsForReturn(ArrayList<Statement> statements) {
        for (Statement stmt : statements) {
            // direct return
            if (stmt instanceof ReturnStatement) {
                return true;
            }

            if (stmt instanceof IfStatement ifStmt) {
                // check that both branches return
                boolean thenReturns = hasReturnInAllPaths(ifStmt.getThenBlock());
                
                // check the else block if there is one
                if (ifStmt.isElse()) {
                    boolean elseReturns = hasReturnInAllPaths(ifStmt.getElseBlock());
                    // it only counts as a return if both branches return
                    if (thenReturns && elseReturns) {
                        return true;
                    }
                }
            }
        }
        
        // no return found in this path
        return false;
    }    
	
	@Override
	public SemType visitBlock(Block block, SymbolTable table) throws Exception {
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
	public SemType visitIfStatement(IfStatement ifStatement, SymbolTable table) throws Exception {
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
	public SemType visitParamDefinition(ParamDefinition paramDefinition, SymbolTable table) throws Exception {

		// add param to local symbol table
		Symbol name = paramDefinition.getIdentifier();
		Type type = paramDefinition.getType();

		SemType semType = getSemTypeFromASTNodeType(table, type);

		table.addSymbol(name.lexeme, semType);

		paramDefinition.semtype = semType;
		return semType;
	}

	@Override
	public SemType visitRecordDefinition(RecordDefinition recordDefinition, SymbolTable table) throws Exception {
		LinkedHashMap<String, SemType> fields = new LinkedHashMap<>();

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

		switch (recordDefinition.getIdentifier().lexeme) {
			case "int", "float", "string", "bool", "num", "any", "while", "for", "main" -> {
				throw new RecordError("Record " + recordDefinition.getIdentifier().lexeme + " cannot be defined, it is a reserved keyword");
			}
		}

		table.addSymbol(recordDefinition.getIdentifier().lexeme, recordSemType);

		recordDefinition.semtype = recordSemType;
		return recordSemType;
	}

	@Override
	public SemType visitRecordFieldDefinition(RecordFieldDefinition recordFieldDefinition, SymbolTable table) throws Exception {
		Type type = recordFieldDefinition.getType();

		SemType retType = getSemTypeFromASTNodeType(table, type);

		recordFieldDefinition.semtype = retType;
		return retType;
	}

	@Override
	public SemType visitReturnStatement(ReturnStatement returnStatement, SymbolTable table) throws Exception{
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

		if (!(functionSemType.getRetType().equals(returnSemType))) {
			throw new ReturnError("Return at line "+returnStatement.line+" type does not match function return type. Expected: " + functionSemType.getRetType() + ", found: " + returnSemType);
		}

		returnStatement.semtype = returnSemType;
		return returnSemType;
	}

	@Override
	public SemType visitVariableAssignment(VariableAssignment variableAssignment, SymbolTable table) throws Exception {
		// VariableAssignment -> IdentifierAccess "=" Expression .
		// IdentifierAccess -> "identifier" AccessChain .
		// AccessChain -> Access AccessChain | .
		// Access -> "[" Expression "]" | "." "identifier" .

		// first get the type of the variable
		SemType varType = variableAssignment.getAccess().accept(this, table);
		variableAssignment.getAccess().willStore = true;

		// then get the type of the expression
		SemType expressionType = variableAssignment.getExpression().accept(this, table);

		// and check that they match
		if (!varType.equals(expressionType)) {

			// the types didn't match, but if the expected type is a float and the given type is an int, we can convert it (here that means we keep going)
			// if we can't convert the type, we throw an error: here if it's not the convertable case, we throw
			if (varType.equals(floatType) && expressionType.equals(intType)) {
				varType.toConvert = true;
				variableAssignment.semtype = varType;
				return null;
			}

			throw new TypeError("Type of the variable '" + variableAssignment.getAccess().semtype + "' at line " + variableAssignment.line + " does not match the type of the expression '" + variableAssignment.getExpression().semtype + "'");
		}

		// then check that the variable is not a constant
		if (varType.isConstant) {
			throw new TypeError("Cannot assign to a constant variable " + variableAssignment.getAccess().toString() + " at line " + variableAssignment.line);
		}

		variableAssignment.semtype = varType;
		return null;
	}

	@Override
	public SemType visitVariableDeclaration(VariableDeclaration variableDeclaration, SymbolTable table) throws Exception {
		Symbol name = variableDeclaration.getName();
		Type type = variableDeclaration.getType();
		if (table.lookupSameScope(name.lexeme) != null) {
			throw new ScopeError("Variable " + name.lexeme + " already exists in the same scope");
		}

		SemType semType;
		if (!variableDeclaration.hasValue()) {
			// if the variable is declared as a prototype
//			semType = new SemType(type.symbol.lexeme, variableDeclaration.isConstant());
			semType = getSemTypeFromASTNodeType(table, variableDeclaration.getType());
			semType.setIsConstant(variableDeclaration.isConstant());
			semType.setGlobal(variableDeclaration.isGlobal());
		} else {
			// a float = 3 + 7.0;
			// or c float = 5;
			semType = variableDeclaration.getValue().accept(this, table);
			semType.setIsConstant(variableDeclaration.isConstant());
			semType.setGlobal(variableDeclaration.isGlobal());

			SemType declType = getSemTypeFromASTNodeType(table, type);
			if (!declType.equals(semType)) {

				if (declType.equals(floatType) && semType.equals(intType)) {
//					variableDeclaration.conversionNeeded = true;
					semType.toConvert = true;
				}

				throw new TypeError("Type of the variable " + name.lexeme + " ("+ declType +") at line " + variableDeclaration.line + " does not match the type of the expression " + semType);
			}
		}

		table.addSymbol(name.lexeme, semType);


		variableDeclaration.semtype = semType;
		return semType;
	}

}

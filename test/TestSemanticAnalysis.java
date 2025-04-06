import static org.junit.Assert.*;

import compiler.Lexer.*;
import compiler.Parser.*;
import compiler.Parser.ASTNodes.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Operators.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.*;
import compiler.Parser.ASTNodes.Types.Type;
import compiler.SemanticAnalysis.*;
import compiler.SemanticAnalysis.Errors.*;
import compiler.SemanticAnalysis.Types.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class TestSemanticAnalysis {
    @Test
    public void testArrayAccess() {
        SymbolTable symbolTable = new SymbolTable(null);
        SemType arrayType = new ArraySemType(new SemType("int"));

        // Set up the symbol table
        symbolTable.addSymbol("myArray", arrayType);

        // myArray[0]
        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "0", 0, 0, 0);
        ConstVal constVal = new ConstVal(0, intSymbol, 0, 0);
        Symbol arraySymbol = new Symbol(TokenTypes.IDENTIFIER, "myArray", 0, 0, 0);
        IdentifierAccess identifierAccess = new IdentifierAccess(arraySymbol, 0, 0);
        ArrayAccess arrayAccess = new ArrayAccess(identifierAccess, constVal, 0, 0);


        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitArrayAccess(arrayAccess, symbolTable);
            assertEquals("Expected type to be array of ints", arrayType, resultType);
        } catch (SemanticException e) {
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }


    @Test
    public void testIdentifierAccess() {
        SymbolTable symbolTable = new SymbolTable(null);
        SemType intType = new SemType("int");

        // Set up the symbol table
        symbolTable.addSymbol("myVar", intType);

        // myVar
        Symbol identifierSymbol = new Symbol(TokenTypes.IDENTIFIER, "myVar", 0, 0);
        IdentifierAccess identifierAccess = new IdentifierAccess(identifierSymbol, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitIdentifierAccess(identifierAccess, symbolTable);
            assertEquals("Expected type to be int", intType, resultType);
        } catch (SemanticException e) {
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testRecordAccess() {
        SymbolTable symbolTable = new SymbolTable(null);
        TreeMap<String, SemType> recordFields = new TreeMap<>();
        recordFields.put("field1", new SemType("int"));
        recordFields.put("field2", new SemType("string"));
        SemType recordType = new RecordSemType(recordFields, "MyRecord");

        // Set up the symbol table: put an instance of the record in the symbol table
        symbolTable.addSymbol("myRecord", recordType);

        // myRecord.field1
        Symbol identifierSymbol = new Symbol(TokenTypes.IDENTIFIER, "myRecord", 0, 0);
        IdentifierAccess identifierAccess = new IdentifierAccess(identifierSymbol, 0, 0);
        Symbol fieldSymbol = new Symbol(TokenTypes.IDENTIFIER, "field1", 0, 0);
        RecordAccess recordAccess = new RecordAccess(identifierAccess, fieldSymbol, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitRecordAccess(recordAccess, symbolTable);
            assertEquals("Expected type to be int", new SemType("int"), resultType);
        } catch (SemanticException e) {
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testArrayExpression() {
        SemType elementType = new SemType("int");
        ArraySemType arrayType = new ArraySemType(elementType, 5);

        // array[5] of int
        Expression sizeExpression = new ConstVal(5, new Symbol(TokenTypes.INT_LITERAL, "5", 0, 0), 0, 0);
        Type type = new Type(new Symbol(TokenTypes.INT, "int", 0, 0), 0, 0);
        ArrayExpression arrayExpression = new ArrayExpression(sizeExpression, type, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitArrayExpression(arrayExpression, null);
            assertEquals("Expected type to be array of ints", arrayType, resultType);
        } catch (SemanticException e) {
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testArrayExpressionWithVar() {
        SemType elementType = new SemType("int");
        ArraySemType arrayType = new ArraySemType(elementType, 5);
        SymbolTable symbolTable = new SymbolTable(null);
        // Set up the symbol table
        symbolTable.addSymbol("myIntVar", new SemType("int"));

        // array[myIntVar*5] of int

        IdentifierAccess identifierAccess = new IdentifierAccess(new Symbol(TokenTypes.IDENTIFIER, "myIntVar", 0, 0), 0, 0);
        BinaryOperator binaryOperator = new BinaryOperator(new Symbol(TokenTypes.MULTIPLY, "*", 0, 0), 0, 0);
        ConstVal constVal = new ConstVal(5, new Symbol(TokenTypes.INT_LITERAL, "5", 0, 0), 0, 0);
        Expression sizeExpression = new BinaryExpression(identifierAccess, binaryOperator, constVal, 0, 0);
        Type type = new Type(new Symbol(TokenTypes.INT, "int", 0, 0), 0, 0);
        ArrayExpression arrayExpression = new ArrayExpression(sizeExpression, type, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitArrayExpression(arrayExpression, symbolTable);
            assertEquals("Expected type to be array of ints", arrayType, resultType);
        } catch (SemanticException e) {
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testBinaryExpression() {
        // 10 + 9
        Symbol intSymbol1 = new Symbol(TokenTypes.INT_LITERAL, "10", 0, 0, 10);
        Symbol intSymbol2 = new Symbol(TokenTypes.INT_LITERAL, "9", 0, 0, 9);
        ConstVal constVal1 = new ConstVal(10, intSymbol1, 0, 0);
        ConstVal constVal2 = new ConstVal(9, intSymbol2, 0, 0);
        BinaryOperator binaryOperator = new BinaryOperator(new Symbol(TokenTypes.PLUS, "+", 0, 0), 0, 0);
        BinaryExpression binaryExpression = new BinaryExpression(constVal1, binaryOperator, constVal2, 0, 0);
        SemType expectedType = new SemType("int");
        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitBinaryExpression(binaryExpression, null);
            assertEquals("Expected type to be int", expectedType, resultType);
        } catch (SemanticException e) {
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testBinaryExpressionWithVar() {
        // a + b
        SymbolTable symbolTable = new SymbolTable(null);
        SemType intType = new SemType("int");
        // Set up the symbol table
        symbolTable.addSymbol("a", intType);
        symbolTable.addSymbol("b", intType);

        IdentifierAccess identifierAccess1 = new IdentifierAccess(new Symbol(TokenTypes.IDENTIFIER, "a", 0, 0), 0, 0);
        IdentifierAccess identifierAccess2 = new IdentifierAccess(new Symbol(TokenTypes.IDENTIFIER, "b", 0, 0), 0, 0);
        BinaryOperator binaryOperator = new BinaryOperator(new Symbol(TokenTypes.PLUS, "+", 0, 0), 0, 0);
        BinaryExpression binaryExpression = new BinaryExpression(identifierAccess1, binaryOperator, identifierAccess2, 0, 0);
        SemType expectedType = new SemType("int");

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitBinaryExpression(binaryExpression, symbolTable);
            assertEquals("Expected type to be int", expectedType, resultType);
        } catch (SemanticException e) {
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testBinaryExpressionBooleans() {
        // a && false
        SymbolTable symbolTable = new SymbolTable(null);
        SemType boolType = new SemType("bool");
        // Set up the symbol table
        symbolTable.addSymbol("a", boolType);
        Symbol boolSymbol = new Symbol(TokenTypes.BOOL_FALSE, "false", 0, 0);
        ConstVal constVal = new ConstVal(false, boolSymbol, 0, 0);
        IdentifierAccess identifierAccess = new IdentifierAccess(new Symbol(TokenTypes.IDENTIFIER, "a", 0, 0), 0, 0);
        BinaryOperator binaryOperator = new BinaryOperator(new Symbol(TokenTypes.AND, "&&", 0, 0), 0, 0);
        BinaryExpression binaryExpression = new BinaryExpression(identifierAccess, binaryOperator, constVal, 0, 0);
        SemType expectedType = new SemType("bool");

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitBinaryExpression(binaryExpression, symbolTable);
            assertEquals("Expected type to be bool", expectedType, resultType);
        } catch (SemanticException e) {
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testBinaryExpressionIntAndFloat() {
        // 4 + 10.5
        // ^-> will be soft converted to float, result will be a float
        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "4", 0, 0, 4);
        Symbol floatSymbol = new Symbol(TokenTypes.FLOAT_LITERAL, "10.5", 0, 0, 10.5f);
        ConstVal constVal1 = new ConstVal(4, intSymbol, 0, 0);
        ConstVal constVal2 = new ConstVal(10.5f, floatSymbol, 0, 0);
        BinaryOperator binaryOperator = new BinaryOperator(new Symbol(TokenTypes.PLUS, "+", 0, 0), 0, 0);
        BinaryExpression binaryExpression = new BinaryExpression(constVal1, binaryOperator, constVal2, 0, 0);
        SemType expectedType = new SemType("float");

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitBinaryExpression(binaryExpression, null);
            assertEquals("Expected type to be float", expectedType, resultType);
        } catch (SemanticException e) {
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testBinaryExpressionFloatAndInt() {
        // 10.5 + 4
        //        ^-> will be soft converted to float, result will be a float
        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "4", 0, 0, 4);
        Symbol floatSymbol = new Symbol(TokenTypes.FLOAT_LITERAL, "10.5", 0, 0, 10.5f);
        ConstVal constVal1 = new ConstVal(4, intSymbol, 0, 0);
        ConstVal constVal2 = new ConstVal(10.5f, floatSymbol, 0, 0);
        BinaryOperator binaryOperator = new BinaryOperator(new Symbol(TokenTypes.PLUS, "+", 0, 0), 0, 0);
        BinaryExpression binaryExpression = new BinaryExpression(constVal2, binaryOperator, constVal1, 0, 0); // SWAPPED THE ORDER
        SemType expectedType = new SemType("float");

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitBinaryExpression(binaryExpression, null);
            assertEquals("Expected type to be float", expectedType, resultType);
        } catch (SemanticException e) {
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testBinaryExpressionFloatAndIntVar() {
        // 4.5 * a

        SymbolTable symbolTable = new SymbolTable(null);
        SemType floatType = new SemType("float");
        SemType intType = new SemType("int");
        // Set up the symbol table
        symbolTable.addSymbol("a", intType);

        Symbol floatSymbol = new Symbol(TokenTypes.FLOAT_LITERAL, "4.5", 0, 0, 4.5f);
        ConstVal constVal1 = new ConstVal(4.5f, floatSymbol, 0, 0);
        IdentifierAccess identifierAccess = new IdentifierAccess(new Symbol(TokenTypes.IDENTIFIER, "a", 0, 0), 0, 0);
        BinaryOperator binaryOperator = new BinaryOperator(new Symbol(TokenTypes.DIVIDE, "/", 0, 0), 0, 0);
        BinaryExpression binaryExpression = new BinaryExpression(constVal1, binaryOperator, identifierAccess, 0, 0);
        SemType expectedType = new SemType("float");

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitBinaryExpression(binaryExpression, symbolTable);
            assertEquals("Expected type to be float", expectedType, resultType);
        } catch (SemanticException e) {
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testBinaryExpressionRecordAccessLessThan() {
        // myRecord.field1 < 10
        SymbolTable symbolTable = new SymbolTable(null);
        TreeMap<String, SemType> recordFields = new TreeMap<>();
        recordFields.put("field1", new SemType("int"));
        SemType recordType = new RecordSemType(recordFields, "MyRecord");

        // Set up the symbol table
        symbolTable.addSymbol("myRecord", recordType);

        Symbol identifierSymbol = new Symbol(TokenTypes.IDENTIFIER, "myRecord", 0, 0);
        IdentifierAccess identifierAccess = new IdentifierAccess(identifierSymbol, 0, 0);
        Symbol fieldSymbol = new Symbol(TokenTypes.IDENTIFIER, "field1", 0, 0);
        RecordAccess recordAccess = new RecordAccess(identifierAccess, fieldSymbol, 0, 0);

        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "10", 0, 0, 10);
        ConstVal constVal = new ConstVal(10, intSymbol, 0, 0);
        BinaryOperator binaryOperator = new BinaryOperator(new Symbol(TokenTypes.LESS_THAN, "<", 0, 0), 0, 0);
        BinaryExpression binaryExpression = new BinaryExpression(recordAccess, binaryOperator, constVal, 0, 0);
        SemType expectedType = new SemType("bool");

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitBinaryExpression(binaryExpression, symbolTable);
            assertEquals("Expected type to be bool", expectedType, resultType);
        } catch (SemanticException e) {
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testUnaryExpression() {
        // -10
        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "10", 0, 0, 10);
        ConstVal constVal = new ConstVal(10, intSymbol, 0, 0);
        UnaryOperator unaryOperator = new UnaryOperator(new Symbol(TokenTypes.MINUS, "-", 0, 0), 0, 0);
        UnaryExpression unaryExpression = new UnaryExpression(unaryOperator, constVal, 0, 0);
        SemType expectedType = new SemType("int");

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitUnaryExpression(unaryExpression, null);
            assertEquals("Expected type to be int", expectedType, resultType);
        } catch (SemanticException e) {
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testUnaryExpressionNegationBoolean() {
        // !true
        Symbol boolSymbol = new Symbol(TokenTypes.BOOL_TRUE, "true", 0, 0);
        ConstVal constVal = new ConstVal(true, boolSymbol, 0, 0);
        UnaryOperator unaryOperator = new UnaryOperator(new Symbol(TokenTypes.NOT, "!", 0, 0), 0, 0);
        UnaryExpression unaryExpression = new UnaryExpression(unaryOperator, constVal, 0, 0);
        SemType expectedType = new SemType("bool");

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitUnaryExpression(unaryExpression, null);
            assertEquals("Expected type to be bool", expectedType, resultType);
        } catch (SemanticException e) {
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testUnaryExpressionNegationBooleanVar() {
        // !boolVar
        SymbolTable symbolTable = new SymbolTable(null);
        SemType boolType = new SemType("bool");

        // Set up the symbol table
        symbolTable.addSymbol("boolVar", boolType);

        UnaryOperator unaryOperator = new UnaryOperator(new Symbol(TokenTypes.NOT, "!", 0, 0), 0, 0);
        IdentifierAccess identifierAccess = new IdentifierAccess(new Symbol(TokenTypes.IDENTIFIER, "boolVar", 0, 0), 0, 0);
        UnaryExpression unaryExpression = new UnaryExpression(unaryOperator, identifierAccess, 0, 0);
        SemType expectedType = new SemType("bool");

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitUnaryExpression(unaryExpression, symbolTable);
            assertEquals("Expected type to be bool", expectedType, resultType);
        } catch (SemanticException e) {
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testConstValues() {
        // 10 -> int
        // 10.5 -> float
        // "hello" -> string
        // true -> bool
        // false -> bool
        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "10", 0, 0, 10);
        Symbol floatSymbol = new Symbol(TokenTypes.FLOAT_LITERAL, "10.5", 0, 0, 10.5f);
        Symbol stringSymbol = new Symbol(TokenTypes.STRING_LITERAL, "\"hello\"", 0, 0, "hello");
        Symbol boolTrueSymbol = new Symbol(TokenTypes.BOOL_TRUE, "true", 0, 0);
        Symbol boolFalseSymbol = new Symbol(TokenTypes.BOOL_FALSE, "false", 0, 0);

        ConstVal intConst = new ConstVal(10, intSymbol, 0, 0);
        ConstVal floatConst = new ConstVal(10.5f, floatSymbol, 0, 0);
        ConstVal stringConst = new ConstVal("hello", stringSymbol, 0, 0);
        ConstVal boolTrueConst = new ConstVal(true, boolTrueSymbol, 0, 0);
        ConstVal boolFalseConst = new ConstVal(false, boolFalseSymbol, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType intType = semanticAnalysis.visitConstValue(intConst, null);
            SemType floatType = semanticAnalysis.visitConstValue(floatConst, null);
            SemType stringType = semanticAnalysis.visitConstValue(stringConst, null);
            SemType boolTrueType = semanticAnalysis.visitConstValue(boolTrueConst, null);
            SemType boolFalseType = semanticAnalysis.visitConstValue(boolFalseConst, null);

            assertEquals("Expected type to be int", new SemType("int"), intType);
            assertEquals("Expected type to be float", new SemType("float"), floatType);
            assertEquals("Expected type to be string", new SemType("string"), stringType);
            assertEquals("Expected type to be bool", new SemType("bool"), boolTrueType);
            assertEquals("Expected type to be bool", new SemType("bool"), boolFalseType);
        } catch (SemanticException e) {
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testFunctionCall() {
        SymbolTable symbolTable = new SymbolTable(null);
        SemType functionReturnType = new SemType("int");
        SemType paramType = new SemType("int");
        SemType[] paramTypes = {paramType};
        FunctionSemType functionSemType = new FunctionSemType(functionReturnType, paramTypes);

        // Set up the symbol table
        symbolTable.addSymbol("myFunction", functionSemType);

        // myFunction(5)
        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "5", 0, 0, 5);
        ConstVal constVal = new ConstVal(5, intSymbol, 0, 0);

        Symbol functionSymbol = new Symbol(TokenTypes.IDENTIFIER, "myFunction", 0, 0);
        ArrayList<ParamCall> paramCalls = new ArrayList<>();
        paramCalls.add(new ParamCall(constVal, 0, 0, 0));
        FunctionCall functionCall = new FunctionCall(functionSymbol, paramCalls, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitFunctionCall(functionCall, symbolTable);
            assertEquals("Expected type to be int", functionReturnType, resultType);
        } catch (SemanticException e) {
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testVoidFunctionCall() {
        SymbolTable symbolTable = new SymbolTable(null);
        SemType functionReturnType = new SemType("void");
        SemType paramType = new SemType("int");
        SemType[] paramTypes = {paramType};
        FunctionSemType functionSemType = new FunctionSemType(functionReturnType, paramTypes);

        // Set up the symbol table
        symbolTable.addSymbol("myVoidFunction", functionSemType);

        // myVoidFunction(5) -> void
        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "5", 0, 0, 5);
        ConstVal constVal = new ConstVal(5, intSymbol, 0, 0);

        Symbol functionSymbol = new Symbol(TokenTypes.IDENTIFIER, "myVoidFunction", 0, 0);
        ArrayList<ParamCall> paramCalls = new ArrayList<>();
        paramCalls.add(new ParamCall(constVal, 0, 0, 0));
        FunctionCall functionCall = new FunctionCall(functionSymbol, paramCalls, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitFunctionCall(functionCall, symbolTable);
            assertEquals("Expected type to be void", functionReturnType, resultType);
        } catch (SemanticException e) {
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testFunctionCallWithAnyArgs() {
        // the write function is defined as follows: write(any) -> void
        SymbolTable symbolTable = new SymbolTable(null);
        SemType functionReturnType = new SemType("void");
        SemType paramType = new SemType("any");
        SemType[] paramTypes = {paramType};
        FunctionSemType functionSemType = new FunctionSemType(functionReturnType, paramTypes);
        // Set up the symbol table
        symbolTable.addSymbol("write", functionSemType);

        // write(5) -> void
        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "5", 0, 0, 5);
        ConstVal constVal = new ConstVal(5, intSymbol, 0, 0);
        Symbol functionSymbol = new Symbol(TokenTypes.IDENTIFIER, "write", 0, 0);
        ArrayList<ParamCall> paramCalls = new ArrayList<>();
        paramCalls.add(new ParamCall(constVal, 0, 0, 0));
        FunctionCall functionCall = new FunctionCall(functionSymbol, paramCalls, 0, 0);

        // write("string") -> void
        Symbol stringSymbol = new Symbol(TokenTypes.STRING_LITERAL, "\"string\"", 0, 0, "string");
        ConstVal constVal2 = new ConstVal("string", stringSymbol, 0, 0);
        ArrayList<ParamCall> paramCalls2 = new ArrayList<>();
        paramCalls2.add(new ParamCall(constVal2, 0, 0, 0));
        FunctionCall functionCall2 = new FunctionCall(functionSymbol, paramCalls2, 0, 0);

        // write(true) -> void
        Symbol boolSymbol = new Symbol(TokenTypes.BOOL_TRUE, "true", 0, 0);
        ConstVal constVal3 = new ConstVal(true, boolSymbol, 0, 0);
        ArrayList<ParamCall> paramCalls3 = new ArrayList<>();
        paramCalls3.add(new ParamCall(constVal3, 0, 0, 0));
        FunctionCall functionCall3 = new FunctionCall(functionSymbol, paramCalls3, 0, 0);

        // write(5.0) -> void
        Symbol floatSymbol = new Symbol(TokenTypes.FLOAT_LITERAL, "5.0", 0, 0, 5.0f);
        ConstVal constVal4 = new ConstVal(5.0f, floatSymbol, 0, 0);
        ArrayList<ParamCall> paramCalls4 = new ArrayList<>();
        paramCalls4.add(new ParamCall(constVal4, 0, 0, 0));
        FunctionCall functionCall4 = new FunctionCall(functionSymbol, paramCalls4, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitFunctionCall(functionCall, symbolTable);
            assertEquals("Expected type to be void", functionReturnType, resultType);

            SemType resultType2 = semanticAnalysis.visitFunctionCall(functionCall2, symbolTable);
            assertEquals("Expected type to be void", functionReturnType, resultType2);

            SemType resultType3 = semanticAnalysis.visitFunctionCall(functionCall3, symbolTable);
            assertEquals("Expected type to be void", functionReturnType, resultType3);

            SemType resultType4 = semanticAnalysis.visitFunctionCall(functionCall4, symbolTable);
            assertEquals("Expected type to be void", functionReturnType, resultType4);
        } catch (SemanticException e) {
            fail("Semantic analysis failed: " + e.getMessage());
        }


    }

}

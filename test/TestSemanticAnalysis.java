import static org.junit.Assert.*;

import compiler.Lexer.*;
import compiler.Parser.ASTNodes.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Operators.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.*;
import compiler.Parser.ASTNodes.Statements.Statements.*;
import compiler.Parser.ASTNodes.Types.Type;
import compiler.Parser.Parser;
import compiler.SemanticAnalysis.*;
import compiler.SemanticAnalysis.Errors.*;
import compiler.SemanticAnalysis.Types.*;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class TestSemanticAnalysis {
    @Test
    public void testArrayAccess() throws Exception {
        SymbolTable symbolTable = new SymbolTable(null);
        SemType intType = new SemType("int");
        SemType arrayType = new ArraySemType(intType);

        // Set up the symbol table
        symbolTable.addSymbol("myArray", arrayType);

        // myArray[0] -> int
        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "0", 0, 0, 0);
        ConstVal constVal = new ConstVal(0, intSymbol, 0, 0);
        Symbol arraySymbol = new Symbol(TokenTypes.IDENTIFIER, "myArray", 0, 0, 0);
        IdentifierAccess identifierAccess = new IdentifierAccess(arraySymbol, 0, 0);
        ArrayAccess arrayAccess = new ArrayAccess(identifierAccess, constVal, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitArrayAccess(arrayAccess, symbolTable);
            assertEquals("Expected type to be array of ints", intType, resultType);
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());

        }
    }

    @Test
    public void testRecordAccess() {
        SymbolTable symbolTable = new SymbolTable(null);
        LinkedHashMap<String, SemType> recordFields = new LinkedHashMap<>();
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
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

//    @Test

    @Test
    public void testBinaryExpressionRecordAccessLessThan() {
        // myRecord.field1 < 10
        SymbolTable symbolTable = new SymbolTable(null);
        LinkedHashMap<String, SemType> recordFields = new LinkedHashMap<>();
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
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test(expected = OperatorError.class)
    public void testUnaryExpressionWithWrongTypes() throws Exception {
        // -"hello"
        Symbol stringSymbol = new Symbol(TokenTypes.STRING_LITERAL, "hello", 0, 0, 10);
        ConstVal constVal = new ConstVal("hello", stringSymbol, 0, 0);
        UnaryOperator unaryOperator = new UnaryOperator(new Symbol(TokenTypes.MINUS, "-", 0, 0), 0, 0);
        UnaryExpression unaryExpression = new UnaryExpression(unaryOperator, constVal, 0, 0);

        SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
        semanticAnalysis.visitUnaryExpression(unaryExpression, null);
    }

    @Test(expected = OperatorError.class)
    public void testUnaryExpressionWithWrongTypes2() throws Exception {
        // !"hello"
        Symbol stringSymbol = new Symbol(TokenTypes.STRING_LITERAL, "hello", 0, 0, 10);
        ConstVal constVal = new ConstVal("hello", stringSymbol, 0, 0);
        UnaryOperator unaryOperator = new UnaryOperator(new Symbol(TokenTypes.NOT, "!", 0, 0), 0, 0);
        UnaryExpression unaryExpression = new UnaryExpression(unaryOperator, constVal, 0, 0);

        SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
        semanticAnalysis.visitUnaryExpression(unaryExpression, null);
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
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test(expected = ArgumentError.class)
    public void testFunctionCallWithIncorrectArgs() throws Exception {
        SymbolTable symbolTable = new SymbolTable(null);
        SemType functionReturnType = new SemType("int");
        SemType paramType = new SemType("int");
        SemType[] paramTypes = {paramType};
        FunctionSemType functionSemType = new FunctionSemType(functionReturnType, paramTypes);

        // Set up the symbol table
        symbolTable.addSymbol("myFunction", functionSemType);

        // myFunction(5.5)
        //            ^^^-> supposed to be int, conversion is not possible from float to int -> must throw an argument error
        Symbol floatSymbol = new Symbol(TokenTypes.FLOAT_LITERAL, "5.5", 0, 0, 5.5f);
        ConstVal constVal = new ConstVal(5.5f, floatSymbol, 0, 0);

        ArrayList<ParamCall> paramCalls = new ArrayList<>();
        paramCalls.add(new ParamCall(constVal, 0, 0, 0));
        Symbol functionSymbol = new Symbol(TokenTypes.IDENTIFIER, "myFunction", 0, 0);
        FunctionCall functionCall = new FunctionCall(functionSymbol, paramCalls, 0, 0);


        SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
        semanticAnalysis.visitFunctionCall(functionCall, symbolTable);
    }

    @Test(expected = ArgumentError.class)
    public void testFunctionCallWithNotEnoughArgs() throws Exception {
        SymbolTable symbolTable = new SymbolTable(null);
        SemType functionReturnType = new SemType("int");
        SemType paramType = new SemType("int");
        SemType[] paramTypes = {paramType, paramType};
        FunctionSemType functionSemType = new FunctionSemType(functionReturnType, paramTypes);

        // Set up the symbol table
        symbolTable.addSymbol("myFunction", functionSemType);

        // myFunction(5, 5)
        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "5", 0, 0, 5);
        ConstVal constVal = new ConstVal(5, intSymbol, 0, 0);

        ArrayList<ParamCall> paramCalls = new ArrayList<>();
        paramCalls.add(new ParamCall(constVal, 0, 0, 0));

        Symbol functionSymbol = new Symbol(TokenTypes.IDENTIFIER, "myFunction", 0, 0);
        FunctionCall functionCall = new FunctionCall(functionSymbol, paramCalls, 0, 0);


        SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
        semanticAnalysis.visitFunctionCall(functionCall, symbolTable);
    }

    @Test
    public void testNewRecord() {
        // Test record instantiation
        LinkedHashMap<String, SemType> recordFields = new LinkedHashMap<>();
        recordFields.put("field1", new SemType("int"));
        recordFields.put("field2", new SemType("string"));
        SemType recordType = new RecordSemType(recordFields, "MyRecord");
        SymbolTable symbolTable = new SymbolTable(null);
        // Set up the symbol table
        symbolTable.addSymbol("MyRecord", recordType);

        // MyRecord(5, "hello");
        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "5", 0, 0, 5);
        ConstVal constVal1 = new ConstVal(5, intSymbol, 0, 0);
        Symbol stringSymbol = new Symbol(TokenTypes.STRING_LITERAL, "hello", 0, 0, "hello");
        ConstVal constVal2 = new ConstVal("hello", stringSymbol, 0, 0);
        ArrayList<ParamCall> paramCalls = new ArrayList<>();
        paramCalls.add(new ParamCall(constVal1, 0, 0, 0));
        paramCalls.add(new ParamCall(constVal2, 1, 0, 1));

        Symbol recordSymbol = new Symbol(TokenTypes.RECORD, "MyRecord", 0, 0);
        NewRecord newRecord = new NewRecord(recordSymbol, paramCalls, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitRecordInstantiation(newRecord, symbolTable);
            assertEquals("Expected type to be MyRecord", recordType, resultType);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test(expected = ArgumentError.class)
    public void testNewRecordWithWrongArgTypes() throws Exception {
        // Test record instantiation with wrong argument types
        LinkedHashMap<String, SemType> recordFields = new LinkedHashMap<>();
        recordFields.put("field1", new SemType("int"));
        recordFields.put("field2", new SemType("string"));
        SemType recordType = new RecordSemType(recordFields, "MyRecord");
        SymbolTable symbolTable = new SymbolTable(null);
        // Set up the symbol table
        symbolTable.addSymbol("MyRecord", recordType);

        // MyRecord(true, "hello");
        Symbol boolSymbol = new Symbol(TokenTypes.BOOL_TRUE, "true", 0, 0, true);
        ConstVal constVal1 = new ConstVal(true, boolSymbol, 0, 0);
        Symbol stringSymbol = new Symbol(TokenTypes.STRING_LITERAL, "hello", 0, 0, "hello");
        ConstVal constVal2 = new ConstVal("hello", stringSymbol, 0, 0);
        ArrayList<ParamCall> paramCalls = new ArrayList<>();
        paramCalls.add(new ParamCall(constVal1, 0, 0, 0));
        paramCalls.add(new ParamCall(constVal2, 1, 0, 1));

        Symbol recordSymbol = new Symbol(TokenTypes.RECORD, "MyRecord", 0, 0);
        NewRecord newRecord = new NewRecord(recordSymbol, paramCalls, 0, 0);

        SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
        semanticAnalysis.visitRecordInstantiation(newRecord, symbolTable);
    }

    @Test()
    public void testRecordDefWithRecordField() {
        // Point rec {
        //    x int;
        //    y int;
        //}
        //
        //Person rec{
        //    name string;
        //    location Point;
        //    history int[];
        //}
        SymbolTable symbolTable = new SymbolTable(null);

        LinkedHashMap<String, SemType> recordFields = new LinkedHashMap<>();
        recordFields.put("x", new SemType("int"));
        recordFields.put("y", new SemType("int"));
        SemType recordType = new RecordSemType(recordFields, "Point");
        symbolTable.addSymbol("Point", recordType);

        LinkedHashMap<String, SemType> recordFields2 = new LinkedHashMap<>();
        recordFields2.put("name", new SemType("string"));

        recordFields2.put("location", recordType);
        recordFields2.put("history", new ArraySemType(new SemType("int"), 0));
        SemType expectedSemType = new RecordSemType(recordFields2, "Person");

        // Create the record definition
        ArrayList<RecordFieldDefinition> recordFieldDefinitions = new ArrayList<>();
        Symbol fieldSymbol1 = new Symbol(TokenTypes.IDENTIFIER, "name", 0, 0);
        Type fieldType1 = new Type(new Symbol(TokenTypes.STRING, "string", 0, 0), 0, 0);
        RecordFieldDefinition recordFieldDefinition = new RecordFieldDefinition(fieldSymbol1, fieldType1, 0, 0, 0);
        recordFieldDefinitions.add(recordFieldDefinition);

        Symbol fieldSymbol2 = new Symbol(TokenTypes.IDENTIFIER, "location", 0, 0);
        Type fieldType2 = new Type(new Symbol(TokenTypes.RECORD, "Point", 0, 0), 0, 0);
        RecordFieldDefinition recordFieldDefinition2 = new RecordFieldDefinition(fieldSymbol2, fieldType2, 1, 0, 0);
        recordFieldDefinitions.add(recordFieldDefinition2);

        Symbol fieldSymbol3 = new Symbol(TokenTypes.IDENTIFIER, "history", 0, 0);
        Type fieldType3 = new Type(new Symbol(TokenTypes.INT, "int", 0, 0), true, 0, 0);
        RecordFieldDefinition recordFieldDefinition3 = new RecordFieldDefinition(fieldSymbol3, fieldType3, 2, 0, 0);
        recordFieldDefinitions.add(recordFieldDefinition3);

        Symbol recordSymbol = new Symbol(TokenTypes.RECORD, "Person", 0, 0);
        RecordDefinition recordDef = new RecordDefinition(recordSymbol, recordFieldDefinitions, 0, 0);

        // Visit the record definition
        SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
        try {
            SemType recSemType = semanticAnalysis.visitRecordDefinition(recordDef, symbolTable);
            assertEquals("Expected type to be Person", expectedSemType, recSemType);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test(expected = ArgumentError.class)
    public void testNewRecordTooFewArguments() throws Exception {
        // Test record instantiation with wrong argument types
        LinkedHashMap<String, SemType> recordFields = new LinkedHashMap<>();
        recordFields.put("field1", new SemType("int"));
        recordFields.put("field2", new SemType("string"));
        SemType recordType = new RecordSemType(recordFields, "MyRecord");
        SymbolTable symbolTable = new SymbolTable(null);
        // Set up the symbol table
        symbolTable.addSymbol("MyRecord", recordType);

        // MyRecord(true, "hello");
        Symbol boolSymbol = new Symbol(TokenTypes.BOOL_TRUE, "true", 0, 0, true);
        ConstVal constVal1 = new ConstVal(true, boolSymbol, 0, 0);
        ArrayList<ParamCall> paramCalls = new ArrayList<>();
        paramCalls.add(new ParamCall(constVal1, 0, 0, 0));

        Symbol recordSymbol = new Symbol(TokenTypes.RECORD, "MyRecord", 0, 0);
        NewRecord newRecord = new NewRecord(recordSymbol, paramCalls, 0, 0);

        SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
        semanticAnalysis.visitRecordInstantiation(newRecord, symbolTable);
    }

    @Test(expected = ArgumentError.class)
    public void testNewRecordTooManyArguments() throws Exception {
        // Test record instantiation with wrong argument types
        LinkedHashMap<String, SemType> recordFields = new LinkedHashMap<>();
        recordFields.put("field1", new SemType("int"));
        recordFields.put("field2", new SemType("string"));
        SemType recordType = new RecordSemType(recordFields, "MyRecord");
        SymbolTable symbolTable = new SymbolTable(null);
        // Set up the symbol table
        symbolTable.addSymbol("MyRecord", recordType);

        // MyRecord(true, "hello", "not a field");
        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "0", 0, 0, 0);
        ConstVal constVal1 = new ConstVal(0, intSymbol, 0, 0);
        Symbol stringSymbol = new Symbol(TokenTypes.STRING_LITERAL, "hello", 0, 0, "hello");
        ConstVal constVal2 = new ConstVal("hello", stringSymbol, 0, 0);
        Symbol stringSymbol2 = new Symbol(TokenTypes.STRING_LITERAL, "not a field", 0, 0, "not a field");
        ConstVal constVal3 = new ConstVal("not a field", stringSymbol2, 0, 0);
        ArrayList<ParamCall> paramCalls = new ArrayList<>();
        paramCalls.add(new ParamCall(constVal1, 0, 0, 0));
        paramCalls.add(new ParamCall(constVal2, 1, 0, 1));
        paramCalls.add(new ParamCall(constVal3, 2, 0, 2));

        Symbol recordSymbol = new Symbol(TokenTypes.RECORD, "MyRecord", 0, 0);
        NewRecord newRecord = new NewRecord(recordSymbol, paramCalls, 0, 0);

        SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
        semanticAnalysis.visitRecordInstantiation(newRecord, symbolTable);
    }

    @Test
    public void testParenthesesTerm() {
        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "5", 0, 0, 5);
        ConstVal constVal = new ConstVal(5, intSymbol, 0, 0);
        ParenthesesTerm parenthesesTerm = new ParenthesesTerm(constVal, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitParenthesesTerm(parenthesesTerm, null);
            assertEquals("Expected type to be int", new SemType("int"), resultType);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testParenthesesTermWithExpression() {
        // (a + b) with and b being int
        SymbolTable symbolTable = new SymbolTable(null);
        SemType intType = new SemType("int");
        // Set up the symbol table
        symbolTable.addSymbol("a", intType);
        symbolTable.addSymbol("b", intType);

        IdentifierAccess identifierAccess1 = new IdentifierAccess(new Symbol(TokenTypes.IDENTIFIER, "a", 0, 0), 0, 0);
        IdentifierAccess identifierAccess2 = new IdentifierAccess(new Symbol(TokenTypes.IDENTIFIER, "b", 0, 0), 0, 0);
        BinaryOperator binaryOperator = new BinaryOperator(new Symbol(TokenTypes.PLUS, "+", 0, 0), 0, 0);
        BinaryExpression binaryExpression = new BinaryExpression(identifierAccess1, binaryOperator, identifierAccess2, 0, 0);
        ParenthesesTerm parenthesesTerm = new ParenthesesTerm(binaryExpression, 0, 0);
        SemType expectedType = new SemType("int");

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitParenthesesTerm(parenthesesTerm, symbolTable);
            assertEquals("Expected type to be int", expectedType, resultType);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testForLoop() {
        // for(i, 0, 10, 1) {
        //     writeln(i);
        // }
        // we don't test the block here

        SymbolTable symbolTable = new SymbolTable(null, "myFunction");
        FunctionSemType functionSemType = new FunctionSemType(new SemType("void"), new SemType[]{});
        SemType intType = new SemType("int");
        // Set up the symbol table
        symbolTable.addSymbol("myFunction", functionSemType);
        symbolTable.addSymbol("i", intType);

//        Symbol startSymbol = new Symbol(TokenTypes.INT_LITERAL, "0", 0, 0, 0);
//        Symbol endSymbol = new Symbol(TokenTypes.INT_LITERAL, "10", 0, 0, 10);
//        Symbol stepSymbol = new Symbol(TokenTypes.INT_LITERAL, "1", 0, 0, 1);
        Expression startExpression = new ConstVal(0, new Symbol(TokenTypes.INT_LITERAL, "0", 0, 0, 0), 0, 0);
        Expression endExpression = new ConstVal(10, new Symbol(TokenTypes.INT_LITERAL, "10", 0, 0, 10), 0, 0);
        Expression stepExpression = new ConstVal(1, new Symbol(TokenTypes.INT_LITERAL, "1", 0, 0, 1), 0, 0);

        Symbol var = new Symbol(TokenTypes.IDENTIFIER, "i", 0, 0);

        Block block = new Block(new ArrayList<Statement>(), new ReturnStatement(null, 0, 0), 0, 0);
        ForLoop forLoop = new ForLoop(var, startExpression, endExpression, stepExpression, block, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            semanticAnalysis.visitForLoop(forLoop, symbolTable);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testForLoopWithVarStart() {
        // for(i, a, 10, 1) {
        //     writeln(i);
        // }
        // we don't test the block here

        SymbolTable symbolTable = new SymbolTable(null, "myFunction");
        FunctionSemType functionSemType = new FunctionSemType(new SemType("void"), new SemType[]{});
        SemType intType = new SemType("int");
        // Set up the symbol table
        symbolTable.addSymbol("myFunction", functionSemType);
        symbolTable.addSymbol("i", intType);
        symbolTable.addSymbol("a", intType);

//        Symbol startSymbol = new Symbol(TokenTypes.IDENTIFIER, "a", 0, 0);
//
//        Symbol endSymbol = new Symbol(TokenTypes.INT_LITERAL, "10", 0, 0, 10);
//        Symbol stepSymbol = new Symbol(TokenTypes.INT_LITERAL, "1", 0, 0, 1);

        Expression startExpression = new IdentifierAccess(new Symbol(TokenTypes.IDENTIFIER, "a", 0, 0), 0, 0);
        Expression endExpression = new ConstVal(10, new Symbol(TokenTypes.INT_LITERAL, "10", 0, 0, 10), 0, 0);
        Expression stepExpression = new ConstVal(1, new Symbol(TokenTypes.INT_LITERAL, "1", 0, 0, 1), 0, 0);

        Symbol var = new Symbol(TokenTypes.IDENTIFIER, "i", 0, 0);
        Block block = new Block(new ArrayList<Statement>(), new ReturnStatement(null, 0, 0), 0, 0);
        ForLoop forLoop = new ForLoop(var, startExpression, endExpression, stepExpression, block, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            semanticAnalysis.visitForLoop(forLoop, symbolTable);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testWhileLoop() {
        // while(i < 10) { ... }

        SymbolTable symbolTable = new SymbolTable(null, "myFunction");
        FunctionSemType functionSemType = new FunctionSemType(new SemType("void"), new SemType[]{});
        SemType intType = new SemType("int");
        // Set up the symbol table
        symbolTable.addSymbol("myFunction", functionSemType);
        symbolTable.addSymbol("i", intType);

        Symbol conditionSymbol = new Symbol(TokenTypes.LESS_THAN, "<", 0, 0);
        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "10", 0, 0, 10);
        IdentifierAccess identifierAccess = new IdentifierAccess(new Symbol(TokenTypes.IDENTIFIER, "i", 0, 0), 0, 0);
        BinaryOperator binaryOperator = new BinaryOperator(conditionSymbol, 0, 0);
        ConstVal constVal = new ConstVal(10, intSymbol, 0, 0);
        BinaryExpression binaryExpression = new BinaryExpression(identifierAccess, binaryOperator, constVal, 0, 0);
        Block block = new Block(new ArrayList<Statement>(), new ReturnStatement(null, 0, 0), 0, 0);
        WhileLoop whileLoop = new WhileLoop(binaryExpression, block, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            semanticAnalysis.visitWhileLoop(whileLoop, symbolTable);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test(expected = MissingConditionError.class)
    public void testWhileLoopNoBooleanCondition() throws Exception {
        // while (i + 1) { ... }
        // this will throw a MissingConditionError
        // this test passes if the exception is thrown
        SymbolTable symbolTable = new SymbolTable(null, "myFunction");
        FunctionSemType functionSemType = new FunctionSemType(new SemType("void"), new SemType[]{});
        SemType intType = new SemType("int");
        // Set up the symbol table
        symbolTable.addSymbol("myFunction", functionSemType);
        symbolTable.addSymbol("i", intType);

        Symbol conditionSymbol = new Symbol(TokenTypes.PLUS, "+", 0, 0);
        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "1", 0, 0, 1);
        IdentifierAccess identifierAccess = new IdentifierAccess(new Symbol(TokenTypes.IDENTIFIER, "i", 0, 0), 0, 0);
        BinaryOperator binaryOperator = new BinaryOperator(conditionSymbol, 0, 0);
        ConstVal constVal = new ConstVal(1, intSymbol, 0, 0);
        BinaryExpression binaryExpression = new BinaryExpression(identifierAccess, binaryOperator, constVal, 0, 0);
        Block block = new Block(new ArrayList<Statement>(), new ReturnStatement(null, 0, 0), 0, 0);
        WhileLoop whileLoop = new WhileLoop(binaryExpression, block, 0, 0);

        SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
        semanticAnalysis.visitWhileLoop(whileLoop, symbolTable);
    }

    @Test
    public void testFreeStatement() {
        // free x
        // where x is in the symbol table
        SymbolTable symbolTable = new SymbolTable(null);
        SemType intType = new SemType("int");
        // Set up the symbol table
        symbolTable.addSymbol("x", intType);

        Symbol identifierSymbol = new Symbol(TokenTypes.IDENTIFIER, "x", 0, 0);
        IdentifierAccess identifierAccess = new IdentifierAccess(identifierSymbol, 0, 0);
        FreeStatement freeStatement = new FreeStatement(identifierAccess, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            semanticAnalysis.visitFreeStatement(freeStatement, symbolTable);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test(expected = ScopeError.class)
    public void testFreeStatementOfNonExistingVar() throws Exception {
        // free x
        // where x is NOT the symbol table
        // this will throw a ScopeError
        // this test passes if the exception is thrown
        SymbolTable symbolTable = new SymbolTable(null);

        Symbol identifierSymbol = new Symbol(TokenTypes.IDENTIFIER, "x", 0, 0);
        IdentifierAccess identifierAccess = new IdentifierAccess(identifierSymbol, 0, 0);
        FreeStatement freeStatement = new FreeStatement(identifierAccess, 0, 0);

        SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
        semanticAnalysis.visitFreeStatement(freeStatement, symbolTable);
    }

    @Test
    public void testFunctionDefinition() {
        // fun copyPoints(p Point[]) Point {
        //     return p[0];
        // }
        // we don't test the function body here
        SymbolTable symbolTable = new SymbolTable(null);
        SemType intType = new SemType("int");

        LinkedHashMap<String, SemType> recordFields = new LinkedHashMap<>();
        recordFields.put("x", intType);
        recordFields.put("y", intType);
        RecordSemType recordSemType = new RecordSemType(recordFields, "Point");
        SemType[] paramTypes = {new ArraySemType(recordSemType)};
        FunctionSemType functionSemType = new FunctionSemType(recordSemType, paramTypes);

        // Set up the symbol table
        symbolTable.addSymbol("copyPoints", functionSemType);
        symbolTable.addSymbol("Point", recordSemType);

        Symbol functionSymbol = new Symbol(TokenTypes.IDENTIFIER, "copyPoints", 0, 0);

        Type returnType = new Type(new Symbol(TokenTypes.RECORD, "Point", 0, 0), 0, 0);

        ArrayList<ParamDefinition> paramDefinitions = new ArrayList<>();
        Symbol paramSymbol = new Symbol(TokenTypes.IDENTIFIER, "p", 0, 0);
        Type paramType = new Type(new Symbol(TokenTypes.RECORD, "Point", 0, 0), true, 0, 0);
        ParamDefinition paramDefinition = new ParamDefinition(paramSymbol, paramType, 0, 0, 0);
        paramDefinitions.add(paramDefinition);

        IdentifierAccess identifierAccess = new IdentifierAccess(new Symbol(TokenTypes.IDENTIFIER, "p", 0, 0), 0, 0);
        Expression expression = new ConstVal(0, new Symbol(TokenTypes.INT_LITERAL, "0", 0, 0, 0), 0, 0);
        ArrayAccess arrayAccess = new ArrayAccess(identifierAccess, expression, 0, 0);
        Block block = new Block(new ArrayList<Statement>(), new ReturnStatement(arrayAccess, 0, 0), 0, 0);
        FunctionDefinition functionDefinition = new FunctionDefinition(functionSymbol, returnType, paramDefinitions, block, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resSemType = semanticAnalysis.visitFunctionDefinition(functionDefinition, symbolTable);
            assertEquals("Expected type to be Point", recordSemType, resSemType);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    // visitBlock returns nothing, so there is nothing to test

    @Test
    public void testIfStatement() {
        // if (i < 10) { ... }
        SymbolTable symbolTable = new SymbolTable(null, "myFunction");
        FunctionSemType functionSemType = new FunctionSemType(new SemType("void"), new SemType[]{});
        SemType intType = new SemType("int");

        // Set up the symbol table
        symbolTable.addSymbol("myFunction", functionSemType);
        symbolTable.addSymbol("i", intType);

        Symbol conditionSymbol = new Symbol(TokenTypes.LESS_THAN, "<", 0, 0);
        IdentifierAccess identifierAccess = new IdentifierAccess(new Symbol(TokenTypes.IDENTIFIER, "i", 0, 0), 0, 0);
        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "10", 0, 0, 10);
        ConstVal constVal = new ConstVal(10, intSymbol, 0, 0);
        BinaryOperator binaryOperator = new BinaryOperator(conditionSymbol, 0, 0);
        BinaryExpression binaryExpression = new BinaryExpression(identifierAccess, binaryOperator, constVal, 0, 0);

        Block block = new Block(new ArrayList<Statement>(), new ReturnStatement(null, 0, 0), 0, 0);
        IfStatement ifStatement = new IfStatement(binaryExpression, block, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            semanticAnalysis.visitIfStatement(ifStatement, symbolTable);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testIfStatementWithElse() {
        // if (i < 10) { ... }
        SymbolTable symbolTable = new SymbolTable(null, "myFunction");
        FunctionSemType functionSemType = new FunctionSemType(new SemType("void"), new SemType[]{});
        SemType intType = new SemType("int");

        // Set up the symbol table
        symbolTable.addSymbol("myFunction", functionSemType);
        symbolTable.addSymbol("i", intType);

        Symbol conditionSymbol = new Symbol(TokenTypes.LESS_THAN, "<", 0, 0);
        IdentifierAccess identifierAccess = new IdentifierAccess(new Symbol(TokenTypes.IDENTIFIER, "i", 0, 0), 0, 0);
        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "10", 0, 0, 10);
        ConstVal constVal = new ConstVal(10, intSymbol, 0, 0);
        BinaryOperator binaryOperator = new BinaryOperator(conditionSymbol, 0, 0);
        BinaryExpression binaryExpression = new BinaryExpression(identifierAccess, binaryOperator, constVal, 0, 0);

        Block block = new Block(new ArrayList<Statement>(), new ReturnStatement(null, 0, 0), 0, 0);
        IfStatement ifStatement = new IfStatement(binaryExpression, block, block, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            semanticAnalysis.visitIfStatement(ifStatement, symbolTable);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test(expected = MissingConditionError.class)
    public void testIfConditionWithNoCondition() throws Exception {
        // if (5) { ... }
        // this will throw a MissingConditionError
        // this test passes if the exception is thrown

        SymbolTable symbolTable = new SymbolTable(null, "myFunction");
        FunctionSemType functionSemType = new FunctionSemType(new SemType("void"), new SemType[]{});
        SemType intType = new SemType("int");
        // Set up the symbol table
        symbolTable.addSymbol("myFunction", functionSemType);

        Symbol conditionSymbol = new Symbol(TokenTypes.INT_LITERAL, "5", 0, 0, 5);
        ConstVal constVal = new ConstVal(5, conditionSymbol, 0, 0);

        Block block = new Block(new ArrayList<Statement>(), new ReturnStatement(null, 0, 0), 0, 0);
        IfStatement ifStatement = new IfStatement(constVal, block, 0, 0);

        SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
        semanticAnalysis.visitIfStatement(ifStatement, symbolTable);
    }

    @Test
    public void testRecordDefinition() {
        // Point rec {
        //    x int;
        //    y int;
        // }

        SymbolTable symbolTable = new SymbolTable(null);
        SemType intType = new SemType("int");

        Symbol identifier = new Symbol(TokenTypes.RECORD, "Point", 0, 0);

        ArrayList<RecordFieldDefinition> fields = new ArrayList<>();
        Symbol field1Symbol = new Symbol(TokenTypes.IDENTIFIER, "x", 0, 0);
        Type field1Type = new Type(new Symbol(TokenTypes.INT, "int", 0, 0), 0, 0);
        RecordFieldDefinition recordFieldDefinition = new RecordFieldDefinition(field1Symbol, field1Type, 0, 0, 0);
        fields.add(recordFieldDefinition);

        Symbol field2Symbol = new Symbol(TokenTypes.IDENTIFIER, "y", 0, 0);
        Type field2Type = new Type(new Symbol(TokenTypes.INT, "int", 0, 0), 0, 0);
        RecordFieldDefinition recordFieldDefinition2 = new RecordFieldDefinition(field2Symbol, field2Type, 0, 0, 0);
        fields.add(recordFieldDefinition2);
        RecordDefinition recordDefinition = new RecordDefinition(identifier, fields, 0, 0);

        // Expected RecordSemType
        LinkedHashMap<String, SemType> fieldsMap = new LinkedHashMap<>();
        fieldsMap.put("x", intType);
        fieldsMap.put("y", intType);
        RecordSemType recordSemType = new RecordSemType(fieldsMap, "Point");

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultSemType = semanticAnalysis.visitRecordDefinition(recordDefinition, symbolTable);
            assertEquals("Expected type to be Point", recordSemType, resultSemType);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test(expected = RecordError.class)
    public void testRecordDefinitionRecordError() throws Exception {
        // Test record instantiation with a name that overrides a previously defined type or record
        SymbolTable symbolTable = new SymbolTable(null);

        Symbol identifier = new Symbol(TokenTypes.RECORD, "int", 0, 0);
        ArrayList<RecordFieldDefinition> fields = new ArrayList<>();
        Symbol fieldSymbol = new Symbol(TokenTypes.IDENTIFIER, "field1", 0, 0);
        Type fieldType = new Type(new Symbol(TokenTypes.INT, "int", 0, 0), 0, 0);
        RecordFieldDefinition recordFieldDefinition = new RecordFieldDefinition(fieldSymbol, fieldType, 0, 0, 0);
        fields.add(recordFieldDefinition);
        RecordDefinition recordDefinition = new RecordDefinition(identifier, fields, 0, 0);

        SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
        semanticAnalysis.visitRecordDefinition(recordDefinition, symbolTable);
    }

    @Test
    public void testReturnStatement() {
        // return 5;
        SemType intType = new SemType("int");
        SymbolTable symbolTable = new SymbolTable(null, "functionName");
        FunctionSemType functionSemType = new FunctionSemType(intType, new SemType[]{});
        symbolTable.addSymbol("functionName", functionSemType);

        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "5", 0, 0, 5);
        ConstVal constVal = new ConstVal(5, intSymbol, 0, 0);
        ReturnStatement returnStatement = new ReturnStatement(constVal, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resultType = semanticAnalysis.visitReturnStatement(returnStatement, symbolTable);
            assertEquals("Expected type to be int", intType, resultType);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test(expected = ReturnError.class)
    public void testReturnFromVoidFunction() throws Exception {
        // return 5;
        // this will throw a ReturnError
        // this test passes if the exception is thrown
        SemType voidType = new SemType("void");
        SymbolTable symbolTable = new SymbolTable(null, "functionName");
        FunctionSemType functionSemType = new FunctionSemType(voidType, new SemType[]{});
        symbolTable.addSymbol("functionName", functionSemType);

        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "5", 0, 0, 5);
        ConstVal constVal = new ConstVal(5, intSymbol, 0, 0);
        ReturnStatement returnStatement = new ReturnStatement(constVal, 0, 0);

        SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
        semanticAnalysis.visitReturnStatement(returnStatement, symbolTable);
    }

    @Test
    public void testVariableAssignment() {
        // x = 5;
        SemType intType = new SemType("int");
        SymbolTable symbolTable = new SymbolTable(null);
        symbolTable.addSymbol("x", intType);

        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "5", 0, 0, 5);
        ConstVal constVal = new ConstVal(5, intSymbol, 0, 0);
        IdentifierAccess identifierAccess = new IdentifierAccess(new Symbol(TokenTypes.IDENTIFIER, "x", 0, 0), 0, 0);
        VariableAssignment assignment = new VariableAssignment(identifierAccess, constVal, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            semanticAnalysis.visitVariableAssignment(assignment, symbolTable);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test(expected = TypeError.class)
    public void testAssigningToConst() throws Exception {
        // x = 5
        // but x is a constant
        SemType constIntType = new SemType("int", true);
        SymbolTable symbolTable = new SymbolTable(null);
        symbolTable.addSymbol("x", constIntType);

        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "5", 0, 0, 5);
        ConstVal constVal = new ConstVal(5, intSymbol, 0, 0);
        IdentifierAccess identifierAccess = new IdentifierAccess(new Symbol(TokenTypes.IDENTIFIER, "x", 0, 0), 0, 0);
        VariableAssignment assignment = new VariableAssignment(identifierAccess, constVal, 0, 0);
        // This should throw a TypeError
        SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
        semanticAnalysis.visitVariableAssignment(assignment, symbolTable);
    }

    @Test(expected = TypeError.class)
    public void testAssigningIncompatibleTypes() throws Exception {
        // x = "Hello"
        // with x being an int
        SemType intType = new SemType("int");
        SymbolTable symbolTable = new SymbolTable(null);
        symbolTable.addSymbol("x", intType);

        Symbol stringSymbol = new Symbol(TokenTypes.STRING_LITERAL, "Hello", 0, 0, "Hello");
        ConstVal constVal = new ConstVal("Hello", stringSymbol, 0, 0);
        IdentifierAccess identifierAccess = new IdentifierAccess(new Symbol(TokenTypes.IDENTIFIER, "x", 0, 0), 0, 0);
        VariableAssignment assignment = new VariableAssignment(identifierAccess, constVal, 0, 0);
        // This should throw a TypeError
        SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
        semanticAnalysis.visitVariableAssignment(assignment, symbolTable);
    }

    @Test(expected = ScopeError.class)
    public void testAssigningNonExistingVar() throws Exception {
        // x = "Hello"
        // with x not declared in table
        SymbolTable symbolTable = new SymbolTable(null);

        Symbol stringSymbol = new Symbol(TokenTypes.STRING_LITERAL, "Hello", 0, 0, "Hello");
        ConstVal constVal = new ConstVal("Hello", stringSymbol, 0, 0);
        IdentifierAccess identifierAccess = new IdentifierAccess(new Symbol(TokenTypes.IDENTIFIER, "x", 0, 0), 0, 0);
        VariableAssignment assignment = new VariableAssignment(identifierAccess, constVal, 0, 0);

        // This should throw a ScopeError
        SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
        semanticAnalysis.visitVariableAssignment(assignment, symbolTable);
    }

    @Test(expected = TypeError.class)
    public void testAssigningIncompatibleTypesFloatToInt() throws Exception {
        // x = 123.45
        // with x being an int
        SemType intType = new SemType("int");
        SymbolTable symbolTable = new SymbolTable(null);
        symbolTable.addSymbol("x", intType);

        Symbol stringSymbol = new Symbol(TokenTypes.FLOAT_LITERAL, "123.45", 0, 0, 123.45f);
        ConstVal constVal = new ConstVal(123.45f, stringSymbol, 0, 0);
        IdentifierAccess identifierAccess = new IdentifierAccess(new Symbol(TokenTypes.IDENTIFIER, "x", 0, 0), 0, 0);
        VariableAssignment assignment = new VariableAssignment(identifierAccess, constVal, 0, 0);

        // This should throw a TypeError
        SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
        semanticAnalysis.visitVariableAssignment(assignment, symbolTable);
    }

    @Test
    public void testVariableDeclaration() {
        // final x int = 2132
        SemType intType = new SemType("int");
        SymbolTable symbolTable = new SymbolTable(null);

        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "2132", 0, 0, 2132);
        ConstVal constVal = new ConstVal(2132, intSymbol, 0, 0);
        Symbol identifierSymbol = new Symbol(TokenTypes.IDENTIFIER, "x", 0, 0);
        Type varType = new Type(new Symbol(TokenTypes.INT, "int", 0, 0), 0, 0);
        VariableDeclaration variableDeclaration = new VariableDeclaration(identifierSymbol, varType, constVal, true, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resType = semanticAnalysis.visitVariableDeclaration(variableDeclaration, symbolTable);
            assertEquals("Expected type to be int", intType, resType);
            assertNotNull("Expected symbol to be added to symbol table", symbolTable.lookup("x"));
            assertTrue("Expected symbol to be constant", resType.isConstant);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testVarDeclNotConst() {
        // x int = 2132
        SemType intType = new SemType("int");
        SymbolTable symbolTable = new SymbolTable(null);

        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "2132", 0, 0, 2132);
        ConstVal constVal = new ConstVal(2132, intSymbol, 0, 0);
        Symbol identifierSymbol = new Symbol(TokenTypes.IDENTIFIER, "x", 0, 0);
        Type varType = new Type(new Symbol(TokenTypes.INT, "int", 0, 0), 0, 0);
        VariableDeclaration variableDeclaration = new VariableDeclaration(identifierSymbol, varType, constVal, false, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resType = semanticAnalysis.visitVariableDeclaration(variableDeclaration, symbolTable);
            assertEquals("Expected type to be int", intType, resType);
            assertNotNull("Expected symbol to be added to symbol table", symbolTable.lookup("x"));
            assertFalse("Expected symbol to not be constant", resType.isConstant);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testVarDeclNoValue() {
        // x string
        SemType stringType = new SemType("string");
        SymbolTable symbolTable = new SymbolTable(null);
        Symbol identifierSymbol = new Symbol(TokenTypes.IDENTIFIER, "x", 0, 0);
        Type varType = new Type(new Symbol(TokenTypes.STRING, "string", 0, 0), 0, 0);
        VariableDeclaration variableDeclaration = new VariableDeclaration(identifierSymbol, varType, null, false, 0, 0);
        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resType = semanticAnalysis.visitVariableDeclaration(variableDeclaration, symbolTable);
            assertEquals("Expected type to be string", stringType, resType);
            assertNotNull("Expected symbol to be added to symbol table", symbolTable.lookup("x"));
            assertFalse("Expected symbol to not be constant", resType.isConstant);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test(expected = ScopeError.class)
    public void testVarDeclOfAlreadyDeclIdentifier() throws Exception {
        // x int = 2132
        // but x is already in the symbol table
        SemType intType = new SemType("int");
        SymbolTable symbolTable = new SymbolTable(null);
        symbolTable.addSymbol("x", intType);

        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "2132", 0, 0, 2132);
        ConstVal constVal = new ConstVal(2132, intSymbol, 0, 0);
        Symbol identifierSymbol = new Symbol(TokenTypes.IDENTIFIER, "x", 0, 0);
        Type varType = new Type(new Symbol(TokenTypes.INT, "int", 0, 0), 0, 0);
        VariableDeclaration variableDeclaration = new VariableDeclaration(identifierSymbol, varType, constVal, false, 0, 0);
        // This should throw a ScopeError
        SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
        semanticAnalysis.visitVariableDeclaration(variableDeclaration, symbolTable);
    }

    @Test
    public void testVarDeclShadowingGlobalVar() {
        // final i int is defined in the global scope
        // we'll define a var i int in a local scope
        SymbolTable globalTable = new SymbolTable(null);
        globalTable.addSymbol("i", new SemType("int", true));
        SymbolTable localTable = new SymbolTable(globalTable);
        SemType intType = new SemType("int");

        Symbol intSymbol = new Symbol(TokenTypes.INT_LITERAL, "2132", 0, 0, 2132);
        ConstVal constVal = new ConstVal(2132, intSymbol, 0, 0);
        Symbol identifierSymbol = new Symbol(TokenTypes.IDENTIFIER, "i", 0, 0);
        Type varType = new Type(new Symbol(TokenTypes.INT, "int", 0, 0), 0, 0);
        VariableDeclaration variableDeclaration = new VariableDeclaration(identifierSymbol, varType, constVal, false, 0, 0);

        try {
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
            SemType resType = semanticAnalysis.visitVariableDeclaration(variableDeclaration, localTable);
            assertEquals("Expected type to be int", intType, resType);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Semantic analysis failed: " + e.getMessage());
        }
    }

    @Test
    public void testNoReturnsInCertainPaths() throws Exception {
        // testing the cases where the function does not return a value in every path
        String input = """
                fun main() int {
                    i int = 20;
                    if (i < 10) {
                        return 0;
                    } else {
                        writeln(i);
                        $ return 1;
                    }
                }
                """;

        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        try {
            Parser parser = new Parser(lexer);

            ASTNode node = parser.getAST();
            SemanticAnalysis semanticAnalysis = new SemanticAnalysis();

            semanticAnalysis.analyze(node, true);
            fail("Expected a ReturnError to be thrown");
        } catch (ReturnError e) {
            // expected
        }

    }
}

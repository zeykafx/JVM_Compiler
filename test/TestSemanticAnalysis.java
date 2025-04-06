import static org.junit.Assert.*;

import compiler.Lexer.*;
import compiler.Parser.*;
import compiler.Parser.ASTNodes.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Operators.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.*;
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

}

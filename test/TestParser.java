import static org.junit.Assert.*;

import org.junit.Test;
import java.io.StringReader;
import java.util.ArrayList;

import compiler.Lexer.*;
import compiler.Parser.*;
import compiler.Parser.ASTNodes.*;
import compiler.Parser.ASTNodes.Types.*;
import compiler.Parser.ASTNodes.Statements.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Operators.*;


public class TestParser {
    @Test
    public void testSimpleProgram() throws Exception {
        String input = """
        final i int = 3;
        fun main() {
            i = 4;
        }
        """;
        
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);

        ASTNode node = parser.getAST();
        assertNotNull(node);

        // Check if the node is a Program
        assertTrue(node instanceof Program);
        Program program = (Program) node;
        assertEquals(1, program.getConstants().size());
        assertEquals(0, program.getRecords().size());
        assertEquals(1, program.getFunctions().size());
        assertEquals(0, program.getGlobals().size());

        VariableDeclaration constant = program.getConstants().getFirst();
		assertTrue(constant.isConstant());

        assertEquals("i", constant.getName().lexeme);
        assertEquals(TokenTypes.INT, constant.getType().symbol.type);
        assertTrue(constant.getValue() instanceof ConstVal);

        ConstVal constVal = (ConstVal) constant.getValue();
        assertEquals(3, constVal.getValue());
        assertEquals(TokenTypes.INT_LITERAL, constVal.getSymbol().type);
    }

    @Test
    public void testFunctionDefinition() throws Exception {
        String input = """
        fun add(a int, b int) int {
            return a + b;
        }
        """;

        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);

        ASTNode node = parser.getAST();
        assertNotNull(node);

        // Check if the node is a Program
        assertTrue(node instanceof Program);
        Program program = (Program) node;
        assertEquals(0, program.getConstants().size());
        assertEquals(0, program.getRecords().size());
        assertEquals(1, program.getFunctions().size());
        assertEquals(0, program.getGlobals().size());

        FunctionDefinition function = program.getFunctions().getFirst();

        assertEquals("add", function.getName().lexeme);
        assertEquals(TokenTypes.INT, function.getReturnType().symbol.type);

        ArrayList<ParamDefinition> params = function.getParamDefinitions();
        assertEquals(2, params.size());

        assertEquals("a", params.get(0).getIdentifier().lexeme);
        assertEquals(TokenTypes.INT, params.get(0).getType().symbol.type);

        assertEquals("b", params.get(1).getIdentifier().lexeme);
        assertEquals(TokenTypes.INT, params.get(1).getType().symbol.type);

		assertNotNull(function.getBlock());
        Block block = (Block) function.getBlock();
        assertNotNull(block.getReturnStatement());
        assertTrue(block.getReturnStatement() instanceof ReturnStatement);
        ReturnStatement returnStmt = (ReturnStatement) block.getReturnStatement();
        assertTrue(returnStmt.getExpression() instanceof BinaryExpression);
        BinaryExpression binaryExpr = (BinaryExpression) returnStmt.getExpression();
        assertTrue(binaryExpr.getLeftTerm() instanceof IdentifierAccess);
        assertTrue(binaryExpr.getRightTerm() instanceof IdentifierAccess);
        IdentifierAccess leftAccess = (IdentifierAccess) binaryExpr.getLeftTerm();
        IdentifierAccess rightAccess = (IdentifierAccess) binaryExpr.getRightTerm();
        assertEquals("a", leftAccess.getIdentifier().lexeme);
        assertEquals("b", rightAccess.getIdentifier().lexeme);
        assertTrue(binaryExpr.getOperator() instanceof BinaryOperator);
        BinaryOperator plusOp = (BinaryOperator) binaryExpr.getOperator();
        assertEquals(TokenTypes.PLUS, plusOp.getOperator().type);
    }
    
    @Test
    public void testRecordDefinition() throws Exception {
        String input = """
        Product rec {
            code int;
            name string;
            price int;
        }
        """;

        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);

        ASTNode node = parser.getAST();
        assertNotNull(node);

        assertTrue(node instanceof Program);
        Program program = (Program) node;
        assertEquals(0, program.getConstants().size());
        assertEquals(1, program.getRecords().size());
        assertEquals(0, program.getFunctions().size());
        assertEquals(0, program.getGlobals().size());

        RecordDefinition record = program.getRecords().getFirst();
        assertEquals("Product", record.getIdentifier().lexeme);

        ArrayList<RecordFieldDefinition> fields = record.getFields();
        assertEquals(3, fields.size());

        assertEquals("code", fields.get(0).getIdentifier().lexeme);
        assertEquals(TokenTypes.INT, fields.get(0).getType().symbol.type);
        
        assertEquals("name", fields.get(1).getIdentifier().lexeme);
        assertEquals(TokenTypes.STRING, fields.get(1).getType().symbol.type);
        
        assertEquals("price", fields.get(2).getIdentifier().lexeme);
        assertEquals(TokenTypes.INT, fields.get(2).getType().symbol.type);
    }

    @Test
    public void testMultipleRecordDefinitions() throws Exception {
        String intput = """
        Product rec {
            code int;
            name string;
            price int;
        }
        Phone rec {
            model string;
        }
        """;

        StringReader reader = new StringReader(intput);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        ASTNode node = parser.getAST();
        assertNotNull(node);

        assertTrue(node instanceof Program);
        Program program = (Program) node;
        assertEquals(0, program.getConstants().size());
        assertEquals(2, program.getRecords().size());
        assertEquals(0, program.getFunctions().size());
        assertEquals(0, program.getGlobals().size());

        RecordDefinition record = program.getRecords().getFirst();
        assertEquals("Product", record.getIdentifier().lexeme);

        ArrayList<RecordFieldDefinition> fields = record.getFields();
        assertEquals(3, fields.size());

        assertEquals("code", fields.get(0).getIdentifier().lexeme);
        assertEquals(TokenTypes.INT, fields.get(0).getType().symbol.type);

        assertEquals("name", fields.get(1).getIdentifier().lexeme);
        assertEquals(TokenTypes.STRING, fields.get(1).getType().symbol.type);

        assertEquals("price", fields.get(2).getIdentifier().lexeme);
        assertEquals(TokenTypes.INT, fields.get(2).getType().symbol.type);

        RecordDefinition record2 = program.getRecords().get(1);
        assertEquals("Phone", record2.getIdentifier().lexeme);
        ArrayList<RecordFieldDefinition> fields2 = record2.getFields();
        assertEquals(1, fields2.size());


        assertEquals("model", fields2.getFirst().getIdentifier().lexeme);
        assertEquals(TokenTypes.STRING, fields2.getFirst().getType().symbol.type);
    }


    @Test
    public void testGlobalVariableDeclaration() throws Exception {
        String input = """
        i int;
        j float;
        k string;
        """;

        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);

        ASTNode node = parser.getAST();
        assertNotNull(node);

        assertTrue(node instanceof Program);
        Program program = (Program) node;
        assertEquals(0, program.getConstants().size());
        assertEquals(0, program.getRecords().size());
        assertEquals(0, program.getFunctions().size());
        assertEquals(3, program.getGlobals().size());

        VariableDeclaration globalVar1 = program.getGlobals().get(0);
        assertFalse(globalVar1.isConstant());
        assertEquals("i", globalVar1.getName().lexeme);
        assertEquals(TokenTypes.INT, globalVar1.getType().symbol.type);

        VariableDeclaration globalVar2 = program.getGlobals().get(1);
        assertFalse(globalVar2.isConstant());
        assertEquals("j", globalVar2.getName().lexeme);
        assertEquals(TokenTypes.FLOAT, globalVar2.getType().symbol.type);

        VariableDeclaration globalVar3 = program.getGlobals().get(2);
        assertFalse(globalVar3.isConstant());
        assertEquals("k", globalVar3.getName().lexeme);
        assertEquals(TokenTypes.STRING, globalVar3.getType().symbol.type);
    }
}

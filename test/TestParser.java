import static org.junit.Assert.*;

import compiler.Parser.ASTNodes.Statements.Statements.*;
import compiler.Parser.ASTNodes.Types.NumType;
import org.junit.Test;
import java.io.StringReader;
import java.util.ArrayList;

import compiler.Lexer.*;
import compiler.Parser.*;
import compiler.Parser.ASTNodes.*;
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
//        assertTrue(constant.getValue() instanceof ConstVal);
        assertTrue(constant.getValue() instanceof ParenthesesTerm);
        ParenthesesTerm parenthesesTerm = (ParenthesesTerm) constant.getValue();
        assertTrue(parenthesesTerm.getExpression() instanceof ConstVal);

        ConstVal constVal = (ConstVal) parenthesesTerm.getExpression();
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
//        assertTrue(binaryExpr.getLeftTerm() instanceof IdentifierAccess);
//        assertTrue(binaryExpr.getRightTerm() instanceof IdentifierAccess);
        assertTrue(binaryExpr.getLeftTerm() instanceof ParenthesesTerm);
        assertTrue(binaryExpr.getRightTerm() instanceof ParenthesesTerm);
        ParenthesesTerm leftParentheses = (ParenthesesTerm) binaryExpr.getLeftTerm();
        ParenthesesTerm rightParentheses = (ParenthesesTerm) binaryExpr.getRightTerm();
        assertTrue(leftParentheses.getExpression() instanceof IdentifierAccess);
        assertTrue(rightParentheses.getExpression() instanceof IdentifierAccess);

        IdentifierAccess leftAccess = (IdentifierAccess) leftParentheses.getExpression();
        IdentifierAccess rightAccess = (IdentifierAccess) rightParentheses.getExpression();
        assertEquals("a", leftAccess.getIdentifier().lexeme);
        assertEquals("b", rightAccess.getIdentifier().lexeme);
		assertNotNull(binaryExpr.getOperator());
        BinaryOperator plusOp = (BinaryOperator) binaryExpr.getOperator();
        assertEquals(TokenTypes.PLUS, plusOp.getSymbol().type);
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

        VariableDeclaration globalVar1 = program.getGlobals().getFirst();
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

    @Test
    public void testRecordInstantiation() throws Exception {
        String input = """
                fun main() {
                    p Product = Product(1, "Phone", 699);
                    ph Phone = Phone("iPhone 47 Pro Max XL Slim");
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
        assertEquals(0, program.getRecords().size());
        assertEquals(0, program.getGlobals().size());
        assertEquals(1, program.getFunctions().size());

        FunctionDefinition function = program.getFunctions().getFirst();
        assertEquals("main", function.getName().lexeme);
        assertEquals(TokenTypes.VOID, function.getReturnType().symbol.type);
        assertTrue(function.isVoidReturnType());
        assertEquals(0, function.getParamDefinitions().size());

        assertNotNull(function.getBlock());
        Block block = (Block) function.getBlock();
        assertEquals(2, block.getStatements().size());

        Statement statement1 = block.getStatements().getFirst();
        assertTrue(statement1 instanceof VariableDeclaration);
        VariableDeclaration varDecl1 = (VariableDeclaration) statement1;
        assertFalse(varDecl1.isConstant());
        assertEquals("p", varDecl1.getName().lexeme);
        assertEquals(TokenTypes.RECORD, varDecl1.getType().symbol.type);

//        assertTrue(varDecl1.getValue() instanceof NewRecord);
        assertTrue(varDecl1.getValue() instanceof ParenthesesTerm);
        ParenthesesTerm parenthesesTerm = (ParenthesesTerm) varDecl1.getValue();
        assertTrue(parenthesesTerm.getExpression() instanceof NewRecord);
        NewRecord newRecord1 = (NewRecord) parenthesesTerm.getExpression();
        assertEquals("Product", newRecord1.getIdentifier().lexeme);

        ArrayList<ParamCall> args1 = newRecord1.getTerms();
        assertEquals(3, args1.size());
//        assertTrue(args1.getFirst().getParamExpression() instanceof ConstVal);
        assertTrue(args1.getFirst().getParamExpression() instanceof ParenthesesTerm);
        ParenthesesTerm parenthesesTerm1 = (ParenthesesTerm) args1.getFirst().getParamExpression();
        assertTrue(parenthesesTerm1.getExpression() instanceof ConstVal);

        ConstVal constVal1 = (ConstVal) parenthesesTerm1.getExpression();
        assertEquals(1, constVal1.getValue());
        assertEquals(TokenTypes.INT_LITERAL, constVal1.getSymbol().type);

//        assertTrue(args1.get(1).getParamExpression() instanceof ConstVal);
        assertTrue(args1.get(1).getParamExpression() instanceof ParenthesesTerm);
        ParenthesesTerm parenthesesTerm2 = (ParenthesesTerm) args1.get(1).getParamExpression();
        assertTrue(parenthesesTerm2.getExpression() instanceof ConstVal);
        ConstVal constVal2 = (ConstVal) parenthesesTerm2.getExpression();
        assertEquals("Phone", constVal2.getValue());
        assertEquals(TokenTypes.STRING_LITERAL, constVal2.getSymbol().type);

//        assertTrue(args1.get(2).getParamExpression() instanceof ConstVal);
        assertTrue(args1.get(2).getParamExpression() instanceof ParenthesesTerm);
        ParenthesesTerm parenthesesTerm3 = (ParenthesesTerm) args1.get(2).getParamExpression();
        assertTrue(parenthesesTerm3.getExpression() instanceof ConstVal);
        ConstVal constVal3 = (ConstVal) parenthesesTerm3.getExpression();
        assertEquals(699, constVal3.getValue());
        assertEquals(TokenTypes.INT_LITERAL, constVal3.getSymbol().type);

        Statement statement2 = block.getStatements().get(1);
        assertTrue(statement2 instanceof VariableDeclaration);
        VariableDeclaration varDecl2 = (VariableDeclaration) statement2;
        assertFalse(varDecl2.isConstant());

        assertEquals("ph", varDecl2.getName().lexeme);
        assertEquals(TokenTypes.RECORD, varDecl2.getType().symbol.type);
//        assertTrue(varDecl2.getValue() instanceof NewRecord);
        assertTrue(varDecl2.getValue() instanceof ParenthesesTerm);
        ParenthesesTerm parenthesesTerm4 = (ParenthesesTerm) varDecl2.getValue();
        assertTrue(parenthesesTerm4.getExpression() instanceof NewRecord);

        NewRecord newRecord2 = (NewRecord) parenthesesTerm4.getExpression();
        assertEquals("Phone", newRecord2.getIdentifier().lexeme);
        ArrayList<ParamCall> args2 = newRecord2.getTerms();
        assertEquals(1, args2.size());
//        assertTrue(args2.getFirst().getParamExpression() instanceof ConstVal);
        assertTrue(args2.getFirst().getParamExpression() instanceof ParenthesesTerm);
        ParenthesesTerm parenthesesTerm5 = (ParenthesesTerm) args2.getFirst().getParamExpression();
        assertTrue(parenthesesTerm5.getExpression() instanceof ConstVal);
        ConstVal constVal4 = (ConstVal) parenthesesTerm5.getExpression();
        assertEquals("iPhone 47 Pro Max XL Slim", constVal4.getValue());
        assertEquals(TokenTypes.STRING_LITERAL, constVal4.getSymbol().type);
    }


    @Test
    public void testMultipleFunctions() throws Exception {
        String input = """
        fun add(a int, b int) int {
            return a + b;
        }

        fun subtract(a int, b int) int {
            return a - b;
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
        assertEquals(0, program.getRecords().size());
        assertEquals(2, program.getFunctions().size());

        assertEquals(0, program.getGlobals().size());

        FunctionDefinition function1 = program.getFunctions().getFirst();
        assertEquals("add", function1.getName().lexeme);
        assertEquals(TokenTypes.INT, function1.getReturnType().symbol.type);


        FunctionDefinition function2 = program.getFunctions().get(1);
        assertEquals("subtract", function2.getName().lexeme);
        assertEquals(TokenTypes.INT, function2.getReturnType().symbol.type);
    }

    @Test
    public void testIfElseStatement() throws Exception {
        String input = """
                fun main() {
                    if (a > b) {
                        return a;
                    } else {
                        return b;
                    }
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
        assertEquals(0, program.getRecords().size());
        assertEquals(1, program.getFunctions().size());
        assertEquals(0, program.getGlobals().size());

        FunctionDefinition function = program.getFunctions().getFirst();
        assertEquals("main", function.getName().lexeme);
        assertEquals(TokenTypes.VOID, function.getReturnType().symbol.type);

        assertNotNull(function.getBlock());
        Block block = (Block) function.getBlock();
        assertEquals(1, block.getStatements().size());

        Statement statement = block.getStatements().getFirst();
        assertTrue(statement instanceof IfStatement);
        IfStatement ifStmt = (IfStatement) statement;
        assertTrue(ifStmt.getCondition() instanceof BinaryExpression);
        BinaryExpression condition = (BinaryExpression) ifStmt.getCondition();

//        assertTrue(condition.getLeftTerm() instanceof IdentifierAccess);
        // Changed: we now always wrap the terms in parentheses, this allows us to have more complex expression
        assertTrue(condition.getLeftTerm() instanceof ParenthesesTerm);
        ParenthesesTerm parenthesesTerm = (ParenthesesTerm) condition.getLeftTerm();
        assertTrue(parenthesesTerm.getExpression() instanceof IdentifierAccess);

//        assertTrue(condition.getRightTerm() instanceof IdentifierAccess);
        assertTrue(condition.getRightTerm() instanceof ParenthesesTerm);
        ParenthesesTerm parenthesesTerm2 = (ParenthesesTerm) condition.getRightTerm();
        assertTrue(parenthesesTerm2.getExpression() instanceof IdentifierAccess);


        IdentifierAccess leftAccess = (IdentifierAccess) parenthesesTerm.getExpression();
        IdentifierAccess rightAccess = (IdentifierAccess) parenthesesTerm2.getExpression();
        assertEquals("a", leftAccess.getIdentifier().lexeme);
        assertEquals("b", rightAccess.getIdentifier().lexeme);
		assertNotNull(condition.getOperator());
        BinaryOperator greaterOp = (BinaryOperator) condition.getOperator();
        assertEquals(TokenTypes.GREATER_THAN, greaterOp.getSymbol().type);

        assertNotNull(ifStmt.getThenBlock());
        Block thenBlock = ifStmt.getThenBlock();
        assertEquals(0, thenBlock.getStatements().size()); // no statements in then block because it's a return statement
        // TODO: maybe change the way we handle return statements in the parser
        Statement thenStatement = thenBlock.getReturnStatement();
        assertTrue(thenStatement instanceof ReturnStatement);
        ReturnStatement returnStmt = (ReturnStatement) thenStatement;

//        assertTrue(returnStmt.getExpression() instanceof IdentifierAccess);
        assertTrue(returnStmt.getExpression() instanceof ParenthesesTerm);
        ParenthesesTerm returnParentheses = (ParenthesesTerm) returnStmt.getExpression();
        assertTrue(returnParentheses.getExpression() instanceof IdentifierAccess);

        IdentifierAccess returnAccess = (IdentifierAccess) returnParentheses.getExpression();
        assertEquals("a", returnAccess.getIdentifier().lexeme);

        assertNotNull(ifStmt.getElseBlock());
        Block elseBlock = ifStmt.getElseBlock();
        assertEquals(0, elseBlock.getStatements().size());
        Statement elseStatement = elseBlock.getReturnStatement();
        assertTrue(elseStatement instanceof ReturnStatement);
        ReturnStatement elseReturnStmt = (ReturnStatement) elseStatement;
//        assertTrue(elseReturnStmt.getExpression() instanceof IdentifierAccess);
        assertTrue(elseReturnStmt.getExpression() instanceof ParenthesesTerm);
        ParenthesesTerm elseReturnParentheses = (ParenthesesTerm) elseReturnStmt.getExpression();
        assertTrue(elseReturnParentheses.getExpression() instanceof IdentifierAccess);
        IdentifierAccess elseReturnAccess = (IdentifierAccess) elseReturnParentheses.getExpression();
        assertEquals("b", elseReturnAccess.getIdentifier().lexeme);
    }


    // free x
    @Test
    public void testFreeingVariable() throws Exception {
        String input = """
                fun main() {
                    free x;
                }
                """;
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        ASTNode node = parser.getAST();

        assertNotNull(node);
        assertTrue(node instanceof Program);
        Program program = (Program) node;

        assertEquals(1, program.getFunctions().size());

        FunctionDefinition function = program.getFunctions().getFirst();
        assertEquals("main", function.getName().lexeme);

        assertNotNull(function.getBlock());
        Block block = (Block) function.getBlock();
        assertEquals(1, block.getStatements().size());
        Statement statement = block.getStatements().getFirst();

        assertTrue(statement instanceof FreeStatement);
        FreeStatement freeStmt = (FreeStatement) statement;

		assertNotNull(freeStmt.getIdentifierAccess());
        IdentifierAccess identifierAccess = freeStmt.getIdentifierAccess();
        assertEquals("x", identifierAccess.getIdentifier().lexeme);
    }

    @Test
    public void testWhileLoop() throws Exception {
        String input = """
                fun main() {
                    while (a < b) {
                        a = a + 1;
                    }
                }
                """;

        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);

        ASTNode node = parser.getAST();
        assertNotNull(node);

        assertTrue(node instanceof Program);
        Program program = (Program) node;

        assertEquals(1, program.getFunctions().size());


        FunctionDefinition function = program.getFunctions().getFirst();
        assertEquals("main", function.getName().lexeme);
        assertEquals(TokenTypes.VOID, function.getReturnType().symbol.type);

        assertNotNull(function.getBlock());
        Block block = (Block) function.getBlock();
        assertEquals(1, block.getStatements().size());

        Statement statement = block.getStatements().getFirst();
        assertTrue(statement instanceof WhileLoop);
        WhileLoop whileLoop = (WhileLoop) statement;
        assertTrue(whileLoop.getCondition() instanceof BinaryExpression);
        BinaryExpression condition = (BinaryExpression) whileLoop.getCondition();
//        assertTrue(condition.getLeftTerm() instanceof IdentifierAccess);
//        assertTrue(condition.getRightTerm() instanceof IdentifierAccess);
        assertTrue(condition.getLeftTerm() instanceof ParenthesesTerm);
        assertTrue(condition.getRightTerm() instanceof ParenthesesTerm);

        ParenthesesTerm leftParentheses = (ParenthesesTerm) condition.getLeftTerm();
        ParenthesesTerm rightParentheses = (ParenthesesTerm) condition.getRightTerm();
        assertTrue(leftParentheses.getExpression() instanceof IdentifierAccess);
        assertTrue(rightParentheses.getExpression() instanceof IdentifierAccess);

        IdentifierAccess leftAccess = (IdentifierAccess) leftParentheses.getExpression();
        IdentifierAccess rightAccess = (IdentifierAccess) rightParentheses.getExpression();
        assertEquals("a", leftAccess.getIdentifier().lexeme);
        assertEquals("b", rightAccess.getIdentifier().lexeme);
		assertNotNull(condition.getOperator());
        BinaryOperator lessOp = (BinaryOperator) condition.getOperator();
        assertEquals(TokenTypes.LESS_THAN, lessOp.getSymbol().type);

        assertNotNull(whileLoop.getBlock());
        Block whileBlock = whileLoop.getBlock();
        assertEquals(1, whileBlock.getStatements().size());
        Statement whileStatement = whileBlock.getStatements().getFirst();
        assertTrue(whileStatement instanceof VariableAssignment);
        VariableAssignment assignment = (VariableAssignment) whileStatement;
        assertTrue(assignment.getAccess() instanceof IdentifierAccess);
        IdentifierAccess access = (IdentifierAccess) assignment.getAccess();
        assertEquals("a", access.getIdentifier().lexeme);
        assertTrue(assignment.getExpression() instanceof BinaryExpression);

        BinaryExpression binaryExpr = (BinaryExpression) assignment.getExpression();
//        assertTrue(binaryExpr.getLeftTerm() instanceof IdentifierAccess);
        assertTrue(binaryExpr.getLeftTerm() instanceof ParenthesesTerm);
        ParenthesesTerm leftParentheses2 = (ParenthesesTerm) binaryExpr.getLeftTerm();
        assertTrue(leftParentheses2.getExpression() instanceof IdentifierAccess);

//        assertTrue(binaryExpr.getRightTerm() instanceof ConstVal);
        assertTrue(binaryExpr.getRightTerm() instanceof ParenthesesTerm);
        ParenthesesTerm rightParentheses2 = (ParenthesesTerm) binaryExpr.getRightTerm();
        assertTrue(rightParentheses2.getExpression() instanceof ConstVal);

        IdentifierAccess leftAccess2 = (IdentifierAccess) leftParentheses2.getExpression();

        assertEquals("a", leftAccess2.getIdentifier().lexeme);
        ConstVal constVal = (ConstVal) rightParentheses2.getExpression();

        assertEquals(1, constVal.getValue());
        assertEquals(TokenTypes.INT_LITERAL, constVal.getSymbol().type);
		assertNotNull(binaryExpr.getOperator());
        BinaryOperator plusOp = (BinaryOperator) binaryExpr.getOperator();
        assertEquals(TokenTypes.PLUS, plusOp.getSymbol().type);
    }


    @Test
    public void testChainedAccess() throws Exception {
        // i int = people[0].locationHistory[3].y;
        // RecordAccess:
        //  ArrayAccess:
        //    RecordAccess:
        //      ArrayAccess:
        //        IdentifierAccess: people
        //        Integer, 0
        //      Identifier: locationHistory
        //    Integer, 3
        //  Identifier: y
        String input = """
                fun main() {
                    i int = people[0].locationHistory[3].y;
                }
                """;
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        ASTNode node = parser.getAST();
        assertNotNull(node);

        assertTrue(node instanceof Program);
        Program program = (Program) node;
        assertEquals(1, program.getFunctions().size());

        FunctionDefinition function = program.getFunctions().getFirst();
        assertEquals("main", function.getName().lexeme);

        assertNotNull(function.getBlock());
        Block block = (Block) function.getBlock();
        assertEquals(1, block.getStatements().size());
        Statement statement = block.getStatements().getFirst();
        assertTrue(statement instanceof VariableDeclaration);
        VariableDeclaration varDecl = (VariableDeclaration) statement;
        assertFalse(varDecl.isConstant());
        assertEquals("i", varDecl.getName().lexeme);
        assertEquals(TokenTypes.INT, varDecl.getType().symbol.type);
        
        
//        assertTrue(varDecl.getValue() instanceof RecordAccess);
        assertTrue(varDecl.getValue() instanceof ParenthesesTerm);
        ParenthesesTerm parenthesesTerm = (ParenthesesTerm) varDecl.getValue();
        assertTrue(parenthesesTerm.getExpression() instanceof RecordAccess);
        RecordAccess access = (RecordAccess) parenthesesTerm.getExpression();

        assertTrue(access.getHeadAccess() instanceof ArrayAccess);
        ArrayAccess locHistArray = (ArrayAccess) access.getHeadAccess();
        
        assertTrue(locHistArray.getHeadAccess() instanceof RecordAccess);
        RecordAccess locHist = (RecordAccess) locHistArray.getHeadAccess();

        assertTrue(locHist.getHeadAccess() instanceof ArrayAccess);
        ArrayAccess peopleArray = (ArrayAccess) locHist.getHeadAccess();

        assertTrue(peopleArray.getHeadAccess() instanceof IdentifierAccess);
        IdentifierAccess peopleAccess = (IdentifierAccess) peopleArray.getHeadAccess();
        assertEquals("people", peopleAccess.getIdentifier().lexeme);

//        assertTrue(peopleArray.getIndexExpression() instanceof ConstVal);
        assertTrue(peopleArray.getIndexExpression() instanceof ParenthesesTerm);
        ParenthesesTerm peopleParentheses = (ParenthesesTerm) peopleArray.getIndexExpression();
        assertTrue(peopleParentheses.getExpression() instanceof ConstVal);
        ConstVal peopleIndex = (ConstVal) peopleParentheses.getExpression();
        assertEquals(0, peopleIndex.getValue());
        assertEquals(TokenTypes.INT_LITERAL, peopleIndex.getSymbol().type);

        assertEquals("locationHistory", locHist.getIdentifier().lexeme);
//        assertTrue(locHistArray.getIndexExpression() instanceof ConstVal);
        assertTrue(locHistArray.getIndexExpression() instanceof ParenthesesTerm);
        ParenthesesTerm locHistParentheses = (ParenthesesTerm) locHistArray.getIndexExpression();
        assertTrue(locHistParentheses.getExpression() instanceof ConstVal);
        ConstVal locHistIndex = (ConstVal) locHistParentheses.getExpression();
        assertEquals(3, locHistIndex.getValue());
        assertEquals(TokenTypes.INT_LITERAL, locHistIndex.getSymbol().type);

        assertEquals("y", access.getIdentifier().lexeme);
    }

    @Test
    public void testForLoop() throws Exception {
        String input = """
                fun main() {
                    i int;
                    for (i, 0, 10, 1) {
                        writeln(i);
                    }
                }
                """;

        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);

        ASTNode node = parser.getAST();
        assertNotNull(node);

        assertTrue(node instanceof Program);
        Program program = (Program) node;
        assertEquals(1, program.getFunctions().size());

        FunctionDefinition function = program.getFunctions().getFirst();
        assertEquals("main", function.getName().lexeme);

        assertNotNull(function.getBlock());
        Block block = (Block) function.getBlock();
        assertEquals(2, block.getStatements().size());

        Statement statement = block.getStatements().getFirst();
        assertTrue(statement instanceof VariableDeclaration);

        VariableDeclaration decl = (VariableDeclaration) statement;
        assertFalse(decl.isConstant());
        assertEquals("i", decl.getName().lexeme);
        assertEquals(TokenTypes.INT, decl.getType().symbol.type);

        assertTrue(block.getStatements().get(1) instanceof ForLoop);
        ForLoop forLoop = (ForLoop) block.getStatements().get(1);
		assertNotNull(forLoop.getVariable());


        Expression startExpression = forLoop.getStart();
        assertNotNull(startExpression);
        assertTrue(startExpression instanceof ParenthesesTerm);
        ParenthesesTerm startParentheses = (ParenthesesTerm) startExpression;
        assertTrue(startParentheses.getExpression() instanceof ConstVal);
        ConstVal startConst = (ConstVal) startParentheses.getExpression();
        assertEquals(0, startConst.getValue());

        Expression endExpression = forLoop.getEnd();
        assertNotNull(endExpression);
        assertTrue(endExpression instanceof ParenthesesTerm);
        ParenthesesTerm endParentheses = (ParenthesesTerm) endExpression;
        assertTrue(endParentheses.getExpression() instanceof ConstVal);
        ConstVal endConst = (ConstVal) endParentheses.getExpression();
        assertEquals(10, endConst.getValue());

        Expression stepExpression = forLoop.getStep();
        assertNotNull(stepExpression);
        assertTrue(stepExpression instanceof ParenthesesTerm);
        ParenthesesTerm stepParentheses = (ParenthesesTerm) stepExpression;
        assertTrue(stepParentheses.getExpression() instanceof ConstVal);
        ConstVal stepConst = (ConstVal) stepParentheses.getExpression();
        assertEquals(1, stepConst.getValue());

        assertNotNull(forLoop.getBlock());
        Block forBlock = (Block) forLoop.getBlock();
        assertEquals(1, forBlock.getStatements().size());
        Statement forStatement = forBlock.getStatements().getFirst();
        assertTrue(forStatement instanceof FunctionCall);
        FunctionCall functionCall = (FunctionCall) forStatement;
        assertEquals("writeln", functionCall.getIdentifier().lexeme);
        assertEquals(1, functionCall.getParameters().size());

//        assertTrue(functionCall.getParameters().getFirst().getParamExpression() instanceof IdentifierAccess);
        assertTrue(functionCall.getParameters().getFirst().getParamExpression() instanceof ParenthesesTerm);
        ParenthesesTerm parenthesesTerm = (ParenthesesTerm) functionCall.getParameters().getFirst().getParamExpression();
        assertTrue(parenthesesTerm.getExpression() instanceof IdentifierAccess);

        IdentifierAccess paramAccess = (IdentifierAccess) parenthesesTerm.getExpression();
        assertEquals("i", paramAccess.getIdentifier().lexeme);
        assertEquals(TokenTypes.IDENTIFIER, paramAccess.getIdentifier().type);
    }

    @Test
    public void testUnaryOperator() throws Exception {
        String input = """
                fun main() {
                    $ comment (idk why)
                    i int = 2132;
                    writeln(-i);
                }
                """;

        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);

        ASTNode node = parser.getAST();
        assertNotNull(node);

        assertTrue(node instanceof Program);
        Program program = (Program) node;

        assertEquals(1, program.getFunctions().size());

        FunctionDefinition function = program.getFunctions().getFirst();
        assertEquals("main", function.getName().lexeme);

        assertNotNull(function.getBlock());
        Block block = (Block) function.getBlock();
        assertEquals(2, block.getStatements().size());
        Statement statement = block.getStatements().getFirst();

        assertTrue(statement instanceof VariableDeclaration);
        VariableDeclaration decl = (VariableDeclaration) statement;
        assertFalse(decl.isConstant());
        assertEquals("i", decl.getName().lexeme);
        assertEquals(TokenTypes.INT, decl.getType().symbol.type);
//        assertTrue(decl.getValue() instanceof ConstVal);
        assertTrue(decl.getValue() instanceof ParenthesesTerm);
        ParenthesesTerm parenthesesTerm = (ParenthesesTerm) decl.getValue();
        assertTrue(parenthesesTerm.getExpression() instanceof ConstVal);
        ConstVal constVal = (ConstVal) parenthesesTerm.getExpression();
        assertEquals(2132, constVal.getValue());
        assertEquals(TokenTypes.INT_LITERAL, constVal.getSymbol().type);

        Statement statement2 = block.getStatements().get(1);
        assertTrue(statement2 instanceof FunctionCall);
        FunctionCall functionCall = (FunctionCall) statement2;
        assertEquals("writeln", functionCall.getIdentifier().lexeme);
        assertEquals(1, functionCall.getParameters().size());

        assertTrue(functionCall.getParameters().getFirst().getParamExpression() instanceof UnaryExpression);
        UnaryExpression unaryExpr = (UnaryExpression) functionCall.getParameters().getFirst().getParamExpression();
//        assertTrue(unaryExpr.getTerm() instanceof IdentifierAccess);
        assertTrue(unaryExpr.getTerm() instanceof ParenthesesTerm);
        ParenthesesTerm termParentheses = (ParenthesesTerm) unaryExpr.getTerm();
        assertTrue(termParentheses.getExpression() instanceof IdentifierAccess);

        IdentifierAccess idAccess = (IdentifierAccess) termParentheses.getExpression();
        assertEquals("i", idAccess.getIdentifier().lexeme);
		assertNotNull(unaryExpr.getOperator());
        UnaryOperator unaryOp = (UnaryOperator) unaryExpr.getOperator();
        assertEquals(TokenTypes.MINUS, unaryOp.getSymbol().type);
    }


    @Test
    public void testParenthesesTerm() throws Exception {
        String input = """
                fun main() {
                    writeln((a + b) * c);
                }
                """;

        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);

        ASTNode node = parser.getAST();
        assertNotNull(node);

        assertTrue(node instanceof Program);
        Program program = (Program) node;

        assertEquals(1, program.getFunctions().size());
        FunctionDefinition function = program.getFunctions().getFirst();
        assertEquals("main", function.getName().lexeme);

        assertNotNull(function.getBlock());
        Block block = (Block) function.getBlock();
        assertEquals(1, block.getStatements().size());
        Statement statement = block.getStatements().getFirst();

        assertTrue(statement instanceof FunctionCall);
        FunctionCall functionCall = (FunctionCall) statement;
        assertEquals("writeln", functionCall.getIdentifier().lexeme);

        assertEquals(1, functionCall.getParameters().size());
        assertTrue(functionCall.getParameters().getFirst().getParamExpression() instanceof BinaryExpression);
        BinaryExpression binaryExpr = (BinaryExpression) functionCall.getParameters().getFirst().getParamExpression();

        assertTrue(binaryExpr.getLeftTerm() instanceof ParenthesesTerm);
        ParenthesesTerm parenthesesTerm = (ParenthesesTerm) binaryExpr.getLeftTerm();
        System.out.println(parenthesesTerm.prettyPrint(0));
//        assertTrue(parenthesesTerm.getExpression() instanceof BinaryExpression);
        assertTrue(parenthesesTerm.getExpression() instanceof ParenthesesTerm);
        ParenthesesTerm parenthesesTerm2 = (ParenthesesTerm) parenthesesTerm.getExpression();
        assertTrue(parenthesesTerm2.getExpression() instanceof BinaryExpression);
        BinaryExpression innerBinaryExpr = (BinaryExpression) parenthesesTerm2.getExpression();

//        assertTrue(innerBinaryExpr.getLeftTerm() instanceof IdentifierAccess);
        assertTrue(innerBinaryExpr.getLeftTerm() instanceof ParenthesesTerm);
        ParenthesesTerm innerParentheses = (ParenthesesTerm) innerBinaryExpr.getLeftTerm();
        assertTrue(innerParentheses.getExpression() instanceof IdentifierAccess);

        IdentifierAccess leftAccess = (IdentifierAccess) innerParentheses.getExpression();
        assertEquals("a", leftAccess.getIdentifier().lexeme);
//        assertTrue(innerBinaryExpr.getRightTerm() instanceof IdentifierAccess);
        assertTrue(innerBinaryExpr.getRightTerm() instanceof ParenthesesTerm);
        ParenthesesTerm innerParentheses2 = (ParenthesesTerm) innerBinaryExpr.getRightTerm();
        assertTrue(innerParentheses2.getExpression() instanceof IdentifierAccess);
        IdentifierAccess rightAccess = (IdentifierAccess) innerParentheses2.getExpression();
        assertEquals("b", rightAccess.getIdentifier().lexeme);
		assertNotNull(innerBinaryExpr.getOperator());
        BinaryOperator plusOp = (BinaryOperator) innerBinaryExpr.getOperator();
        assertEquals(TokenTypes.PLUS, plusOp.getSymbol().type);

//        assertTrue(binaryExpr.getRightTerm() instanceof IdentifierAccess);
        assertTrue(binaryExpr.getRightTerm() instanceof ParenthesesTerm);
        ParenthesesTerm rightParentheses = (ParenthesesTerm) binaryExpr.getRightTerm();
        assertTrue(rightParentheses.getExpression() instanceof IdentifierAccess);

        IdentifierAccess rightTermAccess = (IdentifierAccess) rightParentheses.getExpression();
        assertEquals("c", rightTermAccess.getIdentifier().lexeme);
		assertNotNull(binaryExpr.getOperator());
        BinaryOperator mulOp = (BinaryOperator) binaryExpr.getOperator();
        assertEquals(TokenTypes.MULTIPLY, mulOp.getSymbol().type);
    }

    @Test
    public void testFunctionCallWithParams() throws Exception {
        String input = """
                fun main() {
                    writeln(square(value), p[1]);
                }
                """;
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);

        ASTNode node = parser.getAST();
        assertNotNull(node);

        assertTrue(node instanceof Program);
        Program program = (Program) node;

        assertEquals(1, program.getFunctions().size());
        FunctionDefinition function = program.getFunctions().getFirst();
        assertEquals("main", function.getName().lexeme);

        assertNotNull(function.getBlock());
        Block block = (Block) function.getBlock();
        assertEquals(1, block.getStatements().size());
        Statement statement = block.getStatements().getFirst();

        assertTrue(statement instanceof FunctionCall);
        FunctionCall functionCall = (FunctionCall) statement;
        assertEquals("writeln", functionCall.getIdentifier().lexeme);
        assertEquals(2, functionCall.getParameters().size());
//        assertTrue(functionCall.getParameters().getFirst().getParamExpression() instanceof FunctionCall);
        assertTrue(functionCall.getParameters().getFirst().getParamExpression() instanceof ParenthesesTerm);
        ParenthesesTerm parenthesesTerm = (ParenthesesTerm) functionCall.getParameters().getFirst().getParamExpression();
        assertTrue(parenthesesTerm.getExpression() instanceof FunctionCall);

        FunctionCall innerFunctionCall = (FunctionCall) parenthesesTerm.getExpression();
        assertEquals("square", innerFunctionCall.getIdentifier().lexeme);
        assertEquals(1, innerFunctionCall.getParameters().size());
//        assertTrue(innerFunctionCall.getParameters().getFirst().getParamExpression() instanceof IdentifierAccess);
        assertTrue(innerFunctionCall.getParameters().getFirst().getParamExpression() instanceof ParenthesesTerm);
        ParenthesesTerm innerParentheses = (ParenthesesTerm) innerFunctionCall.getParameters().getFirst().getParamExpression();
        assertTrue(innerParentheses.getExpression() instanceof IdentifierAccess);
        IdentifierAccess idAccess = (IdentifierAccess) innerParentheses.getExpression();

        assertEquals("value", idAccess.getIdentifier().lexeme);
//        assertTrue(functionCall.getParameters().get(1).getParamExpression() instanceof ArrayAccess);
        assertTrue(functionCall.getParameters().get(1).getParamExpression() instanceof ParenthesesTerm);
        ParenthesesTerm arrayParentheses = (ParenthesesTerm) functionCall.getParameters().get(1).getParamExpression();
        assertTrue(arrayParentheses.getExpression() instanceof ArrayAccess);

        ArrayAccess arrayAccess = (ArrayAccess) arrayParentheses.getExpression();
        assertTrue(arrayAccess.getHeadAccess() instanceof IdentifierAccess);
        IdentifierAccess arrayHeadAccess = (IdentifierAccess) arrayAccess.getHeadAccess();
        assertEquals("p", arrayHeadAccess.getIdentifier().lexeme);
//        assertTrue(arrayAccess.getIndexExpression() instanceof ConstVal);
        assertTrue(arrayAccess.getIndexExpression() instanceof ParenthesesTerm);
        ParenthesesTerm indexParentheses = (ParenthesesTerm) arrayAccess.getIndexExpression();
        assertTrue(indexParentheses.getExpression() instanceof ConstVal);

        ConstVal constVal = (ConstVal) indexParentheses.getExpression();
        assertEquals(1, constVal.getValue());
        assertEquals(TokenTypes.INT_LITERAL, constVal.getSymbol().type);
    }

    @Test
    public void testArrayExpression() throws Exception {
        String input = """
                fun main() {
                    c int[] = array [5] of int;
                }
                """;

        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        ASTNode node = parser.getAST();
        assertNotNull(node);

        assertTrue(node instanceof Program);
        Program program = (Program) node;

        assertEquals(1, program.getFunctions().size());
        FunctionDefinition function = program.getFunctions().getFirst();
        assertEquals("main", function.getName().lexeme);
        assertNotNull(function.getBlock());

        Block block = (Block) function.getBlock();
        assertEquals(1, block.getStatements().size());
        Statement statement = block.getStatements().getFirst();
        assertTrue(statement instanceof VariableDeclaration);
        VariableDeclaration varDecl = (VariableDeclaration) statement;
        assertFalse(varDecl.isConstant());
        assertEquals("c", varDecl.getName().lexeme);
        assertEquals(TokenTypes.INT, varDecl.getType().symbol.type);
        assertTrue(varDecl.getType().isList);

        assertTrue(varDecl.getValue() instanceof ArrayExpression);
        ArrayExpression arrayExpr = (ArrayExpression) varDecl.getValue();
//        assertTrue(arrayExpr.getSizeExpression() instanceof ConstVal);
        assertTrue(arrayExpr.getSizeExpression() instanceof ParenthesesTerm);
        ParenthesesTerm parenthesesTerm = (ParenthesesTerm) arrayExpr.getSizeExpression();
        assertTrue(parenthesesTerm.getExpression() instanceof ConstVal);

        ConstVal sizeVal = (ConstVal) parenthesesTerm.getExpression();
        assertEquals(5, sizeVal.getValue());
        assertEquals(TokenTypes.INT_LITERAL, sizeVal.getSymbol().type);

        assertTrue(arrayExpr.getType() instanceof NumType);
        NumType arrayType = (NumType) arrayExpr.getType();
        assertEquals(TokenTypes.INT, arrayType.getSymbol().type);

    }


    @Test(expected = SyntaxErrorException.class)
    public void testError() throws Exception {
        String input = """
            fun main() {
                a = Point(;
            }
            """;

        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        parser.parseProgram();
    }

    @Test
    public void testForLoopVoidRetStatement() throws Exception {
        String input = """
                fun main() int {
                    i int;
                    for (i, 0.0, 10, 1) {
                        writeln(i);
                        return;
                    }
                }
                """;

        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);

        ASTNode node = parser.getAST();
        assertNotNull(node);

        assertTrue(node instanceof Program);
        Program program = (Program) node;
        assertEquals(1, program.getFunctions().size());

        FunctionDefinition function = program.getFunctions().getFirst();
        assertEquals("main", function.getName().lexeme);

        assertNotNull(function.getBlock());
        Block block = (Block) function.getBlock();
        assertEquals(2, block.getStatements().size());

        Statement statement = block.getStatements().getFirst();
        assertTrue(statement instanceof VariableDeclaration);

        VariableDeclaration decl = (VariableDeclaration) statement;
        assertFalse(decl.isConstant());
        assertEquals("i", decl.getName().lexeme);
        assertEquals(TokenTypes.INT, decl.getType().symbol.type);

        assertTrue(block.getStatements().get(1) instanceof ForLoop);
        ForLoop forLoop = (ForLoop) block.getStatements().get(1);
        assertNotNull(forLoop.getVariable());

        assertNotNull(forLoop.getBlock());
        Block forBlock = (Block) forLoop.getBlock();
        assertEquals(1, forBlock.getStatements().size());
        Statement forStatement = forBlock.getStatements().getFirst();
        assertTrue(forStatement instanceof FunctionCall);
        FunctionCall functionCall = (FunctionCall) forStatement;
        assertEquals("writeln", functionCall.getIdentifier().lexeme);
        assertEquals(1, functionCall.getParameters().size());

        // check that the return statement has a null expression
        assertTrue(forBlock.getReturnStatement() instanceof ReturnStatement);
        ReturnStatement returnStmt = (ReturnStatement) forBlock.getReturnStatement();
        assertNull(returnStmt.getExpression());
    }

    @Test(expected = SyntaxErrorException.class)
    public void testIncorrectConstantDecl() throws Exception {
        String input = """
                final string = "myString";
                fun main() {
                    writeln(string);
                }
                """;

        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        parser.parseProgram();
    }

    @Test(expected = SyntaxErrorException.class)
    public void testIncorrectConstantDeclNotMarkedFinal() throws Exception {
        String input = """
                string = "myString";
                fun main() {
                    writeln(string);
                }
                """;

        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        parser.parseProgram();
    }

    @Test(expected = SyntaxErrorException.class)
    public void testIncorrectRecDefAlone() throws Exception {
        String input = """
                recordName rec {
                    fieldName1 int;
                    fieldName2 string;
                }
                """;

        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        parser.parseProgram();
    }

    @Test(expected = SyntaxErrorException.class)
    public void testIncorrectRecDef() throws Exception {
        String input = """
                final myString string = "myString";
                recordName rec {
                    fieldName1 int;
                    fieldName2 string;
                }
                
                fun main() {
                    writeln(myString);
                }
                """;

        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        parser.parseProgram();
    }


    @Test(expected = SyntaxErrorException.class)
    public void testIncorrectGlobalDefAlone() throws Exception {
        String input = """
                int = 30;
                """;

        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        parser.parseProgram();
    }

    @Test(expected = SyntaxErrorException.class)
    public void testIncorrectGlobalDef() throws Exception {
        String input = """
                final myString string = "myString";
                RecordName rec {
                    fieldName1 int;
                    fieldName2 string;
                }
                int = 30;
                
                fun main() {
                    writeln(myString);
                }
                """;

        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        parser.parseProgram();
    }

    @Test(expected = SyntaxErrorException.class)
    public void testIncorrectDefinitionsOrder() throws Exception {
        String input = """
                myString string = "myString";
                fun main() {
                    writeln(myString);
                }
                RecordName rec {
                    fieldName1 int;
                    fieldName2 string;
                }
                """;

        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        parser.parseProgram();
    }

    @Test(expected = SyntaxErrorException.class)
    public void testIncorrectDefinitionsOrder2() throws Exception {
        String input = """
                fun main() {
                    writeln(myString);
                }
                RecordName rec {
                    fieldName1 int;
                    fieldName2 string;
                }
                final test int = 4;
                myString string = "myString";
                """;

        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        parser.parseProgram();
    }
}

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
        assertTrue(constant.getValue() instanceof ConstVal);

        ConstVal constVal = (ConstVal) constant.getValue();
        assertEquals(3, constVal.getValue());
        assertEquals(TokenTypes.INT_LITERAL, constVal.getSymbol().type);
    }

    @Test
    public void testFunctionDefinition() throws Exception {
//        String input = """
//        fun add(a int, b int) int {
//            return a + b;
//        }
//        """;
        // TODO: change back to post typing
        String input = """
        fun add(int a, int b) int {
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

        Statement statement1 = block.getStatements().get(0);
        assertTrue(statement1 instanceof VariableDeclaration);
        VariableDeclaration varDecl1 = (VariableDeclaration) statement1;
        assertFalse(varDecl1.isConstant());
        assertEquals("p", varDecl1.getName().lexeme);
        assertEquals(TokenTypes.RECORD, varDecl1.getType().symbol.type);

        assertTrue(varDecl1.getValue() instanceof NewRecord);
        NewRecord newRecord1 = (NewRecord) varDecl1.getValue();
        assertEquals("Product", newRecord1.getIdentifier().lexeme);

        ArrayList<ParamCall> args1 = newRecord1.getTerms();
        assertEquals(3, args1.size());
        assertTrue(args1.getFirst().getParamExpression() instanceof ConstVal);
        ConstVal constVal1 = (ConstVal) args1.getFirst().getParamExpression();
        assertEquals(1, constVal1.getValue());
        assertEquals(TokenTypes.INT_LITERAL, constVal1.getSymbol().type);

        assertTrue(args1.get(1).getParamExpression() instanceof ConstVal);
        ConstVal constVal2 = (ConstVal) args1.get(1).getParamExpression();
        assertEquals("Phone", constVal2.getValue());
        assertEquals(TokenTypes.STRING_LITERAL, constVal2.getSymbol().type);

        assertTrue(args1.get(2).getParamExpression() instanceof ConstVal);
        ConstVal constVal3 = (ConstVal) args1.get(2).getParamExpression();
        assertEquals(699, constVal3.getValue());
        assertEquals(TokenTypes.INT_LITERAL, constVal3.getSymbol().type);

        Statement statement2 = block.getStatements().get(1);
        assertTrue(statement2 instanceof VariableDeclaration);
        VariableDeclaration varDecl2 = (VariableDeclaration) statement2;
        assertFalse(varDecl2.isConstant());

        assertEquals("ph", varDecl2.getName().lexeme);
        assertEquals(TokenTypes.RECORD, varDecl2.getType().symbol.type);
        assertTrue(varDecl2.getValue() instanceof NewRecord);
        NewRecord newRecord2 = (NewRecord) varDecl2.getValue();
        assertEquals("Phone", newRecord2.getIdentifier().lexeme);
        ArrayList<ParamCall> args2 = newRecord2.getTerms();
        assertEquals(1, args2.size());
        assertTrue(args2.getFirst().getParamExpression() instanceof ConstVal);
        ConstVal constVal4 = (ConstVal) args2.getFirst().getParamExpression();
        assertEquals("iPhone 47 Pro Max XL Slim", constVal4.getValue());
        assertEquals(TokenTypes.STRING_LITERAL, constVal4.getSymbol().type);
    }


    @Test
    public void testMultipleFunctions() throws Exception {
//        String input = """
//        fun add(a int, b int) int {
//            return a + b;
//        }
//
//        fun subtract(a int, b int) int {
//            return a - b;
//        }
//        """;
        // TODO: change back to post typing
        String input = """
        fun add(int a, int b) int {
            return a + b;
        }
        
        fun subtract(int a, int b) int {
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

        Statement statement = block.getStatements().get(0);
        assertTrue(statement instanceof IfStatement);
        IfStatement ifStmt = (IfStatement) statement;
        assertTrue(ifStmt.getCondition() instanceof BinaryExpression);
        BinaryExpression condition = (BinaryExpression) ifStmt.getCondition();
        assertTrue(condition.getLeftTerm() instanceof IdentifierAccess);
        assertTrue(condition.getRightTerm() instanceof IdentifierAccess);
        IdentifierAccess leftAccess = (IdentifierAccess) condition.getLeftTerm();
        IdentifierAccess rightAccess = (IdentifierAccess) condition.getRightTerm();
        assertEquals("a", leftAccess.getIdentifier().lexeme);
        assertEquals("b", rightAccess.getIdentifier().lexeme);
        assertTrue(condition.getOperator() instanceof BinaryOperator);
        BinaryOperator greaterOp = (BinaryOperator) condition.getOperator();
        assertEquals(TokenTypes.GREATER_THAN, greaterOp.getOperator().type);

        assertNotNull(ifStmt.getThenBlock());
        Block thenBlock = ifStmt.getThenBlock();
        assertEquals(0, thenBlock.getStatements().size()); // no statements in then block because it's a return statement
        // TODO: maybe change the way we handle return statements in the parser
        Statement thenStatement = thenBlock.getReturnStatement();
        assertTrue(thenStatement instanceof ReturnStatement);
        ReturnStatement returnStmt = (ReturnStatement) thenStatement;
        assertTrue(returnStmt.getExpression() instanceof IdentifierAccess);
        IdentifierAccess returnAccess = (IdentifierAccess) returnStmt.getExpression();
        assertEquals("a", returnAccess.getIdentifier().lexeme);

        assertNotNull(ifStmt.getElseBlock());
        Block elseBlock = ifStmt.getElseBlock();
        assertEquals(0, elseBlock.getStatements().size());
        Statement elseStatement = elseBlock.getReturnStatement();
        assertTrue(elseStatement instanceof ReturnStatement);
        ReturnStatement elseReturnStmt = (ReturnStatement) elseStatement;
        assertTrue(elseReturnStmt.getExpression() instanceof IdentifierAccess);
        IdentifierAccess elseReturnAccess = (IdentifierAccess) elseReturnStmt.getExpression();
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
        assertTrue(condition.getLeftTerm() instanceof IdentifierAccess);
        assertTrue(condition.getRightTerm() instanceof IdentifierAccess);
        IdentifierAccess leftAccess = (IdentifierAccess) condition.getLeftTerm();
        IdentifierAccess rightAccess = (IdentifierAccess) condition.getRightTerm();
        assertEquals("a", leftAccess.getIdentifier().lexeme);
        assertEquals("b", rightAccess.getIdentifier().lexeme);
        assertTrue(condition.getOperator() instanceof BinaryOperator);
        BinaryOperator lessOp = (BinaryOperator) condition.getOperator();
        assertEquals(TokenTypes.LESS_THAN, lessOp.getOperator().type);

        assertNotNull(whileLoop.getBlock());
        Block whileBlock = whileLoop.getBlock();
        assertEquals(1, whileBlock.getStatements().size());
        Statement whileStatement = whileBlock.getStatements().getFirst();
        assertTrue(whileStatement instanceof VariableAssigment);
        VariableAssigment assignment = (VariableAssigment) whileStatement;
        assertTrue(assignment.getAccess() instanceof IdentifierAccess);
        IdentifierAccess access = (IdentifierAccess) assignment.getAccess();
        assertEquals("a", access.getIdentifier().lexeme);
        assertTrue(assignment.getExpression() instanceof BinaryExpression);

        BinaryExpression binaryExpr = (BinaryExpression) assignment.getExpression();
        assertTrue(binaryExpr.getLeftTerm() instanceof IdentifierAccess);
        assertTrue(binaryExpr.getRightTerm() instanceof ConstVal);
        IdentifierAccess leftAccess2 = (IdentifierAccess) binaryExpr.getLeftTerm();
        assertEquals("a", leftAccess2.getIdentifier().lexeme);
        ConstVal constVal = (ConstVal) binaryExpr.getRightTerm();
        assertEquals(1, constVal.getValue());
        assertEquals(TokenTypes.INT_LITERAL, constVal.getSymbol().type);
        assertTrue(binaryExpr.getOperator() instanceof BinaryOperator);
        BinaryOperator plusOp = (BinaryOperator) binaryExpr.getOperator();
        assertEquals(TokenTypes.PLUS, plusOp.getOperator().type);
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
        
        
        assertTrue(varDecl.getValue() instanceof RecordAccess);
        RecordAccess access = (RecordAccess) varDecl.getValue();

        assertTrue(access.getHeadAccess() instanceof ArrayAccess);
        ArrayAccess locHistArray = (ArrayAccess) access.getHeadAccess();
        
        assertTrue(locHistArray.getHeadAccess() instanceof RecordAccess);
        RecordAccess locHist = (RecordAccess) locHistArray.getHeadAccess();

        assertTrue(locHist.getHeadAccess() instanceof ArrayAccess);
        ArrayAccess peopleArray = (ArrayAccess) locHist.getHeadAccess();

        assertTrue(peopleArray.getHeadAccess() instanceof IdentifierAccess);
        IdentifierAccess peopleAccess = (IdentifierAccess) peopleArray.getHeadAccess();
        assertEquals("people", peopleAccess.getIdentifier().lexeme);

        assertTrue(peopleArray.getIndexExpression() instanceof ConstVal);
        ConstVal peopleIndex = (ConstVal) peopleArray.getIndexExpression();
        assertEquals(0, peopleIndex.getValue());
        assertEquals(TokenTypes.INT_LITERAL, peopleIndex.getSymbol().type);

        assertEquals("locationHistory", locHist.getIdentifier().lexeme);
        assertTrue(locHistArray.getIndexExpression() instanceof ConstVal);
        ConstVal locHistIndex = (ConstVal) locHistArray.getIndexExpression();
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


		assertNotNull(forLoop.getStart());
        NumType start = (NumType) forLoop.getStart();
        assertEquals(0, start.getSymbol().value);
        assertEquals(TokenTypes.INT_LITERAL, start.getSymbol().type);

        assertNotNull(forLoop.getEnd());
        NumType end = (NumType) forLoop.getEnd();
        assertEquals(10, end.getSymbol().value);
        assertEquals(TokenTypes.INT_LITERAL, end.getSymbol().type);

        assertNotNull(forLoop.getStep());
        NumType step = (NumType) forLoop.getStep();
        assertEquals(1, step.getSymbol().value);
        assertEquals(TokenTypes.INT_LITERAL, step.getSymbol().type);

        assertNotNull(forLoop.getBlock());
        Block forBlock = (Block) forLoop.getBlock();
        assertEquals(1, forBlock.getStatements().size());
        Statement forStatement = forBlock.getStatements().getFirst();
        assertTrue(forStatement instanceof FunctionCall);
        FunctionCall functionCall = (FunctionCall) forStatement;
        assertEquals("writeln", functionCall.getIdentifier().lexeme);
        assertEquals(1, functionCall.getParameters().size());

        assertTrue(functionCall.getParameters().getFirst().getParamExpression() instanceof IdentifierAccess);
        IdentifierAccess paramAccess = (IdentifierAccess) functionCall.getParameters().getFirst().getParamExpression();
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
        assertTrue(decl.getValue() instanceof ConstVal);
        ConstVal constVal = (ConstVal) decl.getValue();
        assertEquals(2132, constVal.getValue());
        assertEquals(TokenTypes.INT_LITERAL, constVal.getSymbol().type);

        Statement statement2 = block.getStatements().get(1);
        assertTrue(statement2 instanceof FunctionCall);
        FunctionCall functionCall = (FunctionCall) statement2;
        assertEquals("writeln", functionCall.getIdentifier().lexeme);
        assertEquals(1, functionCall.getParameters().size());

        assertTrue(functionCall.getParameters().getFirst().getParamExpression() instanceof UnaryExpression);
        UnaryExpression unaryExpr = (UnaryExpression) functionCall.getParameters().getFirst().getParamExpression();
        assertTrue(unaryExpr.getTerm() instanceof IdentifierAccess);
        IdentifierAccess idAccess = (IdentifierAccess) unaryExpr.getTerm();
        assertEquals("i", idAccess.getIdentifier().lexeme);
        assertTrue(unaryExpr.getOperator() instanceof UnaryOperator);
        UnaryOperator unaryOp = (UnaryOperator) unaryExpr.getOperator();
        assertEquals(TokenTypes.MINUS, unaryOp.getOperator().type);
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
        assertTrue(parenthesesTerm.getExpression() instanceof BinaryExpression);
        BinaryExpression innerBinaryExpr = (BinaryExpression) parenthesesTerm.getExpression();
        assertTrue(innerBinaryExpr.getLeftTerm() instanceof IdentifierAccess);
        IdentifierAccess leftAccess = (IdentifierAccess) innerBinaryExpr.getLeftTerm();
        assertEquals("a", leftAccess.getIdentifier().lexeme);
        assertTrue(innerBinaryExpr.getRightTerm() instanceof IdentifierAccess);
        IdentifierAccess rightAccess = (IdentifierAccess) innerBinaryExpr.getRightTerm();
        assertEquals("b", rightAccess.getIdentifier().lexeme);
        assertTrue(innerBinaryExpr.getOperator() instanceof BinaryOperator);
        BinaryOperator plusOp = (BinaryOperator) innerBinaryExpr.getOperator();
        assertEquals(TokenTypes.PLUS, plusOp.getOperator().type);

        assertTrue(binaryExpr.getRightTerm() instanceof IdentifierAccess);
        IdentifierAccess rightTermAccess = (IdentifierAccess) binaryExpr.getRightTerm();
        assertEquals("c", rightTermAccess.getIdentifier().lexeme);
        assertTrue(binaryExpr.getOperator() instanceof BinaryOperator);
        BinaryOperator mulOp = (BinaryOperator) binaryExpr.getOperator();
        assertEquals(TokenTypes.MULTIPLY, mulOp.getOperator().type);
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
        assertTrue(functionCall.getParameters().getFirst().getParamExpression() instanceof FunctionCall);
        FunctionCall innerFunctionCall = (FunctionCall) functionCall.getParameters().getFirst().getParamExpression();
        assertEquals("square", innerFunctionCall.getIdentifier().lexeme);
        assertEquals(1, innerFunctionCall.getParameters().size());
        assertTrue(innerFunctionCall.getParameters().getFirst().getParamExpression() instanceof IdentifierAccess);
        IdentifierAccess idAccess = (IdentifierAccess) innerFunctionCall.getParameters().getFirst().getParamExpression();
        assertEquals("value", idAccess.getIdentifier().lexeme);
        assertTrue(functionCall.getParameters().get(1).getParamExpression() instanceof ArrayAccess);
        ArrayAccess arrayAccess = (ArrayAccess) functionCall.getParameters().get(1).getParamExpression();
        assertTrue(arrayAccess.getHeadAccess() instanceof IdentifierAccess);
        IdentifierAccess arrayHeadAccess = (IdentifierAccess) arrayAccess.getHeadAccess();
        assertEquals("p", arrayHeadAccess.getIdentifier().lexeme);
        assertTrue(arrayAccess.getIndexExpression() instanceof ConstVal);

        ConstVal constVal = (ConstVal) arrayAccess.getIndexExpression();
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
        assertTrue(arrayExpr.getSizeExpression() instanceof ConstVal);

        ConstVal sizeVal = (ConstVal) arrayExpr.getSizeExpression();
        assertEquals(5, sizeVal.getValue());
        assertEquals(TokenTypes.INT_LITERAL, sizeVal.getSymbol().type);

        assertTrue(arrayExpr.getType() instanceof NumType);
        NumType arrayType = (NumType) arrayExpr.getType();
        assertEquals(TokenTypes.INT, arrayType.getSymbol().type);

    }


    @Test
    public void testError() throws Exception {
        try {
            String input = """
                fun main() {
                    a = Point(;
                }
                """;

            StringReader reader = new StringReader(input);
            Lexer lexer = new Lexer(reader);
            Parser parser = new Parser(lexer);

            ASTNode node = parser.getAST();
        } catch (SyntaxErrorException e) {
            assertTrue(e.getMessage().contains("Error: Expected ')'"));
        }

    }
}

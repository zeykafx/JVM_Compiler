package compiler.Parser;

import compiler.Lexer.Lexer;
import compiler.Lexer.Symbol;
import compiler.Lexer.TokenTypes;
import compiler.Parser.ASTNodes.ASTNode;
import compiler.Parser.ASTNodes.Block;
import compiler.Parser.ASTNodes.Program;
import compiler.Parser.ASTNodes.Statements.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Operators.BinaryOperator;
import compiler.Parser.ASTNodes.Statements.Expressions.Operators.Operator;
import compiler.Parser.ASTNodes.Statements.Expressions.Operators.UnaryOperator;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.*;
import compiler.Parser.ASTNodes.Types.Type;

import java.util.ArrayList;

public class Parser {
    Lexer lexer;
    Symbol lookAheadSymbol;
    
    public Parser(Lexer lexer) throws Exception {
        this.lexer = lexer;
        lookAheadSymbol = lexer.getNextSymbol();        
    }
    
    /// Returns the root of the AST
    public ASTNode getAST() {
        return null;
    }

    public Symbol match(TokenTypes token) throws Exception {
        if (lookAheadSymbol.type == token) {
            Symbol matchedSymbol = lookAheadSymbol;
            lookAheadSymbol = lexer.getNextSymbol();
            return matchedSymbol;
        } else {
            throw new RuntimeException("Syntax Error: Expected " + token + " but found " + lookAheadSymbol.lexeme + " at line " + lookAheadSymbol.line);
        }
    }

    public Program parseProgram() throws Exception {
        // parse the constants
        ArrayList<VariableDeclaration> constants = parseConstants();
        // parse the record declarations
        ArrayList<RecordDefinition> records = parseRecords();
        // parse the function declarations
        ArrayList<FunctionDefinition> functions = parseFunctions();

        return new Program(constants, records, functions);
    }

    public ArrayList<VariableDeclaration> parseConstants() throws Exception {
        ArrayList<VariableDeclaration> constants = new ArrayList<>();
        while (lookAheadSymbol.type == TokenTypes.FINAL) {
            match(TokenTypes.FINAL);
            VariableDeclaration constant = parseVariableDeclaration(true);
            constants.add(constant);
        }
        return constants;
    }

    public VariableDeclaration parseVariableDeclaration(boolean isConstant) throws Exception {
        Symbol identifier = match(TokenTypes.IDENTIFIER);
        Type type = parseType();
        match(TokenTypes.ASSIGN);
        Expression expression = parseExpression();
        match(TokenTypes.SEMICOLON);
        return new VariableDeclaration(identifier, type, expression, isConstant);
    }

    public Type parseType() throws Exception {
        Symbol type = switch (lookAheadSymbol.type) {
            case INT -> match(TokenTypes.INT);
            case FLOAT -> match(TokenTypes.FLOAT);
            case STRING -> match(TokenTypes.STRING);
            case BOOL -> match(TokenTypes.BOOL);
            case IDENTIFIER -> match(TokenTypes.IDENTIFIER);
            default -> null;
        };

        // Check if the type is an array
        if (lookAheadSymbol.type == TokenTypes.LEFT_SQUARE_BRACKET) {
            match(TokenTypes.LEFT_SQUARE_BRACKET);
            match(TokenTypes.RIGHT_SQUARE_BRACKET);
            return new Type(type, true);
        } else {
            return new Type(type, false);
        }
    }

    public Expression parseExpression() throws Exception {
        //Expression -> Term IsBinaryExpr | ArrayExpression | UnaryOperator Term .
        //IsBinaryExpr -> BinaryOperator Term | .

        //ArrayExpression -> "array" "[" "intval" "]" "of" Type ";" .

        //Term -> "(" Expression ")" | IdentifierOrFunctionCall | NewRecord | ConstVal .
        //IdentifierOrFunctionCall -> "identifier" IdentifierOrFunctionCallTail .
        //IdentifierOrFunctionCallTail -> | "(" ParamsCall ")" .

        //NewRecord -> "recordNameIdentifier" "(" ParamsCall ")" . # Note: record identifiers start with a capital letter

        //UnaryOperator -> "!" | "-" .
        //BinaryOperator -> "+" | "-" | "*" | "/" | "%" | "&&" | "||" | "==" | "!=" | "<" | ">" | "<=" | ">=" .
        //ConstVal -> "intval" | "floatval" | "stringval" | "true" | "false" .

        boolean isUnaryOperator = false;
        Operator operator = null;
        if (lookAheadSymbol.type == TokenTypes.NOT || lookAheadSymbol.type == TokenTypes.MINUS) {
            // UnaryOperator -> "!" | "-"
            Symbol unaryOperator = match(lookAheadSymbol.type);
            operator = new UnaryOperator(unaryOperator);
            isUnaryOperator = true;
        }

        // check for array expression
        if (lookAheadSymbol.type == TokenTypes.ARRAY) {
            // ArrayExpression -> "array" "[" "intval" "]" "of" Type ";" .
            match(TokenTypes.ARRAY);
            match(TokenTypes.LEFT_SQUARE_BRACKET);
            // size expression
            Expression sizeExpression = parseExpression();
            match(TokenTypes.RIGHT_SQUARE_BRACKET);
            match(TokenTypes.OF);
            Type type = parseType();
            match(TokenTypes.SEMICOLON);
            return new ArrayExpression(sizeExpression, type);
        }

        Term term = parseTerm();

        if (isUnaryOperator) {
            // If there was a unary operator, return a UnaryExpression
            return new UnaryExpression(operator, term);
        }

        // Check for binary operators
        if (lookAheadSymbol.type == TokenTypes.PLUS || lookAheadSymbol.type == TokenTypes.MINUS ||
                lookAheadSymbol.type == TokenTypes.MULTIPLY || lookAheadSymbol.type == TokenTypes.DIVIDE ||
                lookAheadSymbol.type == TokenTypes.MODULO || lookAheadSymbol.type == TokenTypes.EQUAL_EQUAL ||
                lookAheadSymbol.type == TokenTypes.NOT_EQUAL || lookAheadSymbol.type == TokenTypes.LESS_THAN ||
                lookAheadSymbol.type == TokenTypes.GREATER_THAN || lookAheadSymbol.type == TokenTypes.LESS_THAN_EQUAL ||
                lookAheadSymbol.type == TokenTypes.GREATER_THAN_EQUAL) {
            // BinaryOperator -> "+" | "-" | "*" | "/" | "%" | "&&" | "||" | "==" | "!=" | "<" | ">" | "<=" | ">="
            Symbol binaryOperator = match(lookAheadSymbol.type);
            Operator binaryOp = new BinaryOperator(binaryOperator);
            Term rightTerm = parseTerm();
			return new BinaryExpression(term, binaryOp, rightTerm);
        }

        // If no binary operator, return the term
        return term;

    }

    public Term parseTerm() throws Exception {
        if (lookAheadSymbol.type == TokenTypes.LEFT_PAR) {
            // Term -> "(" Expression ")"
            match(TokenTypes.LEFT_PAR);
            Expression expression = parseExpression();
            match(TokenTypes.RIGHT_PAR);
            return new ParenthesesTerm(expression);
        } else if (lookAheadSymbol.type == TokenTypes.IDENTIFIER) {
            // IdentifierOrFunctionCall -> "identifier" IdentifierOrFunctionCallTail
            Symbol identifier = match(TokenTypes.IDENTIFIER);
            if (lookAheadSymbol.type == TokenTypes.LEFT_PAR) {
                // IdentifierOrFunctionCallTail -> "(" ParamsCall ")"
                match(TokenTypes.LEFT_PAR);
                ArrayList<ParamCall> params = parseParamsCall();
                match(TokenTypes.RIGHT_PAR);
                return new FunctionCall(identifier, params);
            } else {
                // IdentifierOrFunctionCallTail -> ε
                return new Identifier(identifier);
            }
        } else if (lookAheadSymbol.type == TokenTypes.RECORD) {
            // Term -> NewRecord
            // RECORD is recordIdentifier in the grammar
            Symbol identifier = match(TokenTypes.RECORD);
            match(TokenTypes.LEFT_PAR);
            ArrayList<ParamCall> params = parseParamsCall();
            match(TokenTypes.RIGHT_PAR);
            return new NewRecord(identifier, params);
        } else if (lookAheadSymbol.type == TokenTypes.INT_LITERAL ||
                lookAheadSymbol.type == TokenTypes.FLOAT_LITERAL ||
                lookAheadSymbol.type == TokenTypes.STRING_LITERAL ||
                lookAheadSymbol.type == TokenTypes.BOOL_TRUE ||
                lookAheadSymbol.type == TokenTypes.BOOL_FALSE) {
            // Term -> ConstVal
            // ConstVal -> "intval" | "floatval" | "boolval" | "stringval" | "true" | "false"
            Symbol constVal = switch (lookAheadSymbol.type) {
                case INT_LITERAL -> match(TokenTypes.INT_LITERAL);
                case FLOAT_LITERAL -> match(TokenTypes.FLOAT_LITERAL);
                case STRING_LITERAL -> match(TokenTypes.STRING_LITERAL);
                case BOOL_TRUE -> match(TokenTypes.BOOL_TRUE);
                case BOOL_FALSE -> match(TokenTypes.BOOL_FALSE);
                default -> null;
            };

            return new ConstVal(constVal.value, constVal);
        }

        // If none of the above, throw an error
        throw new Exception("Syntax Error: Expected Term but found " + lookAheadSymbol.lexeme + " at line " + lookAheadSymbol.line);
    }


    public ArrayList<ParamCall> parseParamsCall() throws Exception {
        ArrayList<ParamCall> params = new ArrayList<>();

        if (lookAheadSymbol.type != TokenTypes.RIGHT_PAR) {
            // ParamsCall -> Expression "," ParamsCall | Expression
            params.add(parseParam());
            while (lookAheadSymbol.type == TokenTypes.COMMA) {
                match(TokenTypes.COMMA);
                params.add(parseParam());
            }
        }
        return params;
    }

    public ParamCall parseParam() throws Exception {
        // ParamCall -> Expression
        Expression expression = parseExpression();
        return new ParamCall(expression);
    }

    public ArrayList<RecordDefinition> parseRecords() throws Exception {
        //RecordDefinitions -> RecordDefinition RecordDefinitions | .

        //RecordDefinition -> "recordNameIdentifier" "rec" "{" RecordFields "}" .
        //RecordFields -> RecordField RecordFieldsTail | .
        //RecordFieldsTail -> ";" RecordField RecordFieldsTail | .
        //RecordField -> "identifier" Type .

        ArrayList<RecordDefinition> records = new ArrayList<>();
        if (lookAheadSymbol.type == TokenTypes.RECORD) { // RECORD is the record identifier (starts with a capital letter)
            Symbol recordIdentifier = match(TokenTypes.RECORD); // match the record identifier
            match(TokenTypes.REC); // REC is the keyword "rec"
            match(TokenTypes.LEFT_BRACKET); // "{"
            ArrayList<RecordFieldDefinition> fields = new ArrayList<>();
            while (lookAheadSymbol.type != TokenTypes.RIGHT_BRACKET) {
                RecordFieldDefinition field = parseRecordField();
                fields.add(field);
                if (lookAheadSymbol.type == TokenTypes.SEMICOLON) {
                    match(TokenTypes.SEMICOLON);
                }
            }
            match(TokenTypes.RIGHT_BRACKET); // "}"
            RecordDefinition record = new RecordDefinition(recordIdentifier, fields);
            records.add(record);
        }

        return records;
    }

    public RecordFieldDefinition parseRecordField() throws Exception {
        // RecordField -> "identifier" Type
        Symbol identifier = match(TokenTypes.IDENTIFIER);
        Type type = parseType();
        return new RecordFieldDefinition(identifier, type);
    }

    public ArrayList<FunctionDefinition> parseFunctions() throws Exception {

        //FunctionDefinitions -> FunctionDefinition FunctionDefinitions | .
        //FunctionDefinition -> "fun" "identifier" "(" Params ")" Type Block .

        //Params -> Param ParamsTail | .
        //ParamsTail -> "," Param ParamsTail | .
        //Param -> "identifier" Type .
        ArrayList<FunctionDefinition> functions = new ArrayList<>();
        while (lookAheadSymbol.type == TokenTypes.FUN) {
            functions.add(parseFunction());
        }
        return functions;
    }

    public FunctionDefinition parseFunction() throws Exception {
        // FunctionDefinition -> "fun" "identifier" "(" Params ")" Type Block
        match(TokenTypes.FUN);
        Symbol identifier = match(TokenTypes.IDENTIFIER);
        match(TokenTypes.LEFT_PAR);
        ArrayList<ParamDefinition> params = parseParamDefinitions();
        match(TokenTypes.RIGHT_PAR);
        Type returnType = parseType();
        Block block = parseBlock();
        return new FunctionDefinition(identifier, returnType, params, block);
    }

    public ArrayList<ParamDefinition> parseParamDefinitions() throws Exception {
        // Params -> Param ParamsTail | .
        // ParamsTail -> "," Param ParamsTail | .
        // Param -> "identifier" Type .

        ArrayList<ParamDefinition> params = new ArrayList<>();
        if (lookAheadSymbol.type != TokenTypes.RIGHT_PAR) {
            params.add(parseParamDefinition());
            while (lookAheadSymbol.type == TokenTypes.COMMA) {
                match(TokenTypes.COMMA);
                params.add(parseParamDefinition());
            }
        }
        return params;
    }

    public ParamDefinition parseParamDefinition() throws Exception {
        // Param -> "identifier" Type
        Symbol identifier = match(TokenTypes.IDENTIFIER);
        Type type = parseType();
        return new ParamDefinition(identifier, type);
    }

    public Block parseBlock() throws Exception {
        // Block -> "{" Stmts ReturnStmt "}" .
        //Stmts -> Stmt Stmts | .
        //Stmt -> IfStmt | ForLoop | WhileLoop | FunctionDefinition | BaseStatement ";" .
        //ReturnStmt -> "return" Expression ";" | .

        match(TokenTypes.LEFT_BRACKET);
        ArrayList<Statement> statements = new ArrayList<>();
        ReturnStatement returnStatement = null;

        while (lookAheadSymbol.type != TokenTypes.RIGHT_BRACKET) {
            if (lookAheadSymbol.type == TokenTypes.RETURN) {
                match(TokenTypes.RETURN);
                Expression returnExpression = parseExpression();
                match(TokenTypes.SEMICOLON);
                returnStatement = new ReturnStatement(returnExpression);
            } else {
                Statement statement = parseStatement();
                statements.add(statement);
            }
        }
        match(TokenTypes.RIGHT_BRACKET);
        return new Block(statements, returnStatement);
    }

    public Statement parseStatement() throws Exception {
        // Stmt -> IfStmt | ForLoop | WhileLoop | FunctionDefinition | BaseStatement ";"
        if (lookAheadSymbol.type == TokenTypes.IF) {
            return parseIfStatement();
        } else if (lookAheadSymbol.type == TokenTypes.FOR) {
            return parseForLoop();
        } else if (lookAheadSymbol.type == TokenTypes.WHILE) {
            return parseWhileLoop();
        } else if (lookAheadSymbol.type == TokenTypes.FUN) {
            return parseFunction();
        } else {
            // BaseStatement -> VariableDeclaration | Assignment | Expression ";"
            Statement statement = parseBaseStatement();
            if (lookAheadSymbol.type == TokenTypes.SEMICOLON) {
                match(TokenTypes.SEMICOLON);
            }
            return statement;
        }
    }

    public Statement parseBaseStatement() throws Exception {

        // BaseStatement -> VariableDeclaration | Assignment | Expression
        if (lookAheadSymbol.type == TokenTypes.IDENTIFIER) {
            Symbol identifier = match(TokenTypes.IDENTIFIER);
            if (lookAheadSymbol.type == TokenTypes.ASSIGN) {
                return parseAssignment(identifier);
            } else {
                return parseExpression();
            }
        } else if (lookAheadSymbol.type == TokenTypes.FINAL) {
            return parseVariableDeclaration(true);
        } else {
            return parseExpression();
        }
    }

    public Assignment parseAssignment(Symbol identifier) throws Exception {
        // Assignment -> "identifier" "=" Expression
        match(TokenTypes.ASSIGN);
        Expression expression = parseExpression();
        return new Assignment(identifier, expression);
    }
}

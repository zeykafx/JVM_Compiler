package compiler.Parser;

import compiler.Lexer.Lexer;
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
import compiler.Parser.ASTNodes.Statements.Expressions.Operators.Operator;
import compiler.Parser.ASTNodes.Statements.Expressions.Operators.UnaryOperator;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.*;
import compiler.Parser.ASTNodes.Statements.Statements.*;
import compiler.Parser.ASTNodes.Types.NumType;
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
        try {
            return parseProgram();
        } catch (SyntaxErrorException e) {
            System.err.println("Syntax Error: " + e.getMessage());
            return null;
        }
        catch (Exception e) {
            System.err.println("Unexpected error parsing the program: " + e.getMessage());
            return null;
        }
    }

    public Symbol match(TokenTypes token) throws Exception {
        if (lookAheadSymbol.type == token) {
            Symbol matchedSymbol = lookAheadSymbol;
            lookAheadSymbol = lexer.getNextSymbol();
            return matchedSymbol;
        } else {
            throw new SyntaxErrorException(
                "Syntax Error: Expected " +
                token +
                " but found " +
                lookAheadSymbol.lexeme +
                " of type " +
                lookAheadSymbol.type +
                " at line " +
                lookAheadSymbol.line +
                ", column " +
                lookAheadSymbol.column
            );
        }
    }

    public Program parseProgram() throws Exception {
        // parse the constants
        ArrayList<VariableDeclaration> constants = parseConstants();
        // parse the record declarations
        ArrayList<RecordDefinition> records = parseRecords();
        // parse the global variable declarations
        ArrayList<VariableDeclaration> globalVariables =
            parseGlobalDeclarations();
        // parse the function declarations
        ArrayList<FunctionDefinition> functions = parseFunctions();

        return new Program(constants, records, globalVariables, functions);
    }

    public ArrayList<VariableDeclaration> parseConstants() throws Exception {
        ArrayList<VariableDeclaration> constants = new ArrayList<>();
        while (lookAheadSymbol.type == TokenTypes.FINAL) {
            match(TokenTypes.FINAL);
            VariableDeclaration constant = parseVariableDeclaration(
                true,
                false,
                null
            );
            constants.add(constant);
        }
        return constants;
    }

    public ArrayList<VariableDeclaration> parseGlobalDeclarations()
        throws Exception {
        ArrayList<VariableDeclaration> globalVariables = new ArrayList<>();
        while (lookAheadSymbol.type == TokenTypes.IDENTIFIER) {
            // VariableDeclaration -> "identifier" Type "=" Expression ";"
            VariableDeclaration variable = parseVariableDeclaration(
                false,
                false,
                null
            );
            globalVariables.add(variable);
        }
        return globalVariables;
    }

    public VariableDeclaration parseVariableDeclaration(
        boolean isConstant,
        boolean skipIdent,
        Symbol identIfSkipped
    ) throws Exception {
        Symbol identifier;

        if (skipIdent) {
            identifier = identIfSkipped;
        } else {
            identifier = match(TokenTypes.IDENTIFIER);
        }

        Type type = parseType();
        // there might not be an assignment

        if (lookAheadSymbol.type == TokenTypes.ASSIGN) {
            match(TokenTypes.ASSIGN);

            Expression expression = parseExpression();
            match(TokenTypes.SEMICOLON);
            return new VariableDeclaration(
                identifier,
                type,
                expression,
                isConstant
            );
        } else if (lookAheadSymbol.type == TokenTypes.SEMICOLON) {
            match(TokenTypes.SEMICOLON);
            return new VariableDeclaration(identifier, type);
        } else {
            throw new SyntaxErrorException(
                "Syntax Error: Expected '=' or ';' after variable declaration but found " +
                lookAheadSymbol.lexeme +
                " of type " +
                lookAheadSymbol.type +
                " at line " +
                lookAheadSymbol.line +
                ", column " +
                lookAheadSymbol.column
            );
        }
    }

    public Type parseType() throws Exception {
        Symbol symbol =
            switch (lookAheadSymbol.type) {
                case INT -> match(TokenTypes.INT);
                case FLOAT -> match(TokenTypes.FLOAT);
                case STRING -> match(TokenTypes.STRING);
                case BOOL -> match(TokenTypes.BOOL);
                case IDENTIFIER -> match(TokenTypes.IDENTIFIER); // TODO: maybe remove
                case RECORD -> match(TokenTypes.RECORD);
                default -> null;
            };

        Type type = new Type(symbol, false);

		assert symbol != null;
		if (symbol.type == TokenTypes.INT || symbol.type == TokenTypes.FLOAT) {
            type = new NumType(symbol);
        }

        // Check if the type is an array
        if (lookAheadSymbol.type == TokenTypes.LEFT_SQUARE_BRACKET) {
            match(TokenTypes.LEFT_SQUARE_BRACKET);
            match(TokenTypes.RIGHT_SQUARE_BRACKET);
            type.isList = true;
        }

        return type;
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
        if (
            lookAheadSymbol.type == TokenTypes.NOT ||
            lookAheadSymbol.type == TokenTypes.MINUS
        ) {
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
            return new ArrayExpression(sizeExpression, type);
        }

        Term term = parseTerm();

        if (isUnaryOperator) {
            // If there was a unary operator, return a UnaryExpression
            return new UnaryExpression(operator, term);
        }

        // Check for binary operators
        if (
            lookAheadSymbol.type == TokenTypes.PLUS ||
            lookAheadSymbol.type == TokenTypes.MINUS ||
            lookAheadSymbol.type == TokenTypes.MULTIPLY ||
            lookAheadSymbol.type == TokenTypes.DIVIDE ||
            lookAheadSymbol.type == TokenTypes.MODULO ||
            lookAheadSymbol.type == TokenTypes.EQUAL_EQUAL ||
            lookAheadSymbol.type == TokenTypes.NOT_EQUAL ||
            lookAheadSymbol.type == TokenTypes.LESS_THAN ||
            lookAheadSymbol.type == TokenTypes.GREATER_THAN ||
            lookAheadSymbol.type == TokenTypes.LESS_THAN_EQUAL ||
            lookAheadSymbol.type == TokenTypes.GREATER_THAN_EQUAL
        ) {
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
            Symbol identifier = match(lookAheadSymbol.type);

            // this can be a function call, or an access (identifier, record, array)
            if (lookAheadSymbol.type == TokenTypes.LEFT_PAR) {
                // IdentifierOrFunctionCallTail -> "(" ParamsCall ")"
                return parseParamCall(identifier);
            } else {
                // IdentifierOrFunctionCallTail -> ε
                return parseAccess(true, identifier);
            }
        } else if (lookAheadSymbol.type == TokenTypes.RECORD) {
            // Term -> NewRecord
            // RECORD is recordIdentifier in the grammar
            Symbol identifier = match(TokenTypes.RECORD);
            match(TokenTypes.LEFT_PAR);
            ArrayList<ParamCall> params = parseParamsCall();
            match(TokenTypes.RIGHT_PAR);
            return new NewRecord(identifier, params);
        } else if (
            lookAheadSymbol.type == TokenTypes.INT_LITERAL ||
            lookAheadSymbol.type == TokenTypes.FLOAT_LITERAL ||
            lookAheadSymbol.type == TokenTypes.STRING_LITERAL ||
            lookAheadSymbol.type == TokenTypes.BOOL_TRUE ||
            lookAheadSymbol.type == TokenTypes.BOOL_FALSE
        ) {
            // Term -> ConstVal
            // ConstVal -> "intval" | "floatval" | "boolval" | "stringval" | "true" | "false"
            Symbol constVal =
                switch (lookAheadSymbol.type) {
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
        throw new SyntaxErrorException(
            "Syntax Error: Expected Term but found " +
            lookAheadSymbol.lexeme +
            " of type " +
            lookAheadSymbol.type +
            " at line " +
            lookAheadSymbol.line +
            ", column " +
            lookAheadSymbol.column
        );
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

        while (lookAheadSymbol.type == TokenTypes.RECORD) {
            // RecordDefinition -> "recordNameIdentifier" "rec" "{" RecordFields "}" .
            RecordDefinition record = parseRecord();
            records.add(record);
        }

        return records;
    }

    public RecordDefinition parseRecord() throws Exception {
        // RecordDefinition -> "recordNameIdentifier" "rec" "{" RecordFields "}" .
        // RECORD is the record identifier (starts with a capital letter)
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
        return new RecordDefinition(recordIdentifier, fields);
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

        Type returnType = null;
        if (lookAheadSymbol.type != TokenTypes.LEFT_BRACKET) {
            returnType = parseType();
        }
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
        //Block -> "{" Stmts ReturnStmt "}" .
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
                statements.add(parseStatement());
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
            // BaseStatement -> VariableDeclaration | Assignment | RecordDefinition | free IdentifierAccess | Expression ";"
            Statement statement = parseBaseStatement();
            if (lookAheadSymbol.type == TokenTypes.SEMICOLON) {
                match(TokenTypes.SEMICOLON);
            }
            return statement;
        }
    }

    public IfStatement parseIfStatement() throws Exception {
        // IfStmt -> "if" "(" Expression ")" Block ElseStmt
        match(TokenTypes.IF);
        match(TokenTypes.LEFT_PAR);
        Expression condition = parseExpression();
        match(TokenTypes.RIGHT_PAR);
        Block ifBlock = parseBlock();
        Block elseStatement = parseElseStatement();
        return new IfStatement(condition, ifBlock, elseStatement);
    }

    public Block parseElseStatement() throws Exception {
        // ElseStmt -> "else" Block | .
        if (lookAheadSymbol.type == TokenTypes.ELSE) {
            match(TokenTypes.ELSE);
            return parseBlock();
        }
        return null; // No else statement
    }

    public ForLoop parseForLoop() throws Exception {
        // ForLoop -> "for" "(" ForCondition ")" Block .
        // ForCondition -> "identifier" "," NumType "," NumType "," NumType .
        // NumType -> "int" | "float" .
        match(TokenTypes.FOR);
        match(TokenTypes.LEFT_PAR);
        Symbol identifier = match(TokenTypes.IDENTIFIER);
        match(TokenTypes.COMMA);
        NumType startType = parseNumType();
        match(TokenTypes.COMMA);
        NumType endType = parseNumType();
        match(TokenTypes.COMMA);
        NumType stepType = parseNumType();
        match(TokenTypes.RIGHT_PAR);
        Block block = parseBlock();
        return new ForLoop(identifier, startType, endType, stepType, block);
    }

    public NumType parseNumType() throws Exception {
        // NumType -> "int" | "float"
        Symbol type =
            switch (lookAheadSymbol.type) {
                case INT_LITERAL -> match(TokenTypes.INT_LITERAL);
                case FLOAT_LITERAL -> match(TokenTypes.FLOAT_LITERAL);
                default -> null;
            };
        return new NumType(type);
    }

    public WhileLoop parseWhileLoop() throws Exception {
        // WhileLoop -> "while" "(" Expression ")" Block .
        match(TokenTypes.WHILE);
        match(TokenTypes.LEFT_PAR);
        Expression condition = parseExpression();
        match(TokenTypes.RIGHT_PAR);
        Block block = parseBlock();
        return new WhileLoop(condition, block);
    }

    public Statement parseBaseStatement() throws Exception {
        // BaseStatement -> Declaration | VariableAssignment | RecordDefinition | "free" IdentifierAccess | Expression ";" .

        // Declarations -> Declaration Declarations | .

        // Declaration -> Final "identifier" Type Assignment .
        // Assignment -> "=" Expression | .
        // Final -> "final" | .

        // VariableAssignment -> IdentifierAccess "=" Expression .

        // RecordDefinitions -> RecordDefinition RecordDefinitions | .

        // RecordDefinition -> "recordNameIdentifier" "rec" "{" RecordFields "}" .
        // RecordFields -> RecordField RecordFieldsTail | .
        // RecordFieldsTail -> ";" RecordField RecordFieldsTail | .
        // RecordField -> "identifier" Type .

        // NOT LL(1)
        if (lookAheadSymbol.type == TokenTypes.FINAL) {
            // Declaration (final or not)
            return parseVariableDeclaration(true, false, null);
        } else if (lookAheadSymbol.type == TokenTypes.IDENTIFIER) {
            // VariableAssignment (using an Access) or non-constant declaration
            Symbol identifier = match(TokenTypes.IDENTIFIER);

            // If the lookahead is not an equal sign, then we have a non-constant declaration or a function call
            if (lookAheadSymbol.type != TokenTypes.ASSIGN) {
                if (lookAheadSymbol.type == TokenTypes.LEFT_PAR) {
                    // Function call
                    return parseParamCall(identifier);
                }

                // or it could be something like "i int"
                return parseVariableDeclaration(false, true, identifier);
            }

            // otherwise we have a variable assignment
            Access access = parseAccess(true, identifier);
            match(TokenTypes.ASSIGN);

            Expression expression = parseExpression();
            return new VariableAssigment(access, expression);
        } else if (lookAheadSymbol.type == TokenTypes.RECORD) {
            return parseRecord();
        } else if (lookAheadSymbol.type == TokenTypes.FREE) {
            // free IdentifierAccess
            match(TokenTypes.FREE);
            Access access = parseAccess(false, null);

            return new FreeStatement((IdentifierAccess) access);
        }

        return null;
    }

    public FunctionCall parseParamCall(Symbol identifier) throws Exception {
        match(TokenTypes.LEFT_PAR);
        if (lookAheadSymbol.type == TokenTypes.RIGHT_PAR) {
            // empty params
            match(TokenTypes.RIGHT_PAR);
            return new FunctionCall(identifier, new ArrayList<>());
        }
        ArrayList<ParamCall> params = parseParamsCall();
        match(TokenTypes.RIGHT_PAR);
        return new FunctionCall(identifier, params);
    }

    public Access parseAccess(boolean skipIdent, Symbol identIfSkipped)
        throws Exception {
        // IdentifierAccess -> "identifier" AccessChain .
        // AccessChain -> Access AccessChain | .
        // Access -> "[" Expression "]" | "." "identifier" .
        // e.g.:  "people[0].locationHistory[3].y;"

        Symbol identifier;
        if (skipIdent) {
            identifier = identIfSkipped;
        } else {
            identifier = match(TokenTypes.IDENTIFIER);
        }
        Access access = new IdentifierAccess(identifier);

        // Parse the access chain (array indices and field accesses)
        while (
            lookAheadSymbol.type == TokenTypes.LEFT_SQUARE_BRACKET ||
            lookAheadSymbol.type == TokenTypes.DOT
        ) {
            if (lookAheadSymbol.type == TokenTypes.LEFT_SQUARE_BRACKET) {
                // Array access: [Expression]
                match(TokenTypes.LEFT_SQUARE_BRACKET);
                Expression indexExpr = parseExpression();
                match(TokenTypes.RIGHT_SQUARE_BRACKET);
                access = new ArrayAccess(access, indexExpr);
            } else {
                // Field access: .identifier
                match(TokenTypes.DOT);
                Symbol fieldName = match(TokenTypes.IDENTIFIER);
                access = new RecordAccess(access, fieldName);
            }
        }

        return access;
    }
}

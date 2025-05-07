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
//            System.err.println("Syntax Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
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

        return new Program(constants, records, globalVariables, functions, lookAheadSymbol.line, lookAheadSymbol.column);
    }

    public ArrayList<VariableDeclaration> parseConstants() throws Exception {
        ArrayList<VariableDeclaration> constants = new ArrayList<>();

        checkExpectedSymbolsConstantDef();

        while (lookAheadSymbol.type == TokenTypes.FINAL) {
            match(TokenTypes.FINAL);
            VariableDeclaration constant = parseVariableDeclaration(
                true,
                false,
                null,
                false // technically constants are global but here globals are not constant
            );
            constants.add(constant);

            checkExpectedSymbolsConstantDef();
        }
        return constants;
    }

    private void checkExpectedSymbolsConstantDef() throws SyntaxErrorException {
        switch (lookAheadSymbol.type) {
            case FINAL, RECORD, IDENTIFIER, FUN, EOF -> {
                // do nothing
            }
            default -> {
                throw new SyntaxErrorException(
                        "Syntax Error: Expected FINAL, RECORD, IDENTIFIER, FUN, or EOF but found " +
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
    }

    public ArrayList<VariableDeclaration> parseGlobalDeclarations()
        throws Exception {
        ArrayList<VariableDeclaration> globalVariables = new ArrayList<>();

        checkExpectedSymbolsGlobalVar();

        while (lookAheadSymbol.type == TokenTypes.IDENTIFIER)  {
            // VariableDeclaration -> "identifier" Type "=" Expression ";"
            VariableDeclaration variable = parseVariableDeclaration(
                false,
                false,
                null,
                true
            );
            globalVariables.add(variable);

            checkExpectedSymbolsGlobalVar();
        }
        return globalVariables;
    }

    private void checkExpectedSymbolsGlobalVar() throws SyntaxErrorException {
        switch (lookAheadSymbol.type) {
            case IDENTIFIER, FUN, EOF -> {
                // do nothing
            }
            default -> {
                throw new SyntaxErrorException(
                        "Syntax Error: Expected IDENTIFIER, FUN, or EOF but found " +
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
    }

    public VariableDeclaration parseVariableDeclaration(
        boolean isConstant,
        boolean skipIdent,
        Symbol identIfSkipped,
        boolean isGlobal
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
                isConstant,
                isGlobal,
                identifier.line,
                identifier.column
            );
        } else if (lookAheadSymbol.type == TokenTypes.SEMICOLON) {
            match(TokenTypes.SEMICOLON);
            return new VariableDeclaration(
                    identifier,
                    type,
                    null,
                    isConstant,
                    isGlobal,
                    identifier.line,
                    identifier.column
            );
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
                case IDENTIFIER -> match(TokenTypes.IDENTIFIER);
                case RECORD -> match(TokenTypes.RECORD);
                default -> null;
            };

        if (symbol == null) {
            throw new SyntaxErrorException(
                "Syntax Error: Expected a type but found " +
                lookAheadSymbol.lexeme +
                " of type " +
                lookAheadSymbol.type +
                " at line " +
                lookAheadSymbol.line +
                ", column " +
                lookAheadSymbol.column
            );
        }

        Type type = new Type(symbol, false, lookAheadSymbol.line, lookAheadSymbol.column);

		if (symbol.type == TokenTypes.INT || symbol.type == TokenTypes.FLOAT) {
            boolean isFloat = symbol.type == TokenTypes.FLOAT;
            type = new NumType(symbol, isFloat);
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
        UnaryOperator operator = null;
        if (
            lookAheadSymbol.type == TokenTypes.NOT ||
            lookAheadSymbol.type == TokenTypes.MINUS
        ) {
            // UnaryOperator -> "!" | "-"
            Symbol unaryOperator = match(lookAheadSymbol.type);
            operator = new UnaryOperator(unaryOperator, lookAheadSymbol.line, lookAheadSymbol.column);
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
            return new ArrayExpression(sizeExpression, type, sizeExpression.line, sizeExpression.column);
        }

        Term term1 = parseTerm();
        // if (((val) == (41)) && ((copyPoint(points)) == (p3))))
        Term term = new ParenthesesTerm(term1, term1.line, term1.column);

        if (isUnaryOperator) {
            // If there was a unary operator, return a UnaryExpression
            return new UnaryExpression(operator, term, term.line, term.column);
        }

        // Check for binary operators
        if (
            lookAheadSymbol.type == TokenTypes.AND ||
            lookAheadSymbol.type == TokenTypes.OR ||
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

            BinaryOperator binaryOp = new BinaryOperator(binaryOperator, term.line, term.column);
            Term rightTerm = new ParenthesesTerm(parseTerm(), binaryOp.line, binaryOp.column);

            // construct a binary expression from what we already parsed
            BinaryExpression binaryExpression = new BinaryExpression(term, binaryOp, rightTerm, term.line, term.column);

			// if we see an AND token or an OR token, this means that we might have a bool expression with no parentheses around them
			// e.g. "if  (i+4 > 10 && found == false)", previously, this would have required parentheses around i+4 and also around the resulting term > 10
			// e.g. "if ( ((i+4) > 10) && (found == false) )"
			if (
//					lookAheadSymbol.type == TokenTypes.AND ||
//					lookAheadSymbol.type == TokenTypes.OR ||
//					lookAheadSymbol.type == TokenTypes.GREATER_THAN ||
//					lookAheadSymbol.type == TokenTypes.GREATER_THAN_EQUAL ||
//					lookAheadSymbol.type == TokenTypes.LESS_THAN ||
//					lookAheadSymbol.type == TokenTypes.LESS_THAN_EQUAL ||
//					lookAheadSymbol.type == TokenTypes.EQUAL_EQUAL ||
//					lookAheadSymbol.type == TokenTypes.NOT_EQUAL
                    lookAheadSymbol.type == TokenTypes.AND ||
                    lookAheadSymbol.type == TokenTypes.OR ||
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
                Term binOpTerm = new ParenthesesTerm(binaryExpression, term.line, term.column);
                Symbol otherOp = match(lookAheadSymbol.type);
                BinaryOperator otherBinOp = new BinaryOperator(otherOp, term.line, term.column);
                Term rightRightTerm = new ParenthesesTerm(parseExpression(), term.line, term.column);
                return new BinaryExpression(binOpTerm, otherBinOp, rightRightTerm, term.line, term.column);
            }

            return binaryExpression;
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
            return new ParenthesesTerm(expression, expression.line, expression.column);
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
            return new NewRecord(identifier, params, identifier.line, identifier.column);
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

            return new ConstVal(constVal.value, constVal, constVal.line, constVal.column);
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
        int paramIndex = 0;

        if (lookAheadSymbol.type != TokenTypes.RIGHT_PAR) {
            // ParamsCall -> Expression "," ParamsCall | Expression
            params.add(parseParam(paramIndex));
            paramIndex++;

            while (lookAheadSymbol.type == TokenTypes.COMMA) {
                match(TokenTypes.COMMA);
                params.add(parseParam(paramIndex));
                paramIndex++;
            }
        }
        return params;
    }

    public ParamCall parseParam(int currentParamIndex) throws Exception {
        // ParamCall -> Expression
        Expression expression = parseExpression();
        return new ParamCall(expression, currentParamIndex, expression.line, expression.column);
    }

    public ArrayList<RecordDefinition> parseRecords() throws Exception {
        //RecordDefinitions -> RecordDefinition RecordDefinitions | .

        //RecordDefinition -> "recordNameIdentifier" "rec" "{" RecordFields "}" .
        //RecordFields -> RecordField RecordFieldsTail | .
        //RecordFieldsTail -> ";" RecordField RecordFieldsTail | .
        //RecordField -> "identifier" Type .

        checkExpectedTypesRecordDef();

        ArrayList<RecordDefinition> records = new ArrayList<>();

        while (lookAheadSymbol.type == TokenTypes.RECORD) {
            // RecordDefinition -> "recordNameIdentifier" "rec" "{" RecordFields "}" .
            RecordDefinition record = parseRecord();
            records.add(record);

            checkExpectedTypesRecordDef();
        }

        return records;
    }

    private void checkExpectedTypesRecordDef() throws SyntaxErrorException {
        switch (lookAheadSymbol.type) {
            case RECORD, IDENTIFIER, FUN, EOF -> {
                // do nothing
            }
            default -> {
                throw new SyntaxErrorException(
                        "Syntax Error: Expected RECORD, IDENTIFIER, FUN, or EOF but found " +
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
    }

    public RecordDefinition parseRecord() throws Exception {
        // RecordDefinition -> "recordNameIdentifier" "rec" "{" RecordFields "}" .
        // RECORD is the record identifier (starts with a capital letter)
        Symbol recordIdentifier = match(TokenTypes.RECORD); // match the record identifier
        match(TokenTypes.REC); // REC is the keyword "rec"
        match(TokenTypes.LEFT_BRACKET); // "{"
        ArrayList<RecordFieldDefinition> fields = new ArrayList<>();
        int fieldIndex = 0;
        while (lookAheadSymbol.type != TokenTypes.RIGHT_BRACKET) {
            RecordFieldDefinition field = parseRecordField(fieldIndex);
            fields.add(field);

            fieldIndex++;

            if (lookAheadSymbol.type == TokenTypes.SEMICOLON) {
                match(TokenTypes.SEMICOLON);
            }
        }
        match(TokenTypes.RIGHT_BRACKET); // "}"
        return new RecordDefinition(recordIdentifier, fields, recordIdentifier.line, recordIdentifier.column);
    }

    public RecordFieldDefinition parseRecordField(int fieldIndex) throws Exception {
        // RecordField -> "identifier" Type
        Symbol identifier = match(TokenTypes.IDENTIFIER);
        Type type = parseType();
        return new RecordFieldDefinition(identifier, type, fieldIndex, identifier.line, identifier.column);
    }

    public ArrayList<FunctionDefinition> parseFunctions() throws Exception {
        //FunctionDefinitions -> FunctionDefinition FunctionDefinitions | .
        //FunctionDefinition -> "fun" "identifier" "(" Params ")" Type Block .

        //Params -> Param ParamsTail | .
        //ParamsTail -> "," Param ParamsTail | .
        //Param -> "identifier" Type .
        ArrayList<FunctionDefinition> functions = new ArrayList<>();

        checkExpectedSymbolsFunDef();

        while (lookAheadSymbol.type == TokenTypes.FUN) {
            functions.add(parseFunction());

            checkExpectedSymbolsFunDef();
        }
        return functions;
    }

    private void checkExpectedSymbolsFunDef() throws SyntaxErrorException {
        switch (lookAheadSymbol.type) {
            case FUN, EOF -> {
                // do nothing
            }
            default -> {
                throw new SyntaxErrorException(
                        "Syntax Error: Expected FUN, or EOF but found " +
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
    }

    public FunctionDefinition parseFunction() throws Exception {
        // OLD: FunctionDefinition -> "fun" "identifier" "(" Params ")" Type Block
        // NEW: FunctionDefinition -> "fun" InstanceRef "identifier" "(" Params ")" Type Block
        // InstanceRef ->  "(" "identifier" "recordNameIdentifier" ")" | .

        match(TokenTypes.FUN);

        boolean hasInstanceRef = false;
        Symbol instanceRefIdent = null;
        Type instanceRefType = null;

        // look for the InstanceRef (e.g., "(p Point)")
        if (lookAheadSymbol.type == TokenTypes.LEFT_PAR) {
            hasInstanceRef = true;
            match(TokenTypes.LEFT_PAR);
            instanceRefIdent = match(TokenTypes.IDENTIFIER); // match the record instance identifier (e.g., "p")
            instanceRefType = parseType(); // match the record type (e.g., "Point")
            match(TokenTypes.RIGHT_PAR);

        }

        Symbol identifier = match(TokenTypes.IDENTIFIER);
        match(TokenTypes.LEFT_PAR);
        ArrayList<ParamDefinition> params = parseParamDefinitions();
        match(TokenTypes.RIGHT_PAR);

        Type returnType = null;
        if (lookAheadSymbol.type != TokenTypes.LEFT_BRACKET) {
            returnType = parseType();
        }
        Block block = parseBlock();

        if (hasInstanceRef) {
            return new FunctionDefinition(instanceRefIdent, instanceRefType, identifier, returnType, params, block, identifier.line, identifier.column);
        } else {
            return new FunctionDefinition(identifier, returnType, params, block, identifier.line, identifier.column);
        }

    }

    public ArrayList<ParamDefinition> parseParamDefinitions() throws Exception {
        // Params -> Param ParamsTail | .
        // ParamsTail -> "," Param ParamsTail | .
        // Param -> "identifier" Type .

        ArrayList<ParamDefinition> params = new ArrayList<>();
        int paramIndex = 0;
        if (lookAheadSymbol.type != TokenTypes.RIGHT_PAR) {
            params.add(parseParamDefinition(paramIndex));
            paramIndex++;
            while (lookAheadSymbol.type == TokenTypes.COMMA) {
                match(TokenTypes.COMMA);
                params.add(parseParamDefinition(paramIndex));
                paramIndex++;
            }
        }
        return params;
    }

    public ParamDefinition parseParamDefinition(int paramIndex) throws Exception {
        // Param -> "identifier" Type
        Symbol identifier = match(TokenTypes.IDENTIFIER);
        Type type = parseType(); // Post typing
        return new ParamDefinition(identifier, type, paramIndex, identifier.line, identifier.column);
    }

    public Block parseBlock() throws Exception {
        //Block -> "{" Stmts ReturnStmt "}" .
        //Stmts -> Stmt Stmts | .
        //Stmt -> IfStmt | ForLoop | WhileLoop | FunctionDefinition | BaseStatement ";" .
        //ReturnStmt -> "return" ReturnTail .
        //ReturnTail -> ";" | Expression ";" .

        match(TokenTypes.LEFT_BRACKET);
        ArrayList<Statement> statements = new ArrayList<>();
        ReturnStatement returnStatement = null;

        while (lookAheadSymbol.type != TokenTypes.RIGHT_BRACKET) {
            if (lookAheadSymbol.type == TokenTypes.RETURN) {
                Symbol retSymbol = match(TokenTypes.RETURN);
                if (lookAheadSymbol.type == TokenTypes.SEMICOLON) {
                    match(TokenTypes.SEMICOLON);
                    returnStatement = new ReturnStatement(null, retSymbol.line, retSymbol.column);
                    break;
                }
                Expression returnExpression = parseExpression();
                match(TokenTypes.SEMICOLON);
                returnStatement = new ReturnStatement(returnExpression, retSymbol.line, retSymbol.column);
            } else {
                statements.add(parseStatement());
            }
        }
        match(TokenTypes.RIGHT_BRACKET);
        return new Block(statements, returnStatement, lookAheadSymbol.line, lookAheadSymbol.column);
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
            // BaseStatement -> VariableDeclaration | variableAssignment | RecordDefinition | free IdentifierAccess | Expression ";"
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
//        if (lookAheadSymbol.type != TokenTypes.RIGHT_PAR) {
//            if (lookAheadSymbol.type == TokenTypes.AND || lookAheadSymbol.type == TokenTypes.OR) {
//
//            }
//        }
        match(TokenTypes.RIGHT_PAR);
        Block ifBlock = parseBlock();
        Block elseStatement = parseElseStatement();
        return new IfStatement(condition, ifBlock, elseStatement, condition.line, condition.column);
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

        // * NEW
        // ForCondition -> Expression "," Expression "," Expression "," Expression .

        // OLD
        // ForCondition -> "identifier" "," NumType "," NumType "," NumType .
        // NumType -> "int" | "float" .
        match(TokenTypes.FOR);
        match(TokenTypes.LEFT_PAR);
        Symbol identifier = match(TokenTypes.IDENTIFIER);
        match(TokenTypes.COMMA);
        // Parse the start, end, and step values, they could be int or float literals, or identifiers
//        NumType startType = parseNumType();
//        match(TokenTypes.COMMA);
//        NumType endType = parseNumType();
//        match(TokenTypes.COMMA);
//        NumType stepType = parseNumType();
//        Symbol startType = parseNumLiteralOrIdent();
        Expression startExpr = parseExpression();
        match(TokenTypes.COMMA);
//        Symbol endType = parseNumLiteralOrIdent();
        Expression endExpr = parseExpression();
        match(TokenTypes.COMMA);
//        Symbol stepType = parseNumLiteralOrIdent();
        Expression stepExpr = parseExpression();
//        // Check if the types are valid
//        if (startType == null || endType == null || stepType == null) {
//            throw new SyntaxErrorException(
//                "Syntax Error: Expected int or float literal or identifier but found " +
//                lookAheadSymbol.lexeme +
//                " of type " +
//                lookAheadSymbol.type +
//                " at line " +
//                lookAheadSymbol.line +
//                ", column " +
//                lookAheadSymbol.column
//            );
//        }


        match(TokenTypes.RIGHT_PAR);
        Block block = parseBlock();
        return new ForLoop(identifier, startExpr, endExpr, stepExpr, block, identifier.line, identifier.column);
    }

    public Symbol parseNumLiteralOrIdent() throws Exception {
        // NumType -> "int" | "float"
		return switch (lookAheadSymbol.type) {
			case INT_LITERAL -> match(TokenTypes.INT_LITERAL);
			case FLOAT_LITERAL -> match(TokenTypes.FLOAT_LITERAL);
			case IDENTIFIER -> match(TokenTypes.IDENTIFIER);
			default -> null;
		};
    }

    public WhileLoop parseWhileLoop() throws Exception {
        // WhileLoop -> "while" "(" Expression ")" Block .
        match(TokenTypes.WHILE);
        match(TokenTypes.LEFT_PAR);
        Expression condition = parseExpression();
        match(TokenTypes.RIGHT_PAR);
        Block block = parseBlock();
        return new WhileLoop(condition, block, condition.line, condition.column);
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
            return parseVariableDeclaration(true, false, null, false);
        } else if (lookAheadSymbol.type == TokenTypes.IDENTIFIER) {
            // VariableAssignment (using an Access) or non-constant declaration
            Symbol identifier = match(TokenTypes.IDENTIFIER);

            // If the lookahead is not an equal sign, then we have a non-constant declaration or a function call
            if (lookAheadSymbol.type != TokenTypes.ASSIGN && lookAheadSymbol.type != TokenTypes.LEFT_SQUARE_BRACKET && lookAheadSymbol.type != TokenTypes.DOT) {
                if (lookAheadSymbol.type == TokenTypes.LEFT_PAR) {
                    // Function call
                    return parseParamCall(identifier);
                }

                // or it could be something like "i int"
                return parseVariableDeclaration(false, true, identifier, false);
            }

            // otherwise we have a variable assignment
            Access access = (Access) parseAccess(true, identifier);
            match(TokenTypes.ASSIGN);

            Expression expression = parseExpression();
            return new VariableAssignment(access, expression, access.line, access.column);
        } else if (lookAheadSymbol.type == TokenTypes.RECORD) {
            return parseRecord();
        } else if (lookAheadSymbol.type == TokenTypes.FREE) {
            // free IdentifierAccess
            match(TokenTypes.FREE);
            Access access = (Access) parseAccess(false, null);

            return new FreeStatement((IdentifierAccess) access, access.line, access.column);
        }

        return null;
    }

    public FunctionCall parseParamCall(Symbol identifier) throws Exception {
        match(TokenTypes.LEFT_PAR);
        if (lookAheadSymbol.type == TokenTypes.RIGHT_PAR) {
            // empty params
            match(TokenTypes.RIGHT_PAR);
            return new FunctionCall(identifier, new ArrayList<>(), identifier.line, identifier.column);
        }
        ArrayList<ParamCall> params = parseParamsCall();
        match(TokenTypes.RIGHT_PAR);
        return new FunctionCall(identifier, params, identifier.line, identifier.column);
    }

    public Term parseAccess(boolean skipIdent, Symbol identIfSkipped)
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
        Term access = new IdentifierAccess(identifier, identifier.line, identifier.column);

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
				assert access instanceof Access;

				access = new ArrayAccess((Access) access, indexExpr, indexExpr.line, indexExpr.column);
            } else {
                // Field access: .identifier
                match(TokenTypes.DOT);
                Symbol fieldName = match(TokenTypes.IDENTIFIER);

                if (lookAheadSymbol.type == TokenTypes.LEFT_PAR) {
                    // Method access on a record : (Expression)
                    FunctionCall functionCall = parseParamCall(fieldName);
					assert access instanceof Access;
					functionCall.setRecordAccess((Access) access);

					access = functionCall;
                } else {
                    assert access instanceof Access;
                    access = new RecordAccess((Access) access, fieldName, fieldName.line, fieldName.column);
                }
            }
        }

        return access;
    }
}

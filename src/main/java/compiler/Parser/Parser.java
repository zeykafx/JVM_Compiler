package compiler.Parser;

import compiler.Lexer.Lexer;
import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.ASTNode;

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

    }

    public ArrayList<VariableDeclaration> parseConstants() throws Exception {
        ArrayList<VariableDeclaration> constants = new ArrayList<>();
        while (lookAheadSymbol.type == TokenTypes.FINAL) {
            match(TokenTypes.FINAL);
            VariableDeclaration constant = parseVariableDeclaration();
            constants.add(constant);
        }
        return constants;
    }

    public VariableDeclaration parserVariableDeclaration() throws Exception {
        Symbol identifier = match(TokenTypes.IDENTIFIER);
        Type type = parseType();
        match(TokenTypes.ASSIGN);
        Expression expression = parseExpression();
        match(TokenTypes.SEMICOLON);
        return new VariableDeclaration();
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
        //
        //ArrayExpression -> "array" "[" "intval" "]" "of" Type ";" .
        //
        //NewRecord -> "recordNameIdentifier" "(" ParamsCall ")" . # Note: record identifiers start with a capital letter
        //
        //Term -> "(" Expression ")" | IdentifierOrFunctionCall | NewRecord | ConstVal .
        //IdentifierOrFunctionCall -> "identifier" IdentifierOrFunctionCallTail .
        //IdentifierOrFunctionCallTail -> | "(" ParamsCall ")" .
        //
        //UnaryOperator -> "!" | "-" .
        //BinaryOperator -> "+" | "-" | "*" | "/" | "%" | "&&" | "||" | "==" | "!=" | "<" | ">" | "<=" | ">=" .
        //# Operator -> "||" | "&&" | "==" | "!=" | "<" | ">" | "<=" | ">=" | "+" | "-" | "*" | "/" | "%" .
        //ConstVal -> "intval" | "floatval" | "stringval" | "true" | "false" .

        // TODO: handle ArrayExpression and the rest
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
}

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
}

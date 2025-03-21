package compiler.Parser.ASTNodes.Statements.Expressions.Terms;

import compiler.Lexer.Symbol;
import java.util.ArrayList;

public class NewRecord extends Term {

    private final Symbol identifier;
    private final ArrayList<ParamCall> terms;

    public NewRecord(Symbol identifier, ArrayList<ParamCall> terms) {
        this.identifier = identifier;
        this.terms = terms;
    }

    public Symbol getIdentifier() {
        return identifier;
    }

    public ArrayList<ParamCall> getTerms() {
        return terms;
    }

    @Override
    public String toString() {
        return "NewRecord [identifier=" + identifier + ", terms=" + terms + "]";
    }
    
    @Override
    public String prettyPrint(int indent) {
        StringBuilder str = new StringBuilder("  ".repeat(indent) + "NewRecord: " + identifier.lexeme + "(");
        for (ParamCall term : terms) {
            str.append(term.prettyPrint(0)).append(", ");
        }
        if (terms.size() > 0) {
            str.delete(str.length() - 2, str.length());
        }
        str.append(")");
        return str.toString();
    }
}

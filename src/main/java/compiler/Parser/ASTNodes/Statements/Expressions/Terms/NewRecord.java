package compiler.Parser.ASTNodes.Statements.Expressions.Terms;

import compiler.Lexer.Symbol;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

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
        StringBuilder str = new StringBuilder("  ".repeat(indent) + "NewRecordInstance: \n");
        for (ParamCall term : terms) {
            str.append(term.prettyPrint(indent+1)).append("\n");
        }

        return str.toString();
    }

    @Override
    public SemType accept(Visitor<SemType> v, SymbolTable table) throws SemanticException {
        return v.visitNewRecord(this, table);
    }
}

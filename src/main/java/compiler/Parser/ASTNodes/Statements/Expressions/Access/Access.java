package compiler.Parser.ASTNodes.Statements.Expressions.Access;

import compiler.CodeGen.SlotTable;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.Term;
import compiler.SemanticAnalysis.Visitor;

public abstract class Access extends Term {
	public Access(int line, int column) {
		super(line, column);
	}
}

package compiler.Parser.ASTNodes.Statements.Expressions.Access;

import compiler.Parser.ASTNodes.Statements.Expressions.Terms.Term;

public abstract class Access extends Term {
	public Access(int line, int column) {
		super(line, column);
	}
}

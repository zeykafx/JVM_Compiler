package compiler.Parser.ASTNodes.Statements.Expressions.Terms;

import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public abstract class Term extends Expression {

	public Term(int line, int column) {
		super(line, column);
	}

}

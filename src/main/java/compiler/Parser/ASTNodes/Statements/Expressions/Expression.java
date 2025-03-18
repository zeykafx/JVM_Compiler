package compiler.Parser.ASTNodes.Statements.Expressions;

import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.ASTNode;

public class Expression extends ASTNode {

	private final Term leftTerm;
	private final Operator operator;
	private final Term rightTerm;


}

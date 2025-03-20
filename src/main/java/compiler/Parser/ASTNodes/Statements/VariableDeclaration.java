package compiler.Parser.ASTNodes.Statements;


import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.ASTNode;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;
import compiler.Parser.ASTNodes.Types.Type;

public class VariableDeclaration extends Statement {
	Type type;
	Symbol name;
	Expression value;
	boolean isConstant;

	public VariableDeclaration(Symbol name, Type type, Expression value, boolean isConstant) {
		this.name = name;
		this.type = type;
		this.value = value;
		this.isConstant = isConstant;
	}

	public VariableDeclaration(Symbol name, Type type, Expression value) {
		this.name = name;
		this.type = type;
		this.value = value;
		this.isConstant = false;
	}

	public Type getType() {
		return type;
	}

	public Symbol getName() {
		return name;
	}

	public Expression getValue() {
		return value;
	}

	public boolean isConstant() {
		return isConstant;
	}

	@Override
	public String toString() {
		String constant = isConstant ? "Constant, " : "Variable, ";
		return "Variable, " + name.lexeme + ", " + constant + type.symbol + ", " + value.toString();
	}
}

// final i int = 3;

// final j float = 3.2*5.0;

// final k int = i*3;

// final message string = "Hello";

// final isEmpty bool = true;
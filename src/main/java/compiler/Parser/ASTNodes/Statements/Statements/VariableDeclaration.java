package compiler.Parser.ASTNodes.Statements.Statements;


import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;
import compiler.Parser.ASTNodes.Types.Type;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

public class VariableDeclaration extends Statement {
	Type type;
	Symbol name;
	Expression value;
	boolean isConstant;
	boolean hasValue;

	boolean isGlobal = false;
	public boolean conversionNeeded = false;

	public VariableDeclaration(Symbol name, Type type, Expression value, boolean isConstant, boolean isGlobal, int line, int column) {
		super(line, column);
		this.name = name;
		this.type = type;
		this.value = value;
		this.isConstant = isConstant;
		this.isGlobal = isGlobal;
		this.hasValue = value != null;
	}

	public VariableDeclaration(Symbol name, Type type, Expression value, boolean isConstant, int line, int column) {
		super(line, column);
		this.name = name;
		this.type = type;
		this.value = value;
		this.isConstant = isConstant;
		this.isGlobal = false;
		this.hasValue = value != null;
	}

	public VariableDeclaration(Symbol name, Type type, Expression value, int line, int column) {
		super(line, column);

		this.name = name;
		this.type = type;
		this.value = value;
		this.isConstant = false;
		this.isGlobal = false;
		this.hasValue = value != null;
	}

	public VariableDeclaration(Symbol name, Type type, int line, int column) {
		super(line, column);

		this.name = name;
		this.type = type;
		this.value = null;
		this.isConstant = false;
		this.isGlobal = false;
		this.hasValue = false;
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

	public boolean hasValue() {
		return hasValue;
	}

	public boolean isConstant() {
		return isConstant;
	}

	public boolean isGlobal() {
		return isGlobal;
	}

	public void setGlobal(boolean global) {
		isGlobal = global;
	}

	@Override
	public String toString() {
		String constant = isConstant ? "Constant, " : "Variable, ";
		if (value == null) {
			return constant + name.lexeme + ", " + type.symbol + ", null";
		}
		return "Variable, " + name.lexeme + ", " + constant + type.symbol + ", " + value.toString();
	}
	
	@Override
    public String prettyPrint(int indent) {
        StringBuilder sb = new StringBuilder();
        String typeStr = isConstant ? "Constant" : "Variable";
        sb.append("  ".repeat(indent)).append(typeStr).append(", ").append(name.lexeme).append("\n");
        
        sb.append("  ".repeat(indent + 1)).append("Type: ").append(type.prettyPrint(0)).append("\n");
        
        if (hasValue) {
            sb.append("  ".repeat(indent + 1)).append("Value:\n");
            sb.append(value.prettyPrint(indent + 2));
        }
        
        return sb.toString();
    }

	@Override
	public <R, T> R accept(Visitor<R, T> v, T table) throws Exception {
		return v.visitVariableDeclaration(this, table);
	}
}

// final i int = 3;

// final j float = 3.2*5.0;

// final k int = i*3;

// final message string = "Hello";

// final isEmpty bool = true;
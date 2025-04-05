package compiler.SemanticAnalysis.Types;

import compiler.Lexer.Symbol;

import java.util.Objects;

public class SemType {

	public String type;
	public Boolean isConstant = false;

	public SemType(String type) {
		this.type = type;
		this.isConstant = false;
	}

	public SemType(String type, Boolean isConstant) {
		this.type = type;
		this.isConstant = isConstant;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return type;
	}

	public Boolean getConstant() {
		return isConstant;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		SemType semType = (SemType) o;
		return Objects.equals(type, semType.type) && Objects.equals(isConstant, semType.isConstant);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, isConstant);
	}
}

package compiler.SemanticAnalysis.Types;


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

		// ints can be made equivalent to floats, but not the opposite
		if (type.equals("int") && semType.type.equals("float")) {
			return true;
		}

		// ints and floats are equivalent to the num type
		if ((type.equals("int") || type.equals("float") || type.equals("num")) && semType.type.equals("num")) {
			return true;
		}

		return Objects.equals(type, semType.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, isConstant);
	}
}

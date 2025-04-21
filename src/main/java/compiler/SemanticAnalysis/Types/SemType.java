package compiler.SemanticAnalysis.Types;


import java.util.Objects;

public class SemType {

	public String type;
	public Boolean isConstant = false;
	public Boolean toConvert = false;

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

	public void setIsConstant(Boolean isConstant) {
		this.isConstant = isConstant;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		SemType semType = (SemType) o;

		// ints can be made equivalent to floats, but not the opposite
//		if (type.equals("int") && semType.type.equals("float")) {
//			return true;
//		}
//
//		// ints can be made equivalent to floats, but not the opposite
//		if (type.equals("float") && semType.type.equals("int")) {
//			return true;
//		}

		// ints and floats are equivalent to the num type
		if ((type.equals("int")  || type.equals("float") || type.equals("num")) && semType.type.equals("num")) {
			return true;
		}

		return Objects.equals(type, semType.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type);
	}

	// Inspired by the Norswap compiler https://github.com/norswap/sigh/blob/master/src/norswap/sigh/bytecode/TypeUtils.java#L97
	public String fieldDescriptor () {
		return switch (type) {
			case "int" -> "I"; // int
			case "bool" -> "Z"; // booleans
			case "float" -> "F"; // float
			case "void" -> "V"; // void
			case "string" -> "Ljava/lang/String;";
			default -> throw new Error("unreachable");
		};
	}
}

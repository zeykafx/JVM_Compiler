package compiler.SemanticAnalysis.Types;

public class ArraySemType extends SemType {
	SemType elementSemType;
	int size;

	public ArraySemType(SemType elementSemType, int size) {
		super("array");
		this.elementSemType = elementSemType;
		this.size = size;
	}

	public ArraySemType(SemType elementSemType) {
		super("array");
		this.elementSemType = elementSemType;
	}

	public SemType getElementSemType() {
		return elementSemType;
	}

	@Override
	public String toString() {
		return "ArraySemType{" +
				"elementSemType=" + elementSemType +
				", size=" + size +
				", type='" + type + '\'' +
				", isConstant=" + isConstant +
				'}';
	}

	public String fieldDescriptor () {
		return "[" + elementSemType.fieldDescriptor();
	}
}

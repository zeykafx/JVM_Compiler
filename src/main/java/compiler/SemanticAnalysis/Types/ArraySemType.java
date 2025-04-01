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
}

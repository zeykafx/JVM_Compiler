package compiler.SemanticAnalysis.Types;

public class ArrayType extends Type {
	Type elementType;
	int size;
	public ArrayType(String returnType, Type elementType, int size) {
		super(returnType);
		this.elementType = elementType;
		this.size = size;
	}
}

package compiler.SemanticAnalysis.Types;

public class FunctionType extends Type {
	Type[] paramTypes;


	public FunctionType(String type) {
		super(type);
	}
}

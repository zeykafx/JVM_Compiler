package compiler.SemanticAnalysis.Types;

public class FunctionSemType extends SemType {
	SemType[] paramSemTypes;

	public FunctionSemType(String type, SemType[] paramSemTypes) {
		// this.type is the return type of the function
		super(type);
		this.paramSemTypes = paramSemTypes;
	}

	public SemType[] getParamSemTypes() {
		return paramSemTypes;
	}
}

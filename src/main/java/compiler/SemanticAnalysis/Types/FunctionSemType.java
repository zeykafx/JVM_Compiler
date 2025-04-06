package compiler.SemanticAnalysis.Types;

public class FunctionSemType extends SemType {
	SemType[] paramSemTypes;
	SemType retType;

	public FunctionSemType(SemType retType, SemType[] paramSemTypes) {
		// this.type is the return type of the function
		super("function");
		this.retType = retType;
		this.paramSemTypes = paramSemTypes;
	}

	public SemType[] getParamSemTypes() {
		return paramSemTypes;
	}

	public SemType getRetType() {
		return retType;
	}
}

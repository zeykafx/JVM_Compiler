package compiler.SemanticAnalysis.Types;

import java.util.Arrays;

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

	@Override
	public String toString() {
		return "FunctionSemType{" +
				"isConstant=" + isConstant +
				", type='" + type + '\'' +
				", paramSemTypes=" + Arrays.toString(paramSemTypes) +
				", retType=" + retType +
				'}';
	}

	@Override
	public String fieldDescriptor() {
		StringBuilder descriptor = new StringBuilder();
		descriptor.append("(");
		for (SemType paramType : paramSemTypes) {
			descriptor.append(paramType.fieldDescriptor());
		}
		descriptor.append(")");
		descriptor.append(retType.fieldDescriptor());
		return descriptor.toString();
	}
}

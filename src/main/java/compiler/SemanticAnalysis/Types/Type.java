package compiler.SemanticAnalysis.Types;

public class Type {

	public String type;

	public Type(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return type;
	}
}

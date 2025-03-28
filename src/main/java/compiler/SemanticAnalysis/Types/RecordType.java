package compiler.SemanticAnalysis.Types;

import java.util.HashMap;

public class RecordType extends Type {
	HashMap<String,Type> fields;

	public RecordType(String type, HashMap<String,Type> fields) {
		super(type);
		this.fields = fields;
	}

	@Override
	public String toString() {
		return "RecordType{" +
				"fields=" + fields +
				", type='" + type + '\'' +
				'}';
	}

	// fields.values == fields2.values [int int int] == [int int int]
}

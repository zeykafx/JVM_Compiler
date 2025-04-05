package compiler.SemanticAnalysis.Types;

import java.util.HashMap;

public class RecordSemType extends SemType {
	public HashMap<String, SemType> fields;

	public RecordSemType(HashMap<String, SemType> fields) {
		super("rec");
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

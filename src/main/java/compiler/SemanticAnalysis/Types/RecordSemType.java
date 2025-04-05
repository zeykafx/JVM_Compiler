package compiler.SemanticAnalysis.Types;

import java.util.TreeMap;

public class RecordSemType extends SemType {
	public TreeMap<String, SemType> fields;

	public RecordSemType(TreeMap<String, SemType> fields) {
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

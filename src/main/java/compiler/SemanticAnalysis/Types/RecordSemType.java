package compiler.SemanticAnalysis.Types;

import java.util.TreeMap;

public class RecordSemType extends SemType {
	public TreeMap<String, SemType> fields;
	public String identifier;

	public RecordSemType(TreeMap<String, SemType> fields, String identifier) {
		super("rec");
		this.fields = fields;
		this.identifier = identifier;
	}

	@Override
	public String toString() {
		return "RecordSemType{" +
				"identifier="+identifier +
				", fields=" + fields +
				", type='" + type + '\'' +
				'}';
	}

	// fields.values == fields2.values [int int int] == [int int int]
}

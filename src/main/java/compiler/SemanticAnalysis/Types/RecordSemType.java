package compiler.SemanticAnalysis.Types;

import java.util.Objects;
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

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		RecordSemType that = (RecordSemType) o;
		return Objects.equals(fields, that.fields) && Objects.equals(identifier, that.identifier);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), fields, identifier);
	}

	// fields.values == fields2.values [int int int] == [int int int]
}

package compiler.SemanticAnalysis.Types;

import org.objectweb.asm.Type;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.TreeMap;
import static org.objectweb.asm.Opcodes.*;

public class RecordSemType extends SemType {
	public LinkedHashMap<String, SemType> fields;
	public String identifier;

	public RecordSemType(LinkedHashMap<String, SemType> fields, String identifier) {
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

	public String recordFielDesc(String fieldIdent) {
		return fields.get(fieldIdent).fieldDescriptor();
	}

	@Override
	public String fieldDescriptor() {

//		StringBuilder descriptor = new StringBuilder();
//		descriptor.append("(");
//		for (SemType recordDef : fields.values()) {
//			descriptor.append(recordDef.fieldDescriptor());
//		}
//		descriptor.append(")");
//		descriptor.append("V");
//		descriptor.append("Lmylang/types/");
//		descriptor.append(identifier);
//		descriptor.append(";");

		return "L" + identifier + ";";
//		descriptor.append(retType.fieldDescriptor());
//		return descriptor.toString();
	}

	public String constructorFieldDescriptor() {
		StringBuilder descriptor = new StringBuilder();
		descriptor.append("(");
		for (SemType recordDef : fields.values()) {
			descriptor.append(recordDef.fieldDescriptor());
		}
		descriptor.append(")");
		descriptor.append("V");
//		descriptor.append("Lmylang/types/");
//		descriptor.append(identifier);
//		descriptor.append(";");
		return descriptor.toString();
	}


	public org.objectweb.asm.Type asmType () {
//		return org.objectweb.asm.Type.getType(fieldDescriptor());
		return org.objectweb.asm.Type.getType(RecordSemType.class);
	}
}

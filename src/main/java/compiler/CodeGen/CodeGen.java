package compiler.CodeGen;

import compiler.Lexer.Symbol;
import compiler.Lexer.TokenTypes;
import compiler.Parser.ASTNodes.ASTNode;
import compiler.Parser.ASTNodes.Block;
import compiler.Parser.ASTNodes.Program;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.Access;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.ArrayAccess;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.IdentifierAccess;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.RecordAccess;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.ArrayExpression;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.BinaryExpression;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.Expression;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.UnaryExpression;
import compiler.Parser.ASTNodes.Statements.Expressions.Operators.BinaryOperator;
import compiler.Parser.ASTNodes.Statements.Expressions.Operators.UnaryOperator;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.*;
import compiler.Parser.ASTNodes.Statements.Statements.*;
import compiler.Parser.ASTNodes.Types.NumType;
import compiler.Parser.ASTNodes.Types.Type;
import compiler.SemanticAnalysis.Types.ArraySemType;
import compiler.SemanticAnalysis.Types.FunctionSemType;
import compiler.SemanticAnalysis.Types.RecordSemType;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.objectweb.asm.Opcodes.*;

public class CodeGen implements Visitor<Void, SlotTable> {

	final SemType intType = new SemType("int");
	final SemType floatType = new SemType("float");
	final SemType numType = new SemType("num");
	final SemType numOrBoolType = new SemType("numOrBool");
	final SemType stringType = new SemType("string");
	final SemType boolType = new SemType("bool");
	final SemType voidType = new SemType("void");
	final SemType anyType = new SemType("any");
	final SemType recType = new SemType("rec");


	private ClassWriter cw;
	private ClassWriter structCw;
	private MethodVisitor mv;
	private boolean isRecordMethod = false;
	private String instanceName;
	private boolean isMvTopLevel;
	private SlotTable slotTable;
	private final Map<String, SemType> constantsAndGlobals;
	private final LinkedHashMap<String, ClassWriter> structs;
	private final String filePath;
	private final String className;

	public CodeGen(String filePath, String className) {
		this.slotTable = null;
		this.constantsAndGlobals = new HashMap<>();
		this.structs = new LinkedHashMap<>();
		this.filePath = filePath;
		this.className = className;
	}

	public void generateCode(ASTNode root) throws Exception {
		try {
			root.accept(this, slotTable);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(3);
		}
	}

	@Override
	public Void visitProgram(Program program, SlotTable localTable) throws Exception {

		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		cw.visit(V1_8, ACC_PUBLIC, this.className, null, "java/lang/Object", null);

		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		mv.visitInsn(RETURN);
		mv.visitMaxs(0,0);
		mv.visitEnd();

		mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		mv.visitCode();

		slotTable = new SlotTable(new AtomicReference<>(1), null);

		// define the constants, globals, records from the main function, i.e., static fields will have their value assigned from the main function
		// first visit the constants
		for (VariableDeclaration constant : program.getConstants()) {
			constant.accept(this, slotTable);
		}

		for (RecordDefinition record : program.getRecords()) {
			record.accept(this, slotTable);
		}

		for (VariableDeclaration global : program.getGlobals()) {
			global.accept(this, slotTable);
		}

		// call the actual main function
		mv.visitMethodInsn(INVOKESTATIC, className, "main", "()V", false);

		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);

		slotTable = new SlotTable(new AtomicReference<>(1), null);

		// actual main function
		mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "()V", null, null);
		mv.visitCode();

		isMvTopLevel = true;

		// function declarations save and restore the method visitor
		for (FunctionDefinition function : program.getFunctions()) {
			function.accept(this, slotTable);
		}

		mv.visitInsn(RETURN); // return void from main
		mv.visitMaxs(0, 0); // let ASM calculate the size needed on the stack and of the slots.
		mv.visitEnd();
		cw.visitEnd();

		byte[] bytearray = cw.toByteArray();
		try (FileOutputStream outputStream = new FileOutputStream(filePath + className.toLowerCase() + ".class") ) {
			outputStream.write(bytearray);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(3);
		};

		// iterate over the structs and store them in files
		for (Map.Entry<String, ClassWriter> entry : structs.entrySet()) {
			String structName = entry.getKey();
			ClassWriter structCw = entry.getValue();

			byte[] structByteArray = structCw.toByteArray();
			try (FileOutputStream outputStream = new FileOutputStream(filePath + structName.toLowerCase() + ".class") ) {
				outputStream.write(structByteArray);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(3);
			};
		}

		return null;
	}

	@Override
	public Void visitVariableDeclaration(VariableDeclaration variableDeclaration, SlotTable localTable) throws Exception {
		// create the bytecode
		if (variableDeclaration.isConstant() || variableDeclaration.isGlobal()) {
			boolean isConstant = variableDeclaration.isConstant();
			int access = ACC_PUBLIC | ACC_STATIC;
			if (isConstant){
				access |= ACC_FINAL;
			}

			// if the variable declaration is global, declare it as a constant or a global
			cw.visitField(access,
					variableDeclaration.getName().lexeme,
					variableDeclaration.semtype.fieldDescriptor(),
					null, // no generic signature
					null // no initial value
			).visitEnd();

			loadVarDeclValue(variableDeclaration, slotTable);

			mv.visitFieldInsn(PUTSTATIC, className, variableDeclaration.getName().lexeme, variableDeclaration.semtype.fieldDescriptor());

			// NOTE: Don't add to the local table because it's not a local variable!

			variableDeclaration.semtype.setIsConstant(variableDeclaration.isConstant());
			variableDeclaration.semtype.setGlobal(variableDeclaration.isGlobal());
			constantsAndGlobals.put(variableDeclaration.getName().lexeme, variableDeclaration.semtype);

		} else {
			loadVarDeclValue(variableDeclaration, localTable);

			org.objectweb.asm.Type asmType = variableDeclaration.semtype.asmType();
			int idx = localTable.addSlot(variableDeclaration.getName().lexeme, asmType);

			mv.visitVarInsn(asmType.getOpcode(ISTORE), idx);
		}

		return null;
	}

	private void loadVarDeclValue(VariableDeclaration variableDeclaration, SlotTable localTable) throws Exception {
		if (variableDeclaration.hasValue()) {
			// load on the stack if it has a value
			variableDeclaration.getValue().accept(this, localTable);
		} else {
			// load null on the stack
			if (variableDeclaration.semtype.equals(stringType) || variableDeclaration.semtype instanceof RecordSemType || variableDeclaration.semtype instanceof ArraySemType) {
				mv.visitInsn(ACONST_NULL);
			} else if (variableDeclaration.semtype.equals(intType)) {
				mv.visitInsn(ICONST_0);
			} else if (variableDeclaration.semtype.equals(floatType)) {
				mv.visitInsn(FCONST_0);
			} else if (variableDeclaration.semtype.equals(boolType)) {
				mv.visitInsn(ICONST_0);
			} else {
				throw new RuntimeException("Unexpected type: " + variableDeclaration.semtype.type);
			}
		}
	}

	@Override
	public Void visitVariableAssignment(VariableAssignment variableAssignment, SlotTable localTable) throws Exception {
//		variableAssignment.getAccess().accept(this, localTable);
		if (variableAssignment.getAccess() instanceof RecordAccess recordAccess) { // p.x = 0;
			recordAccess.getHeadAccess().accept(this, localTable);
		}
		if (variableAssignment.getAccess() instanceof ArrayAccess arrayAccess && !(arrayAccess.getHeadAccess().semtype.equals(stringType))) {
			arrayAccess.accept(this, localTable);
		}

		// don't visit the expression if we actually have a string array indexing (e.g. myString[2] = "2")
		if (!(variableAssignment.getAccess() instanceof ArrayAccess arrayAccess && arrayAccess.getHeadAccess().semtype.equals(stringType))) {
			// then visit the expression
			variableAssignment.getExpression().accept(this, localTable);
			implicitTypeConversion(variableAssignment.getAccess().semtype, variableAssignment.getExpression().semtype);
		}


		// then store the results
		switch (variableAssignment.getAccess()) {
			case IdentifierAccess identifierAccess:
				if (variableAssignment.semtype.isGlobal || variableAssignment.semtype.isConstant) {
					SemType constSemType = constantsAndGlobals.get(identifierAccess.getIdentifier().lexeme);
					mv.visitFieldInsn(PUTSTATIC, className, identifierAccess.getIdentifier().lexeme, constSemType.fieldDescriptor());
				} else {
					int index = localTable.lookup(identifierAccess.getIdentifier().lexeme);
					if (index == -1) {
						// unexpected error : the term should be in the slot table.
						throw new RuntimeException("Unexpected error : the variable " + identifierAccess.getIdentifier().lexeme + " is not in the slot table.");
					}
					org.objectweb.asm.Type asmType = variableAssignment.semtype.asmType();
					mv.visitVarInsn(asmType.getOpcode(ISTORE), index);
				}
				break;
			case RecordAccess recordAccess:

				RecordSemType recordSemType = (RecordSemType) recordAccess.getHeadAccess().semtype;
				String accessDesc = recordSemType.recordFielDesc(recordAccess.getIdentifier().lexeme);

				mv.visitFieldInsn(PUTFIELD, recordSemType.identifier, recordAccess.getIdentifier().lexeme, accessDesc);
				break;
			case ArrayAccess arrayAccess:

				if (arrayAccess.getHeadAccess().semtype.equals(stringType)) {
					return stringArrayIndexingModification(variableAssignment, localTable, arrayAccess);
				}

				ArraySemType arraySemType = (ArraySemType) arrayAccess.getHeadAccess().semtype;
				org.objectweb.asm.Type arrayAsmType = arraySemType.getElementSemType().asmType();
				mv.visitInsn(arrayAsmType.getOpcode(IASTORE));
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + variableAssignment.getAccess());
		}


		return null;
	}

	private Void stringArrayIndexingModification(VariableAssignment variableAssignment, SlotTable localTable, ArrayAccess arrayAccess) throws Exception {
		// ex:  str[9] = ord("A");

		// StringBuilder string = new StringBuilder(str);
		// string.setCharAt(index, ch);
		// string.toString()
		int stringBuilderSlot = localTable.currentSlot.get()+1;
		localTable.currentSlot.set(stringBuilderSlot + 1);
		mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
		mv.visitInsn(DUP); // duplicate to get a ref on the stack

		// load the string to pass in the StringBuilder

		arrayAccess.getHeadAccess().accept(this, localTable);

		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
		mv.visitVarInsn(ASTORE, stringBuilderSlot);

		// load the string builder
		mv.visitVarInsn(ALOAD, stringBuilderSlot);

		// load the index
		arrayAccess.getIndexExpression().accept(this, localTable);

		// load the new char
		variableAssignment.getExpression().accept(this, localTable);

		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "setCharAt", "(IC)V", false);

		mv.visitVarInsn(ALOAD, stringBuilderSlot);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);

		// load in place of the original string
		arrayAccess.getHeadAccess().willStore = true;
		arrayAccess.getHeadAccess().accept(this, localTable);

		return null;
	}

	private void implicitTypeConversion(SemType left, SemType right) {
		if (left.equals(floatType) && right.equals(intType)) {
			mv.visitInsn(I2F);
		}
	}

	@Override
	public Void visitRecordDefinition(RecordDefinition recordDefinition, SlotTable localTable) throws Exception {
		structCw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		structCw.visit(V1_8, ACC_PUBLIC, recordDefinition.getIdentifier().lexeme, null, "java/lang/Object", null);
		for (RecordFieldDefinition recordField: recordDefinition.getFields()) {
			recordField.accept(this, null);
		}

		// ---------- init ----------

		StringBuilder descriptor = new StringBuilder();
		descriptor.append("(");
		for (RecordFieldDefinition recordDef : recordDefinition.getFields()) {
			descriptor.append(recordDef.semtype.fieldDescriptor());
		}
		descriptor.append(")");
		descriptor.append("V");

		MethodVisitor init = structCw.visitMethod(ACC_PUBLIC, "<init>", descriptor.toString(), null, null);
		init.visitCode();
		init.visitVarInsn(ALOAD, 0); // this
		init.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

		for (RecordFieldDefinition fieldDefinition : recordDefinition.getFields()) {
			init.visitVarInsn(ALOAD, 0); // this
			// Note: the field index starts at 0 for us, but it must start at 1 here, so we just increase by 1
			init.visitVarInsn(fieldDefinition.semtype.asmType().getOpcode(ILOAD), fieldDefinition.getFieldIndex()+1);
			init.visitFieldInsn(PUTFIELD, recordDefinition.getIdentifier().lexeme, fieldDefinition.getIdentifier().lexeme, fieldDefinition.semtype.fieldDescriptor());
		}

		init.visitInsn(RETURN);
		init.visitMaxs( 0, 0);
		init.visitEnd();

		// ---------- end of init ----------


		// ---------- ToString method on the records ----------
		MethodVisitor toStringmv = structCw.visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", null,null);
		toStringmv.visitCode();

		// the start of the string returned is the record type + {
		toStringmv.visitLdcInsn(recordDefinition.getIdentifier().lexeme +" {");
		toStringmv.visitVarInsn(ASTORE, 1); // store the string in slot 1

		// add each field to the string in slot 1
		for (RecordFieldDefinition recordDef : recordDefinition.getFields()) {
			toStringmv.visitVarInsn(ALOAD, 1); // load the current string

			// add the name of the identifier + "= "
			toStringmv.visitLdcInsn(recordDef.getIdentifier().lexeme + "= ");
			// concatenate the string above to the current string
			toStringmv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);
			toStringmv.visitVarInsn(ASTORE, 1); // store this back (NOTE: this is not required, because we load it again right after, but we do this just to be safe)

			toStringmv.visitVarInsn(ALOAD, 1); // load again
			toStringmv.visitVarInsn(ALOAD, 0); // load "this"
			// load the value of the field on the stack
			toStringmv.visitFieldInsn(GETFIELD, recordDefinition.getIdentifier().lexeme, recordDef.getIdentifier().lexeme, recordDef.semtype.fieldDescriptor());

			// transform the value to a string
			// depending on the value, we will either call the "[Integer, Float, Boolean].toString" methods, or the record's own .toString method
			// or if the field is an array, we call the "Arrays.toString" method, and if the elements of the array are records, then we set the field descriptor of the elements
			// to be objects so that the "Arrays.toString" will call their ".toString" method.
			int access = INVOKESTATIC;
			String className = "java/lang/";

			className += switch (recordDef.semtype.type) {
				case "int" -> "Integer";
				case "float" -> "Float";
				case "bool" -> "Boolean";
				case "string" -> "String";
				default -> "unknown";
			};

			// if it's an array, call Arrays.toString
			if (recordDef.semtype instanceof ArraySemType arraySemType) {
				String desc = "(";

				// HACK: if we have an array of records, mark the records desc as object so that it calls their .toString method
				if (arraySemType.getElementSemType() instanceof RecordSemType recordSemType) {
					desc += "[Ljava/lang/Object;)Ljava/lang/String;"; // note the [ that was added, that is because it's a list of objects
					// and arraySemType.fieldDescriptor() already adds it below so it's not needed there
				} else {
					desc += arraySemType.fieldDescriptor() +")Ljava/lang/String;";
				}
				toStringmv.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "toString", desc, false);

			} else if (!recordDef.semtype.equals(stringType)) {
				String desc = "(";
				if (recordDef.semtype instanceof RecordSemType recordSemType) {
					desc += ")Ljava/lang/String;";
					className = recordSemType.identifier;
					access = INVOKEVIRTUAL;
				} else {
					desc += recordDef.semtype.fieldDescriptor() +")Ljava/lang/String;";
				}

				toStringmv.visitMethodInsn(access, className, "toString", desc, false);
			}


			toStringmv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);

			toStringmv.visitVarInsn(ASTORE, 1);

			if (recordDef.getFieldIndex() < recordDefinition.getFields().size() - 1) {
				toStringmv.visitVarInsn(ALOAD, 1);
				toStringmv.visitLdcInsn( ", ");
				toStringmv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);
				toStringmv.visitVarInsn(ASTORE, 1);
			}

		}

		toStringmv.visitVarInsn(ALOAD, 1);

		toStringmv.visitLdcInsn("}");
		toStringmv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);

		toStringmv.visitVarInsn(ASTORE, 1);

		toStringmv.visitVarInsn(ALOAD, 1);
		toStringmv.visitInsn(ARETURN);

		toStringmv.visitMaxs( 0, 0);
		toStringmv.visitEnd();

		// ---------- end of ToString ----------

		// ---------- Equals definition ----------

		MethodVisitor equalsMv = structCw.visitMethod(ACC_PUBLIC, "equals", "(Ljava/lang/Object;)Z", null, null);
		equalsMv.visitCode();

		// if the objects are the same instance, we return true immediately
		Label returnTrueLabel = new Label();
		Label instanceofCheckLabel = new Label();
		equalsMv.visitVarInsn(ALOAD, 0); // this
		equalsMv.visitVarInsn(ALOAD, 1); // obj
		equalsMv.visitJumpInsn(IF_ACMPEQ, returnTrueLabel);

		// if the param isn't an instance of this record, return false
		equalsMv.visitVarInsn(ALOAD, 1); // obj
		equalsMv.visitTypeInsn(INSTANCEOF, recordDefinition.getIdentifier().lexeme);
		Label returnFalseLabel = new Label();
//		equalsMv.visitJumpInsn(IF_ICMPEQ, returnFalseLabel);
		equalsMv.visitJumpInsn(IFEQ, returnFalseLabel);

		// since theyu are the same type, we can cast the parameter to the record type
		equalsMv.visitVarInsn(ALOAD, 1); // obj
		equalsMv.visitTypeInsn(CHECKCAST, recordDefinition.getIdentifier().lexeme);
		equalsMv.visitVarInsn(ASTORE, 2); // store cast object in slot 2

		// go through and compare each field
		for (RecordFieldDefinition recordField : recordDefinition.getFields()) {
			// ints, floats, bools
			// TODO: maybe do this better idk
			if (recordField.semtype.equals(intType) || recordField.semtype.equals(floatType) || recordField.semtype.equals(boolType)) {
				equalsMv.visitVarInsn(ALOAD, 0); // this object
				equalsMv.visitFieldInsn(GETFIELD, recordDefinition.getIdentifier().lexeme, recordField.getIdentifier().lexeme, recordField.semtype.fieldDescriptor());
				equalsMv.visitVarInsn(ALOAD, 2); // other object
				equalsMv.visitFieldInsn(GETFIELD, recordDefinition.getIdentifier().lexeme, recordField.getIdentifier().lexeme, recordField.semtype.fieldDescriptor());

				// subtract the two values, jump to returnFalseLabel if they are not equal
				if (recordField.semtype.equals(floatType)) {
					equalsMv.visitInsn(FCMPL);
					equalsMv.visitJumpInsn(IFNE, returnFalseLabel);
				} else {
					equalsMv.visitJumpInsn(IF_ICMPNE, returnFalseLabel);
				}
			}
			// string, arrays, record types
			// TODO: need to check for nulls
			else {
//				Label nullCheckLabel = new Label();
				equalsMv.visitVarInsn(ALOAD, 0); // this
				equalsMv.visitFieldInsn(GETFIELD, recordDefinition.getIdentifier().lexeme, recordField.getIdentifier().lexeme, recordField.semtype.fieldDescriptor());

//				equalsMv.visitJumpInsn(IFNULL, returnFalseLabel);

				equalsMv.visitVarInsn(ALOAD, 2); // other object
				equalsMv.visitFieldInsn(GETFIELD, recordDefinition.getIdentifier().lexeme, recordField.getIdentifier().lexeme, recordField.semtype.fieldDescriptor());
				equalsMv.visitMethodInsn(INVOKESTATIC, "java/util/Objects", "equals", "(Ljava/lang/Object;Ljava/lang/Object;)Z", false);
				equalsMv.visitJumpInsn(IFEQ, returnFalseLabel);
			}
		}

		// return true if all fields are equal
		equalsMv.visitLabel(returnTrueLabel);
		equalsMv.visitInsn(ICONST_1);
		equalsMv.visitInsn(IRETURN);

		// return false
		equalsMv.visitLabel(returnFalseLabel);
		equalsMv.visitInsn(ICONST_0);
		equalsMv.visitInsn(IRETURN);

		equalsMv.visitMaxs(0, 0);
		equalsMv.visitEnd();

		// ---------- end of Equals ----------



		structCw.visitEnd();
		structs.put(recordDefinition.getIdentifier().lexeme, structCw);
//		structCw = null;
		return null;
	}

	@Override
	public Void visitRecordFieldDefinition(RecordFieldDefinition recordFieldDefinition, SlotTable localTable) throws Exception {
		structCw.visitField(ACC_PUBLIC, recordFieldDefinition.getIdentifier().lexeme, recordFieldDefinition.semtype.fieldDescriptor(), null, null);
		return null;
	}

	@Override
	public Void visitFunctionDefinition(FunctionDefinition functionDefinition, SlotTable localTable) throws Exception {

		MethodVisitor oldMv = mv;
		ClassWriter oldCw = cw;
		SlotTable funcTable = localTable;

		// the main function is already defined, so we skip the function definition
		if (!functionDefinition.getName().lexeme.equals("main")) {
			isMvTopLevel = false;
			isRecordMethod = functionDefinition.hasInstanceRef();

			if (isRecordMethod) {
				String recordName = functionDefinition.getInstanceRef().getSymbol().lexeme;
				cw = structs.get(recordName); // get the structure's cw
				instanceName = functionDefinition.getInstanceName().lexeme;
			}

			// NOTE: The slots of other functions start at 0, I think it's because they don't have self, except for record methods
			funcTable = new SlotTable(new AtomicReference<>(isRecordMethod ? 1 : 0), localTable);

			// generate string for descriptor
			FunctionSemType functionSemType = (FunctionSemType) functionDefinition.semtype;
			String descriptor = functionSemType.asmType().getDescriptor();

			int access = ACC_PUBLIC;
			if (!isRecordMethod) {
				access |= ACC_STATIC;
			}

			mv = cw.visitMethod(access, functionDefinition.getName().lexeme, descriptor, null, null);
			mv.visitCode();
		}

		// add the parameters to the slot table
		for (ParamDefinition paramDefinition : functionDefinition.getParamDefinitions()) {
			paramDefinition.accept(this, funcTable);
		}

		// accept block (the block handles the return)
		functionDefinition.getBlock().accept(this, funcTable);

		// this isn't needed for the main function
		if (!functionDefinition.getName().lexeme.equals("main")) {

			if (functionDefinition.getRetSemType().equals(voidType)) {
				// if there is no return statement, we need to return void
				mv.visitInsn(RETURN);
			}

			mv.visitEnd();
			mv.visitMaxs(0, 0);

			mv = oldMv;
			cw = oldCw;
			isRecordMethod = false;
			instanceName = null;
			isMvTopLevel = true;
		}
		return null;
	}

	@Override
	public Void visitParamDefinition(ParamDefinition paramDefinition, SlotTable localTable) throws Exception {
		localTable.addSlot(paramDefinition.getIdentifier().lexeme, paramDefinition.semtype.asmType());
		mv.visitParameter(paramDefinition.getIdentifier().lexeme, ACC_STATIC);

		return null;
	}

	@Override
	public Void visitStatement(Statement statement, SlotTable localTable) throws Exception {
		throw new RuntimeException("this should never be called");
	}

	@Override
	public Void visitBlock(Block block, SlotTable localTable) throws Exception {
		for (Statement stmt : block.getStatements()) {
			stmt.accept(this, localTable);
		}

		ReturnStatement returnStatement = (ReturnStatement) block.getReturnStatement();
		if (returnStatement != null) {
			returnStatement.accept(this, localTable);
		}
		return null;
	}

	@Override
	public Void visitReturnStatement(ReturnStatement returnStatement, SlotTable localTable) throws Exception {
		if (returnStatement.getExpression() != null) {
			returnStatement.getExpression().accept(this, localTable); // load the values on the stack
		}

		// get the return statement that corresponds to the asmType of the return value.
		mv.visitInsn(returnStatement.semtype.asmType().getOpcode(IRETURN));
		return null;
	}

	@Override
	public Void visitIdentifierAccess(IdentifierAccess identifierAccess, SlotTable localTable) throws Exception {
		String identLexeme = identifierAccess.getIdentifier().lexeme;

		if (identLexeme.equals(instanceName) && isRecordMethod && !isMvTopLevel) {
			mv.visitVarInsn(ALOAD, 0); // load this
			return null;
		}

		int index = localTable.lookup(identLexeme);

		// if the identifier is a constant or a global, and it isn't redefined in the local scope, we need to visit the field
		if (constantsAndGlobals.containsKey(identLexeme) && index == -1) {
			SemType constSemType = constantsAndGlobals.get(identLexeme);
			mv.visitFieldInsn(GETSTATIC, className, identLexeme, constSemType.fieldDescriptor());
			return null;
		}

		// load symbol in slot table or insert it
		if (index == -1) {
			// unexpected error : the term should be in the slot table.
			throw new RuntimeException("Unexpected error : the variable " + identLexeme + " is not in the slot table.");
		}

		boolean shouldStore = identifierAccess.willStore;
		SemType semtype = identifierAccess.semtype;
		org.objectweb.asm.Type asmType = semtype.asmType();
		mv.visitVarInsn(asmType.getOpcode(shouldStore ? ISTORE : ILOAD), index);

		return null;
	}

	@Override
	public Void visitRecordAccess(RecordAccess recordAccess, SlotTable localTable) throws Exception {
		Access headAccess = recordAccess.getHeadAccess();
		headAccess.accept(this, localTable);

		RecordSemType recordSemType = (RecordSemType) headAccess.semtype;
		String headAccessDescriptor = recordSemType.recordFielDesc(recordAccess.getIdentifier().lexeme);

		mv.visitFieldInsn(GETFIELD, recordSemType.identifier, recordAccess.getIdentifier().lexeme, headAccessDescriptor);

		return null;
	}

	@Override
	public Void visitArrayAccess(ArrayAccess arrayAccess, SlotTable localTable) throws Exception {
		// ArrayAccess -> "[" Expression "]"

		Access access = arrayAccess.getHeadAccess();
		access.accept(this, localTable);

		arrayAccess.getIndexExpression().accept(this, localTable);

		if (access.semtype.equals(stringType) && !arrayAccess.willStore) {

			// ex : writeln(str[9]);

			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C", false);
			return null;
		}

		if (!arrayAccess.willStore) {
			ArraySemType arraySemType = (ArraySemType) arrayAccess.getHeadAccess().semtype;
			org.objectweb.asm.Type arrayAsmType = arraySemType.getElementSemType().asmType();
			mv.visitInsn(arrayAsmType.getOpcode(IALOAD));
		}


		return null;
	}

	@Override
	public Void visitFunctionCall(FunctionCall functionCall, SlotTable localTable) throws Exception {
		// $ "writeInt" and "writeFloat" take an "int" and "float" as argument respectively, and return nothing or True/False if it succeeded or not (to be decided at the code generation phase; for now you can make an arbitrary choice).
		//$ "write" and "writeln" can take anything as argument (including any primitive type). "writeln" will add an end-of-line at the end of what is written. The output is the same as the other "write" functions
		//$ Hint: writeFloat should accept “-a” as an argument, where “a” is a float variable.
		//$ Function calls can forward-reference functions, even in initializers of global variables, but not in constants.
		//
		//
		switch (functionCall.getIdentifier().lexeme) {
			case "chr":
				functionCall.getParameters().getFirst().accept(this, localTable);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(C)Ljava/lang/String;", false);
				break;
			case "ord":
				functionCall.getParameters().getFirst().accept(this, localTable);
				mv.visitLdcInsn(0); // load char at index 0
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C", false);
				break;
			case "len":
				handleLenCall(functionCall, localTable);
				break;
			case "floor":
				// there doesn't seem to be a simple way to round down a float to an int without using math.floor, so we can just use the f2i instruction that
				// converts the float to an int and rounds down in the process
				functionCall.getParameters().getFirst().accept(this, localTable);
				mv.visitInsn(F2I);
				break;
			case "readInt":
				handleReadCall(intType);
				break;
			case "readFloat":
				handleReadCall(floatType);
				break;
			case "readString":
				handleReadCall(stringType);
				break;
			case "writeFloat":
				handleWriteCall(functionCall, false, true, localTable);
				break;
			case "writeInt": // No break, fall back to "write" case
			case "write":
				handleWriteCall(functionCall, false, false, localTable);
				break;
			case "writeln":
				handleWriteCall(functionCall, true, false, localTable);
				break;
			default:
				int access = INVOKESTATIC;
				String functionClass = className;

				// if this is a record method, visit the identifier access to load the instance on the stack
				if (functionCall.hasRecordAccess()) {
					functionCall.recordAccess.accept(this, localTable);
					access = INVOKEVIRTUAL;
					RecordSemType recordSemType = (RecordSemType) functionCall.recordAccess.semtype;
					functionClass = recordSemType.identifier;
				}

				for (ParamCall paramCall : functionCall.getParameters()) {
					// push the params on the stack
					paramCall.accept(this, localTable);
				}

				FunctionSemType functionSemType = (FunctionSemType) functionCall.semtype;
				String descriptor = functionSemType.asmType().getDescriptor();

				mv.visitMethodInsn(access, functionClass, functionCall.getIdentifier().lexeme, descriptor, false);
				break;
		}
		return null;
	}

	private void handleLenCall(FunctionCall functionCall, SlotTable localTable) throws Exception {
		// the len method can be used on strings or arrays
		// again, the first argument is the only one we care about

		ParamCall paramCall = functionCall.getParameters().getFirst();
		SemType argSemType = paramCall.semtype;

		paramCall.accept(this, localTable);

		if (argSemType.equals(stringType)) {
			// we use the .length method on strings to get their length, it returns an integer on the stack (i think)
			String descriptor = "()I";
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", descriptor, false);
		} else {
			// use the arraylength instruction to the length of the array
			// https://asm.ow2.io/javadoc/org/objectweb/asm/Opcodes.html#ARRAYLENGTH
			mv.visitInsn(ARRAYLENGTH);
		}

	}

	private void handleReadCall(SemType version)  {
		// to read we need to create a scanner object
		mv.visitTypeInsn(NEW, "java/util/Scanner");
		mv.visitInsn(DUP); // duplicate to get a ref on the stack

		// java/lang/System.in:Ljava/io/InputStream;
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");

		// 7: invokespecial #15                 // Method java/util/Scanner."<init>":(Ljava/io/InputStream;)V
		mv.visitMethodInsn(INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);

		String versionDesc = "()" + version.fieldDescriptor();
		String methodName;
		switch (version.type) {
			case "int" -> methodName = "nextInt";
			case "float" -> methodName = "nextFloat";
			case "string" -> methodName = "next";
			default -> methodName = "nextLine";
		}
		// 12: invokevirtual #18   // Method java/util/Scanner.next:()Ljava/lang/String;
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner", methodName, versionDesc, false);
	}

	private void handleWriteCall(FunctionCall functionCall, boolean addNewLine, boolean isFloat, SlotTable localTable) throws Exception {
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");

		StringBuilder functionDesc = new StringBuilder();
		// the write methods have different type descriptors based on what gets passed in as argument
		// I don't think the write functions can have more than one thing as argument, so we can jus take the first element
		SemType argSemType;
		if (!functionCall.getParameters().isEmpty()) {
			ParamCall paramCall = functionCall.getParameters().getFirst();
			// load the value in the params
			paramCall.accept(this, localTable);

			argSemType = paramCall.semtype;

			// if the function is writeFloat, and we passed an Int, we convert the int to a float
			// (though it worked when we called writeFloat with an int, but it would just print an int and not a float)
			if (isFloat && argSemType.equals(intType)) {
				implicitTypeConversion(floatType, intType);
				argSemType = floatType;
			}

		} else {
			// if there was no arguments given in (e.g. "writeln()"), we assume that we want to print an empty string
			argSemType = stringType;
			mv.visitLdcInsn("");
		}

		if (argSemType instanceof ArraySemType arraySemType) {
			String desc = "(";

			// HACK: if we have an array of records, mark the records desc as object so that it calls their .toString method
			if (arraySemType.getElementSemType() instanceof RecordSemType || arraySemType.getElementSemType().equals(stringType)) {
				desc += "[Ljava/lang/Object;)Ljava/lang/String;"; // note the [ that was added, that is because it's a list of objects
				// and arraySemType.fieldDescriptor() already adds it below so it's not needed there
			} else {
				desc += arraySemType.fieldDescriptor() +")Ljava/lang/String;";
			}
			mv.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "toString", desc, false);
		}

		functionDesc.append("(");
		if (argSemType instanceof RecordSemType) {
			functionDesc.append("Ljava/lang/Object;"); // prints the object as "RecordName@<hashcode>"
		} else if (argSemType instanceof ArraySemType) {
			functionDesc.append("Ljava/lang/String;");
		} else {
			functionDesc.append(argSemType.fieldDescriptor());
		}
		functionDesc.append(")");
		functionDesc.append("V"); // the write functions return nothing I guess, if they returned something we'd have to handle it

		String descriptor = functionDesc.toString();
		String functionToCall = "print";
		if (addNewLine) {
			functionToCall += "ln";
		}

		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", functionToCall, descriptor, false);
	}

	@Override
	public Void visitRecordInstantiation(NewRecord newRecord, SlotTable localTable) throws Exception {
		mv.visitTypeInsn(NEW, newRecord.getIdentifier().lexeme);
		mv.visitInsn(DUP);

		for (ParamCall paramCall : newRecord.getTerms()) {
			// push the params on the stack
			paramCall.accept(this, localTable);
		}

		// call the constructor
		RecordSemType recordSemType = (RecordSemType) newRecord.semtype;
		mv.visitMethodInsn(INVOKESPECIAL, newRecord.getIdentifier().lexeme, "<init>", recordSemType.constructorFieldDescriptor(), false);
		return null;
	}

	@Override
	public Void visitParamCall(ParamCall paramCall, SlotTable localTable) throws Exception {
		// load the param on the stack
		paramCall.getParamExpression().accept(this, localTable);
		return null;
	}

	@Override
	public Void visitBinaryExpression(BinaryExpression binaryExpression, SlotTable localTable) throws Exception {
		Label endLabel = new Label();
		binaryExpression.getLeftTerm().accept(this, localTable);
		if (binaryExpression.getLeftTerm().semtype.toConvert) {
			mv.visitInsn(I2F);
		}

		if (binaryExpression.getLeftTerm().semtype.equals(boolType)) {
			// this is the short-circuit evaluation for the and/or operators
			mv.visitInsn(DUP);
			mv.visitJumpInsn(binaryExpression.getOperator().getSymbol().type.equals(TokenTypes.AND) ? IFEQ : IFNE, endLabel);
		}

		binaryExpression.getRightTerm().accept(this, localTable);
		if (binaryExpression.getRightTerm().semtype.toConvert) {
			mv.visitInsn(I2F);
		}
		opCodeGenerator op = new opCodeGenerator(binaryExpression, mv);
		op.generateCode();

		mv.visitLabel(endLabel);
		return null;
	}

	@Override
	public Void visitUnaryExpression(UnaryExpression unaryExpression, SlotTable localTable) throws Exception {
		unaryExpression.getTerm().accept(this, slotTable);

		UnaryOperator unaryOperator = unaryExpression.getOperator();
		switch (unaryOperator.getOperator()) {
			case "!":
				// heavily inspired by the code here https://github.com/norswap/sigh/blob/8301a3a988eb4ddd2667de1c8edef11e1d790709/src/norswap/sigh/bytecode/BytecodeCompiler.java#L499
				Label falseLabel = new Label();
				Label endLabel = new Label();
				mv.visitJumpInsn(IFEQ, falseLabel);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitLabel(falseLabel);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(endLabel);
				break;
			case "-":
				if (unaryExpression.getTerm().semtype.equals(floatType)) {
					mv.visitInsn(FNEG);
				} else {
					// else is sufficient here because the semantic analysis guarantees us that nothing else can be here
					mv.visitInsn(INEG);
				}
				break;
		}
		return null;

	}

	@Override
	public Void visitBinaryOperator(BinaryOperator binaryOperator, SlotTable localTable) throws Exception {
		// bypassed because of the distinction between float and int operation.
		return null;
	}

	@Override
	public Void visitUnaryOperator(UnaryOperator unaryOperator, SlotTable localTable) throws Exception {
		// THIS FUNCTION IS BYPASSED, the relevant code is located in visitUnaryExpression
		return null;
	}

	@Override
	public Void visitArrayExpression(ArrayExpression arrayExpression, SlotTable localTable) throws Exception {
		// ArrayExpression -> "array" "[" "intval" "]" "of" Type ";" .

		// first visit the size expression because, as the name implies, it can be an expression
		arrayExpression.getSizeExpression().accept(this, localTable);

		ArraySemType arraySemType = (ArraySemType) arrayExpression.semtype;
//		String arrayElemDesc = arraySemType.getElementSemType().fieldDescriptor();
		String arrayElemDesc = arrayExpression.getType().symbol.lexeme;

		// note: we can't use asm.Type.getOpcode because it doesn't support NEWARRAY
		switch (arraySemType.getElementSemType().type) {
			case "int":
				mv.visitIntInsn(NEWARRAY, T_INT);
				break;
			case "float":
				mv.visitIntInsn(NEWARRAY, T_FLOAT);
				break;
			case "bool":
				mv.visitIntInsn(NEWARRAY, T_BOOLEAN);
				break;
			case "string":
				mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
				break;
			default:
				mv.visitTypeInsn(ANEWARRAY, arrayElemDesc);
				break;
		}

		return null;
	}

	@Override
	public Void visitConstValue(ConstVal constVal, SlotTable localTable) throws Exception {
		mv.visitLdcInsn(constVal.getValue());

		return null;
	}

	@Override
	public Void visitParenthesesTerm(ParenthesesTerm parenthesesTerm, SlotTable localTable) throws Exception {
		parenthesesTerm.getExpression().accept(this, localTable);

		return null;
	}

	@Override
	public Void visitForLoop(ForLoop forLoop, SlotTable localTable) throws Exception {

		Symbol varSymbol = forLoop.getVariable();
		boolean loopVarIsFloat = forLoop.semtype.equals(floatType);

		// load the start expression on the stack
		Expression startExpr = forLoop.getStart();
		forLoop.getStart().accept(this, localTable);
		implicitTypeConversion(forLoop.semtype, startExpr.semtype);

		// load the loop variable on the stack
		loadOrStoreVarFromSymbol(forLoop, localTable, varSymbol, forLoop.semtype, true); // the varType is in forLoop.semtype

		Label startLabel = new Label();
		Label endLabel = new Label();

		mv.visitLabel(startLabel);

		// check the condition (i.e., that the loop variable is smaller than the end symbol/var)
		// load the loop var
		loadOrStoreVarFromSymbol(forLoop, localTable, varSymbol, forLoop.semtype, false);


		// load the end expression on the stack
		Expression endExpr = forLoop.getEnd();
		forLoop.getEnd().accept(this, localTable);
		implicitTypeConversion(forLoop.semtype, endExpr.semtype);


		// compare the loop variable to the end expression
		// if the loop variable is a float, we need to use fcmpg
		// if the comparison is greater than or equal to, we jump to the end label
		// NOTE: this doesn't work for negative steps because the loop variable will never be greater than the end expression
		// Ideally we should try to figure out if the step expression is negative or not, then we should use the right comparison
		// but it seems hard to evaluate the step expression right now
		if (loopVarIsFloat) {
			mv.visitInsn(FCMPG);
			mv.visitJumpInsn(IFGE, endLabel);

		} else {
			mv.visitJumpInsn(IF_ICMPGE, endLabel);
		}

		// visit the loop body
		forLoop.getBlock().accept(this, localTable);

		// increment the loop variable
		// load the step value
		Expression step = forLoop.getStep();

		int loopVarIdx = localTable.lookup(varSymbol.lexeme);
		if (loopVarIdx != -1 && step.semtype.equals(intType) && step instanceof ConstVal constStep && forLoop.semtype.equals(intType)) {
			// since the loop var is a local variable and the step is a constant, we can use the iinc instruction
			mv.visitIincInsn(loopVarIdx, (Integer) constStep.getSymbol().value);
		} else {
			loadOrStoreVarFromSymbol(forLoop, localTable, varSymbol, forLoop.semtype, false);

			forLoop.getStep().accept(this, localTable);
			implicitTypeConversion(forLoop.semtype, step.semtype);

			mv.visitInsn(forLoop.semtype.asmType().getOpcode(IADD));
			loadOrStoreVarFromSymbol(forLoop, localTable, varSymbol, forLoop.semtype, true);
		}

		// go back up to the start (before the condition check)
		mv.visitJumpInsn(GOTO, startLabel);

		// end of the loop
		mv.visitLabel(endLabel);


		return null;
	}

	private void loadOrStoreVarFromSymbol(ForLoop forLoop, SlotTable localTable, Symbol symbol, SemType semtype, boolean shouldStore) {
		String identLexeme = symbol.lexeme;
		int index = localTable.lookup(identLexeme);
		if (index == -1) {
			// maybe it's a const or a global var
			SemType constSemType = constantsAndGlobals.get(identLexeme);
			mv.visitFieldInsn(shouldStore ? PUTSTATIC : GETSTATIC, className, identLexeme, constSemType.fieldDescriptor());
		} else {
			org.objectweb.asm.Type asmType = semtype.asmType();
			mv.visitVarInsn(asmType.getOpcode(shouldStore ? ISTORE : ILOAD), index);
		}
	}

	@Override
	public Void visitFreeStatement(FreeStatement freeStatement, SlotTable localTable) throws Exception {
		// Nothing to do, the garbage collector will free the object for us.
		return null;
	}

	@Override
	public Void visitIfStatement(IfStatement ifStatement, SlotTable localTable) throws Exception {
		Label elseLabel = new Label();
		Label endLabel = new Label();

		// the jump label depends on if we have an else block,
		// if we don't, then at if the condition is not met, we jump directly to the end label
		// otherwise, if we do have an else label and the condition is not met, we jump to the else label which marks the else block
		Label jumpLabel = endLabel;
		if (ifStatement.isElse()) {
			jumpLabel = elseLabel;
		}

		ifStatement.getCondition().accept(this, localTable);

		mv.visitJumpInsn(IFEQ, jumpLabel);

		ifStatement.getThenBlock().accept(this, localTable);

		if (ifStatement.isElse()) {

			mv.visitJumpInsn(GOTO, endLabel); // goto end marks the end of the then block
			mv.visitLabel(elseLabel);
			ifStatement.getElseBlock().accept(this, localTable);
		}

		mv.visitLabel(endLabel);

		return null;
	}

	@Override
	public Void visitWhileLoop(WhileLoop whileLoop, SlotTable localTable) throws Exception {
		Label conditionLabel = new Label();
		Label endLabel = new Label();

		// we need to visit the condition label BEFORE we visit it, because we need to visit it at every iteration of the loop
		mv.visitLabel(conditionLabel);

		whileLoop.getCondition().accept(this, localTable);

		mv.visitJumpInsn(IFEQ, endLabel); // if the condition is met, we jump to the end label

		// otherwise, we run one iteration of the loop
		whileLoop.getBlock().accept(this, localTable);

		// at the end of the iteration, go back to the condition label to re-evaluate the condition
		mv.visitJumpInsn(GOTO, conditionLabel);

		mv.visitLabel(endLabel);

		return null;
	}

	@Override
	public Void visitType(Type type, SlotTable localTable) throws Exception {
		return null;
	}

	@Override
	public Void visitNumType(NumType numType, SlotTable localTable) throws Exception {
		return null;
	}

}

package compiler.CodeGen;

import compiler.Parser.ASTNodes.ASTNode;
import compiler.Parser.ASTNodes.Block;
import compiler.Parser.ASTNodes.Program;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.ArrayAccess;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.IdentifierAccess;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.RecordAccess;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.ArrayExpression;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.BinaryExpression;
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
	private boolean isMvTopLevel;
	private Label endLabel;
	private final SlotTable slotTable;
	private final Map<String, SemType> constantsAndGlobals;
	private final LinkedHashMap<String, ClassWriter> structs;
	private final String filePath;
	private final String className;

	public CodeGen(String filePath, String className) {

		this.slotTable = new SlotTable(new AtomicReference<>(1), null);
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

		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cw.visit(V1_8, ACC_PUBLIC, this.className, null, "java/lang/Object", null);

		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		mv.visitInsn(RETURN);
		mv.visitMaxs(-1,-1);
		mv.visitEnd();

		mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		mv.visitCode();
		isMvTopLevel = true;

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

		// function declarations save and restore the method visitor
		for (FunctionDefinition function : program.getFunctions()) {
			function.accept(this, slotTable);
		}

		mv.visitInsn(RETURN); // return void from main
		mv.visitEnd();
		mv.visitMaxs(-1, -1); // let ASM calculate the size needed on the stack and of the slots.
		cw.visitEnd();

		byte[] bytearray = cw.toByteArray();
		try (FileOutputStream outputStream = new FileOutputStream(filePath + className + ".class") ) {
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
			try (FileOutputStream outputStream = new FileOutputStream(filePath + structName + ".class") ) {
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

			if (variableDeclaration.hasValue()) {
				// visit expression
				variableDeclaration.getValue().accept(this, slotTable);
			}

			mv.visitFieldInsn(PUTSTATIC, className, variableDeclaration.getName().lexeme, variableDeclaration.semtype.fieldDescriptor());

			// NOTE: Don't add to the local table because it's not a local variable!

			variableDeclaration.semtype.setIsConstant(variableDeclaration.isConstant());
			variableDeclaration.semtype.setGlobal(variableDeclaration.isGlobal());
			constantsAndGlobals.put(variableDeclaration.getName().lexeme, variableDeclaration.semtype);

		} else {
			if (variableDeclaration.hasValue()) {
				// load on the stack if it has a value
				variableDeclaration.getValue().accept(this, localTable);
			}

			org.objectweb.asm.Type asmType = variableDeclaration.semtype.asmType();
			int idx = localTable.addSlot(variableDeclaration.getName().lexeme);

			mv.visitVarInsn(asmType.getOpcode(ISTORE), idx);
		}

		return null;
	}

	@Override
	public Void visitVariableAssignment(VariableAssignment variableAssignment, SlotTable localTable) throws Exception {
		variableAssignment.getAccess().accept(this, localTable);

		// then visit the expression
		variableAssignment.getExpression().accept(this, localTable);

		implicitTypeConversion(variableAssignment.getAccess().semtype, variableAssignment.getExpression().semtype);

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
				// TODO: finish the string indexing
//				if (arrayAccess.semtype.equals(stringType)) {
//					return null;
//				}
				ArraySemType arraySemType = (ArraySemType) arrayAccess.getHeadAccess().semtype;
				org.objectweb.asm.Type arrayAsmType = arraySemType.getElementSemType().asmType();
				mv.visitInsn(arrayAsmType.getOpcode(IASTORE));
				break;
			default:
				throw new IllegalStateException("Unexpected value: " + variableAssignment.getAccess());
		}


		return null;
	}

	private void implicitTypeConversion(SemType left, SemType right) {
		if (left.equals(floatType) && right.equals(intType)) {
			mv.visitInsn(I2F);
		}
	}

	@Override
	public Void visitRecordDefinition(RecordDefinition recordDefinition, SlotTable localTable) throws Exception {
		structCw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		structCw.visit(V1_8, ACC_PUBLIC, recordDefinition.getIdentifier().lexeme, null, "java/lang/Object", null);
		for (RecordFieldDefinition recordField: recordDefinition.getFields()) {
			recordField.accept(this, null);
		}

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
		init.visitMaxs(-1, -1);
		init.visitEnd();

		structCw.visitEnd();
		structs.put(recordDefinition.getIdentifier().lexeme, structCw);
		structCw = null;
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
		SlotTable funcTable = localTable;
		// the main function is already defined, so we skip the function definition
		if (!functionDefinition.getName().lexeme.equals("main")) {
			isMvTopLevel = false;
			// NOTE: The slots of other functions start at 0, I think it's because they don't have self (?)
			funcTable = new SlotTable(new AtomicReference<>(0), localTable);

			// generate string for descriptor
			FunctionSemType functionSemType = (FunctionSemType) functionDefinition.semtype;
//			String descriptor = functionSemType.fieldDescriptor();
			String descriptor = functionSemType.asmType().getDescriptor();

			mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, functionDefinition.getName().lexeme, descriptor, null, null);
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

			mv.visitEnd();
			mv.visitMaxs(-1, -1);

			mv = oldMv;
			isMvTopLevel = true;
		}
		return null;
	}

	@Override
	public Void visitParamDefinition(ParamDefinition paramDefinition, SlotTable localTable) throws Exception {
		localTable.addSlot(paramDefinition.getIdentifier().lexeme);
		return null;
	}

	@Override
	public Void visitStatement(Statement statement, SlotTable localTable) throws Exception {
		throw new RuntimeException("this should never be called");
//		return null;
	}

	@Override
	public Void visitBlock(Block block, SlotTable localTable) throws Exception {
		for (Statement stmt : block.getStatements()) {
			stmt.accept(this, localTable);
		}

//		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
////		mv.visitVarInsn(ALOAD, 1);
//		mv.visitFieldInsn(GETSTATIC, className, "val", constantsAndGlobals.get("val").fieldDescriptor());
//		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "("+constantsAndGlobals.get("val").fieldDescriptor()+")V", false);

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

		// if the identifier is a constant or a global, we need to visit the field
		if (constantsAndGlobals.containsKey(identLexeme)){
			SemType constSemType = constantsAndGlobals.get(identLexeme);
			mv.visitFieldInsn(GETSTATIC, className, identLexeme, constSemType.fieldDescriptor());
			return null;
		}

		// load symbol in slot table or insert it
		int index = localTable.lookup(identLexeme);
		if (index == -1) {
			// unexpected error : the term should be in the slot table.
			throw new RuntimeException("Unexpected error : the variable " + identLexeme + " is not in the slot table.");
		}

		SemType semtype = identifierAccess.semtype;
		org.objectweb.asm.Type asmType = semtype.asmType();
		mv.visitVarInsn(asmType.getOpcode(ILOAD), index);

		return null;
	}

	@Override
	public Void visitRecordAccess(RecordAccess recordAccess, SlotTable localTable) throws Exception {
		recordAccess.getHeadAccess().accept(this, localTable);

		// No need to load if we will store something there
		// willStore is set during the semantic analysis phase.
		if (!recordAccess.willStore) {
			RecordSemType recordSemType = (RecordSemType) recordAccess.getHeadAccess().semtype;
			String headAccessDescriptor = recordSemType.recordFielDesc(recordAccess.getIdentifier().lexeme);

			mv.visitFieldInsn(GETFIELD, recordSemType.identifier, recordAccess.getIdentifier().lexeme, headAccessDescriptor);
		}

		return null;
	}

	@Override
	public Void visitArrayAccess(ArrayAccess arrayAccess, SlotTable localTable) throws Exception {
		// ArrayAccess -> "[" Expression "]"

		arrayAccess.getHeadAccess().accept(this, localTable);

//		if (arrayAccess.semtype.equals(stringType)) {
//
//			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
//			org.objectweb.asm.Type arrSemType = arrayAccess.getHeadAccess().semtype.asmType();
//			mv.visitInsn(arrSemType.getOpcode(ISTORE));
//			return null;
//		}

		arrayAccess.getIndexExpression().accept(this, localTable);

		if (!arrayAccess.willStore) {
			ArraySemType arraySemType = (ArraySemType) arrayAccess.getHeadAccess().semtype;
			org.objectweb.asm.Type arrayAsmType = arraySemType.getElementSemType().asmType();
			mv.visitInsn(arrayAsmType.getOpcode(IALOAD));
		}

		// note: for strings use string.charAt(idx)

		return null;
	}

	@Override
	public Void visitFunctionCall(FunctionCall functionCall, SlotTable localTable) throws Exception {
		for (ParamCall paramCall : functionCall.getParameters()) {
			// push the params on the stack
			paramCall.accept(this, localTable);
		}
		// TODO: Handle predefined functions
		FunctionSemType functionSemType = (FunctionSemType) functionCall.semtype;
		String descriptor = functionSemType.asmType().getDescriptor();

		mv.visitMethodInsn(INVOKESTATIC, className, functionCall.getIdentifier().lexeme, descriptor, false);

		return null;
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
		binaryExpression.getLeftTerm().accept(this, slotTable);
		if (binaryExpression.getLeftTerm().semtype.toConvert) {
			mv.visitInsn(I2F);
		}
		binaryExpression.getRightTerm().accept(this, slotTable);
		if (binaryExpression.getRightTerm().semtype.toConvert) {
			mv.visitInsn(I2F);
		}

		opCodeGenerator op = new opCodeGenerator(binaryExpression, mv);
		op.generateCode();
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
				mv.visitInsn(ICONST_0);
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
		return null;
	}

	@Override
	public Void visitForLoop(ForLoop forLoop, SlotTable localTable) throws Exception {
		return null;
	}

	@Override
	public Void visitFreeStatement(FreeStatement freeStatement, SlotTable localTable) throws Exception {
		return null;
	}

	@Override
	public Void visitIfStatement(IfStatement ifStatement, SlotTable localTable) throws Exception {
		return null;
	}

	@Override
	public Void visitWhileLoop(WhileLoop whileLoop, SlotTable localTable) throws Exception {
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

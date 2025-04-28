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
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.objectweb.asm.Opcodes.*;

public class CodeGen implements Visitor<Void, SlotTable> {

	SemType intType = new SemType("int");
	SemType floatType = new SemType("float");
	SemType numType = new SemType("num");
	SemType numOrBoolType = new SemType("numOrBool");
	SemType stringType = new SemType("string");
	SemType boolType = new SemType("bool");
	SemType voidType = new SemType("void");
	SemType anyType = new SemType("any");
	SemType recType = new SemType("rec");


	private ClassWriter cw;
	private MethodVisitor mv;
	private boolean isMvTopLevel;
	private Label endLabel;
	private final SlotTable slotTable;
	private final Map<String, SemType> constantsAndGlobals;
	private final String filePath;
	private final String className;

	public CodeGen(String filePath, String className) {

		this.slotTable = new SlotTable(new AtomicReference<>(1), null);
		this.constantsAndGlobals = new HashMap<>();
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
		mv.visitMaxs(1,1);
		mv.visitEnd();

		mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		mv.visitCode();
		isMvTopLevel = true;

		// 2 + 7.9
		// float b = 4.3
		// int a = b

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

		try (FileOutputStream outputStream = new FileOutputStream(filePath) ) {
			outputStream.write(bytearray);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(3);
		};

		return null;
	}


	@Override
	public Void visitArrayAccess(ArrayAccess arrayAccess, SlotTable localTable) throws Exception {
		return null;
	}

	@Override
	public Void visitIdentifierAccess(IdentifierAccess identifierAccess, SlotTable localTable) throws Exception {
		String identLexeme = identifierAccess.getIdentifier().lexeme;

		// if the identifier is a constant or a global
		// TODO: investigate for globals
		if (constantsAndGlobals.containsKey(identLexeme)){
			SemType constSemType = constantsAndGlobals.get(identLexeme);
			mv.visitFieldInsn(GETSTATIC, className, identLexeme, constSemType.fieldDescriptor());
			return null;
		}

		// load symbol in slot table or insert it and put it in constant pool
		int index = localTable.lookup(identLexeme);
		if (index == -1) {
			// unexpected error : the term should be in the slot table.
			throw new RuntimeException("Unexpected error : the variable " + identLexeme + " is not in the slot table.");
		}

		SemType semtype = identifierAccess.semtype;
//		org.objectweb.asm.Type asmType = org.objectweb.asm.Type.getType(semtype.fieldDescriptor());
		org.objectweb.asm.Type asmType = semtype.asmType();
		mv.visitVarInsn(asmType.getOpcode(ILOAD), index);

		return null;
	}

	@Override
	public Void visitRecordAccess(RecordAccess recordAccess, SlotTable localTable) throws Exception {
		return null;
	}

	@Override
	public Void visitArrayExpression(ArrayExpression arrayExpression, SlotTable localTable) throws Exception {
		// note: for strings use string.charAt(idx)
		return null;
	}

	@Override
	public Void visitBinaryExpression(BinaryExpression binaryExpression, SlotTable localTable) throws Exception {

		// TODO: handle implicit type conversion in terms
		binaryExpression.getLeftTerm().accept(this, slotTable);
		binaryExpression.getRightTerm().accept(this, slotTable);

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

		// a[0]
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
	public Void visitConstValue(ConstVal constVal, SlotTable localTable) throws Exception {
		mv.visitLdcInsn(constVal.getValue());

		// put it in slot table ?
		return null;
	}

	@Override
	public Void visitFunctionCall(FunctionCall functionCall, SlotTable localTable) throws Exception {
		return null;
	}

	@Override
	public Void visitNewRecord(NewRecord newRecord, SlotTable localTable) throws Exception {
		return null;
	}

	@Override
	public Void visitParamCall(ParamCall paramCall, SlotTable localTable) throws Exception {
		return null;
	}

	@Override
	public Void visitParenthesesTerm(ParenthesesTerm parenthesesTerm, SlotTable localTable) throws Exception {
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

	@Override
	public Void visitStatement(Statement statement, SlotTable localTable) throws Exception {
		throw new RuntimeException("this should never be called");
//		return null;
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
	public Void visitFunctionDefinition(FunctionDefinition functionDefinition, SlotTable localTable) throws Exception {

		MethodVisitor oldMv = mv;
		// the main function is already defined, so we skip the function definition
		if (!functionDefinition.getName().lexeme.equals("main")) {
			isMvTopLevel = false;

			// generate string for descriptor
			FunctionSemType functionSemType = (FunctionSemType) functionDefinition.semtype;
			String descriptor = functionSemType.fieldDescriptor();

			// add params to slot table
			mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, functionDefinition.getName().lexeme, descriptor, null, null);
			mv.visitCode();

		}

		// accept block (the block handles the return)
		functionDefinition.getBlock().accept(this, localTable);

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
	public Void visitBlock(Block block, SlotTable localTable) throws Exception {
		for (Statement stmt : block.getStatements()) {
			stmt.accept(this, localTable);
		}

		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//		mv.visitVarInsn(ALOAD, 1);
		mv.visitFieldInsn(GETSTATIC, className, "val", "Ljava/lang/String;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

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

		// return
		mv.visitInsn(RETURN);
		return null;
	}

	@Override
	public Void visitIfStatement(IfStatement ifStatement, SlotTable localTable) throws Exception {
		return null;
	}

	@Override
	public Void visitParamDefinition(ParamDefinition paramDefinition, SlotTable localTable) throws Exception {
//		localTable.addSlot(paramDefinition.semtype.)
		return null;
	}

	@Override
	public Void visitRecordDefinition(RecordDefinition recordDefinition, SlotTable localTable) throws Exception {
		return null;
	}

	@Override
	public Void visitRecordFieldDefinition(RecordFieldDefinition recordFieldDefinition, SlotTable localTable) throws Exception {
		return null;
	}


	@Override
	public Void visitVariableAssignment(VariableAssignment variableAssignment, SlotTable localTable) throws Exception {
		// NOTE: we actually don't need to visit the access, otherwise it creates another local var with the contents
		// first visit the access
//		variableAssignment.getAccess().accept(this, localTable);

		// then visit the expression
		variableAssignment.getExpression().accept(this, localTable);

		// then store the results
		if (variableAssignment.semtype.isGlobal || variableAssignment.semtype.isConstant) {
			switch (variableAssignment.getAccess()) {
				case IdentifierAccess identifierAccess:
					SemType constSemType = constantsAndGlobals.get(identifierAccess.getIdentifier().lexeme);
					mv.visitFieldInsn(PUTSTATIC, className, identifierAccess.getIdentifier().lexeme, constSemType.fieldDescriptor());
					break;
				case RecordAccess recordAccess:
					// TODO
					break;
				case ArrayAccess arrayAccess:
					// TODO
					break;
				default:
					throw new IllegalStateException("Unexpected value: " + variableAssignment.getAccess());
			}

		} else {
			switch (variableAssignment.getAccess()) {
				case IdentifierAccess identifierAccess:
					int index = localTable.lookup(identifierAccess.getIdentifier().lexeme);
					if (index == -1) {
						// unexpected error : the term should be in the slot table.
						throw new RuntimeException("Unexpected error : the variable " + identifierAccess.getIdentifier().lexeme + " is not in the slot table.");
					}
					org.objectweb.asm.Type asmType = variableAssignment.semtype.asmType();
					mv.visitVarInsn(asmType.getOpcode(ISTORE), index);
					break;
				case RecordAccess recordAccess:
					// TODO
					break;
				case ArrayAccess arrayAccess:
					// TODO
					break;
				default:
					throw new IllegalStateException("Unexpected value: " + variableAssignment.getAccess());
			}
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
//			if (variableDeclaration.hasValue()) {
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
//			}
		} else {
			if (variableDeclaration.hasValue()) {
				// load on the stack if it has a value
				variableDeclaration.getValue().accept(this, localTable);
			}

//			String desc = variableDeclaration.semtype.fieldDescriptor();
//			org.objectweb.asm.Type asmType = org.objectweb.asm.Type.getType(desc);
			org.objectweb.asm.Type asmType = variableDeclaration.semtype.asmType();

			int idx = localTable.addSlot(variableDeclaration.getName().lexeme);
			mv.visitVarInsn(asmType.getOpcode(ISTORE), idx);
		}

		return null;
	}

	private void implicitTypeConversion(Type left, Type right, boolean convertLeftTerm) {
		if (left.symbol.type == right.symbol.type) {
			return;
		}

		// 3 + 5.0
		if (left.semtype.equals(intType) && right.semtype.equals(floatType) && convertLeftTerm) {
			this.mv.visitInsn(I2F);
		}

		// 5.0 + 3
		if (left.semtype.equals(floatType) && right.semtype.equals(intType) && !convertLeftTerm) {
			this.mv.visitInsn(I2F);
		}

	}

	@Override
	public Void visitWhileLoop(WhileLoop whileLoop, SlotTable localTable) throws Exception {
		return null;
	}
}

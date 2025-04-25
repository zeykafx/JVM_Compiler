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
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
	private final SlotTable slotTable;
	private final String filePath;
	private final String className;

	public CodeGen(String filePath, String className) {

		this.slotTable = new SlotTable(new AtomicReference<>(0), null);
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
		// load symbol in slot table or insert it and put it in constant pool
		int index = localTable.lookup(identifierAccess.getIdentifier().lexeme);
		if (index == -1) {
			// unexpected error : the term should be in the slot table.
			throw new RuntimeException("Unexpected error : the variable " + identifierAccess.getIdentifier().lexeme + " is not in the slot table.");
		}

		SemType semtype = identifierAccess.semtype;

		if (semtype.equals(intType) || semtype.equals(boolType)) {
				mv.visitVarInsn(ILOAD, index);
        } else if (semtype.equals(floatType)) {
			mv.visitVarInsn(FLOAD, index);
		} else {
			mv.visitVarInsn(AALOAD, index);
		}

		// a[0]

		return null;
	}

	@Override
	public Void visitRecordAccess(RecordAccess recordAccess, SlotTable localTable) throws Exception {
		return null;
	}

	@Override
	public Void visitArrayExpression(ArrayExpression arrayExpression, SlotTable localTable) throws Exception {
		return null;
	}

	@Override
	public Void visitBinaryExpression(BinaryExpression binaryExpression, SlotTable localTable) throws Exception {
		// TODO: handle implicit type conversion in terms
		binaryExpression.getLeftTerm().accept(this, slotTable);
		binaryExpression.getRightTerm().accept(this, slotTable);

		// TODO: on etait entrain de faire string concat donc faut gerer arrayAccess
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
	public Void visitBlock(Block block, SlotTable localTable) throws Exception {
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
	public Void visitFunctionDefinition(FunctionDefinition functionDefinition, SlotTable localTable) throws Exception {
		return null;
	}

	@Override
	public Void visitIfStatement(IfStatement ifStatement, SlotTable localTable) throws Exception {
		return null;
	}

	@Override
	public Void visitParamDefinition(ParamDefinition paramDefinition, SlotTable localTable) throws Exception {
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
	public Void visitReturnStatement(ReturnStatement returnStatement, SlotTable localTable) throws Exception {
		return null;
	}

	@Override
	public Void visitVariableAssignment(VariableAssignment variableAssignment, SlotTable localTable) throws Exception {
		return null;
	}

	@Override
	public Void visitVariableDeclaration(VariableDeclaration variableDeclaration, SlotTable localTable) throws Exception {
		// create the bytecode
		if (variableDeclaration.isConstant()) {
			// if the variable declaration is global, declare it as a constant
			if (variableDeclaration.getValue() != null) {
				cw.visitField(ACC_PUBLIC | ACC_STATIC | ACC_FINAL,
						variableDeclaration.getName().lexeme,
						variableDeclaration.semtype.fieldDescriptor(),
						null, // no generic signature
						null // no initial value
				).visitEnd();

				// visit expression
				variableDeclaration.getValue().accept(this, slotTable);
				mv.visitFieldInsn(PUTSTATIC, className, variableDeclaration.getName().lexeme, variableDeclaration.semtype.fieldDescriptor());
			}
		}

		// add the variable to the local table
		localTable.addSlot(variableDeclaration.getName().lexeme);

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

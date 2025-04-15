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
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.objectweb.asm.Opcodes.*;

public class CodeGen implements Visitor<Void, SlotTable> {

	private final ClassWriter cw;
	private MethodVisitor mv;
	private final SlotTable slotTable;

	public CodeGen(String className) {

		this.slotTable = new SlotTable(new AtomicReference<>(0), null);

		this.cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		this.cw.visit(V1_8, ACC_PUBLIC, className, null, "java/lang/Object", null);
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

		// 2 + 7.9
		// float b = 4.3
		// int a = b

		cw.visit(V1_8, ACC_PUBLIC, "Main", null, "java/lang/Object", null);

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

		for (FunctionDefinition function : program.getFunctions()) {
			function.accept(this, slotTable);
		}

		return null;
	}


	@Override
	public Void visitArrayAccess(ArrayAccess arrayAccess, SlotTable localTable) throws Exception {
		return null;
	}

	@Override
	public Void visitIdentifierAccess(IdentifierAccess identifierAccess, SlotTable localTable) throws Exception {
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
		return null;
	}

	@Override
	public Void visitUnaryExpression(UnaryExpression unaryExpression, SlotTable localTable) throws Exception {
		return null;
	}

	@Override
	public Void visitBinaryOperator(BinaryOperator binaryOperator, SlotTable localTable) throws Exception {
		return null;
	}

	@Override
	public Void visitUnaryOperator(UnaryOperator unaryOperator, SlotTable localTable) throws Exception {
		return null;
	}

	@Override
	public Void visitConstValue(ConstVal constVal, SlotTable localTable) throws Exception {
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
		// add the variable to the local table
		Integer slotIndex = localTable.addSlot(variableDeclaration.getName().lexeme);

		Type type = variableDeclaration.getType();

		// create the bytecode
		if (variableDeclaration.isConstant()) {
			// if the variable declaration is global, declare it as a constant
//			if (type.symbol.lexeme.equals("float") && )
//			variableDeclaration.getValue();
		}

		return null;
	}

	private void implicitTypeConversion(Type left, Type right) {
		if (left.symbol.type == right.symbol.type) {
			return;
		}

		if (left.symbol.lexeme.equals("float") && right.symbol.lexeme.equals("int")) {
			this.mv.visitInsn(I2F);
		}

		if (left.symbol.lexeme.equals("int") && right.symbol.lexeme.equals("float")) {
			this.mv.visitInsn(I2F);
		}
	}

	@Override
	public Void visitWhileLoop(WhileLoop whileLoop, SlotTable localTable) throws Exception {
		return null;
	}
}

package compiler.Parser.ASTNodes;

import compiler.Parser.ASTNodes.Statements.FunctionDefinition;
import compiler.Parser.ASTNodes.Statements.RecordDefinition;
import compiler.Parser.ASTNodes.Statements.VariableDeclaration;

import java.util.ArrayList;

public class Program extends ASTNode {
	ArrayList<VariableDeclaration> constants;
	ArrayList<RecordDefinition> records;
	ArrayList<VariableDeclaration> globals;
	ArrayList<FunctionDefinition> functions;


	public Program(ArrayList<VariableDeclaration> constants, ArrayList<RecordDefinition> records, ArrayList<VariableDeclaration> globals, ArrayList<FunctionDefinition> functions) {
		this.constants = constants;
		this.records = records;
		this.globals = globals;
		this.functions = functions;
	}

	public ArrayList<VariableDeclaration> getConstants() {
		return constants;
	}

	public ArrayList<RecordDefinition> getRecords() {
		return records;
	}

	public ArrayList<FunctionDefinition> getFunctions() {
		return functions;
	}

	public ArrayList<VariableDeclaration> getGlobals() {
		return globals;
	}

	public String toString() {
		return "Program [constants=" + constants + ", records=" + records + ", globals: " + globals + ", functions=" + functions + "]";
	}
}
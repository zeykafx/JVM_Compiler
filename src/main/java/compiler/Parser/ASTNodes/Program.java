package compiler.Parser.ASTNodes;

import compiler.Parser.ASTNodes.Statements.Statements.FunctionDefinition;
import compiler.Parser.ASTNodes.Statements.Statements.RecordDefinition;
import compiler.Parser.ASTNodes.Statements.Statements.VariableDeclaration;
import compiler.SemanticAnalysis.Errors.SemanticException;
import compiler.SemanticAnalysis.SymbolTable;
import compiler.SemanticAnalysis.Types.SemType;
import compiler.SemanticAnalysis.Visitor;

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

    @Override
    public SemType accept(Visitor<SemType> v, SymbolTable table) throws SemanticException {
        return v.visitProgram(this, table);
    }

    public String toString() {
		return "Program [constants=" + constants + ", records=" + records + ", globals: " + globals + ", functions=" + functions + "]";
	}
	
	@Override
    public String prettyPrint(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append("  ".repeat(indent)).append("Program\n");
        
        if (!constants.isEmpty()) {
            sb.append("  ".repeat(indent + 1)).append("Constants:\n");
            for (VariableDeclaration constant : constants) {
                sb.append(constant.prettyPrint(indent + 2)).append("\n");
            }
        }
        
        if (!records.isEmpty()) {
            sb.append("  ".repeat(indent + 1)).append("Records:\n");
            for (RecordDefinition record : records) {
                sb.append(record.prettyPrint(indent + 2)).append("\n");
            }
        }
        
        if (!globals.isEmpty()) {
            sb.append("  ".repeat(indent + 1)).append("Globals:\n");
            for (VariableDeclaration global : globals) {
                sb.append(global.prettyPrint(indent + 2)).append("\n");
            }
        }
        
        if (!functions.isEmpty()) {
            sb.append("  ".repeat(indent + 1)).append("Functions:\n");
            for (FunctionDefinition function : functions) {
                sb.append(function.prettyPrint(indent + 2)).append("\n");
            }
        }
        
        return sb.toString().trim();
    }
}
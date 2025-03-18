package compiler.Parser.ASTNodes;

import compiler.Parser.ASTNodes.Statements.FunctionDefinition;
import compiler.Parser.ASTNodes.Statements.RecordDefinition;
import compiler.Parser.ASTNodes.Statements.VariableDeclaration;

import java.util.ArrayList;

public class Program extends ASTNode {
     ArrayList<VariableDeclaration> constants;
     ArrayList<RecordDefinition> records;
     ArrayList<FunctionDefinition> functions;
    // ArrayList<Globals> globalVariables;
    
    
    public Program() {
        
    }
    
    public String toString() {
        return "Program";
    }
}
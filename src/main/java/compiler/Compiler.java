/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package compiler;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import compiler.CodeGen.CodeGen;
import compiler.Lexer.Lexer;
import compiler.Lexer.Symbol;
import compiler.Parser.ASTNodes.ASTNode;
import compiler.Parser.Parser;
import compiler.SemanticAnalysis.SemanticAnalysis;
import com.beust.jcommander.Parameter;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;


public class Compiler {
	@Parameter(description = "Target", required = true)
	public String file;

	@Parameter(names={"--out", "-o"}, description = "Filepath of output classfile")
	public String out;
	@Parameter(names={"--module", "-m"}, description = "Choose what module will be executed")
	public String module;


	public static void main(String[] args) {

		Compiler main = new Compiler();
		try {
			JCommander.newBuilder()
					.addObject(main)
					.build()
					.parse(args);

		} catch (ParameterException e) {
			e.usage();
			System.exit(1);
		}

		main.run();
	}

	public void run() {
		try {
			if (module != null) {
				Module parsedModule = Module.fromFlag(module);
				processModule(parsedModule, file);
			} else {
				runEverything(file);
			}

		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	private void processModule(Module module, String filepath) throws Exception {
		switch (module) {
			case LEXER:
				runLexer(filepath);
				break;
			case PARSER:
			    runParser(filepath);
                break;
			
			// OTHER MODULES HERE -----
			default:
				throw new IllegalArgumentException("Unknown module: " + module);
		}
	}

	private void runLexer(String filepath) throws Exception {
		FileReader reader = new FileReader(filepath);
		Lexer lexer = new Lexer(reader);

		Symbol symbol;
		do {
			symbol = lexer.getNextSymbol();
			System.out.println(symbol);
		} while (symbol.type != compiler.Lexer.TokenTypes.EOF);
	}
	
	private void runParser(String filepath) throws Exception {
        FileReader reader = new FileReader(filepath);
  		Lexer lexer = new Lexer(reader);	
        Parser parser = new Parser(lexer);
		ASTNode root = parser.getAST();
		System.out.println("AST: " + root.prettyPrint(0));
    }

	private void runEverything(String filepath) throws Exception {
		File f = new File(filepath);


		// If you are given the arguments "./tests/script.lang -o ./tests/test.class", it should
		//save the test.class in the given argument ("./tests/test.class").

		FileReader reader = new FileReader(filepath);

		Lexer lexer = new Lexer(reader);
		Parser parser = new Parser(lexer);
		ASTNode root = parser.getAST();
//		System.out.println("AST: " + root.prettyPrint(0));

		SemanticAnalysis analyzer = new SemanticAnalysis();
		analyzer.analyze(root, false);

		String filename = out == null ? f.getPath() : new File(out).getPath();
//		System.out.println("filename = " + filename);

		String className = out == null ? f.getName() : new File(out).getName();
		String lowercaseClassname = className;

		className = className.split("\\.")[0];
//		className = className.substring(0, 1).toUpperCase() + className.substring(1);
//		System.out.println("className = " + className);

		String outFilename = "";
		String[] filenameParts = filename.split(lowercaseClassname);

		if (filenameParts.length >= 1) {
			outFilename = filename.split(lowercaseClassname)[0];
		}
//		System.out.println("outFilename = " + outFilename);

		CodeGen codeGen = new CodeGen(outFilename, className);
		codeGen.generateCode(root);
	}
}


enum Module {
	LEXER("lexer"),
	PARSER("parser");

	private final String flag;

	Module(String flag) {
		this.flag = flag;
	}

	public String getFlag() {
		return flag;
	}

	public static Module fromFlag(String flag) {
		for (Module module : Module.values()) {
			if (module.getFlag().equals(flag)) {
				return module;
			}
		}

//		return null;
		throw new IllegalArgumentException("Unknown flag: " + flag);
	}
}

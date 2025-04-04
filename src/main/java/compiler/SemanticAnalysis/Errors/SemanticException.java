package compiler.SemanticAnalysis.Errors;

public class SemanticException extends Exception {
    public SemanticException(String message) {
        super(message);
        super.printStackTrace();
    }
}

package compiler.Parser;

public class SyntaxErrorException extends Exception {
	public SyntaxErrorException(String message) {
		super(message);
//		super.printStackTrace();
	}
}

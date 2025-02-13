package compiler.Lexer;
import java.io.Reader;

public class Lexer {
    
    public Lexer(Reader input) {
    }
    
    public Symbol getNextSymbol() {
        try {
            char c = (char) input.read();

            // if c is whitespace or tab or newline, skip
            if (c == ' ' || c == '\t' || c == '\n') {
                return getNextSymbol();
            }

            // if c is a letter, read until not letter then check if keyword or identifier
            if (Character.isLetter(c)) {
                StringBuilder identifier = new StringBuilder("" + c);
                while (Character.isLetterOrDigit(c = (char) input.read())) {
                    identifier.append(c);
                }
                input.unread(c); // unread the last character that is not a letter

                // TOOD: check if keyword, then create symbol
                if (identifier.toString().equals("int")) {
                    return new Symbol(TokenTypes.INT, "int");
                }
            }

            // if c is a digit, read until not digit
            if (Character.isDigit(c)) {
                StringBuilder number = new StringBuilder("" + c);
                Boolean isFloat = false;
                c = (char) input.read();

                while (Character.isDigit(c) || c == '.') {
                    if (c == '.') {
                        isFloat = true;
                    }
                    number.append(c);
                    c = (char) input.read();
                }
                input.unread(c);
                // TODO : transform to symbol
            }

            // if c is a special character, return the symbol


        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("erreur");
        }
        return null;
    }
}

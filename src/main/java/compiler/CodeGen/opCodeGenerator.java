package compiler.CodeGen;

import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.BinaryExpression;
import compiler.SemanticAnalysis.Types.SemType;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class opCodeGenerator {

    BinaryExpression expression;
    MethodVisitor mv;
    boolean isFloat = false;
    boolean isInt = false;
    boolean isString = false;
    boolean isBool;
    boolean isObject;

    SemType intType = new SemType("int");
    SemType floatType = new SemType("float");
    SemType boolType = new SemType("bool");
    SemType stringType = new SemType("string");

    public opCodeGenerator(BinaryExpression binaryExpression, MethodVisitor mv) {
        this.expression = binaryExpression;
        this.mv = mv;

        if (expression.semtype.equals(floatType)) {
            isFloat = true;
        }

        if (expression.semtype.equals(intType)) {
            isInt = true;
        }

        // if left & right terms are type strings, set isString to true
        if (expression.getLeftTerm().semtype.equals(stringType) && expression.getRightTerm().semtype.equals(stringType)) {
            isString = true;
        }
    }

    public void generateCode() {
        if (!isString) {
            int opCode = getOpCode();
            if (opCode != -1) {
                mv.visitInsn(opCode);
            } else {
                comparison();
            }
        } else {
            // +, ==, !=
            if (expression.getOperator().getOperator().equals("+")) {
                // handle concatenation of strings: we use the .concat method on strings
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);

            } else if (expression.getOperator().getOperator().equals("==")) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            } else if (expression.getOperator().getOperator().equals("!=")) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
                mv.visitInsn(ICONST_1);
                mv.visitInsn(IXOR);
            }
        }
    }

    public int getOpCode(){
        return switch (expression.getOperator().getOperator()) {
            case "&&" -> getAnd();
            case "||" -> getOr();
            case "+" -> getAdd();
            case "-" -> getSub();
            case "*" -> getMul();
            case "/" -> getDiv();
            case "%" -> getModulo();
            default -> -1;
        };
    }
    
    private void comparison() {
        Label trueLabel = new Label();
        Label endLabel = new Label();

        if (isFloat) {
//            mv.visitInsn(FSUB);
            mv.visitInsn(FCMPG);
        }

        int opCode = comparisonIntOpCode();
        if (opCode == -1) {
            throw new RuntimeException("Invalid operator: " + expression.getOperator().getOperator());
        }

        mv.visitJumpInsn(opCode, trueLabel);
        mv.visitInsn(ICONST_0); // false
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(trueLabel);
        mv.visitInsn(ICONST_1); // true
        mv.visitLabel(endLabel);
    }

    private int comparisonIntOpCode() {
        return switch (expression.getOperator().getOperator()) {
            case "<" -> getLessThan();
            case "<=" -> getLessThanOrEqual();
            case ">" -> getGreaterThan();
            case ">=" -> getGreaterThanOrEqual();
            case "==" -> getEqual();
            case "!=" -> getNotEqual();
            default -> -1;
        };
    }

    private int getAnd() {
        return IAND;
    }

    private int getOr() {
        if (isFloat) {
            return LOR;
        }
        return IOR;
    }

    private int getAdd() {
        if (isFloat) {
            return FADD;
        }
        return IADD;
    }

    private int getSub() {
        if (isFloat) {
            return FSUB;
        }
        return ISUB;
    }

    private int getMul() {
        if (isFloat) {
            return FMUL;
        }
        return IMUL;
    }

    private int getDiv() {
        if (isFloat) {
            return FDIV;
        }
        return IDIV;
    }

    private int getModulo(){
        if (isFloat) {
            return FREM;
        }
        return IREM;
    }

    private int getLessThan() {
        if (isFloat) {
            return IFLT;
        }
        return IF_ICMPLT;
    }

    private int getLessThanOrEqual() {
        if (isFloat) {
            return IFLE;
        }
        return IF_ICMPLE;
    }

    private int getGreaterThan() {
        if (isFloat) {
            return IFGT;
        }
        return IF_ICMPGT;
    }

    private int getGreaterThanOrEqual() {
        if (isFloat) {
            return IFGT;
        }
        return IF_ICMPGE;
    }

    private int getEqual() {
        if (isFloat) {
            return IFEQ;
        }
        return IF_ICMPEQ;
    }

    private int getNotEqual() {
        if (isFloat) {
            return IFNE;
        }
        return IF_ICMPNE;
    }
}

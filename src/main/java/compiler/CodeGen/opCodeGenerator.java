package compiler.CodeGen;

import com.beust.jcommander.Strings;
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
            System.out.println("setting type to String");
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
                // handle concatenation of strings
//                mv.visitInsn(IADD);
                System.out.println("lqksjfdmqslkdjf");
                mv.visitMethodInsn(INVOKEDYNAMIC, "java/lang/String", "makeConcatWithConstants", "(Ljava/lang/String;)Ljava/lang/String;", false);


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
            mv.visitInsn(FSUB);
            mv.visitJumpInsn(FCMPG, trueLabel);
        } else {
            int opCode = comparionIntOpCode();
            if (opCode == -1) {
                throw new RuntimeException("Invalid operator: " + expression.getOperator().getOperator());
            }
            mv.visitJumpInsn(opCode, trueLabel);
        }

        mv.visitInsn(ICONST_0);
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(trueLabel);
        mv.visitInsn(ICONST_1);
        mv.visitLabel(endLabel);
    }

    private int comparionIntOpCode() {
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
        return IF_ICMPLT;
    }

    private int getLessThanOrEqual() {
        return IF_ICMPLE;
    }

    private int getGreaterThan() {
        return IF_ICMPGT;
    }

    private int getGreaterThanOrEqual() {
        return IF_ICMPGE;
    }

    private int getEqual() {
        return IF_ICMPEQ;
    }

    private int getNotEqual() {
        return IF_ICMPNE;
    }
}

package compiler.CodeGen;

import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.BinaryExpression;
import compiler.SemanticAnalysis.Types.ArraySemType;
import compiler.SemanticAnalysis.Types.RecordSemType;
import compiler.SemanticAnalysis.Types.SemType;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Arrays;

import static org.objectweb.asm.Opcodes.*;

public class opCodeGenerator {

    BinaryExpression expression;
    MethodVisitor mv;
    boolean isFloat = false;
    boolean isInt = false;
    boolean isString = false;
    boolean isBool;
    boolean isObject = false;

    SemType intType = new SemType("int");
    SemType arrType = new SemType("array");
    SemType floatType = new SemType("float");
    SemType boolType = new SemType("bool");
    SemType stringType = new SemType("string");

    public opCodeGenerator(BinaryExpression binaryExpression, MethodVisitor mv) {
        this.expression = binaryExpression;
        this.mv = mv;

        if (expression.semtype.equals(floatType) || binaryExpression.getLeftTerm().semtype.equals(floatType) || binaryExpression.getRightTerm().semtype.equals(floatType)) {
            isFloat = true;
        }

        if (expression.semtype.equals(intType)) {
            isInt = true;
        }

        // if left & right terms are type strings, set isObject to true
        if (
                (expression.getLeftTerm().semtype.equals(stringType) && expression.getRightTerm().semtype.equals(stringType))
                || (expression.getLeftTerm().semtype.type.equals("rec") && expression.getRightTerm().semtype.type.equals("rec"))
                || (expression.getLeftTerm().semtype.type.equals("array") && expression.getRightTerm().semtype.type.equals("array"))
        ) {
            isObject = true;
        }
    }

    public void generateCode() {
        if (!isObject) {
            int opCode = getOpCode();
            if (opCode != -1) {
                mv.visitInsn(opCode);
            } else {
                comparison();
            }
        } else {
            // +, ==, !=
            String op = expression.getOperator().getOperator();
            if (op.equals("+")) {
                // handle concatenation of strings: we use the .concat method on strings
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);
            } else if (op.equals("==") || op.equals("!=")) {
                String path = "java/lang/";
                String classname;
                String descriptor = "(";
                int access = INVOKEVIRTUAL;

                if (expression.getLeftTerm().semtype.type.equals("array")) {
                    path = "java/util/"; // java/util/Arrays
                    classname = "Arrays";
                    // make the descriptor for the array fit the element types
                    String fieldDescriptor = handleArraysToString();
                    descriptor += "["+fieldDescriptor+"["+fieldDescriptor;
                    access = INVOKESTATIC;
                } else {
                    switch (expression.semtype.type) {
                        case "int":
                            classname = "Integer";
                            descriptor += "I";
                            break;
                        case "string":
                            classname = "String";
                            descriptor += "Ljava/lang/Object;";
                            break;
                        case "float":
                            classname = "Float";
                            descriptor += "F";
                            break;
                        case "array":
                            path = "java/util/"; // java/util/Arrays
                            classname = "Arrays";
                            // make the descriptor for the array fit the element types
                            String fieldDescriptor = handleArraysToString();
                            descriptor += "["+fieldDescriptor+"["+fieldDescriptor;
                            access = INVOKESTATIC;
                            break;
                        default:
                            path = "java/util/"; // java/util/Objects
                            classname = "Objects";
                            descriptor += "Ljava/lang/Object;Ljava/lang/Object;";
                            access = INVOKESTATIC;
                            break;
                    }
                }

                descriptor += ")Z";

                mv.visitMethodInsn(access, path+classname, "equals", descriptor, false);

                if (op.equals("!=")) {
                    mv.visitInsn(ICONST_1);
                    mv.visitInsn(IXOR);
                }
            }
        }
    }

    private String handleArraysToString() {
        ArraySemType arraySemType = (ArraySemType) expression.getLeftTerm().semtype;
        SemType elementType = arraySemType.getElementSemType();
        String fieldDescriptor = elementType.fieldDescriptor();
        if (elementType instanceof RecordSemType recordSemType || elementType instanceof ArraySemType || elementType.equals(stringType)) {
            // if the element type is a record, we need to use the record's field descriptor
            fieldDescriptor = "Ljava/lang/Object;";
        }
        return fieldDescriptor;
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
        if (isFloat) {
            return LAND;
        }
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

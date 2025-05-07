import static org.junit.Assert.*;

import compiler.Lexer.*;
import compiler.Parser.ASTNodes.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Access.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Expressions.*;
import compiler.Parser.ASTNodes.Statements.Expressions.Terms.*;
import compiler.Parser.ASTNodes.Statements.Statements.*;
import compiler.Parser.ASTNodes.Types.Type;
import compiler.Parser.Parser;
import compiler.Compiler;
import compiler.CodeGen.*;
import compiler.SemanticAnalysis.*;
import org.junit.Test;
import java.io.File;

public class TestCodeGeneration {

    @Test
    public void testVariableDeclaration() throws Exception {
        String program = "fun main() { x int = 42; writeln(x); }";
        assertOutputEquals(program, "42\n");
    }

    @Test
    public void testConstantDeclaration() throws Exception {
        String program = """
        final x int = 1;
        fun main() {
            writeln(x);
        }
        """;
        assertOutputEquals(program, "1\n");
    }

    @Test
    public void testShadowedConstantDeclaration() throws Exception {
        String program = """
        final x int = 1;
        fun main() {
            x int = 2;
            writeln(x);
        }
        """;
        assertOutputEquals(program, "2\n");
    }

    @Test
    public void testGlobalVarDecl() throws Exception {
        String program = """
        x int = 42;
        fun main() {
            writeln(x);
        }
        """;
        assertOutputEquals(program, "42\n");
    }

    @Test
    public void testGlobalVarAssignmentDecl() throws Exception {
        String program = """
        x int;
        fun main() {
            x = 10;
            writeln(x);
        }
        """;
        assertOutputEquals(program, "10\n");
    }

    @Test
    public void testShadowedGlobalVar() throws Exception {
        String program = """
        x int = 49;
        fun main() {
            x int = 69;
            writeln(x);
        }
        """;
        assertOutputEquals(program, "69\n");
    }

    @Test
    public void testRecordDefinition() throws Exception {
        String program = """
        Person rec {
            name string;
            age int;
        }
        
        fun main() {
            p Person = Person("Alice", 30);
            writeln(p.name);
            writeln(p.age);
        }
        """;
        assertOutputEquals(program, "Alice\n30\n");
    }

    @Test
    public void testRecordWithRecordField() throws Exception {
        String program = """
        Point rec {
            x int;
            y int;
        }
        
        Person rec {
            name string;
            age int;
            location Point;
        }
        
        fun main() {
            p Person = Person("Alice", 30, Point(10, 20));
            writeln(p.name);
            writeln(p.age);
            writeln(p.location.x);
            writeln(p.location.y);
        }
        """;
        assertOutputEquals(program, "Alice\n30\n10\n20\n");
    }

    @Test
    public void testConstsGlobalRecs() throws Exception {
        String program = """
        final x int = 1;
               
        Point rec {
            x int;
            y int;
        }
        
        p Point;
        
        fun main() {
            p = Point(x, 2);
            writeln(p.x);
            writeln(p.y);
        }
        """;
        assertOutputEquals(program, "1\n2\n");
    }

    @Test
    public void testImplicitConversionIntToFloatVarDecl() throws Exception {
        String program = """
        fun main() {
            f float = 10.5 * 100;
            writeln(f);
        }
        """;
        String expectedOutput = """
        1050.0
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testImplicitConversionFunCall() throws Exception {
        String program = """
        fun main() {
            writeFloat(100);
        }
        """;
        String expectedOutput = """
        100.0
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testStrings() throws Exception {
        String program = """
        myString string = "Hello, World!";
        fun main() {
            writeln(myString);
        }
        """;
        String expectedOutput = """
        Hello, World!
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testStringConcatenation() throws Exception {
        String program = """
        fun main() {
            str1 string = "Hello, ";
            str2 string = "World!";
            writeln(str1 + str2);
        }
        """;
        String expectedOutput = """
        Hello, World!
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testStringIndexing() throws Exception {
        String program = """
        fun main() {
            myString string = "Hello, World!";
            charAtIndex int = myString[0];
            writeln(charAtIndex);
        }
        """;
        // H has ASCII value 72
        String expectedOutput = """
        72
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testArrays() throws Exception {
        String program = """
        c int[] = array [5] of int; 
       
        fun main() {
            c[0] = 1;
            c[1] = 2;
            c[2] = 3;
            c[3] = 4;
            c[4] = 5;
            writeln(c[0]);
            writeln(c[1]);
            writeln(c[2]);
            writeln(c[3]);
            writeln(c[4]);
        }
        """;
        String expectedOutput = """
        1
        2
        3
        4
        5
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testArrayLength() throws Exception {
        String program = """
        c int[] = array [5] of int;
        
        fun main() {
            writeln(len(c));
        }
        """;
        String expectedOutput = """
        5
        """;
        assertOutputEquals(program, expectedOutput);
    }


    @Test
    public void testNewArrayOfRecords() throws Exception {
        String program = """
        Point rec {
            x int;
            y int;
        }
        
        fun main() {
            points Point[] = array [3] of Point;
            points[0] = Point(1, 2);
            points[1] = Point(3, 4);
            points[2] = Point(5, 6);
            i int;
            for (i, 0, 3, 1) {
                writeln(points[i].x);
                writeln(points[i].y);
            }
        }
        """;
        String expectedOutput = """
        1
        2
        3
        4
        5
        6
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testNewArrayOfStrings() throws Exception {
        String program = """
        fun main() {
            strings string[] = array [3] of string;
            strings[0] = "Hello";
            strings[1] = "World";
            strings[2] = "!";
            i int;
            for (i, 0, 3, 1) {
                writeln(strings[i]);
            }
        }
        """;
        String expectedOutput = """
        Hello
        World
        !
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testNewArrayOfBooleans() throws Exception {
        String program = """
        fun main() {
            bools bool[] = array [5] of bool;
            bools[0] = true;
            bools[1] = false;
            bools[2] = true;
            bools[3] = false;
            bools[4] = true;
            i int;
            for (i, 0, 5, 1) {
                writeln(bools[i]);
            }
        }
        """;
        String expectedOutput = """
        true
        false
        true
        false
        true
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testNewArrayOfFloats() throws Exception {
        String program = """
        fun main() {
            floats float[] = array [3] of float;
            floats[0] = 1.1;
            floats[1] = 2.2;
            floats[2] = 3.3;
            i int;
            for (i, 0, 3, 1) {
                writeln(floats[i]);
            }
        }
        """;
        String expectedOutput = """
        1.1
        2.2
        3.3
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testNewArrayWithIntExpressions() throws Exception {
        String program = """
        fun main() {
            intArray int[] = array [5] of int;
            intArray[0] = 1 + 2;
            intArray[1] = 3 * 4;
            intArray[2] = 5 - 6;
            intArray[3] = 7 / 8;
            intArray[4] = 9 % 10;
            i int;
            for (i, 0, 5, 1) {
                writeln(intArray[i]);
            }
        }
        """;
        String expectedOutput = """
        3
        12
        -1
        0
        9
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testFunctionDefinition() throws Exception {
        String program = """
        fun add(a int, b int) int {
            return a + b;
        }
        
        fun main() {
            result int = add(5, 10);
            writeln(result);
        }
        """;
        String expectedOutput = """
        15
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testFunctionWithArrayParameter() throws Exception {
        String program = """
        fun sum(arr int[]) int {
            res int = 0;
            length int = len(arr);
            i int;
            for (i, 0, length, 1) {
                res = res + arr[i];
            }
            return res;
        }
        
        fun main() {
            arr int[] = array [5] of int;
            arr[0] = 1;
            arr[1] = 2;
            arr[2] = 3;
            arr[3] = 4;
            arr[4] = 5;
            result int = sum(arr);
            writeln(result);
        }
        """;
        String expectedOutput = """
        15
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testFunctionWithRecordParameter() throws Exception {
        String program = """
        Point rec {
            x int;
            y int;
        }
        
        fun copyPoint(p Point) Point {
            return Point(p.x, p.y);
        }
        
        fun main() {
            p1 Point = Point(123, 456);
            p2 Point = copyPoint(p1);
            writeln(p2.x);
            writeln(p2.y);
        }
        """;
        String expectedOutput = """
        123
        456
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testFunctionWithRecordReturn() throws Exception {
        String program = """
        Point rec {
            x int;
            y int;
        }
        
        fun createPoint(x int, y int) Point {
            return Point(x, y);
        }
        
        fun main() {
            p Point = createPoint(10, 20);
            writeln(p.x);
            writeln(p.y);
        }
        """;
        String expectedOutput = """
        10
        20
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testBuiltinFunctionCall() throws Exception {
        String program = """
        fun main() {
            writeln("Hello, World!");
            writeln(42);
            writeln(3.14);
            writeln(true);
            writeln(false);
            writeln(1 + 2);
            writeInt(3);
            writeln(); $ add a newline
            writeFloat(4.5);
            writeln();
            writeln(len("Hello"));
            writeln(chr(65));
            writeln(floor(3.14));
        }
        """;
        String expectedOutput = """
        Hello, World!
        42
        3.14
        true
        false
        3
        3
        4.5
        5
        A
        3
        """; // TODO: figure out how to test the read... functions
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testBinaryExpression() throws Exception {
        String program = """
        fun main() {
            i int = 5;
            j int = 10;
            k int = i + j * 2; $ equivalent to (i + j) * 2
            writeln(k);
        }
        """;
        String expectedOutput = """
        30
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testUnaryExpression() throws Exception {
        String program = """
        fun main() {
            i int = 5;
            j int = -i;
            writeln(j);
        }
        """;
        String expectedOutput = """
        -5
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testLogicalExpression() throws Exception {
        String program = """
        fun main() {
            a bool = true;
            b bool = false;
            c bool = a && b;
            d bool = a || b;
            writeln(c);
            writeln(d);
        }
        """;
        String expectedOutput = """
        false
        true
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testSimpleBinaryExpression() throws Exception {
        String program = """
        fun main() {
            a int = 5;
            b int = 10;
            c int = a % b;
            writeln(c);
        }
        """;
        String expectedOutput = """
        5
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testCombinedExpressions() throws Exception {
        String program = """
        fun main() {
            a int = -3;
            b float = 33.3;
            writeln(-(a + b));
        }
        """;
        String expectedOutput = """
        -30.3
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testSimpleArithmetic() throws Exception {
        // Test simple arithmetic expression: (2 + 3) * 4
        String program = """
        fun main() {
            writeln(2 + (3 * 4));
        }
        """;
        assertOutputEquals(program, "14\n");
    }

    @Test
    public void testForLoop() throws Exception {
        String program = """
        fun main() {
            i int;
            for (i, 0, 5, 1) {
                writeln(i);
            }
        }
        """;
        String expectedOutput = """
        0
        1
        2
        3
        4
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testForLoopWithStartVariable() throws Exception {
        String program = """
        fun main() {
            i int;
            start int = 2;
            for (i, start, 5, 1) {
                writeln(i);
            }
        }
        """;
        String expectedOutput = """
        2
        3
        4
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testForLoopWithEndVariable() throws Exception {
        String program = """
        fun main() {
            i int;
            end int = 5;
            for (i, 0, end, 1) {
                writeln(i);
            }
        }
        """;
        String expectedOutput = """
        0
        1
        2
        3
        4
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testForLoopWithStepVariable() throws Exception {
        String program = """
        fun main() {
            i int;
            step int = 2;
            for (i, 0, 10, step) {
                writeln(i);
            }
        }
        """;
        String expectedOutput = """
        0
        2
        4
        6
        8
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testForLoopWithExpression() throws Exception {
        String program = """
        fun main() {
            i int;
            for (i, 0, 5 + 2, 1) {
                writeln(i);
            }
        }
        """;
        String expectedOutput = """
        0
        1
        2
        3
        4
        5
        6
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testForLoopWithExpressionStep() throws Exception {
        String program = """
        fun main() {
            i int;
            for (i, 0, 10, 2 + 1) {
                writeln(i);
            }
        }
        """;
        String expectedOutput = """
        0
        3
        6
        9
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testWhileLoop() throws Exception {
        String program = """
        fun main() {
            i int = 0;
            while (i < 5) {
                writeln(i);
                i = i + 1;
            }
        }
        """;
        String expectedOutput = """
        0
        1
        2
        3
        4
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testWhileLoopWithReturn() throws Exception {
        String program = """
        fun main() {
            i int = 0;
            while (i < 10) {
                if (i == 5) {
                    return;
                }
                writeln(i);
                i = i + 1;
            }
        }
        """;
        String expectedOutput = """
        0
        1
        2
        3
        4
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testWhileLoopWithComplexCondition() throws Exception {
        String program = """
        fun main() {
            i int = 0;
            j int = 0;
            while (i < 5 && j < 3) {
                writeln(i);
                i = i + 1;
                j = j + 1;
            }
        }
        """;
        String expectedOutput = """
        0
        1
        2
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testIfStatement() throws Exception {
        String program = """
        fun main() {
            x int = 5;
            if (x > 0) {
                writeln("Positive");
            }
        }
        """;
        String expectedOutput = """
        Positive
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testIfElseStatement() throws Exception {
        String program = """
        fun main() {
            x int = -5;
            if (x > 0) {
                writeln("Positive");
            } else {
                writeln("Non-positive");
            }
        }
        """;
        String expectedOutput = """
        Non-positive
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testReturnInIf() throws Exception {
        String program = """
        fun main() {
            x int = 5;
            if (x > 0) {
                writeln("Before the return");
                return;
            }
            writeln("This will not be printed");
        }
        """;
        String expectedOutput = """
        Before the return
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testReturnIfElseInFunction() throws Exception {
        String program = """
        Point rec {
            x int;
            y int;
        }
        
        fun testOnPoint(p Point) bool {
           if (p.x > 0 && p.y > 0) {
               return true;
           } else {
               return false;
           }
        }
        
        fun main() {
            p1 Point = Point(-1, 1);
            p2 Point = Point(1, 1);
            writeln(testOnPoint(p1));
            writeln(testOnPoint(p2));
        }
        """;
        String expectedOutput = """
        false
        true
        """;
        assertOutputEquals(program, expectedOutput);
    }

    @Test
    public void testReadInt() throws Exception {
        String program = """
        fun main() {
            writeln("Enter an int:");
            x int;
            x = readInt();
            write("You entered: ");
            writeln(x);
        }
        """;
        String expectedOutput = """
        Enter an int:
        You entered: 42
        """;
        assertOutputEqualsWithInput(program, "42\n", expectedOutput);
    }

    @Test
    public void testReadFloat() throws Exception {
        String program = """
        fun main() {
            writeln("Enter a float:");
            x float;
            x = readFloat();
            write("You entered: ");
            writeln(x);
        }
        """;
        String expectedOutput = """
        Enter a float:
        You entered: 3.14
        """;
        assertOutputEqualsWithInput(program, "3.14\n", expectedOutput);
    }

    @Test
    public void testReadString() throws Exception {
        String program = """
        fun main() {
            writeln("Enter a string:");
            x string;
            x = readString();
            write("You entered: ");
            writeln(x);
        }
        """;
        String expectedOutput = """
        Enter a string:
        You entered: Hello
        """;
        assertOutputEqualsWithInput(program, "Hello\n", expectedOutput);
    }

// test template:
//    @Test
//    public void test() throws Exception {
//        String program = """
//
//        fun main() {
//
//        }
//        """;
//        String expectedOutput = """
//        """;
//        assertOutputEquals(program, expectedOutput);
//    }

    /// Helper method that compiles a program runs it and checks the output with the expected output
    private void assertOutputEquals(String program, String expected) throws Exception {
        File tempFile = File.createTempFile("test", ".lang");
        tempFile.deleteOnExit();
        
        java.io.FileWriter writer = new java.io.FileWriter(tempFile);
        writer.write(program);
        writer.close();
        
        // run the compiler
        Compiler compiler = new compiler.Compiler();
        // set the arguments ourselves, this is kinda shitty but it works...
        compiler.file = tempFile.getAbsolutePath();
        compiler.out = tempFile.getAbsolutePath().replace(".lang", ".class");

        compiler.run();

//        System.out.println("tempFile.getParent() = " + tempFile.getParent());
        String className = tempFile.getName().replace(".lang", "");
        className = className.substring(0, 1).toUpperCase() + className.substring(1);

        
        // execute the compiled class and capture output
//        Process process = Runtime.getRuntime().exec("java -cp " + tempFile.getParent() + " " + className);
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", tempFile.getParent(), className);
        processBuilder.redirectErrorStream(true); // redirect error stream to output stream
        Process process = processBuilder.start();

        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        // wait for the process to finish
        process.waitFor();
        System.out.println("output = " + output);

        // verify output
        assertEquals(expected, output.toString());
    }

    private void assertOutputEqualsWithInput(String program, String input, String expected) throws Exception {
        File tempFile = File.createTempFile("test", ".lang");
        tempFile.deleteOnExit();

        java.io.FileWriter writer = new java.io.FileWriter(tempFile);
        writer.write(program);
        writer.close();

        // run the compiler
        Compiler compiler = new compiler.Compiler();
        compiler.file = tempFile.getAbsolutePath();
        compiler.out = tempFile.getAbsolutePath().replace(".lang", ".class");
        compiler.run();

        String className = tempFile.getName().replace(".lang", "");
        className = className.substring(0, 1).toUpperCase() + className.substring(1);

        // execute the compiled class with input
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", tempFile.getParent(), className);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        // Write input to the process
        if (input != null && !input.isEmpty()) {
            java.io.OutputStream stdin = process.getOutputStream();
            stdin.write(input.getBytes());
            stdin.flush();
            stdin.close();
        }

        // Read the output
        java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        // wait for the process to finish
        process.waitFor();
        System.out.println("output = " + output);

        // verify output
        assertEquals(expected, output.toString());
    }
}

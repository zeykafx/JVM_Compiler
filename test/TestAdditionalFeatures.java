import compiler.Compiler;
import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;


public class TestAdditionalFeatures {

	@Test
    public void testMethodOnRecord() throws Exception {
        String program = """
		Rectangle rec {
			length int;
			width int;
		}
		
		fun (r Rectangle) area() int {
			return r.length * r.width;
		}
		
		fun main() {
			r Rectangle = Rectangle(3, 4);
			writeln(r.area());
		}
        """;
        String expectedOutput = """
        12
        """;
        assertOutputEquals(program, expectedOutput);
    }

	@Test
    public void testRecordToString() throws Exception {
        String program = """
		final i int = 4;
		Point rec {
			x int;
			y int;
		}
		Person rec{
			name string;
			location Point;
			history int[];
		}
		person Person = Person("me", Point(3,7), array [i*2] of int);
        fun main() {
			writeln(person);
        }
        """;
		// toString on the records will print each field, and it will also call toString on the records
        String expectedOutput = """
        Person {name= me, location= Point {x= 3, y= 7}, history= [0, 0, 0, 0, 0, 0, 0, 0]}
        """;
        assertOutputEquals(program, expectedOutput);
    }

	@Test
	public void testArraysToString() throws Exception {
		String program = """
		final i int = 4;
		Point rec {
			x int;
			y int;
		}
		fun main() {
			points Point[] = array [i] of Point;
			writeln(points);
		}
		""";
		String expectedOutput = """
		[null, null, null, null]
		""";
		assertOutputEquals(program, expectedOutput);
	}

	@Test
	public void testArraysToStringWithValues() throws Exception {
		String program = """
		final i int = 4;
		Point rec {
			x int;
			y int;
		}
		fun main() {
			points Point[] = array [i] of Point;
			points[0] = Point(1, 2);
			points[1] = Point(3, 4);
			writeln(points);
		}
		""";
		String expectedOutput = """
		[Point {x= 1, y= 2}, Point {x= 3, y= 4}, null, null]
		""";
		assertOutputEquals(program, expectedOutput);
	}

	@Test
	public void testArraysToStringWithInts() throws Exception {
		String program = """
		final i int = 4;
		fun main() {
			points int[] = array [i] of int;
			points[0] = 1;
			points[1] = 2;
			writeln(points);
		}
		""";
		String expectedOutput = """
		[1, 2, 0, 0]
		""";
		assertOutputEquals(program, expectedOutput);
	}

	@Test
	public void testArraysToStringWithStrings() throws Exception {
		String program = """
		final i int = 2;
		fun main() {
			points string[] = array [i] of string;
			points[0] = "hello";
			points[1] = "world";
			writeln(points);
		}
		""";
		String expectedOutput = """
		[hello, world]
		""";
		assertOutputEquals(program, expectedOutput);
	}

	@Test
	public void testArraysToStringWithBooleans() throws Exception {
		String program = """
		final i int = 2;
		fun main() {
			points bool[] = array [i] of bool;
			points[0] = true;
			points[1] = false;
			writeln(points);
		}
		""";
		String expectedOutput = """
		[true, false]
		""";
		assertOutputEquals(program, expectedOutput);
	}

	@Test
	public void testArraysToStringWithFloats() throws Exception {
		String program = """
		final i int = 2;
		fun main() {
			points float[] = array [i] of float;
			points[0] = 1.0;
			points[1] = 2.0;
			writeln(points);
		}
		""";
		String expectedOutput = """
		[1.0, 2.0]
		""";
		assertOutputEquals(program, expectedOutput);
	}

	@Test
	public void testComplexBinaryExpression() throws Exception {
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
	public void testForLoopWithComplexExpression() throws Exception {
		String program = """
        Point rec {
			x int;
			y int;
		}
        fun main() {
        	p Point = Point(1, 2);
            i int;
            for (i, 0, 5 + p.y * 2, 1) { $ ((5 + 2) * 2) = 14
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
        7
        8
        9
        10
        11
        12
        13
        """;
		assertOutputEquals(program, expectedOutput);
	}

	@Test
	public void testSimpleOrdCall() throws Exception {
		String program = """
		fun main() {
			val int = ord("A");
			writeln(val);
		}
		""";
		String expectedOutput = """
		65
		""";
		assertOutputEquals(program, expectedOutput);
	}

	@Test
	public void testOrdAndStringIndexModificationFunction() throws Exception {
		String program = """
		fun main(){
			val string = "abc";
			write("Before: ");
			writeln(val);
		
			val[2] = ord("C");   $ using the new ord() function
			write("After: ");
			writeln(val);
		}
		""";
		String expectedOutput = """
		Before: abc
		After: abC
		""";
		assertOutputEquals(program, expectedOutput);
	}

    @Test
    public void testShortCircuitEvaluation() throws Exception {
        String program = """
		fun complexExpression() bool {
			writeln("Complex expression evaluated");
			return true;
		}
        fun main() {
			if (false && complexExpression()) {  $ complexExpression() will not be evaluated
				writeln("This will not be printed");
			}
			if (true || complexExpression()) {   $ complexExpression() will not be evaluated
				writeln("This will be printed");
			}
        }
        """;
        String expectedOutput = """
        This will be printed
        """;
        assertOutputEquals(program, expectedOutput);
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
//		className = className.substring(0, 1).toUpperCase() + className.substring(1);


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

}

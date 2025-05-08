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
	public void testComplexMethodOnRecord() throws Exception {
		String program = """
		History rec {
		  locations Location[];
		  visited bool[];
		}
		Location rec {
		  x int;
		  y int;
		}
		fun (h History) addLocation(l Location, index int) {
		  if (index < 0 || index >= len(h.locations)) {
			writeln("Index out of bounds");
			return;
		  }
		  h.locations[index] = l;
		  h.visited[index] = true;
		}
		
		fun main() {
		  h History = History(array [5] of Location, array [5] of bool);
		  l Location = Location(1, 2);
		  l2 Location = Location(3, 4);
		  h.addLocation(l, 0);
		  h.addLocation(l2, 1);
		  writeln(h.locations);
		}
        """;
		String expectedOutput = """
		[Location {x: 1, y: 2}, Location {x: 3, y: 4}, null, null, null]
        """;
		assertOutputEquals(program, expectedOutput);
	}

	@Test
	public void testComplexMethodOnRecord2() throws Exception {
		String program = """
		History rec {
			locations Location[];
			visited bool[];
			length int;
		}
		Location rec {
			x int;
			y int;
		}
		fun (h History) addLocation(l Location, index int) {
			if (index < 0 || index >= len(h.locations)) {
				writeln("Index out of bounds");
				return;
			}
			h.locations[index] = l;
			h.visited[index] = true;
			h.length = h.length + 1;
		}
		
		fun (h History) insertLastLocation(l Location) {
			if (h.length >= len(h.locations)) {
				writeln("History is full");
				return;
			}
			h.locations[h.length] = l;
			h.visited[h.length] = true;
			h.length = h.length + 1;
		}
		
		fun main() {
			h History = History(array [5] of Location, array [5] of bool, 0);
			l Location = Location(1, 2);
			l2 Location = Location(3, 4);
			h.addLocation(l, 0);
			h.insertLastLocation(l2);
			writeln(h.locations);
			h.insertLastLocation(l2);
			h.insertLastLocation(l2);
			h.insertLastLocation(l2);
			h.insertLastLocation(l2);
		}
        """;
		String expectedOutput = """
		[Location {x: 1, y: 2}, Location {x: 3, y: 4}, null, null, null]
		History is full
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
		Person {name: "me", location: Point {x: 3, y: 7}, history: [0, 0, 0, 0, 0, 0, 0, 0]}
		""";
        assertOutputEquals(program, expectedOutput);
    }

	@Test
	public void recursiveRecordTest() throws Exception {
		String program = """
        Person rec {
            name string;
            age int;
            contact Person;
        }
        
        fun main() {
            nullPerson Person;
            john Person = Person("John", 25, nullPerson);
            bob Person = Person("Bob", 24, john);
            alice Person = Person("Alice", 23, bob);
            writeln(alice);
        }
        """;
		String expectedOutput = """
        Person {name: "Alice", age: 23, contact: Person {name: "Bob", age: 24, contact: Person {name: "John", age: 25, contact: null}}}
        """;
		assertOutputEquals(program, expectedOutput);
	}

	@Test
	public void testRecursiveRecordWithRecordArray() throws Exception {
		String program = """
        Person rec {
            name string;
            age int;
            bestFriend Person;
            friends Friend[];
            scores int[];
        }
        Friend rec {
            name string;
            age int;
        }
        fun main() {
            nullPerson Person;
            bestFriend Person = Person("Bob", 33, nullPerson, array [3] of Friend, array [3] of int);
            p Person = Person("Alice", 30, bestFriend, array [3] of Friend, array [3] of int);
            p.friends[0] = Friend("Bob", 25);
            p.friends[1] = Friend("Charlie", 28);
            p.friends[2] = Friend("Dave", 22);
            p.scores[0] = 10;
            p.scores[1] = 20;
            p.scores[2] = 30;
            writeln(p.friends);
            writeln(p.scores);
        }
        """;
		String expectedOutput = """
        [Friend {name: "Bob", age: 25}, Friend {name: "Charlie", age: 28}, Friend {name: "Dave", age: 22}]
        [10, 20, 30]
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
		[Point {x: 1, y: 2}, Point {x: 3, y: 4}, null, null]
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
	public void testStringEquality() throws Exception {
		String program = """
		fun main() {
			hello string = "Hello";
			hello2 string = "Hello";
			if (hello == hello2) {
				writeln("Hello World");
			} else {
				writeln(":(");
			}
		}
		""";
		String expectedOutput = """
		Hello World
		""";
		assertOutputEquals(program, expectedOutput);
	}

	@Test
	public void testIntEquality() throws Exception {
		String program = """
		fun main() {
			i int = 5;
			j int = 5;
			if (i == j) {
				writeln("Equal");
			} else {
				writeln("Not Equal");
			}
		}
		""";
		String expectedOutput = """
		Equal
		""";
		assertOutputEquals(program, expectedOutput);
	}

	@Test
	public void testFloatEquality() throws Exception {
		String program = """
		fun main() {
			i float = 5.0;
			j float = 5.0;
			if (i == j) {
				writeln("Equal");
			} else {
				writeln("Not Equal");
			}
		}
		""";
		String expectedOutput = """
		Equal
		""";
		assertOutputEquals(program, expectedOutput);
	}

	@Test
	public void testRecordEquality() throws Exception {
		String program = """
		Point rec {
			x int;
			y int;
		}
		fun main() {
			p1 Point = Point(1, 2);
			p2 Point = Point(1, 2);
			if (p1 == p2) {
				writeln("Equal");
			} else {
				writeln("Not Equal");
			}
		}
		""";
		String expectedOutput = """
		Equal
		""";
		assertOutputEquals(program, expectedOutput);
	}

	@Test
	public void testRecordEqualInArray() throws Exception {
		String program = """
		Point rec {
			x int;
			y int;
		}
		fun main() {
			points Point[] = array [2] of Point;
			points[0] = Point(1, 2);
			points[1] = Point(3, 4);
			
			points2 Point[] = array [2] of Point;
			points2[0] = Point(1, 2);
			points2[1] = Point(3, 4);
			if (points == points2) {
				writeln("Equal");
			} else {
				writeln("Not Equal");
			}
		}
		""";
		String expectedOutput = """
		Equal
		""";
		assertOutputEquals(program, expectedOutput);
	}

	@Test
	public void testArrayOfIntsEquality() throws Exception {
		String program = """
		fun main() {
			arr1 int[] = array [2] of int;
			arr1[0] = 1;
			arr1[1] = 2;
			
			arr2 int[] = array [2] of int;
			arr2[0] = 1;
			arr2[1] = 2;
			
			if (arr1 == arr2) {
				writeln("Equal");
			} else {
				writeln("Not Equal");
			}
		}
		""";
		String expectedOutput = """
		Equal
		""";
		assertOutputEquals(program, expectedOutput);
	}

	@Test
	public void testArrayOfStringsEquality() throws Exception {
		String program = """
		fun main() {
			arr1 string[] = array [2] of string;
			arr1[0] = "hello";
			arr1[1] = "world";
			
			arr2 string[] = array [2] of string;
			arr2[0] = "hello";
			arr2[1] = "world";
			
			if (arr1 == arr2) {
				writeln("Equal");
			} else {
				writeln("Not Equal");
			}
		}
		""";
		String expectedOutput = """
		Equal
		""";
		assertOutputEquals(program, expectedOutput);
	}

	@Test
	public void testArrayOfBooleansEquality() throws Exception {
		String program = """
		fun main() {
			arr1 bool[] = array [2] of bool;
			arr1[0] = true;
			arr1[1] = false;
			
			arr2 bool[] = array [2] of bool;
			arr2[0] = true;
			arr2[1] = false;
			
			if (arr1 == arr2) {
				writeln("Equal");
			} else {
				writeln("Not Equal");
			}
		}
		""";
		String expectedOutput = """
		Equal
		""";
		assertOutputEquals(program, expectedOutput);
	}

	@Test
	public void testArrayOfFloatsEquality() throws Exception {
		String program = """
		fun main() {
			arr1 float[] = array [2] of float;
			arr1[0] = 1.0;
			arr1[1] = 2.0;
			
			arr2 float[] = array [2] of float;
			arr2[0] = 1.0;
			arr2[1] = 2.0;
			
			if (arr1 == arr2) {
				writeln("Equal");
			} else {
				writeln("Not Equal");
			}
		}
		""";
		String expectedOutput = """
		Equal
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

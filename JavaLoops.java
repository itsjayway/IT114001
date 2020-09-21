
public class JavaLoops {
	public static void main(String[] args) {
		int[] arr = { 3, 5, 6, 12, 13, 14, 22, 23 };
		// initialize array in numerical order
		for (int i : arr) { // for-each loop that iterates through each element in the array
			if (i % 2 == 0) { // tests is the element is even
				System.out.println(i); // prints out element
			}

		}
	}
}

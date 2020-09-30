public class Recursion {

	public static int sum(int num) {
		int sum = 0;
		while (num > 0) {
			sum += num;
			num--;
		}
		return sum;
	}

	public static void main(String[] args) {
		System.out.println(sum(10));
	}
}
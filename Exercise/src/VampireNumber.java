import java.util.Arrays;

/**
 * An implementation of a problem in Chapter Controlling Execution, Thinking in
 * Java Fourth Edition, wrote by Bruce Eckel.
 * <p>
 * Exercise 10: (5) A vampire number has an even number of digits and is formed
 * by multiplying a pair of numbers containing half the number of digits of the
 * result. The digits are taken from the original number in any order. Pairs of
 * trailing zeroes are not allowed.
 * <ul>
 * Examples include:
 * <li>1260 = 21 * 60
 * <li>1827 = 21 * 87
 * <li>2187 = 27 * 81
 * </ul>
 * Write a program that finds all the 4-digit vampire numbers. (Suggested by Dan
 * Forhan.)
 * 
 * @author Finally
 */
class VampireNumber {

	static final int LENGTH = 4;

	static final int INIT = (int) Math.pow(10, LENGTH / 2 - 1);

	static final int LIMIT = INIT * 10;

	static int left = INIT;

	static int right = left;

	/**
	 * Go to next pair.
	 * <p>
	 * Guarantees {@code right}>={@code left} always true, which reduces about
	 * half of the loop times.
	 * 
	 * @return {@code false} if {@code left} reaches {@code LIMIT}, {@code true}
	 *         other wise
	 */
	static boolean next() {
		right++;
		if (right < LIMIT)
			return true;
		left++;
		if (left == LIMIT)
			return false;
		right = left;
		return true;
	}

	/**
	 * Check whether the number represented by the pair is a vampire number.
	 * 
	 * @return <li>{@code false} if the pair has trailing zeros. <li>{@code true}
	 *         if {@code left}*{@code right} has same digit components as the
	 *         total of {@code left} and {@code right}. {@code false} otherwise
	 */
	static boolean isVampireNumber() {
		if (left % 10 == 0 && right % 10 == 0)
			return false;
		if (Arrays.equals(digits(left * 100 + right), digits(left * right)))
			return true;
		return false;
	}

	/**
	 * Count what and how many digits a number contains.
	 */
	static int[] digits(int number) {
		int[] digits = new int[10];
		while (number != 0) {
			digits[number % 10]++;
			number /= 10;
		}
		return digits;
	}

	/**
	 * Instead of looping through all 4-digit numbers and try all permutation to
	 * seek a product equal to that number itself, we test all composition of
	 * 2-digit number pairs. If the product of a pair has same digit components
	 * as the pair, plus the pair is not of trailing zeros, the product is a
	 * vampire number.
	 * <ol>
	 * Work around the details below:
	 * <li>The loop starts with {@code left}=10, {@code right}=10, since both
	 * factor has 2 digits.
	 * <li>If both {@code left} and {@code right} has trailing zero, skip.
	 * <li>Check if {@code left} multiply {@code right} is a vampire number.
	 * <li>Go to next pair.
	 * <li>If next pair has {@code left}>100, end loop. Else go to step 2.
	 */
	public static void main(String[] args) {
		do {
			if (isVampireNumber())
				System.out.println(left * right);
		} while (next());
	}
}

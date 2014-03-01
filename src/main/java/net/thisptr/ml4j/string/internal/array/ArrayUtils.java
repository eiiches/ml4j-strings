package net.thisptr.ml4j.string.internal.array;

import net.thisptr.specialize.Specialize;

public class ArrayUtils {
	private ArrayUtils() {
	}

	@Specialize("T: *")
	public static void fill(final Array<T> array, final T value) {
		fill(array, 0, array.size(), value);
	}

	@Specialize("T: *")
	public static void fill(final Array<T> array, final int begin, final int end, final T value) {
		for (int i = begin; i < end; ++i)
			array.set(i, value);
	}

	@Specialize("T: *")
	public static boolean equals(final Array<T> a1, final Array<T> a2) {
		if (a1.size() != a2.size())
			return false;

		final int size = a1.size();
		for (int i = 0; i < size; ++i) {
			if (a1.at(i) != a2.at(i))
				return false;
		}

		return true;
	}

	@Specialize("T: *")
	public static boolean equals(final Array<T> a1, final int offset1, final Array<T> a2, final int offset2, final int length) {
		for (int i = 0; i < length; ++i)
			if (a1.at(offset1 + i) != a2.at(offset2 + i))
				return false;
		return true;
	}
}

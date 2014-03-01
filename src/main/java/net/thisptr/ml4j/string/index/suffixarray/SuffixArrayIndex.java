package net.thisptr.ml4j.string.index.suffixarray;

import java.util.Iterator;
import java.util.NoSuchElementException;

import net.thisptr.ml4j.string.index.StringIndex;
import net.thisptr.ml4j.string.internal.array.Array;
import net.thisptr.ml4j.string.internal.array.ArrayWrappedArray;
import net.thisptr.ml4j.string.internal.array.StringWrappedArray;
import net.thisptr.specialize.Specialize;

public class SuffixArrayIndex implements StringIndex {
	private int[] sa;
	private Array<$char> seq;

	public SuffixArrayIndex(final int[] sa, final char[] seq) {
		this(sa, new ArrayWrappedArray<$char>(seq));
	}

	public SuffixArrayIndex(final int[] sa, final String seq) {
		this(sa, new StringWrappedArray(seq));
	}

	/* package-private */SuffixArrayIndex(final int[] sa, final Array<$char> seq) {
		this.sa = sa;
		this.seq = seq;
	}

	public int[] raw() {
		return sa;
	}

	private static int compare(final Array<$char> seq, final int begin, final CharSequence needle) {
		for (int i = 0; i < needle.length(); ++i) {
			if (begin + i >= seq.size())
				return -1;
			final int d = (int) seq.at(begin + i) - (int) needle.charAt(i);
			if (d < 0)
				return -1;
			if (d > 0)
				return 1;
		}
		return 0;
	}

	private int bsearch(final CharSequence needle) {
		int low = 0;
		int high = sa.length - 1;

		while (low <= high) {
			final int mid = (low + high) / 2;
			final int c = compare(seq, sa[mid], needle); // seq[sa[cur]...] - needle
			if (c < 0) {
				low = mid + 1;
			} else if (c > 0) {
				high = mid - 1;
			} else {
				if (low != mid) {
					high = mid;
				} else {
					return mid;
				}
			}
		}

		return sa.length;
	}

	@Override
	public Iterator<$int> search(final CharSequence needle) {
		return new Iterator<$int>() {
			private int cursor = bsearch(needle);
			private boolean fresh = true;

			@Override
			public boolean hasNext() {
				if (cursor >= sa.length)
					return false;

				if (fresh)
					return true;

				if (compare(seq, sa[++cursor], needle) != 0) {
					cursor = sa.length;
					return false;
				}

				fresh = true;
				return true;
			}

			@Override
			public int next() {
				if (!hasNext())
					throw new NoSuchElementException();

				fresh = false;
				return cursor;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}

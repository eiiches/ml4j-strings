package net.thisptr.ml4j.string.index.suffixarray;

import java.util.Iterator;

import net.thisptr.ml4j.string.internal.array.Array;
import net.thisptr.ml4j.string.internal.array.ArrayUtils;
import net.thisptr.ml4j.string.internal.array.ArrayWrappedArray;
import net.thisptr.ml4j.string.internal.array.StringWrappedArray;
import net.thisptr.specialize.Specialize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of SA-IS, a suffix array construction algorithm, as described in, Ge Nong,
 * "Two Efficient Algorithms for Linear Time Suffix Array Construction," IEEE Transactions on Computers, vol. 60, no. 10, Oct. 2011.
 * 
 * Also some optimization have been used from, [Nong 11, OSACA] Ge Nong, "An Optimal Suffix Array Construction Algorithm," Technical Report, Department of
 * Computer Science, Sun Yat-sen University, 2011.
 * 
 */
public final class SuffixArrayIndexCompiler {
	private static final int EMPTY = -1;

	@Specialize("T: int, char")
	private static boolean isS(final Array<T> seq, final int index) {
		final int n = seq.size();

		T c0 = seq.at(index);
		for (int i = index; i < n - 1; ++i) {
			final T c1 = seq.at(i + 1);
			if (c0 < c1)
				return true;
			if (c0 > c1)
				return false;
			c0 = c1;
		}

		return false;
	}

	/**
	 * Return whether <tt>text[index]</tt> is a LMS character. A character <tt>text[index]</tt> is called LMS, if <tt>text[i]</tt> is S-type and
	 * <tt>text[index - 1]</tt> is L-type.
	 * 
	 * @param text
	 * @param index
	 * @return
	 */
	@Specialize("T: int, char")
	private static boolean isLms(final Array<T> seq, final int index) {
		if (index == 0)
			return false; // by definition

		// return isS(seq, index) && !isS(seq, index - 1);
		return isS(seq, index) && seq.at(index - 1) > seq.at(index);
	}

	/**
	 * @param seq
	 * @param alphabetSize
	 * @return
	 */
	@Specialize("T: int, char")
	private static int[] buildCountArray(final Array<T> seq, final int alphabetSize) {
		final int[] result = new int[alphabetSize];

		for (int i = 0; i < seq.size(); ++i) {
			final T ch = seq.at(i);
			++result[ch];
		}

		return result;
	}

	private static int[] buildBucketIndices(final int[] counts) {
		int index = 0;
		final int[] indices = new int[counts.length];
		for (int i = 0; i < counts.length; ++i) {
			indices[i] = index;
			index += counts[i];
		}
		return indices;
	}

	private static int[] buildBucketEndIndices(final int[] counts) {
		int index = 0;
		final int[] indices = new int[counts.length];
		for (int i = 0; i < counts.length; ++i) {
			index += counts[i];
			indices[i] = index;
		}
		return indices;
	}

	private interface IndexVisitor {
		void visit(int index);
	}

	@Specialize("T: int, char")
	private static void forEachLmsReversed(final Array<T> seq, final IndexVisitor visitor) {
		forEachLmsReversed(seq, visitor, false);
	}

	@Specialize("T: int, char")
	private static void forEachLmsReversed(final Array<T> seq, final IndexVisitor visitor, final boolean includeSentinel) {
		if (includeSentinel)
			visitor.visit(seq.size());

		boolean isS = false;
		for (int i = seq.size() - 2; 0 <= i; --i) {
			final T c0 = seq.at(i);
			final T c1 = seq.at(i + 1);
			if (c0 > c1) {
				if (isS)
					visitor.visit(i + 1);
				isS = false;
			} else if (c0 < c1) {
				isS = true;
			}
		}
	}

	/**
	 * @param text
	 * @param counts
	 * @param suffixArray
	 *            A suffix array (with L-types sorted) to induce sort. This array will be modified directly.
	 */
	@Specialize("T: int, char")
	private static void induceSortS(final Array<T> seq, final int[] counts, final Array<$int> sa) {
		final int n = sa.size();
		final int[] bucketEndIndices = buildBucketEndIndices(counts);

		for (int i = n - 1; i >= 0; --i) {
			final int sai = sa.at(i);
			if (sai <= 0)
				continue;

			// suf(S, SA[i] - 1) is S-type iff,
			// (1). S[SA[i] - 1] < S[SA[i]] or,
			// (2). S[SA[i] - 1] == S[SA[i]] and bucketEndIndices[S[SA[i] - 1]] <= i
			// see details for section 3 of [Nong 11, OSACA] for this optimization
			final T ssai0 = seq.at(sai - 1);
			final T ssai1 = seq.at(sai);
			if (ssai0 < ssai1 || ssai0 == ssai1 && bucketEndIndices[ssai0] <= i) // if (isS(seq, sai - 1))
				sa.set(--bucketEndIndices[ssai0], sai - 1);
		}
	}

	/**
	 * @param text
	 * @param counts
	 * @param suffixArray
	 *            A suffix array to induce sort. This array will be modified directly.
	 */
	@Specialize("T: int, char")
	private static void induceSortL(final Array<T> seq, final int[] counts, final Array<$int> sa) {
		final int n = seq.size();
		final int[] bucketIndices = buildBucketIndices(counts);

		// suffixArray[-1] is the sentinel '$'.
		// if (isL(seq, seq.size() - 1)) which is always true, then
		sa.set(bucketIndices[seq.at(n - 1)]++, n - 1);

		for (int i = 0; i < n; ++i) {
			final int sai = sa.at(i);
			if (sai <= 0)
				continue;

			// see section 3 of [Nong 11, OSACA] for this optimization
			// if (!isS(seq, sa.at(i) - 1))
			final int ssai0 = seq.at(sai - 1);
			final int ssai1 = seq.at(sai);
			if (ssai0 >= ssai1)
				sa.set(bucketIndices[ssai0]++, sai - 1);
		}
	}

	@Specialize("T: int, char")
	private static void sais(final Array<T> seq, final Array<$int> sa, final int alphabetSize) {
		final int n = seq.size();
		if (n == 0)
			return;

		ArrayUtils.fill(sa, EMPTY);

		/*
		 * stage 1: reduce the problem by at least 1/2.
		 */

		final int[] counts = buildCountArray(seq, alphabetSize);

		/* Step-1: */{
			final int[] bucketEndIndices = buildBucketEndIndices(counts);
			forEachLmsReversed(seq, new IndexVisitor() {
				@Override
				public void visit(final int index) {
					sa.set(--bucketEndIndices[seq.at(index)], index);
				}
			});
		}

		/* Step-2: induce sort all the L-type LMS-prefixes */
		induceSortL(seq, counts, sa);
		/* Step-3: induce sort all the LMS-prefixes from the sorted L-type prefixes */
		induceSortS(seq, counts, sa);

		/* compact all the sorted substrings into the first m items of SA */
		/* 2*m must be not larger than n */

		int _n1 = 0;
		for (int i = 0; i < n; ++i) {
			if (isLms(seq, sa.at(i)))
				sa.set(_n1++, sa.at(i));
		}
		final int n1 = _n1;

		if (n1 == 0)
			return;

		/* initialize the name array buffer */
		ArrayUtils.fill(sa, n1, n1 + n / 2, EMPTY);

		/* store the length of all substring */
		forEachLmsReversed(seq, new IndexVisitor() {
			private int nextIndex = n;

			@Override
			public void visit(final int index) {
				sa.set(n1 + index / 2, nextIndex - index + 1);
				nextIndex = index;
			}
		}, true);

		int nNames = 0;
		/* find the lexicographic names of all substrings */{
			int pBegin = n;
			int pLength = 0;
			for (int i = 0; i < n1; ++i) {
				int begin = sa.at(i);
				int length = sa.at(n1 + begin / 2);
				if (!isSequenceEqual(seq, pBegin, pLength, begin, length)) {
					++nNames;
					pBegin = begin;
					pLength = length;
				}
				sa.set(n1 + begin / 2, nNames - 1);
			}
		}

		// move over names to left into range [n1, 2 * n1).
		for (int i = n1, j = n1; i < n1 + n / 2; ++i)
			if (sa.at(i) != EMPTY)
				sa.set(j++, sa.at(i));

		/*
		 * stage 2: solve the reduced problem, recurse if names are not yet unique. in this recursion eventually names will be unique.
		 */

		if (nNames < n1) {
			// names are not unique.
			sais(sa.view(n1, n1 + n1), sa.view(0, n1), nNames);
		} else {
			// since names are unique, it is easy to build suffix array for LMS-substrings names.
			for (int i = 0; i < n1; ++i)
				sa.set(sa.at(n1 + i), i);
		}

		/*
		 * stage 3: Induce the result for the original problem.
		 */

		forEachLmsReversed(seq, new IndexVisitor() {
			private int j = n1;

			@Override
			public void visit(final int index) {
				sa.set(n1 + --j, index);
			}
		});

		for (int i = 0; i < n1; ++i)
			sa.set(i, sa.at(sa.at(i) + n1));

		ArrayUtils.fill(sa, n1, n, EMPTY);

		/* sorted prefixes into their bucket */{
			final int[] bucketEndIndices = buildBucketEndIndices(counts);
			for (int i = n1 - 1; i >= 0; --i) {
				final int tmp = sa.at(i);
				sa.set(i, EMPTY);
				sa.set(--bucketEndIndices[seq.at(tmp)], tmp);
			}
		}

		induceSortL(seq, counts, sa);
		induceSortS(seq, counts, sa);
	}

	@Specialize("T: int, char")
	private static boolean isSequenceEqual(final Array<T> seq, final int leftBegin, final int leftLength, final int rightBegin, final int rightLength) {
		if (rightLength != leftLength || rightBegin + rightLength >= seq.size() || leftBegin + leftLength >= seq.size())
			return false;
		return ArrayUtils.equals(seq, leftBegin, seq, rightBegin, rightLength);
	}

	public SuffixArrayIndex compile(final char[] seq) {
		final int[] result = new int[seq.length];
		final Array<$char> in = new ArrayWrappedArray<$char>(seq);
		sais(in, new ArrayWrappedArray<$int>(result), Character.MAX_VALUE + 1);
		return new SuffixArrayIndex(result, in);
	}

	public SuffixArrayIndex compile(final String seq) {
		final int[] result = new int[seq.length()];
		final Array<$char> in = new StringWrappedArray(seq);
		sais(in, new ArrayWrappedArray<$int>(result), Character.MAX_VALUE + 1);
		return new SuffixArrayIndex(result, in);
	}
}

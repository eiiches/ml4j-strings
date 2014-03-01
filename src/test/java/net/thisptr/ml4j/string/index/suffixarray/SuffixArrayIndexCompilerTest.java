package net.thisptr.ml4j.string.index.suffixarray;

import static org.junit.Assert.assertArrayEquals;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

public class SuffixArrayIndexCompilerTest {
	private SuffixArrayIndexCompiler sut;

	@Before
	public void setUp() {
		sut = new SuffixArrayIndexCompiler();
	}

	@Test
	public void test() {
		final SuffixArrayIndex index = sut.compile("abracadabra");
		final int[] expected = new int[] {
				10, // a
				7, // abra
				0, // abracadabra
				3, // acadabra
				5, // adabra
				8, // bra
				1, // bracadabra
				4, // cadabra
				6, // dabra
				9, // ra
				2, // racadabra
		};
		assertArrayEquals(expected, index.raw());
		Iterator<$int> iter = index.search("ab");
		while (iter.hasNext()) {
			final int i = iter.next();
			System.out.println(i);
		}
	}
}

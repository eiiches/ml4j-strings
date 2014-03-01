package net.thisptr.ml4j.string.index;

import java.util.Iterator;

public interface StringIndex {
	Iterator<$int> search(final CharSequence needle);
}

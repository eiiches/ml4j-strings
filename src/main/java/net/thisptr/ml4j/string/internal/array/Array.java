package net.thisptr.ml4j.string.internal.array;

import net.thisptr.specialize.Specialize;

@Specialize("T: *")
public interface Array<T> {
	T at(int index);

	void set(int index, T value);

	Array<T> view(int begin, int end);

	int size();
}

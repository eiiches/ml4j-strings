package net.thisptr.ml4j.string.internal.array;

import net.thisptr.specialize.Specialize;

@Specialize("T: *")
public class ArrayWrappedArray<T> implements Array<T> {
	private T[] buf;
	private int size;
	private int offset;

	public ArrayWrappedArray(final T[] buf) {
		this(buf, 0, buf.length);
	}

	private ArrayWrappedArray(final T[] buf, final int offset, final int size) {
		this.buf = buf;
		this.offset = offset;
		this.size = size;
	}

	@Override
	public T at(int index) {
		return buf[index + offset];
	}

	@Override
	public void set(int index, T value) {
		buf[index + offset] = value;
	}

	@Override
	public Array<T> view(int begin, int end) {
		return new ArrayWrappedArray<T>(buf, offset + begin, end - begin);
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public String toString() {
		String sep = "";
		final StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < size; ++i) {
			sb.append(sep);
			sb.append(buf[offset + i]);
			sep = ", ";
		}
		sb.append("]");
		return sb.toString();
	}
}

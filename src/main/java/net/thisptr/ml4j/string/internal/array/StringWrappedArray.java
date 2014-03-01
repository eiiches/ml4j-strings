package net.thisptr.ml4j.string.internal.array;

public class StringWrappedArray implements Array<$char> {
	private CharSequence buf;
	private int offset;
	private int size;

	public StringWrappedArray(final CharSequence buf) {
		this(buf, 0, buf.length());
	}

	public StringWrappedArray(final CharSequence buf, final int offset, final int size) {
		this.buf = buf;
		this.offset = offset;
		this.size = size;
	}

	@Override
	public char at(final int index) {
		return buf.charAt(index);
	}

	@Override
	public void set(final int index, final char value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Array<$char> view(final int begin, final int end) {
		return new StringWrappedArray(buf, offset + begin, end - begin);
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
			sb.append(buf.charAt(offset + i));
			sep = ", ";
		}
		sb.append("]");
		return sb.toString();
	}
}

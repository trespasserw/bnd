package aQute.lib.collections;

import java.util.*;

public class IteratorList<T> extends ArrayList<T> {
	private static final long serialVersionUID = 1L;

	public IteratorList(Iterator<T> i) {
		while (i.hasNext())
			add(i.next());
	}
}

package net.gnu.util;

import java.util.Map;
import java.io.Serializable;

public class ComparableEntry<K extends Comparable<K>, V> implements
		Map.Entry<K, V>, Serializable,
		Comparable<ComparableEntry<K, V>> {

	private static final long serialVersionUID = 5887584761454864149L;
	public K key;
	public V value;

	public ComparableEntry(final K key, final V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

//	public K setKey(final K key) {
//		final K oldValue = this.key;
//		this.key = key;
//		return oldValue;
//	}

	public V setValue(final V value) {
		final V oldValue = this.value;
		this.value = value;
		return oldValue;
	}

	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof ComparableEntry) {
			final ComparableEntry<?, ?> e = (ComparableEntry<?, ?>) o;
			return key == null ? false : key.equals(e.key);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return (key == null ? 0 : key.hashCode())
				^ (value == null ? 0 : value.hashCode());
	}

	public String toString() {
		return key + "=" + value;
	}

	@Override
	public int compareTo(final ComparableEntry<K, V> o) {
		return this.key.compareTo(o.key);
	}

}

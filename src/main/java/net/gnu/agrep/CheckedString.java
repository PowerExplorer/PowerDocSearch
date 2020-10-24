package net.gnu.agrep;

public class CheckedString implements Comparable<CheckedString> {

	boolean checked;
    String string;

    public CheckedString(String _s) {
        this(true, _s);
    }
    public CheckedString(boolean _c, String _s) {
        checked = _c;
        string = _s;
    }
    public String toString() {
        return (checked ?"true": "false") + "|" + string;
    }

	@Override
	public int compareTo(CheckedString p1) {
		return string.compareToIgnoreCase(p1.string);
	}

    @Override
	public boolean equals(Object o) {
		if (o instanceof CheckedString) {
			return string.equalsIgnoreCase(((CheckedString)o).string);
		} else {
			return false;
		}
	}

}

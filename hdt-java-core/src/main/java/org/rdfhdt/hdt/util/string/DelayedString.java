package org.rdfhdt.hdt.util.string;

public final class DelayedString implements ComparableCharSequence {
    CharSequence str;

    public DelayedString(final CharSequence str) {
	this.str = str;
    }

    private void ensure() {
	if(!(this.str instanceof String)) {
	    this.str = this.str.toString();
	}
    }

    @Override
    public int length() {
	this.ensure();
	return this.str.length();
    }

    @Override
    public char charAt(final int index) {
	this.ensure();
	return this.str.charAt(index);
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
	this.ensure();
	return this.subSequence(start, end);
    }

    @Override
    public String toString() {
	this.ensure();
	return this.str.toString();
    }

    public CharSequence getInternal() {
	return this.str;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final ComparableCharSequence other) {
	return this.toString().compareTo(other.toString());
    }

}

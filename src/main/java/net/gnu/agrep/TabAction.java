package net.gnu.agrep;

import android.content.Intent;

public interface TabAction {
	public void closeCurTab();
	public void closeOtherTabs();
	public void addTab(final Intent intent);
	public int getSize();
}

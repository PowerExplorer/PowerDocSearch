package net.gnu.common;

import android.app.*;
import android.support.v4.app.*;

/**
 * This interface must be implemented by activities that contain this
 * fragment to allow an interaction in this fragment to be communicated
 * to the activity and potentially other fragments contained in that
 * activity.
 * <p/>
 * See the Android Training lesson <a href=
 * "http://developer.android.com/training/basics/fragments/communicating.html"
 * >Communicating with Other Fragments</a> for more information.
 */
public interface OnFragmentInteractionListener {
	/**
	 * Triggered when the user successfully selected their destination directory.
	 */
	public void onOk(FragmentActivity path);

	/**
	 * Advices the activity to remove the current fragment.
	 */
	//public void onCancelChooser(FragmentActivity path);
	public void onButton1(FragmentActivity path);
	//public void onButton2(FragmentActivity path);
}

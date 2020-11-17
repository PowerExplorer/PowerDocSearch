package net.gnu.common;

import android.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class OkBtnListener implements OnClickListener {
	private FragmentActivity frag;
	private OnFragmentInteractionListener mListener;

	public OkBtnListener(FragmentActivity frag, OnFragmentInteractionListener mListener) {
		this.frag = frag;
		this.mListener = mListener;
	}

	@Override
	public void onClick(View v) {
		mListener.onOk(frag);
	}
}


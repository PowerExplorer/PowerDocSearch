package net.gnu.common;

import android.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.support.v4.app.*;

public class Button1Listener implements OnClickListener {
	private FragmentActivity frag;
	private OnFragmentInteractionListener mListener;

	public Button1Listener(FragmentActivity frag, OnFragmentInteractionListener mListener) {
		this.frag = frag;
		this.mListener = mListener;
	}

	@Override
	public void onClick(View v) {
		mListener.onButton1(frag);
	}
}

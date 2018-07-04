package finalproject.comp3617.billionaire;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class InvitedDialog extends DialogFragment {
    private EditText etHost;
    private EditText etInviteCode;

    public interface InviteInputListener {
        void onDialogPositiveClick(String hname, String icode);
//        public void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    InviteInputListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (InviteInputListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement InviteInputListener");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_invited_dialog, null);
        etHost = view.findViewById(R.id.etHost);
        etInviteCode = view.findViewById(R.id.etInvited);
        builder.setView(view)
                .setPositiveButton("Go", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null) {
                            mListener.onDialogPositiveClick(etHost.getText().toString(), etInviteCode.getText().toString());
                            Log.d("test", etHost.getText().toString());
                        }

                    }
                })
                .setNegativeButton("Cancel", null);
        return builder.create();
    }

}

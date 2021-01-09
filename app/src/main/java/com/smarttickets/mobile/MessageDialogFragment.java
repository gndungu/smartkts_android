package com.smarttickets.mobile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageDialogFragment extends DialogFragment {
    public interface MessageDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
    }

    private String mTitle;
    private String mMessage;
    private String[] params;
    private MessageDialogListener mListener;

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setRetainInstance(true);
    }

    public static MessageDialogFragment newInstance(String title, String message, String[] params, MessageDialogListener listener) {
        MessageDialogFragment fragment = new MessageDialogFragment();
        fragment.mTitle = title;
        fragment.mMessage = message;
        fragment.params = params;
        fragment.mListener = listener;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View view = factory.inflate(R.layout.custom, null);

        builder.setTitle("TICKET VALIDATION");

        ImageView image = (ImageView) view.findViewById(R.id.image);
        TextView cnt = (TextView) view.findViewById(R.id.counttext);
        TextView scanCode = (TextView) view.findViewById(R.id.txtScanCode);
        TextView category = (TextView) view.findViewById(R.id.txtCategory);
        TextView admits = (TextView) view.findViewById(R.id.txtAdmits);
        TextView reason = (TextView) view.findViewById(R.id.txtReason);
        TextView ticktNumber = (TextView) view.findViewById(R.id.txtTicketNo);
        TextView owner = (TextView) view.findViewById(R.id.txtOwner);
        TextView request = (TextView) view.findViewById(R.id.txtRequest);

        System.out.println("MessageDialog "+ params);

        if(params[0] == "1"){
            image.setImageResource(R.drawable.validstamp_p);
            cnt.setText("COUNT: "+ params[1]);
            scanCode.setText("Ticket Code: " + params[2]);
            category.setText("Category: " + params[3]);
            admits.setText("Admits: " + params[4]);
            //reason.setText("Reason: " + params[6]);
            ticktNumber.setText("Ticket Number: " + params[6]);
            if (params[7] != null) owner.setText("Note: " + params[7]);
            if (params[8] != null) request.setText("Requests: " + params[8]);
            System.out.println("COUNT "+params[1]);
        }
        if(params[0] == "0") {
            image.setImageResource(R.drawable.invalidstamp_p);
            try {
                if (Integer.parseInt(params[1]) > Integer.parseInt(params[4])) {
                    image.setImageResource(R.drawable.warning);
                }
            }catch (Exception e){
                image.setImageResource(R.drawable.warning);
            }
            cnt.setText("COUNT: " + params[1]);
            try {
                if (params[2] != null) scanCode.setText("Ticket Code: " + params[2]);
                if (params[3] != null) category.setText("Category: " + params[3]);
                admits.setText("Admits: " + params[4]);
                reason.setText("Reason: " + params[5]);
                ticktNumber.setText("Ticket Number: " + params[6]);
                if (params[7] != null) owner.setText("Note: " + params[7]);
                if (params[8] != null) request.setText("Requests: " + params[8]);
                System.out.println("COUNT "+params[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(mListener != null) {
                    mListener.onDialogPositiveClick(MessageDialogFragment.this);
                }
            }
        });

        return builder.create();
    }
}

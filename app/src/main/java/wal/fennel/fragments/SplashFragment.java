package wal.fennel.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import wal.fennel.R;

/**
 * Created by irfanayaz on 10/24/16.
 */
public class SplashFragment extends Fragment {
    CountDownListener callback;

    public View onCreateView(LayoutInflater inflater, ViewGroup vg,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.splash_fragment, vg, false);
        return root;

    }

    public interface CountDownListener {
        void callLoginFragment();
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {


        new CountDownTimer(1000, 1000) {

            public void onTick(long millisUntilFinished) {
                //    mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
//
                if (callback != null)
                    callback.callLoginFragment();
            }

        }.start();
    }

    @Override
    public void onResume() {
        super.onResume();
       /* new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
  //                          Toast.makeText(getActivity(),"Hello ", Toast.LENGTH_SHORT).show();
                            ((WelcomeActivity)getActivity()).fragmentTaskCompleted();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    */
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
//        Activity activity = (Activity) context;
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
//            Toast.makeText(getActivity(),"Hello ", Toast.LENGTH_SHORT).show();
            callback = (CountDownListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement TimerListener");
        }
    }
}

package com.microchip.intelliwand;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;


public class ViewFragment extends Fragment {
    static TextView spmintv,spmaxtv,csatv,pmpstatustv,gvcp;
    public  static boolean chk;
    public  static ProgressBar cpb;
    public static int temp;
    Timer t;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //startloopupdate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_view, container, false);

//        cpb=(ProgressBar) rootView.findViewById(R.id.circularProgressbar);

        spmintv = (TextView) rootView.findViewById(R.id.spminval);
//        gvcp = (TextView) rootView.findViewById(R.id.tvcp);
        spmaxtv = (TextView) rootView.findViewById(R.id.spmaxval);
        csatv = (TextView) rootView.findViewById(R.id.csaval);
        pmpstatustv = (TextView) rootView.findViewById(R.id.pmpstatus);


//        s=new Timer();
//        s.schedule(settimer, 1);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        t=new Timer();
        t.scheduleAtFixedRate(timer,0,1);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        timer.cancel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }




    TimerTask timer = new TimerTask() {
        @Override
        public void run() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Thread mythread =new Thread(runnable);
//                    mythread.start();

                    if (chk) {

                        try {
                            temp=Integer.parseInt(DeviceControlActivity.spmin);
                            spmintv.setText(String.valueOf(temp));

                        } catch (Exception e) {
                            Log.d("TAG", "spmintv Exception :" + e);
                        }

                        try {
                            temp=Integer.parseInt(DeviceControlActivity.spmax);
                            spmaxtv.setText(String.valueOf(temp));

                        } catch (Exception e) {
                            Log.d("TAG", "spmaxtv Exception :" + e);
                        }


                        try {
                            temp=Integer.parseInt(DeviceControlActivity.cp);
                            gvcp.setText(String.valueOf(temp) + " psi");
                            temp= (int) (temp*0.75);
                            if (temp >225){
                                temp=225;
                            }
                            ObjectAnimator animation =ObjectAnimator.ofInt(cpb,"progress",cpb.getProgress(),temp);
                            animation.setDuration(2000);
                            animation.setInterpolator(new DecelerateInterpolator());
                            animation.start();
                            cpb.setProgress(temp);



                        } catch (Exception e) {
                            Log.d("TAG", "spmaxtv Exception :" + e);
                        }

                        try {
                            temp=Integer.parseInt(DeviceControlActivity.csa);
                            csatv.setText(String.valueOf(temp));

                        } catch (Exception e) {
                            Log.d("TAG", "spmaxtv Exception :" + e);
                        }


                        try {

                            if (Integer.parseInt(DeviceControlActivity.sw) == 0) {
                                pmpstatustv.setText("STOP");
                            }
                            if (Integer.parseInt(DeviceControlActivity.sw) == 1) {
                                pmpstatustv.setText("START");
                            }

                        } catch (Exception e) {
                            Log.d("TAG", "spmaxtv Exception :" + e);
                        }


                    }


                }
            });
        }
    };


//    TimerTask settimer=new TimerTask() {
//        @Override
//        public void run() {
//            t=new Timer();
//            t.scheduleAtFixedRate(timer,0,1);
//        }
//    };

//    private static class updatevalues extends AsyncTask<Void,Double,Void>{
//
//        @Override
//        protected Void doInBackground(Void... params) {
//
//            while (DeviceControlActivity.startflag){
//
//            }
//
//            return null;
//        }
//    }
}


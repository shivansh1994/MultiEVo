package com.microchip.intelliwand;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SettingFragment extends Fragment {

    public  static TextView scp;
    public  static EditText spminet,spmaxet,p1ret,p2ret;
    public  static RadioButton current_rb,voltage_rb;
    public  static RadioGroup rbg;
    public  static Button updatebtn;

    public  String dat= "";

    public  static String sensonrtype_s ="1";



    public  void setdata() {
        int temp;
        try {
            temp=Integer.parseInt(DeviceControlActivity.spmin);
            spminet.setText(String.valueOf(temp));

            temp=Integer.parseInt(DeviceControlActivity.spmax);
            spmaxet.setText(String.valueOf(temp));


            temp=Integer.parseInt(DeviceControlActivity.p1r);
            p1ret.setText(String.valueOf(temp));


            temp=Integer.parseInt(DeviceControlActivity.p2r);
            p2ret.setText(String.valueOf(temp));

            temp=Integer.parseInt(DeviceControlActivity.cp);
            scp.setText(String.valueOf(temp));

            sensonrtype_s = DeviceControlActivity.sen;
            if (Integer.parseInt(sensonrtype_s) == 0) {
                voltage_rb.setChecked(true);
            } else {
                voltage_rb.setChecked(false);
            }
            if (Integer.parseInt(sensonrtype_s) == 1) {
                current_rb.setChecked(true);
            } else {
                current_rb.setChecked(false);
            }
        }catch (Exception e){
            Log.d("Exception",e.toString());

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);
        updatebtn=(Button)rootView.findViewById(R.id.updtbtn);
        scp=(TextView)rootView.findViewById(R.id.scptv);
        spminet=(EditText)rootView.findViewById(R.id.spminet);
        spmaxet=(EditText)rootView.findViewById(R.id.spmaxet);
        p1ret=(EditText)rootView.findViewById(R.id.p1ret);
        p2ret=(EditText)rootView.findViewById(R.id.p2ret);
        current_rb=(RadioButton)rootView.findViewById(R.id.current_rb);
        voltage_rb=(RadioButton)rootView.findViewById(R.id.voltage_rb);
        rbg=(RadioGroup)rootView.findViewById(R.id.rbg);

        /* Set Text Watcher listener */
        spminet.addTextChangedListener(valueWatcher);
        spmaxet.addTextChangedListener(valueWatcher);
        p1ret.addTextChangedListener(valueWatcher);
        p2ret.addTextChangedListener(valueWatcher);

        rbg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // find which radio button is selected
                if (checkedId == R.id.current_rb) {
                    current_rb.setChecked(true);
                    sensonrtype_s = "1";
                }
                if (checkedId == R.id.voltage_rb) {
                    voltage_rb.setChecked(true);
                    sensonrtype_s = "0";
                }
            }
        });

        updatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              Activity activity = getActivity();
                if (activity instanceof DeviceControlActivity) {
                    DeviceControlActivity dca = (DeviceControlActivity) activity;
                    int t;
                    StringBuilder source=new StringBuilder();

                    // For SPMIN
                    dat = "<@$SPMIN#";
                    source.append(dat);
                    t=Integer.parseInt(spminet.getText().toString());
                    dat = String.format("%03d",t);
                    source.append(dat);
                    dca.write(source.toString());
                    source.setLength(0);
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        Log.d("TAG",e.toString());
                    }

                    // For SPMAX
                    dat = "$SPMAX#";
                    source.append(dat);
                    t=Integer.parseInt(spmaxet.getText().toString());
                    dat = String.format("%03d",t);
                    source.append(dat);
                    dca.write(source.toString());
                    source.setLength(0);
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        Log.d("TAG",e.toString());
                    }

                    // For CP , SW & SEN
                    dat="$CP#";
                    source.append(dat);
                    t=Integer.parseInt(scp.getText().toString());
                    dat = String.format("%03d",t);
                    source.append(dat);

                     dat="$SW#";
                    source.append(dat);

                    dat=DeviceControlActivity.sw;
                    source.append(dat);
                     dat="$SEN#";
                    source.append(dat);
                     dat=sensonrtype_s;
                    source.append(dat);
                    dca.write(source.toString());
                    source.setLength(0);
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        Log.d("TAG",e.toString());
                    }

                    // For P1R & P2R
                    dat="$P1R#";
                    source.append(dat);
                    t=Integer.parseInt(p1ret.getText().toString());
                    dat = String.format("%03d", t);
                    source.append(dat);

                     dat="$P2R#";
                    source.append(dat);
                    t=Integer.parseInt(p2ret.getText().toString());
                    dat = String.format("%03d", t);
                    source.append(dat);
                    dca.write(source.toString());
                    source.setLength(0);
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        Log.d("TAG",e.toString());
                    }

                    // For CSA & End of Statement
                    dat="$CSA#";
                    source.append(dat);
                    t=Integer.parseInt(DeviceControlActivity.csa);
                    dat = String.format("%05d", t);
                    source.append(dat);

                    dat="$@!";
                    source.append(dat);
                    dca.write(source.toString());

                }

            }
        });


        return rootView;
    }

    private final TextWatcher valueWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        public void afterTextChanged(Editable s) {
            if (s.length()>0) {
                if (Integer.parseInt(s.toString()) > 300) {
//                textView.setVisibility(View.GONE);
                    s.replace(0, s.length(), "300");
                    Toast.makeText(getActivity(),"Enter Value Less Than Equalto 300",Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getActivity(),"Enter Value",Toast.LENGTH_LONG).show();

            }

        }
    };

}
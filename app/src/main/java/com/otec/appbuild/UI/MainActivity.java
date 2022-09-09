package com.otec.appbuild.UI;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.otec.appbuild.R;
import com.otec.appbuild.utils.util;


public class MainActivity extends AppCompatActivity{


    private Button nfc, qscan, rfid;

    private String TAG = "MainActivity";
    private NfcAdapter mNfcAdapter;

    protected void onPause() {
        super.onPause();
        if(mNfcAdapter!= null)
            mNfcAdapter.disableReaderMode(this);
        Log.d(TAG, "onPause: ");
    }
    


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfc = (Button) findViewById(R.id.nfc);
        qscan = (Button) findViewById(R.id.qscan);
        rfid = (Button) findViewById(R.id.rfid);




        qscan.setOnClickListener(e -> {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            startActivity(intent);
        });


        nfc.setOnClickListener(e -> {
            Log.d(TAG, "onCreate: 3 ");
            new util().openFragment(new Nfc_scan(),"nfc_scan",0,this);
        });


        rfid.setOnClickListener(e->{
            new util().openFragment(new Rfid_scan(),"Rfid_scan",0,this);
        });

    }


}



//            Ndef ndef = Ndef.get(tag);
//            if (ndef == null) {
//                Log.d(TAG, "onCreate: ");
//            }
//
//            NdefMessage ndefMessage = ndef.getCachedNdefMessage();
//
//            if (ndefMessage == null) {
//                Log.d(TAG, "onCreate: The tag is empty !");
//
//            }
//
//            NdefRecord[] records = ndefMessage.getRecords();
//            for (NdefRecord ndefRecord : records) {
//                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
//                    try {
//                        Log.d(TAG, "onCreate: "+ndefRecord);
//                    } catch (Exception es) {
//                        Log.e(TAG, "Unsupported Encoding", es);
//                    }
//                }
//            }
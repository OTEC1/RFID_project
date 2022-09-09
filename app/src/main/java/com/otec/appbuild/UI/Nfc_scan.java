package com.otec.appbuild.UI;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.otec.appbuild.R;
import com.otec.appbuild.utils.util;

import java.util.Arrays;


public class Nfc_scan extends Fragment implements NfcAdapter.ReaderCallback{


    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private TextView output;


    private IntentFilter[] intentFiltersArray = null;
    private NfcManager nfcManager = null;
    private String[][] techListsArray = null;
    private String TAG = "Nfc_scan";
    private  String [] check;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_nfc_scan, container, false);
        output = (TextView)  view.findViewById(R.id.output);


        NfcManager manager = (NfcManager) getContext().getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        PackageManager pm = getActivity().getPackageManager();

        if (adapter == null)
            check = new String[]{"Query 1 is null"};
        if(!pm.hasSystemFeature(PackageManager.FEATURE_NFC))
            check = new String[]{"Query 3 is null"};
        if(!pm.hasSystemFeature(PackageManager.FEATURE_NFC_BEAM))
            check = new String[]{"No nfc supported"};

        Check();
        PendingIntent();
        return view;
    }


    //---------------------------NFC--------------------------------------//
    private void Check() {
        output.setText("Scanning...");
        nfcManager = (NfcManager) getActivity().getSystemService(Context.NFC_SERVICE);
        nfcAdapter = NfcAdapter.getDefaultAdapter(getContext());
        if (nfcAdapter == null) {
            for (int y=0; y < check.length; y++)
              new util().message(check[y], getContext());
            return;
        }
        intentFiltersArray = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)};
        techListsArray = new String[][]{new String[]{NfcA.class.getName()}, new String[]{NfcB.class.getName()}, new String[]{IsoDep.class.getName()}};
    }


    private void  PendingIntent(){
        try {
            Intent intent = new Intent(getContext(), this.getClass());
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
            else
                pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, 0);

        }catch (Exception e){
            output.append("Error: "+e.getLocalizedMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled())
                showWirelessSettings();
            Bundle options = new Bundle();
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250);

            // Enable ReaderMode for all types of card and disable platform sounds
            nfcAdapter.enableReaderMode(getActivity(),  this,
                    NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B |
                            NfcAdapter.FLAG_READER_NFC_F | NfcAdapter.FLAG_READER_NFC_V |
                            NfcAdapter.FLAG_READER_NFC_BARCODE | NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS, options);
            Log.d(TAG, "onResume: ");
        }
    }




    private void showWirelessSettings() {
        new util().message( "You need to enable NFC",getContext());
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }




    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(getActivity());
        }
    }




    @Override
    public void onTagDiscovered(Tag tag) {
        Ndef ndef = Ndef.get(tag);

        if (ndef == null) {
            new util().message( "Ndef payload none", getContext());
            return;
        }
        NdefMessage ndefMessage = ndef.getCachedNdefMessage();
        if (ndefMessage == null) {
            new util().message("NdefMessage payload none", getContext());
            return;
        }

        NdefRecord[] records = ndefMessage.getRecords();
        for (NdefRecord ndefRecord : records) {
            if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                try {
                    getActivity().runOnUiThread(() -> { output.setText(""); });
                    byte[] payload = ndefRecord.getPayload();
                    String text = new String(payload);
                    getActivity().runOnUiThread(() -> {
                        output.append("Type: "+ndef.getType()+"\n");
                        output.append("Storage capacity: "+ndef.getMaxSize() +"\n");
                        output.append(ndef.isWritable() ? "I/O: "+ "True" : "False");
                        output.append("\n"+"Language: "+text.substring(0, 3));
                        output.append("\n"+"Payload: "+text.substring(3));
                    });
                    ndef.close();
                } catch (Exception e) {
                    Log.e(TAG, "Unsupported Encoding", e);
                }
            }
        }
    }





}
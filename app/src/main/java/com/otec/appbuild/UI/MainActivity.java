package com.otec.appbuild.UI;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.otec.appbuild.R;
import com.otec.appbuild.utils.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.concurrent.Executors;

import com.zebra.rfid.api3.*;
import javax.comm.*;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {


    private Button nfc, qscan, rfid;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private TextView output;



    private IntentFilter[] intentFiltersArray = null;
    private NfcManager nfcManager = null;
    private String[][] techListsArray = null;
    private ArrayList<ReaderDevice> availableRfidReaderList;
    private static RFIDReader reader;
    private EventHandler eventHandler;
    private Readers readers;
    private static ReaderDevice readerDevice;

    private String TAG = "MainActivity";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfc = (Button) findViewById(R.id.nfc);
        qscan = (Button) findViewById(R.id.qscan);
        rfid = (Button) findViewById(R.id.rfid);
        output = (TextView) findViewById(R.id.output);


        Check();
        PendingIntent();


        qscan.setOnClickListener(e -> {
        });


        nfc.setOnClickListener(e -> {
        });


        rfid.setOnClickListener(e->{
            Rfid_Antenna();
        });



    }



    //---------------------------NFC--------------------------------------//
    private void Check() {
        output.setText("Scanning...");
        nfcManager = (NfcManager) getSystemService(Context.NFC_SERVICE);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            new util().message("NFC not supported", getApplicationContext());
            return;
        }
        intentFiltersArray = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)};
        techListsArray = new String[][]{new String[]{NfcA.class.getName()}, new String[]{NfcB.class.getName()}, new String[]{IsoDep.class.getName()}};
    }


    private void  PendingIntent(){
        try {
                Intent intent = new Intent(this, this.getClass());
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                    pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
                 else
                    pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        }catch (Exception e){
            output.append("Error: "+e.getLocalizedMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled())
                showWirelessSettings();
            Bundle options = new Bundle();
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250);

            // Enable ReaderMode for all types of card and disable platform sounds
            nfcAdapter.enableReaderMode(this,  this,
                    NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B |
                            NfcAdapter.FLAG_READER_NFC_F | NfcAdapter.FLAG_READER_NFC_V |
                            NfcAdapter.FLAG_READER_NFC_BARCODE | NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS, options);
            Log.d(TAG, "onResume: ");
        }
    }




    private void showWirelessSettings() {
       new util().message( "You need to enable NFC",this);
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }




    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }
    }




    @Override
    public void onTagDiscovered(Tag tag) {
        Ndef ndef = Ndef.get(tag);

        if (ndef == null) {
            new util().message( "Ndef payload none", getApplicationContext());
            return;
        }
        NdefMessage ndefMessage = ndef.getCachedNdefMessage();
        if (ndefMessage == null) {
            new util().message("NdefMessage payload none", getApplicationContext());
            return;
        }

        NdefRecord[] records = ndefMessage.getRecords();
        for (NdefRecord ndefRecord : records) {
            if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                try {
                    runOnUiThread(() -> { output.setText(""); });
                    byte[] payload = ndefRecord.getPayload();
                    String text = new String(payload);
                    runOnUiThread(() -> {
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








//         @Override
//        protected void onNewIntent(Intent intent) {
//                super.onNewIntent(intent);
//                Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//                MifareClassic tag = MifareClassic.get(tagFromIntent);
//                try {
//                    //Variables
//                    int sectorCount = tag.getSectorCount();
//                    int tagSize = tag.getSize();
//                    boolean auth;
//                    //Keys
//                    byte[] defaultKeys;
//                    defaultKeys = MifareClassic.KEY_DEFAULT;
//                    //Connecting to tag
//                    tag.connect();
//                    //auth = true
//                    auth = tag.authenticateSectorWithKeyA(2, defaultKeys);
//                    byte[] data = tag.readBlock(2);
//                    Log.i("OnNewIntent", "Data in sector 2: " + Arrays.toString(data));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//    }







    private String StartProcess(Tag tag) {
        StringBuilder sb = new StringBuilder();
        byte[] id = tag.getId();
        sb.append("ID in hex: ").append(toHex(id)).append('\n');
        sb.append("ID in reversed hex: ").append(toReversedHex(id)).append('\n');
        sb.append("ID in dec: ").append(toDec(id)).append('\n');
        sb.append("ID in reversed dec ").append(toReversedDec(id)).append('\n');

        String prefix = "android.nfc.tech.";
        sb.append("Manufactured name: ");
        for (String tech : tag.getTechList()) {
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }

        sb.delete(sb.length() - 2, sb.length());

        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                String type = "Unknown";

                try {
                    MifareClassic mifareTag = MifareClassic.get(tag);

                    switch (mifareTag.getType()) {
                        case MifareClassic.TYPE_CLASSIC:
                            type = "Classic";
                            break;
                        case MifareClassic.TYPE_PLUS:
                            type = "Plus";
                            break;
                        case MifareClassic.TYPE_PRO:
                            type = "Pro";
                            break;
                    }
                    sb.append("Mifare Classic type: ");
                    sb.append(type);
                    sb.append('\n');

                    sb.append("Mifare size: ");
                    sb.append(mifareTag.getSize() + " bytes");
                    sb.append('\n');

                    sb.append("Mifare sectors: ");
                    sb.append(mifareTag.getSectorCount());
                    sb.append('\n');

                    sb.append("Mifare blocks: ");
                    sb.append(mifareTag.getBlockCount());
                } catch (Exception e) {
                    sb.append("Mifare classic error: " + e.getMessage());
                }
            }

            if (tech.equals(MifareUltralight.class.getName())) {
                sb.append('\n');
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        type = "Ultralight";
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        type = "Ultralight C";
                        break;
                }
                sb.append("Mifare Ultralight type: ");
                sb.append(type);
            }
        }
        return sb.toString();
    }





    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String toReversedHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
        }
        return sb.toString();
    }

    private long toDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    private long toReversedDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

//    public static String getPublicIPAddress(Context context) {
//        //final NetworkInfo info = NetworkUtils.getNetworkInfo(context);
//
//        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        final NetworkInfo info = cm.getActiveNetworkInfo();
//
//        RunnableFuture<String> futureRun = new FutureTask<>(() -> {
//            if ((info != null && info.isAvailable()) && (info.isConnected())) {
//                StringBuilder response = new StringBuilder();
//
//                try {
//                    HttpURLConnection urlConnection = (HttpURLConnection) (
//                            new URL("http://checkip.amazonaws.com/").openConnection());
//                    urlConnection.setRequestProperty("User-Agent", "Android-device");
//                    //urlConnection.setRequestProperty("Connection", "close");
//                    urlConnection.setReadTimeout(15000);
//                    urlConnection.setConnectTimeout(15000);
//                    urlConnection.setRequestMethod("GET");
//                    urlConnection.setRequestProperty("Content-type", "application/json");
//                    urlConnection.connect();
//
//                    int responseCode = urlConnection.getResponseCode();
//
//                    if (responseCode == HttpURLConnection.HTTP_OK) {
//
//                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
//                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//
//                        String line;
//                        while ((line = reader.readLine()) != null) {
//                            response.append(line);
//                        }
//
//                    }
//                    urlConnection.disconnect();
//                    return response.toString();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            } else {
//                //Log.w(TAG, "No network available INTERNET OFF!");
//                return null;
//            }
//            return null;
//        });
//
//        new Thread(futureRun).start();
//
//        try {
//            return futureRun.get();
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//            return null;
//        }
//
//    }


    //-----------------------------END OF NFC -------------------------------------//














    //-----------------------------RFID -------------------------------------//
    private void Rfid_Antenna() {
        if (readers == null)
            readers = new Readers(this, ENUM_TRANSPORT.SERVICE_SERIAL);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                if (readers != null) {
                    if (readers.GetAvailableRFIDReaderList() != null) {
                        availableRfidReaderList = readers.GetAvailableRFIDReaderList();
                        if (availableRfidReaderList.size() > 0) {
                            readerDevice = availableRfidReaderList.get(0);
                            reader = readerDevice.getRFIDReader();
                            Log.d(TAG, "doInBackground: here 1 ");
                            if (!reader.isConnected()) {
                                reader.connect();
                                runOnUiThread(() -> {
                                    output.append("Host " + reader.getHostName());
                                    output.append("Password " + reader.getPassword());
                                    output.append("Transport layer " + reader.getTransport());
                                    output.append("Port " + reader.getPort());
                                });
                                configreader();
                                new util().message("doInBackground: here 2 ", getApplicationContext());

                            }
                        } else
                            runOnUiThread(() -> {
                                new util().message("Error 1 "+availableRfidReaderList.size(), getApplicationContext());
                            });
                    } else runOnUiThread(() -> {
                        new util().message("Error 2", getApplicationContext());
                    });
                } else runOnUiThread(() -> {
                    new util().message("Error 3", getApplicationContext());
                });

            } catch (InvalidUsageException e) {
                e.printStackTrace();
                new util().message(e.getLocalizedMessage() + " |a " + e.getVendorMessage(), getApplicationContext());
            } catch (OperationFailureException e) {
                e.printStackTrace();
                new util().message(e.getLocalizedMessage() + " |b " + e.getVendorMessage(), getApplicationContext());
            }
        });
    }




    private void configreader() {
        new util().message("Called  configreader", getApplicationContext());
        if (reader.isConnected()) {
            TriggerInfo info = new TriggerInfo();
            info.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
            info.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
            Log.d(TAG, "doInBackground: here 4 ");
            try {
                if (eventHandler == null)
                    eventHandler = new EventHandler();
                else
                    new util().message("Error 5", getApplicationContext());
                reader.Events.addEventsListener(eventHandler);
                reader.Events.setHandheldEvent(true);
                reader.Events.setTagReadEvent(true);
                reader.Events.setAttachTagDataWithReadEvent(false);
                reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true);
                reader.Config.setStartTrigger(info.StartTrigger);
                reader.Config.setStopTrigger(info.StopTrigger);
                new util().message("doInBackground: here 5 ", getApplicationContext());
            } catch (Exception e) {
                new util().message(e.getLocalizedMessage() + " | " + e.getCause(), getApplicationContext());
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            if (reader != null) {
                reader.Events.removeEventsListener(eventHandler);
                reader.disconnect();
                new util().message("Disconnecting reader", getApplicationContext());
                reader = null;
                readers.Dispose();
                readers = null;
            }
        } catch (InvalidUsageException e) {
            e.printStackTrace();
            new util().message(e.getLocalizedMessage() + " | " + e.getCause(), getApplicationContext());
        } catch (OperationFailureException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            new util().message(e.getLocalizedMessage() + " | " + e.getCause(), getApplicationContext());
        }
    }



    public class EventHandler implements RfidEventsListener {

        @Override
        public void eventReadNotify(RfidReadEvents rfidReadEvents) {
            TagData[] mytage = reader.Actions.getReadTags(100);
            if (mytage != null) {
                for (int i = 0; i < mytage.length; i++) {
                    output.append("TAG ID " + mytage[i].getTagID());
                    if (mytage[i].getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ && mytage[i].getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS)
                        if (mytage[i].getMemoryBankData().length() > 0)
                            output.append("Memory bank data " + mytage[i].getMemoryBankData());

                }
            } else
                new util().message("Error TAG DATA is null", getApplicationContext());
        }

        @Override
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            output.append("Status Notification: " + rfidStatusEvents.StatusEventData.getStatusEventType());

            if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            reader.Actions.Inventory.perform();
                        } catch (InvalidUsageException e) {
                            e.printStackTrace();
                            new util().message(e.getLocalizedMessage() + " | " + e.getCause(), getApplicationContext());
                        } catch (OperationFailureException e) {
                            e.printStackTrace();
                            new util().message(e.getLocalizedMessage() + " | " + e.getCause(), getApplicationContext());
                        }
                    });
                }
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            reader.Actions.Inventory.stop();
                        } catch (InvalidUsageException e) {
                            e.printStackTrace();
                            new util().message(e.getLocalizedMessage() + " | " + e.getCause(), getApplicationContext());
                        } catch (OperationFailureException e) {
                            e.printStackTrace();
                            new util().message(e.getLocalizedMessage() + " | " + e.getCause(), getApplicationContext());
                        }
                    });
                }
            }
        }
    }


    //-----------------------------END OF RFID -------------------------------------//





}
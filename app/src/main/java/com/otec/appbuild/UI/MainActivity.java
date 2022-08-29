package com.otec.appbuild.UI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.otec.appbuild.R;
import com.otec.appbuild.utils.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.zebra.rfid.api3.*;

public class MainActivity extends Activity {


    private Button nfc, qscan, rfid;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private TextView output;
    private Tag tag;


    private ArrayList<ReaderDevice> availableRfidReaderList;
    private ReaderDevice readerDevice;
    private static RFIDReader reader;
    private EventHandeler eventHandler;
    private Readers readers;


    private String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfc = (Button) findViewById(R.id.nfc);
        qscan = (Button) findViewById(R.id.qscan);
        rfid = (Button) findViewById(R.id.rfid);
        output = (TextView) findViewById(R.id.output);


        qscan.setOnClickListener(e -> {

        });


        nfc.setOnClickListener(e -> {
            Check();
        });


        rfid.setOnClickListener(e -> {
            Rfid_Antenna();
        });

    }


    private void Rfid_Antenna() {
        if (readers == null)
            readers = new Readers(this, ENUM_TRANSPORT.SERVICE_SERIAL);
        Log.d(TAG, "doInBackground: here 0 ");

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
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
                                    output.append("Host " + reader.getHostName());
                                    output.append("Password " + reader.getPassword());
                                    output.append("Transport layer " + reader.getTransport());
                                    output.append("Port " + reader.getPort());
                                    configreader();
                                    Log.d(TAG, "doInBackground: here 2 ");
                                    return true;
                                }
                            }
                        }
                    }
                } catch (InvalidUsageException e) {
                    e.printStackTrace();
                    new util().message(e.getLocalizedMessage() + " |a " + e.getVendorMessage(), getApplicationContext());
                } catch (OperationFailureException e) {
                    e.printStackTrace();
                    new util().message(e.getLocalizedMessage() + " |b " + e.getVendorMessage(), getApplicationContext());
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean)
                    output.append("Reader connected ");
            }
        }.execute();
    }


    private void configreader() {
        Log.d(TAG, "doInBackground: here 3 ");
        if(reader.isConnected()){
            TriggerInfo info = new TriggerInfo();
            info.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
            info.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
            Log.d(TAG, "doInBackground: here 4 ");
            try{
                if(eventHandler == null)
                    eventHandler = new EventHandeler();
                reader.Events.addEventsListener(eventHandler);
                reader.Events.setHandheldEvent(true);
                reader.Events.setTagReadEvent(true);
                reader.Events.setAttachTagDataWithReadEvent(false);
                reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE,true);
                reader.Config.setStartTrigger(info.StartTrigger);
                reader.Config.setStopTrigger(info.StopTrigger);
                Log.d(TAG, "doInBackground: here 5 ");
            }catch (Exception e) {
                new util().message(e.getLocalizedMessage()+" | "+e.getCause(),getApplicationContext());
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
            new util().message(e.getLocalizedMessage()+" | "+e.getCause(),getApplicationContext());
        } catch (OperationFailureException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            new util().message(e.getLocalizedMessage()+" | "+e.getCause(),getApplicationContext());
        }
    }

    public  class EventHandeler  implements  RfidEventsListener{

        @Override
        public void eventReadNotify(RfidReadEvents rfidReadEvents) {
                    TagData[] mytage = reader.Actions.getReadTags(100);
                        if(mytage != null){
                            for(int i = 0; i < mytage.length; i++) {
                                output.append("TAG ID " + mytage[i].getTagID());
                                if (mytage[i].getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ && mytage[i].getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS)
                                    if (mytage[i].getMemoryBankData().length() > 0)
                                        output.append("Memory bank data "+mytage[i].getMemoryBankData());

                            }
                        }  else
                                new util().message("Error TAG DATA is null", getApplicationContext());
        }

        @Override
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            output.append( "Status Notification: " + rfidStatusEvents.StatusEventData.getStatusEventType());

            if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                    new AsyncTask<Void, Void, Boolean>() {
                        @Override
                        protected  Boolean doInBackground(Void... voids) {
                            try {
                                reader.Actions.Inventory.perform();
                            } catch (InvalidUsageException e) {
                                e.printStackTrace();
                                new util().message(e.getLocalizedMessage()+" | "+e.getCause(),getApplicationContext());
                            } catch (OperationFailureException e) {
                                e.printStackTrace();
                                new util().message(e.getLocalizedMessage()+" | "+e.getCause(),getApplicationContext());
                            }
                            return null;
                        }
                    }.execute();
                }
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                    new AsyncTask<Void, Void, Boolean>() {
                        @Override
                        protected Boolean doInBackground(Void... voids) {
                            try {
                                reader.Actions.Inventory.stop();
                            } catch (InvalidUsageException e) {
                                e.printStackTrace();
                                new util().message(e.getLocalizedMessage()+" | "+e.getCause(),getApplicationContext());
                            } catch (OperationFailureException e) {
                                e.printStackTrace();
                                new util().message(e.getLocalizedMessage()+" | "+e.getCause(),getApplicationContext());
                            }
                            return null;
                        }
                    }.execute();
                }
            }
        }
    }




    private void Check() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null)
            new util().message("NFC not supported", getApplicationContext());
        else
            StartProcess();
    }


    private void StartProcess() {
        output.setText("Place the card in the scan area");
        readfromIntent(getIntent());
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tag_detected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tag_detected.addCategory(Intent.CATEGORY_DEFAULT);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        readfromIntent(getIntent());

    }


    private void readfromIntent(Intent intent) {
        Object action = intent.getAction();
        Parcelable var10002;
        if (NfcAdapter.ACTION_TAG_DISCOVERED == action || NfcAdapter.ACTION_TECH_DISCOVERED == action || NfcAdapter.ACTION_NDEF_DISCOVERED == action) {
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            List<NdefMessage> msgs = new ArrayList<>();
            if (rawMsgs != null) {
                int i = 0;
                for (int var6 = rawMsgs.length; i < var6; ++i) {
                    var10002 = rawMsgs[i];
                    if (rawMsgs[i] == null) {
                        new util().message("null cannot be cast to non-null type android.nfc.NdefMessage", getApplicationContext());
                    }
                    msgs.add(i, (NdefMessage) var10002);
                }
                buildviews(msgs);
            }
        }
    }

    private void buildviews(List<NdefMessage> msgs) {
        if (msgs != null && msgs.size() != 0) {
            String text = "";
            NdefRecord var10000 = msgs.get(0).getRecords()[0];
            byte[] payload = var10000.getPayload();
            byte languageCodeLength = payload[0];
            byte var6 = (byte) 128;
            Charset textEncoding = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
                textEncoding = (byte) (languageCodeLength & var6) == 0 ? StandardCharsets.UTF_8 : StandardCharsets.UTF_16;

            var6 = payload[0];
            byte var7 = 51;
            languageCodeLength = (byte) (var6 & var7);

            try {
                int var10 = languageCodeLength + 1;
                int var8 = payload.length - languageCodeLength - 1;
                text = new String(payload, var10, var8, textEncoding);
            } catch (Exception var9) {
                new util().message("UnsupportedEncoding" + var9, getApplicationContext());
            }
            if (text.trim().length() > 0)
                output.setText((CharSequence) ("Message read from NFC Tag:\n " + text));
            else
                readfromIntent2(getIntent());
        } else
            new util().message("Error occurred  no data to read", getApplicationContext());
    }


    private void readfromIntent2(Intent intent) {
        Object action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED == action || NfcAdapter.ACTION_TECH_DISCOVERED == action || NfcAdapter.ACTION_NDEF_DISCOVERED == action) {
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            Ndef ndef = Ndef.get(tag);
            try {
                ndef.connect();
                output.append(ndef.getType());
                output.append(String.valueOf(ndef.getMaxSize()));
                output.append(ndef.isWritable() ? "True" : "False");
                Parcelable[] msg = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

                if (msg != null) {
                    NdefMessage[] nfedmsg = new NdefMessage[msg.length];
                    for (int i = 0; i < msg.length; i++)
                        nfedmsg[i] = (NdefMessage) msg[i];

                    NdefRecord record = nfedmsg[0].getRecords()[0];

                    byte[] payload = record.getPayload();
                    String text = new String(payload);
                    output.append(text);
                    ndef.close();
                }
            } catch (Exception e) {
                new util().message("Error occurred  = " + e, getApplicationContext());
            }
        } else
            new util().message("Could not discover nfc tag !", getApplicationContext());

    }


}
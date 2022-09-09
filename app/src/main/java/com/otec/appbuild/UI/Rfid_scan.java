package com.otec.appbuild.UI;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.otec.appbuild.R;
import java.util.*;


import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.otec.appbuild.utils.util;


public class Rfid_scan extends Fragment{

    public final String ACTION_USB_PERMISSION = "com.otec.appbuild.UI.USB_PERMISSION";
    private TextView output;
    private String TAG = "Rfid_scan";



    private UsbManager usbManager;
    private UsbDevice device;
    private UsbSerialDevice serialPort;
    private UsbDeviceConnection connection;



    UsbSerialInterface.UsbReadCallback  mCallback = bytes -> {
        String data;
       try{
           data = new String(bytes,"UTF-8");
           data.concat("\n");
           tvAppend(output, data);
           }catch (Exception e){
               new util().message(e.getLocalizedMessage(),getContext());
           }
    };


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: "+intent.getAction());
                if(intent.getAction().equals(ACTION_USB_PERMISSION)){
                    boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                    if(granted){
                        connection = usbManager.openDevice(device);
                        serialPort = UsbSerialDevice.createUsbSerialDevice(device,connection);
                        if(serialPort != null){
                            if(serialPort.open()){
                                serialPort.setBaudRate(9600);
                                serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                                serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                                serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                                serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                                serialPort.read(mCallback);
                                tvAppend(output,"Serial Post Started listening");
                            }
                            else
                                new util().message("Post not open", getContext());
                        } else
                               new util().message("Post is null", getContext());
                    }
                    else
                        new util().message("Permission not granted !", getContext());
                }
                else
                    if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED))
                            new util().message("Port is busy", getContext());
                else
                    if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED))
                        new util().message("Port is closed !", getContext());
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rfid_scan, container, false);
        output = (TextView) view.findViewById(R.id.output);
        new util().getDevice(getActivity());
        SetUpUsb();
        CheckUsb();
        return view;
    }

    private void SetUpUsb() {
        usbManager = (UsbManager) requireActivity().getSystemService(getActivity().USB_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        requireActivity().registerReceiver(broadcastReceiver, filter);
    }

    private void CheckUsb(){
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String,UsbDevice> entry  : usbDevices.entrySet()) {
                 device = entry.getValue();
                  int deviceVID = device.getVendorId();
                  Log.d(TAG, "CheckUsb: "+deviceVID);
                    if (deviceVID == 0x2341){
                        PendingIntent pi = PendingIntent.getBroadcast(getContext(), 0, new Intent(ACTION_USB_PERMISSION), 0);
                          usbManager.requestPermission(device, pi);
                        keep = false;
                    } else {
                        connection = null;
                        device = null;
                    }

                if (!keep)
                    break;
            }
        }
        else
            new util().message("OTG port is empty", getContext());

    }





    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;
        requireActivity().runOnUiThread(() -> ftv.append(ftext));
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






}
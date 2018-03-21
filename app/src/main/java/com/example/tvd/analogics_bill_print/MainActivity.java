package com.example.tvd.analogics_bill_print;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.analogics.thermalAPI.Bluetooth_Printer_3inch_prof_ThermalAPI;
import com.analogics.thermalprinter.AnalogicsThermalPrinter;
import com.example.tvd.analogics_bill_print.service.BluetoothService;
import com.example.tvd.analogics_bill_print.values.FunctionCalls;

import static android.text.Layout.Alignment.ALIGN_NORMAL;
import static com.example.tvd.analogics_bill_print.values.Constant.BLUETOOTH_RESULT;
import static com.example.tvd.analogics_bill_print.values.Constant.DISCONNECTED;
import static com.example.tvd.analogics_bill_print.values.Constant.MAIN_PRINTER_CONNECTED;
import static com.example.tvd.analogics_bill_print.values.Constant.MAIN_PRINTER_DISCONNECTED;
import static com.example.tvd.analogics_bill_print.values.Constant.MAIN_PRINTING_COMPLETED;
import static com.example.tvd.analogics_bill_print.values.Constant.RESULT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RequestPermissionCode = 1;
    Toolbar toolbar;
    BluetoothAdapter deviceadapter;
    AnalogicsThermalPrinter conn;
    ProgressDialog printing;
    Bluetooth_Printer_3inch_prof_ThermalAPI api;
    Button bt_print_text, bt_print_image, bt_print_report;
    boolean text_print = false, image_print = false;
    FunctionCalls fcall;
    private final Handler handler;

    {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MAIN_PRINTER_CONNECTED:
                        buttons_enable(true);
                        break;

                    case MAIN_PRINTER_DISCONNECTED:
                        buttons_enable(false);
                        break;

                    case MAIN_PRINTING_COMPLETED:
                        printing.dismiss();
                        break;
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        deviceadapter = BluetoothAdapter.getDefaultAdapter();
        deviceadapter.enable();

        bt_print_text = (Button) findViewById(R.id.bt_print_text);
        bt_print_text.setOnClickListener(this);
        api = new Bluetooth_Printer_3inch_prof_ThermalAPI();
        fcall = new FunctionCalls();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startBluetooth();
            }
        }, 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deviceadapter.disable();
        unregisterReceiver(mReceiver);
        handler.removeCallbacksAndMessages(null);
        stopBluetooth();
    }

    private void startBluetooth() {
        Intent intent = new Intent(MainActivity.this, BluetoothService.class);
        startService(intent);
        conn = BluetoothService.conn;
        registerReceiver(mReceiver, new IntentFilter(BLUETOOTH_RESULT));
    }

    private void stopBluetooth() {
        Intent intent = new Intent(MainActivity.this, BluetoothService.class);
        stopService(intent);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra("message");
            if (status.matches(RESULT)) {
                handler.sendEmptyMessage(MAIN_PRINTER_CONNECTED);
                Toast.makeText(MainActivity.this, "Bluetooth Printer Connected", Toast.LENGTH_SHORT).show();
            } else if (status.matches(DISCONNECTED)) {
                handler.sendEmptyMessage(MAIN_PRINTER_DISCONNECTED);
                Toast.makeText(MainActivity.this, "Bluetooth Printer Disconnected", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void buttons_enable(boolean enable) {
        bt_print_text.setEnabled(enable);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_print_text:
                text_print = true;
                image_print = false;
                printing = ProgressDialog.show(MainActivity.this, "Printing", "Printing Please wait to Complete");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        handler.sendEmptyMessage(MAIN_PRINTING_COMPLETED);
                    }
                }, 3000);
                printanalogics();
                break;


        }
    }

    private void printanalogics() {
        StringBuilder stringBuilder = new StringBuilder();
        analogics_header__double_print(fcall.aligncenter("HUBLI ELECTRICITY SUPPLY COMPANY LTD", 38), 6);
        analogicsprint(fcall.aligncenter("Belagavi", 30), 6);

        analogicsprint(fcall.space("Sub Division", 16) + ":" + " " + "540038", 6);
        analogicsprint(fcall.space("RRNO", 16) + ":" + " " + "BA12345", 6);
        analogics_double_print(fcall.space("Account ID", 16) + ":" + " " + "1234567890", 6);
        analogics_48_print(fcall.aligncenter("Name and Address", 48), 6);
        analogics_48_print("Mr. XYZ", 3);
        analogics_48_print("Peneya, Bangalore", 3);
        analogics_48_print("Sub register office", 6);

        analogicsprint(fcall.space("Tariff", 16) + ":" + " " + "5LT2A2", 6);
        analogicsprint(fcall.space("Sanct Load", 14) + ":" + "HP:" + fcall.alignright("0", 4) + " " + "KW:" + fcall.alignright("2", 4), 6);
        analogicsprint(fcall.space("Billing", 8) + ":" + "10/10/2017" + "-" + "10/11/2017", 6);
        analogicsprint(fcall.space("Reading Date", 16) + ":" + " " + "10/11/2017", 6);
        analogicsprint(fcall.space("BillNo", 7) + ":" + " " + "1234567890" + "-" + "10/11/2017", 6);
        analogicsprint(fcall.space("Meter SlNo.", 16) + ":" + " " + "5000101010", 6);
        analogicsprint(fcall.space("Pres Rdg", 16) + ":" + " " + "4000", 6);
        analogicsprint(fcall.space("Prev Rdg", 16) + ":" + " " + "3000", 6);
        analogicsprint(fcall.space("Constant", 16) + ":" + " " + "1", 6);
        analogicsprint(fcall.space("Consumption", 16) + ":" + " " + "250", 6);
        analogicsprint(fcall.space("Average", 16) + ":" + " " + "250", 6);

        stringBuilder.append("\n");

        analogicsprint(fcall.space("Rebates/TOD", 11) + "(-)" + ":" + " " + fcall.alignright("0.00", 14), 5);
        analogicsprint(fcall.space("PF Penalty", 14) + ":" + " " + fcall.alignright("0.00", 14), 5);
        analogicsprint(fcall.space("MD Penalty", 14) + ":" + " " + fcall.alignright("0.00", 14), 5);
        analogicsprint(fcall.space("Interest", 11) + "@1%" + ":" + " " + fcall.alignright("1.00", 14), 5);
        analogicsprint(fcall.space("Others", 14) + ":" + " " + fcall.alignright("0.05", 14), 5);
        analogicsprint(fcall.space("Tax", 11) + "@6%" + ":" + " " + fcall.alignright("0.01", 14), 5);
        analogicsprint(fcall.space("Cur Bill Amt", 14) + ":" + " " + fcall.alignright("100.00", 14), 6);
        analogicsprint(fcall.space("Arrears", 14) + ":" + " " + fcall.alignright("0.00", 14), 4);
        analogicsprint(fcall.space("Credits&Adj", 11) + "(-)" + ":" + " " + fcall.alignright("0.00", 14), 4);
        analogicsprint(fcall.space("GOK Subsidy", 11) + "(-)" + ":" + " " + fcall.alignright("0.05", 14), 0);
        analogics_double_print(fcall.space("Net Amt Due", 14) + ":" + " " + fcall.alignright("300.00", 14), 0);
        analogicsprint(fcall.space("Due Date", 14) + ":" + " " + fcall.alignright("25/11/2017", 14), 4);
        analogicsprint(fcall.space("Billed On", 12) + ":" + " " + fcall.alignright(fcall.currentDateandTime(), 16), 6);

        print_bar_code("1234567890" + "300");
        analogicsprint(fcall.space(" ", 3) + "12345678" + "54003801", 6);
        stringBuilder.setLength(0);
        stringBuilder.append("\n");
        stringBuilder.append("\n");
        stringBuilder.append("\n");
        analogicsprint(stringBuilder.toString(), 4);

    }

    public void analogicsprint(String Printdata, int feed_line) {
        conn.printData(api.font_Courier_30_VIP(Printdata));
        text_line_spacing(feed_line);
    }

    public void analogics_header__double_print(String Printdata, int feed_line) {
        conn.printData(api.font_Double_Height_On_VIP());
        analogics_header_print(Printdata, feed_line);
        conn.printData(api.font_Double_Height_Off_VIP());
    }

    public void analogics_double_print(String Printdata, int feed_line) {
        conn.printData(api.font_Double_Height_On_VIP());
        analogicsprint(Printdata, feed_line);
        conn.printData(api.font_Double_Height_Off_VIP());
    }

    public void analogics_header_print(String Printdata, int feed_line) {
        conn.printData(api.font_Courier_38_VIP(Printdata));
        text_line_spacing(feed_line);
    }

    public void text_line_spacing(int space) {
        conn.printData(api.variable_Size_Line_Feed_VIP(space));
    }

    public void analogics_48_print(String Printdata, int feed_line) {
        conn.printData(api.font_Courier_48_VIP(Printdata));
        text_line_spacing(feed_line);
    }

    private void print_bar_code(String msg) {
        String feeddata = "";
        feeddata = api.barcode_Code_128_Alpha_Numerics_VIP(msg);
        conn.printData(feeddata);
    }

}

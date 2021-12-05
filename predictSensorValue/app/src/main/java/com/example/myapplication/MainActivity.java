package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManger;
    private Sensor linearSensor;
    private Sensor gyroSensor;
    private Queue axque;
    private Queue ayque;
    private Queue azque;
    private Queue gxque;
    private Queue gyque;
    private Queue gzque;

    private int num = 0;

    float ax = (float) 0.0;
    float ay = (float) 0.0;
    float az = (float) 0.0;
    float gx = (float) 0.0;
    float gy = (float) 0.0;
    float gz = (float) 0.0;
    float axmax = (float) 0.0;
    float axmin = (float) 0.0;
    float aymax = (float) 0.0;
    float aymin = (float) 0.0;
    float azmax = (float) 0.0;
    float azmin = (float) 0.0;
    float gxmax = (float) 0.0;
    float gxmin = (float) 0.0;
    float gymax = (float) 0.0;
    float gymin = (float) 0.0;
    float gzmax = (float) 0.0;
    float gzmin = (float) 0.0;
    float CVA = (float) 0.0;

    TextView textView;

    static final int SMS_SEND_PERMISSION=1;

    private GpsTracker gpsTracker;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


// ----------------------------------------------------- 센서값 읽어오기 -------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        axque=new Queue();
        ayque=new Queue();
        azque=new Queue();
        gxque=new Queue();
        gyque=new Queue();
        gzque=new Queue();

        textView = (TextView) findViewById(R.id.result);

        mSensorManger=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        linearSensor = mSensorManger.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gyroSensor=mSensorManger.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


// ------------------------------------------- 주소값 읽어오기 ----------------------------------------------------
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
        }

        final TextView textAddress = (TextView)findViewById(R.id.textAddress);
        Button ShowLocationButton = (Button) findViewById(R.id.getAddrButton);
        ShowLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                gpsTracker = new GpsTracker(MainActivity.this);

                double latitude = gpsTracker.getLatitude();
                double longitude = gpsTracker.getLongitude();

                String address = getCurrentAddress(latitude, longitude);
                textAddress.setText(address);

//                Toast.makeText(MainActivity.this, "현재위치 \n위도 " + latitude + "\n경도 " + longitude, Toast.LENGTH_SHORT).show();

                checkSmsPermission();   // SMS 권한 확인
                String phoneNumber = "여기에 -없이 전화번호 입력";

                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNumber, null, address, null, null);
                    Toast.makeText(getApplicationContext(), "메시지 전송 완료", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "메시지 전송 실패...", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
// -----------------------------------------------------------------------------------------------------------

    }


// ----------------------------------------------------- 센서값 읽어오기 -------------------------------------------------------
    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(MainActivity.this, modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 모델을 읽어오는 함수로, 텐서플로 라이트 홈페이지에 있다.
    // MappedByteBuffer 바이트 버퍼를 Interpreter 객체에 전달하면 모델 해석을 할 수 있다.
    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mSensorManger.registerListener(this, linearSensor, SensorManager.SENSOR_DELAY_UI);
        mSensorManger.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManger.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == linearSensor && num > 160) {
            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];
        }

        if (event.sensor == gyroSensor && num > 160) {
            double gyroX = event.values[0];
            double gyroY = event.values[1];
            double gyroZ = event.values[2];
            gx = (float) gyroX;
            gy = (float) gyroY;
            gz = (float) gyroZ;

            axque.enqueue(ax);
            ayque.enqueue(ay);
            azque.enqueue(az);
            gxque.enqueue(gx);
            gyque.enqueue(gy);
            gzque.enqueue(gz);

            if (axque.size() > 40) {
                axque.dequeue();
                ayque.dequeue();
                azque.dequeue();
                gxque.dequeue();
                gyque.dequeue();
                gzque.dequeue();
            }

            if (axque.size() == 40) {
                axmax = axque.max();
                aymax = ayque.max();
                azmax = azque.max();
                axmin = axque.min();
                aymin = ayque.min();
                azmin = azque.min();
                gxmax = gxque.max();
                gymax = gyque.max();
                gzmax = gzque.max();
                gxmin = gxque.min();
                gymin = gyque.min();
                gzmin = gzque.min();
                CVA = (float) Math.sqrt(Math.pow(ax, 2) + Math.pow(ay, 2) + Math.pow(az, 2));

                float[][][] input = new float[][][]{{{axmax, axmin, aymax, aymin, azmax, azmin, CVA, gxmax, gxmin, gymax, gymin, gzmax, gzmin}}};
                float[][][] output = new float[][][]{{{(float) 0.0, (float) 0.0, (float) 0.0, (float) 0.0, (float) 0.0, (float) 0.0}}};

                Interpreter tflite = getTfliteInterpreter("tensorModel_211205.tflite");
                tflite.run(input, output);

                float max = output[0][0][0];
                int activity = (int) 0;
                String[] activities = new String[]{"Sit", "Stand", "Walk", "Run", "StairUp", "StrDown"};

                for (int i = 0; i < output[0][0].length; i++) {
                    if (output[0][0][i] > max) {
                        max = output[0][0][i];
                        activity = i;
                    }
                }
                textView.setText(activities[activity]);
            }
        }
        num = num + 1;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


// ---------------------------------------------------- 주소값 읽어오기 -------------------------------------------------------------------
    /*
     * ActivityCompat.requestPermissions를 사용하여 퍼미션 요청의 결과를 리턴받는 메소드
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                //위치 값을 가져올 수 있음
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    // 런타임 퍼미션 처리
    void checkRunTimePermission(){
        // 위치 퍼미션을 가지고 있는지 체크함
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);

        // 퍼미션을 가지고 있다면 위치 값을 가져올 수 있음
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
        }
        else {  // 퍼미션 요청을 허용한 적이 없다면
            // 사용자가 과거에 퍼미션 거부를 한 적이 있는 경우, 설명과 함께 사용자에게 퍼미션을 요청. 요청 결과는 onRequestPermissionResult에서 수신됨.
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
            // 사용자가 퍼미션 거부를 한 적이 없는 경우에는 바로 퍼미션 요청
            ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
        }

    }

    // GeoCoder를 사용하여 위도&경도를 주소로 변환
    public String getCurrentAddress( double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표입니다.", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표입니다.";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "해당하는 주소가 없습니다.";
        }

        Address address = addresses.get(0);

        return address.getAddressLine(0).toString()+"\n";
    }

    // GPS를 활성화하기 위한 메소드(1)
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n" + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);

        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    // GPS를 활성화하기 위한 메소드(2)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }

    // GPS를 활성화하기 위한 메소드(3)
    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // SMS 권한이 부여되어 있는지 확인하는 함수
    void checkSmsPermission(){
        int permissionCheck= ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);

        if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(), "SMS 수신권한 있음", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "SMS 수신권한 없음", Toast.LENGTH_SHORT).show();

            // 권한 설정 dialog에서 '거부'를 누르면
            // ActivityCompat.shouldShowRequestPermissionRationale 메소드의 반환값이 true가 된다.
            // 단, 사용자가 "Don't ask again"을 체크한 경우
            // 거부하더라도 false를 반환하여, 직접 사용자가 권한을 부여하지 않는 이상, 권한을 요청할 수 없게 된다.

            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                // 이곳에 권한이 왜 필요한지 설명하는 Toast나 dialog를 띄워준 후, 다시 권한을 요청한다.
                Toast.makeText(getApplicationContext(), "SMS 권한이 필요합니다", Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.SEND_SMS}, SMS_SEND_PERMISSION);
        }
    }

}
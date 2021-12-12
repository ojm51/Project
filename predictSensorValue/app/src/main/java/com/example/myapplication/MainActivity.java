package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

public class MainActivity extends AppCompatActivity implements SensorEventListener, OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        PlacesListener {

    private SensorManager mSensorManger;
    private Sensor linearSensor;
    private Sensor gyroSensor;
    private Queue axque;
    private Queue ayque;
    private Queue azque;
    private Queue gxque;
    private Queue gyque;
    private Queue gzque;
    private KalmanFilter mKalmanAccX;
    private KalmanFilter mKalmanAccY;
    private KalmanFilter mKalmanAccZ;
    private KalmanFilter mKalmanGyroX;
    private KalmanFilter mKalmanGyroY;
    private KalmanFilter mKalmanGyroZ;

    private int num = 0;
    private String phoneNumber;

    float ax = (float) 0.0;
    float ay = (float) 0.0;
    float az = (float) 0.0;
    float gx = (float) 0.0;
    float gy = (float) 0.0;
    float gz = (float) 0.0;
    float Kalax = (float) 0.0;
    float Kalay = (float) 0.0;
    float Kalaz = (float) 0.0;
    float Kalgx = (float) 0.0;
    float Kalgy = (float) 0.0;
    float Kalgz = (float) 0.0;
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

    int hasSMSPermission;
    int hasFineLocationPermission;
    int hasCoarseLocationPermission;

    private static final String TAG = "googlemap_example";
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SEND_SMS,Manifest.permission.CALL_PHONE};

    private GoogleMap mMap;
    private Marker currentMarker = null;
    List<Marker> previous_marker = null;

    String address;
    Location mCurrentLocation;
    LatLng currentPosition;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;

    private Dialog dialog;
    private Handler dHandler;

    private FirebaseAuth mAuth;

    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        // 퍼미션을 가지고 있는지 체크함
        hasSMSPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);

        mKalmanAccX=new KalmanFilter(0.0f);
        mKalmanAccY=new KalmanFilter(0.0f);
        mKalmanAccZ=new KalmanFilter(0.0f);
        mKalmanGyroX=new KalmanFilter(0.0f);
        mKalmanGyroY=new KalmanFilter(0.0f);
        mKalmanGyroZ=new KalmanFilter(0.0f);

        axque=new Queue();
        ayque=new Queue();
        azque=new Queue();
        gxque=new Queue();
        gyque=new Queue();
        gzque=new Queue();

        mSensorManger=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        linearSensor = mSensorManger.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gyroSensor=mSensorManger.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // 주소값 읽어오기
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
        }

        // 메시지 전송 확인창
        dialog=new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog);
        dHandler=new Handler();

        // 네비게이션바(메뉴바)
        bottomNavigation=findViewById(R.id.nav_view);
        bottomNavigation.setOnItemSelectedListener(new TabSelected());

        // 현위치 불러오기
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 구글지도 불러오기
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

//        Button location = (Button) findViewById(R.id.location);
//        location.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // location누르면 현위치 마커 찍어주고, real_time누르면 실시간으로 움직이게?
//            }
//        });

        // 마지막으로 알려진 사용자 위치 가져오기
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // Logic to handle location object
                        }
                    }
                });

        // 경찰서 위치 불러오기
        previous_marker = new ArrayList<Marker>();
        Button button = (Button)findViewById(R.id.police_station);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlaceInformation(currentPosition);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "onMapReady :");
        mMap = googleMap;

        // 지도의 초기 위치를 서울로 설정
        setDefaultLocation();

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                Log.d( TAG, "onMapClick :");
            }
        });

    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                Location location = locationList.get(locationList.size() - 1);

                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());

                Log.d(TAG, "onLocationResult : " + markerSnippet);

                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);
                mCurrentLocation = location;
            }
        }
    };

    // 주변의 특정 건물 찾기
    @Override
    public void onPlacesFailure(PlacesException e) {

    }

    @Override
    public void onPlacesStart() {

    }

    @Override
    public void onPlacesSuccess(final List<Place> places) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (noman.googleplaces.Place place : places) {
                    LatLng latLng = new LatLng(place.getLatitude(), place.getLongitude());

                    String markerSnippet = getCurrentAddress(latLng);

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(place.getName());
                    markerOptions.snippet(markerSnippet);
                    Marker item = mMap.addMarker(markerOptions);
                    previous_marker.add(item);
                }

                //중복 마커 제거
                HashSet<Marker> hashSet = new HashSet<Marker>();
                hashSet.addAll(previous_marker);
                previous_marker.clear();
                previous_marker.addAll(hashSet);

            }
        });
    }

    public void showPlaceInformation(LatLng location)
    {
        mMap.clear();//지도 클리어

        if (previous_marker != null)
            previous_marker.clear();//지역정보 마커 클리어

        new NRPlaces.Builder()
                .listener(MainActivity.this)
                .key("AIzaSyBfl2h0siwrjJfLfyuhbtixLDYsOA3z5qA")
                .latlng(location.latitude, location.longitude)//현재 위치
                .radius(1500) //1500 미터 내에서 검색
                .type(PlaceType.POLICE) //경찰서
                .build()
                .execute();
    }

    @Override
    public void onPlacesFinished() {

    }
    // 주변의 특정 건물 찾기 - 여기까지

    class TabSelected implements NavigationBarView.OnItemSelectedListener {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch ((item.getItemId())){
                case R.id.navigation_detection:{

                    return true;
                }
                case R.id.navigation_call:{
                    // 112 신고(전화)
                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("tel:"+phoneNumber));
                    startActivity(intent);
                    return true;
                }
                case R.id.navigation_info:{
                    Intent intent_info = new Intent(MainActivity.this, writeInfo.class);
                    startActivity(intent_info);
                    return true;
                }
            }
            return false;
        }
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

            Kalax=(float)mKalmanAccX.update(ax);
            Kalay=(float)mKalmanAccY.update(ay);
            Kalaz=(float)mKalmanAccZ.update(az);
        }

        if (event.sensor == gyroSensor && num > 160) {
            double gyroX = event.values[0];
            double gyroY = event.values[1];
            double gyroZ = event.values[2];
            gx = (float) gyroX;
            gy = (float) gyroY;
            gz = (float) gyroZ;

            Kalgx=(float)mKalmanGyroX.update(gx);
            Kalgy=(float)mKalmanGyroY.update(gy);
            Kalgz=(float)mKalmanGyroZ.update(gz);

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
                CVA = (float) Math.sqrt(Math.pow(Kalax, 2) + Math.pow(Kalay, 2) + Math.pow(Kalaz, 2));

                float[][][] input = new float[][][]{{{axmax, axmin, aymax, aymin, azmax, azmin, CVA, gxmax, gxmin, gymax, gymin, gzmax, gzmin}}};
                float[][][] output = new float[][][]{{{(float) 0.0, (float) 0.0, (float) 0.0, (float) 0.0, (float) 0.0, (float) 0.0, (float) 0.0}}};

                Interpreter tflite = getTfliteInterpreter("tensorModel_211211.tflite");
                tflite.run(input, output);

                float max = output[0][0][0];
                int activity = (int) 0;
                String[] activities = new String[]{"Sit", "Stand", "Walk", "Run", "StairUp", "StrDown", "abnormal"};

                for (int i = 0; i < output[0][0].length; i++) {
                    if (output[0][0][i] > max) {
                        max = output[0][0][i];
                        activity = i;
                    }
                }
                if(activities[activity]=="abnormal") {
                    mSensorManger.unregisterListener(this);
                    showDialog();
                }
            }
        }
        num = num + 1;
    }

    // Dialog popup
    public  void showDialog(){
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);
        dialog.show();

        // 아니오 버튼
        Button noBtn = dialog.findViewById(R.id.noBtn);
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 원하는 기능 구현
                dialog.dismiss(); // 다이얼로그 닫기
            }
        });
        // 네 버튼
        dialog.findViewById(R.id.yesBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GpsTracker gpsTracker = new GpsTracker(MainActivity.this);
                double latitude = gpsTracker.getLatitude();
                double longitude = gpsTracker.getLongitude();

                address = getCurrentAddress(latitude, longitude);

                phoneNumber = "여기에 -없이 전화번호 입력";

                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNumber, null, address, null, null);
                    Toast.makeText(getApplicationContext(), "메시지 전송 완료", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "메시지 전송 실패", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        dHandler.postDelayed(dRunnable, 5000);
    }

    //Dialog delay
    private Runnable dRunnable=new Runnable() {
        @Override
        public void run() {
            dialog.dismiss();
        }
    };

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void startLocationUpdates() {
        if (!checkLocationServicesStatus()) {
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        }else {
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED   ) {
                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }

            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){}
        else{
            signInAnonymously();
        }

        if (checkPermission()) {
            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            if (mMap!=null)
                mMap.setMyLocationEnabled(true);
        }
    }

    private void signInAnonymously() {
        // [START signin_anonymously]
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // [END signin_anonymously]
    }
    private void updateUI(FirebaseUser user) { }

    @Override
    protected void onStop() {
        super.onStop();

        if (mFusedLocationClient != null) {
            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private boolean checkPermission() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        return hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED;
    }

    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        if (currentMarker != null) currentMarker.remove();

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);
    }

    public void setDefaultLocation() {
        // 디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치 정보를 가져올 수 없음";
        String markerSnippet = "위치 허용 여부를 확인하세요";

        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);
    }

    /*
     * ActivityCompat.requestPermissions를 사용하여 퍼미션 요청의 결과를 리턴받는 메소드
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);

        // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신된 경우
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (!check_result) {
                startLocationUpdates();
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[2])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[3])) {
                    Toast.makeText(MainActivity.this, "권한이 거부되었습니다. 앱을 재실행하여 권한을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "권한이 거부되었습니다. 설정(앱 정보)에서 권한을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    // 런타임 퍼미션 처리
    void checkRunTimePermission(){
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasSMSPermission == PackageManager.PERMISSION_GRANTED) {
            // 퍼미션을 가지고 있다면 위치 값을 가져올 수 있음
        }
        else {  // 퍼미션 요청이 허용되지 않았다면
            // i) 사용자가 과거에 퍼미션 거부를 한 적이 있는 경우, 퍼미션을 다시 요청. 요청 결과는 onRequestPermissionResult에서 수신됨.
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {
                Toast.makeText(MainActivity.this, "설정(앱 정보)에서 위치 및 SMS 권한을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
            }
            // ii) 사용자가 퍼미션 거부를 한 적이 없는 경우, 바로 퍼미션 요청
            ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
        }
    }

    // GeoCoder를 사용하여 위도&경도를 주소로 변환
    public String getCurrentAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {     //네트워크 문제
            Toast.makeText(this, "주소 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "주소 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표입니다.", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표입니다.";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "해당하는 주소가 없습니다.", Toast.LENGTH_LONG).show();
            return "해당하는 주소가 없습니다.";
        }

        Address address = addresses.get(0);

        return address.getAddressLine(0).toString()+"\n";
    }

    public String getCurrentAddress(LatLng latlng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }

    // GPS를 활성화하기 위한 메소드(1)
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 사용 권한이 필요합니다.");
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

        if (requestCode == GPS_ENABLE_REQUEST_CODE) {//사용자가 GPS 활성화했는지 검사
            if (checkLocationServicesStatus()) {
                if (checkLocationServicesStatus()) {
                    Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                    checkRunTimePermission();
                }
            }
        }
    }

    // GPS를 활성화하기 위한 메소드(3)
    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

}
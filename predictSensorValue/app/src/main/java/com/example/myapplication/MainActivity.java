package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManger;
    private Sensor linearSensor;
    private Sensor gyroSensor;

    private double roll;
    private double pitch;
    private double yaw;

    private double timestamp=0.0;
    private double dt;

    private double rad_to_dgr=180/Math.PI;
    private static final float NS2S=1.0f/1000000000.0f;

    TextView textView;
    Button button;
    TextView xaxis, yaxis, zaxis, xaxis2, yaxis2, zaxis2;
    float x = (float) 0.0;
    float y = (float) 0.0;
    float z = (float) 0.0;
    float gx = (float) 0.0;
    float gy = (float) 0.0;
    float gz = (float) 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.result);
        xaxis=(TextView)findViewById(R.id.xaxis);
        yaxis=(TextView)findViewById(R.id.yaxis);
        zaxis=(TextView)findViewById(R.id.zaxis);
        xaxis2=(TextView)findViewById(R.id.xaxis2);
        yaxis2=(TextView)findViewById(R.id.yaxis2);
        zaxis2=(TextView)findViewById(R.id.zaxis2);
        button = (Button)findViewById(R.id.button);

        mSensorManger=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        linearSensor = mSensorManger.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gyroSensor=mSensorManger.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float cva = (float)Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
                try {
                    float[][][] input = new float[][][]{{{x,y,z,gx,gy,gz}}};
                    float[][][] output = new float[][][]{{{0}}};

                    Interpreter tflite = getTfliteInterpreter("tflite_model_211108.tflite");
                    tflite.run(input, output);

                    float activity = (float)0.5;
                    if (output[0][0][0] < activity){
                        textView.setText("The activity is walking");
                    }
                    else{
                        textView.setText("The activity is running");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

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
        if (event.sensor == linearSensor) {
            xaxis.setText("X axis :" + String.format("%.2f", event.values[0]));
            x = event.values[0];
            yaxis.setText("Y axis :" + String.format("%.2f", event.values[1]));
            y = event.values[1];
            zaxis.setText("Z axis :" + String.format("%.2f", event.values[2]));
            z = event.values[2];
        }
        if (event.sensor == gyroSensor) {
            double gyroX = event.values[0];
            double gyroY = event.values[1];
            double gyroZ = event.values[2];
            gx = (float) gyroX;
            gy = (float) gyroY;
            gz = (float) gyroZ;

            dt = (event.timestamp - timestamp) * NS2S;
            timestamp = event.timestamp;

            if (dt - timestamp * NS2S != 0) {
                pitch = pitch + gyroY * dt;
                roll = roll + gyroX * dt;
                yaw = yaw + gyroZ * dt;

                xaxis2.setText("X axis :" + String.format("%.2f", gx));
                yaxis2.setText("Y axis :" + String.format("%.2f", gy));
                zaxis2.setText("Z axis :" + String.format("%.2f", gz));
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
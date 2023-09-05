package com.example.busta;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class search_ing extends AppCompatActivity {
    // 필요한 권한을 정의합니다.
    private final String[] PERMISSIONS = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private final int REQUEST_ALL_PERMISSION = 1;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice device;
    private ConnectThread connectThread;
    private Handler handler;

    private ImageView Buffer;
    private int mDegree;
    private int loopkey = 1;
    private Handler rotationHandler = new Handler(Looper.getMainLooper());
    private Runnable rotationRunnable;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // SPP UUID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_ing);

        // 이미지 뷰를 초기화합니다.
        Buffer = findViewById(R.id.Buffer);

        // 이미지 회전 관련 변수들을 초기화하고 회전 작업을 설정합니다.
        rotationRunnable = new Runnable() {
            @Override
            public void run() {
                mDegree = mDegree - 36;
                Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.buffer_img);
                Bitmap rotatedBitmap = rotateImageWithOriginalSize(originalBitmap, mDegree);
                Buffer.setImageBitmap(rotatedBitmap);
                rotationHandler.postDelayed(this, 500); // 5초마다 회전
            }
        };

        // 권한을 확인하고 부여받지 않은 경우 권한 요청을 합니다.
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_ALL_PERMISSION);
        } else {
            initializeBluetooth(); // 블루투스 초기화
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        rotationHandler.post(rotationRunnable); // 회전 작업 시작
    }

    @Override
    protected void onPause() {
        super.onPause();
        rotationHandler.removeCallbacks(rotationRunnable); // 회전 작업 중지
    }

    // 권한을 확인합니다.
    private boolean hasPermissions(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "권한이 부여되었습니다!", Toast.LENGTH_SHORT).show();
                initializeBluetooth(); // 블루투스 초기화
            } else {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_ALL_PERMISSION);
                Toast.makeText(this, "권한이 부여되어야 합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 블루투스 초기화를 수행합니다.
    private void initializeBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        String deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null) {
            device = findBluetoothDevice(deviceName); // 블루투스 디바이스 찾기
            if (device != null) {
                connectDevice(); // 디바이스 연결
            } else {
                // 디바이스를 찾지 못한 경우 처리
            }
        }
    }

    // 주어진 디바이스 이름으로 블루투스 디바이스를 찾습니다.
    private BluetoothDevice findBluetoothDevice(String deviceName) {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice pairedDevice : pairedDevices) {
            if (pairedDevice.getName().equals(deviceName)) {
                return pairedDevice;
            }
        }
        return null;
    }

    // 디바이스에 연결을 시도합니다.
    private void connectDevice() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        Intent intent = new Intent(search_ing.this, connected.class);
                        startActivity(intent);
                        finish();
                        break;
                }
                return true;
            }
        });

        connectThread = new ConnectThread(device);
        connectThread.start();
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tempSocket = null;
            try {
                tempSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = tempSocket;
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();

            try {
                if (socket != null) {
                    socket.connect();
                    handler.obtainMessage(1).sendToTarget();
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    // 원본 크기로 이미지를 회전합니다.
    public Bitmap rotateImageWithOriginalSize(Bitmap src, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);

        // 이미지 회전
        Bitmap rotatedBitmap = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);

        // 회전된 이미지를 원본 크기로 스케일링
        float scaleFactor = Math.min(
                (float) src.getWidth() / rotatedBitmap.getWidth(),
                (float) src.getHeight() / rotatedBitmap.getHeight()
        );
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(rotatedBitmap,
                Math.round(rotatedBitmap.getWidth() * scaleFactor),
                Math.round(rotatedBitmap.getHeight() * scaleFactor), true);

        return scaledBitmap;
    }
}

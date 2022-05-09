package com.example.performanceopsample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Sample-MainA";
    private TextView textView = null;
    private static boolean isStrictMode = false;

    private static final boolean DEVELOPER_MODE_1 = false;

    private static final boolean DEVELOPER_MODE_2 = false;

    private static final boolean DEVELOPER_MODE_3 = false;

    private static final boolean DEVELOPER_MODE_4 = false;

    private static final boolean DEVELOPER_MODE_5 = false;
    private MyReceiver receiver;

    //[ DEVELOPER_MODE_6
    private static final boolean DEVELOPER_MODE_6 = true;
    private static class MyClass {
    }
    private static List<MainActivity.MyClass> classList = new ArrayList<>();
    // DEVELOPER_MODE_6]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.text_view);
        log("onCreate");

        if (DEVELOPER_MODE_1) {
            testNetwork();
        }

        if (DEVELOPER_MODE_2) {
            testSlowCall();
        }

        if (DEVELOPER_MODE_3) {
            testActivityLeak();
        }

        if (DEVELOPER_MODE_4) {
            testLeakedClosableObjects();
        }

        if (DEVELOPER_MODE_5) {
            testLeakedRegistrationObjects();
        }

        if (DEVELOPER_MODE_6) {
            testClassInstanceLimit();
        }
    }

    //1. detectAll (Network)
    private void testNetwork() {
        textView.setText("In testNetwork -> detectAll");
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyDialog() //弹出违规提示对话框
                .penaltyLog() //在Logcat 中打印违规异常信息
                .build());
        try {
            URL url = new URL("http://www.baidu.com");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String lines = null;
            StringBuffer sb = new StringBuffer();
            while ((lines = reader.readLine()) != null) {
                sb.append(lines);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //2. detectCustomSlowCalls
    private void testSlowCall() {
        textView.setText("In testSlowCall -> detectCustomSlowCalls");
        new Thread() {
            public void run() {
                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                        .detectCustomSlowCalls()
                        .penaltyLog()
                        .build());
                this.slowCallInCustomThread();
            }

            private void slowCallInCustomThread() {
                //用来标记潜在执行比较慢的方法
                StrictMode.noteSlowCall("slowCallInCustomThread");
                SystemClock.sleep(1000 * 2);
            }
        }.start();
    }

    //3. detectActivityLeaks
    //19:08:10.130  16975  16975  StrictMode:  StrictMode policy violation: android.os.strictmode.InstanceCountViolation:
    // class com.example.performanceopsample.MainActivity; instances=4; limit=3
    private void testActivityLeak() {
        textView.setText("In testActivityLeak -> detectActivityLeaks");
        if (!isStrictMode) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectActivityLeaks()
                    .penaltyLog()
                    .build());
            isStrictMode = true;
        }
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    SystemClock.sleep(1000);
                }
            }
        }.start();
    }

    //4. detectLeakedClosableObjects
    //19:19:15.456  18014  18028  StrictMode:  StrictMode policy violation: android.os.strictmode.LeakedClosableViolation:
    //A resource was acquired at attached stack trace but never released.
    //See java.io.Closeable for information on avoiding resource leaks.
    private void testLeakedClosableObjects() {
        textView.setText("In testLeakedClosableObjects -> detectLeakedClosableObjects");
        if (!isStrictMode) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build());
            isStrictMode = true;
        }
        //File newxmlfile = new File(Environment.getExternalStorageDirectory(), "aaa.txt");
        File newxmlfile = new File(getCacheDir(), "aaa.txt");
        try {
            newxmlfile.createNewFile();
            FileWriter fw = new FileWriter(newxmlfile);
            fw.write("aaaaaaaaaaa");
            //fw.close(); 我们在这里故意没有关闭 fw
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 5.detectLeakedRegistrationObjects
    // 并为触发???
    private void testLeakedRegistrationObjects() {
        textView.setText("In testLeakedRegistrationObjects -> detectLeakedRegistrationObjects");
        if (!isStrictMode) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedRegistrationObjects()
                    .penaltyLog()
                    .build());
            log("setVmPolicy for detectLeakedRegistrationObjects");
            isStrictMode = true;
        }
        this.receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.MY_BROADCAST");
        registerReceiver(this.receiver, filter);
    }

    // 6.testClassInstanceLimit
    // 并为触发???
    private void testClassInstanceLimit() {
        textView.setText("In testClassInstanceLimit -> setClassInstanceLimit");
        if (!isStrictMode) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .setClassInstanceLimit(MainActivity.class, 2)
                    .penaltyLog()
                    .build());
            isStrictMode = true;
        }
        classList.add(new MainActivity.MyClass());
        classList.add(new MainActivity.MyClass());
        classList.add(new MainActivity.MyClass());
        classList.add(new MainActivity.MyClass());
        classList.add(new MainActivity.MyClass());
        classList.add(new MainActivity.MyClass());
        classList.add(new MainActivity.MyClass());
        classList.add(new MainActivity.MyClass());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }
}
package com.example.performanceopsample;

import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Tmp extends AppCompatActivity {
    private static boolean isStrictMode = false;

    private static class MyClass{}
    private static List<MyClass> classList = new ArrayList<MyClass>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testLeakedRegistrationObjects();
    }

    private void testLeakedRegistrationObjects() {
        if(! isStrictMode){
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .setClassInstanceLimit(MyClass.class, 2)
                    .penaltyLog()
                    .build());
            isStrictMode = true;
        }
        classList.add(new MyClass());
        classList.add(new MyClass());
        classList.add(new MyClass());
        classList.add(new MyClass());
        classList.add(new MyClass());
        classList.add(new MyClass());
        classList.add(new MyClass());
        classList.add(new MyClass());
    }
}

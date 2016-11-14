package com.icapps.sampleapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.icapps.niddler.NiddlerRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NiddlerSampleApplication) getApplication()).getNiddler().logRequest(new NiddlerRequest() {
                    @Override
                    public String getRequestId() {
                        return "Request ID'tje";
                    }

                    @Override
                    public String getUrl() {
                        return "http://www.google.com";
                    }

                    @Override
                    public Map<String, List<String>> getHeaders() {
                        return new HashMap<>(0);
                    }

                    @Override
                    public String getMethod() {
                        return "GET";
                    }

                    @Override
                    public void writeBody(OutputStream stream) {
                        try {
                            stream.write("GOOGLE SEARCH".getBytes("UTF-8"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}

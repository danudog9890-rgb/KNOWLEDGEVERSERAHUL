package com.rahulsingh.knowledgeverse;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new AndroidBridge(), "Android");
        webView.loadUrl("file:///android_asset/index.html");
    }

    public class AndroidBridge {
        @JavascriptInterface
        public void savePdf(String fileName, String base64Data) {
            runOnUiThread(() -> {
                try {
                    byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                        values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/KnowledgeVerse");
                        android.net.Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                        if (uri == null) throw new Exception("Could not create file");
                        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                            if (out == null) throw new Exception("Could not open output stream");
                            out.write(bytes);
                        }
                        Toast.makeText(MainActivity.this, "PDF saved: Downloads/KnowledgeVerse", Toast.LENGTH_LONG).show();
                    } else {
                        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "KnowledgeVerse");
                        if (!dir.exists() && !dir.mkdirs()) throw new Exception("Could not create folder");
                        File file = new File(dir, fileName);
                        try (OutputStream out = new FileOutputStream(file)) { out.write(bytes); }
                        Toast.makeText(MainActivity.this, "PDF saved in app Downloads folder", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "PDF save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}

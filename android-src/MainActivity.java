package com.votan.sochotdon;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.getcapacitor.BridgeActivity;

import java.io.OutputStream;

public class MainActivity extends BridgeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đăng ký bridge TRƯỚC khi WebView load — đảm bảo window.AndroidSaveImage có sẵn
        this.bridge.getWebView().addJavascriptInterface(
            new AndroidSaveImage(this), "AndroidSaveImage"
        );
    }

    public static class AndroidSaveImage {
        private final Context context;
        AndroidSaveImage(Context ctx) { this.context = ctx; }

        @JavascriptInterface
        public void saveBase64Image(String base64Data, String fileName) {
            try {
                String pureBase64 = base64Data.contains(",")
                    ? base64Data.split(",")[1] : base64Data;
                byte[] bytes = Base64.decode(pureBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/SoChotDon");

                Uri uri = context.getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                if (uri != null) {
                    try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    }
                    ((android.app.Activity) context).runOnUiThread(() ->
                        Toast.makeText(context,
                            "✅ Đã lưu vào Thư viện ảnh!", Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (Exception e) {
                ((android.app.Activity) context).runOnUiThread(() ->
                    Toast.makeText(context,
                        "❌ Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }
    }
}

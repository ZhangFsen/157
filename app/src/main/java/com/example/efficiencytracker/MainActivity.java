package com.example.efficiencytracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.os.Build;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.ValueCallback;
import org.json.JSONObject;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/** 兼容 Android 7.0 及以上的轻量 WebView 容器。 */
public class MainActivity extends Activity {
    private WebView webView;
    private static final String SHARE_DIR = "share";
    private static final int REQ_EXPORT = 4101;
    private static final int REQ_IMPORT = 4102;
    private static final int REQ_EXPORT_BINARY = 4104;
    private static final int REQ_FILE_CHOOSER = 4103;
    private String pendingExportJson;
    private String pendingBinaryBase64;
    private String pendingBinaryMime;
    private ValueCallback<Uri[]> fileChooserCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureSystemBars();
        try {
            createWebView();
        } catch (Throwable error) {
            showWebViewError(error);
        }
    }

    private void configureSystemBars() {
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);
        int flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    private void applySystemBarInsets(WebView view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.setOnApplyWindowInsetsListener((v, insets) -> {
                int top = insets.getInsets(WindowInsets.Type.statusBars()).top;
                int bottom = insets.getInsets(WindowInsets.Type.navigationBars()).bottom;
                setWebInsets(top, bottom);
                return insets;
            });
        } else {
            view.setOnApplyWindowInsetsListener((v, insets) -> {
                int top = insets.getSystemWindowInsetTop();
                int bottom = insets.getSystemWindowInsetBottom();
                setWebInsets(top, bottom);
                return insets;
            });
        }
    }

    private void setWebInsets(int topPx, int bottomPx) {
        if (webView == null) return;
        String js = "document.documentElement.style.setProperty('--android-status-inset', '"
                + topPx + "px');"
                + "document.documentElement.style.setProperty('--android-nav-inset', '"
                + bottomPx + "px');";
        webView.evaluateJavascript(js, null);
    }

    private void createWebView() {
        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setTextZoom(100);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(false);
        settings.setSupportZoom(false);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        webView.setBackgroundColor(Color.rgb(246, 248, 252));
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
        webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        webView.addJavascriptInterface(new AndroidShareBridge(), "AndroidShare");
        webView.addJavascriptInterface(new AndroidDataBridge(), "AndroidData");
        applySystemBarInsets(webView);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileChooserCallback != null) fileChooserCallback.onReceiveValue(null);
                fileChooserCallback = callback;
                try {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/*");
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            "application/vnd.ms-excel.sheet.macroEnabled.12",
                            "application/json", "text/plain", "application/octet-stream"
                    });
                    startActivityForResult(intent, REQ_FILE_CHOOSER);
                    return true;
                } catch (Exception e) {
                    fileChooserCallback = null;
                    return false;
                }
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("确认")
                        .setMessage(message)
                        .setPositiveButton("确定", (dialog, which) -> result.confirm())
                        .setNegativeButton("取消", (dialog, which) -> result.cancel())
                        .setOnCancelListener(dialog -> result.cancel())
                        .show();
                return true;
            }
        });
        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
    }

    private final class AndroidDataBridge {
        @JavascriptInterface
        public boolean exportData(String json, String fileName) {
            try {
                pendingExportJson = json == null ? "{}" : json;
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json");
                intent.putExtra(Intent.EXTRA_TITLE, (fileName == null || fileName.trim().isEmpty()) ? "个人效率数据备份.json" : fileName);
                runOnUiThread(() -> startActivityForResult(intent, REQ_EXPORT));
                return true;
            } catch (Exception e) { return false; }
        }

        @JavascriptInterface
        public boolean exportBinary(String base64, String fileName, String mimeType) {
            try {
                pendingBinaryBase64 = base64 == null ? "" : base64;
                pendingBinaryMime = (mimeType == null || mimeType.isEmpty()) ? "application/octet-stream" : mimeType;
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(pendingBinaryMime);
                intent.putExtra(Intent.EXTRA_TITLE, (fileName == null || fileName.trim().isEmpty()) ? "项目工序备份.xlsx" : fileName);
                runOnUiThread(() -> startActivityForResult(intent, REQ_EXPORT_BINARY));
                return true;
            } catch (Exception e) { return false; }
        }

        @JavascriptInterface
        public boolean pickImportFile() {
            try {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/json");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/json", "text/plain", "application/octet-stream"});
                runOnUiThread(() -> startActivityForResult(intent, REQ_IMPORT));
                return true;
            } catch (Exception e) { return false; }
        }
    }

    private final class AndroidShareBridge {
        @JavascriptInterface
        public boolean shareImage(String dataUrl, String fileName) {
            try {
                final boolean jpeg = dataUrl != null && dataUrl.startsWith("data:image/jpeg");
                final String safeName = (fileName == null || fileName.trim().isEmpty())
                        ? (jpeg ? "efficiency-report.jpg" : "efficiency-report.png")
                        : fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
                final String encoded = dataUrl.substring(dataUrl.indexOf(',') + 1);
                final byte[] bytes = Base64.decode(encoded, Base64.DEFAULT);

                File dir = new File(getCacheDir(), SHARE_DIR);
                if (!dir.exists() && !dir.mkdirs()) return false;
                File file = new File(dir, safeName);
                try (FileOutputStream out = new FileOutputStream(file)) {
                    out.write(bytes);
                }

                Uri uri = Uri.parse("content://" + getPackageName() + ".share/" + safeName);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType(jpeg ? "image/jpeg" : "image/png");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setClipData(ClipData.newRawUri("image", uri));

                runOnUiThread(() -> startActivity(Intent.createChooser(intent, "分享效率报告")));
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    private void showWebViewError(Throwable error) {
        TextView message = new TextView(this);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        message.setPadding(padding, padding, padding, padding);
        message.setTextSize(16);
        message.setTextColor(Color.DKGRAY);
        message.setText("无法启动个人效率计算。请在系统设置中启用或更新 Android System WebView，然后重新打开应用。\n\n" + error.getClass().getSimpleName());
        setContentView(message);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            if (requestCode == REQ_FILE_CHOOSER && fileChooserCallback != null) { fileChooserCallback.onReceiveValue(null); fileChooserCallback = null; }
            return;
        }
        Uri uri = data.getData();
        try {
            if (requestCode == REQ_FILE_CHOOSER) {
                if (fileChooserCallback != null) fileChooserCallback.onReceiveValue(new Uri[]{uri});
                fileChooserCallback = null;
                return;
            } else if (requestCode == REQ_EXPORT_BINARY) {
                if (pendingBinaryBase64 == null) return;
                byte[] bytes = Base64.decode(pendingBinaryBase64, Base64.DEFAULT);
                try (java.io.OutputStream out = getContentResolver().openOutputStream(uri)) {
                    if (out == null) throw new IOException("无法打开导出位置");
                    out.write(bytes);
                }
                pendingBinaryBase64 = null; pendingBinaryMime = null;
                if (webView != null) webView.evaluateJavascript("window.UI&&UI.toast&&UI.toast('Excel备份导出成功')", null);
                return;
            } else if (requestCode == REQ_EXPORT) {
                if (pendingExportJson == null) return;
                try (java.io.OutputStream out = getContentResolver().openOutputStream(uri)) {
                    if (out == null) throw new IOException("无法打开导出位置");
                    out.write(pendingExportJson.getBytes("UTF-8"));
                }
                pendingExportJson = null;
                if (webView != null) webView.evaluateJavascript("window.UI&&UI.toast&&UI.toast('数据导出成功')", null);
            } else if (requestCode == REQ_IMPORT) {
                StringBuilder sb = new StringBuilder();
                try (java.io.InputStream in = getContentResolver().openInputStream(uri);
                     java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(in, "UTF-8"))) {
                    String line; while ((line = br.readLine()) != null) sb.append(line).append('\n');
                }
                if (webView != null) {
                    String js = "window.onAndroidImportData && window.onAndroidImportData(" + JSONObject.quote(sb.toString()) + ")";
                    webView.evaluateJavascript(js, null);
                }
            }
        } catch (Exception e) {
            if (webView != null) {
                String msg = requestCode == REQ_EXPORT ? "数据导出失败，请重试" : "读取备份文件失败";
                webView.evaluateJavascript("alert(" + JSONObject.quote(msg) + ")", null);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView == null) return;

        // 单页 App 的“返回”由网页路由统一处理：先关弹窗，再返回上一级页面。
        // 根页面不退出 Activity，避免误返回到桌面。
        webView.evaluateJavascript(
                "(function(){ return window.handleAndroidBack ? window.handleAndroidBack() : true; })()",
                value -> {
                    // handleAndroidBack 在当前 App 中始终消费返回事件。
                });
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}

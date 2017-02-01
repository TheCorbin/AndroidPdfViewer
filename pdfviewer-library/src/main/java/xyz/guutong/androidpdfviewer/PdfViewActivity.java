package xyz.guutong.androidpdfviewer;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.File;

import xyz.guutong.androidpdfviewer.Utils.DownloadFile;
import xyz.guutong.androidpdfviewer.Utils.DownloadFileUrlConnectionImpl;
import xyz.guutong.androidpdfviewer.Utils.FileUtil;

public class PdfViewActivity extends AppCompatActivity implements DownloadFile.Listener, OnPageChangeListener, OnLoadCompleteListener {

    public static final String EXTRA_PDF_URL = "EXTRA_PDF_URL";
    public static final String EXTRA_PDF_TITLE = "EXTRA_PDF_TITLE";
    public static final String EXTRA_SHOW_SCROLL = "EXTRA_SHOW_SCROLL";
    public static final String EXTRA_SWIPE_HORIZONTAL = "EXTRA_SWIPE_HORIZONTAL";
    public static final String EXTRA_TOOLBAR_COLOR = "EXTRA_TOOLBAR_COLOR";

    private Toolbar toolbar;
    private PDFView pdfView;
    private Intent intentUrl;
    private ProgressBar progressBar;
    private String pdfUrl;
    private Boolean showScroll;
    private Boolean swipeHorizontal;
    private String toolbarColor = "#1191d5";
    private String toolbarTitle;
    private DefaultScrollHandle scrollHandle;
    private java.net.URI pdfUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view);

        intentUrl = getIntent();
        pdfUrl = intentUrl.getStringExtra(EXTRA_PDF_URL);
        toolbarTitle = intentUrl.getStringExtra(EXTRA_PDF_TITLE) == null ? "" : intentUrl.getStringExtra(EXTRA_PDF_TITLE);
        toolbarColor = intentUrl.getStringExtra(EXTRA_TOOLBAR_COLOR) == null ? toolbarColor : intentUrl.getStringExtra(EXTRA_TOOLBAR_COLOR);
        showScroll = intentUrl.getBooleanExtra(EXTRA_SHOW_SCROLL,false);
        swipeHorizontal = intentUrl.getBooleanExtra(EXTRA_SWIPE_HORIZONTAL,false);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        pdfView = (PDFView) findViewById(R.id.pdfView);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        /* set color colorPrimaryDark*/
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(toolbarColor), hsv);
        hsv[2] *= 0.8f;
        int colorPrimaryDark = Color.HSVToColor(hsv);
        if(Build.VERSION.SDK_INT>=21) {
            this.getWindow().setStatusBarColor(colorPrimaryDark);
        }


        toolbar.setBackgroundColor(Color.parseColor(toolbarColor));
        toolbar.setTitle(toolbarTitle);

        if(showScroll){
            scrollHandle = new DefaultScrollHandle(this);
        }

        setSupportActionBar(toolbar);

        progressBar.setVisibility(View.VISIBLE);

        downloadPdf(pdfUrl);
    }

    private void downloadPdf(String inPdfUrl) {
        try {
          if(inPdfUrl.startsWith("file://")) {
            this.onSuccess(inPdfUrl, inPdfUrl.replace("file://", ""));
          } else {
            DownloadFile downloadFile = new DownloadFileUrlConnectionImpl(this, new Handler(), this);
            downloadFile.download(inPdfUrl, new File(this.getCacheDir(), FileUtil.extractFileNameFromURL(inPdfUrl)).getAbsolutePath());
          }
        }catch (Exception e){
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_close) {
            finish();
        }
        return true;
    }

    @Override
    public void onSuccess(String url, String destinationPath) {
        File pdf = new File(destinationPath);

        pdfView.fromFile(pdf)
                .defaultPage(0)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(scrollHandle)
                .swipeHorizontal(swipeHorizontal)
                .load();
    }

    @Override
    public void onFailure(Exception e) {
        progressBar.setVisibility(View.GONE);
        AlertDialog.Builder alert = new AlertDialog.Builder(this)
                .setMessage("Cannot open file!")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert);
        alert.show();
    }

    @Override
    public void onProgressUpdate(int progress, int total) {

    }

    @Override
    public void loadComplete(int nbPages) {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onPageChanged(int page, int pageCount) {

    }
}

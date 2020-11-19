package kr.ac.konkuk.timetable;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;

public class LecturePlanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_plan);

        // 뒤로가기 버튼 활성화
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 인텐트로 넘어온 데이터 저장
        Intent in = getIntent();
        String year = in.getStringExtra("year");
        String semester = in.getStringExtra("semester");
        String number = in.getStringExtra("number");

        // 웹뷰에 줌, 자바스크립트 허용 후 강의계획서 URL 로드
        WebView webView = (WebView)findViewById(R.id.lecture_plan_webview_main);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.loadUrl("https://kupis.konkuk.ac.kr/sugang/acd/cour/plan/CourLecturePlanInq.jsp?ltYy="+year+"&ltShtm=B0101"+semester+"&sbjtId="+number);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
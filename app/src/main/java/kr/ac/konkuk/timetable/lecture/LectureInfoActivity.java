package kr.ac.konkuk.timetable.lecture;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import kr.ac.konkuk.timetable.R;

import java.util.ArrayList;

public class LectureInfoActivity extends AppCompatActivity {

    private String year, semester; // 연, 학기 저장할 스트링
    private Handler handler; // 스레드와 통신하는 핸들러
    private ErrorCodeBlock errorCode; // 스레드 에러여부 확인하는 블록
    private AlertDialog progressDialog; // 스레드 중 다이얼로그
    private LectureBlock lectureBlock; // 현재 선택한 강의 블럭
    private ArrayList<LectureBlock> arrayList; // 어레이리스트
    private int[][] peopleBlock; // 학년별 인원을 저장하는 배열, 스레드와 자료 공유
    private int[][] textViewID = {  // 학년별 인원을 표시할 텍스트뷰 ID
            {R.id.lecture_info_textview_1_1, R.id.lecture_info_textview_1_2, R.id.lecture_info_textview_1_3, R.id.lecture_info_textview_1_4},
            {R.id.lecture_info_textview_2_1, R.id.lecture_info_textview_2_2, R.id.lecture_info_textview_2_3, R.id.lecture_info_textview_2_4},
            {R.id.lecture_info_textview_3_1, R.id.lecture_info_textview_3_2, R.id.lecture_info_textview_3_3, R.id.lecture_info_textview_3_4},
            {R.id.lecture_info_textview_4_1, R.id.lecture_info_textview_4_2, R.id.lecture_info_textview_4_3, R.id.lecture_info_textview_4_4},
            {R.id.lecture_info_textview_5_1, R.id.lecture_info_textview_5_2, R.id.lecture_info_textview_5_3, R.id.lecture_info_textview_5_4}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_info);

        // 뒤로가기 설정
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // 인텐트로 넘어온 자료 저장
        Intent in = getIntent();
        year = in.getStringExtra("year");
        semester = in.getStringExtra("semester");
        lectureBlock = (LectureBlock) in.getSerializableExtra("lectureblock");

        // 조회 중 다이얼로그 초기화
        progressDialog = new AlertDialog.Builder(this)
                .setMessage("조회 중 입니다.")
                .setCancelable(false).create();
        progressDialog.show();

        // 변수 초기화
        errorCode = new ErrorCodeBlock();
        peopleBlock = new int[4][3];

        // 스레드가 종료되면 실행 될 핸들러
        handler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (errorCode.getErrorCode()) {
                    Toast.makeText(getApplicationContext(), "네트워크를 확인해주세요.", Toast.LENGTH_SHORT).show();
                    finish();
                }

                if(msg.what == 1){
                    if(arrayList.size() > 0){
                        // 현재 강의 블럭에 담긴 텍스트뷰에 정보를 표시
                        LectureBlock lb = arrayList.get(0);
                        ((TextView) findViewById(R.id.lecture_info_textview_lecture_info)).setText(
                                "강의번호　　" + lb.getNumber()
                                        + "\n학수번호　　" + lb.getHaksu()
                                        + "\n강의명　　　" + lb.getTitle()
                                        + "\n교수　　　　" + lb.getProfessor()
                                        + "\n학년　　　　" + lb.getGrade()
                                        + "\n시간강의실　" + lb.getTime()
                                        + "\n이수구분　　" + lb.getDiv()
                                        + "\n학점　　　　" + lb.getCredit()
                                        + "\n원어강의　　" + lb.getForeign()
                                        + "\n강의유형　　" + lb.getType1()
                                        + "\n강의종류　　" + lb.getType2()
                                        + "\n비고　　　　" + lb.getRemark()
                        );
                    } else {
                        ((TextView) findViewById(R.id.lecture_info_textview_lecture_info)).setText("정보를 표시할 수 없습니다.");
                    }
                }

                if(msg.what == 2) {
                    // 만약 에러 발생 시 종료
                    int[] sum = new int[4]; // 컬럼별 합계 배열

                    // 현재 인원 표에 스레드에서 받아온 학년별 인원 적용하기
                    for (int i = 0; i < 4; i++) {
                        for (int j = 0; j < 3; j++) {
                            sum[j] += peopleBlock[i][j];
                            ((TextView) findViewById(textViewID[i][j])).setText(peopleBlock[i][j] + "명");
                            ((TextView) findViewById(textViewID[4][j])).setText(sum[j] + "명");
                        }
                        ((TextView) findViewById(textViewID[i][3])).setText((peopleBlock[i][2] - peopleBlock[i][1]) + "명");
                        ((TextView) findViewById(textViewID[4][3])).setText((sum[2] - sum[1]) + "명");
                    }
                }

                // 다이얼로그 종료
                progressDialog.dismiss();
            }
        };

        // 스레드 초기화 및 시작
        Thread searchThread = new Thread(new SearchingTask(handler, lectureBlock, peopleBlock, errorCode, year, "B0101" + semester));
        searchThread.start();

        arrayList = new ArrayList<LectureBlock>();
        Thread infoThread = new Thread(new SearchingTask(handler, arrayList, errorCode, year, "B0101" + semester, "0", lectureBlock.getNumber()));
        infoThread.start();
    }

    public void onClicked(View view) {
        // 강의계획서로 액티비티 전환
        Intent intent = new Intent(this, LecturePlanActivity.class);
        intent.putExtra("year", year).putExtra("semester", semester).putExtra("number", lectureBlock.getNumber());
        startActivity(intent);
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
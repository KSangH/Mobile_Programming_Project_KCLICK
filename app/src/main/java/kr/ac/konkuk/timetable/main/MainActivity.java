package kr.ac.konkuk.timetable.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.ChipGroup;
import kr.ac.konkuk.timetable.R;
import kr.ac.konkuk.timetable.lecture.LectureActivity;
import kr.ac.konkuk.timetable.lecture.LectureBlock;
import kr.ac.konkuk.timetable.lecture.LectureInfoActivity;
import kr.ac.konkuk.timetable.theme.ThemeSettingActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //private TimeblockView[][] timeTableArrays; // 시간표 배열
    private TableRow[] tableRows;
    private String year, semester, code = "1"; // 연도, 학기, 시간표번호
    private ArrayList<LectureBlock> lectureList; // 과목 배열
    private final int TIME_COUNT = 24; // 현재 지원은 0 - 23교시 
    private AbsoluteLayout timeAbsoluteLayout; // 블록 위에 강의명, 교수명 적혀질 레이아웃
    private LinearLayout timeLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTimetableInit();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 만약 LectureActivity에서 시간표 수정 후 Result_OK가 넘어오면 시간표 재설정하기
        if (resultCode == RESULT_OK)
            setTimetable();
    }

    private void setTimetableInit() {

        // 초기 로딩 중 다이얼로그
        AlertDialog initDialog = new AlertDialog.Builder(this)
                .setMessage("로딩 중 입니다.")
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        setTimetable();
                    }
                }).create();
        initDialog.show();

        // 컬러 정보 초기화
        SharedPreferences sharedPreferences_color = getSharedPreferences("KU-COLOR", MODE_PRIVATE);
        String color_init = sharedPreferences_color.getString("color1", "");

        if (color_init.equals("")) {
            SharedPreferences.Editor editor = sharedPreferences_color.edit();
            String[] colorArray = {"#ba6b6c", "#c48b9f", "#9c64a6", "#a094b7", "#6f79a8",
                    "#8aacc8", "#4ba3c7", "#81b9bf", "#4f9a94", "#97b498",
                    "#94af76", "#808e95", "#8c7b75", "#ca9b52", "#cb9b8c"};
            for (int i = 0; i < 15; i++) {
                editor.putString("color" + (i + 1), colorArray[i]);
            }
            editor.commit();
        }

        // 년, 학기 저장을 위한 SharedPreferences 사용
        SharedPreferences sharedPreferences = getSharedPreferences("KU", MODE_PRIVATE);
        // 값을 얻어온다.
        year = sharedPreferences.getString("year", "");
        semester = sharedPreferences.getString("semester", "");

        // 만약 초기 실행 시 값이 없다면 2021, 1학기로 설정해준다.
        if (year.equals("") || semester.equals("")) {
            year = "2021";
            semester = "1";
            saveSemester();
        }

        // 상단 타이틀 이름을 바꿔준다.
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name) + " (" + year + "년 " + semester + "학기)");

        // 과목 블록 초기화
        lectureList = new ArrayList<LectureBlock>();

        // 시간표 생성을 위한 내용
        //timeTableArrays = new TimeblockView[TIME_COUNT][5]; // 타임 테이블 배경화면
        TableLayout table = (TableLayout) findViewById(R.id.main_timetable_tablelayout); // 테이블 레이아웃 불러오기
        timeAbsoluteLayout = findViewById(R.id.main_timetable_absolutelayout); // 절대레이아웃 불러오기
        timeLinearLayout = findViewById(R.id.main_linearlayout_elearning);
        TableRow.LayoutParams timeTableParams = new TableRow.LayoutParams(getResources().getDimensionPixelSize(R.dimen.timetable_column_width), TableRow.LayoutParams.MATCH_PARENT); // 레이아웃 정보 설정
        tableRows = new TableRow[14];

        for (int i = 0; i < 14; i++) {
            // 각각의 테이블로우를 만듦.
            tableRows[i] = new TableRow(this);

            // 교시를 나타내는 타임블록뷰
            TimeblockView countView = new TimeblockView(this, " " + (8 + i) + " ");

            // 레이아웃 설정
            countView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, getResources().getDimensionPixelSize(R.dimen.timetable_column_height)));

            // 테이블 행에 추가
            tableRows[i].addView(countView);

            // 월화수목금 블럭 생성
            for (int j = 0; j < 5; j++) {
                TimeblockView timeblockView = new TimeblockView(this, "");
                timeblockView.setLayoutParams(timeTableParams);
                tableRows[i].addView(timeblockView);
            }

            // 테이블에 테이블로우 추가
            table.addView(tableRows[i]);

            if (i == 0 || i > 9) {
                tableRows[i].setVisibility(View.GONE);
            }
        }

        // 칩이 선택되었을 때, 코드 변경 후, 시간표 변경
        ChipGroup chipGroup = (ChipGroup) findViewById(R.id.main_chip_group);
        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.main_chip_1:
                        code = "1";
                        break;
                    case R.id.main_chip_2:
                        code = "2";
                        break;
                    case R.id.main_chip_3:
                        code = "3";
                        break;
                    case R.id.main_chip_4:
                        code = "4";
                        break;
                    case R.id.main_chip_5:
                        code = "5";
                        break;
                }
                // 시간표를 새로고침한다.
                setTimetable();
            }
        });

        // 로딩 다이얼로그 종료
        initDialog.dismiss();
    }

    // 시간표 초기화 함수
    private void setTimetableClear() {
        // 과목배열 초기화
        lectureList.clear();
        // 시간표 위에 표시된 강의 뷰 삭제
        timeAbsoluteLayout.removeAllViews();
        timeLinearLayout.removeAllViews();
        // 강의블록 색상 순서 초기화
        TimeblockView.setInitColor(getApplicationContext());

        for (int i = 0; i < tableRows.length; i++) {
            if (i == 0 || i > 9) {
                tableRows[i].setVisibility(View.GONE);
            }
        }
    }

    // 시간표 셋팅 함수 (새로고침 함수)
    private void setTimetable() {
        // 기존 표시 된 시간표를 삭제한다.
        setTimetableClear();

        // 파일이름 설정
        String fileName = year + "-" + semester + "-" + code + ".txt";
        File file = new File(getFilesDir(), fileName);

        // 파일이 없으면 종료한다.
        if (!file.exists())
            return;
        try {
            // 파일을 읽어드린다.
            FileInputStream fileInputStream = openFileInput(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((fileInputStream)));
            String readStr;
            // 한줄 씩 읽고, null이 되면 종료한다.
            while ((readStr = bufferedReader.readLine()) != null) {
                // 강의블록을 추가함
                lectureList.add(new LectureBlock(readStr));
            }
            bufferedReader.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 기본 시간표 9시-18시 초과 시 행 추가
        for (LectureBlock lb : lectureList) {
            int[][] convertTime = lb.convertTime();
            if (convertTime == null) continue;
            for (int i = 0; i < convertTime[0].length; i++) {
                if (convertTime[0][i] == 1) {
                    tableRows[0].setVisibility(View.VISIBLE);
                }
                if (convertTime[19][i] == 1) {
                    tableRows[10].setVisibility(View.VISIBLE);
                }
                if (convertTime[20][i] == 1) {
                    tableRows[10].setVisibility(View.VISIBLE);
                    tableRows[11].setVisibility(View.VISIBLE);
                }
                if (convertTime[21][i] == 1) {
                    tableRows[10].setVisibility(View.VISIBLE);
                    tableRows[11].setVisibility(View.VISIBLE);
                    tableRows[12].setVisibility(View.VISIBLE);
                }
                if (convertTime[22][i] == 1 || convertTime[23][i] == 1) {
                    tableRows[10].setVisibility(View.VISIBLE);
                    tableRows[11].setVisibility(View.VISIBLE);
                    tableRows[12].setVisibility(View.VISIBLE);
                    tableRows[13].setVisibility(View.VISIBLE);
                }
            }
        }

        // 블록의 기본정보 저장 (0행 높이, 가로, 세로)
        float dayHeight = getResources().getDimension(R.dimen.default_text_size);
        int blockWidth = tableRows[1].getChildAt(1).getWidth();
        int blockHeight = tableRows[1].getChildAt(1).getHeight();
        float blockHeight2 = getResources().getDimension(R.dimen.default_text_size);
        // 각각의 시간 표기
        int[][] times = {{0, 0, 1, 0}, {1, 0, 1, 30}, {1, 30, 2, 0}, {2, 0, 2, 30}, {2, 30, 3, 0},
                {3, 0, 3, 30}, {3, 30, 4, 0}, {4, 0, 4, 30}, {4, 30, 5, 0}, {5, 0, 5, 30}, {5, 30, 6, 0},
                {6, 0, 6, 30}, {6, 30, 7, 0}, {7, 0, 7, 30}, {7, 30, 8, 0}, {8, 0, 8, 30}, {8, 30, 9, 0},
                {9, 0, 9, 30}, {9, 30, 10, 0}, {10, 15, 11, 00}, {11, 0, 11, 45}, {11, 45, 12, 30}, {12, 30, 13, 15}, {13, 15, 14, 0}};
        int visible = tableRows[0].getVisibility() == View.GONE ? -1 : 0;

        for (LectureBlock lb : lectureList) {

            int[][] convertTime = lb.convertTime();

            // 클릭 시 작동할 리스너
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LectureBlock lb = ((TimeblockView) view).getLectureBlock();
                    Intent intent = new Intent(getApplicationContext(), LectureInfoActivity.class);
                    intent.putExtra("year", year).putExtra("semester", semester).putExtra("lectureblock", lb);
                    startActivity(intent);
                }
            };

            if (convertTime == null) {
                TimeblockView tb = new TimeblockView(this, lb);
                tb.setOnClickListener(listener);
                timeLinearLayout.addView(tb);
                continue;
            }

            for (int j = 0; j < 5; j++) {
                // 기본 초기화
                int check = 0;
                TimeblockView lectureTextBlock = null;

                for (int k = 0; k < TIME_COUNT; k++) {
                    // j요일, k교시인 경우
                    if (convertTime[k][j] == 1) {
                        if (check == 0) {
                            // 해당 과목 시작시간이면 현재 블록의 x,y좌표와 텍스트를 만들어 새로운 텍스트블럭을 만든다.
                            float x = tableRows[1].getChildAt(j + 1).getX();
                            float y = (times[k][0] + visible) * blockHeight + times[k][1] / 15 * blockHeight2 + dayHeight;
                            lectureTextBlock = new TimeblockView(this, lb, x, y, blockWidth, 50);
                            // 해당 블록을 클릭했을 때
                            lectureTextBlock.setOnClickListener(listener);
                        }
                        check++;
                    } else {
                        // 만약 현재 텍스트 블럭이 만들어져 있으면
                        if (lectureTextBlock != null) {
                            // 높이를 블럭높이 * 교시개수를 곱하여 설정하고, 메인 레이아웃에 추가한다.
                            float height = (times[k - 1][2] + visible) * blockHeight + times[k - 1][3] / 15 * blockHeight2 + dayHeight - lectureTextBlock.getY();
                            lectureTextBlock.setHeight((int) height);
                            timeAbsoluteLayout.addView(lectureTextBlock);
                        }
                        lectureTextBlock = null;
                        check = 0;
                    }
                }
                // 만약 현재 텍스트 블럭이 만들어져 있으면 (~23교시 인 경우 아래의 코드가 실행된다)
                if (lectureTextBlock != null) {
                    float height = (times[23][2] + visible) * blockHeight + times[23][3] / 15 * blockHeight2 + dayHeight - lectureTextBlock.getY();
                    lectureTextBlock.setHeight((int) height);
                    timeAbsoluteLayout.addView(lectureTextBlock);
                }
            }
            // 블럭의 색상을 변경한다
            TimeblockView.setNextColor();

        }
    }

    // SharedPreferences 를 이용하여 현재 설정된 년, 학기를 저장한다.
    private void saveSemester() {
        SharedPreferences.Editor editor = getSharedPreferences("KU", MODE_PRIVATE).edit();
        editor.putString("year", year);
        editor.putString("semester", semester);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_action_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_menu:
                // 강의 추가 액티비티로 넘긴다
                Intent in = new Intent(this, LectureActivity.class);
                // 연, 학기, 현재시간표번호, 강의목록도 같이 넘긴다.
                in.putExtra("year", year);
                in.putExtra("semester", semester);
                in.putExtra("code", code);
                in.putExtra("lectureArray", lectureList);
                // 액티비티 실행한다.
                startActivityForResult(in, 0);
                return true;
            case R.id.changesemister_menu:
                SemesterDialog semesterdialog = new SemesterDialog(this, year, semester);
                semesterdialog.setSemesterClickListener(new SemesterDialog.SemesterClickListener() {
                    @Override
                    public void onClick(String result1, String result2) {
                        // 연과 학기를 단말기에 저장하고, 타이틀을 변경해준다.
                        year = result1;
                        semester = result2;
                        saveSemester();
                        getSupportActionBar().setTitle(getResources().getString(R.string.app_name) + " (" + year + "년 " + semester + "학기)");
                        // 시간표를 새로고침 한다. (학기가 변경 되었음으로)
                        setTimetable();
                    }
                });
                semesterdialog.show();
                return true;
            case R.id.changecolor_menu:
                Intent intent = new Intent(this, ThemeSettingActivity.class);
                startActivityForResult(intent, 0);
                return true;
            case R.id.infomation_menu:
                startActivity(new Intent(this, AppInfoActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
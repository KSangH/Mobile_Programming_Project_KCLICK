package kr.ac.konkuk.timetable;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class LectureActivity extends AppCompatActivity {

    private HashMap<String, String> departmentCode, divCode;         // 학과코드, 이수구분 코드, 이수구분구체 코드
    private Spinner departmentSpinner, divSpinner, infoSpinner, gradeSpinner;   // 각각의 스피너
    private CheckBox peopleCheckBox, timeCheckBox;  // 체크박스 2개
    private ArrayList<LectureBlock> searchLectureList, userLectureList;  // 리스트뷰에 들어갈 어레이리스트
    private LectureAdapter searchLectureAdapter, userLectureAdapter; // 리스트뷰에 들어갈 어댑터
    private ListView searchLectureListView; // 리스트뷰
    private ArrayList<String> departmentArraylist; // 학과명에 들어갈 어레이 리스트
    private String year, semester, code, grade; // 연, 학기, 시간표번호 저장할 스트링
    private Handler handler; // 스레드와 통신하는 핸들러
    private ErrorCodeBlock errorCode; // 스레드 에러여부 확인하는 블록
    private AlertDialog progressDialog; // 스레드 작동 시 다이얼로그
    private TextView messageTextView; // 조회리스트에 메시지 출력하는 텍스트뷰

    private final String[] divKey = {"전체", "전필", "전선", "지필", "지교", "기교", "심교", "교직", "일선"}; // 이수구분
    private final String[] divValue = {"ALL", "B04044", "B04045", "B04061", "B04043", "B0404P", "B04054", "B04047", "B04046"}; // 이수구분 코드
    private final String[] infoKey = {"외국어", "글쓰기", "취창업", "SW", "인성"}; // 기교 세부영역
    private final String[] infoValue = {"B52001", "B52002", "B52003", "B52004", "B52005"}; // 기교 세부영역 코드
    private final String[] infoKey2 = {"학문소양및인성함양", "글로벌인재양성", "사고력증진"}; // 심교 세부영역
    private final String[] gradeKey = {"전체", "1학년", "2학년", "3학년", "4학년"}; // 심교 세부영역

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture);

        // 뒤로가기 버튼 설정
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setLectureInit();
    }

    // 액티비티 실행 시 실행되는 함수
    private void setLectureInit() {

        // 인탠트를 통해 넘어온 데이터
        Intent in = getIntent();
        year = in.getStringExtra("year");
        semester = in.getStringExtra("semester");
        code = in.getStringExtra("code");
        userLectureList = (ArrayList<LectureBlock>) in.getSerializableExtra("lectureArray");

        // 스레드가 종료되면 실행 될 핸들러 처리
        handler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    //  만약 정보를 받아오지 못한 경우 true가 되어 에러 메시지 발생
                    if (errorCode.getErrorCode()) {
                        errorMessage(true);
                        return;
                    }
                    // 학과 스피너에 서버에서 받아온 학과정보 입력
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_dropdown_item, departmentArraylist);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    departmentSpinner.setAdapter(adapter);
                } else {
                    lectureHandler();
                }
                // 다이얼로그 종료
                progressDialog.dismiss();
            }
        };

        // 스레드 다이얼로그 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("서버에서 정보를 받아오는 중...");
        builder.setCancelable(false);
        progressDialog = builder.create();

        // 코드를 저장하기 위한 해쉬맵
        departmentCode = new HashMap<String, String>();
        divCode = new HashMap<String, String>();

        // 각각의 스피너 ID 불러오기
        departmentSpinner = (Spinner) findViewById(R.id.lecture_spinner_department);
        divSpinner = (Spinner) findViewById(R.id.lecture_spinner_div);
        infoSpinner = (Spinner) findViewById(R.id.lecture_spinner_info);
        gradeSpinner = (Spinner) findViewById(R.id.lecture_spinner_grade);

        // 각각의 체크박스 ID 불러오기
        peopleCheckBox = (CheckBox) findViewById(R.id.lecture_checkbox_people);
        timeCheckBox = (CheckBox) findViewById(R.id.lecture_checkbox_time);

        // 리스트뷰 ID 불러오기
        searchLectureListView = (ListView) findViewById(R.id.lecture_listview_lecture);
        ListView userLectureListView = (ListView) findViewById(R.id.lecture_listview_user);

        // 텍스트뷰 ID 불러오기
        messageTextView = (TextView) findViewById(R.id.lecture_textview_message);

        // 리스트뷰를 위한 어레이 및 어댑터 생성
        searchLectureList = new ArrayList<LectureBlock>();
        if (userLectureList == null) {
            userLectureList = new ArrayList<LectureBlock>();
        }
        searchLectureAdapter = new LectureAdapter(searchLectureList);
        userLectureAdapter = new LectureAdapter(userLectureList);
        userLectureAdapter.setUser(true);

        // 리스트 클릭 시 작동 될 함수
        searchLectureListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final LectureBlock lb = searchLectureList.get(i);
                // 클릭 된 강의의 리스트 다이얼로그를 출력
                AlertDialog.Builder alertdialog = new AlertDialog.Builder(adapterView.getContext());
                alertdialog.setItems(new String[]{"강의 계획서", "강의 정보 (학년별인원)", "강의 추가", "취소"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) { // 강의계획서
                            openActivity(0, lb);
                        } else if (i == 1) { // 강의정보
                            openActivity(1, lb);
                        } else if (i == 2) { // 강의추가
                            addLecture(lb);
                        }
                    }
                }).show();

            }
        });

        userLectureListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int index, long l) {
                final LectureBlock lb = userLectureList.get(index);
                // 클릭 된 강의의 리스트 다이얼로그를 출력
                AlertDialog.Builder alertdialog = new AlertDialog.Builder(adapterView.getContext());
                alertdialog.setItems(new String[]{"강의 계획서", "강의 정보 (학년별인원)", "강의 삭제", "취소"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {  // 강의계획서
                            openActivity(0, lb);
                        } else if (i == 1) {  // 강의정보
                            openActivity(1, lb);
                        } else if (i == 2) {  // 강의삭제
                            removeLecture(lb);
                        }
                    }
                }).show();
            }
        });

        // 리스트뷰에 어댑터 설정
        searchLectureListView.setAdapter(searchLectureAdapter);
        userLectureListView.setAdapter(userLectureAdapter);

        // 학과 정보를 받아오는 함수
        setDepartmentCode();

        // 이수 구분 코드 지정 및 이수 구분 지정 함수
        setDivCode();
    }

    // 검색 스레드 종료후 실행할 함수.
    private void lectureHandler() {
        if (errorCode.getErrorCode()) {
            // 에러 메시지를 출력한다.
            errorMessage(false);
            return;
        }
        // 스레드에서 에러가 없었으면 아래의 내용을 실행한다.
        // 사이즈가 0인 경우 조회된 내용이 없습니다.
        if (searchLectureList.size() == 0) {
            messageTextView.setVisibility(View.VISIBLE);
            messageTextView.setText("조회된 강의가 없습니다.");
        } else {
            messageTextView.setVisibility(View.GONE);
        }

        // 인원 초과 제외
        if (peopleCheckBox.isChecked()) {
            for (int i = searchLectureList.size() - 1; i >= 0; i--) {
                if (searchLectureList.get(i).isFull()) {
                    searchLectureList.remove(i);
                }
            }
        }

        // 시간 중복 제외하기
        if (timeCheckBox.isChecked()) {
            for (int i = searchLectureList.size() - 1; i >= 0; i--) {
                if (isDuplicate(searchLectureList.get(i).convertTime())) {
                    searchLectureList.remove(i);
                }
            }
        }

        // 리스트가 변경되었음을 알리고 리스트뷰를 맨위로 올리기
        searchLectureAdapter.notifyDataSetChanged();
        searchLectureListView.smoothScrollToPosition(0);

    }


    // 학과 정보를 받아오는 함수
    private void setDepartmentCode() {
        // 학과 이름을 넣을 배열과, 에러발생여부를 확인하는 에러코드블럭 생성
        departmentArraylist = new ArrayList<String>();
        errorCode = new ErrorCodeBlock();
        progressDialog.show();
        try {
            // 스레드 생성 후 시작 (SearchingTask Type 0)
            Thread initThread = new Thread(new SearchingTask(handler, departmentCode, departmentArraylist, errorCode));
            initThread.start();
        } catch (Exception e) {
            // 예외 발생시 에러 메시지 발생
            errorMessage(true);
            return;
        }
    }

    // 이수 구분 코드 지정 및 이수 구분 지정 함수
    private void setDivCode() {
        // HashMap에 각 키와 value를 추가 (이수구분 + 영역 같이 추가)
        for (int i = 0; i < divKey.length; i++) {
            divCode.put(divKey[i], divValue[i]);
            if (i < infoKey.length)
                divCode.put(infoKey[i], infoValue[i]);
        }

        // 이수구분(div~) 스피너 설정
        ArrayAdapter<String> divAdapter = new ArrayAdapter<String>(this, R.layout.spinner_dropdown_item, new ArrayList<String>(Arrays.asList(divKey)));
        divAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        divSpinner.setAdapter(divAdapter);
        divSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getSelectedItem().toString().equals("기교")) {
                    // 이수구분 = 기교의 경우, 총 5개의 영역으로 분리, 학과 정보 스피너를 GONE, 영역 스피너를 VISIBLE 처리
                    departmentSpinner.setVisibility(View.GONE);
                    infoSpinner.setVisibility(View.VISIBLE);
                    ArrayAdapter<String> infoAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_dropdown_item, new ArrayList<String>(Arrays.asList(infoKey)));
                    infoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    infoSpinner.setAdapter(infoAdapter);

                } else if (adapterView.getSelectedItem().toString().equals("심교")) {
                    // 이수구분 = 심교의 경우, 총 3개의 영역으로 분리, 학과 정보 스피너를 GONE, 영역 스피너를 VISIBLE 처리
                    departmentSpinner.setVisibility(View.GONE);
                    infoSpinner.setVisibility(View.VISIBLE);
                    ArrayAdapter<String> infoAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_dropdown_item, new ArrayList<String>(Arrays.asList(infoKey2)));
                    infoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    infoSpinner.setAdapter(infoAdapter);
                } else {
                    // 그 외를 선택 시에는 학과 정보 스피너를 VISIBLE, 영역 스피너를 GONE 처리
                    departmentSpinner.setVisibility(View.VISIBLE);
                    infoSpinner.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<String> gradeAdapter = new ArrayAdapter<String>(this, R.layout.spinner_dropdown_item, new ArrayList<String>(Arrays.asList(gradeKey)));
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gradeSpinner.setAdapter(gradeAdapter);

        gradeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                grade = i + "";
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        if(findViewById(R.id.lecture_layout_user).getVisibility() == View.GONE){
            zoomClicked(findViewById(R.id.lecture_textview_zoom));
        } else {
            AlertDialog.Builder alertdialog = new AlertDialog.Builder(this);
            alertdialog.setTitle("알림")
                    .setMessage("저장하시겠습니까?")
                    .setPositiveButton("저장", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            saveLecture();
                            setResult(RESULT_OK);
                            finish();
                        }
                    })
                    .setNegativeButton("저장하지 않기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            setResult(RESULT_OK);
                            finish();
                        }
                    }).show();
        }
    }

    public void zoomClicked(View view){
        LinearLayout ll = findViewById(R.id.lecture_layout_user);
        if(ll.getVisibility() != View.GONE){
            ((TextView)view).setText("원래대로");
            ll.setVisibility(View.GONE);
        } else {
            ((TextView)view).setText("크게보기");
            ll.setVisibility(View.VISIBLE);
        }


    }

    // 조회 버튼 클릭 시 작동 함수
    public void onClicked(View view) {
        try {
            // 연, 학기, 학년, 그 외 파라미터
            String params_year = year, params_semester = "B0101" + semester, params_grade = grade, params_etc1, params_etc2, params_etc3;
            // 스레드 선언
            Thread searchThread = null;
            // 에러블록 선언
            errorCode = new ErrorCodeBlock();

            switch (view.getId()) {
                case R.id.lecture_button_condition:

                    params_etc1 = ((EditText) findViewById(R.id.lecture_edittext_lecturename)).getText().toString();
                    // 강의명이 적혀있으면 조회
                    if (!params_etc1.equals("")) {
                        searchThread = new Thread(new SearchingTask(handler, searchLectureList, errorCode, params_year, params_semester, params_grade, params_etc1));
                        break;
                    }
                    
                    // 이수구분 코드
                    String div = divSpinner.getSelectedItem().toString();

                    // 전체 + 전체대학 조합인 경우, 서버 과부하가 걸릴 수 있으므로 조회하지 않음.
                    if (div.equals("전체") && departmentSpinner.getSelectedItem().toString().equals("전체대학")) {
                        AlertDialog.Builder alertdialog = new AlertDialog.Builder(this);
                        alertdialog.setTitle("알림")
                                .setMessage("이수구분을 전체로 조회 시, 학과를 선택해주세요.")
                                .setPositiveButton("확인", null).show();
                        return;
                    }

                    // 기교, 심교, 나머지로 구분하여 조회
                    if (div.equals("기교")) {
                        // 기교의 경우 외국어/글쓰기/SW 등으로 조회
                        params_etc1 = departmentCode.get("전체대학");   // 부서코드
                        params_etc2 = divCode.get(div);                // 이수구분코드
                        params_etc3 = divCode.get(infoSpinner.getSelectedItem().toString()); // 영역코드
                        searchThread = new Thread(new SearchingTask(handler, searchLectureList, errorCode, params_year, params_semester, params_grade, params_etc1, params_etc2, params_etc3));

                    } else if (div.equals("심교")) {
                        // 심교의 경우 영역으로 조회
                        params_etc1 = departmentCode.get("전체대학");  // 부서코드
                        params_etc2 = divCode.get(div);              // 이수구분코드
                        params_etc3 = infoSpinner.getSelectedItem().toString(); // 영역텍스트
                        searchThread = new Thread(new SearchingTask(handler, searchLectureList, errorCode, params_year, params_semester, params_grade, params_etc1, params_etc2, params_etc3, "Key"));

                    } else {
                        // 그외의 영역의 조회
                        params_etc1 = departmentCode.get(departmentSpinner.getSelectedItem().toString()); // 부서코드
                        params_etc2 = divCode.get(div); // 이수구분코드
                        searchThread = new Thread(new SearchingTask(handler, searchLectureList, errorCode, params_year, params_semester, params_grade, params_etc1, params_etc2));
                    }
                    break;
                    
            }

            // 검색 스레드가 설정된 경우
            if (searchThread != null) {
                // 강의리스트를 초기화, 스레드를 시작
                searchLectureList.clear();
                progressDialog.show();
                searchThread.start();
            }

        } catch (Exception e) {
            errorMessage(false);
        }

    }

    // 현재 사용자 시간표와 중복여부 확인하기
    private boolean isDuplicate(int[][] convertTime) {
        // 기존 유저 시간표와 비교함. checktime이 true이면 시간이 겹치는 것임.
        for (int i = 0; i < userLectureList.size(); i++) {
            if (userLectureList.get(i).checkTime(convertTime)) {
                return true;
            }
        }
        return false;
    }

    // 강의 계획서, 강의 정보로 액티비티 전환해주는 함수
    private void openActivity(int number, LectureBlock lb) {
        // 0인 경우 강의 계획서로 연결, 1인 경우 강의 정보로 연결된다.
        if (number == 0) {
            Intent intent = new Intent(this, LecturePlanActivity.class);
            intent.putExtra("year", year).putExtra("semester", semester).putExtra("number", lb.getNumber());
            startActivity(intent);
        } else if (number == 1) {
            Intent intent = new Intent(this, LectureInfoActivity.class);
            intent.putExtra("year", year).putExtra("semester", semester).putExtra("lectureblock", lb);
            startActivity(intent);
        }
    }

    // 강의 추가 시, 호출되는 함수
    private void addLecture(LectureBlock lb) {

        int[][] convertTime = lb.convertTime();

        // 시간이 정상적이지 않음 (월-금, 1-18교시가 아니면 null)
        if (convertTime == null) {
            AlertDialog.Builder alertdialog = new AlertDialog.Builder(this);
            alertdialog.setTitle("안내")
                    .setMessage("월-금, 1-19교시가 아닌 시간표의 경우, 추가할 수 없습니다.")
                    .setPositiveButton("확인", null).show();
            return;
        }

        // 중복이면 추가하지 않음
        if (isDuplicate(convertTime)) {
            AlertDialog.Builder alertdialog = new AlertDialog.Builder(this);
            alertdialog.setTitle("안내")
                    .setMessage("강의 시간이 중복된 경우, 추가할 수 없습니다.")
                    .setPositiveButton("확인", null).show();
            return;
        }

        // 시간표 충돌 오류도 없고, 월-금, 1-18교시 정상적인 시간이면, 리스트에 추가하고
        userLectureList.add(lb);

        // 과목번호 순으로 정렬
        userLectureList.sort(new Comparator<LectureBlock>() {
            @Override
            public int compare(LectureBlock t1, LectureBlock t2) {
                return t1.getNumber().compareTo(t2.getNumber());
            }
        });

        // 수정되었음을 알린다.
        userLectureAdapter.notifyDataSetChanged();
    }

    // 강의 삭제 시, 호출되는 함수
    private void removeLecture(LectureBlock lb) {
        if (userLectureList.contains(lb))
            userLectureList.remove(lb);
        userLectureAdapter.notifyDataSetChanged();
    }

    // 강의를 단말기에 저장하는 함수
    private void saveLecture() {
        String fileName = year + "-" + semester + "-" + code + ".txt";
        try {
            FileOutputStream fileOutputStream = openFileOutput(fileName, MODE_PRIVATE);
            for (int i = 0; i < userLectureList.size(); i++) {
                fileOutputStream.write(userLectureList.get(i).toString().getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 에러 메시지 발생 시, 호출되는 함수
    private void errorMessage(boolean init) {

        // 만약 init 과정에서 에러가 발생 시 액티비티 종료.
        if (init) {
            Toast.makeText(this, "네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // 리스트 뷰 위치에 메시지 출력
            messageTextView.setVisibility(View.VISIBLE);
            messageTextView.setText("네트워크 상태를 확인해주세요.");

            // 네트워크 오류 다이얼로그 띄우기
            AlertDialog.Builder alertdialog = new AlertDialog.Builder(this);
            alertdialog.setTitle("네트워크 오류")
                    .setMessage("네트워크 상태를 확인해주세요.")
                    .setPositiveButton("확인", null).show();

            // 네트워크 에러 발생시, 조회된 강의 리스트 초기화 및 변경 알림
            searchLectureList.clear();
            searchLectureAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lecture_action_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // 뒤로가기 버튼 시 종료한다.
                onBackPressed();
                return true;
            case R.id.save_menu:
                // 저장하고 종료한다.
                saveLecture();
                setResult(RESULT_OK);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

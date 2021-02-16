package kr.ac.konkuk.timetable.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;
import kr.ac.konkuk.timetable.R;
import kr.ac.konkuk.timetable.lecture.LectureBlock;

import static android.content.Context.MODE_PRIVATE;

public class TimeblockView extends TextView {

    private static int count = 0; // 현재 카운트의 위치 (공용)
    private static int[] colors = new int[15];
    static final int UP = 0, DOWN = 1; // 상수 설정
    private LectureBlock lectureBlock = null;

    // TableLayout에 들어갈 기본 생성자
    public TimeblockView(Context context) {
        super(context);
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
    }

    // 배경화면용 생성자
    public TimeblockView(Context context, String text) {
        this(context);
        setBackgroundResource(R.drawable.main_table_border);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setText(text);
    }

    // 이러닝용 생성자
    public TimeblockView(Context context, LectureBlock lb) {
        this(context);
        lectureBlock = lb;
        setPadding(15,15,5,15);
        setBackgroundResource(R.drawable.main_table_border);
        setGravity(Gravity.CENTER_VERTICAL);
        setText(lb.getTitle() + "/" + lb.getProfessor());
    }

    // 강의명과 교수명을 표시하는 텍스트뷰의 기본 생성자
    public TimeblockView(Context context, LectureBlock lb, float x, float y, int width, int height) {
        this(context);
        lectureBlock = lb;

        // paddingleft 설정
        setPadding(3, 0, 0, 0);

        // x + 0.5dp(경계) 후 설정, y 좌표 설정
        setX(x + getResources().getDimension(R.dimen.timetable_column_border));
        setY(y);

        // 양옆 경계를 0.5dp * 2 제외하고 width 설정, height 설정
        setWidth(width - getResources().getDimensionPixelSize(R.dimen.timetable_column_border) * 2);
        setHeight(height);

        // 텍스트 관련 설정
        setText(lb.getTitle() + "\n" + lb.getProfessor());
        setTextColor(Color.WHITE);

        // 현재 인덱스가 count인 색상으로 설정
        setBackgroundColor(colors[count]);
    }

    public LectureBlock getLectureBlock() {
        return lectureBlock;
    }

    static public void setNextColor() {
        // 다음 컬러로 넘김
        count = (count + 1) % colors.length;
    }

    static public void setInitColor(Context context) {
        // 인덱스 초기화
        SharedPreferences sharedPreferences = context.getSharedPreferences("KU-COLOR", MODE_PRIVATE);
        for (int i = 0; i < 15; i++) {
            colors[i] = Color.parseColor(sharedPreferences.getString("color" + (i + 1), ""));
        }
        count = 0;
    }
}

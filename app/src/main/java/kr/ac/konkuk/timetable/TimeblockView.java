package kr.ac.konkuk.timetable;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

public class TimeblockView extends TextView {

    private static int count = 0; // 현재 카운트의 위치 (공용)
    private static int[] colors = { // 색상 모음 
            R.color.timetable_color_1, R.color.timetable_color_2, R.color.timetable_color_3,
            R.color.timetable_color_4, R.color.timetable_color_5, R.color.timetable_color_6,
            R.color.timetable_color_7, R.color.timetable_color_8, R.color.timetable_color_9,
            R.color.timetable_color_10, R.color.timetable_color_11, R.color.timetable_color_12,
            R.color.timetable_color_13, R.color.timetable_color_14, R.color.timetable_color_15};
    static final int UP = 0, DOWN = 1; // 상수 설정
    private LectureBlock lectureBlock = null;
    
    // TableLayout에 들어갈 기본 생성자
    public TimeblockView(Context context) {
        super(context);
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
    }

    // 배경화면용 생성자
    public TimeblockView(Context context, String text, int location) {
        this(context);
        if(location == UP)
            setBackgroundResource(R.drawable.main_table_border_up);
        else if(location == DOWN)
            setBackgroundResource(R.drawable.main_table_border_down);
        else
            setBackgroundResource(R.drawable.main_table_border);
        setGravity(Gravity.CENTER);
        setText(text);
    }

    // 강의명과 교수명을 표시하는 텍스트뷰의 기본 생성자
    public TimeblockView(Context context, LectureBlock sb, float x, float y, int width, int height) {
        this(context);
        lectureBlock = sb;
        
        // paddingleft 설정
        setPadding(3, 0, 0, 0);
        
        // x + 0.5dp(경계) 후 설정, y 좌표 설정
        setX(x + getResources().getDimension(R.dimen.timetable_column_border));
        setY(y);
        
        // 양옆 경계를 0.5dp * 2 제외하고 width 설정, height 설정
        setWidth(width - getResources().getDimensionPixelSize(R.dimen.timetable_column_border) * 2);
        setHeight(height);
        
        // 텍스트 관련 설정
        setText(sb.getTitle()+"\n"+sb.getProfessor());
        setTextColor(Color.WHITE);
        
        // 현재 인덱스가 count인 색상으로 설정
        setBackgroundColor(getResources().getColor(colors[count], context.getTheme()));
    }

    public LectureBlock getLectureBlock(){
        return lectureBlock;
    }

    static public void setNextColor() {
        // 다음 컬러로 넘김
        count = (count + 1) % colors.length;
    }

    static public void setInitColor() {
        // 인덱스 초기화
        count = 0;
    }
}

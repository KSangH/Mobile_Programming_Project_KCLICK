package kr.ac.konkuk.timetable.lecture;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import kr.ac.konkuk.timetable.R;

import java.util.ArrayList;

public class LectureAdapter extends BaseAdapter {

    private ArrayList<LectureBlock> lectureList;
    private boolean user = false;
    private boolean grade = true;

    public LectureAdapter(ArrayList<LectureBlock> lectureList) {
        this.lectureList = lectureList;
    }

    @Override
    public int getCount() {
        return lectureList.size();
    }

    @Override
    public LectureBlock getItem(int i) {
        return lectureList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Context context = viewGroup.getContext();

        // 뷰가 없으면 지정해준다.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.adapter_lecture_list, viewGroup, false);
        }

        // 현재 i번쨰의 블록을 가져온다.
        LectureBlock lb = getItem(i);

        // 각각의 객체를 불러온다.
        TextView numberText = (TextView) view.findViewById(R.id.adapter_lecture_textview_number);
        TextView titleText = (TextView) view.findViewById(R.id.adapter_lecture_textview_title);
        TextView proText = (TextView) view.findViewById(R.id.adapter_lecture_textview_professor);
        TextView timeText = (TextView) view.findViewById(R.id.adapter_lecture_textview_time);
        TextView countText = (TextView) view.findViewById(R.id.adapter_lecture_textview_count);
        TextView suguniText = (TextView) view.findViewById(R.id.adapter_lecture_textview_suguni);

        // 텍스트를 맞게 설정해준다.
        numberText.setText(lb.getNumber());
        titleText.setText(lb.getTitle());
        proText.setText(lb.getProfessor() + "   " + lb.getGrade() + "   " + lb.getDiv() + "   " + lb.getCredit());
        timeText.setText(lb.getTime());

        // 만약 유저리스트의 경우에는 인원수가 나오지 않는다.
        if (user) {
            countText.setVisibility(View.GONE);
            suguniText.setVisibility(View.GONE);
        } else {
            // 만약 인원수가 초과된 경우 빨간색, 아닌경우 파란색으로 표시한다.
            if (lb.isFull()) {
                countText.setTextColor(Color.RED);
            } else {
                countText.setTextColor(Color.BLUE);
            }

            // 수구니가 초과된 경우 빨갠색, 아닌경우 파란색으로 표시한다.
            if (lb.isSuguniFull()) {
                suguniText.setTextColor(Color.RED);
            } else {
                suguniText.setTextColor(Color.BLUE);
            }
            // 그렇지 않은 경우에는 인원수를 출력한다.
            countText.setText(lb.getCurrent() + "/" + lb.getTotal());

            // 정보가 없는 경우 N/A 로 표기한다.
            String rate = "N/A";

            if (lb.getTotal() > 0)
                rate = String.format("%.2f:1", (double) lb.getSuguni() / lb.getTotal());

            // 수구니 인원과 경쟁률을 표시한다.
            suguniText.setText(lb.getSuguni() + "/" + lb.getTotal() + "(" + rate + ")");
        }

        // 전체학년으로 조회 시 수구니 정보 삭제
        if (grade)
            suguniText.setVisibility(View.GONE);
        else
            suguniText.setVisibility(View.VISIBLE);

        return view;
    }

    // 사용자시간표인지 종합강의시간표인지 구분하는 코드
    public void setUser(boolean b) {
        this.user = b;
    }

    // 조회된 학년 설정
    public void setGrade(String b) {
        if (b.equals("0"))
            this.grade = true;
        else this.grade = false;
    }

}

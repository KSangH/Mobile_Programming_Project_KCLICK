package kr.ac.konkuk.timetable.main;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import kr.ac.konkuk.timetable.R;

public class SemesterDialog extends Dialog {

    private EditText yearEditText, semesterEditText;
    private SemesterClickListener listener;
    private TextView errorTextView;

    public SemesterDialog(Context context, String year, String semester){
        super(context);
        setContentView(R.layout.dialog_semester);
        
        // EditText ID 불러오기
        yearEditText = (EditText)findViewById(R.id.dialog_semester_edittext_year);
        semesterEditText = (EditText)findViewById(R.id.dialog_semester_edittext_semester);
        errorTextView = (TextView)findViewById(R.id.dialog_semester_textview_error);
        
        // 각각의 텍스트 설정
        yearEditText.setText(year);
        semesterEditText.setText(semester);

        // 버튼 별 기능 설정
        Button okButton = (Button) findViewById(R.id.dialog_semester_button_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 각각의 내용을 숫자로 변경
                int y = Integer.parseInt(yearEditText.getText().toString());
                int s = Integer.parseInt(semesterEditText.getText().toString());

                // 범위안에 있으면 리스너 호출 후 종료
                if( 2017 <= y && y <= 2025 && (s==1|| s==2)){
                    if(listener != null)
                        listener.onClick(Integer.toString(y),Integer.toString(s));
                    dismiss();
                } else {
                    // 숫자를 잘못입력한 경우 에러메시지 출력
                    errorTextView.setVisibility(View.VISIBLE);
                }
            }
        });

        // 취소버튼 클릭시 다이얼로그 종료
        Button cancelButton = (Button) findViewById(R.id.dialog_semester_button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    public interface SemesterClickListener{
        public void onClick(String result1, String result2);
    }

    public void setSemesterClickListener(SemesterClickListener listener){
        this.listener = listener;
    }

}

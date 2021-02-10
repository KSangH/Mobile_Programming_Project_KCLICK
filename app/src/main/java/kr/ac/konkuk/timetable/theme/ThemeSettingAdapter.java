package kr.ac.konkuk.timetable.theme;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import kr.ac.konkuk.timetable.R;

public class ThemeSettingAdapter extends RecyclerView.Adapter<ThemeSettingAdapter.ThemeSettingViewHolder> {

    private String[] colorList;
    private ThemeSettingListener listener;

    // 생성자
    public ThemeSettingAdapter(String[] colorList) {
        this.colorList = colorList;
    }

    // 터치 리스너 설정
    public void setThemeSettingListener(ThemeSettingListener listener) {
        this.listener = listener;
    }

    // 뷰 설정
    @NonNull
    @Override
    public ThemeSettingViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_theme_setting, viewGroup, false);
        ThemeSettingViewHolder viewHolder = new ThemeSettingViewHolder(view);
        return viewHolder;
    }

    // 뷰 내부 수정
    @Override
    public void onBindViewHolder(@NonNull ThemeSettingViewHolder holder, int position) {
        holder.textView.setText("색상 " + (position + 1));
        holder.imageView.setBackgroundColor(Color.parseColor(colorList[position]));
    }

    // 아이템 수
    @Override
    public int getItemCount() {
        return colorList.length;
    }
    
    // 커스텀 홀더 클래스 생성
    class ThemeSettingViewHolder extends RecyclerView.ViewHolder {

        protected TextView textView;
        protected ImageView imageView;

        public ThemeSettingViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.adapter_theme_textview);
            imageView = view.findViewById(R.id.adapter_theme_imageview);

            // 터치 시 작동할 함수
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                            listener.onClick(view, getAdapterPosition());
                        }
                    }
                }
            });
        }
    }

    
    // 리스너 인터페이스
    interface ThemeSettingListener {
        public void onClick(View view, int position);
    }


}

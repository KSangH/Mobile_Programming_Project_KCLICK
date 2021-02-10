package kr.ac.konkuk.timetable.theme;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import kr.ac.konkuk.timetable.R;

public class ThemeSettingAdapter extends BaseAdapter {

    private String[] colorList;

    public ThemeSettingAdapter(String[] colorList) {
        this.colorList = colorList;
    }

    @Override
    public int getCount() {
        return colorList.length;
    }

    @Override
    public String getItem(int i) {
        return colorList[i];
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
            view = inflater.inflate(R.layout.adapter_theme_setting, viewGroup, false);
        }

        // 현재 i번쨰의 블록을 가져온다.
        String color = getItem(i);

        // 각각의 객체를 불러온다.
        TextView textView = view.findViewById(R.id.adapter_theme_textview);
        ImageView imageView = view.findViewById(R.id.adapter_theme_imagebutton);

        textView.setText("색상 " + (i + 1));
        imageView.setBackgroundColor(Color.parseColor(color));

        return view;
    }


}

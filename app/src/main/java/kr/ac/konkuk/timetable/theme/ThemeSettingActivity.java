package kr.ac.konkuk.timetable.theme;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.azeesoft.lib.colorpicker.ColorPickerDialog;
import kr.ac.konkuk.timetable.R;

public class ThemeSettingActivity extends AppCompatActivity {

    private String[] colors = new String[15];
    ThemeSettingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_setting);

        // 뒤로가기 추가 및 세로 고정
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // 단말기에 저장된 테마 정보 불러오기
        SharedPreferences sharedPreferences = getSharedPreferences("KU-COLOR", MODE_PRIVATE);
        for (int i = 0; i < 15; i++) {
            colors[i] = sharedPreferences.getString("color" + (i + 1), "");
        }

        // 리사이클러뷰 어댑터 생성
        adapter = new ThemeSettingAdapter(colors);
        
        // 리사이클러뷰에 레아이웃 설정
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.theme_recyclerview_main);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // 터치 시 작동하는 리스너 설정
        adapter.setThemeSettingListener(new ThemeSettingAdapter.ThemeSettingListener() {
            @Override
            public void onClick(View view, int position) {
                ColorPickerDialog colorPickerDialog = ColorPickerDialog.createColorPickerDialog(view.getContext());
                colorPickerDialog.setOnColorPickedListener(new ColorPickerDialog.OnColorPickedListener() {
                    @Override
                    public void onColorPicked(int color, String hexVal) {
                        colors[position] = hexVal;
                        adapter.notifyDataSetChanged();
                    }
                });
                colorPickerDialog.setLastColor(Color.parseColor(colors[position]));
                colorPickerDialog.show();
            }
        });

        recyclerView.setAdapter(adapter);
    }

    // 저장버튼을 눌렀을 때, 저장
    public void onSaveClicked(View v) {
        SharedPreferences.Editor editor = getSharedPreferences("KU-COLOR", MODE_PRIVATE).edit();
        for(int i=0; i<15; i++){
            editor.putString("color"+(i+1), colors[i]);
        }
        editor.commit();
        setResult(RESULT_OK);
        finish();
    }

    // 초기화 버튼을 눌렀을 때 초기값으로 변경
    public void onInitClicked(View v){
        String[] colorArray = {"#ba6b6c", "#c48b9f", "#9c64a6", "#a094b7", "#6f79a8",
                "#8aacc8", "#4ba3c7", "#81b9bf", "#4f9a94", "#97b498",
                "#94af76", "#808e95", "#8c7b75", "#ca9b52", "#cb9b8c"};
        for(int i=0; i<15; i++){
            colors[i] = colorArray[i];
        }
        adapter.notifyDataSetChanged();
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
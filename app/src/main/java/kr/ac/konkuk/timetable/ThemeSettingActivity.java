package kr.ac.konkuk.timetable;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.azeesoft.lib.colorpicker.ColorPickerDialog;

public class ThemeSettingActivity extends AppCompatActivity {

    private String[] colors = new String[15];
    ThemeSettingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_setting);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        SharedPreferences sharedPreferences = getSharedPreferences("KU-COLOR", MODE_PRIVATE);
        for (int i = 0; i < 15; i++) {
            colors[i] = sharedPreferences.getString("color" + (i + 1), "");
        }

        adapter = new ThemeSettingAdapter(colors);
        ListView listView = (ListView)findViewById(R.id.theme_listview_main);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ColorPickerDialog colorPickerDialog = ColorPickerDialog.createColorPickerDialog(adapterView.getContext());
                colorPickerDialog.setOnColorPickedListener(new ColorPickerDialog.OnColorPickedListener() {
                    @Override
                    public void onColorPicked(int color, String hexVal) {
                        colors[i] = hexVal;
                        adapter.notifyDataSetChanged();
                    }
                });
                colorPickerDialog.setLastColor(Color.parseColor(colors[i]));
                colorPickerDialog.show();
            }
        });
        listView.setAdapter(adapter);
    }

    public void onSaveClicked(View v) {
        SharedPreferences.Editor editor = getSharedPreferences("KU-COLOR", MODE_PRIVATE).edit();
        for(int i=0; i<15; i++){
            editor.putString("color"+(i+1), colors[i]);
        }
        editor.commit();
        setResult(RESULT_OK);
        finish();
    }

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
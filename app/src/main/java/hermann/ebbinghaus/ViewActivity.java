package hermann.ebbinghaus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;

import androidx.appcompat.app.AppCompatActivity;

public class ViewActivity extends AppCompatActivity {

	private CalendarView calendarView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view);
		calendarView = (CalendarView) findViewById(R.id.calendarView);
		calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
			public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
				String dateStr = year + String.format("%02d", month + 1) + String.format("%02d", dayOfMonth);
				Log.e("@@@@@", dateStr);
				Intent intent = new Intent();
				intent.setClass(getApplicationContext(), MainActivity.class);
				intent.putExtra("dateStr", dateStr);
				startActivity(intent);
			}
		});
	}

}
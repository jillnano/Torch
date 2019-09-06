package hermann.ebbinghaus;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter.ViewBinder;

public class ListViewBinder implements ViewBinder {

	public boolean setViewValue(View arg0, Object arg1, String arg2) {
        if((arg0 instanceof ImageView) && (arg1 instanceof Bitmap)) {  
            ImageView imageView = (ImageView) arg0;  
            Bitmap bmp = (Bitmap) arg1;  
            imageView.setImageBitmap(bmp);  
            return true;  
        }  
        return false;  
	}

}

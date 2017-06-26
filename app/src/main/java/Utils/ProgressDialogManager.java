package Utils;


import android.app.ProgressDialog;
import android.content.Context;

import com.example.cosmin.kdocscanner.OCRResultActivity;

public class ProgressDialogManager {

    public static ProgressDialog initiateProgressDialog(String message, Context context){
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setIndeterminate(true);
        pd.setMessage(message);
        pd.setCancelable(true);
        pd.show();

        return pd;
    }

    public static void destroyProgressDialog(ProgressDialog pd){
        pd.dismiss();
    }
}

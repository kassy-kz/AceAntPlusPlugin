package orz.kassy.aceantplusextension;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by kashimoto on 16/07/20.
 */
public class PrefUtils {

    /**
     * ハートレートモニターデバイスのIDを保管
     * @param context コンテキスト
     * @return int
     */
    public static int loadPrefHeartRateDeviceId(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        int id = sharedPref.getInt(context.getResources().getString(R.string.pref_heartrate_device_id), -1);
        return id;
    }
    public static void savePrefHeartRateDeviceId(Context context, int id) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPref.edit().putInt(context.getResources().getString(R.string.pref_heartrate_device_id), id).apply();
        return;
    }

    /**
     * ケイデンスセンサーのIDを保管
     * @param context
     * @return
     */
    public static int loadPrefCadenceDeviceId(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        int id = sharedPref.getInt(context.getResources().getString(R.string.pref_cadence_device_id), -1);
        return id;
    }
    public static void savePrefCadenceDeviceId(Context context, int id) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPref.edit().putInt(context.getResources().getString(R.string.pref_cadence_device_id), id).apply();
        return;
    }

}

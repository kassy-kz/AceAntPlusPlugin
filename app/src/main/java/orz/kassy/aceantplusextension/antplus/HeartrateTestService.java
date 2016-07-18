package orz.kassy.aceantplusextension.antplus;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusCommonPcc;
import com.dsi.ant.plugins.antplus.pccbase.AntPlusLegacyCommonPcc;
import com.dsi.ant.plugins.antplus.pccbase.MultiDeviceSearch;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;

import java.math.BigDecimal;
import java.util.EnumSet;

public class HeartrateTestService extends Service {

    private static final String TAG = "TestService";
    protected PccReleaseHandle<AntPlusHeartRatePcc> releaseHandle = null;
    private AntPlusHeartRatePcc hrPcc = null;


    public HeartrateTestService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int ret = super.onStartCommand(intent, flags, startId);
        requestAccessToPcc(intent);
        return ret;
    }

    private void requestAccessToPcc(Intent intent) {
        if (intent.hasExtra(Activity_MultiDeviceSearchSampler.EXTRA_KEY_MULTIDEVICE_SEARCH_RESULT)) {
            // device has already been selected through the multi-device search
            MultiDeviceSearch.MultiDeviceSearchResult result = intent
                    .getParcelableExtra(Activity_MultiDeviceSearchSampler.EXTRA_KEY_MULTIDEVICE_SEARCH_RESULT);
            // 実験
            releaseHandle = AntPlusHeartRatePcc.requestAccess(this, result.getAntDeviceNumber(), 0,
                    base_IPluginAccessResultReceiver, base_IDeviceStateChangeReceiver);
//            releaseHandle = AntPlusHeartRatePcc.requestAccess(this, 23756, 0,
//                    base_IPluginAccessResultReceiver, base_IDeviceStateChangeReceiver);
            Log.i(TAG, "result : " + result.getDeviceDisplayName() + ", " + result.getAntDeviceType() + ", " + result.getAntDeviceNumber());
        } else {
            Log.w(TAG, "errorrrrrrrrrrr");
        }
    }


    protected AntPluginPcc.IPluginAccessResultReceiver<AntPlusHeartRatePcc> base_IPluginAccessResultReceiver =
            new AntPluginPcc.IPluginAccessResultReceiver<AntPlusHeartRatePcc>() {
                @Override
                public void onResultReceived(AntPlusHeartRatePcc result, RequestAccessResult resultCode,
                                             DeviceState initialDeviceState) {
                    switch (resultCode) {
                        case SUCCESS: {
                            Log.i(TAG, "onResultReceived : success");
                            hrPcc = result;
                            subscribeToHrEvents();

                            break;
                        }
                        default: {
                            Log.i(TAG, "onResultReceived : " + resultCode);
                        }
                    }
                }
            };

    //Receives state changes and shows it on the status display line
    protected AntPluginPcc.IDeviceStateChangeReceiver base_IDeviceStateChangeReceiver = new AntPluginPcc.IDeviceStateChangeReceiver() {
        @Override
        public void onDeviceStateChange(final DeviceState newDeviceState) {
        }
    };


    public void subscribeToHrEvents() {
        hrPcc.subscribeHeartRateDataEvent(new AntPlusHeartRatePcc.IHeartRateDataReceiver() {
            @Override
            public void onNewHeartRateData(final long estTimestamp, EnumSet<EventFlag> eventFlags,
                                           final int computedHeartRate, final long heartBeatCount,
                                           final BigDecimal heartBeatEventTime, final AntPlusHeartRatePcc.DataState dataState) {
                // Mark heart rate with asterisk if zero detected
                final String textHeartRate = String.valueOf(computedHeartRate)
                        + ((AntPlusHeartRatePcc.DataState.ZERO_DETECTED.equals(dataState)) ? "*" : "");

                // Mark heart beat count and heart beat event time with asterisk if initial value
                final String textHeartBeatCount = String.valueOf(heartBeatCount)
                        + ((AntPlusHeartRatePcc.DataState.INITIAL_VALUE.equals(dataState)) ? "*" : "");
                final String textHeartBeatEventTime = String.valueOf(heartBeatEventTime)
                        + ((AntPlusHeartRatePcc.DataState.INITIAL_VALUE.equals(dataState)) ? "*" : "");

                Log.i(TAG, "get HeartRate : " + textHeartRate);
            }
        });
    }
}

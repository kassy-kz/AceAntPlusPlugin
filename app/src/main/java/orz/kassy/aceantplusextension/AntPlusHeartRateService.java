package orz.kassy.aceantplusextension;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;
import com.eaglesakura.andriders.plugin.AcePluginService;
import com.eaglesakura.andriders.plugin.Category;
import com.eaglesakura.andriders.plugin.CentralEngineConnection;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.PluginInformation;
import com.eaglesakura.andriders.plugin.data.CentralEngineData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AntPlusHeartRateService extends Service implements AcePluginService {
    private static final String TAG = "AntPluginService";
    private AntPlusHeartRatePcc mHeartRatePcc = null;
    private CentralEngineData mCentralDataExtension;

    public AntPlusHeartRateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        CentralEngineConnection connection = CentralEngineConnection.onBind(this, intent);
        return connection.getBinder();
    }

    @Override
    public PluginInformation getExtensionInformation(CentralEngineConnection centralEngineConnection) {
        PluginInformation info = new PluginInformation(this, "antplus_hr");
        info.setSummary("Ant+対応センサーから心拍を取得します");
        info.setCategory(Category.CATEGORY_HEARTRATEMONITOR);
        return info;
    }

    @Override
    public List<DisplayKey> getDisplayInformation(CentralEngineConnection centralEngineConnection) {
        return new ArrayList<DisplayKey>();
    }

    @Override
    public void onAceServiceConnected(CentralEngineConnection centralEngineConnection) {
        mCentralDataExtension = centralEngineConnection.getCentralDataExtension();
        requestAccessToPcc();
    }

    @Override
    public void onAceServiceDisconnected(CentralEngineConnection centralEngineConnection) {

    }

    @Override
    public void onEnable(CentralEngineConnection centralEngineConnection) {
        // do nothing
    }

    @Override
    public void onDisable(CentralEngineConnection centralEngineConnection) {
        // do nothing
    }

    @Override
    public void startSetting(CentralEngineConnection centralEngineConnection) {
        // do nothing
    }

    /**
     * Ant+デバイスに接続しに行く
     */
    private void requestAccessToPcc() {
        int deviceNumber = PrefUtils.loadPrefHeartRateDeviceId(this);
        if (deviceNumber == -1) {
            Toast.makeText(this, "ハートレートモニターは設定されていません", Toast.LENGTH_LONG).show();
            return;
        }

        // 接続
        PccReleaseHandle<AntPlusHeartRatePcc> releaseHandle;
        releaseHandle = AntPlusHeartRatePcc.requestAccess(this, deviceNumber, 0, mAccessResultReceiver, mDeviceStateChangeReceiver);
    }

    protected AntPluginPcc.IPluginAccessResultReceiver<AntPlusHeartRatePcc> mAccessResultReceiver = new AntPluginPcc.IPluginAccessResultReceiver<AntPlusHeartRatePcc>() {
        @Override
        public void onResultReceived(AntPlusHeartRatePcc result,
                                     RequestAccessResult resultCode,
                                     DeviceState initialDeviceState) {
            switch (resultCode) {
                case SUCCESS: {
                    Log.i(TAG, "onResultReceived : success");
                    mHeartRatePcc = result;
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
    protected AntPluginPcc.IDeviceStateChangeReceiver mDeviceStateChangeReceiver = new AntPluginPcc.IDeviceStateChangeReceiver() {
        @Override
        public void onDeviceStateChange(final DeviceState newDeviceState) {
        }
    };


    public void subscribeToHrEvents() {
        mHeartRatePcc.subscribeHeartRateDataEvent(new AntPlusHeartRatePcc.IHeartRateDataReceiver() {
            @Override
            public void onNewHeartRateData(final long estTimestamp,
                                           EnumSet<EventFlag> eventFlags,
                                           final int computedHeartRate,
                                           final long heartBeatCount,
                                           final BigDecimal heartBeatEventTime,
                                           final AntPlusHeartRatePcc.DataState dataState) {

                final String textHeartRate = String.valueOf(computedHeartRate)
                        + ((AntPlusHeartRatePcc.DataState.ZERO_DETECTED.equals(dataState)) ? "*" : "");
                final String textHeartBeatCount = String.valueOf(heartBeatCount)
                        + ((AntPlusHeartRatePcc.DataState.INITIAL_VALUE.equals(dataState)) ? "*" : "");
                final String textHeartBeatEventTime = String.valueOf(heartBeatEventTime)
                        + ((AntPlusHeartRatePcc.DataState.INITIAL_VALUE.equals(dataState)) ? "*" : "");

                Log.i(TAG, "get HeartRate : " + textHeartRate);
                mCentralDataExtension.setHeartrate(computedHeartRate);
            }
        });
    }
}

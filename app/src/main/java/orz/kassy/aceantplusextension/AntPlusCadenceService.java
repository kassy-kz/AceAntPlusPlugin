package orz.kassy.aceantplusextension;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeCadencePcc;
import com.dsi.ant.plugins.antplus.pcc.AntPlusBikeSpeedDistancePcc;
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
import java.util.EnumSet;
import java.util.List;

public class AntPlusCadenceService extends Service implements AcePluginService {
    private static final String TAG = "CadenceService";
    private CentralEngineData mCentralDataExtension;

    private AntPlusBikeCadencePcc mCadencePcc = null;
    private AntPlusBikeSpeedDistancePcc mSpeedPcc = null;

    private PccReleaseHandle<AntPlusBikeCadencePcc> bcReleaseHandle = null;
    private PccReleaseHandle<AntPlusBikeSpeedDistancePcc> bsReleaseHandle = null;
    private float mCrankRpm, mWheelRpm;
    private long mCrankRev, mWheelRev;


    public AntPlusCadenceService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        CentralEngineConnection connection = CentralEngineConnection.onBind(this, intent);

        // TODO for debug
        onAceServiceConnected(null);
        return null;
//        return connection.getBinder();
    }

    /**
     * from AcePluginService
     * @param centralEngineConnection
     * @return
     */
    @Override
    public PluginInformation getExtensionInformation(CentralEngineConnection centralEngineConnection) {
        PluginInformation info = new PluginInformation(this, "antplus_hr");
        info.setSummary("Ant+対応センサーからケイデンス・速度を取得します");
        info.setCategory(Category.CATEGORY_SPEED_AND_CADENCE);
        return info;
    }

    /**
     * from AcePluginService
     * @param centralEngineConnection
     * @return
     */
    @Override
    public List<DisplayKey> getDisplayInformation(CentralEngineConnection centralEngineConnection) {
        return new ArrayList<DisplayKey>();
    }

    /**
     * from AcePluginService
     * @param centralEngineConnection
     */
    @Override
    public void onAceServiceConnected(CentralEngineConnection centralEngineConnection) {
        Log.i(TAG, "onAceServiceConnected");
//        mCentralDataExtension = centralEngineConnection.getCentralDataExtension();
        requestAccessToPcc();
    }

    /**
     * from AcePluginService
     * @param centralEngineConnection
     */
    @Override
    public void onAceServiceDisconnected(CentralEngineConnection centralEngineConnection) {
        bcReleaseHandle.close();
        bsReleaseHandle.close();
    }

    /**
     * from AcePluginService
     * @param centralEngineConnection
     */
    @Override
    public void onEnable(CentralEngineConnection centralEngineConnection) {
        // do nothing
    }

    /**
     * from AcePluginService
     * @param centralEngineConnection
     */
    @Override
    public void onDisable(CentralEngineConnection centralEngineConnection) {
        // do nothing
    }

    /**
     * from AcePluginService
     * @param centralEngineConnection
     */
    @Override
    public void startSetting(CentralEngineConnection centralEngineConnection) {
        // do nothing
    }

    /**
     * Ant+デバイスに接続しに行く
     */
    private void requestAccessToPcc() {
        int deviceNumber = PrefUtils.loadPrefCadenceDeviceId(this);
        if (deviceNumber == -1) {
            Toast.makeText(this, "ケイデンスセンサーは設定されていません", Toast.LENGTH_LONG).show();
            return;
        }

        // 接続
        boolean isBSC;
        int deviceType = PrefUtils.loadPrefCadenceDeviceType(this);
        if (deviceType == DeviceSearchActivity.DEVICE_TYPE_SPDCAD) {
            isBSC = true;
        } else {
            isBSC = false;
        }
        Log.i(TAG, "number : " + deviceNumber + ", bsc : " + isBSC);
        bcReleaseHandle = AntPlusBikeCadencePcc.requestAccess(this,
                deviceNumber, 0, isBSC, mAccessResultReceiver, mDeviceStateChangeReceiver);
    }

    /**
     * Ant+ Receiver
     */
    protected AntPluginPcc.IPluginAccessResultReceiver<AntPlusBikeCadencePcc> mAccessResultReceiver = new AntPluginPcc.IPluginAccessResultReceiver<AntPlusBikeCadencePcc>() {
        @Override
        public void onResultReceived(AntPlusBikeCadencePcc result,
                                     RequestAccessResult resultCode,
                                     DeviceState initialDeviceState) {
            switch (resultCode) {
                case SUCCESS: {
                    Log.i(TAG, "onResultReceived : success");
                    mCadencePcc = result;
                    subscribeToCadenceEvents();
                    break;
                }
                default: {
                    Log.i(TAG, "onResultReceived : " + resultCode);
                }
            }
        }
    };

    /**
     * State Change Receiver
     */
    protected AntPluginPcc.IDeviceStateChangeReceiver mDeviceStateChangeReceiver = new AntPluginPcc.IDeviceStateChangeReceiver() {
        @Override
        public void onDeviceStateChange(final DeviceState newDeviceState) {
        }
    };

    /**
     * Subscribe set
     */
    public void subscribeToCadenceEvents() {
        subscribeCalculatedCadenceEvent();
        subscribeRawCadenceDataEvent();
        requestBikeCadenceDistnace();
    }

    private void subscribeCalculatedCadenceEvent() {
        mCadencePcc.subscribeCalculatedCadenceEvent(new AntPlusBikeCadencePcc.ICalculatedCadenceReceiver() {
            @Override
            public void onNewCalculatedCadence(final long estTimestamp,
                                               final EnumSet<EventFlag> eventFlags,
                                               final BigDecimal calculatedCadence) {
                Log.i(TAG, "calculatedCadence : " + String.valueOf(calculatedCadence));
                mCrankRpm = calculatedCadence.floatValue();
                mCentralDataExtension.setSpeedAndCadence(mCrankRpm, (int)mCrankRev, mWheelRpm, (int)mWheelRev);
            }
        });
    }

    private void subscribeRawCadenceDataEvent() {
        mCadencePcc.subscribeRawCadenceDataEvent(new AntPlusBikeCadencePcc.IRawCadenceDataReceiver() {
            @Override
            public void onNewRawCadenceData(final long estTimestamp,
                                            final EnumSet<EventFlag> eventFlags,
                                            final BigDecimal timestampOfLastEvent,
                                            final long cumulativeRevolutions) {
                Log.i(TAG, "cumulativeRevolutions : " + String.valueOf(cumulativeRevolutions));
                mCrankRev = cumulativeRevolutions;
            }
        });
    }

    private void requestBikeCadenceDistnace() {
        if (mCadencePcc.isSpeedAndCadenceCombinedSensor()) {
            bsReleaseHandle = AntPlusBikeSpeedDistancePcc.requestAccess(
                    AntPlusCadenceService.this,
                    mCadencePcc.getAntDeviceNumber(),
                    0,
                    true,
                    resultReceiver,
                    stateReceiver);
        }
    }

    AntPluginPcc.IPluginAccessResultReceiver<AntPlusBikeSpeedDistancePcc> resultReceiver = new AntPluginPcc.IPluginAccessResultReceiver<AntPlusBikeSpeedDistancePcc>() {
        @Override
        public void onResultReceived(
                AntPlusBikeSpeedDistancePcc result,
                RequestAccessResult resultCode,
                DeviceState initialDeviceStateCode) {
            switch (resultCode) {
                case SUCCESS: {
                    mSpeedPcc = result;
                    mSpeedPcc.subscribeRawSpeedAndDistanceDataEvent(new AntPlusBikeSpeedDistancePcc.IRawSpeedAndDistanceDataReceiver() {
                        @Override
                        public void onNewRawSpeedAndDistanceData(final long estTimestamp,
                                                                 final EnumSet<EventFlag> eventFlags,
                                                                 final BigDecimal timestampOfLastEvent,
                                                                 final long cumulativeRevolutions) {
                            Log.i(TAG, "cumulativeRevolutions : " + String.valueOf(cumulativeRevolutions));
                            mWheelRev = cumulativeRevolutions;
                        }
                    });
                    mSpeedPcc.subscribeCalculatedSpeedEvent(
                            new AntPlusBikeSpeedDistancePcc.CalculatedSpeedReceiver(new BigDecimal(2.095)) {
                                @Override
                                public void onNewCalculatedSpeed(
                                        long estTimestamp,
                                        EnumSet<EventFlag> eventFlags,
                                        final BigDecimal calculatedSpeed) {
                                    Log.i(TAG, "calculatedSpeed : " + String.valueOf(calculatedSpeed));
                                    mWheelRpm = calculatedSpeed.floatValue();
                                }
                            });
                    break;
                }

                case CHANNEL_NOT_AVAILABLE:
//                    tv_calculatedSpeed.setText("CHANNEL NOT AVAILABLE");
                    break;
                case BAD_PARAMS:
//                    tv_calculatedSpeed.setText("BAD_PARAMS");
                    break;
                case OTHER_FAILURE:
//                    tv_calculatedSpeed.setText("OTHER FAILURE");
                    break;
                case DEPENDENCY_NOT_INSTALLED:
//                    tv_calculatedSpeed
//                            .setText("DEPENDENCY NOT INSTALLED");
                    break;
                default:
//                    tv_calculatedSpeed.setText("UNRECOGNIZED ERROR: "
//                            + resultCode);
                    break;
            }
        }
    };


    AntPluginPcc.IDeviceStateChangeReceiver stateReceiver = new AntPluginPcc.IDeviceStateChangeReceiver() {
        @Override
        public void onDeviceStateChange(final DeviceState newDeviceState) {
            if (newDeviceState != DeviceState.TRACKING) {
//                tv_calculatedSpeed.setText(newDeviceState
//                        .toString());
            }
            if (newDeviceState == DeviceState.DEAD) {
                mSpeedPcc = null;
            }
        }
    };
}

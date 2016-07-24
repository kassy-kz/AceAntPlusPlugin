package orz.kassy.aceantplusextension;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.dsi.ant.plugins.antplus.pcc.defines.DeviceType;
import com.dsi.ant.plugins.antplus.pccbase.MultiDeviceSearch;

import java.util.EnumSet;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class DeviceSetActivity extends AppCompatActivity {

    private static final String TAG = "DeviceSetActivity";
    @InjectView(R.id.txtHeartRateDeviceName)
    TextView mTxtHeartRateName;

    @InjectView(R.id.txtCadenceDeviceName)
    TextView mTxtCadenceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_set);
        ButterKnife.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int cadenceDeviceId = PrefUtils.loadPrefCadenceDeviceId(this);
        Log.i(TAG, "onResume number1 : " + cadenceDeviceId);
        if (cadenceDeviceId != -1) {
            String name = PrefUtils.loadPrefCadenceDeviceName(this);
            setDeviceNameText(mTxtCadenceName, name, cadenceDeviceId + "");
        }

        int heartrateDeviceId = PrefUtils.loadPrefHeartRateDeviceId(this);
        Log.i(TAG, "onResume number2 : " + heartrateDeviceId);
        if (heartrateDeviceId != -1) {
            String name = PrefUtils.loadPrefHeartRateDeviceName(this);
            setDeviceNameText(mTxtHeartRateName, name, heartrateDeviceId + "");
        }
    }

    private void setDeviceNameText(TextView textView, String name, String number) {
        String showString;

        if (name.contains(number)) {
            showString = name;
        } else {
            showString = name + ": " + number;
        }

        textView.setText(showString);
    }

    /**
     * ハートレートモニターを検索しに行く
     */
    @OnClick(R.id.btnSearchHeartRate)
    public void onClickSearchHeartRate() {

        // 検索Activityを出す
        Intent intent = new Intent(this, DeviceSearchActivity.class);
        Bundle bundle = new Bundle();

        EnumSet<DeviceType> set = EnumSet.noneOf(DeviceType.class);
        set.add(DeviceType.HEARTRATE);

        bundle.putSerializable(DeviceSearchActivity.BUNDLE_KEY_DEVICE_TYPE, set);
        intent.putExtra(DeviceSearchActivity.INTENT_EX_DEVICE_TYPE, bundle);
        startActivityForResult(intent, DeviceSearchActivity.REQUEST_CODE_SEARCH_DEVICE);
    }

    /**
     * ケイデンスセンサーを検索しに行く
     */
    @OnClick(R.id.btnSearchCadence)
    public void onClickSearchCadence() {

        // 検索Activityを出す
        Intent intent = new Intent(this, DeviceSearchActivity.class);
        Bundle bundle = new Bundle();

        EnumSet<DeviceType> set = EnumSet.noneOf(DeviceType.class);
        set.add(DeviceType.BIKE_CADENCE);
        set.add(DeviceType.BIKE_SPD);
        set.add(DeviceType.BIKE_SPDCAD);

        bundle.putSerializable(DeviceSearchActivity.BUNDLE_KEY_DEVICE_TYPE, set);
        intent.putExtra(DeviceSearchActivity.INTENT_EX_DEVICE_TYPE, bundle);
        startActivityForResult(intent, DeviceSearchActivity.REQUEST_CODE_SEARCH_DEVICE);
    }

    /**
     * ハートレートモニターを消去
     */
    @OnClick(R.id.btnDeleteHeartRate)
    public void onClickDeleteHeartRate() {
        mTxtHeartRateName.setText("(未選択)");
        PrefUtils.savePrefHeartRateDeviceId(this, -1);
        PrefUtils.savePrefHeartRateDeviceName(this, "");
    }

    /**
     * ケイデンスセンサーを消去
     */
    @OnClick(R.id.btnDeleteCadence)
    public void onClickDeleteCadence() {
        mTxtCadenceName.setText("(未選択)");
        PrefUtils.savePrefCadenceDeviceId(this, -1);
        PrefUtils.savePrefCadenceDeviceName(this, "");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DeviceSearchActivity.REQUEST_CODE_SEARCH_DEVICE ||
                resultCode == DeviceSearchActivity.RESULT_CODE_SEARCH_DEVICE) {
            MultiDeviceSearch.MultiDeviceSearchResult result = data.getParcelableExtra(DeviceSearchActivity.INTENT_EX_RESULT_DEVICE);
            if (result == null) {
                return;
            }

            String name = result.getDeviceDisplayName();
            String number = result.getAntDeviceNumber() + "";
            Log.i(TAG, "number : " + number);

            // 登録する
            switch (data.getIntExtra(DeviceSearchActivity.INTENT_EX_DEVICE_TYPE, -1)) {
                case DeviceSearchActivity.DEVICE_TYPE_HEARTRATE: {
                    setDeviceNameText(mTxtHeartRateName, name, number);
                    PrefUtils.savePrefHeartRateDeviceId(this, result.getAntDeviceNumber());
                    PrefUtils.savePrefHeartRateDeviceName(this, result.getDeviceDisplayName());
                    break;
                }
                case DeviceSearchActivity.DEVICE_TYPE_SPD:
                case DeviceSearchActivity.DEVICE_TYPE_SPDCAD:
                case DeviceSearchActivity.DEVICE_TYPE_CADENCE: {
                    setDeviceNameText(mTxtCadenceName, name, number);
                    PrefUtils.savePrefCadenceDeviceId(this, result.getAntDeviceNumber());
                    PrefUtils.savePrefCadenceDeviceName(this, result.getDeviceDisplayName());
                    break;
                }
            }
        }
    }
}

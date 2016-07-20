package orz.kassy.aceantplusextension;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.dsi.ant.plugins.antplus.pcc.defines.DeviceType;
import com.dsi.ant.plugins.antplus.pccbase.MultiDeviceSearch;

import java.util.EnumSet;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class DeviceSetActivity extends AppCompatActivity {

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DeviceSearchActivity.REQUEST_CODE_SEARCH_DEVICE ||
                resultCode == DeviceSearchActivity.RESULT_CODE_SEARCH_DEVICE) {
            MultiDeviceSearch.MultiDeviceSearchResult result = data.getParcelableExtra(DeviceSearchActivity.INTENT_EX_RESULT_DEVICE);
            if (result == null) {
                return;
            }
            String showString = result.getDeviceDisplayName() + " : " + result.getAntDeviceNumber();
            // 登録する
            switch (data.getIntExtra(DeviceSearchActivity.INTENT_EX_DEVICE_TYPE, -1)) {
                case DeviceSearchActivity.DEVICE_TYPE_HEARTRATE: {
                    mTxtHeartRateName.setText(showString);
                    PrefUtils.savePrefHeartRateDeviceId(this, result.getAntDeviceNumber());
                    break;
                }
                case DeviceSearchActivity.DEVICE_TYPE_SPD:
                case DeviceSearchActivity.DEVICE_TYPE_SPDCAD:
                case DeviceSearchActivity.DEVICE_TYPE_CADENCE: {
                    mTxtCadenceName.setText(showString);
                    PrefUtils.savePrefCadenceDeviceId(this, result.getAntDeviceNumber());
                    break;
                }
            }
        }
    }
}

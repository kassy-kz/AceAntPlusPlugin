package orz.kassy.aceantplusextension;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.dsi.ant.plugins.antplus.pcc.defines.DeviceType;

import java.util.EnumSet;

import butterknife.ButterKnife;
import butterknife.OnClick;
import orz.kassy.aceantplusextension.antplus.Activity_MultiDeviceSearchSampler;

public class DeviceSetActivity extends AppCompatActivity {

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
        Intent intent = new Intent(this, DeviceSearchActivity.class);
        Bundle bundle = new Bundle();

        EnumSet<DeviceType> set = EnumSet.noneOf(DeviceType.class);
        set.add(DeviceType.HEARTRATE);

        bundle.putSerializable(DeviceSearchActivity.BUNDLE_KEY_DEVICE_TYPE, set);
        intent.putExtra(DeviceSearchActivity.INTENT_EX_DEVICE_TYPE, bundle);
        startActivityForResult(intent, DeviceSearchActivity.RESULT_SEARCH_STOPPED);
    }
}

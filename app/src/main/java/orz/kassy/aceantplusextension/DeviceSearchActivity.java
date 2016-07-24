/*
This software is subject to the license described in the License.txt file
included with this software distribution. You may not use this file except in compliance
with this license.

Copyright (c) Dynastream Innovations Inc. 2014
All rights reserved.
 */

package orz.kassy.aceantplusextension;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dsi.ant.plugins.antplus.pcc.MultiDeviceSearch;
import com.dsi.ant.plugins.antplus.pcc.MultiDeviceSearch.RssiSupport;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceType;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.MultiDeviceSearch.MultiDeviceSearchResult;

import java.util.ArrayList;
import java.util.EnumSet;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Searches for multiple devices on the same channel using the multi-device
 * search interface
 */
public class DeviceSearchActivity extends AppCompatActivity {

    public static final String EXTRA_KEY_MULTIDEVICE_SEARCH_RESULT = "ex_key_search_result";
    public static final String INTENT_EX_DEVICE_TYPE = "intent_ex_device_type";
    public static final String BUNDLE_KEY_DEVICE_TYPE = "bundle_key_device_type";
    public static final String INTENT_EX_RESULT_DEVICE = "intent_ex_result_device";
    public static final int REQUEST_CODE_SEARCH_DEVICE = 1;
    public static final int RESULT_CODE_SEARCH_DEVICE = 2;

    public static final int DEVICE_TYPE_HEARTRATE = 1;
    public static final int DEVICE_TYPE_SPD       = 2;
    public static final int DEVICE_TYPE_SPDCAD    = 3;
    public static final int DEVICE_TYPE_CADENCE   = 4;

    private Context mContext;
    private TextView mStatus;

    private ListView mFoundDevicesList;
    private ArrayList<MultiDeviceSearchResultWithRSSI> mFoundDevices = new ArrayList<MultiDeviceSearchResultWithRSSI>();
    private DevieSearchAdapter mFoundAdapter;

    private ListView mConnectedDevicesList;
    private ArrayList<MultiDeviceSearchResultWithRSSI> mConnectedDevices = new ArrayList<MultiDeviceSearchResultWithRSSI>();
    private DevieSearchAdapter mConnectedAdapter;
    private MultiDeviceSearch mSearch;

    @InjectView(R.id.progressEmpty)
    ProgressBar mProgressEmptyView;

    /**
     * onCreate
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multidevice_scan);
        ButterKnife.inject(this);

        mContext = getApplicationContext();
        mStatus = (TextView) findViewById(R.id.textView_Status);

        mFoundDevicesList = (ListView) findViewById(R.id.listView_FoundDevices);

        mFoundAdapter = new DevieSearchAdapter(this, mFoundDevices);
        mFoundDevicesList.setAdapter(mFoundAdapter);

        mFoundDevicesList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectDevice(mFoundAdapter.getItem(position).mDevice);
            }
        });

        mConnectedDevicesList = (ListView) findViewById(R.id.listView_AlreadyConnectedDevices);

        mConnectedAdapter = new DevieSearchAdapter(this, mConnectedDevices);
        mConnectedDevicesList.setAdapter(mConnectedAdapter);

        mConnectedDevicesList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectDevice(mConnectedAdapter.getItem(position).mDevice);
            }
        });

        Intent i = getIntent();
        Bundle args = i.getBundleExtra(INTENT_EX_DEVICE_TYPE);
        EnumSet<DeviceType> devices = (EnumSet<DeviceType>) args.getSerializable(BUNDLE_KEY_DEVICE_TYPE);
        mSearch = new MultiDeviceSearch(this, devices, mSearchCallback, mRssiCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSearch.close();
    }

    /**
     * ListItemを選択
     * Resultを返して本Activity終了
     * @param result
     */
    public void selectDevice(MultiDeviceSearchResult result) {
        switch (result.getAntDeviceType()) {
            case BIKE_CADENCE: {
                Intent data = new Intent();
                data.putExtra(INTENT_EX_DEVICE_TYPE, DEVICE_TYPE_CADENCE);
                data.putExtra(INTENT_EX_RESULT_DEVICE, result);
                setResult(RESULT_CODE_SEARCH_DEVICE, data);
                finish();
                break;
            }
            case BIKE_SPD: {
                Intent data = new Intent();
                data.putExtra(INTENT_EX_DEVICE_TYPE, DEVICE_TYPE_SPD);
                data.putExtra(INTENT_EX_RESULT_DEVICE, result);
                setResult(RESULT_CODE_SEARCH_DEVICE, data);
                finish();
                break;
            }
            case BIKE_SPDCAD: {
                Intent data = new Intent();
                data.putExtra(INTENT_EX_DEVICE_TYPE, DEVICE_TYPE_SPDCAD);
                data.putExtra(INTENT_EX_RESULT_DEVICE, result);
                setResult(RESULT_CODE_SEARCH_DEVICE, data);
                finish();
                break;
            }
            case HEARTRATE: {
                Intent data = new Intent();
                data.putExtra(INTENT_EX_DEVICE_TYPE, DEVICE_TYPE_HEARTRATE);
                data.putExtra(INTENT_EX_RESULT_DEVICE, result);
                setResult(RESULT_CODE_SEARCH_DEVICE, data);
                finish();
                break;
            }
            default:
                break;
        }
    }

    /**
     * Callbacks from the multi-device search interface
     */
    private MultiDeviceSearch.SearchCallbacks mSearchCallback = new MultiDeviceSearch.SearchCallbacks() {
        /**
         * Called when a device is found. Display found devices in connected and
         * found lists
         */
        public void onDeviceFound(final MultiDeviceSearchResult deviceFound) {
            final MultiDeviceSearchResultWithRSSI result = new MultiDeviceSearchResultWithRSSI();
            result.mDevice = deviceFound;

            // We split up devices already connected to the plugin from
            // un-connected devices to make this information more visible to the
            // user, since the user most likely wants to be aware of which
            // device they are already using in another app
            if (deviceFound.isAlreadyConnected()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // connected device category is invisible unless there
                        // are some present
                        if (mConnectedAdapter.isEmpty()) {
                            findViewById(R.id.textView_AlreadyConnectedTitle).setVisibility(
                                    View.VISIBLE);
                            mConnectedDevicesList.setVisibility(View.VISIBLE);
                        }
                        mProgressEmptyView.setVisibility(View.GONE);
                        mConnectedAdapter.add(result);
                        mConnectedAdapter.notifyDataSetChanged();
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressEmptyView.setVisibility(View.GONE);
                        mFoundAdapter.add(result);
                        mFoundAdapter.notifyDataSetChanged();
                    }
                });
            }
        }

        /**
         * The search has been stopped unexpectedly
         */
        public void onSearchStopped(RequestAccessResult reason) {
            Intent result = new Intent();
            result.putExtra(EXTRA_KEY_MULTIDEVICE_SEARCH_RESULT, reason.getIntValue());
            setResult(REQUEST_CODE_SEARCH_DEVICE, result);
            finish();
        }

        @Override
        public void onSearchStarted(RssiSupport supportsRssi) {
            if (supportsRssi == RssiSupport.UNAVAILABLE) {
                Toast.makeText(mContext, "Rssi information not available.", Toast.LENGTH_SHORT).show();
            } else if (supportsRssi == RssiSupport.UNKNOWN_OLDSERVICE) {
                Toast.makeText(mContext, "Rssi might be supported. Please upgrade the plugin service.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * Callback for RSSI data of previously found devices
     */
    private MultiDeviceSearch.RssiCallback mRssiCallback = new MultiDeviceSearch.RssiCallback() {
        /**
         * Receive an RSSI data update from a specific found device
         */
        @Override
        public void onRssiUpdate(final int resultId, final int rssi) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (MultiDeviceSearchResultWithRSSI result : mFoundDevices) {
                        if (result.mDevice.resultID == resultId) {
                            result.mRSSI = rssi;
                            mFoundAdapter.notifyDataSetChanged();

                            break;
                        }
                    }
                }
            });
        }
    };
}

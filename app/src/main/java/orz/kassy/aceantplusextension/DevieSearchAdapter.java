package orz.kassy.aceantplusextension;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class DevieSearchAdapter extends ArrayAdapter<MultiDeviceSearchResultWithRSSI> {
    private static final int DEFAULT_MIN_RSSI = -100;
    private static final String TAG = "DeviceSearchAdapter";

    private ArrayList<MultiDeviceSearchResultWithRSSI> mSearchResultList;
    private String[] mDeviceTypes;
    private int mMinRSSI = DEFAULT_MIN_RSSI;

    public DevieSearchAdapter(Context context, ArrayList<MultiDeviceSearchResultWithRSSI> searchResultList) {
        super(context, R.layout.layout_multidevice_searchresult, searchResultList);
        mSearchResultList = searchResultList;
        mDeviceTypes = context.getResources().getStringArray(R.array.device_types);
    }

    /**
     * Update the display with new data for the specified position
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_multidevice_searchresult, null);
        }

        MultiDeviceSearchResultWithRSSI result = mSearchResultList.get(position);

        if (result != null) {
            Log.i(TAG, "rssi:" + result.mRSSI);

            TextView tv_deviceType = (TextView) convertView.findViewById(R.id.textView_multiDeviceType);
            TextView tv_deviceName = (TextView) convertView.findViewById(R.id.textView_multiDeviceName);
            ProgressBar pb_RSSI = (ProgressBar) convertView.findViewById(R.id.progressBar_multiDeviceRSSI);

            if (tv_deviceType != null) {
                tv_deviceType.setText(mDeviceTypes[result.mDevice.getAntDeviceType().ordinal()]);
            }
            if (tv_deviceName != null) {
                tv_deviceName.setText(result.mDevice.getDeviceDisplayName());
            }

            // only update once i.mRSSI value has been populated
            if (pb_RSSI != null && result.mRSSI != Integer.MIN_VALUE) {
                // display RSSI data
                if (pb_RSSI.getVisibility() != View.VISIBLE) {
                    convertView.findViewById(R.id.label_RSSI).setVisibility(View.VISIBLE);
                    pb_RSSI.setVisibility(View.VISIBLE);
                }

                // Device is nearest it can be, cap to zero
                if (result.mRSSI >= 0) {
                    result.mRSSI = 0;
                }

                // 0 is farthest away, (- mMinRSSI) is nearest
                int nearness = result.mRSSI - mMinRSSI;

                // find the new farthest
                if (nearness < 0) {
                    mMinRSSI = result.mRSSI;
                    nearness = 0;
                }

                int display = 100 * nearness / -mMinRSSI;
                pb_RSSI.setProgress(display);
            }
        }
        return convertView;
    }
}

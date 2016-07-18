package orz.kassy.aceantplusextension;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.eaglesakura.andriders.plugin.AcePluginService;
import com.eaglesakura.andriders.plugin.Category;
import com.eaglesakura.andriders.plugin.CentralEngineConnection;
import com.eaglesakura.andriders.plugin.DisplayKey;
import com.eaglesakura.andriders.plugin.PluginInformation;
import com.eaglesakura.andriders.plugin.data.CentralEngineData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AntPlusHeartRateService extends Service implements AcePluginService {
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
        final CentralEngineData centralDataExtension = centralEngineConnection.getCentralDataExtension();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Date date = new Date();
                centralDataExtension.setHeartrate(date.getSeconds());
            }
        }, 1000, 1000);
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
}

package orz.kassy.aceantplusextension;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import orz.kassy.aceantplusextension.antplus.Activity_BikeCadenceSampler;
import orz.kassy.aceantplusextension.antplus.Activity_BikeSpeedDistanceSampler;
import orz.kassy.aceantplusextension.antplus.Activity_HeartRateDisplay;
import orz.kassy.aceantplusextension.antplus.Activity_MultiDeviceFilter;
import orz.kassy.aceantplusextension.antplus.Activity_MultiDeviceSearchSampler;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View btn1 = findViewById(R.id.btn1);
        btn1.setOnClickListener(this);
        View btn2 = findViewById(R.id.btn2);
        btn2.setOnClickListener(this);
        View btn3 = findViewById(R.id.btn3);
        btn3.setOnClickListener(this);

        View btn4 = findViewById(R.id.btn4);
        btn4.setOnClickListener(this);
        View btn5 = findViewById(R.id.btn5);
        btn5.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn1: {
                Log.i(TAG, "btn1");
                Intent intent = new Intent(this, Activity_BikeCadenceSampler.class);
                startActivity(intent);

                break;
            }
            case R.id.btn2: {
                Intent intent = new Intent(this, Activity_BikeSpeedDistanceSampler.class);
                startActivity(intent);

                break;
            }
            case R.id.btn3: {
                Intent intent = new Intent(this, Activity_HeartRateDisplay.class);
                startActivity(intent);

                break;
            }
            case R.id.btn4: {
                Intent intent = new Intent(this, Activity_MultiDeviceFilter.class);
                startActivity(intent);

                break;
            }
            case R.id.btn5: {
                Intent intent = new Intent(this, Activity_MultiDeviceSearchSampler.class);
                startActivity(intent);

                break;
            }

        }

    }
}

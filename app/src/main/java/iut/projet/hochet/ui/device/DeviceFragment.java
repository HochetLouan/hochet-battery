package iut.projet.hochet.ui.device;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import iut.projet.hochet.MainActivity;
import iut.projet.hochet.databinding.FragmentDeviceBinding;

import iut.projet.hochet.ENode;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import androidx.lifecycle.Observer;

import com.google.android.material.progressindicator.CircularProgressIndicator;

public class DeviceFragment extends Fragment implements Observer {

    private FragmentDeviceBinding binding;

    private final HashMap<String, View> composants = new HashMap<>();

    private String device = "not connected";
    private String mode = "ouais";
    private String etat = "";
    private String dischargeLimit = "";
    private String chargeRate = "";
    private String capacity = "";
    private String level = "";
    private CircularProgressIndicator progress;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentDeviceBinding.inflate(inflater, container, false);

        View view = binding.getRoot();

        composants.put("mode", binding.mode);
        composants.put("level", binding.level);
        composants.put("Capacity", binding.Capacity);
        composants.put("ChargeRate", binding.ChargeRate);
        composants.put("DischargeLimit", binding.DischargeLimit);
        composants.put("etat", binding.etat);
        progress = binding.circularProgress;
        // ...

        Log.d("DeviceFragment", "registering observer");
        ((MainActivity) getActivity()).enode.addObserver(this);

        return view;
    }

    private void refreshUI() {
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar()
                    .setTitle("DEVICE : " + device);
        }

        // ...

        ((TextView) composants.get("mode")).setText("mode : " + mode);
        ((TextView) composants.get("etat")).setText(etat);
        ((TextView) composants.get("DischargeLimit")).setText(dischargeLimit + "%");
        ((TextView) composants.get("Capacity")).setText(capacity + "kwh");
        ((TextView) composants.get("ChargeRate")).setText(chargeRate + "kw");
        ((TextView) composants.get("level")).setText(level + "%");


    }

    private void processDatas(){
        JSONObject datas = ENode.datas;
        Log.d("DeviceFragment","datas : " + datas);
        if (datas != null) {
            try {
                // model
                JSONObject jsonDeviceInformation = datas.getJSONObject("information");
                if (!jsonDeviceInformation.isNull("siteName"))
                    device = jsonDeviceInformation.getString("siteName");
                // config
                JSONObject jsonDeviceConfig = datas.getJSONObject("config");
                if (!jsonDeviceConfig.isNull("operationMode"))
                    mode = jsonDeviceConfig.getString("operationMode");
                JSONObject jsonDeviceChargeState = datas.getJSONObject("chargeState");
                if (!jsonDeviceChargeState.isNull("status"))
                    etat = jsonDeviceChargeState.getString("status");
                if (!jsonDeviceChargeState.isNull("chargeRate"))
                    chargeRate = jsonDeviceChargeState.getString("chargeRate");
                if (!jsonDeviceChargeState.isNull("dischargeLimit"))
                    dischargeLimit = jsonDeviceChargeState.getString("dischargeLimit");
                if (!jsonDeviceChargeState.isNull("batteryLevel"))
                    level = jsonDeviceChargeState.getString("batteryLevel");
                if (!jsonDeviceChargeState.isNull("batteryCapacity"))
                    capacity = jsonDeviceChargeState.getString("batteryCapacity");
                int batteryPercent = Integer.parseInt(level);
                progress.setProgress(batteryPercent);
                if (batteryPercent <= 20) {
                    progress.setIndicatorColor(Color.RED);
                } else if (batteryPercent <= 50) {
                    progress.setIndicatorColor(Color.YELLOW);
                } else {
                    progress.setIndicatorColor(Color.GREEN);
                }
                // device state
                // ...

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onChanged(Object o) {
        processDatas();
        refreshUI();
    }
}
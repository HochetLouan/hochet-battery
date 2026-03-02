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

public class DeviceFragment extends Fragment implements Observer {

    private FragmentDeviceBinding binding;

    private final HashMap<String, View> composants = new HashMap<>();

    private String device = "not connected";
    private String mode = "ouais";
    // ...

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentDeviceBinding.inflate(inflater, container, false);

        View view = binding.getRoot();

        composants.put("mode", binding.mode);
        composants.put("level", binding.level);
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
package iut.projet.hochet.ui.devices;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import iut.projet.hochet.ENode;
import iut.projet.hochet.MainActivity;
import iut.projet.hochet.databinding.FragmentDevicesBinding;

public class DevicesFragment extends Fragment implements Observer<ENode> {

    private FragmentDevicesBinding binding;
    private ListView listBatteries;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> batteriesNames = new ArrayList<>();
    private ArrayList<String> batteriesIds = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        DevicesViewModel devicesViewModel =
                new ViewModelProvider(this).get(DevicesViewModel.class);

        binding = FragmentDevicesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        listBatteries = binding.listBatteries;

        adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_list_item_1,
                batteriesNames
        );

        listBatteries.setAdapter(adapter);
        ((MainActivity) getActivity()).enode.addObserver(this);
        ((MainActivity) getActivity()).enode.fetchBatteries();

        listBatteries.setOnItemClickListener((parent, view, position, id) -> {

            String deviceId = batteriesIds.get(position);

            Log.d("DevicesFragment", "Battery clicked: " + deviceId);

            ENode.selectedDeviceId = deviceId;
            //Pour eviter l'attente des 2 seconde lorsqu'on retourne vite dans la page d'info
            ((MainActivity) getActivity()).enode.fetchDatas();
        });

        return root;
    }

    @Override
    public void onChanged(ENode eNode) {
        JSONArray list = ENode.batteriesList;
        if (list != null) {
            batteriesNames.clear();
            batteriesIds.clear();
            try {
                for (int i = 0; i < list.length(); i++) {
                    JSONObject battery = list.getJSONObject(i);
                    String id = battery.getString("id");
                    String tempName = "Batterie " + (i + 1);
                    if (battery.has("information") && !battery.isNull("information")) {
                        tempName = battery
                                .getJSONObject("information")
                                .optString("siteName", tempName);
                    }
                    batteriesNames.add(tempName + " (" + id.substring(0, 8) + ")");
                    batteriesIds.add(id);
                }
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Log.e("DevicesFragment", "Erreur lecture liste: " + e.getMessage());
            }
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity) getActivity()).enode.removeObserver(this);
        binding = null;
    }
}
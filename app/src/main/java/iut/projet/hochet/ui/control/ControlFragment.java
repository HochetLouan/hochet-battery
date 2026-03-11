package iut.projet.hochet.ui.control;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import iut.projet.hochet.ENode;
import iut.projet.hochet.databinding.FragmentModifyBinding;

public class ControlFragment extends Fragment {

    private FragmentModifyBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ControlViewModel controlViewModel =
                new ViewModelProvider(this).get(ControlViewModel.class);

        binding = FragmentModifyBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        ENode enode = new ENode();
        binding.selfReliance.setOnClickListener(v -> enode.setBatteryOperationMode("SELF_RELIANCE"));
        binding.timeUse.setOnClickListener(v -> enode.setBatteryOperationMode("TIME_OF_USE"));
        binding.importFocus.setOnClickListener(v -> enode.setBatteryOperationMode("IMPORT_FOCUS"));
        binding.exportFocus.setOnClickListener(v -> enode.setBatteryOperationMode("EXPORT_FOCUS"));
        binding.iddle.setOnClickListener(v -> enode.setBatteryOperationMode("IDLE"));
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
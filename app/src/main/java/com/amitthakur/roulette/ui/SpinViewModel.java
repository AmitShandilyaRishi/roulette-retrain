package com.amitthakur.roulette.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.amitthakur.roulette.data.SpinRepository;
import com.amitthakur.roulette.model.SpinRecord;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;
import java.util.List;

@HiltViewModel
public class SpinViewModel extends ViewModel {
    private final SpinRepository repo;
    public final LiveData<List<SpinRecord>> recentSpins;

    @Inject
    public SpinViewModel(SpinRepository repo) {
        this.repo = repo;
        this.recentSpins = repo.getRecentSpins();
    }

    public void submitSpin(SpinRecord r) {
        repo.addSpin(r);
    }
}



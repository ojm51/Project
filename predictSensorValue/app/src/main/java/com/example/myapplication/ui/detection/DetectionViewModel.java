package com.example.myapplication.ui.detection;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DetectionViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public DetectionViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is detection fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
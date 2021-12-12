package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class writeInfo extends AppCompatActivity {
    private EditText writerName;
    private EditText writerPhone;
    private EditText parentName;
    private EditText parentPhone;

    private Button writeInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        writerName = findViewById(R.id.userName);
        writerPhone = findViewById(R.id.userPhone);
        parentName = findViewById(R.id.parentName);
        parentPhone = findViewById(R.id.parentPhone);

        writeInfo = findViewById(R.id.btnWriteInput);

        writeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });
    }

    private void upload(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String writer = auth.getCurrentUser().getUid();
        String wName = writerName.getText().toString();
        String wPhone = writerPhone.getText().toString();
        String pName = parentName.getText().toString();
        String pPhone = parentPhone.getText().toString();

        if(wName.length() > 0 && wPhone.length() > 0 && pName.length() > 0 && pPhone.length() > 0){
            myInfo newInfo = new myInfo(writer, wName, wPhone, pName, pPhone);
            uploader(newInfo, writer);
        }
        else{
            Toast.makeText(getApplicationContext(), "정보 작성에 실패하였습니다 모든 정보란을 작성해주세요", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploader(myInfo newInfo, String uid){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Info").document(uid)
                .set(newInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "정보 작성 완료", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "정보 작성 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}

package com.example.myapplication;

import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class writeInfo extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText writerName;
    private EditText writerPhone;
    private EditText parentName;
    private EditText parentPhone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        writerName = findViewById(R.id.userName);
        writerPhone = findViewById(R.id.userPhone);
        parentName = findViewById(R.id.parentName);
        parentPhone = findViewById(R.id.parentPhone);

        // 기존 정보 불러오기
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            DocumentReference docRef = FirebaseFirestore.getInstance().collection("Info").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    try {
                        myInfo info = documentSnapshot.toObject(myInfo.class);
                        writerName.setText(info.getWriterName());
                        writerPhone.setText(info.getWriterPhone());
                        parentName.setText(info.getParentName());
                        parentPhone.setText(info.getParentPhone());
                    }catch (Exception e){
                        //
                    }
                }
            });
        }

        // 신규 및 갱신 정보 등록하기
        Button writeInfo = findViewById(R.id.btnWriteInput);
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
            Toast.makeText(getApplicationContext(), "정보 작성에 실패하였습니다.\n모든 정보란을 작성해주세요!", Toast.LENGTH_LONG).show();
        }
    }

    private void uploader(myInfo newInfo, String uid){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Info").document(uid)
                .set(newInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "정보가 저장되었습니다!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "정보 작성에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}

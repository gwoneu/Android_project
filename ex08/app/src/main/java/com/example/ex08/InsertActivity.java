package com.example.ex08;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InsertActivity extends AppCompatActivity {

    FirebaseUser user;
    FirebaseFirestore db;
    EditText title, price;
    String strFile;
    ImageView image;
    FirebaseStorage storage;
    ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();

        getSupportActionBar().setTitle("상품등록 | " + user.getEmail());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = findViewById(R.id.title);
        price = findViewById(R.id.price);
        image = findViewById(R.id.image);
        bar = findViewById(R.id.bar);

        //저장 버튼을 클릭할 때
        findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bar.setVisibility(View.VISIBLE);
                String strTitle = title.getText().toString();
                String strPrice = price.getText().toString();
                if(strTitle.equals("") || strPrice.equals("") || strFile.equals("")) {
                    Toast.makeText(InsertActivity.this, "상품명과 가격, 이미지를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }else {
                    AlertDialog.Builder box = new AlertDialog.Builder(InsertActivity.this);
                    box.setTitle("질의");
                    box.setMessage("상품을 등록하시겠습니까?");
                    box.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ShopVO vo = new ShopVO();
                            vo.setTitle(strTitle);
                            vo.setDate(sdf.format(new Date()));
                            vo.setEmail(user.getEmail());
                            vo.setPrice(Integer.parseInt(strPrice));
                            //insertShop(vo);

                            //새로운 파일 이름
                            String fileName = System.currentTimeMillis() + ".jpg";
                            //업로드할 파일 이름
                            Uri file = Uri.fromFile(new File(strFile));

                            StorageReference ref = storage.getReference("/images/" + fileName);
                            ref.putFile(file)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    vo.setImage((uri.toString()));
                                                    insertShop(vo);
                                                    bar.setVisibility(View.GONE);
                                                }
                                            });
                                        }
                                    });
                        }
                    });
                    box.setNegativeButton("아니오", null);
                    box.show();
                }
            }
        });

        findViewById(R.id.image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);
            }
        });

    } //onCreate

    //미리 보기


    @SuppressLint("Range")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0) {
            Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
            cursor.moveToFirst();
            strFile = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            //System.out.println("................" + strFile);
            cursor.close();
            image.setImageBitmap(BitmapFactory.decodeFile(strFile));
        }
    }

    //상품 등록
    public void insertShop(ShopVO vo) {
        db.collection("shop")
                .add(vo)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(InsertActivity.this, "등록 성공", Toast.LENGTH_SHORT).show();
                            finish();
                        }else {
                            Toast.makeText(InsertActivity.this, "등록 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //뒤로 가기 버튼 메소드
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
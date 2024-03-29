package com.example.ex07;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    FirebaseUser user;
    FirebaseDatabase db;
    DatabaseReference ref; //어디에 저장(위치 지정)
    List<ChatVO> array = new ArrayList<>();
    ChatAdapter adapter = new ChatAdapter();
    RecyclerView list;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        user = FirebaseAuth.getInstance().getCurrentUser();

        db = FirebaseDatabase.getInstance();

        getList();

        list = findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(this));

        //registerForContextMenu(list);

        getSupportActionBar().setTitle("채팅 : " + user.getEmail());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText contents = findViewById(R.id.edtContents);
                String strContents = contents.getText().toString();
                if(strContents.equals("")) {
                    Toast.makeText(ChatActivity.this, "내용을 입력하세요.", Toast.LENGTH_SHORT).show();
                }else {
                    //전송
                    ChatVO vo = new ChatVO();
                    vo.setEmail(user.getEmail());
                    vo.setContents(strContents);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
                    String strDate = sdf.format(new Date());
                    vo.setDate(strDate);
                    //System.out.println("................." + vo.toString()); //데이터 확인

                    ref = db.getReference("/chat").push();
                    vo.setKey(ref.getKey());
                    ref.setValue(vo);
                    //System.out.println("................." + vo.toString());

                    contents.setText("");
                }
            }
        });
    }

    //채팅 데이터 읽어오기
    public void getList() {
        ref = db.getReference("chat");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatVO vo = snapshot.getValue(ChatVO.class);
                array.add(vo);
                adapter.notifyDataSetChanged();
                list.scrollToPosition(array.size()-1);
                //System.out.println(vo.toString());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                ChatVO vo = snapshot.getValue(ChatVO.class);
                for(ChatVO chat:array) {
                    if(chat.getKey().equals(vo.getKey())) {
                        array.remove(chat);
                        break;
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //어댑터
    class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

        @NonNull
        @Override
        public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_chat, parent, false);
            return new ViewHolder(view);
        }

        //내용 가져오기
        @Override
        public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
            ChatVO vo = array.get(position);
            holder.txtContents.setText(vo.getContents());
            holder.txtDate.setText(vo.getDate());
            holder.txtEmail.setText(vo.getEmail());

            LinearLayout.LayoutParams pContent = (LinearLayout.LayoutParams) holder.txtContents.getLayoutParams();
            LinearLayout.LayoutParams pDate = (LinearLayout.LayoutParams) holder.txtDate.getLayoutParams();

            /*
            if(vo.getEmail().equals(user.getEmail())) {
                //자신이 쓴 채팅에는 자신의 이메일 안보이게 하기
                holder.txtEmail.setVisibility(View.GONE);
                //자신의 채팅은 오른쪽으로 정렬하기
                pContent.gravity = Gravity.RIGHT;
                pDate.gravity = Gravity.RIGHT;
            }
            */

            if(vo.getEmail().equals(user.getEmail())) {
                pContent.gravity = Gravity.RIGHT;
                pDate.gravity = Gravity.RIGHT;
                holder.txtEmail.setVisibility(View.GONE);
            }else {
                pContent.gravity = Gravity.LEFT;
                pDate.gravity = Gravity.LEFT;
                holder.txtEmail.setVisibility(View.VISIBLE);
            }

            //내용을 길게 눌렀을 때
            holder.txtContents.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(vo.getEmail().equals(user.getEmail())) {
                        AlertDialog.Builder box = new AlertDialog.Builder(ChatActivity.this);
                        box.setTitle("메뉴를 선택하세요.");
                        box.setItems(new String[]{"삭제", "취소"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == 0) {
                                    //db에서 선택한 데이터 삭제
                                    db.getReference("chat/" + vo.getKey()).removeValue();
                                }
                            }
                        });
                        box.show();
                    }
                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return array.size();
        }

        //id 읽기
        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtContents, txtDate, txtEmail;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                txtContents = itemView.findViewById(R.id.txtContents);
                txtDate = itemView.findViewById(R.id.txtDate);
                txtEmail = itemView.findViewById(R.id.txtEmail);
            }
        }
    }

    //Contents를 꾹 눌렀을 때 나오는 메뉴
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("선택");
        menu.add(0, 1, 0, "삭제");
    }
} //Activity
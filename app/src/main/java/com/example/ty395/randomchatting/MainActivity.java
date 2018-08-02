package com.example.ty395.randomchatting;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    String USER_NAME;
    RecyclerView recyclerView;
    ImageButton send;
    ImageButton dialog;
    EditText chat_message;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        String room_key;

        //viewc초기화
        recyclerView = (RecyclerView) findViewById(R.id.list);
        send = (ImageButton) findViewById(R.id.send);
        chat_message = (EditText) findViewById(R.id.chat_message);

        //이전 액티비티에서 userName과 방번호 가져오기
        Intent intent = getIntent();
        USER_NAME = intent.getStringExtra("username");
        room_key=intent.getStringExtra("roomkey");

                //해당 방번호의 DB목록 가져오기(없으면 해당 방을 방번호로 생성)
                databaseReference = firebaseDatabase.getReference(room_key);
                Log.d("main",room_key);

                dialog=(ImageButton)findViewById(R.id.image);
                final ArrayList<ChatData> singModles = new ArrayList<>();

                final RecycleAdapter adapter = new RecycleAdapter(MainActivity.this, R.layout.recycler_item, singModles);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setAdapter(adapter);


                send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                ChatData chatData = new ChatData(USER_NAME, chat_message.getText().toString());  // 유저 이름과 메세지로 chatData 만들기
                        ChatData chatData = new ChatData();
                        chatData.setUsername(USER_NAME);
                chatData.setMymessage(chat_message.getText().toString());

                databaseReference.push().setValue(chatData);  // 기본 database 하위 message라는 child에 chatData를 list로 만들기
                chat_message.setText("");
            }
        });

        dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDialog customDialog = new CustomDialog(MainActivity.this);

                // 커스텀 다이얼로그를 호출한다.
                // 커스텀 다이얼로그의 결과를 출력할 TextView를 매개변수로 같이 넘겨준다.
                customDialog.CallFuntion();
            }
        });

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("addChildEventListener", dataSnapshot.getChildrenCount()+"");

                if(dataSnapshot.getChildrenCount()>0){
                    ChatData chatData = dataSnapshot.getValue(ChatData.class);
                    Log.d("addChildEventListener", chatData.getUsername());

                    singModles.add(chatData);
                    adapter.notifyDataSetChanged();

                    if(chatData.getUsername().equals(USER_NAME)){
                        chatData.setType(ChatData.MY_TYPE);
                        chatData.setMessage(chatData.getMymessage());

                    }else{
                        chatData.setType(ChatData.YOUR_TYPE);
                        chatData.setMessage(chatData.getMymessage());
                    }
                    recyclerView.scrollToPosition(adapter.getItemCount()-1);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Toast.makeText(getApplicationContext(), "방을 나가셨습니다. 새로운 사람을 매칭하세요", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        databaseReference.removeValue();
    }


}

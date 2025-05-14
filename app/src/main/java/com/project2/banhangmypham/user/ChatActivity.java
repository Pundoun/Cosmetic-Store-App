package com.project2.banhangmypham.user;

import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project2.banhangmypham.adapter.MessageAdapter;
import com.project2.banhangmypham.common_screen.LoginActivity;
import com.project2.banhangmypham.databinding.ActivityChatBinding;
import com.project2.banhangmypham.model.Message;
import com.project2.banhangmypham.model.User;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    public static final String TAG = "ChatActivity";
    ActivityChatBinding binding ;
    List<com.project2.banhangmypham.model.Message> messageList = new ArrayList<>();
    MessageAdapter messageAdapter ;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference("Chats");
    User currentUser ;
    ViewTreeObserver.OnGlobalLayoutListener viewTreeObserver ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        messageAdapter = new MessageAdapter(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                currentUser = bundle.getParcelable("loginState", User.class);
            }else {
                currentUser = bundle.getParcelable("loginState");
            }
        }
        viewTreeObserver = () -> {
            // Get the visible frame of the screen
            Rect rect = new Rect();
            binding.getRoot().getWindowVisibleDisplayFrame(rect);

            int screenHeight = binding.getRoot().getRootView().getHeight(); // Total screen height
            int keyboardHeight = screenHeight - rect.bottom; // Height of the keyboard

            // Adjust the message layout based on keyboard visibility
            if (keyboardHeight > screenHeight * 0.15) {
                binding.llMessage.setTranslationY(-keyboardHeight);
                ViewGroup.MarginLayoutParams layoutParams =
                        (ViewGroup.MarginLayoutParams) binding.rcvChat.getLayoutParams();
                layoutParams.setMargins(0, 0, 0, keyboardHeight + 150); // Left, Top, Right, Bottom (in pixels)
                binding.rcvChat.scrollToPosition(messageList.size() - 1);
                binding.rcvChat.setLayoutParams(layoutParams);
            } else {
                binding.llMessage.setTranslationY(0);
                ViewGroup.MarginLayoutParams layoutParams =
                        (ViewGroup.MarginLayoutParams) binding.rcvChat.getLayoutParams();
                layoutParams.setMargins(0, 0, 0, 0); // Left, Top, Right, Bottom (in pixels)
                binding.rcvChat.scrollToPosition(messageList.size() - 1);
                binding.rcvChat.setLayoutParams(layoutParams);
            }
        };

        binding.nameTitle.setText("Admin");
        assert currentUser != null;
        messageAdapter.setCurrentUserId(currentUser.get_id());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        databaseReference.child(currentUser.get_id()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    Log.d(TAG, "onDataChange: ====> data" );
                    messageList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        com.project2.banhangmypham.model.Message message = dataSnapshot.getValue(Message.class);
                        Log.d(TAG, "onDataChange: ====> message = " + message );
                        messageList.add(message);
                    }
                    messageAdapter.submitList(messageList);
                    binding.rcvChat.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        binding.rcvChat.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.rcvChat.hasFixedSize();
        binding.rcvChat.setAdapter(messageAdapter);
        binding.ivBack.setOnClickListener(view -> {
            finish();
        });
        binding.btnSend.setOnClickListener(view ->{
            Message message = new Message();
            message.setIdSender(currentUser.get_id());
            message.setMessage(binding.edtMessage.getText().toString());
            message.setUserUrlImage(currentUser.getProfile_image());
            message.setTimeSent(System.currentTimeMillis());
            String idMessage = databaseReference.push().getKey();
            message.setIdMessage(idMessage);
            databaseReference.child(currentUser.get_id()).child(idMessage).setValue(message);
            binding.edtMessage.setText("");
        });
    }


    @Override
    protected void onStop() {
        binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(viewTreeObserver);
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(viewTreeObserver);

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}
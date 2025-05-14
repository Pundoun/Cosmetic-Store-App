package com.project2.banhangmypham.admin.chat;

import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.project2.banhangmypham.R;
import com.project2.banhangmypham.adapter.MessageAdapter;
import com.project2.banhangmypham.common_screen.LoginActivity;
import com.project2.banhangmypham.databinding.ActivityAdminChatBinding;
import com.project2.banhangmypham.firebase.ChatManager;
import com.project2.banhangmypham.firebase.IEventMessage;
import com.project2.banhangmypham.model.Message;
import com.project2.banhangmypham.model.User;

import java.util.ArrayList;
import java.util.List;

public class AdminChatActivity extends AppCompatActivity {

    ActivityAdminChatBinding binding;
    MessageAdapter messageAdapter;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference("Chats");
    User currentUser;
    User currentAdmin;
    ViewTreeObserver.OnGlobalLayoutListener viewTreeObserver ;
    List<Message> messageList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAdminChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        messageAdapter = new MessageAdapter(this);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                currentUser = bundle.getParcelable("loginState", User.class);
                currentAdmin = bundle.getParcelable("adminState", User.class);
            }else {
                currentUser = bundle.getParcelable("loginState");
                currentAdmin = bundle.getParcelable("adminState");
            }
        }
        assert currentUser != null;
        binding.nameTitle.setText(currentUser.getUsername());
        messageAdapter.setCurrentUserId(currentAdmin.get_id());
        ChatManager.getInstance().setUserId(currentUser.get_id());
        ChatManager.getInstance().setEventListener(new IEventMessage() {
            @Override
            public void onReceived(List<Message> data) {
                messageList.clear();
                messageList.addAll(data);
                messageAdapter.submitList(data);
                binding.rcvChat.scrollToPosition(data.size() - 1);
            }

            @Override
            public void onError(Throwable throwable) {

            }
        });

        ChatManager.getInstance().registerMessage();
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewTreeObserver = () -> {
            // Get the visible frame of the screen
            Rect rect = new Rect();
            binding.getRoot().getWindowVisibleDisplayFrame(rect);

            int screenHeight = binding.getRoot().getRootView().getHeight(); // Total screen height
            int keyboardHeight = screenHeight - rect.bottom; // Height of the keyboard

            // Adjust the message layout based on keyboard visibility
            if (keyboardHeight > screenHeight * 0.15) {
                // Keyboard is visible
                binding.llMessage.setTranslationY(-keyboardHeight);
                binding.rcvChat.scrollToPosition(messageList.size() - 1);
                ViewGroup.MarginLayoutParams layoutParams =
                        (ViewGroup.MarginLayoutParams) binding.rcvChat.getLayoutParams();
                layoutParams.setMargins(0, 0, 0, keyboardHeight + 150); // Left, Top, Right, Bottom (in pixels)
                binding.rcvChat.setLayoutParams(layoutParams);
            } else {
                // Keyboard is hidden
                binding.llMessage.setTranslationY(0);
                ViewGroup.MarginLayoutParams layoutParams =
                        (ViewGroup.MarginLayoutParams) binding.rcvChat.getLayoutParams();
                layoutParams.setMargins(0, 0, 0, 0); // Left, Top, Right, Bottom (in pixels)
                binding.rcvChat.setLayoutParams(layoutParams);

            }
        };

        binding.rcvChat.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.rcvChat.hasFixedSize();
        binding.rcvChat.setAdapter(messageAdapter);
        binding.ivBack.setOnClickListener(view -> {
            finish();
        });
        binding.btnSend.setOnClickListener(view -> {
            Message message = new Message();
            message.setIdSender(currentAdmin.get_id());
            message.setMessage(binding.edtMessage.getText().toString());
            message.setTimeSent(System.currentTimeMillis());
            message.setUserUrlImage(currentAdmin.getProfile_image());
            String idMessage = databaseReference.push().getKey();
            message.setIdMessage(idMessage);
            databaseReference.child(currentUser.get_id()).child(idMessage).setValue(message);
            binding.edtMessage.setText("");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ChatManager.getInstance().registerMessage();
        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(viewTreeObserver);

    }

    @Override
    protected void onStop() {
        super.onStop();
        ChatManager.getInstance().unRegisterMessage();
        binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(viewTreeObserver);

    }
}
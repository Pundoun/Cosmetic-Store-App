package com.project2.banhangmypham.admin.admin_profile;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project2.banhangmypham.R;
import com.project2.banhangmypham.adapter.UserManagementAdapter;
import com.project2.banhangmypham.admin.chat.AdminChatActivity;
import com.project2.banhangmypham.admin.repository.UserRepositoryImpl;
import com.project2.banhangmypham.admin.viewmodel.UserViewModel;
import com.project2.banhangmypham.databinding.ActivityUserManagementBinding;
import com.project2.banhangmypham.model.User;

public class UserManagementActivity extends AppCompatActivity implements UserManagementAdapter.IUserEventItem {

    ActivityUserManagementBinding binding ;
    UserManagementAdapter userManagementAdapter;
    UserViewModel userViewModel;
    User adminUser ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityUserManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                adminUser = bundle.getParcelable("adminState", User.class);
            }else {
                adminUser = bundle.getParcelable("adminState");
            }
        }

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.setUserRepository(new UserRepositoryImpl());
        userViewModel.getAllUsersList();
        userViewModel.getUsersListLiveData().observe(this, result ->{
            if (result != null && result.getData() != null){
                userManagementAdapter.submitList(result.getData());
            }
        });

        userViewModel.getDeleteUserLiveData().observe(this, result -> {
            if (result != null  && result.getData() != null) {
                userManagementAdapter.submitList(result.getData());
            }
        });
        userManagementAdapter = new UserManagementAdapter(this, this);

        binding.rcvUser.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.rcvUser.setHasFixedSize(true);
        binding.rcvUser.setAdapter(userManagementAdapter);

        binding.ivBack.setOnClickListener(view -> finish());
    }

    @Override
    public void onClickItem(User user) {
        Intent intentUser = new Intent(this , AdminChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("loginState", user);
        bundle.putParcelable("adminState", adminUser);
        intentUser.putExtras(bundle);
        startActivity(intentUser);
    }

    @Override
    public void onDeleteItem(User user) {
        // call api delete user
        userViewModel.deleteUserById(user.get_id());
    }

    @Override
    protected void onStop() {
        super.onStop();
        userViewModel.clear();
    }
}
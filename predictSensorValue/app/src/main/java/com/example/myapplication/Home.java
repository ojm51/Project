package com.example.myapplication;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapplication.databinding.ActivityHomeBinding;

public class Home extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_detection, R.id.navigation_home, R.id.navigation_info).build();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager() .findFragmentById(R.id.nav_host_fragment_activity_home);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_home);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);   // 주석 풀면 오류남 - 이유 모름(https://yooniversal.github.io/blog/post169/)
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

}
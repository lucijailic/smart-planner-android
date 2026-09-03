package com.smartplanner.app;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.smartplanner.app.fragments.EventsFragment;
import com.smartplanner.app.fragments.HomeFragment;
import com.smartplanner.app.fragments.ProfileFragment;
import com.smartplanner.app.fragments.StatisticsFragment;
import com.smartplanner.app.fragments.TasksFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initViews();
        setupBottomNavigation();
        setupBackNavigation();

        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupBottomNavigation() {

        bottomNavigation.setOnItemSelectedListener(item -> {

            Fragment selectedFragment;

            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {

                selectedFragment = new HomeFragment();

            } else if (itemId == R.id.nav_tasks) {

                selectedFragment = new TasksFragment();

            } else if (itemId == R.id.nav_events) {

                selectedFragment = new EventsFragment();

            } else if (itemId == R.id.nav_statistics) {

                selectedFragment = new StatisticsFragment();

            } else if (itemId == R.id.nav_profile) {

                selectedFragment = new ProfileFragment();

            } else {

                return false;
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.fragmentContainer,
                            selectedFragment
                    )
                    .commit();

            return true;
        });
    }

    private void setupBackNavigation() {

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (bottomNavigation.getSelectedItemId()
                                != R.id.nav_home) {

                            bottomNavigation.setSelectedItemId(
                                    R.id.nav_home
                            );

                        } else {

                            setEnabled(false);

                            getOnBackPressedDispatcher()
                                    .onBackPressed();
                        }
                    }
                }
        );
    }
}
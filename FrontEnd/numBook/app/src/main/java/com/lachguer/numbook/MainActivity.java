package com.lachguer.numbook;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ContactAdapter adapter;
    private ContactViewModel viewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextInputLayout searchLayout;
    private EditText searchInput;
    private static final int PERMISSION_READ_CONTACTS = 1;
    private List<String> allContacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.rv_contacts);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        searchLayout = findViewById(R.id.search_layout);
        searchInput = findViewById(R.id.search_input);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        observeViewModel();

        setupSearchBar();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.loadContacts();
            swipeRefreshLayout.setRefreshing(false);
        });

        checkPermissions();
    }

    private void setupSearchBar() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterContacts(String query) {
        List<String> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(allContacts);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (String contact : allContacts) {
                String[] parts = contact.split("\n");
                String name = parts[0].toLowerCase();
                String number = parts.length > 1 ? parts[1] : "";

                if (name.contains(lowerCaseQuery) || number.contains(query)) {
                    filteredList.add(contact);
                }
            }
        }
        adapter.updateContacts(filteredList);
    }

    private void observeViewModel() {
        viewModel.getContacts().observe(this, contacts -> {
            allContacts = contacts;
            adapter.updateContacts(contacts);
        });

        viewModel.getErrors().observe(this, error -> {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSION_READ_CONTACTS);
        } else {
            viewModel.loadContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_READ_CONTACTS &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            viewModel.loadContacts();
        }
    }
}
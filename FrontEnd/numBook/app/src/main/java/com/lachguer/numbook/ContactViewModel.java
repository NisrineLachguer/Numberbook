package com.lachguer.numbook;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContactViewModel extends AndroidViewModel {
    private static final String PREFS_NAME = "NumBookPrefs";
    private static final String USER_SYNCED_KEY = "user_synced";

    private final MutableLiveData<List<String>> contactsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> successLiveData = new MutableLiveData<>();
    private final ApiService apiService;
    private final Set<String> syncedContacts = new HashSet<>();
    private Long currentUserId = null;

    public ContactViewModel(@NonNull Application application) {
        super(application);
        apiService = new ApiService(application);
        contactsLiveData.setValue(new ArrayList<>());
        initializeEmulatorUser();
    }

    public LiveData<List<String>> getContacts() {
        return contactsLiveData;
    }

    public LiveData<String> getErrors() {
        return errorLiveData;
    }

    public LiveData<String> getSuccessMessages() {
        return successLiveData;
    }

    @SuppressLint({"HardwareIds", "MissingPermission"})
    private void initializeEmulatorUser() {
        SharedPreferences prefs = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String imei = Settings.Secure.getString(getApplication().getContentResolver(), Settings.Secure.ANDROID_ID);
        String phoneNumber = "";

        try {
            TelephonyManager tm = (TelephonyManager) getApplication().getSystemService(Context.TELEPHONY_SERVICE);

            if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                phoneNumber = tm.getLine1Number();

                if (phoneNumber == null || phoneNumber.isEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        phoneNumber = tm.getSimOperator();
                    }

                    if ((phoneNumber == null || phoneNumber.isEmpty()) && Build.FINGERPRINT.startsWith("generic")) {
                        phoneNumber = getEmulatorPhoneNumberFromProperties();
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String tmpImei = tm.getImei();
                    if (tmpImei != null) imei = tmpImei;
                } else {
                    String tmpDeviceId = tm.getDeviceId();
                    if (tmpDeviceId != null) imei = tmpDeviceId;
                }
            }
        } catch (Exception e) {
            Log.w("ContactViewModel", "Error getting phone info", e);
        }

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            phoneNumber = "+16 505556789";
            Log.w("ContactViewModel", "Using default phone number");
        }

        phoneNumber = phoneNumber.replaceAll("[^0-9+]", "");

        Log.d("ContactViewModel", "Final IMEI: " + imei + ", Phone: " + phoneNumber);

        apiService.createUser(imei, phoneNumber, new ApiService.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    currentUserId = jsonResponse.getLong("id");
                    Log.d("ContactViewModel", "User created with ID: " + currentUserId);
                    successLiveData.postValue("Données émulateur envoyées avec succès");
                    loadContacts();
                } catch (JSONException e) {
                    errorLiveData.postValue("Error parsing user response");
                }
            }

            @Override
            public void onError(String error) {
                errorLiveData.postValue("User creation error: " + error);
                loadContacts();
            }
        });
    }

    private String getEmulatorPhoneNumberFromProperties() {
        try {
            Process process = Runtime.getRuntime().exec("getprop ril.iccid.sim1");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null && !line.trim().isEmpty()) {
                return line.trim();
            }

            process = Runtime.getRuntime().exec("getprop ro.telephony.imei");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            line = reader.readLine();
            if (line != null && !line.trim().isEmpty()) {
                return line.trim();
            }
        } catch (Exception e) {
            Log.e("ContactViewModel", "Error reading emulator properties", e);
        }
        return "";
    }

    public void loadContacts() {
        List<String> contacts = new ArrayList<>();
        ContentResolver cr = getApplication().getContentResolver();

        try (Cursor phones = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                null, null, null)) {

            if (phones != null) {
                int nameIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numberIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                while (phones.moveToNext()) {
                    String name = phones.getString(nameIndex);
                    String number = phones.getString(numberIndex);
                    String normalizedNumber = number.replaceAll("[^0-9]", "");
                    String contactEntry = name + "\n" + normalizedNumber;

                    contacts.add(contactEntry);

                    if (!syncedContacts.contains(contactEntry)) {
                        addContactToServer(name, normalizedNumber);
                    }
                }
            }
        } catch (Exception e) {
            errorLiveData.postValue("Error loading contacts");
            Log.e("ContactViewModel", "Error loading contacts", e);
        }

        contactsLiveData.postValue(contacts);
    }

    private void addContactToServer(String name, String number) {
        String contactKey = name + "|" + number;
        if (currentUserId == null || syncedContacts.contains(contactKey)) {
            return;
        }

        apiService.addContact(name, number, new ApiService.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                syncedContacts.add(contactKey);
                Log.d("ContactViewModel", "Contact synced: " + name);
            }

            @Override
            public void onError(String error) {
                errorLiveData.postValue("Sync failed for " + name + ": " + error);
                new Handler().postDelayed(() -> addContactToServer(name, number), 5000);
            }
        });
    }
}
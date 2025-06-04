package com.example.gpsandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.example.gpsandroid.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FusedLocationProviderClient locationProviderClient;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        binding.btnFind.setOnClickListener(v -> getLocation());
    }

    @SuppressLint("SetTextI18n")
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, 10);
            }
            return;
        }

        locationProviderClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Tampilkan koordinat GPS
                        binding.latitude.setText(String.format(Locale.getDefault(), "%.6f", location.getLatitude()));
                        binding.longitude.setText(String.format(Locale.getDefault(), "%.6f", location.getLongitude()));
                        binding.altitude.setText(String.format(Locale.getDefault(), "%.2f meters", location.getAltitude()));
                        binding.akurasi.setText(String.format(Locale.getDefault(), "%.2f meters", location.getAccuracy()));

                        // Dapatkan alamat dari koordinat
                        getCompleteAddress(location.getLatitude(), location.getLongitude());
                    } else {
                        Toast.makeText(this, "Lokasi tidak tersedia. Pastikan GPS aktif.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(this, "Gagal mendapatkan lokasi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.alamat.setText("Gagal mendapatkan lokasi");
                });
    }

    private void getCompleteAddress(double latitude, double longitude) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // Format alamat lengkap
                String fullAddress = formatAddress(address);
                binding.alamat.setText(fullAddress);
            } else {
                binding.alamat.setText("Alamat tidak ditemukan untuk koordinat ini");
            }
        } catch (IOException e) {
            e.printStackTrace();
            binding.alamat.setText("Error: " + e.getMessage());
            Toast.makeText(this, "Gagal mendapatkan alamat", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatAddress(Address address) {
        StringBuilder sb = new StringBuilder();

        // Nama jalan
        if (address.getThoroughfare() != null) {
            sb.append("Jalan: ").append(address.getThoroughfare()).append("\n");
        }

        // Nomor jalan
        if (address.getSubThoroughfare() != null) {
            sb.append("No: ").append(address.getSubThoroughfare()).append("\n");
        }

        // Kelurahan/Desa
        if (address.getSubLocality() != null) {
            sb.append("Kelurahan: ").append(address.getSubLocality()).append("\n");
        }

        // Kecamatan
        if (address.getLocality() != null) {
            sb.append("Kecamatan: ").append(address.getLocality()).append("\n");
        }

        // Kabupaten/Kota
        if (address.getSubAdminArea() != null) {
            sb.append("Kota/Kab: ").append(address.getSubAdminArea()).append("\n");
        }

        // Provinsi
        if (address.getAdminArea() != null) {
            sb.append("Provinsi: ").append(address.getAdminArea()).append("\n");
        }

        // Kode Pos
        if (address.getPostalCode() != null) {
            sb.append("Kode Pos: ").append(address.getPostalCode()).append("\n");
        }

        // Negara
        if (address.getCountryName() != null) {
            sb.append("Negara: ").append(address.getCountryName());
        }

        return sb.toString();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 10) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Izin lokasi diperlukan untuk mendapatkan posisi Anda", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
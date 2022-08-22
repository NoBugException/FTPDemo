package com.nobug.ftpservice;

import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.TwoStatePreference;

import androidx.appcompat.app.AppCompatActivity;

import com.nobug.ftpservice.databinding.ActivityFtpServiceBinding;
import com.nobug.ftpservice.gui.MyPreferenceFragment;

import net.vrallev.android.cat.Cat;

import java.net.InetAddress;

public class FtpServiceActivity extends AppCompatActivity {

    private ActivityFtpServiceBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Cat.d("created");
        setTheme(FsSettings.getTheme());
        super.onCreate(savedInstanceState);
//        binding = ActivityFtpServiceBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MyPreferenceFragment())
                .commit();
    }

//    private void updateRunningState() {
//        Resources res = getResources();
//        TwoStatePreference runningPref = findPref("running_switch");
//        if (FsService.isRunning()) {
//            runningPref.setChecked(true);
//            // Fill in the FTP server address
//            InetAddress address = FsService.getLocalInetAddress();
//            if (address == null) {
//                Cat.v("Unable to retrieve wifi ip address");
//                runningPref.setSummary(R.string.running_summary_failed_to_get_ip_address);
//                return;
//            }
//            String ipText = "ftp://" + address.getHostAddress() + ":"
//                    + FsSettings.getPortNumber() + "/";
//            String summary = res.getString(R.string.running_summary_started, ipText);
//            runningPref.setSummary(summary);
//        } else {
//            runningPref.setChecked(false);
//            runningPref.setSummary(R.string.running_summary_stopped);
//        }
//    }

//    @SuppressWarnings({"unchecked"})
//    protected <T extends Preference> T findPref(CharSequence key) {
//        return (T) findPreference(key);
//    }
}
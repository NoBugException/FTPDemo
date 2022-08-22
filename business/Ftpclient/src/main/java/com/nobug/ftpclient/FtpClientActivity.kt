package com.nobug.ftpclient

import android.os.Bundle
import com.luxshare.common.ScopedActivity
import com.nobug.ftpclient.databinding.ActivityFtpClientBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FtpClientActivity : ScopedActivity() {

    private lateinit var binding: ActivityFtpClientBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityFtpClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btUpload.setOnClickListener {
            FTPUtils.getInstance().initFtpClient("192.168.1.29", 2121, "admin", "123456")
            launch(Dispatchers.IO) {
                val ftpSavePath = "/ftp/"
                val ftpSaveFileName = "lt7911_copy.bin"
                val originFileName = "/storage/emulated/0/ZZPTS/ota/LT7911/lt7911.bin"
                FTPUtils.getInstance().uploadFile(ftpSavePath, ftpSaveFileName, originFileName)
            }
        }
    }
}
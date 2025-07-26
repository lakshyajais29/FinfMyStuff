package com.example.findr

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

object CloudinaryUtil {

    private var initialized = false

    fun initCloudinary(context: Context) {
        if (initialized) return

        val config: HashMap<String, String> = HashMap()
        config["cloud_name"] = "driulxei4"  // ✅ Replace with your cloud name
        config["upload_preset"] = "upload_preset_android" // 🔓 Unsigned preset only

        MediaManager.init(context, config)
        initialized = true
    }

    fun uploadImage(
        fileUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        MediaManager.get().upload(fileUri)
            .unsigned("upload_preset_android")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d("Cloudinary", "Upload started")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: MutableMap<Any?, Any?>?) {
                    val url = resultData?.get("secure_url") as? String
                    if (url != null) onSuccess(url) else onError("Upload failed.")
                }

                override fun onError(requestId: String, error: ErrorInfo?) {
                    onError("Upload error: ${error?.description}")
                }

                override fun onReschedule(requestId: String, error: ErrorInfo?) {
                    onError("Reschedule error: ${error?.description}")
                }
            })
            .dispatch()
    }
}

package com.example.intelli_tags

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_second.view.*
import kotlinx.android.synthetic.main.fragment_third.view.*
import org.json.JSONObject
import java.io.File
import java.lang.StringBuilder

/**
 * A simple [Fragment] subclass.
 */

class ThirdFragment : Fragment() {
    private lateinit var viewOfLayout3rd: View
    private var videoUri: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOfLayout3rd = inflater.inflate(R.layout.fragment_third, container, false)
        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.configure(requireContext())
            Log.i("MyAmplifyApp", "Initialized Amplify")
//            uploadFile()
        } catch (error: AmplifyException) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
        }
        viewOfLayout3rd.button2.setOnClickListener {
            Log.i("hello", "hello")
            launchGallery()

        }
        return viewOfLayout3rd
    }


    private fun launchGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            videoUri = data?.data
            Log.i("hogaya", "andar")
            Log.i("hogaya", videoUri.toString())
            uploadVideoToS3(videoUri)
        }
    }

    private fun uploadVideoToS3(videoUri: Uri?) {
        val stream = videoUri?.let { requireContext().contentResolver.openInputStream(it) }
        if (stream != null) {
            Amplify.Storage.uploadInputStream("Video.mp4", stream, {
                Log.i("MyAmplifyApp", "Successfully uploaded: ${it.key}")

                getJobId()
            }, {
                Log.e("MyAmplifyApp", "Upload failed", it)
            })
        }
    }

    private fun getJobId() {
        //api call to get the jobId

        val url = "https://app.modzy.com/api/jobs"

        //body
        val finalBody = JSONObject()
        val temp1 = JSONObject()
        temp1.put("identifier", "cyoxn54q5g")
        temp1.put("version", "0.0.2")

        finalBody.put("model", temp1)

//        val temp2 = JSONObject()
//        temp2.put("bucket", "modzybucket35738-dev")
//        temp2.put("key", "public/config.json")

//        val confiWaliBody = JSONObject()

        //daalni hai
//        confiWaliBody.put("config.json", temp2)

        val temp3 = JSONObject()
        temp3.put("bucket", "modzybucket35738-dev")
        temp3.put("key", "public/Video.mp4")

        val inputWaliBody = JSONObject()
        inputWaliBody.put("input", temp3)
//        inputWaliBody.put("config.json", temp2)

//        val body0001 = JSONObject()
        val sourcesWaliBaat = JSONObject()
        sourcesWaliBaat.put("0001", inputWaliBody)

        val properInput = JSONObject()
        properInput.put("type", "aws-s3")
        properInput.put("accessKeyID", "AKIATLNIEWDMNMKGF4EF")
        properInput.put("secretAccessKey", "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")
        properInput.put("region", "us-east-2")
        properInput.put("sources", sourcesWaliBaat)

        finalBody.put("input", properInput)
        Log.i("body", finalBody.toString())

        val queue = Volley.newRequestQueue(viewOfLayout3rd.context)
        var response = ""
        val req = object : JsonObjectRequest(
            Method.POST, url, finalBody,
            {
                Log.i("inside", "Inside Api Call")

                response = it.getString("jobIdentifier")
                Handler().postDelayed({ getTextOut(response) }, 60000)
                Log.i("identifier", response)
                Toast.makeText(requireContext(), "Api Call success", Toast.LENGTH_SHORT).show()

            }, {
                Toast.makeText(requireContext(), "Api Call Failed", Toast.LENGTH_SHORT).show()
                Log.i("status code", it.message.toString())
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headerMap = mutableMapOf<String, String>()
                headerMap["Authorization"] = "ApiKey KSQslWseSzQ3hfcWeC0A.lMIZQC7rTsApVTnDeArW"
                headerMap["Content-Type"] = "application/json"
                headerMap["Accept"] = "application/json"
                headerMap["User-Agent"] = "PostmanRuntime/7.28.4"
                return headerMap
            }
        }
        queue.add(req)
    }

    private fun getTextOut(response: String) {
        Log.i("status", response)

        val queue2 = Volley.newRequestQueue(viewOfLayout3rd.context)
        val req = object : JsonObjectRequest(
            Method.GET, "https://app.modzy.com/api/results/$response", null,
            {
                Log.i("inside", "Inside Api Call")
                val outputText = (it.getJSONObject("results")).getJSONObject("0001")
                    .getJSONObject("results.json").getString("caption")
                processText(outputText)
                Log.i("Text", outputText.toString())
                Toast.makeText(requireContext(), "Api Call success", Toast.LENGTH_SHORT).show()

            }, {
                Toast.makeText(requireContext(), "Api Call Failed second", Toast.LENGTH_SHORT)
                    .show()
                Log.i("status code", it.message.toString())
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headerMap = mutableMapOf<String, String>()
                headerMap["Authorization"] = "ApiKey KSQslWseSzQ3hfcWeC0A.lMIZQC7rTsApVTnDeArW"
                headerMap["Content-Type"] = "application/json"
                headerMap["Accept"] = "application/json"
                headerMap["User-Agent"] = "PostmanRuntime/7.28.4"
                return headerMap
            }
        }
        queue2.add(req)
    }

    private fun processText(text: String) {

//        val text =
//            "API is the acronym for Application Programming Interface, which is a software intermediary that allows two applications to talk to each other. Each time you use an app like Facebook, send an instant message, or check the weather on your phone, you're using an API."
        val url = "https://app.modzy.com/api/jobs"
        val body = JSONObject()
        val body2 = JSONObject()
        body2.put("identifier", "m8z2mwe3pt")
        body2.put("version", "0.0.1")
        body.put("model", body2)
        val body4 = JSONObject()
        body4.put("input.txt", text)
        val body3 = JSONObject()
        body3.put("my-input", body4)
        val body5 = JSONObject()
        body5.put("type", "text")
        body5.put("sources", body3)

        //final body
        body.put("input", body5)

        Log.i("body", body.toString())

//
        val queue = Volley.newRequestQueue(viewOfLayout3rd.context)
        var response = ""
        val req = object : JsonObjectRequest(
            Method.POST, url, body,
            {
                Log.i("inside", "Inside Api Call")

                response = it.getString("jobIdentifier")
                Handler().postDelayed({ getTopics(response) }, 3000)
                Log.i("identifier", response)
                Toast.makeText(requireContext(), "Api Call success", Toast.LENGTH_SHORT).show()

            }, {
                Toast.makeText(requireContext(), "Api Call Failed", Toast.LENGTH_SHORT).show()
                Log.i("status code", it.message.toString())
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headerMap = mutableMapOf<String, String>()
                headerMap["Authorization"] = "ApiKey KSQslWseSzQ3hfcWeC0A.lMIZQC7rTsApVTnDeArW"
                headerMap["Content-Type"] = "application/json"
                headerMap["Accept"] = "application/json"
                headerMap["User-Agent"] = "PostmanRuntime/7.28.4"
                return headerMap
            }
        }
        queue.add(req)
    }

    fun getTopics(response: String) {
        Log.i("status", response)

        val queue2 = Volley.newRequestQueue(viewOfLayout3rd.context)
        val req = object : JsonObjectRequest(
            Method.GET, "https://app.modzy.com/api/results/$response", null,
            {
                Log.i("inside", "Inside Api Call")
                val topics = (it.getJSONObject("results")).getJSONObject("my-input")
                    .getJSONArray("results.json")
                Log.i("topics", topics.toString())
                var output = StringBuilder()
                for (i in 0 until topics.length())
                    output.append("#").append(topics[i]).append("\n")
                Log.i("topics", output.toString())
                viewOfLayout3rd.findViewById<TextView>(R.id.textView).text = output
                viewOfLayout3rd.button4.setOnClickListener {
                    copy_to_clipboard(output.toString())
                }
                Toast.makeText(requireContext(), "Api Call success", Toast.LENGTH_SHORT).show()

            }, {
                Toast.makeText(requireContext(), "Api Call Failed second", Toast.LENGTH_SHORT)
                    .show()
                Log.i("status code", it.message.toString())
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headerMap = mutableMapOf<String, String>()
                headerMap["Authorization"] = "ApiKey KSQslWseSzQ3hfcWeC0A.lMIZQC7rTsApVTnDeArW"
                headerMap["Content-Type"] = "application/json"
                headerMap["Accept"] = "application/json"
                headerMap["User-Agent"] = "PostmanRuntime/7.28.4"
                return headerMap
            }
        }
        queue2.add(req)
    }

    private fun copy_to_clipboard(topics: String) {
        val textToCopy = topics

        val clipboard =
            ContextCompat.getSystemService(
                requireContext(),
                ClipboardManager::class.java
            ) as ClipboardManager
        val clip = ClipData.newPlainText("label", textToCopy)
        clipboard!!.setPrimaryClip(clip)
    }

}
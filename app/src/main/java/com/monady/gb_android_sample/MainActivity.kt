package com.monady.gb_android_sample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gameball.gameball.GameballApp
import com.gameball.gameball.model.request.PlayerAttributes
import com.gameball.gameball.model.response.PlayerRegisterResponse
import com.gameball.gameball.network.Callback
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.monady.gb_android_sample.databinding.ActivityMainBinding


class NotificationsHandler: FirebaseMessagingService() {
    // TODO : Add GooglePlay Services json file to be able to use notifications
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        message.notification?.let {
            doSendBroadcast(it.title.toString(), it.body.toString())
            Log.d("NTX", "${it.title}: ${it.body}")
        }
    }

    private fun doSendBroadcast(title: String, body: String) {
        val it = Intent("EVENT_SNACKBAR")
        if (!TextUtils.isEmpty(title)) it.putExtra("GBX_N_TITLE", title)
        if (!TextUtils.isEmpty(body)) it.putExtra("GBX_N_BODY", body)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(it)
    }
}

class MainActivity : AppCompatActivity() {
    data class envConfig (val baseUrl: String, val widgetUrl: String)

    lateinit var binding: ActivityMainBinding

    lateinit var gameballApp: GameballApp

    lateinit var mMessageReceiver: BroadcastReceiver

    var playerAttributes = PlayerAttributes.Builder()

    val Tag = "GameballAppTest"

    var baseUrl: String? = null

    var widgetUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mMessageReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                intent.extras?.let {
                    var title = it.getString("GBX_N_TITLE")
                    var body = it.getString("GBX_N_BODY")
                    showSnackbar("${title}: ${body}")
                }
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
            IntentFilter("EVENT_SNACKBAR")
        );


        binding.btnAdditionalAdd.setOnClickListener{
            val additionalKey = binding.inputAdditionalKey.text.toString()
            val additionalValue = binding.inputAdditionalValue.text.toString()

            if(additionalKey.isNotEmpty() && additionalValue.isNotEmpty()){
                playerAttributes.withAdditionalAttribute(additionalKey, additionalValue)
                showSnackbar("Added $additionalKey: $additionalValue")
                binding.inputAdditionalKey.text.clear()
                binding.inputAdditionalValue.text.clear()
            }
            else{
                showSnackbar("Key and Value are Empty!")
            }
        }

        binding.btnCustomAdd.setOnClickListener{
            val customKey = binding.inputCustomKey.text.toString()
            val customValue = binding.inputCustomValue.text.toString()

            if(customKey.isNotEmpty() && customValue.isNotEmpty()){
                playerAttributes.withCustomAttribute(customKey, customValue)
                showSnackbar("Added $customKey: $customValue")
                binding.inputCustomKey.text.clear()
                binding.inputCustomValue.text.clear()
            }
            else{
                showSnackbar("Key and Value are Empty!")
            }
        }

        gameballApp = GameballApp.getInstance(applicationContext)

        gameballApp.initializeFirebase()

        binding.btnRegister.setOnClickListener {
            val apiKey = binding.inputApiKey.text.toString()
            val playerUniqueId = binding.inputPlayerId.text.toString()
            val language = binding.inputLanguage.text.toString()

            var playerAttributes = initializePlayerAttributes(binding)

            if(apiKey.isEmpty() && playerUniqueId.isEmpty())
            {
                showSnackbar("ApiKey and PlayerUniqueId must not be empty.")
            }
            else if(apiKey.isEmpty()){
                showSnackbar("ApiKey must not be empty.")
            }
            else if(playerUniqueId.isEmpty()){
                showSnackbar("PlayerUniqueId must not be empty.")
            }
            else if(apiKey.isNotEmpty() && playerUniqueId.isNotEmpty())
            {
                initializePlayer(apiKey, playerUniqueId, playerAttributes, language, baseUrl)
                playerAttributes = PlayerAttributes.Builder().build()
            }
        }

        binding.btnWidget.setOnClickListener {
            val apiKey = binding.inputApiKey.text.toString()
            val playerUniqueId = binding.inputPlayerId.text.toString()
            val language = binding.inputLanguage.text.toString()

            var playerAttributes = initializePlayerAttributes(binding)

            if(apiKey.isEmpty() && playerUniqueId.isEmpty())
            {
                showSnackbar("ApiKey and PlayerUniqueId must not be empty.")
            }
            else if(apiKey.isEmpty()){
                showSnackbar("ApiKey must not be empty.")
            }
            else if(playerUniqueId.isEmpty()){
                showSnackbar("PlayerUniqueId must not be empty.")
            }
            else if(apiKey.isNotEmpty() && playerUniqueId.isNotEmpty())
            {
                initializePlayer(apiKey, playerUniqueId, playerAttributes, language, baseUrl)
                gameballApp.showProfile(this, playerUniqueId, "", false, widgetUrl)
                playerAttributes = PlayerAttributes.Builder().build()
            }
        }
    }

    private fun showSnackbar(msg: String){
        Snackbar.make(binding.root,
            msg, Snackbar.LENGTH_LONG).show()
    }

    private fun initializePlayer(apiKey: String, playerUniqueId: String, playerAttributes: PlayerAttributes, language: String, baseUrl: String?){
        gameballApp.init(apiKey, language, "", "", baseUrl)
        gameballApp.registerPlayer(playerUniqueId, "testEmail@mail.com", "0100001000", playerAttributes,this, this.intent,
            object: Callback<PlayerRegisterResponse> {
                override fun onSuccess(t: PlayerRegisterResponse?) {
                    t?.let { resp -> showSnackbar("Player created with Id: ${resp.gameballId}") }
                }

                override fun onError(e: Throwable?) {
                    e?.message?.let { msg -> showSnackbar(msg)}
                }
            })
    }

    private fun initializePlayerAttributes(binding: ActivityMainBinding): PlayerAttributes {
        val firstName = binding.inputFirstName.text.toString()
        val lastName = binding.inputLastName.text.toString()
        val email = binding.inputEmail.text.toString()
        val mobile = binding.inputMobile.text.toString()
        val displayName = binding.inputDisplayName.text.toString()
        val birthDate = binding.inputBirthDate.text.toString()
        val joinDate = binding.inputJoinDate.text.toString()
        val preferredLanguage = binding.inputPreferredLanguage.text.toString()

        //val playerAttributes = PlayerAttributes.Builder()

        if(firstName.isNotEmpty()){
            playerAttributes.withFirstName(firstName)
        }
        if(lastName.isNotEmpty()){
            playerAttributes.withLastName(lastName)
        }
        if(email.isNotEmpty()){
            playerAttributes.withEmail(email)
        }
        if(mobile.isNotEmpty()){
            playerAttributes.withMobileNumber(mobile)
        }
        if(displayName.isNotEmpty()){
            playerAttributes.withDisplayName(displayName)
        }
        if(birthDate.isNotEmpty()){
            playerAttributes.withDateOfBirth(birthDate)
        }
        if(joinDate.isNotEmpty()){
            playerAttributes.withJoinDate(joinDate)
        }
        if(preferredLanguage.isNotEmpty()){
            playerAttributes.withPreferredLanguage(preferredLanguage)
        }

        return playerAttributes.build()
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
        super.onDestroy()
    }
}
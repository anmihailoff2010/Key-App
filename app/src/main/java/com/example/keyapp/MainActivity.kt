package com.example.keyapp

import android.app.AlertDialog
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.keyapp.viewmodel.MainViewModel
import com.example.keyapp.viewmodel.MainViewModelFactory

class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private lateinit var intentFiltersArray: Array<IntentFilter>
    private lateinit var techListsArray: Array<Array<String>>
    private lateinit var mainViewModel: MainViewModel
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainViewModel = ViewModelProvider(this, MainViewModelFactory(this)).get(MainViewModel::class.java)
        setupNFC()

        navController = findNavController(R.id.nav_host_fragment)

        if (savedInstanceState == null) {
            navController.setGraph(R.navigation.nav_graph)
        }

        setupBluetooth()
    }

    private fun setupNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC не поддерживается на этом устройстве", Toast.LENGTH_LONG).show()
            return
        }

        if (!nfcAdapter!!.isEnabled) {
            showNfcSettingsDialog()
        }

        pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                addDataType("*/*")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("fail", e)
            }
        }

        val tag = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        val tech = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)

        intentFiltersArray = arrayOf(ndef, tag, tech)
        techListsArray = arrayOf(arrayOf(NfcA::class.java.name, Ndef::class.java.name))
    }

    private fun showNfcSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Включите NFC")
            .setMessage("NFC выключен. Пожалуйста, включите его в настройках устройства.")
            .setPositiveButton("Настройки") { _, _ ->
                val intent = Intent(Settings.ACTION_NFC_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun setupBluetooth() {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Log.e("MainActivity", "Bluetooth не поддерживается на этом устройстве")
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }

    private fun promptUserToSetDefault() {
        val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray)
        Log.d("MainActivity", "Foreground dispatch enabled")
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
        Log.d("MainActivity", "Foreground dispatch disabled")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            Log.d("MainActivity", "NFC Tag Discovered: ${intent.extras}")
            handleNfcIntent(intent)
        }
    }

    private fun handleNfcIntent(intent: Intent) {
        val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        if (tag != null) {
            mainViewModel.processTag(tag)
            Log.d("MainActivity", "Tag processed: ${tag.id.joinToString(",")}")
        } else {
            Log.e("MainActivity", "No NFC tag found in intent")
        }
    }

    companion object {
        private const val REQUEST_ENABLE_BT = 1
    }
}


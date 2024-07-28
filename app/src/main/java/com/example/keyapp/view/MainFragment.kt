package com.example.keyapp.view

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.keyapp.KeysAdapter
import com.example.keyapp.R
import com.example.keyapp.databinding.FragmentMainBinding
import com.example.keyapp.toMacAddress
import com.example.keyapp.viewmodel.MainViewModel
import com.example.keyapp.viewmodel.MainViewModelFactory

class MainFragment : Fragment(), NfcAdapter.ReaderCallback {

    private lateinit var viewModel: MainViewModel
    private var readKey: ByteArray? = null
    private lateinit var keysAdapter: KeysAdapter
    private lateinit var nfcAdapter: NfcAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentMainBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_main, container, false
        )
        viewModel = ViewModelProvider(this, MainViewModelFactory(requireContext())).get(MainViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        keysAdapter = KeysAdapter(
            { key ->
                readKey = key
                binding.keyTextView.text = "Выбранный ключ: ${key.toMacAddress()}"
            },
            { key ->
                viewModel.deleteKey(key)
                Toast.makeText(context, "Ключ удален", Toast.LENGTH_SHORT).show()
            }
        )
        binding.keysRecyclerView.adapter = keysAdapter
        binding.keysRecyclerView.layoutManager = LinearLayoutManager(context)

        binding.readKeyButton.setOnClickListener {
            Toast.makeText(context, "Поднесите метку NFC, чтобы прочитать ключ", Toast.LENGTH_SHORT).show()
        }

        binding.openLockButton.setOnClickListener {
            if (readKey != null) {
                Log.d("MainFragment", "Attempting to open lock with key: ${readKey!!.toMacAddress()}")
                if (viewModel.openLock(readKey!!)) {
                    Toast.makeText(context, "Замок открыт", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Не удалось открыть замок", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Ключ не найден", Toast.LENGTH_SHORT).show()
            }
        }

        binding.writeKeyButton.setOnClickListener {
            if (readKey != null) {
                if (viewModel.writeKeyToExternalReader(readKey!!)) {
                    Toast.makeText(context, "Ключ записан во внешний считыватель", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Не удалось записать ключ", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Ключ не найден", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.readKeyLiveData.observe(viewLifecycleOwner, { key ->
            readKey = key
            if (key != null) {
                binding.keyTextView.text = "Считанный ключ: ${key.toMacAddress()}"
                Toast.makeText(context, "Считанный ключ: ${key.toMacAddress()}", Toast.LENGTH_SHORT).show()
                // Не открываем замок автоматически
            } else {
                binding.keyTextView.text = "Не удалось считать ключ"
                Toast.makeText(context, "Не удалось считать ключ", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.getAllKeys().observe(viewLifecycleOwner, { keys ->
            keysAdapter.setKeys(keys)
        })

        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
        if (nfcAdapter == null) {
            Toast.makeText(context, "NFC не поддерживается на этом устройстве", Toast.LENGTH_LONG).show()
        } else {
            Log.d("MainFragment", "NfcAdapter initialized successfully")
        }


        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainFragment", "onResume called")
        enableNfcReaderMode()
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainFragment", "onPause called")
        disableNfcReaderMode()
    }


    private fun enableNfcReaderMode() {
        if (nfcAdapter != null && nfcAdapter.isEnabled) {
            val options = Bundle()
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 1000)
            nfcAdapter.enableReaderMode(activity, this, NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, options)
            Log.d("MainFragment", "NFC Reader Mode enabled")
        } else {
            Log.d("MainFragment", "NFC Adapter is not enabled")
        }
    }

    private fun disableNfcReaderMode() {
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(activity)
            Log.d("MainFragment", "NFC Reader Mode disabled")
        }
    }


    override fun onTagDiscovered(tag: Tag) {
        Log.d("MainFragment", "onTagDiscovered called with tag: ${tag.id.joinToString(",")}")
        activity?.runOnUiThread {
            viewModel.processTag(tag)
        }
    }

}


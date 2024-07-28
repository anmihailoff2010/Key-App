package com.example.keyapp

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import java.util.Arrays


class MyHostApduService : HostApduService() {
    private val cardData = ByteArray(1024) // Размер для MIFARE Classic 1K карты

    override fun onCreate() {
        super.onCreate()
        initVirtualCard()
    }

    override fun processCommandApdu(apdu: ByteArray, extras: Bundle): ByteArray {
        if (apdu == null) {
            return UNKNOWN_CMD_SW
        }

        if (isSelectAidApdu(apdu)) {
            return SELECT_OK_SW
        }

        return handleApduCommand(apdu)
    }

    override fun onDeactivated(reason: Int) {
        // Обработка деактивации
    }

    private fun isSelectAidApdu(apdu: ByteArray): Boolean {
        if (apdu.size < SELECT_AID_APDU.size) {
            return false
        }
        return SELECT_AID_APDU.contentEquals(apdu.copyOf(SELECT_AID_APDU.size))
    }

    private fun handleApduCommand(apdu: ByteArray): ByteArray {
        if (apdu[0] == 0x00.toByte() && apdu[1] == 0xB0.toByte()) {
            val offset = ((apdu[2].toInt() and 0xFF) shl 8) or (apdu[3].toInt() and 0xFF)
            val length = apdu[4].toInt() and 0xFF

            if (offset + length > cardData.size) {
                return UNKNOWN_CMD_SW
            }

            val response = Arrays.copyOfRange(cardData, offset, offset + length)
            return concatenateArrays(response, SELECT_OK_SW)
        }

        return UNKNOWN_CMD_SW
    }

    private fun initVirtualCard() {
        // Заполнение Sector 0
        // Блок 0: UID и производственные данные (пример UID: 0x04, 0xA5, 0xA6, 0xA7, 0xA8)
        cardData[0] = 0x04
        cardData[1] = 0xA5.toByte()
        cardData[2] = 0xA6.toByte()
        cardData[3] = 0xA7.toByte()
        cardData[4] = 0xA8.toByte()

        // Остальные данные можно оставить нулевыми или заполнить по необходимости

        // Заполнение пользовательскими данными
        // Например, блок 1, 2 в секторе 0
        for (i in 16..31) {
            cardData[i] = i.toByte() // Просто пример, заполняем по порядку
        }

        // Заполнение Sector Trailer (блок 3)
        // Пример ключей A и B: 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF (дефолтные ключи)
        val sectorTrailerIndex = 3 * 16
        Arrays.fill(cardData, sectorTrailerIndex, sectorTrailerIndex + 6, 0xFF.toByte()) // Ключ A
        cardData[sectorTrailerIndex + 6] = 0xFF.toByte() // Access Bits
        cardData[sectorTrailerIndex + 7] = 0x07.toByte() // Access Bits
        cardData[sectorTrailerIndex + 8] = 0x80.toByte() // Access Bits
        Arrays.fill(cardData, sectorTrailerIndex + 9, sectorTrailerIndex + 16, 0xFF.toByte()) // Ключ B

        // Заполнение остальных секторов (1-15) аналогичным образом
        for (sector in 1..15) {
            val baseIndex = sector * 4 * 16
            // Заполнение пользовательскими данными
            for (block in 0..2) {
                val blockIndex = baseIndex + block * 16
                for (i in 0..15) {
                    cardData[blockIndex + i] = (blockIndex + i).toByte()
                }
            }
            // Заполнение Sector Trailer
            val trailerIndex = baseIndex + 3 * 16
            Arrays.fill(cardData, trailerIndex, trailerIndex + 6, 0xFF.toByte()) // Ключ A
            cardData[trailerIndex + 6] = 0xFF.toByte() // Access Bits
            cardData[trailerIndex + 7] = 0x07.toByte() // Access Bits
            cardData[trailerIndex + 8] = 0x80.toByte() // Access Bits
            Arrays.fill(cardData, trailerIndex + 9, trailerIndex + 16, 0xFF.toByte()) // Ключ B
        }
    }

    private fun concatenateArrays(first: ByteArray, second: ByteArray): ByteArray {
        val result = ByteArray(first.size + second.size)
        System.arraycopy(first, 0, result, 0, first.size)
        System.arraycopy(second, 0, result, first.size, second.size)
        return result
    }

    companion object {
        private const val TAG = "MyHostApduService"

        private val SELECT_AID_APDU = byteArrayOf(
            0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(),
            0x07.toByte(), 0xD2.toByte(), 0x76.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x85.toByte(), 0x01.toByte(), 0x00.toByte(),
            0x00.toByte()
        )

        private val SELECT_OK_SW = byteArrayOf(
            0x90.toByte(), 0x00.toByte()
        )

        private val UNKNOWN_CMD_SW = byteArrayOf(
            0x00.toByte(), 0x00.toByte()
        )
    }
}



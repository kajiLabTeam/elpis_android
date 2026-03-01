package net.kajilab.elpissender.utils

import org.junit.Assert.assertTrue
import org.junit.Test

class BleAdvertisementParserTest {
    @Test
    fun `extracts uuid from ibeacon manufacturer data`() {
        val packet =
            byteArrayOf(
                0x02,
                0x01,
                0x06,
                0x1A,
                0xFF.toByte(),
                0x4C,
                0x00,
                0x02,
                0x15,
                0xFD.toByte(),
                0xA5.toByte(),
                0x06,
                0x93.toByte(),
                0xA4.toByte(),
                0xE2.toByte(),
                0x4F,
                0xB1.toByte(),
                0xAF.toByte(),
                0xCF.toByte(),
                0xC6.toByte(),
                0xEB.toByte(),
                0x07,
                0x64,
                0x78,
                0x25,
                0x00,
                0x64,
                0x00,
                0x01,
                0xC5.toByte(),
            )

        val identifiers = BleAdvertisementParser.parseUuids(packet)

        assertTrue(identifiers.contains("FDA50693-A4E2-4FB1-AFCF-C6EB07647825"))
    }

    @Test
    fun `extracts uuid from service uuid and service data advertisements without duplicates`() {
        val packet =
            byteArrayOf(
                0x02,
                0x01,
                0x06,
                0x03,
                0x03,
                0xAA.toByte(),
                0xFE.toByte(),
                0x0B,
                0x16,
                0xAA.toByte(),
                0xFE.toByte(),
                0x10,
                0xEE.toByte(),
                0x6F,
                0x70,
                0x65,
                0x6E,
                0x61,
                0x69,
            )

        val identifiers = BleAdvertisementParser.parseUuids(packet)

        assertTrue(identifiers.contains("0000FEAA-0000-1000-8000-00805F9B34FB"))
        assertTrue(identifiers.size == 1)
    }

    @Test
    fun `returns empty when advertisement has no uuid`() {
        val packet =
            byteArrayOf(
                0x05,
                0xFF.toByte(),
                0x4C,
                0x00,
                0x12,
                0x02,
                0x00,
            )

        val identifiers = BleAdvertisementParser.parseUuids(packet)

        assertTrue(identifiers.isEmpty())
    }
}

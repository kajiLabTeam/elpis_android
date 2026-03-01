package net.kajilab.elpissender.utils

import java.util.Locale

object BleAdvertisementParser {
    private const val TYPE_FLAGS = 0x01
    private const val TYPE_SERVICE_UUID_16_PARTIAL = 0x02
    private const val TYPE_SERVICE_UUID_16_COMPLETE = 0x03
    private const val TYPE_SERVICE_UUID_32_PARTIAL = 0x04
    private const val TYPE_SERVICE_UUID_32_COMPLETE = 0x05
    private const val TYPE_SERVICE_UUID_128_PARTIAL = 0x06
    private const val TYPE_SERVICE_UUID_128_COMPLETE = 0x07
    private const val TYPE_LOCAL_NAME_SHORT = 0x08
    private const val TYPE_LOCAL_NAME_COMPLETE = 0x09
    private const val TYPE_TX_POWER_LEVEL = 0x0A
    private const val TYPE_SLAVE_CONNECTION_INTERVAL_RANGE = 0x12
    private const val TYPE_SERVICE_SOLICITATION_UUIDS_16_BIT = 0x14
    private const val TYPE_SERVICE_SOLICITATION_UUIDS_128_BIT = 0x15
    private const val TYPE_SERVICE_DATA_16_BIT = 0x16
    private const val TYPE_PUBLIC_TARGET_ADDRESS = 0x17
    private const val TYPE_RANDOM_TARGET_ADDRESS = 0x18
    private const val TYPE_APPEARANCE = 0x19
    private const val TYPE_ADVERTISING_INTERVAL = 0x1A
    private const val TYPE_LE_BLUETOOTH_DEVICE_ADDRESS = 0x1B
    private const val TYPE_LE_ROLE = 0x1C
    private const val TYPE_SIMPLE_PAIRING_HASH_C_256 = 0x1D
    private const val TYPE_SIMPLE_PAIRING_RANDOMIZER_R_256 = 0x1E
    private const val TYPE_SERVICE_SOLICITATION_UUIDS_32_BIT = 0x1F
    private const val TYPE_SERVICE_DATA_32_BIT = 0x20
    private const val TYPE_SERVICE_DATA_128_BIT = 0x21
    private const val TYPE_LE_SECURE_CONNECTIONS_CONFIRMATION_VALUE = 0x22
    private const val TYPE_LE_SECURE_CONNECTIONS_RANDOM_VALUE = 0x23
    private const val TYPE_URI = 0x24
    private const val TYPE_INDOOR_POSITIONING = 0x25
    private const val TYPE_TRANSPORT_DISCOVERY_DATA = 0x26
    private const val TYPE_LE_SUPPORTED_FEATURES = 0x27
    private const val TYPE_CHANNEL_MAP_UPDATE_INDICATION = 0x28
    private const val TYPE_PB_ADV = 0x29
    private const val TYPE_MESH_MESSAGE = 0x2A
    private const val TYPE_MESH_BEACON = 0x2B
    private const val TYPE_BIG_INFO = 0x2C
    private const val TYPE_BROADCAST_CODE = 0x2D
    private const val TYPE_RESOLVABLE_SET_IDENTIFIER = 0x2E
    private const val TYPE_ADVERTISING_INTERVAL_LONG = 0x2F
    private const val TYPE_BROADCAST_NAME = 0x30
    private const val TYPE_ENCRYPTED_ADVERTISING_DATA = 0x31
    private const val TYPE_PERIODIC_ADVERTISING_RESPONSE_TIMING_INFORMATION = 0x32
    private const val TYPE_ELECTRONIC_SHELF_LABEL = 0x34
    private const val TYPE_3D_INFORMATION_DATA = 0x3D
    private const val TYPE_MANUFACTURER_SPECIFIC_DATA = 0xFF
    private const val BLUETOOTH_BASE_UUID_SUFFIX = "-0000-1000-8000-00805F9B34FB"
    private const val IBEACON_COMPANY_ID = 0x004C

    fun parseUuids(bytes: ByteArray): List<String> {
        if (bytes.isEmpty()) {
            return emptyList()
        }

        val uuids = linkedSetOf<String>()
        for (structure in extractStructures(bytes)) {
            uuids.addAll(parseStructureUuids(structure.type, structure.data))
        }
        return uuids.toList()
    }

    fun parseIdentifiers(bytes: ByteArray): List<String> {
        if (bytes.isEmpty()) {
            return emptyList()
        }

        val identifiers = linkedSetOf<String>()
        for (structure in extractStructures(bytes)) {
            identifiers.addAll(parseStructure(structure.type, structure.data))
        }
        return identifiers.toList()
    }

    private fun parseStructureUuids(
        type: Int,
        data: ByteArray,
    ): List<String> =
        when (type) {
            TYPE_SERVICE_UUID_16_PARTIAL,
            TYPE_SERVICE_UUID_16_COMPLETE,
            -> parsePlainUuidList(data, 2)
            TYPE_SERVICE_UUID_32_PARTIAL,
            TYPE_SERVICE_UUID_32_COMPLETE,
            TYPE_SERVICE_SOLICITATION_UUIDS_32_BIT,
            -> parsePlainUuidList(data, 4)
            TYPE_SERVICE_UUID_128_PARTIAL,
            TYPE_SERVICE_UUID_128_COMPLETE,
            TYPE_SERVICE_SOLICITATION_UUIDS_128_BIT,
            -> parsePlainUuidList(data, 16)
            TYPE_SERVICE_SOLICITATION_UUIDS_16_BIT,
            TYPE_SERVICE_DATA_16_BIT,
            -> parseServiceDataUuid(data, 2)
            TYPE_SERVICE_DATA_32_BIT -> parseServiceDataUuid(data, 4)
            TYPE_SERVICE_DATA_128_BIT -> parseServiceDataUuid(data, 16)
            TYPE_MANUFACTURER_SPECIFIC_DATA -> parseManufacturerUuid(data)
            else -> emptyList()
        }

    private fun extractStructures(bytes: ByteArray): List<AdvertisementStructure> {
        val structures = mutableListOf<AdvertisementStructure>()
        var index = 0

        while (index < bytes.size) {
            val length = bytes[index].toInt() and 0xFF
            if (length == 0) {
                break
            }

            val typeIndex = index + 1
            val dataStart = index + 2
            val dataEnd = (index + length + 1).coerceAtMost(bytes.size)

            if (typeIndex >= bytes.size || dataStart > dataEnd) {
                break
            }

            val type = bytes[typeIndex].toInt() and 0xFF
            structures.add(
                AdvertisementStructure(
                    type = type,
                    data = bytes.copyOfRange(dataStart, dataEnd),
                ),
            )

            index += length + 1
        }

        return structures
    }

    private fun parseStructure(
        type: Int,
        data: ByteArray,
    ): List<String> =
        when (type) {
            TYPE_FLAGS -> listOf(rawIdentifier("flags", data))
            TYPE_SERVICE_UUID_16_PARTIAL -> parseUuidList(data, 2, "service_uuid_16_partial")
            TYPE_SERVICE_UUID_16_COMPLETE -> parseUuidList(data, 2, "service_uuid_16_complete")
            TYPE_SERVICE_UUID_32_PARTIAL -> parseUuidList(data, 4, "service_uuid_32_partial")
            TYPE_SERVICE_UUID_32_COMPLETE -> parseUuidList(data, 4, "service_uuid_32_complete")
            TYPE_SERVICE_UUID_128_PARTIAL -> parseUuidList(data, 16, "service_uuid_128_partial")
            TYPE_SERVICE_UUID_128_COMPLETE -> parseUuidList(data, 16, "service_uuid_128_complete")
            TYPE_LOCAL_NAME_SHORT -> listOf(textIdentifier("local_name_short", data))
            TYPE_LOCAL_NAME_COMPLETE -> listOf(textIdentifier("local_name_complete", data))
            TYPE_TX_POWER_LEVEL -> listOf("tx_power:${signedValue(data)}")
            TYPE_SLAVE_CONNECTION_INTERVAL_RANGE -> listOf(rawIdentifier("slave_connection_interval_range", data))
            TYPE_SERVICE_SOLICITATION_UUIDS_16_BIT -> parseUuidList(data, 2, "service_solicitation_uuid_16")
            TYPE_SERVICE_SOLICITATION_UUIDS_32_BIT -> parseUuidList(data, 4, "service_solicitation_uuid_32")
            TYPE_SERVICE_SOLICITATION_UUIDS_128_BIT -> parseUuidList(data, 16, "service_solicitation_uuid_128")
            TYPE_SERVICE_DATA_16_BIT -> parseServiceData(data, 2, "service_data_16")
            TYPE_SERVICE_DATA_32_BIT -> parseServiceData(data, 4, "service_data_32")
            TYPE_SERVICE_DATA_128_BIT -> parseServiceData(data, 16, "service_data_128")
            TYPE_MANUFACTURER_SPECIFIC_DATA -> parseManufacturerData(data)
            TYPE_PUBLIC_TARGET_ADDRESS -> listOf(rawIdentifier("public_target_address", data))
            TYPE_RANDOM_TARGET_ADDRESS -> listOf(rawIdentifier("random_target_address", data))
            TYPE_APPEARANCE -> listOf(rawIdentifier("appearance", data))
            TYPE_ADVERTISING_INTERVAL,
            TYPE_ADVERTISING_INTERVAL_LONG,
            -> listOf(rawIdentifier("advertising_interval", data))
            TYPE_LE_BLUETOOTH_DEVICE_ADDRESS -> listOf(rawIdentifier("le_bluetooth_device_address", data))
            TYPE_LE_ROLE -> listOf(rawIdentifier("le_role", data))
            TYPE_SIMPLE_PAIRING_HASH_C_256 -> listOf(rawIdentifier("simple_pairing_hash_c_256", data))
            TYPE_SIMPLE_PAIRING_RANDOMIZER_R_256 -> listOf(rawIdentifier("simple_pairing_randomizer_r_256", data))
            TYPE_LE_SECURE_CONNECTIONS_CONFIRMATION_VALUE ->
                listOf(rawIdentifier("le_secure_connections_confirmation_value", data))
            TYPE_LE_SECURE_CONNECTIONS_RANDOM_VALUE ->
                listOf(rawIdentifier("le_secure_connections_random_value", data))
            TYPE_URI -> listOf(rawIdentifier("uri", data))
            TYPE_INDOOR_POSITIONING -> listOf(rawIdentifier("indoor_positioning", data))
            TYPE_TRANSPORT_DISCOVERY_DATA -> listOf(rawIdentifier("transport_discovery_data", data))
            TYPE_LE_SUPPORTED_FEATURES -> listOf(rawIdentifier("le_supported_features", data))
            TYPE_CHANNEL_MAP_UPDATE_INDICATION -> listOf(rawIdentifier("channel_map_update_indication", data))
            TYPE_PB_ADV -> listOf(rawIdentifier("pb_adv", data))
            TYPE_MESH_MESSAGE -> listOf(rawIdentifier("mesh_message", data))
            TYPE_MESH_BEACON -> listOf(rawIdentifier("mesh_beacon", data))
            TYPE_BIG_INFO -> listOf(rawIdentifier("big_info", data))
            TYPE_BROADCAST_CODE -> listOf(rawIdentifier("broadcast_code", data))
            TYPE_RESOLVABLE_SET_IDENTIFIER -> listOf(rawIdentifier("resolvable_set_identifier", data))
            TYPE_BROADCAST_NAME -> listOf(textIdentifier("broadcast_name", data))
            TYPE_ENCRYPTED_ADVERTISING_DATA -> listOf(rawIdentifier("encrypted_advertising_data", data))
            TYPE_PERIODIC_ADVERTISING_RESPONSE_TIMING_INFORMATION ->
                listOf(rawIdentifier("periodic_advertising_response_timing_information", data))
            TYPE_ELECTRONIC_SHELF_LABEL -> listOf(rawIdentifier("electronic_shelf_label", data))
            TYPE_3D_INFORMATION_DATA -> listOf(rawIdentifier("3d_information_data", data))
            else -> listOf("ad_type_${formatHex(type, 2)}:${toHex(data)}")
        }

    private fun parseManufacturerData(data: ByteArray): List<String> {
        if (data.size < 2) {
            return listOf("manufacturer:raw:${toHex(data)}")
        }

        val companyId = uint16LittleEndian(data, 0)
        val payload = data.copyOfRange(2, data.size)
        val identifiers =
            mutableListOf(
                "manufacturer:${formatHex(companyId, 4)}:${toHex(payload)}",
            )

        parseIBeacon(data)?.let { iBeacon ->
            identifiers.add(iBeacon.uuid)
            identifiers.add("ibeacon:${iBeacon.uuid}:${iBeacon.major}:${iBeacon.minor}")
        }

        return identifiers
    }

    private fun parseManufacturerUuid(data: ByteArray): List<String> =
        parseIBeacon(data)?.let { listOf(it.uuid) } ?: emptyList()

    private fun parseServiceData(
        data: ByteArray,
        uuidSize: Int,
        label: String,
    ): List<String> {
        if (data.size < uuidSize) {
            return listOf("$label:raw:${toHex(data)}")
        }

        val uuid =
            when (uuidSize) {
                2 -> expand16BitUuid(uint16LittleEndian(data, 0))
                4 -> expand32BitUuid(uint32LittleEndian(data, 0))
                16 -> uuid128FromLittleEndian(data.copyOfRange(0, 16))
                else -> return listOf("$label:raw:${toHex(data)}")
            }
        val payload = data.copyOfRange(uuidSize, data.size)
        return if (payload.isEmpty()) {
            listOf("$label:$uuid")
        } else {
            listOf("$label:$uuid:${toHex(payload)}")
        }
    }

    private fun parseServiceDataUuid(
        data: ByteArray,
        uuidSize: Int,
    ): List<String> {
        if (data.size < uuidSize) {
            return emptyList()
        }

        val uuid =
            when (uuidSize) {
                2 -> expand16BitUuid(uint16LittleEndian(data, 0))
                4 -> expand32BitUuid(uint32LittleEndian(data, 0))
                16 -> uuid128FromLittleEndian(data.copyOfRange(0, 16))
                else -> return emptyList()
            }
        return listOf(uuid)
    }

    private fun parseUuidList(
        data: ByteArray,
        uuidSize: Int,
        label: String,
    ): List<String> {
        if (uuidSize !in setOf(2, 4, 16)) {
            return listOf("$label:raw:${toHex(data)}")
        }
        if (data.isEmpty()) {
            return emptyList()
        }
        if (data.size % uuidSize != 0) {
            return listOf("$label:raw:${toHex(data)}")
        }

        val identifiers = mutableListOf<String>()
        var index = 0
        while (index < data.size) {
            val uuid =
                when (uuidSize) {
                    2 -> expand16BitUuid(uint16LittleEndian(data, index))
                    4 -> expand32BitUuid(uint32LittleEndian(data, index))
                    16 -> uuid128FromLittleEndian(data.copyOfRange(index, index + 16))
                    else -> error("Unsupported UUID size: $uuidSize")
                }
            identifiers.add("$label:$uuid")
            index += uuidSize
        }
        return identifiers
    }

    private fun parsePlainUuidList(
        data: ByteArray,
        uuidSize: Int,
    ): List<String> {
        if (uuidSize !in setOf(2, 4, 16)) {
            return emptyList()
        }
        if (data.isEmpty() || data.size % uuidSize != 0) {
            return emptyList()
        }

        val uuids = mutableListOf<String>()
        var index = 0
        while (index < data.size) {
            val uuid =
                when (uuidSize) {
                    2 -> expand16BitUuid(uint16LittleEndian(data, index))
                    4 -> expand32BitUuid(uint32LittleEndian(data, index))
                    16 -> uuid128FromLittleEndian(data.copyOfRange(index, index + 16))
                    else -> error("Unsupported UUID size: $uuidSize")
                }
            uuids.add(uuid)
            index += uuidSize
        }
        return uuids
    }

    private fun parseIBeacon(data: ByteArray): IBeacon? {
        if (data.size < 25) {
            return null
        }
        if (uint16LittleEndian(data, 0) != IBEACON_COMPANY_ID) {
            return null
        }
        if (data[2] != 0x02.toByte() || data[3] != 0x15.toByte()) {
            return null
        }

        return IBeacon(
            uuid = uuid128FromBigEndian(data.copyOfRange(4, 20)),
            major = uint16BigEndian(data, 20),
            minor = uint16BigEndian(data, 22),
        )
    }

    private fun rawIdentifier(
        label: String,
        data: ByteArray,
    ): String = "$label:${toHex(data)}"

    private fun textIdentifier(
        label: String,
        data: ByteArray,
    ): String {
        val decoded = data.toString(Charsets.UTF_8)
        val sanitized =
            decoded
                .replace(",", "_")
                .replace("\r", " ")
                .replace("\n", " ")
                .trim()
        return if (sanitized.isEmpty()) {
            rawIdentifier(label, data)
        } else {
            "$label:$sanitized"
        }
    }

    private fun signedValue(data: ByteArray): Int {
        if (data.isEmpty()) {
            return 0
        }
        return data[0].toInt()
    }

    private fun expand16BitUuid(value: Int): String = "${formatHex(value, 8)}$BLUETOOTH_BASE_UUID_SUFFIX"

    private fun expand32BitUuid(value: Long): String = "${formatHex(value, 8)}$BLUETOOTH_BASE_UUID_SUFFIX"

    private fun uuid128FromLittleEndian(bytes: ByteArray): String {
        val reversed = bytes.reversedArray()
        return uuid128FromBigEndian(reversed)
    }

    private fun uuid128FromBigEndian(bytes: ByteArray): String {
        val hex = toHex(bytes)
        return buildString(36) {
            append(hex.substring(0, 8))
            append('-')
            append(hex.substring(8, 12))
            append('-')
            append(hex.substring(12, 16))
            append('-')
            append(hex.substring(16, 20))
            append('-')
            append(hex.substring(20, 32))
        }
    }

    private fun uint16LittleEndian(
        bytes: ByteArray,
        startIndex: Int,
    ): Int {
        val b0 = bytes[startIndex].toInt() and 0xFF
        val b1 = bytes[startIndex + 1].toInt() and 0xFF
        return b0 or (b1 shl 8)
    }

    private fun uint16BigEndian(
        bytes: ByteArray,
        startIndex: Int,
    ): Int {
        val b0 = bytes[startIndex].toInt() and 0xFF
        val b1 = bytes[startIndex + 1].toInt() and 0xFF
        return (b0 shl 8) or b1
    }

    private fun uint32LittleEndian(
        bytes: ByteArray,
        startIndex: Int,
    ): Long {
        val b0 = bytes[startIndex].toLong() and 0xFF
        val b1 = bytes[startIndex + 1].toLong() and 0xFF
        val b2 = bytes[startIndex + 2].toLong() and 0xFF
        val b3 = bytes[startIndex + 3].toLong() and 0xFF
        return b0 or (b1 shl 8) or (b2 shl 16) or (b3 shl 24)
    }

    private fun formatHex(
        value: Number,
        width: Int,
    ): String = String.format(Locale.US, "%0${width}X", value.toLong())

    private fun toHex(bytes: ByteArray): String =
        bytes.joinToString(separator = "") { byte ->
            String.format(Locale.US, "%02X", byte.toInt() and 0xFF)
        }

    private data class AdvertisementStructure(
        val type: Int,
        val data: ByteArray,
    )

    private data class IBeacon(
        val uuid: String,
        val major: Int,
        val minor: Int,
    )
}

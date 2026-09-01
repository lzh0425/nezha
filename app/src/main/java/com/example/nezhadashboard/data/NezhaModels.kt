package com.example.nezhadashboard.data

import com.google.gson.annotations.SerializedName

data class NezhaResponse(
    val code: Int,
    val message: String?,
    val data: List<ServerItem>?
)

data class ServerItem(
    val id: Long,
    val name: String,
    val host: HostInfo?,
    val state: ServerState?,
    @SerializedName("last_active") val lastActive: String?
)

data class HostInfo(
    val platform: String?,
    val cpu: List<String>?,
    val memTotal: Long,
    val diskTotal: Long
)

data class ServerState(
    val cpu: Double,
    @SerializedName("mem_used") val memUsed: Long,
    @SerializedName("swap_used") val swapUsed: Long,
    @SerializedName("disk_used") val diskUsed: Long,
    @SerializedName("net_in_transfer") val netInTransfer: Long,
    @SerializedName("net_out_transfer") val netOutTransfer: Long,
    val uptime: Long
)

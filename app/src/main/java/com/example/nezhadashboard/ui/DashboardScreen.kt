package com.example.nezhadashboard.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nezhadashboard.data.NezhaApiService
import com.example.nezhadashboard.data.ServerItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

sealed interface UiState {
    object Idle : UiState
    object Loading : UiState
    data class Success(val servers: List<ServerItem>) : UiState
    data class Error(val message: String) : UiState
}

class NezhaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()
    private var job: Job? = null

    private val api: NezhaApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://placeholder.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NezhaApiService::class.java)
    }

    fun startPolling(url: String) {
        job?.cancel()
        job = viewModelScope.launch(Dispatchers.IO) {
            val reqUrl = url.trimEnd('/') + "/api/v1/server/details"
            while (isActive) {
                try {
                    val res = api.getServerDetails(reqUrl)
                    if (res.data != null) {
                        _uiState.value = UiState.Success(res.data)
                    } else {
                        _uiState.value = UiState.Error(res.message ?: "拉取失败")
                    }
                } catch (e: Exception) {
                    _uiState.value = UiState.Error(e.message ?: "网络错误")
                }
                delay(3000)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: NezhaViewModel) {
    val state by viewModel.uiState.collectAsState()
    var urlInput by remember { mutableStateOf("https://") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("哪吒监控面板") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { p ->
        Column(modifier = Modifier.padding(p).fillMaxSize().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("面板地址") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { viewModel.startPolling(urlInput) }) {
                    Text("连接")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val s = state) {
                is UiState.Idle -> Text("请输入面板地址并点击连接")
                is UiState.Loading -> CircularProgressIndicator()
                is UiState.Error -> Text("错误: ${s.message}", color = MaterialTheme.colorScheme.error)
                is UiState.Success -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(s.servers, key = { it.id }) { srv ->
                            val cpu = srv.state?.cpu ?: 0.0
                            val memUsed = (srv.state?.memUsed ?: 0L) / (1024 * 1024)
                            val memTotal = (srv.host?.memTotal ?: 1L) / (1024 * 1024)
                            val memRatio = if (memTotal > 0) (memUsed.toFloat() / memTotal.toFloat()).coerceIn(0f, 1f) else 0f
                            val cpuRatio = (cpu / 100.0).toFloat().coerceIn(0f, 1f)

                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(srv.name, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            if (srv.state != null) "在线" else "离线",
                                            color = if (srv.state != null) Color(0xFF2E7D32) else Color.Gray
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("CPU: ${String.format("%.1f", cpu)}%")
                                    LinearProgressIndicator(
                                        progress = { cpuRatio },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("内存: ${memUsed}MB / ${memTotal}MB")
                                    LinearProgressIndicator(
                                        progress = { memRatio },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

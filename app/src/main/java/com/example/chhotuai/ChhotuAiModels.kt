package com.example.chhotuai

enum class CheckStatus(val label: String) {
    CONNECTED("Connected"),
    DEGRADED("Needs Attention"),
    NOT_CONNECTED("Not Connected"),
    ERROR("Error")
}

data class AppCheckItem(
    val moduleName: String,
    val category: String,
    val status: CheckStatus,
    val detail: String,
    val cause: String = "",
    val solution: String = ""
)

data class BackendConnectionState(
    val isConfigured: Boolean,
    val isOnline: Boolean,
    val statusLabel: String,
    val httpCode: Int? = null,
    val latencyMs: Long? = null,
    val endpointMasked: String = "Not Configured",
    val lastCheckedAt: Long = System.currentTimeMillis(),
    val errorDetail: String = "",
    val openAiConfigured: Boolean = false,
    val openAiStatusLabel: String = "Awaiting OPENAI_API_KEY in Secrets"
)

data class ChhotuReplyResult(
    val text: String,
    val sourceMode: String
)

data class SystemHealthSnapshot(
    val checkedAt: Long,
    val items: List<AppCheckItem>,
    val backendState: BackendConnectionState,
    val connectedCount: Int,
    val degradedCount: Int,
    val notConnectedCount: Int,
    val errorCount: Int
) {
    val totalCount: Int get() = items.size
}

enum class ChhotuPendingActionType {
    TOGGLE_CHHOTU_AI,
    BAN_OR_RESTORE_USER,
    TRIGGER_ADMIN_PAYOUT_REVIEW
}

data class ChhotuPendingAdminAction(
    val type: ChhotuPendingActionType,
    val title: String,
    val description: String,
    val targetId: String = "",
    val boolValue: Boolean = false
)

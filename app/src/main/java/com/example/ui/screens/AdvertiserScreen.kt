package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AdCampaignEntity
import com.example.data.model.AdPlacement
import com.example.data.model.BharatTubeCatalog
import com.example.data.model.PlatformSettingsEntity
import com.example.ui.components.IntegrationDisclosureBanner
import com.example.ui.theme.SaffronFlame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvertiserScreen(
    campaigns: List<AdCampaignEntity>,
    settings: PlatformSettingsEntity,
    isAdmin: Boolean,
    onBack: () -> Unit,
    onCreateCampaign: (String, String, String, String, String, Double) -> Unit,
    onReviewCampaign: (AdCampaignEntity, Boolean) -> Unit
) {
    var advertiserName by remember { mutableStateOf("") }
    var campaignTitle by remember { mutableStateOf("") }
    var targetCountry by remember { mutableStateOf("IN") }
    var targetLanguage by remember { mutableStateOf("hi") }
    var placement by remember { mutableStateOf(AdPlacement.PRE_ROLL) }
    var budgetInput by remember { mutableStateOf("5000") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advertiser Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                IntegrationDisclosureBanner(
                    title = "Meta Audience Network & Ad Adapter Status",
                    message = "Meta Audience Network requires verified business onboarding, app review, and valid Placement IDs in Meta Monetization Manager. " +
                        "Adapter is currently ${if (settings.metaAudienceNetworkEnabled) "Enabled (${if (settings.metaTestMode) "Test Mode" else "Live"})" else "Unconfigured/Disabled"}. No fake impressions or ad revenue are generated."
                )
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Create Ad Campaign", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = advertiserName,
                            onValueChange = { advertiserName = it },
                            label = { Text("Advertiser / Brand Name *") },
                            modifier = Modifier.fillMaxWidth().testTag("adv_name_input")
                        )
                        OutlinedTextField(
                            value = campaignTitle,
                            onValueChange = { campaignTitle = it },
                            label = { Text("Campaign Title *") },
                            modifier = Modifier.fillMaxWidth().testTag("adv_title_input")
                        )
                        OutlinedTextField(
                            value = budgetInput,
                            onValueChange = { budgetInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Budget (INR ₹, min 500)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Placement", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(AdPlacement.entries) { p ->
                                FilterChip(
                                    selected = placement == p,
                                    onClick = { placement = p },
                                    label = { Text(p.label) }
                                )
                            }
                        }
                        Text("Target Country", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(BharatTubeCatalog.countries.take(6)) { c ->
                                FilterChip(
                                    selected = targetCountry == c.code,
                                    onClick = { targetCountry = c.code },
                                    label = { Text(c.name) }
                                )
                            }
                        }
                        Text("Target Language", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(BharatTubeCatalog.initialLanguages.take(8)) { l ->
                                FilterChip(
                                    selected = targetLanguage == l.code,
                                    onClick = { targetLanguage = l.code },
                                    label = { Text(l.englishName) }
                                )
                            }
                        }
                        Button(
                            onClick = {
                                val budget = budgetInput.toDoubleOrNull() ?: 0.0
                                onCreateCampaign(advertiserName, campaignTitle, targetCountry, targetLanguage, placement.name, budget)
                                campaignTitle = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronFlame),
                            modifier = Modifier.fillMaxWidth().testTag("submit_campaign_button")
                        ) {
                            Text("Submit Campaign for Admin Review")
                        }
                    }
                }
            }

            item {
                Text("Campaigns (${campaigns.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(campaigns, key = { it.id }) { cmp ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(cmp.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("${cmp.advertiserName} • ${cmp.placement} • ${cmp.targetCountry}/${cmp.targetLanguage.uppercase()}", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Budget: ₹${String.format("%.0f", cmp.budgetInr)} • Valid Impressions: ${cmp.validImpressions} • Clicks: ${cmp.clicks}",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text("Status: ${cmp.approvalStatus}", style = MaterialTheme.typography.labelSmall, color = SaffronFlame)
                        if (isAdmin && cmp.approvalStatus == "PENDING_ADMIN_REVIEW") {
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { onReviewCampaign(cmp, true) }) { Text("Approve (Test)") }
                                OutlinedButton(onClick = { onReviewCampaign(cmp, false) }) { Text("Reject") }
                            }
                        }
                    }
                }
            }
        }
    }
}

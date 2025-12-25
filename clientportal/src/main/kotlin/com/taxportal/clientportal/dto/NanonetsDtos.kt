package com.taxportal.clientportal.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class NanonetsSubmitResponse(
    val message: String?,
    val result: List<NanonetsResult>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NanonetsResult(
    @JsonProperty("request_file_id")
    val requestFileId: String?,
    val input: String?,
    val message: String?,
    val status: String?,
    val page: Int?,
    val prediction: List<NanonetsPrediction>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NanonetsPrediction(
    val id: String?,
    val label: String?,
    @JsonProperty("xmin")
    val xMin: Int?,
    @JsonProperty("ymin")
    val yMin: Int?,
    @JsonProperty("xmax")
    val xMax: Int?,
    @JsonProperty("ymax")
    val yMax: Int?,
    val score: Double?,
    @JsonProperty("ocr_text")
    val ocrText: String?,
    val type: String?,
    val cells: List<NanonetsCell>?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NanonetsCell(
    val id: String?,
    val row: Int?,
    val col: Int?,
    @JsonProperty("row_span")
    val rowSpan: Int?,
    @JsonProperty("col_span")
    val colSpan: Int?,
    val label: String?,
    @JsonProperty("xmin")
    val xMin: Int?,
    @JsonProperty("ymin")
    val yMin: Int?,
    @JsonProperty("xmax")
    val xMax: Int?,
    @JsonProperty("ymax")
    val yMax: Int?,
    val score: Double?,
    val text: String?,
    @JsonProperty("verification_status")
    val verificationStatus: String?,
    val status: String?,
    @JsonProperty("failed_validation")
    val failedValidation: String?,
    @JsonProperty("label_id")
    val labelId: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NanonetsWebhookPayload(
    val message: String?,
    val result: List<NanonetsResult>?,
    @JsonProperty("signed_urls")
    val signedUrls: Map<String, String>?,
    @JsonProperty("request_file_id")
    val requestFileId: String?,
    @JsonProperty("request_metadata")
    val requestMetadata: String?
)

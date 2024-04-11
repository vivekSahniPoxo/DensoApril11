package com.example.denso.dispatch.model

data class DispatchSubmitItem(
    val silNo: String,
    val customer: String,
    val partNo: String,
    val pkgGroupName: String,
    val createdby: String,
    val rfidNumber: MutableList<DispatchRFIDList>,
    var found: Int = 0,
    var notFound: Int = 0
)

data class DispatchRFIDList(
    val rfidNumber: String,
    val status: String
)
package com.example.denso.dispatch.model

class RFIDTagAnStatusModel : ArrayList<RFIDTagAnStatusModel.RFIDTagAnStatusModelItem>(){
    data class RFIDTagAnStatusModelItem(val groupId: Int, val rfidTags: List<RfidTag>)


}
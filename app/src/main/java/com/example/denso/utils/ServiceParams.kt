package com.example.denso

import com.densowave.scannersdk.Common.CommScanner
import java.io.Serializable


object ServiceParam : Serializable {
    var commScanner: CommScanner? = null
}
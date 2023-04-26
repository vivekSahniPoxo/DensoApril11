//package com.example.denso.bin_search
//
//import android.content.Context
//import com.example.denso.R
//import java.util.*
//
///**
// * The class that shows UII of tag
// * The tag UII has restrictions of range and hexadecimal notation, so express it by wrapping
// */
//class TagUII {
//    /**
//     * Return as hexadecimal string
//     * @return The hexadecimal string
//     */
//
//
//    // UII value (hexadecimal number)
//    // UII extends to 256 bits ((0or1) * 256), so specify it by a character string
//    var hexString: String
//        private set
//
//
//    /**
//     * Return as a list of byte
//     * @return The byte list
//     */
//    // Byte list UII value
//    // Put in the list from the highest byte
//    // For example, if the character string is "ABC", it becomes {0xA, 0xBC}
//    var bytes: ByteArray
//        private set
//
//    /**
//     * Initialize from hexadecimal string
//     * @param hexString The hexadecimal string
//     */
//    private constructor(hexString: String) {
//        this.hexString = hexString
//
//        // Request byte list
//        bytes = hexStringToBytes(hexString)
//
//    }
//
//    /**
//     * Initialize from byte list
//     * @param bytes Byte list
//     */
//    private constructor(bytes: ByteArray) {
//        this.bytes = bytes
//
//        // Find the hexadecimal string
//        hexString = bytesToHexString(bytes)
//
//
//    }
//
//
//
//    companion object {
//        val hexCharacters = charArrayOf(
//            '0',
//            '1',
//            '2',
//            '3',
//            '4',
//            '5',
//            '6',
//            '7',
//            '8',
//            '9',
//            'A',
//            'B',
//            'C',
//            'D',
//            'E',
//            'F'
//        )
//
//        /**
//         * Return tag UII based on hexadecimal string
//         * @param hexString The hexadecimal string
//         * @return Tag UII based on the specified hexadecimal string
//         * @throws NotHexException When the specified string is not in  hexadecimal form
//         * @throws OverflowBitException When the number of bits in the specified string exceeds the defined number of bits
//         */
//        @Throws(NotHexException::class, OverflowBitException::class)
//        fun valueOf(hexString: String, context: Context?): TagUII {
//            if (!checkHexString(hexString)) {
//                throw NotHexException(context)
//            }
//            if (hexString.length > 64 /* Up to 256 bits can be input, and 64 hexadecimal digits correspond to 256 bits */) {
//                throw OverflowBitException(256, context)
//            }
//            return TagUII(hexString)
//        }
//
//
//
//        /**
//         * Return tag UII based on byte list
//         * @param bytes Byte list
//         * @return  Tag UII based on specified byte list
//         * @throws OverflowBitException When the number of bits in the specified byte list exceeds the defined number of bits
//         */
//        @Throws(OverflowBitException::class)
//        fun valueOf(bytes: ByteArray, context: Context?): TagUII {
//            if (bytes.size > 32 /* Up to 256 bits can be input. Since 1 byte is 8 bits, 32 bytes correspond to 256 bits */) {
//                throw OverflowBitException(256, context)
//            }
//            return TagUII(bytes)
//        }
//
//        /**
//         * Verify whether it is a hexadecimal string
//         * @param string The string to be verified
//         * @return True if it is a hexadecimal string. Otherwise, False
//         */
//        private fun checkHexString(string: String): Boolean {
//            for (i in 0 until string.length) {
//                val character = string[i]
//                if (!checkHexCharacter(character)) {
//                    return false
//                }
//            }
//            return true
//        }
//
//        /**
//         * Verify whether it is a hexadecimal character
//         * @param character The character to be verified
//         * @return True if it is a hexadecimal character. Otherwise, False
//         */
//        private fun checkHexCharacter(character: Char): Boolean {
//            for (hexCharacter in hexCharacters) {
//                if (character == hexCharacter) {
//                    return true
//                }
//            }
//            return false
//        }
//    }
//
//    /**
//     * This exception is thrown if the number is not in hexadecimal form
//     */
//    private class NotHexException
//    /**
//     * Initialize
//     */
//    internal constructor(context: Context?) :
//        Exception(context!!.getString(R.string.E_MSG_NOT_HEXADECIMAL))
//
//    /**
//     * This exception is thrown if the number of bits overflows
//     */
//    private class OverflowBitException
//    /**
//     * Initialize from number of bits
//     * @param bitNumber The number of bits
//     */
//    internal constructor(bitNumber: Int, context: Context?) : Exception(
//        String.format(
//            Locale.getDefault(),
//            context!!.getString(R.string.E_MSG_OVER_FLOW_BIT),
//            bitNumber
//        )
//    )
//}
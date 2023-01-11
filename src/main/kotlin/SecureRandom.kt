package org.ton.crypto

import kotlin.random.Random

object SecureRandom : Random() {
    private val javaSecureRandom = java.security.SecureRandom()

    override fun nextBits(bitCount: Int): Int {
        return javaSecureRandom.nextInt() ushr (Int.SIZE_BITS - bitCount)
    }
}

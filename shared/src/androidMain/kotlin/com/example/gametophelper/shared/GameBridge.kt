package com.example.gametophelper.shared

actual object GameBridge {
    init {
        System.loadLibrary("gametophelper")
    }

    actual external fun initGame()
    actual external fun addMoney(amount: Int)
    actual external fun getMoney(): Int
    actual external fun addTap()
    actual external fun buySkin(skinId: Int): Boolean
    actual external fun unlockHelper(index: Int)
    actual external fun selectHelper(index: Int): Boolean
    actual external fun getCurrentHelperBonus(): Int
    actual external fun getCurrentHelperName(): String
    actual external fun getCurrentlyUnlockedGames(): Array<String>
}
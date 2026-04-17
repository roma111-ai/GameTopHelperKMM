package com.example.gametophelper.shared

expect object GameBridge {
    fun initGame()
    fun addMoney(amount: Int)
    fun getMoney(): Int
    fun addTap()
    fun buySkin(skinId: Int): Boolean
    fun unlockHelper(index: Int)
    fun selectHelper(index: Int): Boolean
    fun getCurrentHelperBonus(): Int
    fun getCurrentHelperName(): String
    fun getCurrentlyUnlockedGames(): Array<String>
}
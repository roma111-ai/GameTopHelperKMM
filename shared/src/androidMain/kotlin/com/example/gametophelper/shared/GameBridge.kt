package com.example.gametophelper.shared

object GameBridge {
    init {
        System.loadLibrary("gametophelper")
    }

    // ========== БАЗОВЫЕ ФУНКЦИИ ==========
    external fun initGame(isAdmin: Boolean)
    external fun addMoney(amount: Int)
    external fun getMoney(): Int
    external fun addExperience(amount: Int)
    external fun getExperience(): Int
    external fun getLevel(): Int
    external fun getSuperPowerLevel(): Int
    external fun upgradeSuperPower(): Boolean
    external fun addTap()

    // ========== ЗАГРУЗКА ДАННЫХ ==========
    external fun loadGameData(
        money: Int,
        experience: Int,
        superPowerLevel: Int,
        helperIndex: Int
    )

    // ========== ПОМОЩНИКИ (HELPERS) ==========
    external fun getCurrentHelperIndex(): Int
    external fun getCurrentHelperName(): String
    external fun selectHelper(index: Int)
    external fun setHelperIndex(index: Int)
    external fun isHelperUnlocked(index: Int): Boolean
    external fun unlockHelper(index: Int)

    // ========== МАГАЗИН ==========
    external fun buySkin(skinId: Int): Boolean

    // ========== АДМИНСКИЙ РЕЖИМ ==========
    private var isAdminMode = false

    fun enableAdminMode() {
        isAdminMode = true
    }

    fun disableAdminMode() {
        isAdminMode = false
    }

    fun isAdmin(): Boolean = isAdminMode

    fun initGameWithAdminCheck() {
        initGame(isAdminMode)
    }
}
#include <jni.h>
#include "Player.h"
#include "Shop.h"
#include "Helper.h"
#include <android/log.h>

static Player* player = nullptr;
static Shop* shop = nullptr;
static bool isAdminMode = false;

#define LOG_TAG "GameBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" {

// ========== ИНИЦИАЛИЗАЦИЯ ==========
JNIEXPORT void JNICALL
Java_com_example_gametophelper_shared_GameBridge_initGame(JNIEnv*, jobject, jboolean isAdmin) {
    if (!player) player = new Player();
    if (!shop) shop = new Shop();

    isAdminMode = (isAdmin == JNI_TRUE);
    LOGI("initGame, isAdmin=%d", isAdminMode);

    if (isAdminMode) {
        player->addMoney(999999);
        for (int i = 1; i < 100; i++) {
            player->upgradeSuperPower();
        }
        for (int i = 0; i < 4; i++) {
            player->getHelperManager().unlockHelper(i);
        }
    }
}

// ========== ЗАГРУЗКА ДАННЫХ ==========
JNIEXPORT void JNICALL
Java_com_example_gametophelper_shared_GameBridge_loadGameData(
        JNIEnv*, jobject,
        jint money,
        jint experience,
        jint superPowerLevel,
        jint helperIndex) {

    if (!player) player = new Player();
    if (!shop) shop = new Shop();

    // Загружаем деньги
    if (money > 0) {
        player->addMoney(money);
        LOGI("💰 Загружены деньги: %d", money);
    }

    // Загружаем опыт
    if (experience > 0) {
        player->addExperience(experience);
        LOGI("🎓 Загружен опыт: %d", experience);
    }

    // Загружаем уровень суперсилы
    if (superPowerLevel > 1) {
        for (int i = 1; i < superPowerLevel; i++) {
            player->upgradeSuperPower();
        }
        LOGI("⚡ Загружен уровень суперсилы: %d", superPowerLevel);
    }

    // Загружаем выбранного помощника
    if (helperIndex >= 0 && helperIndex < 4) {
        player->getHelperManager().selectHelper(helperIndex);
        LOGI("🐱 Загружен помощник: %d", helperIndex);
    }

    LOGI("✅ Данные загружены в C++: money=%d, exp=%d, power=%d, helper=%d",
         money, experience, superPowerLevel, helperIndex);
}

// ========== ДЕНЬГИ ==========
JNIEXPORT void JNICALL
Java_com_example_gametophelper_shared_GameBridge_addMoney(JNIEnv*, jobject, jint amount) {
    if (player) player->addMoney(amount);
    LOGI("addMoney: %d", amount);
}

JNIEXPORT jint JNICALL
Java_com_example_gametophelper_shared_GameBridge_getMoney(JNIEnv*, jobject) {
    return player ? player->getMoney() : 0;
}

JNIEXPORT void JNICALL
Java_com_example_gametophelper_shared_GameBridge_addTap(JNIEnv*, jobject) {
    if (player) player->addMoney(10);
    LOGI("addTap called");
}

// ========== ОПЫТ ==========
JNIEXPORT void JNICALL
Java_com_example_gametophelper_shared_GameBridge_addExperience(JNIEnv*, jobject, jint amount) {
    if (player) player->addExperience(amount);
    LOGI("addExperience: %d", amount);
}

JNIEXPORT jint JNICALL
Java_com_example_gametophelper_shared_GameBridge_getExperience(JNIEnv*, jobject) {
    return player ? player->getExperience() : 0;
}

JNIEXPORT jint JNICALL
Java_com_example_gametophelper_shared_GameBridge_getLevel(JNIEnv*, jobject) {
    return player ? player->getLevel() : 1;
}

// ========== СУПЕРСИЛА ==========
JNIEXPORT jint JNICALL
Java_com_example_gametophelper_shared_GameBridge_getSuperPowerLevel(JNIEnv*, jobject) {
    return player ? player->getSuperPowerLevel() : 1;
}

JNIEXPORT jboolean JNICALL
Java_com_example_gametophelper_shared_GameBridge_upgradeSuperPower(JNIEnv*, jobject) {
    if (player) return player->upgradeSuperPower() ? JNI_TRUE : JNI_FALSE;
    return JNI_FALSE;
}

// ========== ПОМОЩНИКИ (HELPERS) ==========
JNIEXPORT jint JNICALL
Java_com_example_gametophelper_shared_GameBridge_getCurrentHelperIndex(JNIEnv*, jobject) {
    if (player) {
        int index = player->getHelperManager().getCurrentHelperIndex();
        LOGI("getCurrentHelperIndex: %d", index);
        return index;
    }
    return 0;
}

JNIEXPORT jstring JNICALL
Java_com_example_gametophelper_shared_GameBridge_getCurrentHelperName(JNIEnv* env, jobject) {
    if (player) {
        return env->NewStringUTF(player->getHelperManager().getCurrentHelper().getName().c_str());
    }
    return env->NewStringUTF("Кошка");
}

JNIEXPORT jint JNICALL
Java_com_example_gametophelper_shared_GameBridge_getCurrentHelperBonus(JNIEnv*, jobject) {
    if (player) return player->getHelperManager().getCurrentBonus();
    return 0;
}

JNIEXPORT void JNICALL
Java_com_example_gametophelper_shared_GameBridge_selectHelper(JNIEnv*, jobject, jint index) {
    if (player) {
        player->getHelperManager().selectHelper(index);
        LOGI("selectHelper: %d", index);
    }
}

JNIEXPORT void JNICALL
Java_com_example_gametophelper_shared_GameBridge_setHelperIndex(JNIEnv*, jobject, jint index) {
    if (player) {
        player->getHelperManager().selectHelper(index);
        LOGI("setHelperIndex: %d", index);
    }
}

JNIEXPORT jboolean JNICALL
Java_com_example_gametophelper_shared_GameBridge_isHelperUnlocked(JNIEnv*, jobject, jint index) {
    if (player) return player->getHelperManager().isHelperUnlocked(index) ? JNI_TRUE : JNI_FALSE;
    return JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_example_gametophelper_shared_GameBridge_unlockHelper(JNIEnv*, jobject, jint index) {
    if (player) player->getHelperManager().unlockHelper(index);
}

// ========== МАГАЗИН ==========
JNIEXPORT jboolean JNICALL
Java_com_example_gametophelper_shared_GameBridge_buySkin(JNIEnv*, jobject, jint skinId) {
    if (!player || !shop) return JNI_FALSE;
    Skin* skin = shop->getSkinById(skinId);
    if (skin && player->buySkin(*skin)) {
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

} // extern "C"
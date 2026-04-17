#include <jni.h>
#include "Player.h"
#include "Shop.h"
#include "Helper.h"

static Player* player = nullptr;
static Shop* shop = nullptr;
static HelperManager* helperManager = nullptr;

extern "C" {

JNIEXPORT void JNICALL
Java_com_example_gametophelper_shared_GameBridge_initGame(JNIEnv*, jobject) {
if (!player) player = new Player();
if (!shop) shop = new Shop();
if (!helperManager) helperManager = new HelperManager();
}

JNIEXPORT void JNICALL
Java_com_example_gametophelper_shared_GameBridge_addMoney(JNIEnv*, jobject, jint amount) {
if (player) player->addMoney(amount);
}

JNIEXPORT jint JNICALL
Java_com_example_gametophelper_shared_GameBridge_getMoney(JNIEnv*, jobject) {
    return player ? player->getMoney() : 0;
}

JNIEXPORT void JNICALL
Java_com_example_gametophelper_shared_GameBridge_addTap(JNIEnv*, jobject) {
if (player) player->addMoney(10);
}

JNIEXPORT jboolean JNICALL
        Java_com_example_gametophelper_shared_GameBridge_buySkin(JNIEnv*, jobject, jint skinId) {
if (!player || !shop) return false;
Skin* skin = shop->getSkinById(skinId);
if (skin && player->buySkin(*skin)) {
return true;
}
return false;
}

JNIEXPORT void JNICALL
Java_com_example_gametophelper_shared_GameBridge_unlockHelper(JNIEnv*, jobject, jint index) {
if (helperManager) helperManager->unlockHelper(index);
}

JNIEXPORT jboolean JNICALL
        Java_com_example_gametophelper_shared_GameBridge_selectHelper(JNIEnv*, jobject, jint index) {
if (helperManager) return helperManager->selectHelper(index);
return false;
}

JNIEXPORT jint JNICALL
Java_com_example_gametophelper_shared_GameBridge_getCurrentHelperBonus(JNIEnv*, jobject) {
    if (helperManager) return helperManager->getCurrentBonus();
    return 0;
}

JNIEXPORT jstring JNICALL
Java_com_example_gametophelper_shared_GameBridge_getCurrentHelperName(JNIEnv* env, jobject) {
    if (helperManager) {
        return env->NewStringUTF(helperManager->getCurrentHelper().getName().c_str());
    }
    return env->NewStringUTF("Cat");
}

JNIEXPORT jobjectArray JNICALL
Java_com_example_gametophelper_shared_GameBridge_getCurrentlyUnlockedGames(JNIEnv* env, jobject) {
    if (!helperManager) return nullptr;

    std::vector<std::string> games = helperManager->getCurrentlyUnlockedGames();
    jobjectArray result = env->NewObjectArray(games.size(), env->FindClass("java/lang/String"), nullptr);

    for (int i = 0; i < (int)games.size(); i++) {
        env->SetObjectArrayElement(result, i, env->NewStringUTF(games[i].c_str()));
    }
    return result;
}

} // extern "C"
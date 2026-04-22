#include "Player.h"
#include <algorithm>

Player::Player() : money(1000), experience(0), level(1), superPowerLevel(1), currentSkinId(0) {}

void Player::addMoney(int amount) { money += amount; }
bool Player::spendMoney(int amount) {
    if (money >= amount) {
        money -= amount;
        return true;
    }
    return false;
}
int Player::getMoney() const { return money; }

void Player::addExperience(int exp) {
    experience += exp;
    if (experience >= level * 100) {
        level++;
        experience = 0;
    }
}
int Player::getExperience() const { return experience; }
int Player::getLevel() const { return level; }

int Player::getSuperPowerLevel() const { return superPowerLevel; }

bool Player::upgradeSuperPower() {
    if (money >= 500) {
        money -= 500;
        superPowerLevel++;
        return true;
    }
    return false;
}

HelperManager& Player::getHelperManager() {
    return helperManager;
}

bool Player::buySkin(const Skin& skin) {
    if (!skin.isOwned() && spendMoney(skin.getPrice())) {
        ownedSkins.push_back(skin);
        return true;
    }
    return false;
}

void Player::equipSkin(int skinId) { currentSkinId = skinId; }
int Player::getCurrentSkinId() const { return currentSkinId; }

void Player::loadSkinsFromStorage(const std::vector<Skin>& skins) {
    ownedSkins = skins;
}
#ifndef PLAYER_H
#define PLAYER_H

#include <vector>
#include "Skin.h"
#include "Helper.h"

class Player {
private:
    int money;
    int experience;
    int level;
    int superPowerLevel;
    HelperManager helperManager;
    std::vector<Skin> ownedSkins;
    int currentSkinId;
public:
    Player();

    // Деньги
    void addMoney(int amount);
    bool spendMoney(int amount);
    int getMoney() const;

    // Опыт
    void addExperience(int exp);
    int getExperience() const;
    int getLevel() const;

    // Суперсила
    int getSuperPowerLevel() const;
    bool upgradeSuperPower();

    // Помощники
    HelperManager& getHelperManager();

    // Скины и магазин
    bool buySkin(const Skin& skin);
    void equipSkin(int skinId);
    int getCurrentSkinId() const;
    void loadSkinsFromStorage(const std::vector<Skin>& skins);
};

#endif
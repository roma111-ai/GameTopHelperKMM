#ifndef HELPER_H
#define HELPER_H

#include <string>
#include <vector>

class Helper {
private:
    std::string name;
    int bonusCoins;
    std::string superpower;
    std::vector<std::string> unlockedGames;
    bool unlocked;
public:
    Helper(const std::string& name, int bonusCoins, const std::string& superpower, const std::vector<std::string>& games);

    std::string getName() const;
    int getBonusCoins() const;
    std::string getSuperpower() const;
    const std::vector<std::string>& getUnlockedGames() const;
    bool isUnlocked() const;
    void unlock();
};

class HelperManager {
private:
    std::vector<Helper> helpers;
    int currentHelperIndex;
public:
    HelperManager();

    void addHelper(const Helper& helper);
    const std::vector<Helper>& getAllHelpers() const;

    // Выбор помощника
    bool selectHelper(int index);
    Helper getCurrentHelper() const;
    int getCurrentHelperIndex() const;
    int getCurrentBonus() const;

    // Разблокировка и покупка
    void unlockHelper(int index);
    bool isHelperUnlocked(int index) const;
    bool buyHelper(int index, int price);   // для магазина позже

    // Игры, открываемые помощником
    std::vector<std::string> getCurrentlyUnlockedGames() const;
};

#endif
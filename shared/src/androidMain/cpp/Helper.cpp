#include "Helper.h"

Helper::Helper(const std::string& name, int bonusCoins, const std::string& superpower, const std::vector<std::string>& games)
        : name(name), bonusCoins(bonusCoins), superpower(superpower), unlockedGames(games), unlocked(false) {}

std::string Helper::getName() const { return name; }
int Helper::getBonusCoins() const { return bonusCoins; }
std::string Helper::getSuperpower() const { return superpower; }
const std::vector<std::string>& Helper::getUnlockedGames() const { return unlockedGames; }
bool Helper::isUnlocked() const { return unlocked; }
void Helper::unlock() { unlocked = true; }

HelperManager::HelperManager() : currentHelperIndex(0) {
    helpers.emplace_back("Darkness", 0, "Purr Power", std::vector<std::string>{"Jump Game"});
    helpers.emplace_back("Vector", 5, "Mathematical Genius", std::vector<std::string>{"Math Game", "Logic Puzzle"});
    helpers.emplace_back("Titan", 10, "Bark Attack", std::vector<std::string>{"Fetch Game"});
    helpers.emplace_back("Lighter", 15, "Copycat Spell", std::vector<std::string>{"Repeat Game"});
}

void HelperManager::addHelper(const Helper& helper) {
    helpers.push_back(helper);
}

const std::vector<Helper>& HelperManager::getAllHelpers() const {
    return helpers;
}

bool HelperManager::selectHelper(int index) {
    if (index >= 0 && index < (int)helpers.size()) {
        currentHelperIndex = index;
        return true;
    }
    return false;
}

Helper HelperManager::getCurrentHelper() const {
    if (helpers.empty()) return Helper("Cat", 0, "No power", {});
    return helpers[currentHelperIndex];
}

int HelperManager::getCurrentBonus() const {
    if (helpers.empty()) return 0;
    return helpers[currentHelperIndex].getBonusCoins();
}

int HelperManager::getCurrentHelperIndex() const {
    return currentHelperIndex;
}

void HelperManager::unlockHelper(int index) {
    if (index >= 0 && index < (int)helpers.size()) {
        helpers[index].unlock();
    }
}

bool HelperManager::isHelperUnlocked(int index) const {
    if (index >= 0 && index < (int)helpers.size()) {
        return helpers[index].isUnlocked();
    }
    return false;
}

bool HelperManager::buyHelper(int index, int price) {
    if (index >= 0 && index < (int)helpers.size() && !helpers[index].isUnlocked()) {
        // Здесь будет проверка монет через Player
        // Пока просто разблокируем
        helpers[index].unlock();
        return true;
    }
    return false;
}

std::vector<std::string> HelperManager::getCurrentlyUnlockedGames() const {
    if (helpers.empty()) return {};
    return helpers[currentHelperIndex].getUnlockedGames();
}
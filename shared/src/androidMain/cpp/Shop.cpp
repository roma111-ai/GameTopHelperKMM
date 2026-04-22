#include "Shop.h"

Shop::Shop() {
    skins.emplace_back(0, "Default Cat", 0);
    skins.emplace_back(1, "Ninja Cat", 500);
    skins.emplace_back(2, "Pirate Cat", 1000);
    skins.emplace_back(3, "Astronaut Cat", 2000);
}

void Shop::addSkin(const Skin& skin) { skins.push_back(skin); }
const std::vector<Skin>& Shop::getSkins() const { return skins; }

Skin* Shop::getSkinById(int id) {
    for (auto& skin : skins) {
        if (skin.getId() == id) return &skin;
    }
    return nullptr;
}
#ifndef SHOP_H
#define SHOP_H

#include <vector>
#include "Skin.h"

class Shop {
private:
    std::vector<Skin> skins;
public:
    Shop();
    void addSkin(const Skin& skin);
    const std::vector<Skin>& getSkins() const;
    Skin* getSkinById(int id);
};

#endif
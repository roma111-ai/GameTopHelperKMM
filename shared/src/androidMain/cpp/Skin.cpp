#include "Skin.h"

Skin::Skin(int id, const std::string& name, int price)
        : id(id), name(name), price(price), owned(false) {}

int Skin::getId() const { return id; }
std::string Skin::getName() const { return name; }
int Skin::getPrice() const { return price; }
bool Skin::isOwned() const { return owned; }
void Skin::setOwned(bool owned) { this->owned = owned; }
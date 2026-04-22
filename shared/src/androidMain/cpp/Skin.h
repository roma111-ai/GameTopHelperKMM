#ifndef SKIN_H
#define SKIN_H

#include <string>

class Skin {
private:
    int id;
    std::string name;
    int price;
    bool owned;
public:
    Skin(int id, const std::string& name, int price);
    int getId() const;
    std::string getName() const;
    int getPrice() const;
    bool isOwned() const;
    void setOwned(bool owned);
};

#endif
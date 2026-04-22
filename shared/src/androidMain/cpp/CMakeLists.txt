cmake_minimum_required(VERSION 3.18.1)

project("gametophelper")

add_library(gametophelper SHARED
        Skin.cpp
        Player.cpp
        Shop.cpp
        Helper.cpp
        GameBridge.cpp
)

target_include_directories(gametophelper PRIVATE .)

target_link_libraries(gametophelper
        android
        log
)
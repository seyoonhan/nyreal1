package com.han.startup.support.concurrent.tree.util;


public class NodeCharacterKey implements NodeCharacterProvider {

    private final Character character;

    public NodeCharacterKey(Character character) {
        this.character = character;
    }

    @Override
    public Character getIncomingEdgeFirstCharacter() {
        return character;
    }
}

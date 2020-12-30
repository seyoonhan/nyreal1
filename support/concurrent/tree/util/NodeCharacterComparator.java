package com.han.startup.support.concurrent.tree.util;


import java.util.Comparator;

public class NodeCharacterComparator implements Comparator<NodeCharacterProvider> {

    @Override
    public int compare(NodeCharacterProvider o1, NodeCharacterProvider o2) {
        return o1.getIncomingEdgeFirstCharacter().compareTo(o2.getIncomingEdgeFirstCharacter());
    }
}

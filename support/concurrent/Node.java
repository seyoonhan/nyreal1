package com.han.startup.support.concurrent;


import com.ubisoft.hfx.support.concurrent.tree.util.NodeCharacterProvider;

import java.io.Serializable;
import java.util.List;

public interface Node extends NodeCharacterProvider, Serializable {

    Character getIncomingEdgeFirstCharacter();

    CharSequence getIncomingEdge();

    Object getValue();


    Node getOutgoingEdge(Character edgeFirstCharacter);

    void updateOutgoingEdge(Node childNode);

    List<Node> getOutgoingEdges();
}

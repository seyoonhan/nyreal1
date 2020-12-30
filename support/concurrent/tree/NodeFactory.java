package com.han.startup.support.concurrent.tree;

import com.ubisoft.hfx.support.concurrent.Node;

import java.io.Serializable;
import java.util.List;

public interface NodeFactory extends Serializable {

    Node createNode(CharSequence edgeCharacters, Object value, List<Node> childNodes, boolean isRoot);

}

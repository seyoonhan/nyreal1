package com.han.startup.support.concurrent.tree.util.concrete;


import com.ubisoft.hfx.support.concurrent.Node;
import com.ubisoft.hfx.support.concurrent.tree.NodeFactory;
import com.ubisoft.hfx.support.concurrent.tree.util.concrete.bytearray.ByteArrayCharSequence;

import java.util.List;

public class SmartArrayBasedNodeFactory implements NodeFactory {

    final NodeFactory charArrayNodeFactory = new DefaultCharArrayNodeFactory();
    final NodeFactory byteArrayNodeFactory = new DefaultByteArrayNodeFactory();

    @Override
    public Node createNode(CharSequence edgeCharacters, Object value, List<Node> childNodes, boolean isRoot) {
        try {
            return byteArrayNodeFactory.createNode(edgeCharacters, value, childNodes, isRoot);
        } catch (ByteArrayCharSequence.IncompatibleCharacterException e) {
            return charArrayNodeFactory.createNode(edgeCharacters, value, childNodes, isRoot);
        }
    }
}

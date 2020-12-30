package com.han.startup.support.concurrent.tree.util.concrete.chararray;


import com.ubisoft.hfx.support.concurrent.CharSequences;
import com.ubisoft.hfx.support.concurrent.Node;
import com.ubisoft.hfx.support.concurrent.tree.util.concrete.voidvalue.VoidValue;

import java.util.Collections;
import java.util.List;

public class CharArrayNodeLeafVoidValue implements Node {

    private final char[] incomingEdgeCharArray;

    public CharArrayNodeLeafVoidValue(CharSequence edgeCharSequence) {
        this.incomingEdgeCharArray = CharSequences.toCharArray(edgeCharSequence);
    }

    @Override
    public CharSequence getIncomingEdge() {
        return CharSequences.fromCharArray(incomingEdgeCharArray);
    }

    @Override
    public Character getIncomingEdgeFirstCharacter() {
        return incomingEdgeCharArray[0];
    }

    @Override
    public Object getValue() {
        return VoidValue.SINGLETON;
    }

    @Override
    public Node getOutgoingEdge(Character edgeFirstCharacter) {
        return null;
    }

    @Override
    public void updateOutgoingEdge(Node childNode) {
        throw new IllegalStateException("Cannot update the reference to the following child node for the edge starting with '" + childNode.getIncomingEdgeFirstCharacter() + "', no such edge already exists: " + childNode);
    }

    @Override
    public List<Node> getOutgoingEdges() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        String sb = "Node{" +
                "edge=" + String.valueOf(incomingEdgeCharArray) +
                ", value=" + VoidValue.SINGLETON +
                ", edges=[]" +
                "}";
        return sb;
    }
}

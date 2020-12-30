package com.han.startup.support.concurrent.tree.util.concrete.charsequence;

import com.ubisoft.hfx.support.concurrent.Node;
import com.ubisoft.hfx.support.concurrent.tree.util.concrete.voidvalue.VoidValue;

import java.util.Collections;
import java.util.List;

public class CharSequenceNodeLeafVoidValue implements Node {


    private final CharSequence incomingEdgeCharSequence;

    public CharSequenceNodeLeafVoidValue(CharSequence edgeCharSequence) {
        this.incomingEdgeCharSequence = edgeCharSequence;
    }

    @Override
    public CharSequence getIncomingEdge() {
        return incomingEdgeCharSequence;
    }

    @Override
    public Character getIncomingEdgeFirstCharacter() {
        return incomingEdgeCharSequence.charAt(0);
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
                "edge=" + incomingEdgeCharSequence +
                ", value=" + VoidValue.SINGLETON +
                ", edges=[]" +
                "}";
        return sb;
    }
}


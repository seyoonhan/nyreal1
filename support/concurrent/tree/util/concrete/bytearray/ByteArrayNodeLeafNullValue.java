package com.han.startup.support.concurrent.tree.util.concrete.bytearray;

import com.ubisoft.hfx.support.concurrent.Node;

import java.util.Collections;
import java.util.List;

public class ByteArrayNodeLeafNullValue implements Node {

    private final byte[] incomingEdgeCharArray;

    public ByteArrayNodeLeafNullValue(CharSequence edgeCharSequence) {
        this.incomingEdgeCharArray = ByteArrayCharSequence.toSingleByteUtf8Encoding(edgeCharSequence);
    }

    @Override
    public CharSequence getIncomingEdge() {
        return new ByteArrayCharSequence(incomingEdgeCharArray, 0, incomingEdgeCharArray.length);
    }

    @Override
    public Character getIncomingEdgeFirstCharacter() {
        return (char) (incomingEdgeCharArray[0] & 0xFF);
    }

    @Override
    public Object getValue() {
        return null;
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
                "edge=" + getIncomingEdge() +
                ", value=null" +
                ", edges=[]" +
                "}";
        return sb;
    }
}

package com.han.startup.support.concurrent.tree.util.concrete.bytearray;

import com.ubisoft.hfx.support.concurrent.Node;

import java.util.Collections;
import java.util.List;

public class ByteArrayNodeLeafWithValue implements Node {


    private final byte[] incomingEdgeCharArray;

    private final Object value;

    public ByteArrayNodeLeafWithValue(CharSequence edgeCharSequence, Object value) {
        this.incomingEdgeCharArray = ByteArrayCharSequence.toSingleByteUtf8Encoding(edgeCharSequence);
        this.value = value;
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
        return value;
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
                ", value=" + value +
                ", edges=[]" +
                "}";
        return sb;
    }
}


package com.han.startup.support.concurrent.tree.util.concrete.bytearray;

import com.ubisoft.hfx.support.concurrent.Node;
import com.ubisoft.hfx.support.concurrent.tree.util.AtomicReferenceArrayListAdapter;
import com.ubisoft.hfx.support.concurrent.tree.util.NodeCharacterComparator;
import com.ubisoft.hfx.support.concurrent.tree.util.NodeUtil;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class ByteArrayNodeNonLeafNullValue implements Node {

    private final byte[] incomingEdgeCharArray;

    private final AtomicReferenceArray<Node> outgoingEdges;
    private final List<Node> outgoingEdgesAsList;

    public ByteArrayNodeNonLeafNullValue(CharSequence edgeCharSequence, List<Node> outgoingEdges) {
        Node[] childNodeArray = outgoingEdges.toArray(new Node[0]);
        Arrays.sort(childNodeArray, new NodeCharacterComparator());
        this.outgoingEdges = new AtomicReferenceArray<>(childNodeArray);
        this.incomingEdgeCharArray = ByteArrayCharSequence.toSingleByteUtf8Encoding(edgeCharSequence);
        this.outgoingEdgesAsList = new AtomicReferenceArrayListAdapter<>(this.outgoingEdges);
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
        int index = NodeUtil.binarySearchForEdge(outgoingEdges, edgeFirstCharacter);
        if (index < 0) {
            return null;
        }

        return outgoingEdges.get(index);
    }

    @Override
    public void updateOutgoingEdge(Node childNode) {
        int index = NodeUtil.binarySearchForEdge(outgoingEdges, childNode.getIncomingEdgeFirstCharacter());
        if (index < 0) {
            throw new IllegalStateException("Cannot update the reference to the following child node for the edge starting with '" + childNode.getIncomingEdgeFirstCharacter() + "', no such edge already exists: " + childNode);
        }

        outgoingEdges.set(index, childNode);
    }

    @Override
    public List<Node> getOutgoingEdges() {
        return outgoingEdgesAsList;
    }

    @Override
    public String toString() {
        String sb = "Node{" +
                "edge=" + getIncomingEdge() +
                ", value=null" +
                ", edges=" + getOutgoingEdges() +
                "}";
        return sb;
    }
}


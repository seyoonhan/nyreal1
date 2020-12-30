package com.han.startup.support.concurrent.tree.util.concrete;


import com.ubisoft.hfx.support.concurrent.CharSequences;
import com.ubisoft.hfx.support.concurrent.Node;
import com.ubisoft.hfx.support.concurrent.tree.NodeFactory;
import com.ubisoft.hfx.support.concurrent.tree.util.NodeUtil;
import com.ubisoft.hfx.support.concurrent.tree.util.concrete.bytearray.*;
import com.ubisoft.hfx.support.concurrent.tree.util.concrete.voidvalue.VoidValue;

import java.util.List;

public class DefaultByteArrayNodeFactory implements NodeFactory {

    @Override
    public Node createNode(CharSequence edgeCharacters, Object value, List<Node> childNodes, boolean isRoot) {
        if (edgeCharacters == null) {
            throw new IllegalStateException("The edgeCharacters argument was null");
        }
        if (!isRoot && edgeCharacters.length() == 0) {
            throw new IllegalStateException("Invalid edge characters for non-root node: " + CharSequences.toString(edgeCharacters));
        }
        if (childNodes == null) {
            throw new IllegalStateException("The childNodes argument was null");
        }
        NodeUtil.ensureNoDuplicateEdges(childNodes);
        if (childNodes.isEmpty()) {
            // Leaf node...
            if (value instanceof VoidValue) {
                return new ByteArrayNodeLeafVoidValue(edgeCharacters);
            } else if (value != null) {
                return new ByteArrayNodeLeafWithValue(edgeCharacters, value);
            } else {
                return new ByteArrayNodeLeafNullValue(edgeCharacters);
            }
        } else {
            // Non-leaf node...
            if (value instanceof VoidValue) {
                return new ByteArrayNodeNonLeafVoidValue(edgeCharacters, childNodes);
            } else if (value == null) {
                return new ByteArrayNodeNonLeafNullValue(edgeCharacters, childNodes);
            } else {
                return new ByteArrayNodeDefault(edgeCharacters, value, childNodes);
            }
        }
    }

}

package com.han.startup.support.concurrent.tree.util.concrete;

import com.ubisoft.hfx.support.concurrent.CharSequences;
import com.ubisoft.hfx.support.concurrent.Node;
import com.ubisoft.hfx.support.concurrent.tree.NodeFactory;
import com.ubisoft.hfx.support.concurrent.tree.util.NodeUtil;
import com.ubisoft.hfx.support.concurrent.tree.util.concrete.charsequence.*;
import com.ubisoft.hfx.support.concurrent.tree.util.concrete.voidvalue.VoidValue;

import java.util.List;

public class DefaultCharSequenceNodeFactory implements NodeFactory {

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
                return new CharSequenceNodeLeafVoidValue(edgeCharacters);
            } else if (value != null) {
                return new CharSequenceNodeLeafWithValue(edgeCharacters, value);
            } else {
                return new CharSequenceNodeLeafNullValue(edgeCharacters);
            }
        } else {
            // Non-leaf node...
            if (value instanceof VoidValue) {
                return new CharSequenceNodeNonLeafVoidValue(edgeCharacters, childNodes);
            } else if (value == null) {
                return new CharSequenceNodeNonLeafNullValue(edgeCharacters, childNodes);
            } else {
                return new CharSequenceNodeDefault(edgeCharacters, value, childNodes);
            }
        }
    }

}

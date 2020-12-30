package com.han.startup.support.concurrent.tree.util;


import com.ubisoft.hfx.support.concurrent.Node;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class NodeUtil {

    public static int binarySearchForEdge(AtomicReferenceArray<Node> childNodes, Character edgeFirstCharacter) {
        // inspired by Collections#indexedBinarySearch()
        int low = 0;
        int high = childNodes.length() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            Node midVal = childNodes.get(mid);
            int cmp = midVal.getIncomingEdgeFirstCharacter().compareTo(edgeFirstCharacter);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found
    }

    public static void ensureNoDuplicateEdges(List<Node> nodes) {
        // Sanity check that no two nodes specify an edge with the same first character...
        Set<Character> uniqueChars = new HashSet<>(nodes.size());
        for (Node node : nodes) {
            uniqueChars.add(node.getIncomingEdgeFirstCharacter());
        }
        if (nodes.size() != uniqueChars.size()) {
            throw new IllegalStateException("Duplicate edge detected in list of nodes supplied: " + nodes);
        }
    }
}


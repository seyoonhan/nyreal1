package com.han.startup.support.concurrent;


import com.ubisoft.hfx.support.concurrent.tree.util.PrettyPrintable;

import java.io.IOException;
import java.util.List;

public class PrettyPrinter {

    /**
     * ○
     * └── ○ B (1)
     * └── ○ A (2)
     * └── ○ N (3)
     * ├── ○ AN (5)
     * │   └── ○ A (6)
     * └── ○ DANA (4)
     */
    public static String prettyPrint(PrettyPrintable tree) {
        StringBuilder sb = new StringBuilder();
        prettyPrint(tree.getNode(), sb, "", true, true);
        return sb.toString();
    }

    /**
     * ○
     * └── ○ B (1)
     * └── ○ A (2)
     * └── ○ N (3)
     * ├── ○ AN (5)
     * │   └── ○ A (6)
     * └── ○ DANA (4)
     */
    public static void prettyPrint(PrettyPrintable tree, Appendable appendable) {
        prettyPrint(tree.getNode(), appendable, "", true, true);
    }

    static void prettyPrint(Node node, Appendable sb, String prefix, boolean isTail, boolean isRoot) {
        try {
            StringBuilder label = new StringBuilder();
            if (isRoot) {
                label.append("○");
                if (node.getIncomingEdge().length() > 0) {
                    label.append(" ");
                }
            }
            label.append(node.getIncomingEdge());
            if (node.getValue() != null) {
                label.append(" (").append(node.getValue()).append(")");
            }
            sb.append(prefix).append(isTail ? isRoot ? "" : "└── ○ " : "├── ○ ").append(label).append("\n");
            List<Node> children = node.getOutgoingEdges();
            for (int i = 0; i < children.size() - 1; i++) {
                prettyPrint(children.get(i), sb, prefix + (isTail ? isRoot ? "" : "    " : "│   "), false, false);
            }
            if (!children.isEmpty()) {
                prettyPrint(children.get(children.size() - 1), sb, prefix + (isTail ? isRoot ? "" : "    " : "│   "), true, false);
            }
        } catch (IOException ioException) {
            // Rethrow the checked exception as a runtime exception...
            throw new IllegalStateException(ioException);
        }
    }
}

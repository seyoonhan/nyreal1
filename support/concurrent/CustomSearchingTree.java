package com.han.startup.support.concurrent;


import com.google.common.collect.Lists;
import com.ubisoft.hfx.support.concurrent.tree.NodeFactory;
import com.ubisoft.hfx.support.concurrent.tree.util.PrettyPrintable;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CustomSearchingTree<O> implements SearchingTree<O>, PrettyPrintable, Serializable {

    private final NodeFactory nodeFactory;
    private final Lock writeLock = new ReentrantLock();
    protected volatile Node root;

    public CustomSearchingTree(NodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
        this.root = nodeFactory.createNode("", null, Collections.emptyList(), true);
    }

    protected void acquireWriteLock() {
        writeLock.lock();
    }

    protected void releaseWriteLock() {
        writeLock.unlock();
    }

    @SuppressWarnings("unchecked")
    @Override
    public O put(CharSequence key, O value) {
        return (O) putInternal(key, value, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public O putIfAbsent(CharSequence key, O value) {
        return (O) putInternal(key, value, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public O getValueForExactKey(CharSequence key) {
        SearchResult searchResult = searchTree(key);
        if (searchResult.classification.equals(SearchResult.Classification.EXACT_MATCH)) {
            return (O) searchResult.nodeFound.getValue();
        }
        return null;
    }

    @Override
    public Iterable<CharSequence> getKeysStartingWith(CharSequence prefix) {
        SearchResult searchResult = searchTree(prefix);
        SearchResult.Classification classification = searchResult.classification;
        switch (classification) {
            case EXACT_MATCH: {
                return getDescendantKeys(prefix, searchResult.nodeFound);
            }
            case KEY_ENDS_MID_EDGE: {
                CharSequence edgeSuffix = CharSequences.getSuffix(searchResult.nodeFound.getIncomingEdge(), searchResult.charsMatchedInNodeFound);
                prefix = CharSequences.concatenate(prefix, edgeSuffix);
                return getDescendantKeys(prefix, searchResult.nodeFound);
            }
            default: {
                return Collections.emptySet();
            }
        }
    }

    @Override
    public Iterable<O> getValuesForKeysStartingWith(CharSequence prefix) {
        SearchResult searchResult = searchTree(prefix);
        SearchResult.Classification classification = searchResult.classification;
        switch (classification) {
            case EXACT_MATCH: {
                return getDescendantValues(prefix, searchResult.nodeFound);
            }
            case KEY_ENDS_MID_EDGE: {
                CharSequence edgeSuffix = CharSequences.getSuffix(searchResult.nodeFound.getIncomingEdge(), searchResult.charsMatchedInNodeFound);
                prefix = CharSequences.concatenate(prefix, edgeSuffix);
                return getDescendantValues(prefix, searchResult.nodeFound);
            }
            default: {
                return Collections.emptySet();
            }
        }
    }

    @Override
    public Iterable<KeyValuePair<O>> getKeyValuePairsForKeysStartingWith(CharSequence prefix) {
        SearchResult searchResult = searchTree(prefix);
        SearchResult.Classification classification = searchResult.classification;
        switch (classification) {
            case EXACT_MATCH: {
                return getDescendantKeyValuePairs(prefix, searchResult.nodeFound);
            }
            case KEY_ENDS_MID_EDGE: {
                CharSequence edgeSuffix = CharSequences.getSuffix(searchResult.nodeFound.getIncomingEdge(), searchResult.charsMatchedInNodeFound);
                prefix = CharSequences.concatenate(prefix, edgeSuffix);
                return getDescendantKeyValuePairs(prefix, searchResult.nodeFound);
            }
            default: {
                return Collections.emptySet();
            }
        }
    }

    @Override
    public boolean remove(CharSequence key) {
        if (key == null) {
            throw new IllegalArgumentException("The key argument was null");
        }
        acquireWriteLock();
        try {
            SearchResult searchResult = searchTree(key);
            SearchResult.Classification classification = searchResult.classification;
            switch (classification) {
                case EXACT_MATCH: {
                    if (searchResult.nodeFound.getValue() == null) {
                        return false;
                    }

                    List<Node> childEdges = searchResult.nodeFound.getOutgoingEdges();
                    if (childEdges.size() > 1) {
                        Node cloned = nodeFactory.createNode(searchResult.nodeFound.getIncomingEdge(), null, searchResult.nodeFound.getOutgoingEdges(), false);
                        searchResult.parentNode.updateOutgoingEdge(cloned);
                    } else if (childEdges.size() == 1) {
                        Node child = childEdges.get(0);
                        CharSequence concatenatedEdges = CharSequences.concatenate(searchResult.nodeFound.getIncomingEdge(), child.getIncomingEdge());
                        Node mergedNode = nodeFactory.createNode(concatenatedEdges, child.getValue(), child.getOutgoingEdges(), false);
                        searchResult.parentNode.updateOutgoingEdge(mergedNode);
                    } else {
                        List<Node> currentEdgesFromParent = searchResult.parentNode.getOutgoingEdges();
                        List<Node> newEdgesOfParent = Arrays.asList(new Node[searchResult.parentNode.getOutgoingEdges().size() - 1]);
                        for (int i = 0, added = 0, numParentEdges = currentEdgesFromParent.size(); i < numParentEdges; i++) {
                            Node node = currentEdgesFromParent.get(i);
                            if (node != searchResult.nodeFound) {
                                newEdgesOfParent.set(added++, node);
                            }
                        }

                        boolean parentIsRoot = (searchResult.parentNode == root);
                        Node newParent;
                        if (newEdgesOfParent.size() == 1 && searchResult.parentNode.getValue() == null && !parentIsRoot) {
                            Node parentsRemainingChild = newEdgesOfParent.get(0);
                            CharSequence concatenatedEdges = CharSequences.concatenate(searchResult.parentNode.getIncomingEdge(), parentsRemainingChild.getIncomingEdge());
                            newParent = nodeFactory.createNode(concatenatedEdges, parentsRemainingChild.getValue(), parentsRemainingChild.getOutgoingEdges(), parentIsRoot);
                        } else {
                            newParent = nodeFactory.createNode(searchResult.parentNode.getIncomingEdge(), searchResult.parentNode.getValue(), newEdgesOfParent, parentIsRoot);
                        }

                        if (parentIsRoot) {
                            this.root = newParent;
                        } else {
                            searchResult.parentNodesParent.updateOutgoingEdge(newParent);
                        }
                    }
                    return true;
                }
                default: {
                    return false;
                }
            }
        } finally {
            releaseWriteLock();
        }
    }

    @Override
    public Iterable<CharSequence> getClosestKeys(CharSequence candidate) {
        SearchResult searchResult = searchTree(candidate);
        SearchResult.Classification classification = searchResult.classification;
        switch (classification) {
            case EXACT_MATCH: {
                return getDescendantKeys(candidate, searchResult.nodeFound);
            }
            case KEY_ENDS_MID_EDGE: {
                CharSequence edgeSuffix = CharSequences.getSuffix(searchResult.nodeFound.getIncomingEdge(), searchResult.charsMatchedInNodeFound);
                candidate = CharSequences.concatenate(candidate, edgeSuffix);
                return getDescendantKeys(candidate, searchResult.nodeFound);
            }
            case INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE: {
                CharSequence keyOfParentNode = CharSequences.getPrefix(candidate, searchResult.charsMatched - searchResult.charsMatchedInNodeFound);
                CharSequence keyOfNodeFound = CharSequences.concatenate(keyOfParentNode, searchResult.nodeFound.getIncomingEdge());
                return getDescendantKeys(keyOfNodeFound, searchResult.nodeFound);
            }
            case INCOMPLETE_MATCH_TO_END_OF_EDGE: {
                if (searchResult.charsMatched == 0) {
                    break;
                }
                CharSequence keyOfNodeFound = CharSequences.getPrefix(candidate, searchResult.charsMatched);
                return getDescendantKeys(keyOfNodeFound, searchResult.nodeFound);
            }
        }
        return Collections.emptySet();
    }

    @Override
    public Iterable<O> getValuesForClosestKeys(CharSequence candidate) {
        SearchResult searchResult = searchTree(candidate);
        SearchResult.Classification classification = searchResult.classification;
        switch (classification) {
            case EXACT_MATCH: {
                return getDescendantValues(candidate, searchResult.nodeFound);
            }
            case KEY_ENDS_MID_EDGE: {
                CharSequence edgeSuffix = CharSequences.getSuffix(searchResult.nodeFound.getIncomingEdge(), searchResult.charsMatchedInNodeFound);
                candidate = CharSequences.concatenate(candidate, edgeSuffix);
                return getDescendantValues(candidate, searchResult.nodeFound);
            }
            case INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE: {
                CharSequence keyOfParentNode = CharSequences.getPrefix(candidate, searchResult.charsMatched - searchResult.charsMatchedInNodeFound);
                CharSequence keyOfNodeFound = CharSequences.concatenate(keyOfParentNode, searchResult.nodeFound.getIncomingEdge());
                return getDescendantValues(keyOfNodeFound, searchResult.nodeFound);
            }
            case INCOMPLETE_MATCH_TO_END_OF_EDGE: {
                if (searchResult.charsMatched == 0) {
                    break;
                }
                CharSequence keyOfNodeFound = CharSequences.getPrefix(candidate, searchResult.charsMatched);
                return getDescendantValues(keyOfNodeFound, searchResult.nodeFound);
            }
        }
        return Collections.emptySet();
    }

    @Override
    public Iterable<KeyValuePair<O>> getKeyValuePairsForClosestKeys(CharSequence candidate) {
        SearchResult searchResult = searchTree(candidate);
        SearchResult.Classification classification = searchResult.classification;
        switch (classification) {
            case EXACT_MATCH: {
                return getDescendantKeyValuePairs(candidate, searchResult.nodeFound);
            }
            case KEY_ENDS_MID_EDGE: {
                CharSequence edgeSuffix = CharSequences.getSuffix(searchResult.nodeFound.getIncomingEdge(), searchResult.charsMatchedInNodeFound);
                candidate = CharSequences.concatenate(candidate, edgeSuffix);
                return getDescendantKeyValuePairs(candidate, searchResult.nodeFound);
            }
            case INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE: {
                CharSequence keyOfParentNode = CharSequences.getPrefix(candidate, searchResult.charsMatched - searchResult.charsMatchedInNodeFound);
                CharSequence keyOfNodeFound = CharSequences.concatenate(keyOfParentNode, searchResult.nodeFound.getIncomingEdge());
                return getDescendantKeyValuePairs(keyOfNodeFound, searchResult.nodeFound);
            }
            case INCOMPLETE_MATCH_TO_END_OF_EDGE: {
                if (searchResult.charsMatched == 0) {
                    break;
                }
                CharSequence keyOfNodeFound = CharSequences.getPrefix(candidate, searchResult.charsMatched);
                return getDescendantKeyValuePairs(keyOfNodeFound, searchResult.nodeFound);
            }
        }
        return Collections.emptySet();
    }

    @Override
    public int size() {
        Deque<Node> stack = new LinkedList<>();
        stack.push(this.root);
        int count = 0;
        while (true) {
            if (stack.isEmpty()) {
                return count;
            }
            Node current = stack.pop();
            stack.addAll(current.getOutgoingEdges());
            if (current.getValue() != null) {
                count++;
            }
        }
    }

    Object putInternal(CharSequence key, Object value, boolean overwrite) {
        if (key == null) {
            throw new IllegalArgumentException("The key argument was null");
        }
        if (key.length() == 0) {
            throw new IllegalArgumentException("The key argument was zero-length");
        }
        if (value == null) {
            throw new IllegalArgumentException("The value argument was null");
        }
        acquireWriteLock();
        try {
            SearchResult searchResult = searchTree(key);
            SearchResult.Classification classification = searchResult.classification;

            switch (classification) {
                case EXACT_MATCH: {
                    Object existingValue = searchResult.nodeFound.getValue();
                    if (!overwrite && existingValue != null) {
                        return existingValue;
                    }
                    Node replacementNode = nodeFactory.createNode(searchResult.nodeFound.getIncomingEdge(), value, searchResult.nodeFound.getOutgoingEdges(), false);
                    searchResult.parentNode.updateOutgoingEdge(replacementNode);
                    return existingValue;
                }
                case KEY_ENDS_MID_EDGE: {
                    CharSequence keyCharsFromStartOfNodeFound = key.subSequence(searchResult.charsMatched - searchResult.charsMatchedInNodeFound, key.length());
                    CharSequence commonPrefix = CharSequences.getCommonPrefix(keyCharsFromStartOfNodeFound, searchResult.nodeFound.getIncomingEdge());
                    CharSequence suffixFromExistingEdge = CharSequences.subtractPrefix(searchResult.nodeFound.getIncomingEdge(), commonPrefix);

                    Node newChild = nodeFactory.createNode(suffixFromExistingEdge, searchResult.nodeFound.getValue(), searchResult.nodeFound.getOutgoingEdges(), false);
                    Node newParent = nodeFactory.createNode(commonPrefix, value, Lists.newArrayList(newChild), false);

                    searchResult.parentNode.updateOutgoingEdge(newParent);

                    return null;
                }
                case INCOMPLETE_MATCH_TO_END_OF_EDGE: {
                    CharSequence keySuffix = key.subSequence(searchResult.charsMatched, key.length());
                    Node newChild = nodeFactory.createNode(keySuffix, value, Collections.emptyList(), false);

                    List<Node> edges = new ArrayList<>(searchResult.nodeFound.getOutgoingEdges().size() + 1);
                    edges.addAll(searchResult.nodeFound.getOutgoingEdges());
                    edges.add(newChild);
                    Node clonedNode = nodeFactory.createNode(searchResult.nodeFound.getIncomingEdge(), searchResult.nodeFound.getValue(), edges, searchResult.nodeFound == root);

                    if (searchResult.nodeFound == root) {
                        this.root = clonedNode;
                    } else {
                        searchResult.parentNode.updateOutgoingEdge(clonedNode);
                    }

                    return null;
                }
                case INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE: {
                    CharSequence keyCharsFromStartOfNodeFound = key.subSequence(searchResult.charsMatched - searchResult.charsMatchedInNodeFound, key.length());
                    CharSequence commonPrefix = CharSequences.getCommonPrefix(keyCharsFromStartOfNodeFound, searchResult.nodeFound.getIncomingEdge());
                    CharSequence suffixFromExistingEdge = CharSequences.subtractPrefix(searchResult.nodeFound.getIncomingEdge(), commonPrefix);
                    CharSequence suffixFromKey = key.subSequence(searchResult.charsMatched, key.length());

                    Node n1 = nodeFactory.createNode(suffixFromKey, value, Collections.emptyList(), false);
                    Node n2 = nodeFactory.createNode(suffixFromExistingEdge, searchResult.nodeFound.getValue(), searchResult.nodeFound.getOutgoingEdges(), false);
                    Node n3 = nodeFactory.createNode(commonPrefix, null, Arrays.asList(n1, n2), false);

                    searchResult.parentNode.updateOutgoingEdge(n3);

                    return null;
                }
                default: {
                    throw new IllegalStateException("Unexpected classification for search result: " + searchResult);
                }
            }
        } finally {
            releaseWriteLock();
        }
    }

    Iterable<CharSequence> getDescendantKeys(final CharSequence startKey, final Node startNode) {
        return () -> new LazyIterator<CharSequence>() {
            Iterator<NodeKeyPair> descendantNodes = lazyTraverseDescendants(startKey, startNode).iterator();

            @Override
            protected CharSequence computeNext() {
                while (descendantNodes.hasNext()) {
                    NodeKeyPair nodeKeyPair = descendantNodes.next();
                    Object value = nodeKeyPair.node.getValue();
                    if (value != null) {
                        CharSequence optionallyTransformedKey = transformKeyForResult(nodeKeyPair.key);

                        return CharSequences.toString(optionallyTransformedKey);
                    }
                }
                return endOfData();
            }
        };
    }

    <O> Iterable<O> getDescendantValues(final CharSequence startKey, final Node startNode) {
        return () -> new LazyIterator<O>() {
            Iterator<NodeKeyPair> descendantNodes = lazyTraverseDescendants(startKey, startNode).iterator();

            @SuppressWarnings("unchecked")
            @Override
            protected O computeNext() {
                while (descendantNodes.hasNext()) {
                    NodeKeyPair nodeKeyPair = descendantNodes.next();
                    Object value = nodeKeyPair.node.getValue();
                    if (value != null) {
                        return (O) value;
                    }
                }
                return endOfData();
            }
        };
    }

    <O> Iterable<KeyValuePair<O>> getDescendantKeyValuePairs(final CharSequence startKey, final Node startNode) {
        return () -> new LazyIterator<KeyValuePair<O>>() {
            Iterator<NodeKeyPair> descendantNodes = lazyTraverseDescendants(startKey, startNode).iterator();

            @Override
            protected KeyValuePair<O> computeNext() {
                while (descendantNodes.hasNext()) {
                    NodeKeyPair nodeKeyPair = descendantNodes.next();
                    Object value = nodeKeyPair.node.getValue();
                    if (value != null) {
                        CharSequence optionallyTransformedKey = transformKeyForResult(nodeKeyPair.key);
                        String keyString = CharSequences.toString(optionallyTransformedKey);
                        return new KeyValuePairImpl<>(keyString, value);
                    }
                }
                return endOfData();
            }
        };
    }

    protected Iterable<NodeKeyPair> lazyTraverseDescendants(final CharSequence startKey, final Node startNode) {
        return () -> new LazyIterator<NodeKeyPair>() {

            Deque<NodeKeyPair> stack = new LinkedList<>();

            {
                stack.push(new NodeKeyPair(startNode, startKey));
            }

            @Override
            protected NodeKeyPair computeNext() {
                if (stack.isEmpty()) {
                    return endOfData();
                }
                NodeKeyPair current = stack.pop();
                List<Node> childNodes = current.node.getOutgoingEdges();

                for (int i = childNodes.size(); i > 0; i--) {
                    Node child = childNodes.get(i - 1);
                    stack.push(new NodeKeyPair(child, CharSequences.concatenate(current.key, child.getIncomingEdge())));
                }
                return current;
            }
        };
    }

    protected CharSequence transformKeyForResult(CharSequence rawKey) {
        return rawKey;
    }

    SearchResult searchTree(CharSequence key) {
        Node parentNodesParent = null;
        Node parentNode = null;
        Node currentNode = root;
        int charsMatched = 0, charsMatchedInNodeFound = 0;

        final int keyLength = key.length();
        outer_loop:
        while (charsMatched < keyLength) {
            Node nextNode = currentNode.getOutgoingEdge(key.charAt(charsMatched));
            if (nextNode == null) {
                break;
            }

            parentNodesParent = parentNode;
            parentNode = currentNode;
            currentNode = nextNode;
            charsMatchedInNodeFound = 0;
            CharSequence currentNodeEdgeCharacters = currentNode.getIncomingEdge();
            for (int i = 0, numEdgeChars = currentNodeEdgeCharacters.length(); i < numEdgeChars && charsMatched < keyLength; i++) {
                if (currentNodeEdgeCharacters.charAt(i) != key.charAt(charsMatched)) {
                    break outer_loop;
                }
                charsMatched++;
                charsMatchedInNodeFound++;
            }
        }
        return new SearchResult(key, currentNode, charsMatched, charsMatchedInNodeFound, parentNode, parentNodesParent);
    }

    @Override
    public Node getNode() {
        return root;
    }

    public static class KeyValuePairImpl<O> implements KeyValuePair<O> {

        final String key;
        final O value;

        @SuppressWarnings("unchecked")
        public KeyValuePairImpl(String key, Object value) {
            this.key = key;
            this.value = (O) value;
        }

        @Override
        public CharSequence getKey() {
            return key;
        }

        @Override
        public O getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            KeyValuePairImpl that = (KeyValuePairImpl) o;

            return key.equals(that.key);

        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public String toString() {
            return "(" + key + ", " + value + ")";
        }
    }

    protected static class NodeKeyPair {
        public final Node node;
        public final CharSequence key;

        public NodeKeyPair(Node node, CharSequence key) {
            this.node = node;
            this.key = key;
        }
    }

    static class SearchResult {
        final CharSequence key;
        final Node nodeFound;
        final int charsMatched;
        final int charsMatchedInNodeFound;
        final Node parentNode;
        final Node parentNodesParent;
        final Classification classification;

        SearchResult(CharSequence key, Node nodeFound, int charsMatched, int charsMatchedInNodeFound, Node parentNode, Node parentNodesParent) {
            this.key = key;
            this.nodeFound = nodeFound;
            this.charsMatched = charsMatched;
            this.charsMatchedInNodeFound = charsMatchedInNodeFound;
            this.parentNode = parentNode;
            this.parentNodesParent = parentNodesParent;

            this.classification = classify(key, nodeFound, charsMatched, charsMatchedInNodeFound);
        }

        protected Classification classify(CharSequence key, Node nodeFound, int charsMatched, int charsMatchedInNodeFound) {
            if (charsMatched == key.length()) {
                if (charsMatchedInNodeFound == nodeFound.getIncomingEdge().length()) {
                    return Classification.EXACT_MATCH;
                } else if (charsMatchedInNodeFound < nodeFound.getIncomingEdge().length()) {
                    return Classification.KEY_ENDS_MID_EDGE;
                }
            } else if (charsMatched < key.length()) {
                if (charsMatchedInNodeFound == nodeFound.getIncomingEdge().length()) {
                    return Classification.INCOMPLETE_MATCH_TO_END_OF_EDGE;
                } else if (charsMatchedInNodeFound < nodeFound.getIncomingEdge().length()) {
                    return Classification.INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE;
                }
            }
            throw new IllegalStateException("Unexpected failure to classify SearchResult: " + this);
        }

        @Override
        public String toString() {
            return "SearchResult{" +
                    "key=" + key +
                    ", nodeFound=" + nodeFound +
                    ", charsMatched=" + charsMatched +
                    ", charsMatchedInNodeFound=" + charsMatchedInNodeFound +
                    ", parentNode=" + parentNode +
                    ", parentNodesParent=" + parentNodesParent +
                    ", classification=" + classification +
                    '}';
        }

        enum Classification {
            EXACT_MATCH,
            INCOMPLETE_MATCH_TO_END_OF_EDGE,
            INCOMPLETE_MATCH_TO_MIDDLE_OF_EDGE,
            KEY_ENDS_MID_EDGE,
        }
    }

}


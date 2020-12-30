package com.han.startup.support.concurrent;


public interface SearchingTree<TK> {

    TK put(CharSequence key, TK value);

    TK putIfAbsent(CharSequence key, TK value);

    boolean remove(CharSequence key);

    TK getValueForExactKey(CharSequence key);

    Iterable<CharSequence> getKeysStartingWith(CharSequence prefix);

    Iterable<TK> getValuesForKeysStartingWith(CharSequence prefix);

    Iterable<KeyValuePair<TK>> getKeyValuePairsForKeysStartingWith(CharSequence prefix);

    Iterable<CharSequence> getClosestKeys(CharSequence candidate);

    Iterable<TK> getValuesForClosestKeys(CharSequence candidate);

    Iterable<KeyValuePair<TK>> getKeyValuePairsForClosestKeys(CharSequence candidate);

    int size();
}


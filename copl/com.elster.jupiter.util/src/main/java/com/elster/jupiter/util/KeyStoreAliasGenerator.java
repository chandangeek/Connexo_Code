package com.elster.jupiter.util;

import java.util.ArrayList;
import java.util.List;

public class KeyStoreAliasGenerator {

    private final String preffix;
    private final int size;

    public KeyStoreAliasGenerator(String preffix, int noOfKeys) {
        this.preffix = preffix;
        this.size = noOfKeys;
    }

    public List<String> getAll() {
        List<String> allAliases = new ArrayList<>(size);
        for (int i=1; i<=size; i++) {
            allAliases.add(getAlias(i));
        }
        return allAliases;
    }

    public String getAlias(int i) {
        return preffix + i;
    }

    public String getPreffix() {
        return preffix;
    }
}

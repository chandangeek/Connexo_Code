package com.elster.jupiter.domain.util;

public enum HasNotAllowedChars {
    ALPHABETS(Constant.SPECIAL_CHARS),
    SCRIPT_CHARS(Constant.SCRIPT_CHARS);

    private String name;
    private HasNotAllowedChars(String regExp) {
        this.name = regExp;
    }

    @Override
    public String toString(){
        return name;
    }
    public static class Constant {
        public static final String SPECIAL_CHARS = "[`~<>'\"/{}=+]";
        public static final String SCRIPT_CHARS = "[<>]";
    }
}
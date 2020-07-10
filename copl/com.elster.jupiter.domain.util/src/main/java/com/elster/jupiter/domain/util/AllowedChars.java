package com.elster.jupiter.domain.util;

public enum AllowedChars {
    ALPHABETS(Constant.TEXT_FEILD_CHARS),
    NUMBERS(Constant.TEXTAREA_FEILD_CHARS);

    private String name;
    private AllowedChars(String regExp) {
        this.name = regExp;
    }

    @Override
    public String toString(){
        return name;
    }
    public static class Constant {
        public static final String TEXT_FEILD_CHARS = "[`~<>'\"/{}=+]";
        public static final String TEXTAREA_FEILD_CHARS = "[<>]";
    }
}
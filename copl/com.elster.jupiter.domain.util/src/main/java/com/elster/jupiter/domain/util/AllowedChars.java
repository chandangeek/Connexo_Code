package com.elster.jupiter.domain.util;

public enum AllowedChars {
    ALPHABETS(Constant.ALPHABETS),
    NUMBERS(Constant.NUMBERS),
    ALPHABETS_AND_NUMBERS(Constant.ALPHABETS_AND_NUMBERS),
    ALLOWED_SPECIAL_CHARS(Constant.ALLOWED_SPECIAL_CHARS),
    ALLOWED_CHARS_WITH_SPACE(Constant.ALLOWED_CHARS_WITH_SPACE);

    private String name;
    private AllowedChars(String regExp) {
        this.name = regExp;
    }

    @Override
    public String toString(){
        return name;
    }
    public static class Constant {
        public static final String ALPHABETS = "[a-zA-Z]";
        public static final String NUMBERS="[0-9]";
        public static final String ALPHABETS_AND_NUMBERS = "^[a-zA-Z0-9._@-]+$";
        public static final String ALLOWED_CHARS_WITH_SPACE = "^[a-zA-Z0-9\\s._@-]+$";
        public static final String ALLOWED_SPECIAL_CHARS = "^[a-zA-Z0-9\\s._*#$!(){}@-]+$";
    }
}

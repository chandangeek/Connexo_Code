package com.energyict.mdc.engine.offline.core;

public interface Translator {

    public String getTranslation(String key);

    public String getTranslation(String key, boolean flagError);

    public String getErrorMsg(String key);

    public String getCustomTranslation(String key);

    String getErrorCode(String messageId);
    public String getTranslation(String key, String defaultValue);
    public boolean hasTranslation(String key);
}

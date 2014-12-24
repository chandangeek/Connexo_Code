package com.elster.jupiter.users.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.FormatKey;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserPreference;
import com.elster.jupiter.users.UserPreferencesService;

public class UserPreferencesServiceImpl implements UserPreferencesService {
    
    public final DataModel dataModel;

    public UserPreferencesServiceImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public UserPreference createUserPreference(Locale locale, FormatKey key, String formatBE, String formatFE, boolean isDefault) {
        UserPreferenceImpl userPreference = new UserPreferenceImpl();
        userPreference.setLanguageTag(locale != null ? locale.toLanguageTag() : null);
        userPreference.setKey(key);
        userPreference.setFormatBE(formatBE);
        userPreference.setFormatFE(formatFE);
        userPreference.setDefault(isDefault);
        Save.CREATE.save(dataModel, userPreference);
        return userPreference;
    }

    @Override
    public List<Locale> getSupportedLocales() {
        Map<Locale, List<UserPreference>> locales = dataModel.mapper(UserPreference.class).find().stream().collect(Collectors.groupingBy(up -> up.getLocale()));
        List<Locale> supportedLocales = new ArrayList<>(locales.size());
        supportedLocales.addAll(locales.keySet());
        return supportedLocales;
    }

    @Override
    public List<UserPreference> getPreferences(User user) {
        return dataModel.mapper(UserPreference.class).find(
                new String[] { "locale", "isDefault" },
                new Object[] { user.getLocale().orElse(Locale.getDefault()).toLanguageTag(), true });
    }

    @Override
    public Optional<UserPreference> getPreferenceByKey(User user, FormatKey key) {
        return this.getPreferenceByKey(user.getLocale().orElse(Locale.getDefault()), key);
    }

    @Override
    public Optional<UserPreference> getPreferenceByKey(Locale locale, FormatKey key) {
        return dataModel.mapper(UserPreference.class).getUnique(
                new String[] { "locale", "key", "isDefault" },
                new Object[] { locale.toLanguageTag(), key, true });
    }
}
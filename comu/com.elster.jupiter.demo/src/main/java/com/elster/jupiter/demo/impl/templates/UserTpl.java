package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.UserBuilder;
import com.elster.jupiter.users.User;

import java.util.Locale;

public enum UserTpl implements Template<User, UserBuilder> {
    MELISSA ("Melissa", Locale.ENGLISH.toLanguageTag(), UserRoles.METER_EXPERT),
    SAM ("Sam", Locale.US.toLanguageTag(), UserRoles.ADMINISTRATORS),
    MONICA ("Monica", Locale.ENGLISH.toLanguageTag(), UserRoles.METER_OPERATOR),
    PIETER ("Pieter", Locale.ENGLISH.toLanguageTag(), UserRoles.ADMINISTRATORS, UserRoles.METER_EXPERT, UserRoles.METER_OPERATOR),
    JOLIEN ("Jolien", Locale.ENGLISH.toLanguageTag(), UserRoles.ADMINISTRATORS, UserRoles.METER_EXPERT, UserRoles.METER_OPERATOR),
    INGE ("Inge", Locale.ENGLISH.toLanguageTag(), UserRoles.ADMINISTRATORS, UserRoles.METER_EXPERT, UserRoles.METER_OPERATOR),
    KOEN ("Koen", Locale.ENGLISH.toLanguageTag(), UserRoles.ADMINISTRATORS, UserRoles.METER_EXPERT, UserRoles.METER_OPERATOR),
    SEBASTIEN ("Sebastien", Locale.ENGLISH.toLanguageTag(), UserRoles.ADMINISTRATORS, UserRoles.METER_EXPERT, UserRoles.METER_OPERATOR),
    VEERLE ("Veerle", Locale.ENGLISH.toLanguageTag(), UserRoles.ADMINISTRATORS, UserRoles.METER_EXPERT, UserRoles.METER_OPERATOR),
    KURT ("Kurt", Locale.ENGLISH.toLanguageTag(), UserRoles.ADMINISTRATORS, UserRoles.METER_EXPERT, UserRoles.METER_OPERATOR),
    EDUARDO ("Eduardo", Locale.US.toLanguageTag(), UserRoles.ADMINISTRATORS, UserRoles.METER_EXPERT, UserRoles.METER_OPERATOR),
    BOB ("Bob", Locale.US.toLanguageTag(), UserRoles.ADMINISTRATORS, UserRoles.METER_EXPERT, UserRoles.METER_OPERATOR),
    MICHELLE ("Michelle", Locale.FRANCE.toLanguageTag(), UserRoles.ADMINISTRATORS, UserRoles.METER_EXPERT, UserRoles.METER_OPERATOR),
    FRANK ("Frank", Locale.UK.toLanguageTag(), UserRoles.ADMINISTRATORS, UserRoles.METER_EXPERT, UserRoles.METER_OPERATOR)
    ;

    private String name;
    private String locale;
    private String[] roles;

    UserTpl(String name, String locale, String... roles) {
        this.name = name;
        this.locale = locale;
        this.roles = roles;
    }

    @Override
    public Class<UserBuilder> getBuilderClass() {
        return UserBuilder.class;
    }

    @Override
    public UserBuilder get(UserBuilder builder) {
        return builder.withName(this.name).withLanguage(this.locale).withRoles(this.roles);
    }

    public static final class UserRoles{
        public static final String ADMINISTRATORS = "Administrator";
        public static final String METER_EXPERT = "Meter expert";
        public static final String METER_OPERATOR = "Meter operator";
        public static final String INSIGHT_EXPERT = "Insight data expert";
        public static final String READ_ONLY = "Read only";

        private UserRoles() {}
    }
}

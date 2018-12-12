/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.UserBuilder;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.streams.FancyJoiner;

import java.util.Arrays;
import java.util.Locale;

public enum UserTpl implements Template<User, UserBuilder> {
    MELISSA("Melissa", Locale.ENGLISH.toLanguageTag(), UserRoles.METER_EXPERT, UserRoles.REPORT_VIEWER),
    MONICA("Monica", Locale.ENGLISH.toLanguageTag(), UserRoles.METER_OPERATOR, UserRoles.REPORT_VIEWER),
    DEVRON("Devron", Locale.ENGLISH.toLanguageTag(), UserRoles.DATA_OPERATOR),
    DOMINIQUE("Dominique", Locale.ENGLISH.toLanguageTag(), UserRoles.DATA_EXPERT),
    SAM("Sander", Locale.US.toLanguageTag(), UserRoles.ADMINISTRATORS),
    UDO("Udo", Locale.ENGLISH.toLanguageTag(), UserRoles.USER_ADMINISTRATOR),
    DON("Don", Locale.ENGLISH.toLanguageTag(), UserRoles.DUAL_CONTROL_ADMINISTRATOR),
    CASANDRA("Casandra", Locale.ENGLISH.toLanguageTag(), UserRoles.COMMAND_LIMITATION_RULE_APPROVER),
    GOVANNI("Govanni", Locale.ENGLISH.toLanguageTag(), UserRoles.COMMAND_LIMITATION_RULE_APPROVER),
    RONNY("Ronny", Locale.ENGLISH.toLanguageTag(), UserRoles.REPORT_DESIGNER),
    FELICE("Félice", Locale.ENGLISH.toLanguageTag(), UserRoles.BUSINESS_PROCESS_DESIGNER),
    THOMAS("Thomas", Locale.ENGLISH.toLanguageTag(), UserRoles.ADMINISTRATORS),
    STIJN("Stijn", Locale.ENGLISH.toLanguageTag(), UserRoles.METER_OPERATOR, UserRoles.REPORT_VIEWER),
    JORIS("Joris", Locale.ENGLISH.toLanguageTag(), UserRoles.METER_OPERATOR, UserRoles.REPORT_VIEWER),
    ROB("Rob", Locale.ENGLISH.toLanguageTag(), UserRoles.METER_OPERATOR, UserRoles.REPORT_VIEWER),
    PIETER("Pieter", Locale.ENGLISH.toLanguageTag(), UserRoles.METER_OPERATOR, UserRoles.REPORT_VIEWER),
    INGE("Inge", Locale.ENGLISH.toLanguageTag(), UserRoles.DATA_OPERATOR),
    JOLIEN("Jolien", Locale.ENGLISH.toLanguageTag(), UserRoles.DATA_OPERATOR),
    RONALD("Ronald", Locale.ENGLISH.toLanguageTag(), UserRoles.DATA_OPERATOR),
    BOB("Bob", Locale.ENGLISH.toLanguageTag(), UserRoles.DATA_OPERATOR)
    ;

    private String name;
    private String locale;
    private String[] roles;

    UserTpl(String name, String locale, String... roles) {
        this.name = name;
        this.locale = locale;
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

    @Override
    public Class<UserBuilder> getBuilderClass() {
        return UserBuilder.class;
    }

    @Override
    public UserBuilder get(UserBuilder builder) {
        String description = Arrays.stream(this.roles).collect(FancyJoiner.joining(", ", " and "));
        return builder.withName(this.name).withDescription(description).withLanguage(this.locale).withRoles(this.roles);
    }

    public static final class UserRoles {
        public static final String ADMINISTRATORS = "System administrator";
        public static final String METER_EXPERT = "Meter expert";
        public static final String METER_OPERATOR = "Meter operator";
        public static final String DATA_EXPERT = "Data expert";
        public static final String DATA_OPERATOR = "Data operator";
        public static final String READ_ONLY = "Read only";
        public static final String USER_ADMINISTRATOR = "User administrator";
        public static final String DUAL_CONTROL_ADMINISTRATOR = "Dual control administrator";
        public static final String COMMAND_LIMITATION_RULE_APPROVER = "Command limitation rule approver";
        public static final String REPORT_DESIGNER = "Report designer";
        public static final String REPORT_VIEWER = "Report viewer";
        public static final String BUSINESS_PROCESS_DESIGNER = "Business process designer";

        private UserRoles() {
        }
    }
}

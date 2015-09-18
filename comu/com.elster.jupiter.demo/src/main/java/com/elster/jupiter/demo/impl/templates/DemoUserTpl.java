package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.UserBuilder;
import com.elster.jupiter.users.User;

import java.util.Collections;
import java.util.Locale;

/**
 * Template for creating a 'DemoUser'
 *
 * Copyrights EnergyICT
 * Date: 17/09/2015
 * Time: 9:56
 */
public class DemoUserTpl implements Template<User, UserBuilder> {

    private String name;

    public DemoUserTpl(String name){
       this.name = name;
    }

     @Override
     public Class<UserBuilder> getBuilderClass() {
         return UserBuilder.class;
     }

     @Override
     public UserBuilder get(UserBuilder builder) {
         return builder.withName(this.name).withPassword("D3mo").withLanguage(Locale.getDefault().toLanguageTag()).withRoles(Collections.singletonList(UserTpl.UserRoles.READ_ONLY));
     }

}

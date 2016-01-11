package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.builders.UserBuilder;
import com.elster.jupiter.users.User;

import java.util.Locale;

/**
 * {@link Template} for creating a 'DemoUser': a user with only 'view' privileges
 * Password is set to "D3mo"
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
         return builder.withName(this.name).withPassword(UserBuilder.DEMO_PASSWORD).withLanguage(Locale.UK.toLanguageTag()).withRoles(UserTpl.UserRoles.READ_ONLY);
     }

}

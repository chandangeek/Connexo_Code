package com.elster.jupiter.parties;

import com.elster.jupiter.cbo.TelephoneNumber;

public interface Person extends Party {

    String TYPE_IDENTIFIER = "P";

    String getFirstName();

    TelephoneNumber getLandLinePhone();

    String getLastName();

    String getMiddleName();

    TelephoneNumber getMobilePhone();

    String getPrefix();

    String getSpecialNeed();

    String getSuffix();

    void setLandLinePhone(TelephoneNumber telephoneNumber);

    void setMiddleName(String mName);

    void setMobilePhone(TelephoneNumber telephoneNumber);

    void setPrefix(String prefix);

    void setSpecialNeed(String specialNeed);

    void setSuffix(String suffix);

    void setFirstName(String firstName);

    void setLastName(String lastName);
}

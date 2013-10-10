package com.elster.jupiter.cbo;

import java.util.Date;

public interface StatusBuilder {

    StatusBuilder value(String value);

    StatusBuilder reason(String reason);

    StatusBuilder remark(String remark);

    StatusBuilder at(Date dateTime);

    Status build();
}

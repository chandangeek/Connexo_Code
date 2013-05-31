package com.elster.jupiter.metering;

import java.math.BigDecimal;
import java.util.Date;

public interface ReadingStorer {
	void add(Channel channel, Date dateTime, long profileStatus , BigDecimal... values);
	void add(Channel channel , Date dateTime, BigDecimal value);
	void add(Channel channel , Date dateTime, BigDecimal value, Date from);
	void add(Channel channel , Date dateTime, BigDecimal value, Date from , Date when);
	boolean overrules();
	void execute();
}

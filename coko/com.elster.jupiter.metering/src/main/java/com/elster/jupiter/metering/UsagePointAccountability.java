/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;

public interface UsagePointAccountability extends Effectivity {
	public UsagePoint getUsagePoint();
	public Party getParty();
	public PartyRole getRole();
	public boolean isCurrent();
}

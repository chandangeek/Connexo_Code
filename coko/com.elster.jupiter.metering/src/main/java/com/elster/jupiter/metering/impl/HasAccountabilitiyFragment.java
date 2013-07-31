package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.plumbing.Bus;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.sql.SqlFragment;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

@LiteralSql
public class HasAccountabilitiyFragment implements SqlFragment {

	private long when;
	
	public HasAccountabilitiyFragment() {
		this(new Date());
	}
	HasAccountabilitiyFragment(Date when) {
		this.when = when.getTime();
	}
	
	@Override
	public int bind(PreparedStatement statement, int position) throws SQLException {
		statement.setString(position++, Bus.getOrmClient().getDataModel().getPrincipal().getName());
		statement.setLong(position++, when);
		statement.setLong(position++, when);
		statement.setLong(position++, when);
		statement.setLong(position++, when);
		return position;
	}

	@Override
	public String getText() {
        // TODO depends on alias generation in QueryExecutor, expose generated alias?
		return 
			"exists (select null from mtr_upaccountability upa join prt_partyrep pr on upa.partyid = pr.partyid " +
			"where \"up\".id = upa.usagepointid and pr.delegate = ? and " +
			"upa.starttime <= ? and upa.endtime > ? and pr.starttime <= ? and pr.endtime > ?)";
	}

}

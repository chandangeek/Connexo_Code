package com.elster.jupiter.orm.plumbing;

import com.elster.jupiter.orm.callback.InstallService;

public class Installer implements InstallService {

	@Override
	public void install() {
		Bus.getOrmClient().install(true, true);
	}

}

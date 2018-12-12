/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.validation.impl;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ProviderResolverServiceTest {
	@Test
	public void test() {
		assertThat(new ProviderResolverService().getValidationProviders()).isNotEmpty();
	}
}

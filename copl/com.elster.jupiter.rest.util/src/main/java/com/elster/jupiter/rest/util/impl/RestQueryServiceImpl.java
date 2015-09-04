package com.elster.jupiter.rest.util.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.OrmService;
import org.osgi.service.component.annotations.Component;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.rest.util.*;

import java.util.Arrays;
import java.util.List;

@Component (
		name = "com.elster.jupiter.rest.util",
		service = {RestQueryService.class, TranslationKeyProvider.class},
		property = "name=" + MessageSeeds.COMPONENT_NAME,
		immediate = true)
public class RestQueryServiceImpl implements RestQueryService, TranslationKeyProvider {
	
	@Override
	public <T> RestQuery<T> wrap(Query<T> query) {
		return new RestQueryImpl<>(query);
	}


	@Override
	public String getComponentName() {
		return MessageSeeds.COMPONENT_NAME;
	}

	@Override
	public Layer getLayer() {
		return Layer.REST;
	}

	@Override
	public List<TranslationKey> getKeys() {
		return Arrays.asList(MessageSeeds.values());
	}
}

package com.elster.jupiter.rest.util.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.util.exception.MessageSeed;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

@Component (
		name = "com.elster.jupiter.rest.util",
		service = {RestQueryService.class, MessageSeedProvider.class},
		property = "name=" + MessageSeeds.COMPONENT_NAME,
		immediate = true)
public class RestQueryServiceImpl implements RestQueryService, MessageSeedProvider {

	@Override
	public <T> RestQuery<T> wrap(Query<T> query) {
		return new RestQueryImpl<>(query);
	}

	@Override
	public Layer getLayer() {
		return Layer.REST;
	}

	@Override
	public List<MessageSeed> getSeeds() {
		return Arrays.asList(MessageSeeds.values());
	}

}
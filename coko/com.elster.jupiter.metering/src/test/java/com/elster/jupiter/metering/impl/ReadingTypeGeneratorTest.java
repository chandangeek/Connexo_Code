/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyString;

@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeGeneratorTest {

    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private MeteringServiceImpl meteringService;

    @Before
    public void setUp() {
    	when(meteringService.createReadingType(anyString(), anyString())).thenAnswer(invocationOnMock -> {
            Object[] args = invocationOnMock.getArguments();
            return new ReadingTypeImpl(dataModel, thesaurus).init((String) args[0], (String) args[1]);
        });
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGeneration() {
    	List<Pair<String, String>> generated = ReadingTypeGenerator.generate();
    	Set<String> mRIDs = new HashSet<>();
    	Set<String> aliases = new HashSet<>();
    	for (Pair<String, String> each : generated) {
    		mRIDs.add(each.getFirst());
    		aliases.add(each.getLast());
    	}
        System.out.println(generated.size());
        assertThat(generated.size()).isEqualTo(mRIDs.size());
    }
}

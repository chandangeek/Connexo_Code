package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

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
    	when(meteringService.createReadingType(anyString(), anyString())).thenAnswer(new Answer<Object>() {
    		@Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
    			Object[] args = invocationOnMock.getArguments();
    			return new ReadingTypeImpl(dataModel, thesaurus).init((String) args[0], (String) args[1]);
    		}
    	});
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGeneration() {
    	List<ReadingTypeImpl> generated = ReadingTypeGenerator.generate(meteringService);
    	Set<String> mRIDs = new HashSet<>();
    	Set<String> aliases = new HashSet<>();
    	for (ReadingType each : generated) {
    		mRIDs.add(each.getMRID());
    		aliases.add(each.getAliasName());
    	}
    	assertThat(generated.size()).isEqualTo(aliases.size()).isEqualTo(mRIDs.size());
    }
}

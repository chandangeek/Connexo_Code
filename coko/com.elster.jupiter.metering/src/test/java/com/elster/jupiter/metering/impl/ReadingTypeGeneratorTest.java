package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.ReadingType;
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

@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeGeneratorTest {

    @Mock
    private DataModel dataModel;

    @Before
    public void setUp() {
        when(dataModel.getInstance(ReadingTypeImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ReadingTypeImpl(dataModel);
            }
        });
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGeneration() {
    	List<ReadingTypeImpl> generated = ReadingTypeGenerator.generate(dataModel);
    	Set<String> mRIDs = new HashSet<>();
    	Set<String> aliases = new HashSet<>();
    	for (ReadingType each : generated) {
    		mRIDs.add(each.getMRID());
    		aliases.add(each.getAliasName());
    	}
    	assertThat(generated.size()).isEqualTo(aliases.size()).isEqualTo(mRIDs.size());
    }
}

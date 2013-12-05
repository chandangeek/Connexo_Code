package com.elster.jupiter.metering.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.elster.jupiter.metering.ReadingType;
import static org.assertj.core.api.Assertions.assertThat;

public class ReadingTypeGeneratorTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGeneration() {
    	List<ReadingTypeImpl> generated = ReadingTypeGenerator.generate();
    	Set<String> mRIDs = new HashSet<>();
    	Set<String> aliases = new HashSet<>();
    	for (ReadingType each : generated) {
    		mRIDs.add(each.getMRID());
    		aliases.add(each.getAliasName());
    	}
    	assertThat(generated.size()).isEqualTo(aliases.size()).isEqualTo(mRIDs.size());
    }
}

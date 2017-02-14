/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.rest.util.impl.MessageSeeds;
import com.elster.jupiter.util.exception.MessageSeed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonModel;

import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VersionInfoTest {
    private static ObjectMapper mapper = new ObjectMapper();

    private JsonModel getJsonModel(Object info) throws Exception{
        return JsonModel.create(mapper.writeValueAsString(info));
    }

    private String fullJson(String jsonForIdField) throws Exception{
        return  "{\"parent\": {\"id\":" + jsonForIdField + ", \"version\": 1}}";
    }

    private <T> VersionInfo<T> getVersionInfo(String jsonForIdField) throws Exception{
        String json = "{\"id\":" + jsonForIdField + ", \"version\": 1}";
        return mapper.readValue(json, new TypeReference<VersionInfo<T>>() {
        });
    }

    @Test
    public void testSerializationForEmptyArrays() throws Exception{
        VersionInfo<long[]> info = new VersionInfo<>();
        info.id = new long[]{};
        info.version = 1L;
        JsonModel model = getJsonModel(info);
        assertThat(model.<List>get("$.id")).isEmpty();
    }

    @Test
    public void testSerializationForLongArrays() throws Exception{
        VersionInfo<long[]> info = new VersionInfo<>();
        info.id = new long[]{1L, 9L};
        info.version = 1L;
        JsonModel model = getJsonModel(info);
        assertThat(model.<List>get("$.id")).hasSize(2);
        assertThat(model.<Number>get("$.id[0]")).isEqualTo(1);
    }

    @Test
    public void testSerializationForStringArrays() throws Exception{
        VersionInfo<String[]> info = new VersionInfo<>();
        info.id = new String[]{"132", "123"};
        info.version = 1L;
        JsonModel model = getJsonModel(info);
        assertThat(model.<List>get("$.id")).hasSize(2);
        assertThat(model.<String>get("$.id[0]")).isEqualTo("132");
    }

    @Test
    public void testSerializationForObjectArrays() throws Exception{
        VersionInfo<Object[]> info = new VersionInfo<>();
        info.id = new Object[]{"132", 5L};
        info.version = 1L;
        JsonModel model = getJsonModel(info);
        assertThat(model.<List>get("$.id")).hasSize(2);
        assertThat(model.<String>get("$.id[0]")).isEqualTo("132");
        assertThat(model.<Number>get("$.id[1]")).isEqualTo(5);
    }

    @Test
    public void testSerializationForLong() throws Exception{
        VersionInfo<Long> info = new VersionInfo<>();
        info.id = 9L;
        info.version = 1L;
        JsonModel model = getJsonModel(info);
        assertThat(model.<Number>get("$.id")).isEqualTo(9);
    }

    @Test
    public void testSerializationForString() throws Exception{
        VersionInfo<String> info = new VersionInfo<>();
        info.id = "132";
        info.version = 1L;
        JsonModel model = getJsonModel(info);
        assertThat(model.<String>get("$.id")).isEqualTo("132");
    }

    private static class UseVersionInfoWithLongArrayId{
        public VersionInfo<long[]> parent;
    }

    @Test
    public void testDeserializationForEmptyArrays() throws Exception {
        UseVersionInfoWithLongArrayId info = mapper.readValue(fullJson("[]"), UseVersionInfoWithLongArrayId.class);
        assertThat(info.parent.id).isEmpty();
    }

    @Test
    public void testDeserializationForLongArrays() throws Exception{
        UseVersionInfoWithLongArrayId info = mapper.readValue(fullJson("[1, 9]"), UseVersionInfoWithLongArrayId.class);
        assertThat(info.parent.id).hasSize(2);
        assertThat(info.parent.id[0]).isEqualTo(1L);
        assertThat(info.parent.id[1]).isEqualTo(9L);
    }

    private static class UseVersionInfoWithStringArrayId{
        public VersionInfo<String[]> parent;
    }

    @Test
    public void testDeserializationForStringArrays() throws Exception{
        UseVersionInfoWithStringArrayId info = mapper.readValue(fullJson("[\"132\", \"589\"]"), UseVersionInfoWithStringArrayId.class);
        assertThat(info.parent.id).hasSize(2);
        assertThat(info.parent.id[0]).isEqualTo("132");
    }

    private static class UseVersionInfoWithObjectArrayId{
        public VersionInfo<Object[]> parent;
    }

    @Test
    public void testDeserializationForObjectArrays() throws Exception{
        UseVersionInfoWithObjectArrayId info = mapper.readValue(fullJson("[\"132\", 5]"), UseVersionInfoWithObjectArrayId.class);
        assertThat(info.parent.id).hasSize(2);
        assertThat(info.parent.id[0]).isEqualTo("132");
        assertThat(info.parent.id[1]).isEqualTo(5);
    }


    private static class UseVersionInfoWithLongId{
        public VersionInfo<Long> parent;
    }

    @Test
    public void testDeserializationForLong() throws Exception{
        UseVersionInfoWithLongId info = mapper.readValue(fullJson("5"), UseVersionInfoWithLongId.class);
        assertThat(info.parent.id).isEqualTo(5);
    }

    @Test
    public void testDeserializationForString() throws Exception{
        VersionInfo<String> info = getVersionInfo("\"7\"");
        assertThat(info.id).isEqualTo("7");
    }

    @Test
    public void testSerializationErrorInfoSingleId() throws Exception {
        Thesaurus thesaurus = mock(Thesaurus.class);
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
        when(thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        ConcurrentModificationException exception = new ConcurrentModificationException(thesaurus, MessageSeeds.CONCURRENT_EDIT_TITLE, "some");
        exception.setMessageBody(MessageSeeds.CONCURRENT_EDIT_BODY);
        exception.setMessageBodyArgs(new String[]{"some"});
        exception.setVersion(() -> 2L);
        exception.setParentVersion(() -> 12L);
        exception.setParentId(new Long[]{123L});
        ConcurrentModificationInfo info = new ConcurrentModificationInfo().from(exception);
        JsonModel model = getJsonModel(info);
        assertThat(model.<Number>get("$.parent.id")).isEqualTo(123);
    }

    @Test
    public void testSerializationErrorInfoArrayId() throws Exception {
        Thesaurus thesaurus = mock(Thesaurus.class);
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
        when(thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        ConcurrentModificationException exception = new ConcurrentModificationException(thesaurus, MessageSeeds.CONCURRENT_EDIT_TITLE, "some");
        exception.setMessageBody(MessageSeeds.CONCURRENT_EDIT_BODY);
        exception.setMessageBodyArgs(new String[]{"some"});
        exception.setVersion(() -> 2L);
        exception.setParentVersion(() -> 12L);
        exception.setParentId(new Object[]{123L, "bla-bla"});
        ConcurrentModificationInfo info = new ConcurrentModificationInfo().from(exception);
        JsonModel model = getJsonModel(info);
        assertThat(model.<List>get("$.parent.id")).hasSize(2);
        assertThat(model.<Number>get("$.parent.id[0]")).isEqualTo(123);
        assertThat(model.<String>get("$.parent.id[1]")).isEqualTo("bla-bla");
    }

    @Test
    public void testSerializationErrorInfoNullParentId() throws Exception {
        Thesaurus thesaurus = mock(Thesaurus.class);
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
        when(thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
        when(thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        ConcurrentModificationException exception = new ConcurrentModificationException(thesaurus, MessageSeeds.CONCURRENT_EDIT_TITLE, "some");
        exception.setMessageBody(MessageSeeds.CONCURRENT_EDIT_BODY);
        exception.setMessageBodyArgs(new String[]{"some"});
        exception.setVersion(() -> 2L);
        exception.setParentVersion(() -> 12L);
        exception.setParentId(new Long[]{});
        ConcurrentModificationInfo info = new ConcurrentModificationInfo().from(exception);
        JsonModel model = getJsonModel(info);
        assertThat(model.<Object>get("$.parent")).isNull();
    }
}

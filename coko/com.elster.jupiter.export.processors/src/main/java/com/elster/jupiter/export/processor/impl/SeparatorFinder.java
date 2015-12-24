package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.CanFindByStringKey;

import java.util.Arrays;
import java.util.Optional;

public class SeparatorFinder implements CanFindByStringKey<TranslatablePropertyValueInfo> {

    private final Thesaurus thesaurus;

    public SeparatorFinder(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public Optional<TranslatablePropertyValueInfo> find(String key) {
        return Arrays.stream(getValues())
                .filter(candidate -> candidate.getId().equals(key))
                .findFirst();
    }

    @Override
    public Class<TranslatablePropertyValueInfo> valueDomain() {
        return TranslatablePropertyValueInfo.class;
    }

    public TranslatablePropertyValueInfo[] getValues() {
        return new TranslatablePropertyValueInfo[]{
                new TranslatablePropertyValueInfo(FormatterProperties.SEPARATOR_COMMA.getKey(), this.thesaurus.getFormat(FormatterProperties.SEPARATOR_COMMA).format()),
                new TranslatablePropertyValueInfo(FormatterProperties.SEPARATOR_SEMICOLON.getKey(), this.thesaurus.getFormat(FormatterProperties.SEPARATOR_SEMICOLON).format())
        };
    }
}
package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.MessageSeeds;

import javax.validation.constraints.Size;
import java.time.Instant;

public class SystemPropertyImpl implements SystemProperty {

    private long id;

    @Size(max = Table.NAME_LENGTH, message = "{" + "TO-DO: NAME TOOOOOO LONG please make it correct" + "}")
    private String propertyName;

    @Size(max = Table.NAME_LENGTH, message = "{" + "TO-DO: NAME TOOOOOO LONG please make it correct" + "}")
    private String propertyValue;

    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName; // for auditing
    @SuppressWarnings("unused")
    private long version;


    public enum Fields {
        ID("id"),
        PROP_NAME("propertyName"),
        PROP_VALUE("propertyValue");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }


    @Override
    public long getId(){
        return  id;
    };

    @Override
    public String getName(){
        return propertyName;
    };

    @Override
    public String getValue(){
        return propertyValue;
    };





}

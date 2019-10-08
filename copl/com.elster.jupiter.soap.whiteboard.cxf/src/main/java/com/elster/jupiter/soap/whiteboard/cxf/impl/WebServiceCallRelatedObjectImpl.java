/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedObject;
import com.elster.jupiter.util.HasId;

import javax.inject.Inject;

public class WebServiceCallRelatedObjectImpl implements WebServiceCallRelatedObject, HasId {

    private final DataModel dataModel;

    private long id;
    private String key;
    private String value;

    public enum Fields {
        ID("id"),
        OBJECT_KEY("key"),
        OBJECT_VALUE("value");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @Inject
    public WebServiceCallRelatedObjectImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }



    public WebServiceCallRelatedObjectImpl init(String key,
                                                String value) {
        this.key = key;
        this.value = value;
        return this;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getKey(){return key;}

    @Override
    public String getValue(){return value;}

    public void save() {
        if (id > 0) {
            Save.UPDATE.save(this.dataModel, this, Save.Update.class);
        } else {
            Save.CREATE.save(this.dataModel, this, Save.Create.class);
        }
    }
}

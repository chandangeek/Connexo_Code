/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedAttribute;
import com.elster.jupiter.util.HasId;

import javax.inject.Inject;

public class WebServiceCallRelatedAttributeImpl implements WebServiceCallRelatedAttribute, HasId {

    private final DataModel dataModel;

    private long id;
    private String key;
    private String value;

    public enum Fields {
        ID("id"),
        ATTRIBUTE_KEY("key"),
        ATTRIBUTE_VALUE("value");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @Inject
    public WebServiceCallRelatedAttributeImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public WebServiceCallRelatedAttributeImpl init(String key,
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
    public String getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void save() {
        if (id > 0) {
            Save.UPDATE.save(this.dataModel, this, Save.Update.class);
        } else {
            Save.CREATE.save(this.dataModel, this, Save.Create.class);
        }
    }
    @Override
    public String toString () {
        return "KEY = "+key +"value = "+value;
    }


    @Override
    public boolean equals(Object o) {
        System.out.println("CALL equals!!!!!!!!");

        if (this == o) {
            System.out.println("THE SAME OBJECT!!!!!!!!");
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            System.out.println("NULL or not the same object !!!!!!!!" );
            return false;
        }
        /*if (!super.equals(o)) {
            return false;
        }*/

        WebServiceCallRelatedAttributeImpl that = (WebServiceCallRelatedAttributeImpl) o;
        return this.getKey().equals(that.getKey()) && this.getValue().equals(that.getValue());

    }
}

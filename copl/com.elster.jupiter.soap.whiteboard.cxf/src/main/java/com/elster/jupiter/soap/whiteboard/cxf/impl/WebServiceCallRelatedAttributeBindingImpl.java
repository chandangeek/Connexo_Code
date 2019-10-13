/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedAttributeBinding;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedAttribute;
import com.elster.jupiter.util.HasId;

import javax.inject.Inject;

public class WebServiceCallRelatedAttributeBindingImpl implements WebServiceCallRelatedAttributeBinding, HasId {
    private final DataModel dataModel;

    private long id;
    @IsPresent
    private Reference<WebServiceCallOccurrence> occurrence = Reference.empty();
    @IsPresent
    private Reference<WebServiceCallRelatedAttribute> type = Reference.empty();

    public enum Fields {
        ID("id"),
        OCCURRENCE("occurrence"),
        TYPE("type");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @Inject
    public WebServiceCallRelatedAttributeBindingImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }


    public WebServiceCallRelatedAttributeBindingImpl init(WebServiceCallOccurrence occurrence,
                                                          WebServiceCallRelatedAttribute type) {
        this.occurrence.set(occurrence);
        this.type.set(type);
        return this;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public WebServiceCallOccurrence getOccurrence() {
        return this.occurrence.get();
    }

    @Override
    public WebServiceCallRelatedAttribute getType() {
        return this.type.get();
    }


    public void save() {
        if (id > 0) {
            Save.UPDATE.save(this.dataModel, this, Save.Update.class);
        } else {
            Save.CREATE.save(this.dataModel, this, Save.Create.class);
        }
    }
}


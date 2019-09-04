package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.HasId;

import javax.inject.Inject;

public class WebServiceCallRelatedObjectType implements HasId {

    private final DataModel dataModel;

    private long id;
    private String typeDomain;
    private String typeTranslation;
    private String key;
    private String value;

    public enum Fields {
        ID("id"),
        TYPE_DOMAIN("typeDomain"),
        TYPE_TRANSLATION("typeTranslation"),
        TYPE_KEY("key"),
        TYPE_VALUE("value");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @Inject
    public WebServiceCallRelatedObjectType(DataModel dataModel) {
        this.dataModel = dataModel;
    }



    public WebServiceCallRelatedObjectType init(String typeDomain,
                                                String key,
                                                String value) {
        this.typeDomain = typeDomain;
        this.key = key;
        this.value = value;
        return this;
    }

    @Override
    public long getId() {
        return this.id;
    }

    public String getTypeDomain(){return typeDomain;}

    public String getTypeTranslation(){return typeTranslation;}

    public String getKey(){return key;}

    public String getValue(){return value;}

    public void save() {
        if (id > 0) {
            Save.UPDATE.save(this.dataModel, this, Save.Update.class);
        } else {
            Save.CREATE.save(this.dataModel, this, Save.Create.class);
        }
    }
}

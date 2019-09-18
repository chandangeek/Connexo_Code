package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedObjectType;
import com.elster.jupiter.util.HasId;

import javax.inject.Inject;

public class WebServiceCallRelatedObjectTypeImpl implements WebServiceCallRelatedObjectType, HasId {

    private final DataModel dataModel;

    private long id;
    private String typeDomain;
    private String typeTranslation;
    private String key;
    private String value;

    public enum Fields {
        ID("id"),
        //TYPE_DOMAIN("typeDomain"),
        //TYPE_TRANSLATION("typeTranslation"),
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
    public WebServiceCallRelatedObjectTypeImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }



    public WebServiceCallRelatedObjectTypeImpl init(String key,
                                                    String value) {
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

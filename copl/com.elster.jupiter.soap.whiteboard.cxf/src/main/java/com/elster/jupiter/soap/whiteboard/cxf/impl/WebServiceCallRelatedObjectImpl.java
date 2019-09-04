package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedObject;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.HasId;

import javax.inject.Inject;
import java.util.Optional;

public class WebServiceCallRelatedObjectImpl implements WebServiceCallRelatedObject, HasId {
    private final DataModel dataModel;

    private long id;
    private Reference<WebServiceCallOccurrence> occurrence = Reference.empty();
    private Reference<WebServiceCallRelatedObjectType> type = Reference.empty();

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
    public WebServiceCallRelatedObjectImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }



    public WebServiceCallRelatedObjectImpl init(WebServiceCallOccurrence occurrence,
                                                WebServiceCallRelatedObjectType type) {
        this.occurrence.set(occurrence);
        this.type.set(type);
        return this;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public Optional<WebServiceCallOccurrence> getOccurrence(){
        return this.occurrence.getOptional();
    }

    @Override
    public Optional<WebServiceCallRelatedObjectType> getType(){return this.type.getOptional();}


    public void save() {
        if (id > 0) {
            Save.UPDATE.save(this.dataModel, this, Save.Update.class);
        } else {
            Save.CREATE.save(this.dataModel, this, Save.Create.class);
        }
    }
}


package com.elster.jupiter.systemproperties;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.domain.util.Save;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;

public class SystemPropertyImpl implements SystemProperty{
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


    private DataModel dataModel;

    @Inject
    public SystemPropertyImpl(DataModel dataModel){
        this.dataModel = dataModel;
    }


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


    /*@Override
    public long getId(){
        return  id;
    };*/

    @Override
    public String getName(){
        return propertyName;
    };

    @Override
    public String getValue(){
        return propertyValue;
    };

    @Override
    public void setValue(String value){
        this.propertyValue = value;
    };

    public void update(){
        System.out.println("TRY YO SAVE UPDATED OBJECT !!!!!!!!!!");
        Save.UPDATE.save(this.dataModel, this);
        System.out.println("UPDATE WAS PERFORMED!!!!!!!!!!");
    }
}

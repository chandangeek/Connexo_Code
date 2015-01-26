package com.elster.jupiter.demo.impl.factories;

import com.elster.jupiter.demo.impl.Log;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;

public class ReadingTypeFactory implements Factory<ReadingType> {
    private static final String CREATE_READING_TYPE = "INSERT INTO MTR_READINGTYPE (MRID, ALIASNAME, VERSIONCOUNT, CREATETIME, MODTIME, USERNAME)" +
            "VALUES(?, ?, 1, ?, ?, 'Demo')";

    private final DataModel dataModel;
    private final MeteringService meteringService;

    private String mrid;
    private String aliasName;

    @Inject
    public ReadingTypeFactory(DataModel dataModel, MeteringService meteringService) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
    }

    public ReadingTypeFactory withMrid(String mrid){
        this.mrid = mrid;
        return this;
    }

    public ReadingTypeFactory withAlias(String alias){
        this.aliasName = alias;
        return this;
    }

    @Override
    public ReadingType get() {
        Log.write(this);
        if (this.mrid == null) {
            throw new UnableToCreate("Mrid can't be null");
        }
        if (this.aliasName == null) {
            throw new UnableToCreate("Alias name can't be null");
        }
        try (Connection connection = this.dataModel.getConnection(false)){
            PreparedStatement statement = connection.prepareStatement(CREATE_READING_TYPE);
            Instant creationTime = Instant.now();

            statement.setString(1, this.mrid);
            statement.setString(2, this.aliasName);
            statement.setLong(3, creationTime.toEpochMilli());
            statement.setLong(4, creationTime.toEpochMilli());
            statement.execute();
            return meteringService.getReadingType(this.mrid).get();
        } catch (SQLException e) {
            throw new UnableToCreate("Unable to execute native sql command for reading type creation: " + e.getMessage());
        }
    }
}

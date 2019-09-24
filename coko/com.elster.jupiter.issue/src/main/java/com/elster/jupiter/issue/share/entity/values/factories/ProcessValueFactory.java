package com.elster.jupiter.issue.share.entity.values.factories;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.issue.share.entity.values.ProcessValue;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.ProcessPropertyFactory;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProcessValueFactory implements ValueFactory<ProcessValue>, ProcessPropertyFactory {

    private final BpmService bpmService;

    public ProcessValueFactory(final BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Override
    public ProcessValue fromStringValue(String stringValue) {
        return bpmService.getActiveBpmProcessDefinitions()
                .stream()
                .filter(p -> p.getId() == Long.parseLong(stringValue))
                .findFirst()
                .map(ProcessValue::new)
                .orElse(null);
    }

    @Override
    public String toStringValue(ProcessValue process) {
        return String.valueOf(process.getId());
    }

    @Override
    public Class<ProcessValue> getValueType() {
        return ProcessValue.class;
    }

    @Override
    public ProcessValue valueFromDatabase(Object object) {
        return this.fromStringValue((String) object);
    }

    @Override
    public Object valueToDatabase(ProcessValue object) {
        return this.toStringValue(object);
    }

    @Override
    public void bind(PreparedStatement statement, int offset, ProcessValue value) throws SQLException {
    }

    @Override
    public void bind(SqlBuilder builder, ProcessValue value) {
    }
}

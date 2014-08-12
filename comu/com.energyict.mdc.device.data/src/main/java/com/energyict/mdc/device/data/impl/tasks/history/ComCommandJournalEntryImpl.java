package com.energyict.mdc.device.data.impl.tasks.history;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.device.data.tasks.history.ComCommandJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.device.data.tasks.history.JournalEntryVisitor;

import javax.inject.Inject;
import java.util.Date;

/**
 * Provides an implementation for the {@link ComCommandJournalEntry} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-08 (10:51)
 */
public class ComCommandJournalEntryImpl extends ComTaskExecutionJournalEntryImpl<ComCommandJournalEntry> implements ComCommandJournalEntry {

    private CompletionCode completionCode;
    private String commandDescription;

    @Inject
    ComCommandJournalEntryImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(ComCommandJournalEntry.class, dataModel, eventService, thesaurus);
    }

//    @Override
//    protected void doLoad (ResultSetIterator resultSet) throws SQLException {
//        super.doLoad(resultSet);
//        this.completionCode = CompletionCode.valueFromDb(resultSet.nextInt());
//        this.commandDescription = resultSet.nextStringFromClob();
//    }
//
//    @Override
//    protected int bindBody (PreparedStatement preparedStatement, int firstParameterNumber) throws SQLException {
//        int parameterNumber = super.bindBody(preparedStatement, firstParameterNumber);
//        preparedStatement.setInt(parameterNumber++, this.completionCode.dbValue());
//        this.bindClob(preparedStatement, this.commandDescription, parameterNumber++);
//        return parameterNumber;
//    }

//    @Override
//    protected ComTaskExecutionJournalEntryFactoryImpl.ComTaskExecutionJournalEntryDiscriminator getDiscriminator () {
//        return ComTaskExecutionJournalEntryFactoryImpl.ComTaskExecutionJournalEntryDiscriminator.COMMAND;
//    }

//    private void validateNew (ComCommandJournalEntryShadow shadow) throws BusinessException {
//        this.validate(shadow);
//    }
//
//    private void validate (ComCommandJournalEntryShadow shadow) throws BusinessException {
//        super.validate(shadow);
//        this.validateNotNull(shadow.getCompletionCode(), "comtaskjournalentry.completioncode");
//        this.validateNotNull(shadow.getCommandDescription(), "comtaskjournalentry.commanddescription");
//    }

    @Override
    public CompletionCode getCompletionCode () {
        return completionCode;
    }

    @Override
    public String getCommandDescription () {
        return this.commandDescription;
    }

    @Override
    protected void doDelete() {
        //TODO automatically generated method body, provide implementation.

    }

    @Override
    protected void validateDelete() {
        //TODO automatically generated method body, provide implementation.

    }

    @Override
    public void accept(JournalEntryVisitor visitor) {
        visitor.visit(this);
    }

    public static ComCommandJournalEntryImpl from(DataModel dataModel, ComTaskExecutionSession comTaskExecutionSession, Date timestamp, CompletionCode completionCode, String errorDescription, String commandDescription) {
        ComCommandJournalEntryImpl instance = dataModel.getInstance(ComCommandJournalEntryImpl.class);
        return instance.init(comTaskExecutionSession, timestamp, completionCode, errorDescription, commandDescription);
    }

    private ComCommandJournalEntryImpl init(ComTaskExecutionSession comTaskExecutionSession, Date timestamp, CompletionCode completionCode, String errorDescription, String commandDescription) {
        this.comTaskExecutionSession.set(comTaskExecutionSession);
        this.completionCode = completionCode;
        this.errorDescription = errorDescription;
        this.timestamp = new UtcInstant(timestamp);
        this.commandDescription = commandDescription;
        return this;
    }
}
package com.energyict.mdc.device.data.impl.tasks.history;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionJournalEntry;

import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

/**
 * Provides an implementation for the {@link ComSessionJournalEntry} interface.
 *
 * @author Rudi Vankeirsbilck (rvk)
 * @since 2012-07-27 (17:16)
 */
public class ComSessionJournalEntryImpl extends PersistentIdObject<ComSessionJournalEntry> implements ComSessionJournalEntry {

    private Reference<ComSession> comSession = ValueReference.absent();
    private UtcInstant timestamp;
    private String message;
    private String stackTrace;
    private Date modDate;

    @Inject
    ComSessionJournalEntryImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(ComSessionJournalEntry.class, dataModel, eventService, thesaurus);
    }

//    protected void doLoad (ResultSet resultSet) throws SQLException {
//        this.doLoad(ResultSetIterator.newForPersistentIdObject(resultSet));
//    }
//
//    protected void doLoad (ResultSetIterator resultSet) throws SQLException {
//        this.comSessionId = resultSet.nextInt();
//        this.timestamp = this.fromMilliseconds(resultSet.nextLong());
//        this.message = resultSet.nextString();
//        this.stackTrace = resultSet.nextStringFromClob();
//    }

//    protected void doInit (ComSession comSession, ComSessionJournalEntryShadow shadow) throws SQLException, BusinessException {
//        this.ownedBy(comSession);
//        this.validateNew(shadow);
//        this.copyNew(shadow);
//        this.postNew();
//        this.created();
//    }

//    private void validateNew (ComSessionJournalEntryShadow shadow) throws BusinessException {
//        this.validate(shadow);
//    }
//
//    private void validate (ComSessionJournalEntryShadow shadow) throws BusinessException {
//        this.validateNotNull(shadow.getMessage(), "comsessionjournalentry.message");
//        this.validateNotNull(shadow.getTimestamp(), "comsessionjournalentry.timestamp");
//    }

//    @Override
//    protected int bindBody (PreparedStatement preparedStatement, int firstParameterNumber) throws SQLException {
//        int parameterNumber = firstParameterNumber;
//        preparedStatement.setInt(parameterNumber++, this.comSessionId);
//        preparedStatement.setLong(parameterNumber++, this.asMilliseconds(this.timestamp));
//        preparedStatement.setString(parameterNumber++, this.message);
//        if (this.stackTrace == null) {
//            preparedStatement.setString(parameterNumber++, null);
//        }
//        else {
//            StringReader reader = new StringReader(this.stackTrace);
//            preparedStatement.setCharacterStream(parameterNumber++, reader, this.stackTrace.length());
//        }
//        return parameterNumber;
//    }

//    private void validateNotNull (Object propertyValue, String propertyName) throws InvalidValueException {
//        if (propertyValue == null) {
//            throw new InvalidValueException("XcannotBeEmpty", "\"{0}\" is a required property", propertyName);
//        }
//    }

    /**
     * Marks this ComSessionJournalEntryImpl as being owned (and managed)
     * by the specified ComSession.
     *
     * @param comSession The ComSession
     */
    protected void ownedBy (ComSession comSession) {
        this.comSession.set(comSession);
    }

    @Override
    public ComSession getComSession () {
        return comSession.get();
    }

    @Override
    public Date getTimestamp () {
        return timestamp.toDate();
    }

    @Override
    public String getMessage () {
        return message;
    }

    @Override
    public String getStackTrace () {
        return this.stackTrace;
    }

    @Override
    protected void doDelete() {
        //TODO automatically generated method body, provide implementation.

    }

    @Override
    protected void validateDelete() {
        //TODO automatically generated method body, provide implementation.

    }

    public static ComSessionJournalEntryImpl from(DataModel dataModel, ComSessionImpl comSession, Date timestamp, String message, Throwable cause) {
        ComSessionJournalEntryImpl entry = dataModel.getInstance(ComSessionJournalEntryImpl.class);
        return entry.init(comSession, timestamp, message, cause);
    }

    private ComSessionJournalEntryImpl init(ComSessionImpl comSession, Date timestamp, String message, Throwable cause) {
        this.comSession.set(comSession);
        this.timestamp = new UtcInstant(timestamp);
        this.message = message;
        this.stackTrace = StackTracePrinter.print(cause);
        return this;
    }

    /**
     * Provides printing services for stack traces of exceptions
     * that cause journal entries to be created.
     *
     * @author Rudi Vankeirsbilck (rudi)
     * @since 2012-08-07 (17:20)
     */
    private enum StackTracePrinter {
        ;

        /**
         * Prints the stacktrace for the Throwable.
         *
         * @param thrown The Throwable (can be <code>null</code>).
         * @return The stacktrace or <code>null</code> if there was not Throwable.
         */
        public static String print (Throwable thrown) {
            if (thrown == null) {
                return null;
            }
            else {
                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                thrown.printStackTrace(printWriter);
                return writer.toString();
            }
        }

    }
}
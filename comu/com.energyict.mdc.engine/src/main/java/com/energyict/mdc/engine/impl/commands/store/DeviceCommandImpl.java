package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.comserver.logging.CanProvideDescriptionTitle;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilderImpl;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

/**
 * Serves as the root for all components that intend to implement
 * the {@link DeviceCommand} interface and will provide
 * code reuse opportunities.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-23 (09:10)
 */
public abstract class DeviceCommandImpl implements DeviceCommand, CanProvideDescriptionTitle {

    private ExecutionLogger logger;

    public DeviceCommandImpl() {
        super();
    }

    @Override
    public final void execute (ComServerDAO comServerDAO) {
        try {
        	this.doExecute(comServerDAO);
        }
        finally {
            if (this.getExecutionLogger() != null) {
                this.getExecutionLogger().executed(this);
            }
        }
    }

    protected abstract void doExecute (ComServerDAO comServerDAO);

    protected IssueService getIssueService() {
        return ServiceProvider.instance.get().issueService();
    }

    protected Clock getClock() {
        return ServiceProvider.instance.get().clock();
    }

    protected DeviceDataService getDeviceDataService(){
        return ServiceProvider.instance.get().deviceDataService();
    }

    protected MdcReadingTypeUtilService getMdcReadingTypeUtilService(){
        return ServiceProvider.instance.get().mdcReadingTypeUtilService();
    }

    protected EngineService getEngineService(){
        return ServiceProvider.instance.get().engineService();
    }

    @Override
    public void executeDuringShutdown (ComServerDAO comServerDAO) {
        /* Default is NOT to execute during shutdown
         * Really urgent subclasses will override this
         * and call the execute method instead. */
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel () {
        return ComServer.LogLevel.DEBUG;
    }

    @Override
    public void logExecutionWith (ExecutionLogger logger) {
        this.logger = logger;
    }

    protected ExecutionLogger getExecutionLogger () {
        return this.logger;
    }

    @Override
    public String getDescriptionTitle() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toJournalMessageDescription(ComServer.LogLevel serverLogLevel) {
        DescriptionBuilder builder = new DescriptionBuilderImpl(this);
        toJournalMessageDescription(builder, serverLogLevel);
        return builder.toString();
    }

    protected abstract void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel);

    /**
     * Tests if the specified server log level enables details of the
     * minimum level to be shown in journal messages.
     *
     * @param serverLogLevel The server LogLevel
     * @param minimumLevel   The minimum level that is required for a message to show up in journaling
     * @return A flag that indicates if message details of the minimum level should show up in journaling
     */
    protected boolean isJournalingLevelEnabled(ComServer.LogLevel serverLogLevel, ComServer.LogLevel minimumLevel) {
        return serverLogLevel.compareTo(minimumLevel) >= 0;
    }
}
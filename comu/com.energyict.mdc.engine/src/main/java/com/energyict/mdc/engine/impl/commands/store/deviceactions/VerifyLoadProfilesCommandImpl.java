package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.VerifyLoadProfilesCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.impl.ProblemImpl;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.exceptions.DeviceConfigurationException;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A Command that will read the Configuration of the LoadProfiles of the device and verify them against the Configuration
 * in the Database. If they do not match, the corresponding reader will be deleted from the
 * {@link LoadProfileCommandImpl#getLoadProfileReaders() loadProfileReader-List} and a
 * proper {@link ProblemImpl} will be created.
 *
 * @author gna
 * @since 21/05/12 - 15:39
 */
public class VerifyLoadProfilesCommandImpl extends SimpleComCommand implements VerifyLoadProfilesCommand {

    /**
     * The initial {@link LoadProfileCommandImpl Command} which needs verification before reading all data
     */
    private final LoadProfileCommand loadProfileCommand;

    /**
     * Boolean indicating whether or not data should be fetched when the profile configuration is invalid
     */
    private final boolean failIfConfigurationMisMatch;

    /**
     * The list of configurations which are read from the device(s)
     */
    private List<CollectedLoadProfileConfiguration> loadProfileConfigurations = new ArrayList<>(0);

    /**
     * The list of {@link LoadProfileReader loadProfileReaders} to remove from the
     * {@link LoadProfileCommandImpl#getLoadProfileReaders()} List
     */
    private List<LoadProfileReader> readersToRemove;


    public VerifyLoadProfilesCommandImpl(final LoadProfileCommand loadProfileCommand, final CommandRoot commandRoot) {
        super(commandRoot);
        this.loadProfileCommand = loadProfileCommand;
        this.failIfConfigurationMisMatch = loadProfileCommand.getLoadProfilesTask().failIfLoadProfileConfigurationMisMatch();
        this.readersToRemove = new ArrayList<>();
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.VERIFY_LOAD_PROFILE_COMMAND;
    }

    @Override
    protected LogLevel defaultJournalingLogLevel () {
        return LogLevel.DEBUG;
    }

    @Override
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        PropertyDescriptionBuilder loadProfileConfigurationsBuilder = builder.addListProperty("loadProfileObisCodes");
        for (CollectedLoadProfileConfiguration loadProfileConfig : loadProfileConfigurations) {
            loadProfileConfigurationsBuilder = loadProfileConfigurationsBuilder.append(loadProfileConfig.getObisCode()).next();
        }
    }

    @Override
    public void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        setLoadProfileConfigurations(deviceProtocol.fetchLoadProfileConfiguration(loadProfileCommand.getLoadProfileReaders()));
        verifyConfigurations();
    }

    /**
     * Verify all configuration read from the device with the configuration in EIServer.
     */
    private void verifyConfigurations() {
        this.readersToRemove = new ArrayList<>();
        if(getLoadProfileConfigurations() != null && !getLoadProfileConfigurations().isEmpty()){
            for (CollectedLoadProfileConfiguration loadProfileConfiguration : getLoadProfileConfigurations()) {
                LoadProfileReader loadProfileReader = getLoadProfileReaderForGivenLoadProfileConfiguration(loadProfileConfiguration);
                if (loadProfileReader != null) {
                    if (!loadProfileConfiguration.isSupportedByMeter()) {
                        readersToRemove.add(loadProfileReader);
                        if (!loadProfileConfiguration.getIssues().isEmpty()){
                            for (Issue issue : loadProfileConfiguration.getIssues()) {
                                addIssue(issue, CompletionCode.forResultType(loadProfileConfiguration.getResultType()));
                            }
                        } else {
                            addIssue(
                                    getIssueService().newProblem(
                                            loadProfileConfiguration.getObisCode(),
// Todo: Move CommonExceptionReferences to MessageSeeds
// Environment.DEFAULT.get().getTranslation("unknownclocktasktype").replaceAll("'", "''"),
                                            "Loadprofile {0} is not supported by the device.",
                                            loadProfileConfiguration.getObisCode()),
                                    CompletionCode.ConfigurationWarning);
                        }
                    } else {
                        verifyNumberOfChannels(loadProfileReader, loadProfileConfiguration);
                        verifyProfileInterval(loadProfileReader, loadProfileConfiguration);
                        verifyChannelConfiguration(loadProfileReader, loadProfileConfiguration);
                    }
                }
            }
        } else {
            this.readersToRemove.addAll(loadProfileCommand.getLoadProfileReaders());
        }
        this.loadProfileCommand.removeIncorrectLoadProfileReaders(readersToRemove);
    }

    /**
     * Verify that the configuration of the channels match. If the baseUnit is the same, then we can store the data, otherwise we should not even fetch the data
     *
     * @param loadProfileReader        the given reader
     * @param loadProfileConfiguration the given configuration
     */
    protected void verifyChannelConfiguration(final LoadProfileReader loadProfileReader, final CollectedLoadProfileConfiguration loadProfileConfiguration) {
        for (ChannelInfo channelInfo : loadProfileReader.getChannelInfos()) {
            verifyLocalChannelConfiguration(loadProfileReader, loadProfileConfiguration, channelInfo);
        }
    }

    private void verifyLocalChannelConfiguration(LoadProfileReader loadProfileReader, CollectedLoadProfileConfiguration loadProfileConfiguration, ChannelInfo localChannelInfo) {
        ObisCode loadProfileConfigurationObisCode = loadProfileConfiguration.getObisCode();
        for (ChannelInfo meterChannelInfo : loadProfileConfiguration.getChannelInfos()) {
            try {
                if (match(localChannelInfo, meterChannelInfo)) {
                    if (shouldRemove(localChannelInfo, meterChannelInfo)) {
                        readersToRemove.add(loadProfileReader);
                        addIssue(
                                getIssueService().newProblem(
                                        loadProfileConfigurationObisCode,
// Todo: Move CommonExceptionReferences to MessageSeeds
// Environment.DEFAULT.get().getTranslation("loadprofileobiscodeXincorrectchannelunits").replaceAll("'", "''"),
                                        "Channel unit mismatch: load profile in the meter with OBIS code '{0}' has a channel ({1}) with the unit '{2}', whilst the configured unit for that channel is '{3}'",
                                        loadProfileConfigurationObisCode,
                                        meterChannelInfo.getChannelObisCode(),
                                        meterChannelInfo.getUnit(),
                                        localChannelInfo.getUnit()),
                                CompletionCode.ConfigurationError);
                    }
                }
            } catch (IOException e) {
                throw DeviceConfigurationException.channelNameNotAnObisCode(meterChannelInfo.getName());
            }
        }
    }

    private boolean match(ChannelInfo localChannelInfo, ChannelInfo meterChannelInfo) throws IOException {
        return meterChannelInfo.getChannelObisCode().equalsIgnoreBChannel(localChannelInfo.getChannelObisCode())
                && meterChannelInfo.getMeterIdentifier().equalsIgnoreCase(localChannelInfo.getMeterIdentifier());
    }

    private boolean shouldRemove(ChannelInfo localChannelInfo, ChannelInfo meterChannelInfo) {
        return !(meterChannelInfo.getUnit().isUndefined() || localChannelInfo.getUnit().isUndefined())
                && !meterChannelInfo.getUnit().getBaseUnit().equals(localChannelInfo.getUnit().getBaseUnit());
    }

    /**
     * Verify if the interval of the {@link LoadProfileReader} is equal to the interval which is read from the device.
     * If the interval of the device is '0', then the capture period is asynchronous, for ex. monthly, in this case we don't fail.
     *
     * @param loadProfileReader        the given reader
     * @param loadProfileConfiguration the given configuration
     */
    protected void verifyProfileInterval(final LoadProfileReader loadProfileReader, final CollectedLoadProfileConfiguration loadProfileConfiguration) {
        int loadProfileInterval = this.loadProfileCommand.findLoadProfileIntervalForLoadProfileReader(loadProfileReader);
        if (loadProfileConfiguration.getProfileInterval() != 0 && loadProfileConfiguration.getProfileInterval() != loadProfileInterval) {
            readersToRemove.add(loadProfileReader);
            addIssue(
                    getIssueService().newProblem(
                            loadProfileConfiguration.getObisCode(),
// Todo: Move CommonExceptionReferences to MessageSeeds
// Environment.DEFAULT.get().getTranslation("loadprofileobiscodeXincorrectinterval").replaceAll("'", "''"),
                            "Load profile interval mismatch; load profile with OBIS code '{0}' has a {1} second(s) interval on the device, while {2} second(s) is configured in eiMaster",
                            loadProfileConfiguration.getObisCode(),
                            loadProfileConfiguration.getProfileInterval(),
                            loadProfileInterval),
                    CompletionCode.ConfigurationError);
        }
    }

    /**
     * Verify if the number of channels from the {@link LoadProfileReader} are equal to the number of channels from the DeviceLoadProfileConfiguration
     *
     * @param loadProfileReader        the given reader
     * @param loadProfileConfiguration the given configuration
     */
    protected void verifyNumberOfChannels(final LoadProfileReader loadProfileReader, final CollectedLoadProfileConfiguration loadProfileConfiguration) {
        if (failIfLoadProfileConfigurationMisMatch()) {
            if (loadProfileReader.getChannelInfos().size() != loadProfileConfiguration.getNumberOfChannels()) {
                readersToRemove.add(loadProfileReader);
                addIssue(
                        getIssueService().newProblem(
                                loadProfileConfiguration.getObisCode(),
// Todo: Move CommonExceptionReferences to MessageSeeds
// Environment.DEFAULT.get().getTranslation("loadProfileObisCodeXIncorrectChannelNumbers").replaceAll("'", "''"),
                                "Number of channels mismatch; load profile with OBIS code '{0}' has {1} channel(s) on the device, while there are {2} channel(s) in eiMaster",
                                loadProfileConfiguration.getObisCode(),
                                loadProfileReader.getChannelInfos().size(),
                                loadProfileConfiguration.getNumberOfChannels()),
                        CompletionCode.ConfigurationError);
            }
        }
    }

    /**
     * Get the {@link LoadProfileReader} from the {@link LoadProfileCommandImpl LoadProfileCommand} which was created for the given
     * DeviceLoadProfileConfiguration
     *
     * @param loadProfileConfiguration the given DeviceLoadProfileConfiguration
     * @return the requested LoadProfileReader or null if the reader was not found
     */
    protected LoadProfileReader getLoadProfileReaderForGivenLoadProfileConfiguration(final CollectedLoadProfileConfiguration loadProfileConfiguration) {
        for (LoadProfileReader loadProfileReader : loadProfileCommand.getLoadProfileReaders()) {
            if (loadProfileReader.getProfileObisCode().equalsIgnoreBChannel(loadProfileConfiguration.getObisCode()) &&
                    loadProfileReader.getDeviceIdentifier().getIdentifier().equals(loadProfileConfiguration.getDeviceIdentifier().getIdentifier())) {
                return loadProfileReader;
            }
        }
        return null;
    }

    private void setLoadProfileConfigurations(final List<CollectedLoadProfileConfiguration> loadProfileConfigurations) {
        if (loadProfileConfigurations == null) {
            this.loadProfileConfigurations = new ArrayList<>(0);
        }
        else {
            this.loadProfileConfigurations = loadProfileConfigurations;
        }
    }

    private List<CollectedLoadProfileConfiguration> getLoadProfileConfigurations() {
        return loadProfileConfigurations;
    }

    protected List<LoadProfileReader> getReadersToRemove() {
        return readersToRemove;
    }

    /**
     * Returns true if a LoadProfile should <b>NOT</b> be read if his configuration does not match,
     * false if we should fetch the data even if the configuration is invalid.
     *
     * @return the indication whether to fail if the configuration does not match
     */
    public boolean failIfLoadProfileConfigurationMisMatch() {
        return failIfConfigurationMisMatch;
    }
}

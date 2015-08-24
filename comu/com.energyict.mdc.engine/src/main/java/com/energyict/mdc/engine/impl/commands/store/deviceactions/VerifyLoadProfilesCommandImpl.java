package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.VerifyLoadProfilesCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.engine.impl.meterdata.DeviceLoadProfile;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.issues.impl.ProblemImpl;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * The initial {@link LoadProfileCommandImpl Command} which needs verification before reading all data.
     */
    private final LoadProfileCommand loadProfileCommand;

    /**
     * Boolean indicating whether or not data should be fetched when the profile configuration is invalid.
     */
    private final boolean failIfConfigurationMisMatch;

    /**
     * The list of configurations which are read from the device(s).
     */
    private List<CollectedLoadProfileConfiguration> loadProfileConfigurations = new ArrayList<>(0);

    /**
     * The list of {@link LoadProfileReader loadProfileReaders} to remove from the
     * {@link LoadProfileCommandImpl#getLoadProfileReaders()} List.
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
    public String getDescriptionTitle() {
        return "Read out and verify the load profile configuration";
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
        if (getLoadProfileConfigurations() != null && !getLoadProfileConfigurations().isEmpty()) {
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
                                            this.getThesaurus(),
                                            MessageSeeds.UNSUPPORTED_LOAD_PROFILE,
                                            loadProfileConfiguration.getObisCode()),
                                    CompletionCode.ConfigurationWarning);
                        }
                    } else {
                        List<Issue> issues = new ArrayList<>();
                        issues.addAll(verifyNumberOfChannels(loadProfileReader, loadProfileConfiguration));
                        issues.addAll(verifyProfileInterval(loadProfileReader, loadProfileConfiguration));
                        issues.addAll(verifyChannelConfiguration(loadProfileReader, loadProfileConfiguration));
                        if (!issues.isEmpty()) {
                            createAndAddFailedCollectedLoadProfile(loadProfileReader, ResultType.ConfigurationError, issues);
                        }
                    }
                }
            }
        } else {
            this.readersToRemove.addAll(loadProfileCommand.getLoadProfileReaders());
            for (LoadProfileReader loadProfileReader : loadProfileCommand.getLoadProfileReaders()) {
                createAndAddFailedCollectedLoadProfile(loadProfileReader, ResultType.NotSupported, loadProfileNotSupportedIssue(loadProfileReader.getProfileObisCode()));
            }
        }
        this.loadProfileCommand.removeIncorrectLoadProfileReaders(readersToRemove);
    }

    private Issue loadProfileNotSupportedIssue(ObisCode profileObisCode) {
        return getIssueService().newProblem(profileObisCode, "loadProfileXnotsupported", profileObisCode, CompletionCode.ConfigurationWarning);
    }

    private void createAndAddFailedCollectedLoadProfile(LoadProfileReader loadProfileReader, ResultType resultType, Issue issue) {
        this.createAndAddFailedCollectedLoadProfile(loadProfileReader, resultType, Arrays.asList(issue));
    }

    private void createAndAddFailedCollectedLoadProfile(LoadProfileReader loadProfileReader, ResultType resultType, List<Issue> issues) {
        DeviceLoadProfile collectedLoadProfile = createFailedCollectedLoadProfile(
                loadProfileReader,
                issues,
                resultType);
        this.addCollectedDataItem(collectedLoadProfile);
    }

    private DeviceLoadProfile createFailedCollectedLoadProfile(LoadProfileReader loadProfileReader, List<Issue> issues, ResultType resultType) {
        LoadProfileIdentifier loadProfileIdentifier = this.getCommandRoot()
                .getServiceProvider()
                .identificationService()
                .createLoadProfileIdentifierByDatabaseId(loadProfileReader.getLoadProfileId());
        DeviceLoadProfile collectedLoadProfile = new DeviceLoadProfile(loadProfileIdentifier);
        collectedLoadProfile.setFailureInformation(resultType, issues);
        return collectedLoadProfile;
    }

    /**
     * Verify that the configuration of the channels match. If the baseUnit is the same, then we can store the data, otherwise we should not even fetch the data
     *
     * @param loadProfileReader        the given reader
     * @param loadProfileConfiguration the given configuration
     */
    protected List<Issue> verifyChannelConfiguration(final LoadProfileReader loadProfileReader, final CollectedLoadProfileConfiguration loadProfileConfiguration) {
        List<Issue> issues = new ArrayList<>();
        for (ChannelInfo channelInfo : loadProfileReader.getChannelInfos()) {
            issues.addAll(verifyLocalChannelConfiguration(loadProfileConfiguration, channelInfo));
        }
        if (!issues.isEmpty()) {
            addLoadProfileReaderToTheListOfReadersToRemove(loadProfileReader);
        }
        return issues;
    }

    private List<Issue> verifyLocalChannelConfiguration(CollectedLoadProfileConfiguration loadProfileConfiguration, ChannelInfo localChannelInfo) {
        List<Issue> issues = new ArrayList<>();
        ObisCode loadProfileConfigurationObisCode = loadProfileConfiguration.getObisCode();
        Optional<Problem> incorrectChannelUnitProblem = Optional.empty();
        for (ChannelInfo meterChannelInfo : loadProfileConfiguration.getChannelInfos()) {
            if (match(localChannelInfo, meterChannelInfo)) {
                if (unitMismatch(localChannelInfo, meterChannelInfo)) {
                    /* Do not add problem right away, because we can may have multiple channels with the same obiscode but with different units.
                     * Instead we should continue the loop and check if one of the other channels has a perfect match (for both obis and unit);
                     * If a perfect match is found later on, then validation should not fail. */
                    incorrectChannelUnitProblem = Optional.of(
                            getIssueService().newProblem(
                                    loadProfileConfigurationObisCode,
                                    this.getThesaurus(),
                                    MessageSeeds.CHANNEL_UNIT_MISMATCH,
                                    loadProfileConfigurationObisCode,
                                    meterChannelInfo.getChannelObisCode(),
                                    meterChannelInfo.getUnit(),
                                    localChannelInfo.getUnit()));
                }
                else {
                    return issues; // Configuration of the channel match, so return
                }
            }
        }
        if (incorrectChannelUnitProblem.isPresent()) { // When configuration of the channel doesn't match
            issues.add(incorrectChannelUnitProblem.get());
        }
        else { // When the channel is missing (load profile in the meter doesn't have the channel, whilst it is configured in eiMaster)
            issues.add(createLoadProfileMissingChannelIssue(loadProfileConfiguration, localChannelInfo, loadProfileConfigurationObisCode));
        }
        return issues;
    }

    private boolean match(ChannelInfo localChannelInfo, ChannelInfo meterChannelInfo) {
        return meterChannelInfo.getChannelObisCode().equalsIgnoreBChannel(localChannelInfo.getChannelObisCode())
                && meterChannelInfo.getMeterIdentifier().equalsIgnoreCase(localChannelInfo.getMeterIdentifier());
    }

    private boolean unitMismatch(ChannelInfo localChannelInfo, ChannelInfo meterChannelInfo) {
        return !(meterChannelInfo.getUnit().isUndefined() || localChannelInfo.getUnit().isUndefined())
                && !meterChannelInfo.getUnit().getBaseUnit().equals(localChannelInfo.getUnit().getBaseUnit());
    }

    private Issue createLoadProfileMissingChannelIssue(CollectedLoadProfileConfiguration loadProfileConfiguration, ChannelInfo localChannelInfo, ObisCode loadProfileConfigurationObisCode) {
        String allDevicesMatchingChannel = composeStringOfAllDevicesMatchingChannelObis(localChannelInfo, loadProfileConfiguration);
        if (allDevicesMatchingChannel.isEmpty()) {
            return getIssueService().newProblem(
                    loadProfileConfigurationObisCode,
                    "loadprofileobiscodeXmissingchannel",
                    loadProfileConfigurationObisCode,
                    localChannelInfo.getName(),
                    localChannelInfo.getMeterIdentifier()
            );
        } else {
            return getIssueService().newProblem(
                    loadProfileConfigurationObisCode,
                    "loadprofileobiscodeXmissingchannelOtherMatch",
                    loadProfileConfigurationObisCode,
                    localChannelInfo.getName(),
                    localChannelInfo.getMeterIdentifier(),
                    allDevicesMatchingChannel
            );
        }
    }

    /**
     * Compose a String containing the serial numbers of all devices
     * that have a channel whose obisCode and unit do match the given localChannelInfo.
     */
    private String composeStringOfAllDevicesMatchingChannelObis(ChannelInfo localChannelInfo, CollectedLoadProfileConfiguration loadProfileConfiguration) {
        return loadProfileConfiguration
                .getChannelInfos()
                .stream()
                .filter(meterChannelInfo -> meterChannelInfo.getChannelObisCode().equalsIgnoreBChannel(localChannelInfo.getChannelObisCode()))
                .filter(meterChannelInfo -> !unitMismatch(meterChannelInfo, localChannelInfo))
                .map(ChannelInfo::getMeterIdentifier)
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private void addLoadProfileReaderToTheListOfReadersToRemove(LoadProfileReader reader) {
        if (!readersToRemove.contains(reader)) { // Only add, if not already in the list or readers to remove
            readersToRemove.add(reader);
        }
    }

    /**
     * Verify if the interval of the {@link LoadProfileReader} is equal to the interval which is read from the device.
     * If the interval of the device is '0', then the capture period is asynchronous, for ex. monthly, in this case we don't fail.
     *
     * @param loadProfileReader        the given reader
     * @param loadProfileConfiguration the given configuration
     */
    protected List<Issue> verifyProfileInterval(final LoadProfileReader loadProfileReader, final CollectedLoadProfileConfiguration loadProfileConfiguration) {
        List<Issue> issues = new ArrayList<>();
        int loadProfileInterval = this.loadProfileCommand.findLoadProfileIntervalForLoadProfileReader(loadProfileReader);
        if (loadProfileConfiguration.getProfileInterval() != 0 && loadProfileConfiguration.getProfileInterval() != loadProfileInterval) {
            addLoadProfileReaderToTheListOfReadersToRemove(loadProfileReader);
            issues.add(
                    getIssueService().newProblem(
                            loadProfileConfiguration.getObisCode(),
                            this.getThesaurus(),
                            MessageSeeds.LOAD_PROFILE_INTERVAL_MISMATCH,
                            loadProfileConfiguration.getObisCode(),
                            loadProfileConfiguration.getProfileInterval(),
                            loadProfileInterval));
        }
        return issues;
    }

    /**
     * Verify that the number of channels from the {@link LoadProfileReader}
     * are equal to the number of channels from the DeviceLoadProfileConfiguration.
     *
     * @param loadProfileReader the LoadProfileReader
     * @param loadProfileConfiguration The CollectedLoadProfileConfiguration
     */
    protected List<Issue> verifyNumberOfChannels(final LoadProfileReader loadProfileReader, final CollectedLoadProfileConfiguration loadProfileConfiguration) {
        List<Issue> issues = new ArrayList<>();
        if (failIfLoadProfileConfigurationMisMatch()) {
            if (loadProfileReader.getChannelInfos().size() != loadProfileConfiguration.getNumberOfChannels()) {
                addLoadProfileReaderToTheListOfReadersToRemove(loadProfileReader);
                issues.add(
                        getIssueService().newProblem(
                                loadProfileConfiguration.getObisCode(),
                                this.getThesaurus(),
                                MessageSeeds.LOAD_PROFILE_NUMBER_OF_CHANNELS_MISMATCH,
                                loadProfileConfiguration.getObisCode(),
                                loadProfileReader.getChannelInfos().size(),
                                loadProfileConfiguration.getNumberOfChannels()));
            }
        }
        return issues;
    }

    /**
     * Get the {@link LoadProfileReader} from the {@link LoadProfileCommand}
     * which was created for the given DeviceLoadProfileConfiguration.
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
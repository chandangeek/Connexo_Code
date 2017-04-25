Ext.define('Mdc.util.LinkPurpose', {
    alias: 'LinkPurpose',
    requires: 'Mdc.model.Device',
    singleton: true,

    LINK_SLAVE: 1,
    LINK_DATALOGGER_SLAVE: 2,
    LINK_MULTI_ELEMENT_SLAVE: 3,
    properties: {
        1: {
            name: 'LinkSlave',
            value: 1,
            displayValue: Uni.I18n.translate('general.linkSlave', 'MDC', 'Link slave'),
            pageTitle: Uni.I18n.translate('general.slaves', 'MDC', 'Slaves'),
            manageLinkText: Uni.I18n.translate('general.manageSlaves', 'MDC', 'Manage slaves'),
            noItemsFoundText: Uni.I18n.translate('slavesGrid.empty.title', 'MDC', 'No slaves found'),
            noItemsFoundReasons: [Uni.I18n.translate('slavesGrid.empty.reason1', 'MDC', 'No slaves have been linked yet.')],
            menuWizardStep1: Uni.I18n.translate('general.selectSlave', 'MDC', 'Select slave'),
            titleWizardStep1: Uni.I18n.translate('linkwizard.step1.title.linkSlave', 'MDC', 'Step 1: Select a slave'),
            newSlaveOption: Uni.I18n.translate('linkwizard.step1.option.new.slave', 'MDC', 'New slave device'),
            noChannelsMessage: Uni.I18n.translate('general.slave.noChannels', 'MDC', 'There are no channels on the slave.'),
            noRegistersMessage: Uni.I18n.translate('general.slave.noRegisters', 'MDC', 'There are no registers on the slave.'),
            slaveLinkedMessage: Uni.I18n.translate('general.slaveXLinkedToDeviceY.success', 'MDC', "Slave '{0}' has been linked to device '{1}'."),
            slaveLinkedFailedMessage: Uni.I18n.translate('general.slaveXLinkedToDeviceY.noSuccess', 'MDC', "Slave '{0}' has not been linked to device '{1}' due to a failure. Please try again."),
            linkConfirmation: Uni.I18n.translate('general.question.linkSlaveXToDeviceY', 'MDC', "Link slave '{0}' to device '{1}'?"),
            slavesGridDisplayMsg: Uni.I18n.translate('slaves.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} slaves'),
            slavesGridDisplayMoreMsg: Uni.I18n.translate('slaves.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} slaves'),
            slavesGridEmptyMsg: Uni.I18n.translate('slaves.pagingtoolbartop.emptyMsg', 'MDC', 'There are no slaves to display'),
            slavesGridItemsPerPageMsg: Uni.I18n.translate('slaves.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Slaves per page'),
            channelGridSlaveColumn: Uni.I18n.translate('general.slave', 'MDC', 'Slave'),
            dataLoggerSlaveHistoryTitle: Uni.I18n.translate('slaveHistory.title', 'MDC', 'Slave history'),
            deviceTypeFilter: function (deviceType) {
                return (deviceType.get('deviceTypePurpose') === 'DATALOGGER_SLAVE' || deviceType.get('deviceTypePurpose') === 'MULTI_ELEMENT_SLAVE') && deviceType.get('activeDeviceConfigurationCount') > 0;
            }
        },
        2: {
            name: 'LinkDataLoggerSlave',
            value: 2,
            displayValue: Uni.I18n.translate('general.linkDataLoggerSlave', 'MDC', 'Link data logger slave'),
            pageTitle: Uni.I18n.translate('general.dataLoggerSlaves', 'MDC', 'Data logger slaves'),
            manageLinkText: Uni.I18n.translate('general.manageDataLoggerSlaves', 'MDC', 'Manage data logger slaves'),
            noItemsFoundText: Uni.I18n.translate('dataLoggerSlavesGrid.empty.title', 'MDC', 'No data logger slaves found'),
            noItemsFoundReasons: [Uni.I18n.translate('dataLoggerSlavesGrid.empty.reason1', 'MDC', 'No data logger slaves have been linked yet.')],
            menuWizardStep1: Uni.I18n.translate('general.selectDataLoggerSlave', 'MDC', 'Select data logger slave'),
            titleWizardStep1: Uni.I18n.translate('linkwizard.step1.title', 'MDC', 'Step 1: Select data logger slave'),
            newSlaveOption: Uni.I18n.translate('linkwizard.step1.option.new.datalogger', 'MDC', 'New datalogger slave device'),
            noChannelsMessage: Uni.I18n.translate('general.dataLoggerSlave.noChannels', 'MDC', 'There are no channels on the data logger slave.'),
            noRegistersMessage: Uni.I18n.translate('general.dataLoggerSlave.noRegisters', 'MDC', 'There are no registers on the data logger slave.'),
            slaveLinkedMessage: Uni.I18n.translate('general.slaveXLinkedToDataLoggerY.success', 'MDC', "Slave '{0}' has been linked to data logger '{1}'."),
            slaveLinkedFailedMessage: Uni.I18n.translate('general.slaveXLinkedToDataLoggerY.noSuccess', 'MDC', "Slave '{0}' has not been linked to data logger '{1}' due to a failure. Please try again."),
            linkConfirmation: Uni.I18n.translate('general.question.linkSlaveXToDataLoggerY', 'MDC', "Link slave '{0}' to data logger '{1}'?"),
            slavesGridDisplayMsg: Uni.I18n.translate('dataLoggerSlaves.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} data logger slaves'),
            slavesGridDisplayMoreMsg: Uni.I18n.translate('dataLoggerSlaves.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} data logger slaves'),
            slavesGridEmptyMsg: Uni.I18n.translate('dataLoggerSlaves.pagingtoolbartop.emptyMsg', 'MDC', 'There are no data logger slaves to display'),
            slavesGridItemsPerPageMsg: Uni.I18n.translate('dataLoggerSlaves.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Data logger slaves per page'),
            channelGridSlaveColumn: Uni.I18n.translate('general.dataLoggerSlave', 'MDC', 'Data logger slave'),
            dataLoggerSlaveHistoryTitle: Uni.I18n.translate('dataLoggerSlaveHistory.title', 'MDC', 'Data logger slave history'),
            deviceTypeFilter: function (deviceType) {
                return deviceType.get('deviceTypePurpose') === 'DATALOGGER_SLAVE' && deviceType.get('activeDeviceConfigurationCount') > 0;
            }
        },
        3: {
            name: 'LinkMultiElementSlave',
            value: 3,
            displayValue: Uni.I18n.translate('general.linkMultiElementSlave', 'MDC', 'Link multi-element slave'),
            pageTitle: Uni.I18n.translate('general.multiElementSlaves', 'MDC', 'Multi-element slaves'),
            manageLinkText: Uni.I18n.translate('general.manageMultiElementSlaves', 'MDC', 'Manage multi-element slaves'),
            noItemsFoundText: Uni.I18n.translate('multiElementSlavesGrid.empty.title', 'MDC', 'No multi-element slaves found'),
            noItemsFoundReasons: [Uni.I18n.translate('multiElementSlavesGrid.empty.reason1', 'MDC', 'No multi-element slaves have been linked yet.')],
            menuWizardStep1: Uni.I18n.translate('general.selectMultiElementSlave', 'MDC', 'Add multi-element slave'),
            titleWizardStep1: Uni.I18n.translate('linkwizard.step1.title.linkMultiElementSlave', 'MDC', 'Step 1: Add multi-element slave'),
            noChannelsMessage: Uni.I18n.translate('general.multiElementSlave.noChannels', 'MDC', 'There are no channels on the multi-element slave.'),
            noRegistersMessage: Uni.I18n.translate('general.multiElementSlave.noRegisters', 'MDC', 'There are no registers on the multi-element slave.'),
            slaveLinkedMessage: Uni.I18n.translate('general.slaveXLinkedToMultiElementDeviceY.success', 'MDC', "Slave '{0}' has been linked to multi-element device '{1}'."),
            slaveLinkedFailedMessage: Uni.I18n.translate('general.slaveXLinkedToMultiElementDeviceY.noSuccess', 'MDC', "Slave '{0}' has not been linked to multi-element device '{1}' due to a failure. Please try again."),
            linkConfirmation: Uni.I18n.translate('general.question.linkSlaveXToMultiElementDeviceY', 'MDC', "Link slave '{0}' to multi-element device '{1}'?"),
            slavesGridDisplayMsg: Uni.I18n.translate('multiElementSlaves.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} multi-element slaves'),
            slavesGridDisplayMoreMsg: Uni.I18n.translate('multiElementSlaves.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} multi-element slaves'),
            slavesGridEmptyMsg: Uni.I18n.translate('multiElementSlaves.pagingtoolbartop.emptyMsg', 'MDC', 'There are no multi-element slaves to display'),
            slavesGridItemsPerPageMsg: Uni.I18n.translate('multiElementSlaves.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Multi-element slaves per page'),
            newSlaveOption: '',
            channelGridSlaveColumn: Uni.I18n.translate('general.multiElementSlave', 'MDC', 'Multi-element slave'),
            dataLoggerSlaveHistoryTitle: Uni.I18n.translate('multiElementSlaveHistory.title', 'MDC', 'Multi-element slave history'),
            deviceTypeFilter: function (deviceType) {
                return deviceType.get('deviceTypePurpose') === 'MULTI_ELEMENT_SLAVE' && deviceType.get('activeDeviceConfigurationCount') > 0;
            }
        }
    },
    forDevice: function (device) {
        if (device.get('isDataLogger') && device.get('isMultiElementDevice')) {
            return this.properties[this.LINK_SLAVE];
        } else if (device.get('isDataLogger')) {
            return this.properties[this.LINK_DATALOGGER_SLAVE];
        } else if (device.get('isMultiElementDevice')) {
            return this.properties[this.LINK_MULTI_ELEMENT_SLAVE];
        }
    }
});

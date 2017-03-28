Ext.define('Mdc.util.LinkPurpose', {
    alias:'LinkPurpose',
    requires:'Mdc.model.Device',
    singleton: true,

    LINK_SLAVE: 1,
    LINK_DATALOGGER_SLAVE: 2,
    LINK_MULTI_ELEMENT_SLAVE: 3,
    properties: {
        1:{name: 'LinkSlave',
           value: 1,
           displayValue: Uni.I18n.translate('general.linkSlave', 'MDC', 'Link slave'),
           pageTitle: Uni.I18n.translate('general.slaves', 'MDC', 'Slaves'),
           manageLinkText: Uni.I18n.translate('general.manageSlaves', 'MDC', 'Manage slaves'),
           noItemsFoundText: Uni.I18n.translate('slavesGrid.empty.title', 'MDC', 'No slaves found'),
           noItemsFoundReasons: [Uni.I18n.translate('slavesGrid.empty.reason1', 'MDC', 'No slaves have been linked yet.')],
           menuWizardStep1: Uni.I18n.translate('general.selectSlave', 'MDC', 'Select slave'),
           titleWizardStep1: Uni.I18n.translate('linkwizard.step1.title.linkSlave', 'MDC', 'Step 1: Select a slave')
          },
        2:{name: 'LinkDataLoggerSlave',
           value: 2,
           displayValue: Uni.I18n.translate('general.linkDataLoggerSlave', 'MDC', 'Link data logger slave'),
           pageTitle: Uni.I18n.translate('general.dataLoggerSlaves', 'MDC', 'Data logger slaves'),
           manageLinkText: Uni.I18n.translate('general.manageDataLoggerSlaves', 'MDC', 'Manage data logger slaves'),
           noItemsFoundText: Uni.I18n.translate('dataLoggerSlavesGrid.empty.title', 'MDC', 'No data logger slaves found'),
           noItemsFoundReasons: [Uni.I18n.translate('dataLoggerSlavesGrid.empty.reason1', 'MDC', 'No data logger slaves have been linked yet.')],
           menuWizardStep1: Uni.I18n.translate('general.selectDataLoggerSlave', 'MDC', 'Select data logger slave'),
           titleWizardStep1: Uni.I18n.translate('linkwizard.step1.title', 'MDC', 'Step 1: Select data logger slave')
        },
        3:{name: 'LinkMultiElementSlave',
           value: 3,
           displayValue: Uni.I18n.translate('general.linkMultiElementSlave', 'MDC', 'Link multi-element slave'),
           pageTitle: Uni.I18n.translate('general.multiElementSlaves', 'MDC', 'Multi-element slaves'),
           manageLinkText: Uni.I18n.translate('general.manageMultiElementSlaves', 'MDC', 'Manage multi-element slaves'),
           noItemsFoundText: Uni.I18n.translate('multiElementSlavesGrid.empty.title', 'MDC', 'No multi-element slaves found'),
           noItemsFoundReasons: [Uni.I18n.translate('multiElementSlavesGrid.empty.reason1', 'MDC', 'No multi-element slaves have been linked yet.')],
           menuWizardStep1: Uni.I18n.translate('general.selectMultiElementSlave', 'MDC', 'Add multi-element slave'),
           titleWizardStep1: Uni.I18n.translate('linkwizard.step1.title.linkMultiElementSlave', 'MDC', 'Step 1: Add multi-element slave')
        }
    },
    forDevice: function(device){
        if (device.get('isDataLogger') && device.get('isMultiElementDevice')){
            return this.properties[this.LINK_SLAVE];
        }else if (device.get('isDataLogger')){
            return this.properties[this.LINK_DATALOGGER_SLAVE];
        }else if (device.get('isMultiElementDevice')){
            return this.properties[this.LINK_MULTI_ELEMENT_SLAVE];
        }
    }
});

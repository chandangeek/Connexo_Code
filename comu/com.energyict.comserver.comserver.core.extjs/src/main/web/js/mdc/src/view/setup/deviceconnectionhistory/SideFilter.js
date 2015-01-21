Ext.define('Mdc.view.setup.deviceconnectionhistory.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceconnectionhistorySideFilter',
    itemId: 'deviceconnectionhistorySideFilter',
    ui: 'medium',
    width: 288,
    title: Uni.I18n.translate('deviceconnectionhistory.sideFilter.title', 'DSH', 'Filter'),
    items: {
        xtype: 'form',
        itemId: 'deviceconnectionhistorySideFilterForm',
        ui: 'filter',
        items: [
            {
                xtype: 'checkboxgroup',
                fieldLabel: Uni.I18n.translate('deviceconnectionhistory.logLevel', 'MDC', 'Log level'),
                labelAlign: 'top',
                columns: 1,
                vertical: true,
                defaultType: 'checkboxfield',
                itemId: 'logLevelField',
                items: [
                    {
                        itemId: 'error',
                        inputValue: 'Error',
                        name: 'logLevels',
                        boxLabel: Uni.I18n.translate('deviceconnectionhistory.error', 'MDC', 'Error')
                    },
                    {
                        itemId: 'warning',
                        inputValue: 'Warning',
                        name: 'logLevels',
                        boxLabel: Uni.I18n.translate('deviceconnectionhistory.warning', 'MDC', 'Warning')
                    },
                    {
                        itemId: 'information',
                        inputValue: 'Information',
                        name: 'logLevels',
                        boxLabel: Uni.I18n.translate('deviceconnectionhistory.information', 'MDC', 'Information')
                    },
                    {
                        itemId: 'debug',
                        inputValue: 'Debug',
                        name: 'logLevels',
                        boxLabel: Uni.I18n.translate('deviceconnectionhistory.debug', 'MDC', 'Debug')
                    },
                    {
                        itemId: 'trace',
                        inputValue: 'Trace',
                        name: 'logLevels',
                        boxLabel: Uni.I18n.translate('deviceconnectionhistory.trace', 'MDC', 'Trace')
                    }
                ]
            },
            {
                xtype: 'checkboxgroup',
                fieldLabel: Uni.I18n.translate('deviceconnectionhistory.logType', 'MDC', 'Log Type'),
                labelAlign: 'top',
                defaultType: 'checkboxfield',
                columns: 1,
                vertical: true,
                itemId: 'logTypeField',
                items: [
                    {
                        itemId: 'connection',
                        inputValue: 'Connections',
                        name: 'logTypes',
                        boxLabel: Uni.I18n.translate('deviceconnectionhistory.connection', 'MDC', 'Connections')
                    },
                    {
                        itemId: 'communications',
                        inputValue: 'Communications',
                        name: 'logTypes',
                        boxLabel: Uni.I18n.translate('deviceconnectionhistory.communicationTask', 'MDC', 'Communications')
                    }
                ]
            }

        ],
        dockedItems: [
            {
                xtype: 'toolbar',
                dock: 'bottom',
                items: [
                    {
                        itemId: 'deviceLoadProfileDataFilterApplyBtn',
                        ui: 'action',
                        text: Uni.I18n.translate('general.apply', 'MDC', 'Apply'),
                        action: 'applyfilter'
                    },
                    {
                        itemId: 'deviceLoadProfileDataFilterResetBtn',
                        text: Uni.I18n.translate('general.clearAll', 'MDC', 'Clear all'),
                        action: 'resetfilter'
                    }
                ]
            }
        ]
    }
});

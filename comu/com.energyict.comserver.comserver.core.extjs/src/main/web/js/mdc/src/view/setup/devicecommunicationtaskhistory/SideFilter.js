Ext.define('Mdc.view.setup.devicecommunicationtaskhistory.SideFilter', {
    extend: 'Uni.view.navigation.SubMenu',
    requires: [
    ],
    alias: 'widget.deviceCommunicationTaskHistorySideFilter',
    itemId: 'deviceCommunicationTaskHistorySideFilter',
    cls: 'filter-form',
    title: Uni.I18n.translate('devicecommunicationtaskhistory.sideFilter.title', 'DSH', 'Filter'),
    items: {
        xtype: 'form',
        itemId: 'devicecommunicationtaskhistorySideFilterForm',
        ui: 'filter',
        items: [
            {
                xtype: 'checkboxgroup',
                fieldLabel: Uni.I18n.translate('devicecommunicationtaskhistory.logLevel', 'MDC', 'Log level'),
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
                        boxLabel: Uni.I18n.translate('devicecommunicationtaskhistory.error', 'MDC', 'Error')
                    },
                    {
                        itemId: 'warning',
                        inputValue: 'Warning',
                        name: 'logLevels',
                        boxLabel: Uni.I18n.translate('devicecommunicationtaskhistory.warning', 'MDC', 'Warning')
                    },
                    {
                        itemId: 'information',
                        inputValue: 'Information',
                        name: 'logLevels',
                        boxLabel: Uni.I18n.translate('devicecommunicationtaskhistory.information', 'MDC', 'Information')
                    },
                    {
                        itemId: 'debug',
                        inputValue: 'Debug',
                        name: 'logLevels',
                        boxLabel: Uni.I18n.translate('devicecommunicationtaskhistory.debug', 'MDC', 'Debug')
                    },
                    {
                        itemId: 'trace',
                        inputValue: 'Trace',
                        name: 'logLevels',
                        boxLabel: Uni.I18n.translate('devicecommunicationtaskhistory.trace', 'MDC', 'Trace')
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

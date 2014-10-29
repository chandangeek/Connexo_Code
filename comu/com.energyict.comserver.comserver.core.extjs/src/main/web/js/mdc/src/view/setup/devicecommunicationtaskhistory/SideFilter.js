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
                items: [
                    {
                        itemId: 'error',
                        inputValue: 'Error',
                        name: 'logLevels',
                        boxLabel: Uni.I18n.translate('devicecommunicationtaskhistory.error', 'MDC', 'Error'),
                        afterBoxLabelTpl: '&nbsp;<span class="icon-validation icon-validation-red"></span>'
                    },
                    {
                        itemId: 'warning',
                        inputValue: 'Warning',
                        name: 'logLevels',
                        boxLabel: Uni.I18n.translate('devicecommunicationtaskhistory.warning', 'MDC', 'Warning'),
                        afterBoxLabelTpl: '&nbsp;<span class="icon-validation icon-validation-red"></span>'
                    },
                    {
                        itemId: 'information',
                        inputValue: 'Information',
                        name: 'logLevels',
                        boxLabel: Uni.I18n.translate('devicecommunicationtaskhistory.information', 'MDC', 'Information'),
                        afterBoxLabelTpl: '&nbsp;<span class="icon-validation icon-validation-red"></span>'
                    },
                    {
                        itemId: 'debug',
                        inputValue: 'Debug',
                        name: 'logLevels',
                        boxLabel: Uni.I18n.translate('devicecommunicationtaskhistory.debug', 'MDC', 'Debug'),
                        afterBoxLabelTpl: '&nbsp;<span class="icon-validation icon-validation-red"></span>'
                    },
                    {
                        itemId: 'trace',
                        inputValue: 'Trace',
                        name: 'logLevels',
                        boxLabel: Uni.I18n.translate('devicecommunicationtaskhistory.trace', 'MDC', 'Trace'),
                        afterBoxLabelTpl: '&nbsp;<span class="icon-validation icon-validation-red"></span>'
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
                        text: Uni.I18n.translate('general.reset', 'MDC', 'Reset'),
                        action: 'resetfilter'
                    }
                ]
            }
        ]
    }
});

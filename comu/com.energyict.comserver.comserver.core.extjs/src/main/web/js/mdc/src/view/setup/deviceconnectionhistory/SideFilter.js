Ext.define('Mdc.view.setup.deviceconnectionhistory.SideFilter', {
    extend: 'Uni.view.navigation.SubMenu',
    requires: [
    ],
    alias: 'widget.deviceconnectionhistorySideFilter',
    itemId: 'deviceconnectionhistorySideFilter',
    cls: 'filter-form',
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
                items: [
                    {
                        itemId: 'error',
                        inputValue: 'Error',
                        name: 'logLevels',
                        boxLabel: Uni.I18n.translate('deviceconnectionhistory.error', 'MDC', 'Error'),
                        afterBoxLabelTpl: '&nbsp;<span class="icon-validation icon-validation-red"></span>'
                    },
                    {
                        itemId: 'warning',
                        inputValue: 'Warning',
                        name: 'logLevels',
                        boxLabel: Uni.I18n.translate('deviceconnectionhistory.warning', 'MDC', 'Warning'),
                        afterBoxLabelTpl: '&nbsp;<span class="icon-validation icon-validation-red"></span>'
                    },
                    {
                        itemId: 'information',
                        inputValue: 'Information',
                        name: 'logLevels',
                        boxLabel: Uni.I18n.translate('deviceconnectionhistory.information', 'MDC', 'Information'),
                        afterBoxLabelTpl: '&nbsp;<span class="icon-validation icon-validation-red"></span>'
                    },
                    {
                        itemId: 'debug',
                        inputValue: 'Debug',
                        name: 'logLevels',
                        boxLabel: Uni.I18n.translate('deviceconnectionhistory.debug', 'MDC', 'Debug'),
                        afterBoxLabelTpl: '&nbsp;<span class="icon-validation icon-validation-red"></span>'
                    },
                    {
                        itemId: 'trace',
                        inputValue: 'Trace',
                        name: 'logLevels',
                        boxLabel: Uni.I18n.translate('deviceconnectionhistory.trace', 'MDC', 'Trace'),
                        afterBoxLabelTpl: '&nbsp;<span class="icon-validation icon-validation-red"></span>'
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
                items: [
                    {
                        itemId: 'connection',
                        inputValue: 'connections',
                        name: 'logTypes',
                        boxLabel: Uni.I18n.translate('deviceconnectionhistory.connection', 'MDC', 'Connection'),
                        afterBoxLabelTpl: '&nbsp;<span class="icon-validation icon-validation-red"></span>'
                    },
                    {
                        itemId: 'communications',
                        inputValue: 'communications',
                        name: 'logTypes',
                        boxLabel: Uni.I18n.translate('deviceconnectionhistory.communicationTask', 'MDC', 'Communication task'),
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

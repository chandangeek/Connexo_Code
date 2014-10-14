Ext.define('Mdc.view.setup.deviceregisterdata.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceRegisterDataSideFilter',
    itemId: 'deviceLoadProfileDataSideFilter',
    ui: 'medium',
    width: 150,
    cls: 'filter-form',
    title: Uni.I18n.translate('connection.widget.sideFilter.title', 'DSH', 'Filter'),
    items: {
        xtype: 'form',
        itemId: 'deviceRegisterDataFilterForm',
        ui: 'filter',
        items: [
            {
                xtype: 'fieldcontainer',
                itemId: 'suspectContainer',
                fieldLabel: Uni.I18n.translate('deviceregisterconfiguration.validation.result', 'MDC', 'Validation result'),
                labelAlign: 'top',
                defaultType: 'checkboxfield',
                items: [
                    {
                        itemId: 'suspect',
                        inputValue: 'suspect',
                        name: 'onlySuspect',
                        boxLabel: Uni.I18n.translate('validationStatus.suspect', 'MDC', 'Suspect'),
                        afterBoxLabelTpl: '&nbsp;<span class="icon-validation icon-validation-red"></span>'
                    },
                    {
                        itemId: 'nonSuspect',
                        inputValue: 'nonSuspect',
                        name: 'onlyNonSuspect',
                        padding: '-10 0 0 0',
                        boxLabel: Uni.I18n.translate('validationStatus.ok', 'MDC', 'Not suspect')
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
                        itemId: 'deviceRegisterDataFilterApplyBtn',
                        ui: 'action',
                        text: Uni.I18n.translate('general.apply', 'MDC', 'Apply'),
                        action: 'applyfilter'
                    },
                    {
                        itemId: 'deviceRegisterDataFilterResetBtn',
                        text: Uni.I18n.translate('general.reset', 'MDC', 'Reset'),
                        action: 'resetfilter'
                    }
                ]
            }
        ]
    }
});

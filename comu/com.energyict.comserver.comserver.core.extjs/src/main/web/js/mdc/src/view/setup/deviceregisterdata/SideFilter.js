Ext.define('Mdc.view.setup.deviceregisterdata.SideFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceRegisterDataSideFilter',
    requires: [
        'Uni.form.field.DateTime',
        'Mdc.store.RegisterDataDurations'
    ],
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
                itemId: 'fco-date-container',
                fieldLabel: Uni.I18n.translate('device.registerData.registerfilter', 'MDC', 'Register'),
                labelAlign: 'top',
                defaults: {
                    width: '100%'
                },
                items: [
                    {
                        xtype: 'date-time',
                        itemId: 'dtm-start-of-interval',
                        name: 'intervalStart',
                        fieldLabel: Uni.I18n.translate('deviceloadprofiles.filter.from', 'MDC', 'From'),
                        labelAlign: 'top',
                        labelStyle: 'font-weight: normal',
                        dateConfig: {
                            format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                        },
                        hoursConfig: {
                            margin: '0 0 0 -10'
                        }
                    }
                ]
            },
            {
                xtype: 'combobox',
                itemId: 'cbo-side-filter-duration',
                name: 'duration',
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.filter.duration', 'MDC', 'Duration'),
                labelAlign: 'top',
                store: 'Mdc.store.RegisterDataDurations',
                displayField: 'localizeValue',
                valueField: 'id',
                queryMode: 'local',
                anchor: '100%'
            },
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
                        text: Uni.I18n.translate('general.clearAll', 'MDC', 'Clear all'),
                        action: 'resetfilter'
                    }
                ]
            }
        ]
    }
});

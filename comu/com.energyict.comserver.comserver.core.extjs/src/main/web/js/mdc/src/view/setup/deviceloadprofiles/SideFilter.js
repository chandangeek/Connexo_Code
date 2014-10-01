Ext.define('Mdc.view.setup.deviceloadprofiles.SideFilter', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.form.field.DateTime',
        'Mdc.store.LoadProfileDataDurations'
    ],
    alias: 'widget.deviceLoadProfileDataSideFilter',
    itemId: 'deviceLoadProfileDataSideFilter',
    ui: 'medium',
    width: 150,
    cls: 'filter-form',
    title: Uni.I18n.translate('connection.widget.sideFilter.title', 'DSH', 'Filter'),
    items: {
        xtype: 'form',
        itemId: 'deviceLoadProfileDataFilterForm',
        ui: 'filter',
        items: [
            {
                xtype: 'fieldcontainer',
                itemId: 'dateContainer',
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval'),
                labelAlign: 'top',
                defaults: {
                    width: '100%'
                },
                items: [
                    {
                        xtype: 'date-time',
                        itemId: 'endOfInterval',
                        name: 'intervalStart',
                        fieldLabel: Uni.I18n.translate('deviceloadprofiles.filter.from', 'MDC', 'From'),
                        labelAlign: 'top',
                        labelStyle: 'font-weight: normal'
                    }
                ]
            },
            {
                xtype: 'combobox',
                itemId: 'sideFilterDuration',
                name: 'duration',
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.filter.duration', 'MDC', 'Duration'),
                labelAlign: 'top',
                store: 'Mdc.store.LoadProfileDataDurations',
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
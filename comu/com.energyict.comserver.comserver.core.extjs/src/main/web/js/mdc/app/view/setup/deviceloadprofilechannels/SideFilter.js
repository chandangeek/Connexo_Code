Ext.define('Mdc.view.setup.deviceloadprofilechannels.SideFilter', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.component.filter.view.Filter',
        'Mdc.widget.DateTimeField',
        'Mdc.store.LoadProfileDataDurations'
    ],
    alias: 'widget.deviceLoadProfileChannelDataSideFilter',
    itemId: 'deviceLoadProfileChannelDataSideFilter',
    ui: 'medium',
    width: 310,
    items: {
        xtype: 'filter-form',
        itemId: 'deviceLoadProfileChannelDataFilterForm',
        title: Uni.I18n.translate('general.filter', 'MDC', 'Filter'),
        ui: 'filter',
        items: [
            {
                xtype: 'component',
                html: '<h4>' + Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval') + '</h4>'
            },
            {
                xtype: 'dateTimeField',
                name: 'intervalStart',
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.filter.from', 'MDC', 'From'),
                labelAlign: 'top',
                dateCfg: {
                    flex: 1,
                    editable: false,
                    maxValue: new Date()
                },
                hourCfg: {
                    width: 57,
                    valueToRaw: function (value) {
                        return (value < 10 ? '0' : '') + value;
                    }
                },
                minuteCfg: {
                    width: 57,
                    valueToRaw: function (value) {
                        return (value < 10 ? '0' : '') + value;
                    }
                }
            },
            {
                xtype: 'combobox',
                name: 'duration',
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.filter.duration', 'MDC', 'Duration'),
                labelAlign: 'top',
                store: 'Mdc.store.LoadProfileDataDurations',
                displayField: 'localizeValue',
                valueField: 'id',
                editable: false,
                queryMode: 'local'
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
                        action: 'filter'
                    },
                    {
                        itemId: 'deviceLoadProfileDataFilterResetBtn',
                        text: Uni.I18n.translate('general.reset', 'MDC', 'Reset'),
                        action: 'reset'
                    }
                ]
            }
        ]
    }
});
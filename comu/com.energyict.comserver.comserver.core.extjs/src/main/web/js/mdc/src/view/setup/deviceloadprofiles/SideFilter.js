Ext.define('Mdc.view.setup.deviceloadprofiles.SideFilter', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.component.filter.view.Filter',
        'Uni.form.NestedForm',
        'Mdc.widget.DateTimeField',
        'Mdc.store.LoadProfileDataDurations',
        'Mdc.view.setup.deviceloadprofiles.SideFilterDateTime',
        'Mdc.view.setup.deviceloadprofiles.SideFilterDuration'
    ],
    alias: 'widget.deviceLoadProfileDataSideFilter',
    itemId: 'deviceLoadProfileDataSideFilter',
    ui: 'medium',
    cls: 'filter-form',
    width: 250,
    title: Uni.I18n.translate('connection.widget.sideFilter.title', 'DSH', 'Filter'),
    items: {
        xtype: 'nested-form',
        itemId: 'deviceLoadProfileDataFilterForm',
        ui: 'filter',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [
            {
                xtype: 'side-filter-date-time-profiles',
                itemId: 'endOfInterval',
                name: 'endOfInterval',
                wTitle: Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval')
            },
//            {
//                xtype: 'component',
//                html: '<h4>' + Uni.I18n.translate('deviceloadprofiles.endOfInterval', 'MDC', 'End of interval') + '</h4>'
//            },
//            {
//                xtype: 'dateTimeField',
//                name: 'intervalStart',
//                fieldLabel: Uni.I18n.translate('deviceloadprofiles.filter.from', 'MDC', 'From'),
//                labelAlign: 'top',
//                dateCfg: {
//                    flex: 1,
//                    editable: false,
//                    maxValue: new Date()
//                },
//                hourCfg: {
//                    width: 57,
//                    valueToRaw: function (value) {
//                        return (value < 10 ? '0' : '') + value;
//                    }
//                },
//                minuteCfg: {
//                    width: 57,
//                    valueToRaw: function (value) {
//                        return (value < 10 ? '0' : '') + value;
//                    }
//                }
//            },
            {
                xtype: 'side-filter-duration',
                itemId: 'sideFilterDuration',
                name: 'duration',
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.filter.duration', 'MDC', 'Duration'),
                labelAlign: 'top',
                store: 'Mdc.store.LoadProfileDataDurations',
                displayField: 'localizeValue',
                valueField: 'id'
            },
//            {
//                xtype: 'component',
//                html: '<h4>' + Uni.I18n.translate('deviceregisterconfiguration.validation.result', 'MDC', 'Validation result') + '</h4>'
//            },
//            {
//                xtype: 'checkbox',
//                fieldLabel: Uni.I18n.translate('deviceloadprofiles.filter.from', 'MDC', 'From')
//            }
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
                        text: Uni.I18n.translate('connection.widget.sideFilter.clearAll', 'DSH', 'Clear all'),
                        action: 'clearfilter'
                    }
                ]
            }
        ]
    }
});
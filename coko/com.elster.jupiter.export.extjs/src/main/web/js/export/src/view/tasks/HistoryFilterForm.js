Ext.define('Dxp.view.tasks.HistoryFilterForm', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.history-filter-form',

    requires: [
        'Uni.component.filter.view.Filter',
        'Uni.form.NestedForm',
        'Uni.form.field.DateTime'
    ],

    cls: 'filter-form',
    width: 250,
    style: 'padding: 0',
    title: Uni.I18n.translate('todo', 'DES', 'Filter'),
    ui: 'medium',

    items: [
        {
            xtype: 'form',
            itemId: 'filter-form',
            ui: 'filter',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'fieldset',
                    style: {
                        border: 'none',
                        padding: 0,
                        marginTop: '15px'
                    },
                    defaults: {
                        xtype: 'date-time',
                        labelWidth: 30,
                        labelAlign: 'left',
                        labelStyle: 'font-weight: normal',
                        style: {
                            border: 'none',
                            padding: 0,
                            marginBottom: '10px'
                        },
                        getRawValue: true
                    },
                    items: [
                        {
                            xtype: 'panel',
                            baseCls: 'x-form-item-label',
                            title: Uni.I18n.translate('todo', 'DES', 'Started between')
                        },
                        {
                            xtype: 'date-time',
                            name: 'startedOnFrom',
                            fieldLabel: Uni.I18n.translate('todo', 'DES', 'From')
                        },
                        {
                            xtype: 'date-time',
                            name: 'startedOnTo',
                            fieldLabel: Uni.I18n.translate('todo', 'DES', 'To')
                        }
                    ]
                },
                {
                    xtype: 'fieldset',
                    style: {
                        border: 'none',
                        padding: 0,
                        marginTop: '15px'
                    },
                    defaults: {
                        xtype: 'date-time',
                        labelWidth: 30,
                        labelAlign: 'left',
                        labelStyle: 'font-weight: normal',
                        style: {
                            border: 'none',
                            padding: 0,
                            marginBottom: '10px'
                        },
                        getRawValue: true
                    },
                    items: [
                        {
                            xtype: 'panel',
                            baseCls: 'x-form-item-label',
                            title: Uni.I18n.translate('todo', 'DES', 'Finished between')
                        },
                        {
                            name: 'finishedOnFrom',
                            fieldLabel: Uni.I18n.translate('todo', 'DES', 'From')
                        },
                        {
                            name: 'finishedOnTo',
                            fieldLabel: Uni.I18n.translate('todo', 'DES', 'To')
                        }
                    ]
                },
                {
                    xtype: 'datefield',
                    name: 'exportPeriodContains',
                    labelAlign: 'top',
                    fieldLabel: Uni.I18n.translate('todo', 'DES', 'Export period contains'),
                    rawToValue: function (rawValue) {
                        if (rawValue) {
                            var date = Ext.Date.parse(rawValue, 'd/m/Y');
                            if (date && Ext.isDate(date)) return date.getTime();
                        }
                        return null;
                    },
                    valueToRaw: function (value) {
                        if (value) return Ext.util.Format.date(new Date(value), 'd/m/Y');
                        return null;
                    }
                }
            ],
            dockedItems: [
                {
                    xtype: 'toolbar',
                    dock: 'bottom',
                    items: [
                        {
                            text: Uni.I18n.translate('connection.widget.sideFilter.apply', 'DES', 'Apply'),
                            ui: 'action',
                            action: 'applyfilter'
                        },
                        {
                            text: Uni.I18n.translate('connection.widget.sideFilter.clearAll', 'DES', 'Clear all'),
                            action: 'clearfilter'
                        }
                    ]
                }
            ]
        }
    ]
});
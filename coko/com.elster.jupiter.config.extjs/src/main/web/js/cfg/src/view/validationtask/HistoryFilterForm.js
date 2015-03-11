Ext.define('Cfg.view.validationtask.HistoryFilterForm', {
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
    title: Uni.I18n.translate('dataValidationTasks.filter', 'CFG', 'Filter'),
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
                            title: Uni.I18n.translate('dataValidationTasks.started', 'CFG', 'Started between')
                        },
                        {
                            xtype: 'date-time',
                            name: 'startedOnFrom',
                            fieldLabel: Uni.I18n.translate('dataValidationTasks.from', 'CFG', 'From'),
                            dateConfig: {
                                format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                            }
                        },
                        {
                            xtype: 'date-time',
                            name: 'startedOnTo',
                            fieldLabel: Uni.I18n.translate('dataValidationTasks.to', 'CFG', 'To'),
                            dateConfig: {
                                format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                            }
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
                        getRawValue: true,
                        dateConfig: {
                            format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                        }
                    },
                    items: [
                        {
                            xtype: 'panel',
                            baseCls: 'x-form-item-label',
                            title: Uni.I18n.translate('dataValidationTasks.finished', 'CFG', 'Finished between')
                        },
                        {
                            name: 'finishedOnFrom',
                            fieldLabel: Uni.I18n.translate('dataValidationTasks.from', 'CFG', 'From')
                        },
                        {
                            name: 'finishedOnTo',
                            fieldLabel: Uni.I18n.translate('dataValidationTasks.to', 'CFG', 'To')
                        }
                    ]
                },
                {
                    xtype: 'datefield',
                    name: 'exportPeriodContains',
                    labelAlign: 'top',
                    fieldLabel: Uni.I18n.translate('dataValidationTasks.validationPeriod', 'CFG', 'Validation period contains'),
                    rawToValue: function (rawValue) {
                        if (rawValue) {
                            var date = Ext.Date.parse(rawValue, Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault));
                            if (date && Ext.isDate(date)) return date.getTime();
                        }
                        return null;
                    },
                    valueToRaw: function (value) {
                        if (value) return Uni.DateTime.formatDateShort(new Date(value));
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
                            text: Uni.I18n.translate('dataValidationTasks.connection.widget.sideFilter.apply', 'CFG', 'Apply'),
                            ui: 'action',
                            action: 'applyfilter'
                        },
                        {
                            text: Uni.I18n.translate('dataValidationTasks.connection.widget.sideFilter.clearAll', 'CFG', 'Clear all'),
                            action: 'clearfilter'
                        }
                    ]
                }
            ]
        }
    ]
});
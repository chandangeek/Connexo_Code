Ext.define('Est.estimationtasks.view.HistoryFilterForm', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.estimationtasks-history-filter-form',

    requires: [
        'Uni.component.filter.view.Filter',
        'Uni.form.NestedForm',
        'Uni.form.field.DateTime'
    ],

    cls: 'filter-form',
    width: 250,
    style: 'padding: 0',
    title: Uni.I18n.translate('general.filter', 'EST', 'Filter'),
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
                            title: Uni.I18n.translate('estimationtasks', 'EST', 'Started between')
                        },
                        {
                            xtype: 'date-time',
                            name: 'startedOnFrom',
                            fieldLabel: Uni.I18n.translate('estimationtasks.from', 'EST', 'From'),
                            dateConfig: {
                                format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                            }
                        },
                        {
                            xtype: 'date-time',
                            name: 'startedOnTo',
                            fieldLabel: Uni.I18n.translate('estimationtasks.to', 'EST', 'To'),
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
                            title: Uni.I18n.translate('estimationtasks.finished', 'EST', 'Finished between')
                        },
                        {
                            name: 'finishedOnFrom',
                            fieldLabel: Uni.I18n.translate('estimationtasks.from', 'EST', 'From')
                        },
                        {
                            name: 'finishedOnTo',
                            fieldLabel: Uni.I18n.translate('estimationtasks.to', 'EST', 'To')
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
                            text: Uni.I18n.translate('general.apply', 'EST', 'Apply'),
                            ui: 'action',
                            action: 'applyfilter'
                        },
                        {
                            text: Uni.I18n.translate('general.clearAll', 'EST', 'Clear all'),
                            action: 'clearfilter'
                        }
                    ]
                }
            ]
        }
    ]
});
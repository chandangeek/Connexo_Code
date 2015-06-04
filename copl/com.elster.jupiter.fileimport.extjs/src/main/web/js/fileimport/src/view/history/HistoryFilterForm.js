Ext.define('Fim.view.history.HistoryFilterForm', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.fim-history-filter-form',
    requires: [
        'Uni.component.filter.view.Filter',
        'Uni.form.NestedForm',
        'Uni.form.field.DateTime',
        'Uni.form.filter.FilterCombobox',
        'Fim.store.ImportServicesFilter',
        'Fim.store.Status'
    ],

    cls: 'filter-form',
    width: 250,
    style: 'padding: 0',
    title: Uni.I18n.translate('importService.history.sideFilter.title', 'FIM', 'Filter'),
    ui: 'medium',
    showImportService: false,
    initComponent: function () {
        var me = this;
        me.items = [
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
                        xtype: 'uni-filter-combo',
                        itemId: 'cbo-import-service',
                        name: 'importService',
                        fieldLabel: Uni.I18n.translate('general.importService', 'FIM', 'Import service'),
                        displayField: 'name',
                        valueField: 'id',
                        labelAlign: 'top',
                        store: 'Fim.store.ImportServicesFilter',
                        hidden: !me.showImportService
                    },
                    {
                        xtype: 'fieldset',
                        style: {
                            border: 'none',
                            padding: 0,
                            marginTop: '15px'
                        },
                        name: 'startedOn',
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
                                title: Uni.I18n.translate('importService.history.started', 'FIM', 'Started between')
                            },
                            {
                                name: 'startedOnFrom',
                                fieldLabel: Uni.I18n.translate('importService.history.from', 'FIM', 'From')
                            },
                            {
                                name: 'startedOnTo',
                                fieldLabel: Uni.I18n.translate('importService.history.to', 'FIM', 'To')
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
                        name: 'finishedOn',
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
                                title: Uni.I18n.translate('importService.history.finished', 'FIM', 'Finished between')
                            },
                            {
                                name: 'finishedOnFrom',
                                fieldLabel: Uni.I18n.translate('importService.history.from', 'FIM', 'From')
                            },
                            {
                                name: 'finishedOnTo',
                                fieldLabel: Uni.I18n.translate('importService.history.to', 'FIM', 'To')
                            }
                        ]
                    },
                    {
                        xtype: 'uni-filter-combo',
                        labelAlign: 'top',
                        itemId: 'cbo-status',
                        name: 'status',
                        fieldLabel: Uni.I18n.translate('importService.history.status', 'FIM', 'Status'),
                        displayField: 'display',
                        valueField: 'value',
                        store: 'Fim.store.Status'
                    }

                ],
                dockedItems: [
                    {
                        xtype: 'toolbar',
                        dock: 'bottom',
                        items: [
                            {
                                text: Uni.I18n.translate('importService.history.sideFilter.apply', 'FIM', 'Apply'),
                                ui: 'action',
                                action: 'applyfilter'
                            },
                            {
                                text: Uni.I18n.translate('importService.history.sideFilter.clearAll', 'FIM', 'Clear all'),
                                action: 'clearfilter'
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }

});
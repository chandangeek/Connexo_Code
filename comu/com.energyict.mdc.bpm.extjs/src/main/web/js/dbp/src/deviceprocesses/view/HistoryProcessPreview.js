Ext.define('Dbp.deviceprocesses.view.HistoryProcessPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.dbp-history-process-preview',
    requires: [
        'Uni.form.field.Duration'
    ],

    items: {
        itemId: 'frm-preview-history-process',
        xtype: 'form',
        defaults: {
            labelWidth: 250
        },
        items: [
            {
                xtype: 'container',
                layout: {
                    type: 'column',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'container',
                        columnWidth: 0.5,
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            labelWidth: 150
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('dbp.process.startDate', 'DBP', 'Start date'),
                                name: 'startDateDisplay',
                                itemId: 'dbp-preview-history-process-start-date'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('dbp.process.endDate', 'DBP', 'End date'),
                                name: 'endDateDisplay',
                                itemId: 'dbp-preview-history-process-end-date'
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        columnWidth: 0.5,
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            labelWidth: 150
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('dbp.process.startedBy', 'DBP', 'Started by'),
                                name: 'startedBy',
                                itemId: 'dbp-preview-history-process-started-by'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('dbp.process.version', 'DBP', 'Version'),
                                name: 'version',
                                itemId: 'dbp-preview-history-process-version'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('dbp.process.status', 'DBP', 'Status'),
                                name: 'statusDisplay',
                                itemId: 'dbp-history-preview-process-status'
                            }
                        ]
                    }
                ]
            }

        ]
    }
});


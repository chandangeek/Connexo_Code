Ext.define('Dbp.deviceprocesses.view.RunningProcessPreview', {
    extend: 'Ext.form.Panel',
    frame: true,
    alias: 'widget.dbp-running-process-preview',
    requires: [],

    items: {
        itemId: 'frm-preview-running-process',
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
                                itemId: 'dbp-preview-running-process-start-date'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('dbp.process.openTasks', 'DBP', 'Open tasks'),
                                name: 'openTasks',
                                itemId: 'dbp-preview-running-process-open-tasks',
                                renderer: function (value, field) {
                                    return value;
                                }
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
                                itemId: 'dbp-preview-running-process-started-by'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('dbp.process.version', 'DBP', 'Version'),
                                name: 'version',
                                itemId: 'dbp-preview-running-process-version'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('dbp.process.status', 'DBP', 'Status'),
                                name: 'statusDisplay',
                                itemId: 'dbp-running-preview-process-status'
                            }
                        ]
                    }
                ]
            }

        ]
    }
});


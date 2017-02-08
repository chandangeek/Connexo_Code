/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorprocesses.view.RunningProcessPreview', {
    extend: 'Ext.form.Panel',
    frame: true,
    alias: 'widget.bpm-running-process-preview',
    requires: [],

    items: {
        itemId: 'frm-preview-running-process',
        xtype: 'form',
        height: 140,
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
                                fieldLabel: Uni.I18n.translate('bpm.process.startDate', 'BPM', 'Start date'),
                                name: 'startDateDisplay',
                                itemId: 'bpm-preview-running-process-start-date'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('bpm.process.openTasks', 'BPM', 'Open tasks'),
                                name: 'openTasks',
                                itemId: 'bpm-preview-running-process-open-tasks',
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
                                fieldLabel: Uni.I18n.translate('bpm.process.startedBy', 'BPM', 'Started by'),
                                name: 'startedBy',
                                itemId: 'bpm-preview-running-process-started-by'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('bpm.process.version', 'BPM', 'Version'),
                                name: 'version',
                                itemId: 'bpm-preview-running-process-version'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('bpm.process.status', 'BPM', 'Status'),
                                name: 'statusDisplay',
                                itemId: 'bpm-running-preview-process-status'
                            }
                        ]
                    }
                ]
            }

        ]
    }
});


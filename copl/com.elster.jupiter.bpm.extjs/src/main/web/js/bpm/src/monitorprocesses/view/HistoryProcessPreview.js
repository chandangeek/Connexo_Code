/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.monitorprocesses.view.HistoryProcessPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.bpm-history-process-preview',
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
                                fieldLabel: Uni.I18n.translate('bpm.process.startDate', 'BPM', 'Start date'),
                                name: 'startDateDisplay',
                                itemId: 'bpm-preview-history-process-start-date'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('bpm.process.endDate', 'BPM', 'End date'),
                                name: 'endDateDisplay',
                                itemId: 'bpm-preview-history-process-end-date'
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
                                itemId: 'bpm-preview-history-process-started-by'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('bpm.process.version', 'BPM', 'Version'),
                                name: 'version',
                                itemId: 'bpm-preview-history-process-version'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('bpm.process.status', 'BPM', 'Status'),
                                name: 'statusDisplay',
                                itemId: 'bpm-history-preview-process-status'
                            }
                        ]
                    }
                ]
            }

        ]
    }
});


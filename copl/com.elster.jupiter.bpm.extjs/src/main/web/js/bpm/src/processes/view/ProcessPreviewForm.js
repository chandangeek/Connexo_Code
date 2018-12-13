/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.processes.view.ProcessPreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.bpm-process-preview-form',

    requires: [],

    defaults: {
        labelWidth: 250
    },

    initComponent: function () {
        var me = this;
        me.items = [
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
                                fieldLabel: Uni.I18n.translate('bpm.process.associated', 'BPM', 'Associated to'),
                                name: 'displayType',
                                itemId: 'bpm-preview-process-associated'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('bpm.process.deploymentId', 'BPM', 'DeploymentID'),
                                name: 'deploymentId',
                                itemId: 'bpm-preview-deployment-id'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('bpm.process.version', 'BPM', 'Version'),
                                name: 'version',
                                itemId: 'bpm-preview-version'
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
                                fieldLabel: Uni.I18n.translate('bpm.process.status', 'BPM', 'Status'),
                                name: 'active',
                                itemId: 'bpm-preview-process-status',
                                renderer: function (value) {
                                    switch (value) {
                                        case 'ACTIVE':
                                            return Uni.I18n.translate('bpm.process.active', 'BPM', 'Active');
                                            break;
                                        case 'INACTIVE':
                                            return Uni.I18n.translate('bpm.process.inactive', 'BPM', 'Inactive');
                                            break;
                                        case 'UNDEPLOYED':
                                            return Uni.I18n.translate('bpm.process.undeployed', 'BPM', 'Undeployed');
                                            break;
                                        default:
                                            return Ext.isEmpty(value) ? '-' : value;
                                    }
                                }
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('bpm.process.application', 'BPM', 'Application'),
                                name: 'appKey',
                                itemId: 'bpm-preview-appKey',
                                renderer: function (value) {
                                    switch (value) {
                                        case 'INS':
                                            return Uni.I18n.translate('bpm.process.insight', 'BPM', 'Insight');
                                            break;
                                        case 'MDC':
                                            return Uni.I18n.translate('bpm.process.mdc', 'BPM', 'MultiSense');
                                            break;
                                        default:
                                            return Ext.isEmpty(value) ? '-' : value;
                                    }
                                }
                            }
                        ]
                    }
                ]
            }

        ];

        me.callParent();
    }
});

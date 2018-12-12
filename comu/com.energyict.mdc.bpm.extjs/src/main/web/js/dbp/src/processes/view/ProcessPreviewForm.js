/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dbp.processes.view.ProcessPreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.dbp-process-preview-form',

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
                                fieldLabel: Uni.I18n.translate('dbp.process.associated', 'DBP', 'Associated to'),
                                name: 'associatedTo',
                                itemId: 'dbp-preview-process-associated'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('dbp.process.deploymentId', 'DBP', 'DeploymentID'),
                                name: 'deploymentId',
                                itemId: 'dbp-preview-deployment-id'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('dbp.process.version', 'DBP', 'Version'),
                                name: 'version',
                                itemId: 'dbp-preview-version'
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
                                fieldLabel: Uni.I18n.translate('dbp.process.status', 'DBP', 'Status'),
                                name: 'active',
                                itemId: 'dbp-preview-process-status',
                                renderer: function (value) {
                                    switch (value) {
                                        case 'ACTIVE':
                                            return Uni.I18n.translate('dbp.process.active', 'DBP', 'Active');
                                            break;
                                        case 'INACTIVE':
                                            return Uni.I18n.translate('dbp.process.inactive', 'DBP', 'Inactive');
                                            break;
                                        case 'UNDEPLOYED':
                                            return Uni.I18n.translate('dbp.process.undeployed', 'DBP', 'Undeployed');
                                            break;
                                        default:
                                            return value;
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

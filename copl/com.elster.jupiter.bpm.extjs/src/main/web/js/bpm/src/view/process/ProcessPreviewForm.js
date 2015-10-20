Ext.define('Bpm.view.process.ProcessPreviewForm', {
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
                                name: 'associated',
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
                                fieldLabel: Uni.I18n.translate('bpm.process.state', 'BPM', 'State'),
                                name: 'active',
                                itemId: 'bpm-preview-process-state',
                                renderer: function (value) {
                                    if (value) {
                                        return Uni.I18n.translate('validation.active', 'CFG', 'Active')
                                    } else {
                                        return Uni.I18n.translate('validation.inactive', 'CFG', 'Inactive')
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

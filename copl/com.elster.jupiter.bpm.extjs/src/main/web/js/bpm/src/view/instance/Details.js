Ext.define('Bpm.view.instance.Details', {
    extend: 'Ext.form.Panel',
    alias: 'widget.instanceDetails',
    itemId: 'instanceDetails',
    frame: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    requires: [
        'Bpm.store.ProcessInstances',
        'Bpm.model.ProcessInstance'
    ],
    title: Uni.I18n.translate('proces.processInstance','BPM','Process instance'),

    items: [
        {
            xtype: 'form',
            itemId: 'instanceDetailsForm',
            defaults: {
                xtype: 'container',
                layout: 'form',
                columnWidth: 0.5
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
                                    name: 'id',
                                    fieldLabel: Uni.I18n.translate('bpm.instance.id', 'BPM', 'Process id')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'name',
                                    fieldLabel: Uni.I18n.translate('bpm.instance.name', 'BPM', 'Definition name')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'deploymentId',
                                    fieldLabel: Uni.I18n.translate('bpm.instance.deploymentId', 'BPM', 'Deployment id')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'version',
                                    fieldLabel: Uni.I18n.translate('bpm.instance.version', 'BPM', 'Version')
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
                                    name: 'initiator',
                                    fieldLabel: Uni.I18n.translate('bpm.instance.initiator', 'BPM', 'Initiator')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'state',
                                    renderer: function (value) {
                                        switch(value) {
                                            case 1 : return Uni.I18n.translate('bpm.instance.state.active', 'BPM', 'Active');
                                            case 2 : return Uni.I18n.translate('bpm.instance.state.completed', 'BPM', 'Completed');
                                            case 3 : return Uni.I18n.translate('bpm.instance.state.aborted', 'BPM', 'Aborted');
                                        }
                                    },
                                    fieldLabel: Uni.I18n.translate('bpm.instance.state', 'BPM', 'State')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'startDate',
                                    fieldLabel: Uni.I18n.translate('bpm.instance.startDate', 'BPM', 'Start date')
                                },                                {
                                    xtype: 'displayfield',
                                    name: 'endDate',
                                    fieldLabel: Uni.I18n.translate('bpm.instance.endDate', 'BPM', 'End date')
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ]
});


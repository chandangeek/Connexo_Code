Ext.define('Bpm.view.instance.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.instanceOverview',
    itemId: 'instanceOverview',
    overflowY: 'auto',
    requires: [
        'Ext.panel.Panel',
        'Bpm.model.ProcessInstance',
        'Bpm.view.instance.Variable',
        'Bpm.view.instance.Node'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('bpm.processInstance', 'BPM', 'Process instance'),
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'form',
                    title: Uni.I18n.translate('bpm.instance.overview', 'BPM', 'Overview'),
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
                                labelWidth: 250
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'name',
                                    fieldLabel: Uni.I18n.translate('bpm.instance.name', 'BPM', 'Definition name')
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
                                    name: 'initiator',
                                    fieldLabel: Uni.I18n.translate('bpm.instance.initiator', 'BPM', 'Initiator')
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
                                labelWidth: 250
                            },
                            items: [
                                {
                                    xtype: 'displayfield',
                                    name: 'startDate',
                                    fieldLabel: Uni.I18n.translate('bpm.instance.startDate', 'BPM', 'Start date')
                                },
                                {
                                    xtype: 'displayfield',
                                    name: 'endDate',
                                    fieldLabel: Uni.I18n.translate('bpm.instance.endtDate', 'BPM', 'End date')
                                },
                                {
                                    xtype: 'displayfield',
                                    itemId: 'currentActivities',
                                    fieldLabel: Uni.I18n.translate('bpm.instance.current', 'BPM', 'Current activities')
                                }

                            ]
                        }
                    ]
                },
                {
                    xtype: 'variableList'
                },
                {
                    xtype: 'nodeList'
                }
            ]
        }
    ]
});

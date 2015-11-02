Ext.define('Apr.view.taskoverview.TaskPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.task-preview',

    requires: [],

    items: [
        {
            xtype: 'form',
            items: [
                {
                    xtype: 'panel',
                    layout: {
                        type: 'column'
                    },

                    items: [
                        {
                            xtype: 'container',
                            columnWidth: 0.49,
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            items: [
                                {
                                    xtype: 'fieldcontainer',
                                    fieldLabel: Uni.I18n.translate('general.task', 'APR', 'Task'),
                                    labelAlign: 'top',
                                    layout: 'vbox',
                                    defaults: {
                                        xtype: 'displayfield',
                                        labelWidth: 250
                                    },
                                    items: [
                                        {
                                            fieldLabel: Uni.I18n.translate('general.name', 'APR', 'name'),
                                            name: 'name'
                                        },
                                        {
                                            fieldLabel: Uni.I18n.translate('general.application', 'APR', 'Application'),
                                            name: 'application'
                                        },
                                        {
                                            fieldLabel: Uni.I18n.translate('general.queue', 'APR', 'Queue'),
                                            name: 'queue'
                                        }
                                    ]
                                },
                                {
                                    xtype: 'fieldcontainer',
                                    fieldLabel: Uni.I18n.translate('general.schedule', 'APR', 'Schedule'),
                                    labelAlign: 'top',
                                    layout: 'vbox',
                                    defaults: {
                                        xtype: 'displayfield',
                                        labelWidth: 200
                                    },
                                    items: [
                                        {
                                            fieldLabel: Uni.I18n.translate('general.trigger', 'APR', 'Trigger'),
                                            name: 'trigger'
                                        },
                                        {
                                            fieldLabel: Uni.I18n.translate('general.queueStatus', 'APR', 'Queue status'),
                                            name: 'queueStatusString'
                                        },
                                        {
                                            fieldLabel: Uni.I18n.translate('general.duration', 'APR', 'Duration'),
                                            name: 'duration'
                                        },
                                        {
                                            fieldLabel: Uni.I18n.translate('general.nextRun', 'APR', 'Next run'),
                                            name: 'nextRun'
                                        }
                                    ]
                                }
                            ]
                        },

                        {
                            xtype: 'container',
                            columnWidth: 0.49,
                            layout: {
                                type: 'vbox',
                                align: 'stretch'
                            },
                            items: [
                                {
                                    xtype: 'fieldcontainer',
                                    fieldLabel: Uni.I18n.translate('general.lastRun', 'APR', 'Last run'),
                                    labelAlign: 'top',
                                    layout: 'vbox',
                                    defaults: {
                                        xtype: 'displayfield',
                                        labelWidth: 250
                                    },
                                    items: [
                                        {
                                            fieldLabel: Uni.I18n.translate('general.status', 'APR', 'Status'),
                                            name: 'trigger'
                                        },
                                        {
                                            fieldLabel: Uni.I18n.translate('general.duration', 'APR', 'Duration'),
                                            name: 'duration'
                                        }
                                    ]
                                }
                            ]
                        }
                    ]
                }
            ]

        }
    ]
});

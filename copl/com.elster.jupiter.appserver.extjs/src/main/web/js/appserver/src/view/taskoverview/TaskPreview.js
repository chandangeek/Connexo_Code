/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.taskoverview.TaskPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.task-preview',

    requires: [
        'Uni.form.field.Duration'
    ],

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
                                            fieldLabel: Uni.I18n.translate('general.name', 'APR', 'Name'),
                                            name: 'name'
                                        },
                                        {
                                            fieldLabel: Uni.I18n.translate('general.application', 'APR', 'Application'),
                                            name: 'application',
                                            renderer: function(value) {
                                                if(!Ext.isEmpty(value)) {
                                                    return value.name;
                                                } else {
                                                    return '-';
                                                }
                                            }
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
                                        labelWidth: 250
                                    },
                                    items: [
                                        {
                                            fieldLabel: Uni.I18n.translate('general.trigger', 'APR', 'Trigger'),
                                            name: 'trigger'
                                        },
                                        {
                                            fieldLabel: Uni.I18n.translate('general.currentrun', 'APR', 'Current run'),
                                            itemId: 'currentRunField',
                                            name: 'queueStatusString'
                                        },
                                        {
                                            xtype: 'uni-form-field-duration',
                                            fieldLabel: Uni.I18n.translate('general.duration', 'APR', 'Duration'),
                                            itemId: 'durationField',
                                            name: 'currentRunDuration'
                                        },
                                        {
                                            fieldLabel: Uni.I18n.translate('general.nextRun', 'APR', 'Next run'),
                                            itemId: 'nextRunField',
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
                                            name: 'lastRunStatusString',
                                            renderer: function(value){
                                                if(!value){
                                                    return '-';
                                                } else {
                                                    return value;
                                                }
                                            }
                                        },
                                        {
                                            xtype: 'uni-form-field-duration',
                                            fieldLabel: Uni.I18n.translate('general.duration', 'APR', 'Duration'),
                                            name: 'lastRunDuration'
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

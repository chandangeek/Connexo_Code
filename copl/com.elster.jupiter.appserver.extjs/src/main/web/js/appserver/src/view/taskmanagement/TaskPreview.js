/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.taskmanagement.TaskPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.task-management-preview',

    requires: [
        'Uni.form.field.Duration',
        'Apr.view.taskmanagement.ActionMenu'
    ],
    tools: [
        {
            xtype: 'uni-button-action',
            itemId: 'btn-task-management-preview-action-menu',
            menu: {
                xtype: 'task-management-action-menu'
            }
        }
    ],
    items: [
        {
            xtype: 'form',
            itemId: 'task-management-preview-form',
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
                                            renderer: function (value) {
                                                if (!value) {
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

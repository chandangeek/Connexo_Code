/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.taskmanagement.DetailsGeneralTask', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.general-task-details',
    requires: [
        'Apr.view.taskmanagement.ActionMenu'
    ],
    actionMenu: null,
    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    ui: 'large',
                    itemId: 'general-task-details-panel',
                    title: Uni.I18n.translate('general.details', 'APR', 'Details'),
                    flex: 1,
                    items: {
                        xtype: 'form',
                        itemId: 'general-task-details-form',
                        margin: '0 0 0 100',
                        defaults: {
                            labelWidth: 250,
                        },
                        items: [
                            {
                                fieldLabel: Uni.I18n.translate('general.name', 'APR', 'Name'),
                                xtype: 'displayfield',
                                htmlEncode: false,
                                itemId: 'name-field-container'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.followedBy', 'APR', 'Followed by'),
                                xtype: 'displayfield',
                                htmlEncode: false,
                                itemId: 'followedBy-field-container'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.precededBy', 'APR', 'Preceded by'),
                                xtype: 'displayfield',
                                htmlEncode: false,
                                itemId: 'precededBy-field-container'
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.suspended', 'APR', 'Suspended'),
                                xtype: 'displayfield',
                                htmlEncode: false,
                                itemId: 'suspended-field-container',
                                name: 'suspendUntilTime',
                                renderer: function(value){
                                    return value  ? Uni.I18n.translate('general.suspended.yes','APR','Yes.</br>The task has been suspended until next run') : Uni.I18n.translate('general.suspended.no','APR','No')
                                }
                            }

                        ]
                    }
                },
                {
                    xtype: 'uni-button-action',
                    margin: '20 0 0 0',
                    menu: me.actionMenu,
                    hidden: !me.actionMenu
                }
            ]
        };
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'uni-view-menu-side',
                        itemId: 'general-task-details-side-menu',
                        title: Uni.I18n.translate('general.generalTask', 'APR', 'General Task'),
                        objectType: Uni.I18n.translate('general.generalTask', 'APR', 'General Task'),
                        menuItems: [
                            {
                                text: Uni.I18n.translate('general.details', 'APR', 'Details'),
                                itemId: 'general-task-details-overview-link'
                            }
                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);
    },

    setRecurrentTasks: function (itemId, recurrentTasks) {
        var me = this,
            recurrentTaskList = [];

        Ext.isArray(recurrentTasks) && Ext.Array.each(recurrentTasks, function (recurrentTask) {
            recurrentTaskList.push('- ' + Ext.htmlEncode(recurrentTask.name));
        });
        me.down(itemId).setValue((recurrentTaskList.length == 0) ? recurrentTaskList = '-' : recurrentTaskList.join('<br/>'));
        return;
    }
});

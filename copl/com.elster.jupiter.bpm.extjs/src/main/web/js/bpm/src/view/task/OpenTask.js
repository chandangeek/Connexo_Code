Ext.define('Bpm.view.task.OpenTask', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.bpm-task-open-task',
    requires: [

    ],
    taskRecord: null,
    edit: false,

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                itemId: 'frm-open-task',
                ui: 'large',
                items: [
                    {
                        xtype: 'container',
                        padding: 10,
                        border: 1,
                        itemId: 'frm-open-task-container',
                        style: {
                            borderColor: 'lightgray',
                            borderStyle: 'solid'
                        },
                        height: 320,
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        items: [
                            {
                                xtype: 'container',
                                itemId: 'formContent',
                                height: 300
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        margin: '10 0 0 0',
                        layout: 'hbox',
                        items: [
                            {
                                text: Uni.I18n.translate('task.action.claim', 'BPM', 'Claim'),
                                xtype: 'button',
                                hidden: true,
                                ui: 'action',
                                itemId: 'btn-claim',
                                action: 'claimTask',
                                taskRecord: me.taskRecord
                            },
                            {
                                text: Uni.I18n.translate('task.action.save', 'BPM', 'Save'),
                                xtype: 'button',
                                hidden: true,
                                ui: 'action',
                                itemId: 'btn-save',
                                action: 'saveTask',
                                taskRecord: me.taskRecord
                            },
                            {
                                text: Uni.I18n.translate('task.action.release', 'BPM', 'Release'),
                                xtype: 'button',
                                hidden: true,
                                ui: 'action',
                                itemId: 'btn-release',
                                action: 'releaseTask',
                                taskRecord: me.taskRecord
                            },
                            {
                                text: Uni.I18n.translate('task.action', 'BPM', 'Start'),
                                xtype: 'button',
                                hidden: true,
                                ui: 'action',
                                itemId: 'btn-start',
                                action: 'startTask',
                                taskRecord: me.taskRecord
                            },
                            {
                                text: Uni.I18n.translate('task.action.complete', 'BPM', 'Complete'),
                                xtype: 'button',
                                hidden: true,
                                ui: 'action',
                                itemId: 'btn-complete',
                                action: 'completeTask',
                                taskRecord: me.taskRecord
                            },
                            {
                                text: Uni.I18n.translate('task.action.taskactions', 'BPM', 'Task actions'),
                                xtype: 'button',
                                hidden: true,
                                ui: 'action',
                                itemId: 'btn-taskactions',
                                action: 'taskaction',
                                taskRecord: me.taskRecord
                            }/*,
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('general.cancel', 'BPM', 'Cancel'),
                                href: '#/administration/taksmanagementtasks',
                                itemId: 'btn-cancel-link',
                                ui: 'link'
                            }*/
                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
    //    me.setEdit(me.edit, me.returnLink);
    }
});


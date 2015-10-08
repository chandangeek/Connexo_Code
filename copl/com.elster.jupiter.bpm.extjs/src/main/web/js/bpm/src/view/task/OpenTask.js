Ext.define('Bpm.view.task.OpenTask', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.bpm-task-open-task',
    requires: [
//        'Uni.util.FormErrorMessage',
        //      'Usr.store.SecurityProtocols'
    ],

    edit: false,
    setEdit: function (edit, returnLink) {
        /*    if (edit) {
         this.edit = edit;
         this.down('#btn-add').setText(Uni.I18n.translate('general.save', 'USR', 'Save'));
         this.down('#btn-add').action = 'edit';
         } else {
         this.edit = edit;
         this.down('#btn-add').setText(Uni.I18n.translate('general.add', 'USR', 'Add'));
         this.down('#btn-add').action = 'add';
         }
         */
        this.down('#btn-cancel-link').href = returnLink;
    },

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                itemId: 'frm-add-user-directory',
                ui: 'large',
                items: [
                    {
                        xtype: 'container',
                        padding: 10,
                        border: 1,
                        style: {
                            borderColor: 'lightgray',
                            borderStyle: 'solid'
                        },
                        height: 400,
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        items: [
                            {
                                xtype: 'container',
                                itemId: 'formContent',
                                height: 390
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        margin: '0 0 0 0',
                        layout: 'hbox',
                        items: [
                            {
                                text: Uni.I18n.translate('task.action.claim', 'BPM', 'Claim'),
                                xtype: 'button',
                                hidden: true,
                                ui: 'action',
                                itemId: 'btn-claim'
                            },
                            {
                                text: Uni.I18n.translate('task.action.save', 'BPM', 'Save'),
                                xtype: 'button',
                                hidden: true,
                                ui: 'action',
                                itemId: 'btn-save'
                            },
                            {
                                text: Uni.I18n.translate('task.action.release', 'BPM', 'Release'),
                                xtype: 'button',
                                hidden: true,
                                ui: 'action',
                                itemId: 'btn-release'
                            },
                            {
                                text: Uni.I18n.translate('task.action', 'BPM', 'Start'),
                                xtype: 'button',
                                hidden: true,
                                ui: 'action',
                                itemId: 'btn-start'
                            },
                            {
                                text: Uni.I18n.translate('task.action.complete', 'BPM', 'Complete'),
                                xtype: 'button',
                                hidden: true,
                                ui: 'action',
                                itemId: 'btn-complete'
                            },
                            {
                                text: Uni.I18n.translate('task.action.taskactions', 'BPM', 'Task actions'),
                                xtype: 'button',
                                hidden: true,
                                ui: 'action',
                                itemId: 'btn-taskactions'
                            },
                            {
                                xtype: 'button',
                                text: Uni.I18n.translate('general.cancel', 'BPM', 'Cancel'),
                                href: '#/administration/taksmanagementtasks',
                                itemId: 'btn-cancel-link',
                                ui: 'link'
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
        me.setEdit(me.edit, me.returnLink);
    }
});


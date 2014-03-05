Ext.define('Mtr.view.workspace.issues.Close', {
    extend: 'Ext.container.Container',
    requires: [
        'Ext.form.Panel',
        'Ext.form.RadioGroup',
        'Ext.form.field.Hidden',
        'Uni.view.breadcrumb.Trail'
    ],
    alias: 'widget.issues-close',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    overflowY: 'auto',
    items: [
        {
            xtype: 'breadcrumbTrail',
            padding: 6
        }
    ],
    listeners: {
        render: {
            fn: function (self) {
                self.addForm();
            }
        }
    },
    addForm: function () {
        var self = this;
        self.add({
            xtype: 'form',
            flex: 1,
            minHeight: 305,
            sendingData: {
                issues: [
                    {
                        id: self.record.data.id,
                        version: self.record.data.version
//                        version: null
                    }
                ]
            },
            border: false,
            header: false,
            bodyPadding: 10,
            defaults: {
                border: false
            },
            items: [
                {
                    html: '<h3>Close issue '
                        + self.record.data.reason.charAt(0).toLowerCase() + self.record.data.reason.slice(1)
                        + (self.record.data.device ? ' to ' + self.record.data.device.name + ' ' + self.record.data.device.sNumber : '')
                        + '</h3>'
                },
                {
                    xype: 'container',
                    border: 0,
                    padding: '30 50 0 50',
                    defaults: {
                        padding: '0 0 30 0'
                    },
                    items: [
                        {
                            xtype: 'radiogroup',
                            fieldLabel: 'Reason *',
                            name: 'status',
                            columns: 1,
                            vertical: true,
                            submitValue: false,
                            items: [
                                { boxLabel: 'Resolved', name: 'status', inputValue: 'CLOSED', checked: true },
                                { boxLabel: 'Rejected', name: 'status', inputValue: 'REJECTED' }
                            ]
                        },
                        {
                            xtype: 'textarea',
                            fieldLabel: 'Comment',
                            name: 'comment',
                            width: 500,
                            height: 150,
                            emptyText: 'Provide a comment (optionally)'
                        }
                    ]
                },
                {
                    xtype: 'container',
                    padding: '0 155',
                    items: [
                        {
                            xtype: 'button',
                            name: 'close',
                            text: 'Close',
                            formBind: true,
                            disabled: true
                        },
                        {
                            xtype: 'button',
                            name: 'cancel',
                            text: 'Cancel',
                            cls: Ext.baseCSSPrefix + 'btn-plain-toolbar-medium'
                        }
                    ]
                }
            ]
        })
    }
});
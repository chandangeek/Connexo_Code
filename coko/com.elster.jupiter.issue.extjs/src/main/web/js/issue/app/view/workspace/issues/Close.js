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

//    listeners: {
//        render: {
//            fn: function (self) {
//                self.addForm();
//            }
//        }
//    },

    initComponent: function () {
        this.callParent(arguments);
        this.addForm();
     },
    addForm: function () {
        var self = this;
        form_item = {
            xtype: 'form',
            flex: 1,
            border: false,
            header: false,
            defaults: {
                border: false
            },
            items: [
                {
                },
                {
                    xype: 'container',
                    border: 0,
                    margin: '0 0 0 -25',
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
                {}
            ]
        };
        if (Ext.isEmpty(this.bulk)) {
            self.add({
                xtype: 'breadcrumbTrail',
                padding: 6
            });
            form_item.bodyPadding = 10;
            form_item.minHeight = 305;
            form_item.recordTitle = self.record.data.reason + ' ' + (self.record.data.device ? ' to ' + self.record.data.device.name + ' ' + self.record.data.device.serialNumber : '')  
            form_item.sendingData = {
                issues: [
                    {
                        id: self.record.data.id,
                        version: self.record.data.version
//                        version: null
                    }
                ]
            };
            form_item.items[0] = { html: '<h3>Close issue '
                + self.record.data.reason.charAt(0).toLowerCase() + self.record.data.reason.slice(1)
                + (self.record.data.device ? ' to ' + self.record.data.device.name + ' ' + self.record.data.device.sNumber : '')
                + '</h3>'};
            form_item.items[1].padding = '30 50 0 50';
            form_item.items[1].defaults = { padding: '0 0 30 0' },
            form_item.items[1].margin = '0',
            form_item.items[2] = {
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
        }
        self.add(form_item)
    }
});
Ext.define('Isu.view.workspace.issues.Assign', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Ext.form.Panel',
        'Ext.form.RadioGroup',
        'Ext.form.field.Hidden'
    ],
    alias: 'widget.issues-assign',

    initComponent: function () {
        var self = this;

        self.callParent(arguments);
        self.addForm();
    },

    addForm: function () {
        var self = this;

        self.getCenterContainer().add({
            flex: 1,
            minHeight: 305,
            border: false,
            header: false,
            recordTitle: self.record.data.reason + (self.record.data.device ? ' to ' + self.record.data.device.name + ' ' + self.record.data.device.serialNumber : ''),
            bodyPadding: 10,
            defaults: {
                border: false
            },
            items: [
                {
                    html: '<h3 class="isu-assign-text"><span>Assign issue </span><span>' + self.record.data.reason + (self.record.data.device ? ' to ' + self.record.data.device.name + ' ' + self.record.data.device.serialNumber : '') + '</span></h3>',
                    margin: '0 0 20 0'
                },
                {
                    xtype: 'issues-assign-form'
                },
                {
                    layout: 'hbox',
                    margin: '20 0 0 174',
                    defaults: {
                        xtype: 'button',
                        margin: '0 10 0 0'
                    },
                    items: [
                        {
                            text: 'Assign',
                            name: 'assign',
                            formBind: false
                        },
                        {
                            text: 'Cancel',
                            name: 'cancel',
                            cls: 'isu-btn-link',
                            hrefTarget: '',
                            href: '#/workspace/datacollection/issues'
                        }
                    ]
                }
            ]
        });
    }
});
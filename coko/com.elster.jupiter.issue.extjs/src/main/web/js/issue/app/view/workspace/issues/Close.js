Ext.define('Isu.view.workspace.issues.Close', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.issues-close',

    requires: [
       'Isu.view.workspace.issues.CloseForm'
    ],

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
            recordTitle: self.record.data.reason.name + ' ' + (self.record.data.device ? ' to ' + self.record.data.device.name + ' ' + self.record.data.device.serialNumber : ''),
            bodyPadding: 10,
            defaults: {
                border: false
            },
            items: [
                {
                    html: '<h3 class="isu-assign-text"><span>Close issue </span><span>'
                    + (self.record.data.reason ? self.record.data.reason.name : '')
                    + (self.record.data.device ? ' to ' + self.record.data.device.name + ' ' + self.record.data.device.serialNumber : '')
                    + '</span></h3>'
                },
                {
                    itemId: 'close-form',
                    xtype: 'issues-close-form',
                    padding: '30 50 0 50',
                    margin: '0',
                    defaults: {
                        padding: '0 0 30 0'
                    }

                },
                {
                    xtype: 'container',
                    padding: '0 155',
                    defaults: {
                        xtype: 'button',
                        margin: '0 10 0 0'
                    },
                    items: [
                        {   itemId: '#Close',
                            name: 'close',
                            text: 'Close',
                            formBind: true
                        },
                        {
                            itemId: '#Cancel',
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
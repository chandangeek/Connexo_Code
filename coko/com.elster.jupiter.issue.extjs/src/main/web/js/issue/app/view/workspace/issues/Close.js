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
        console.info(this.record);
        var self = this,
            name = self.record.get('reason_name') || '',
            deviceName = self.record.get('device_name') || '',
            deviceSerialNumber = (self.record.get('device') ? self.record.get('device'.serialNumber) : null) || '';

        self.title = 'Close issue ' + name + ((deviceName || deviceSerialNumber) ? ' to' : '') + deviceName + ' ' + deviceSerialNumber;

        self.getCenterContainer().add({
            flex: 1,
            minHeight: 305,
            border: false,
            header: false,
            recordTitle: self.title,
            bodyPadding: 10,
            defaults: {
                border: false
            },
            items: [
                {
                    xtype: 'panel',
                    ui: 'medium',
                    title: self.title

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
                        {
                            itemId: '#Close',
                            name: 'close',
                            text: 'Close',
                            formBind: true
                        },
                        {
                            itemId: '#Cancel',
                            text: 'Cancel',
                            name: 'cancel',
                            ui: 'link',
                            hrefTarget: '',
                            href: '#/workspace/datacollection/issues'
                        }
                    ]
                }
            ]
        });
    }
});
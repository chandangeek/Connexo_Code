Ext.define('Mdc.view.setup.logbooktype.Item', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.logbook-item',
    height: 310,

    initComponent: function () {
        var self = this;

        self.callParent();
        self.addEvents('change');
        self.addEvents('afterChange');
        self.on('change', self.onChange, self);
    },

    onChange: function (panel, record) {
        var self = this;

        self.removeAll();
        self.add(self.getItems(record));
        self.fireEvent('afterChange', self);
    },

    getItems: function (record) {
        return {
            frame: true,
            tools: [
                {
                    xtype: 'button',
                    text: 'Actions',
                    iconCls: 'x-uni-action-iconA',
                    menu: {
                        xtype: 'logbook-action-menu',
                        logBookId: record.getData().id
                    }
                }
            ],
            items: [
                {
                    xtype: 'form',
                    name: 'logbookDetails',
                    layout: 'column',
                    defaults: {
                        xtype: 'container',
                        layout: 'form',
                        columnWidth: 0.5
                    },
                    items: [
                        {
                            items: [
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: 'Name',
                                    name: 'name'
                                }
                            ]
                        },
                        {
                            items: [
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: 'OBIS code',
                                    name: 'obis'
                                }
                            ]
                        }
                    ]
                }
            ]
        };
    }
});

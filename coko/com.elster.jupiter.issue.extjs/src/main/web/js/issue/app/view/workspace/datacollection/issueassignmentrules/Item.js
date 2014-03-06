Ext.define('Mtr.view.workspace.datacollection.issueassignmentrules.Item', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Mtr.view.ext.button.ItemAction',
        'Mtr.view.workspace.datacollection.issueassignmentrules.ActionMenu'
    ],
    alias: 'widget.issues-assignment-rules-item',
    height: 150,

    initComponent: function () {
        var self = this;

        self.callParent();
        self.addEvents('change');
        self.on('change', self.onChange, self);
        self.fireEvent('change', self);
    },

    onChange: function (panel, record) {
        var self = this;

        self.removeAll();
        self.add(self.getItems(record));
    },

    getItems: function (record) {
        if (!record) {
            return {
                html: '<h3>No rule selected</h3><p>Select a rule to view its detail.</p>'
            }
        }

        return {
            border: false,
            defaults: {
                border: false
            },
            items: [
                {
                    xtype: 'toolbar',
                    ui: 'footer',
                    layout: {
                        type: 'hbox',
                        pack: 'end'
                    },
                    items: [
                        {
                            xtype: 'item-action',
                            menu: {
                                xtype: 'rule-action-menu',
                                record: record
                            }
                        }
                    ]
                },
                {
                    bodyPadding: '20 10 0',
                    data: record.data,
                    tpl: new Ext.XTemplate(
                        '<table class="isu-assign-rules-item-data-table">',
                        '<tr>',
                        '<td>Priority:</td>',
                        '<td>{priority}</td>',
                        '</tr>',
                        '<tr>',
                        '<td>Status:</td>',
                        '<td>{status}</td>',
                        '</tr>',
                        '<tr>',
                        '<td>Name:</td>',
                        '<td>{name}</td>',
                        '</tr>',
                        '<tr>',
                        '<td>When:</td>',
                        '<td></td>',
                        '</tr>',
                        '<tr>',
                        '<td>Then:</td>',
                        '<td>Assign to {assignee.title}</td>',
                        '</tr>',
                        '</table>'
                    )
                }
            ]
        };
    }
});
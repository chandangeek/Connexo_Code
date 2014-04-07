Ext.define('Isu.view.administration.datacollection.issuecreationrules.Item', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.ext.button.ItemAction',
        'Isu.view.administration.datacollection.issuecreationrules.ActionMenu'
    ],
    alias: 'widget.issue-creation-rules-item',
    height: 150,

    initComponent: function () {
        var self = this;

        self.callParent();
        self.addEvents('change');
        self.on('change', self.onChange, self);
    },

    onChange: function (panel, record) {
        var self = this;

        self.removeAll();
        self.add(self.getItems(record));
    },

    getItems: function (record) {

        return {
            border: false,
            defaults: {
                border: false
            },
            items: [
                {
                    xtype: 'toolbar',
                    padding: 10,
                    ui: 'footer',
                    items: [
                        {
                            xtype: 'container',
                            flex: 1,
                            html: '<h3>' + record.data.name + '</h3>'
                        },
                        {
                            xtype: 'item-action',
                            menu: {
                                xtype: 'creation-rule-action-menu',
                                issueId: record.data.id
                            }
                        }
                    ]
                },
                {
                    bodyPadding: '20 10 0',
                    data: record.raw,
                    tpl: new Ext.XTemplate(
                        '<table class="isu-item-data-table">',
                        '<tr>',
                        '<td><b>Issue type:</b></td>',
                        '<td><b><tpl if="type">{type.name}</tpl></b></td>',
                        '<td><b>Due in:</b></td>',
                        '<td><tpl if="duein">{duein.number} {duein.type}</tpl></td>',
                        '</tr>',
                        '<tr>',
                        '<td><b>Rule template:</b></td>',
                        '<td><tpl if="template">{template.name}</tpl></td>',
                        '<td><b>Created:</b></td>',
                        '<td>{[values.creationdate ? this.formatRuleDate(values.creationdate) : ""]}</td>',
                        '</tr>',
                        '<tr>',
                        '<td><b>Issue reason:</b></td>',
                        '<td><tpl if="reason">{reason.name}</tpl></td>',
                        '<td><b>Last modified:</b></td>',
                        '<td>{[values.modificationdate ? this.formatRuleDate(values.modificationdate) : ""]}</td>',
                        '</tr>',
                        '</table>',
                        {
                            formatRuleDate: function (date) {
                                return Ext.Date.format(date, 'M d, Y H:i');
                            }
                        }
                    )
                }
            ]
        };
    }
});
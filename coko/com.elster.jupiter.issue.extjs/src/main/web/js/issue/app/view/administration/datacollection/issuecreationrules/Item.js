Ext.define('Isu.view.administration.datacollection.issuecreationrules.Item', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.ext.button.ItemAction',
        'Isu.view.administration.datacollection.issuecreationrules.ActionMenu'
    ],
    alias: 'widget.issue-creation-rules-item',
    height: 230,

    initComponent: function () {
        var self = this;

        self.callParent();
        self.addEvents('change');
        self.on('change', self.onChange, self);
        self.on('clear', self.onClear, self);
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
                    data: record.data,
                    tpl: new Ext.XTemplate(
                        '<table class="isu-item-data-table">',
                        '<tr>',
                        '<td><b>Name</b></td>',
                        '<td><tpl if="name">{name}</tpl></td>',
                        '<td><b>Created</b></td>',
                        '<td>{[values.creationDate ? this.formatRuleDate(values.creationDate) : ""]}</td>',
                        '</tr>',
                        '<tr>',
                        '<td><b>Rule template</b></td>',
                        '<td><tpl if="template">{template.name}</tpl></td>',
                        '<td><b>Last modified</b></td>',
                        '<td>{[values.modificationDate ? this.formatRuleDate(values.modificationDate) : ""]}</td>',
                        '</tr>',
                        '<tr>',
                        '<td><b>Issue type</b></td>',
                        '<td colspan="3"><tpl if="issueType">{issueType.name}</tpl></td>',
                        '</tr>',
                        '<tr>',
                        '<td><b>Issue reason</b></td>',
                        '<td colspan="3"><tpl if="reason">{reason.name}</tpl></td>',
                        '</tr>',
                        '<tr>',
                        '<td><b>Due in</b></td>',
                        '<td colspan="3"><tpl if="dueIn && dueIn.number">{dueIn.number} {dueIn.type}</tpl></td>',
                        '</tr>',
                        '</table>',
                        {
                            formatRuleDate: function (date) {
                                date = Ext.isDate(date) ? date : new Date(date);
                                return Ext.Date.format(date, 'M d, Y H:i');
                            }
                        }
                    )
                }
            ]
        };
    },

    onClear: function (text) {
        this.removeAll();
    }
});
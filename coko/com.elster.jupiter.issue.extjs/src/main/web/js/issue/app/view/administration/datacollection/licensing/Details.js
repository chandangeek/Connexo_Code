Ext.define('Isu.view.administration.datacollection.licensing.Details', {
    extend: 'Ext.panel.Panel',

    alias: 'widget.licensing-details',
    height: 500,

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
            frame: true,
            items: [
                {
                    xtype: 'toolbar',
                    padding: 10,
                    ui: 'footer',
                    items: [
                        {
                            xtype: 'container',
                            flex: 1,
                            html: record.data.application + ' license details'
                        },
                        {
                            xtype: 'item-action',
                            menu: {
                                xtype: 'licensing-action-menu',
                                issueId: record.data.id
                            }
                        }
                    ]
                },
                {
                    data: record.data,
                    bodyPadding: '20 40',
                    tpl: new Ext.XTemplate(
                        '<table class="isu-item-data-table">',
                        '<tr>',
                        '<td><b>Application</b></td>',
                        '<td><tpl if="application">{application}</tpl></td>',
                        '<td><b>Status</b></td>',
                        '<td><tpl if="status">{status}</tpl></td>',
                        '</tr>',
                        '<tr>',
                        '<td><b>Type</b></td>',
                        '<td><tpl if="type">{type} </tpl></td>',
                        '<td><b>Valid from</b></td>',
                        '<td>{[values.validfrom ? this.func(values.validfrom) : ""]}</td>',
                        '</tr>',
                        '<tr>',
                        '<td><b>Description</b></td>',
                        '<td><tpl if="description">{description} </tpl></td>',
                        '<td><tpl if="graceperiod"><b>Grace period</b></tpl></td>',
                        '<td><tpl if="graceperiod">{graceperiod}<span> days</span></tpl></td>',
                        '</tr>',
                        '</table>',
                        {
                            func: function (date) {
                                return Ext.Date.format(date, 'M d Y');
                            }
                        }
                    )
                },
                {
                    xtype: 'label',
                    html: '<hr>'
                },
                {
                    title: 'Licensed items',
                    frame: true,
                    margin: '20 20 20 20',
                    items: [
                        {
                            bodyPadding: '10 10 0 0',
                            data: record.data,
                            tpl: new Ext.XTemplate(
                                '<table class="isu-item-data-table">',
                                '<tpl foreach="content">',
                                '<tr>',
                                '<td><b>{key}</b></td>',
                                '<td>{value}</td>',
                                '</tr>',
                                '</tpl>',
                                '</table>'
                            )
                        }
                    ]
                }
            ]
        };
    }
});

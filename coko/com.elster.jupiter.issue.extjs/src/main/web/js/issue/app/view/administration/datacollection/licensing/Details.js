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
            items: [
                {
                    xtype: 'toolbar',
                    padding: 10,
                    ui: 'footer',
                    items: [
                        {
                            xtype: 'container',
                            flex: 1,
                            html: record.data.application
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
                        '<td><b>Activation date</b></td>',
                        '<td>{[values.validfrom ? this.formatActivationDate(values.validfrom) : ""]}</td>',
                        '</tr>',
                        '<tr>',
                        '<td><b>Description</b></td>',
                        '<td><tpl if="description">{description} </tpl></td>',
                        '<td><b>Expiration date</b></td>',
                        '<td>{[values.expires ? this.formatExpirationDate(values.expires) : ""]}</td>',
                        '</tr>',
                        '<tr>',
                        '<td></td>',
                        '<td></td>',
                        '<td><tpl if="graceperiod"><b>Grace period</b></tpl></td>',
                        '<td><tpl if="graceperiod">{graceperiod}<span> days</span></tpl></td>',
                        '</tr>',
                        '</table>',
                        {
                            formatActivationDate: function (date) {
                                return Ext.Date.format(date, 'd-m-Y');
                            },
                            formatExpirationDate: function (date) {
                                return Ext.Date.format(new Date(parseInt(date)), 'd-m-Y');
                            }
                        }
                    )
                },
                {
                    title: '<span class="license-title"><b>License coverage</b></span>',
                    ui: 'plain',
                    margin: '20 20 20 40',
                    items: [
                        {
                            bodyPadding: '20 10 0 0',
                            data: record.data,
                            tpl: new Ext.XTemplate(
                                '<table class="isu-item-data-table">',
                                '<tpl foreach="content">',
                                '<tr>',
                                '<td><b><tpl switch="key">',
                                '<tpl case="licensed.device">Number of devices',
                                '<tpl case="licensed.users">Number of users',
                                '<tpl case="licensed.protocols">Number of protocols',
                                '</tpl></b></td>',
                                '<td>{[this.formatValue(values.value)]}</td>',
                                '</tr>',
                                '</tpl>',
                                '</table>',
                                {
                                    formatValue: function (value) {
                                        var regexp = /,/g;
                                        return value.replace(regexp, '<br>');
                                    }
                                }
                            )
                        }
                    ]
                }
            ]
        };
    }
});

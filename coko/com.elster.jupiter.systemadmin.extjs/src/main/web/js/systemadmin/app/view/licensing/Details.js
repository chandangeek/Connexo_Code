Ext.define('Sam.view.licensing.Details', {
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
                    cls: 'license-details-toolbar',
                    ui: 'footer',
                    items: [
                        {
                            xtype: 'container',
                            flex: 1,
                            html: record.data.applicationname
                        }
                    ]
                },
                {
                    data: record.data,
                    ui: 'medium',
                    bodyPadding: '20 40',
                    tpl: new Ext.XTemplate(
                        '<table class="isu-item-data-table">',
                        '<tr>',
                        '<td><b>Application</b></td>',
                        '<td><tpl if="applicationname">{applicationname} </tpl></td>',
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
                        '<td>{[values.expires ? this.formatActivationDate(values.expires) : ""]}</td>',
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
                            }
                        }
                    )
                },
                {
                    title: '<span class="license-title"><b>License coverage</b></span>',
                    ui: 'plain',
                    cls: 'license-details-coverage',
                    items: [
                        {
                            bodyPadding: '20 10 0 0',
                            data: record.data,
                            tpl: new Ext.XTemplate(
                                '<table class="isu-item-data-table">',
                                '<tpl foreach="content">',
                                '<tr>',
                                '<td><b>{[this.translateKey(values.key)]}</b></td>',
                                '<td>{[this.formatValue(values.value)]}</td>',
                                '</tr>',
                                '</tpl>',
                                '</table>',
                                {
                                    translateKey: function (value) {
                                        return Uni.I18n.translate(value, 'SAM', value);
                                    },
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


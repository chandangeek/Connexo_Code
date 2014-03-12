Ext.define('Isu.view.workspace.issues.Item', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.ext.button.ItemAction',
        'Isu.view.workspace.issues.ActionMenu'
    ],
    alias: 'widget.issues-item',
    height: 310,
 /*   items: [
        {
            html: '<h3>No issue selected</h3><p>Select an issue to view its detail.</p>',
            bodyPadding: 10,
            border: false
        }
    ], */

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
                            html: record.data.reason + (record.data.device ? ' to ' + record.data.device.name + ' ' + record.data.device.serialNumber : '')
                        },
                        {
                            xtype: 'item-action',
                            menu: {
                                xtype: 'issue-action-menu',
                                record: record
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
                        '<td><b>Reason:</b></td>',
                        '<td><tpl if="reason">{reason}</tpl></td>',
                        '<td><b>Status:</b></td>',
                        '<td><tpl if="status">{status.name} <span class="isu-icon-filter"></span></tpl></td>',
                        '</tr>',
                        '<tr>',
                        '<td><b>Customer:</b></td>',
                        '<td><tpl if="customer">{customer} <span class="isu-icon-filter"></span></tpl></td>',
                        '<td><b>Due date:</b></td>',
                        '<td>{[values.dueDate ? this.formatDueDate(values.dueDate) : ""]}</td>',
                        '</tr>',
                        '<tr>',
                        '<td><b>Location:</b></td>',
                        '<td><tpl if="device.serviceLocation">{[values.device.serviceLocation ? values.device.serviceLocation.info : ""]}<span class="isu-icon-filter"></span></tpl></td>',
                        '<td><b>Assignee:</b></td>',
                        '<td><tpl if="assignee">{assignee.name}<tpl else>None</tpl> <span class="isu-icon-filter"></span></td>',
                        '</tr>',
                        '<tr>',
                        '<td><b>Usage point:</b></td>',
                        '<td><tpl if="device.usagePoint">{[values.device.usagePoint ? values.device.usagePoint.info : ""]}<span class="isu-icon-filter"></span></tpl></td>',
                        '<td><b>Creation date:</b></td>',
                        '<td>{[values.creationDate ? this.formatCreationDate(values.creationDate) : ""]}</td>',
                        '</tr>',
                        '<tr>',
                        '<td><b>Device:</b></td>',
                        '<td colspan="3"><tpl if="device">{device.name} {device.serialNumber} <span class="isu-icon-filter"></span></tpl></td>',
                        '</tr>',
                        '<tr>',
                        '<td><b>Service category:</b></td>',
                        '<td colspan="3"><tpl if="device">{[values.device.serviceCategory ? values.device.serviceCategory.info : ""]}</tpl></td>',
                        '</tr>',
                        '</table>',
                        '<div class="isu-item-details-bottom"><a href="javascript:void(0)">View details</a></div>',
                        {
                            formatCreationDate: function (date) {
                                return Ext.Date.format(date, 'M d, Y h:m');
                            }
                        },
                        {
                            formatDueDate: function (date) {
                                return Ext.Date.format(date, 'M d, Y');
                            }
                        }
                    )
                }
            ]
        };
    }
});
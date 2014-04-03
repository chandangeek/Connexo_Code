Ext.define('Isu.view.workspace.issues.Item', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Isu.view.ext.button.ItemAction',
        'Isu.view.workspace.issues.ActionMenu'
    ],
    alias: 'widget.issues-item',
    height: 310,

    initComponent: function () {
        var self = this;

        self.callParent();
        self.addEvents('change');
        self.addEvents('afterChange');
        self.on('change', self.onChange, self);
        self.on('clear', self.onClear, self);
    },

    onChange: function (panel, record) {
        var self = this;

        self.removeAll();
        self.add(self.getItems(record));
        self.fireEvent('afterChange', self);
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
                            html: record.data.reason.name + (record.data.device ? ' to ' + record.data.device.name + ' ' + record.data.device.serialNumber : '')
                        },
                        {
                            xtype: 'item-action',
                            menu: {
                                xtype: 'issue-action-menu',
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
                        '<td><b>Reason:</b></td>',
                        '<td><tpl if="reason"><span>{reason.name}</span> <span class="isu-icon-filter isu-apply-filter" data-filterType="reason" data-filterValue="{reason.id}"></span></tpl></td>',
                        '<td><b>Status:</b></td>',
                        '<td><tpl if="status"><span>{status.name}</span> <span class="isu-icon-filter isu-apply-filter" data-filterType="status" data-filterValue="{status.id}"></span></tpl></td>',
                        '</tr>',
                        '<tr>',
                        '<td><b>Customer:</b></td>',
                        '<td><tpl if="customer">{customer} <span class="isu-icon-filter"></span></tpl></td>',
                        '<td><b>Due date:</b></td>',
                        '<td>{[values.dueDate ? this.formatDueDate(values.dueDate) : ""]}</td>',
                        '</tr>',
                        '<tr>',
                        '<td><b>Location:</b></td>',
                        '<td><tpl if="device.serviceLocation">{[values.device.serviceLocation ? values.device.serviceLocation.info : ""]}</tpl></td>',
                        '<td><b>Assignee:</b></td>',
                        '<td><tpl if="assignee"><span>{assignee.name}</span> <span class="isu-icon-filter isu-apply-filter" data-filterType="assignee" data-filterValue="{assignee.id}:{assignee.type}"></span><tpl else>None</tpl></td>',
                        '</tr>',
                        '<tr>',
                        '<td><b>Usage point:</b></td>',
                        '<td><tpl if="device.usagePoint">{[values.device.usagePoint ? values.device.usagePoint.info : ""]}</tpl></td>',
                        '<td><b>Creation date:</b></td>',
                        '<td>{[values.creationDate ? this.formatCreationDate(values.creationDate) : ""]}</td>',
                        '</tr>',
                        '<tr>',
                        '<td><b>Device:</b></td>',
                        '<td colspan="3"><tpl if="device"><span>{device.name} {device.serialNumber}</span> <span class="isu-icon-filter isu-apply-filter" data-filterType="meter" data-filterValue="{device.id}"></span></tpl></td>',
                        '</tr>',
                        '<tr>',
                        '<td><b>Service category:</b></td>',
                        '<td colspan="3"><tpl if="device">{[values.device.serviceCategory ? values.device.serviceCategory.info : ""]}</tpl></td>',
                        '</tr>',
                        '</table>',
                        '<div class="isu-item-details-bottom"><a href="#/workspace/datacollection/issues/{id}">View details</a></div>',
                        {
                            formatCreationDate: function (date) {
                                return Ext.Date.format(date, 'M d, Y H:i');
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
    },

    onClear: function (text) {
        this.removeAll();
        this.add({
            html: text ? text : '<h3>No issue selected</h3><p>Select an issue to view its detail.</p>',
            bodyPadding: 10,
            border: false
        });
    }
});
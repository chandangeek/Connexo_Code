Ext.define('Isu.view.workspace.issues.Detail', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.issue-detail',
    tpl: new Ext.XTemplate(
        '<h1>{reason.name}<tpl if="device"> to {device.serialNumber}</tpl></h1>',
        '<h3 class="isu-subheader">Details</h3>',
        '<table class="isu-item-data-table">',
        '<tr>',
        '<td><b>Reason:</b></td>',
        '<td><tpl if="reason">{reason.name}</tpl></td>',
        '<td><b>Status:</b></td>',
        '<td><tpl if="status">{status.name}</tpl></td>',
        '</tr>',
        '<tr>',
        '<td><b>Customer:</b></td>',
        '<td><tpl if="customer">{customer}</tpl></td>',
        '<td><b>Due date:</b></td>',
        '<td>{[values.dueDate ? this.formatDueDate(values.dueDate) : ""]}</td>',
        '</tr>',
        '<tr>',
        '<td><b>Location:</b></td>',
        '<td><tpl if="device.serviceLocation">{[values.device.serviceLocation ? values.device.serviceLocation.info : ""]}</tpl></td>',
        '<td><b>Assignee:</b></td>',
        '<td><tpl if="assignee">{assignee.name}<tpl else>None</tpl></td>',
        '</tr>',
        '<tr>',
        '<td><b>Usage point:</b></td>',
        '<td><tpl if="device.usagePoint">{[values.device.usagePoint ? values.device.usagePoint.info : ""]}</tpl></td>',
        '<td><b>Creation date:</b></td>',
        '<td>{[values.creationDate ? this.formatCreationDate(values.creationDate) : ""]}</td>',
        '</tr>',
        '<tr>',
        '<td><b>Device:</b></td>',
        '<td colspan="3"><tpl if="device">{device.serialNumber}</tpl></td>',
        '</tr>',
        '<tr>',
        '<td><b>Service category:</b></td>',
        '<td colspan="3"><tpl if="device">{[values.device.serviceCategory ? values.device.serviceCategory.info : ""]}</tpl></td>',
        '</tr>',
        '</table>',
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
});
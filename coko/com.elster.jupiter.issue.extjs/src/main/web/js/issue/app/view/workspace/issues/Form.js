Ext.define('Isu.view.workspace.issues.Form', {
    extend: 'Ext.form.Panel',
    alias: 'widget.issue-form',
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
                    fieldLabel: 'Reason',
                    name: 'reason_name'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: 'Customer',
                    name: 'customer'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: 'Location',
                    name: 'service_location'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: 'Usage point',
                    name: 'usage_point'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: 'Device',
                    name: 'device_name'
                }
            ]
        },
        {
            items: [
                {
                    xtype: 'displayfield',
                    fieldLabel: 'Status',
                    name: 'status_name'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: 'Due date',
                    name: 'dueDate',
                    renderer: Ext.util.Format.dateRenderer('M d, Y')
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: 'Assignee',
                    name: 'assignee_name'
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: 'Creation date',
                    name: 'creationDate',
                    renderer: Ext.util.Format.dateRenderer('M d, Y H:i')
                },
                {
                    xtype: 'displayfield',
                    fieldLabel: 'Service category',
                    name: 'service_category'
                }
            ]
        }
    ],
    // todo: animate button
    buttons: [{
        text: 'View details',
        action: 'view'
    }]
});
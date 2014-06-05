Ext.define('Isu.view.workspace.issues.FormWithFilters', {
    extend: 'Ext.form.Panel',
    alias: 'widget.issue-form-with-filters',
    layout: 'column',
    itemId: 'issue-detailed-form',
    defaults: {
        xtype: 'container',
        layout: 'form',
        columnWidth: 0.5
    },
    ui: 'medium',

    items: [
        {
            items: [
                {
                    itemId: '_reason',
                    xtype: 'displayfield',
                    fieldLabel: 'Reason',
                    name: 'reason_name_f'
                },
                {
                    itemId: '_customer',
                    xtype: 'displayfield',
                    fieldLabel: 'Customer',
                    name: 'customer'
                },
                {
                    itemId: '_location',
                    xtype: 'displayfield',
                    fieldLabel: 'Location',
                    name: 'service_location'
                },
                {
                    itemId: '_usagepoint',
                    xtype: 'displayfield',
                    fieldLabel: 'Usage point',
                    name: 'usage_point'
                },
                {
                    itemId: '_devicename',
                    xtype: 'displayfield',
                    fieldLabel: 'Device',
                    name: 'device_f'
                }
            ]
        },
        {
            items: [
                {
                    itemId: '_status',
                    xtype: 'displayfield',
                    fieldLabel: 'Status',
                    name: 'status_name_f'
                },
                {
                    itemId: '_dueDate',
                    xtype: 'displayfield',
                    fieldLabel: 'Due date',
                    name: 'dueDate',
                    renderer: Ext.util.Format.dateRenderer('M d, Y')
                },
                {
                    itemId: '_assignee',
                    xtype: 'displayfield',
                    fieldLabel: 'Assignee',
                    name: 'assignee_name_f'
                },
                {
                    itemId: '_creationDate',
                    xtype: 'displayfield',
                    fieldLabel: 'Creation date',
                    name: 'creationDate',
                    renderer: Ext.util.Format.dateRenderer('M d, Y H:i')
                },
                {
                    itemId: '_serviceCat',
                    xtype: 'displayfield',
                    fieldLabel: 'Service category',
                    name: 'service_category'
                }
            ]
        }
    ]
});
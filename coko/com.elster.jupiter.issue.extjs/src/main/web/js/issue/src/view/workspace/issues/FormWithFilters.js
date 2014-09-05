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
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200
            },
            items: [
                {
                    itemId: '_reason',
                    fieldLabel: Uni.I18n.translate('general.title.reason', 'ISE', 'Reason'),
                    name: 'reason_name_f'
                },
                {
                    itemId: '_customer',
                    fieldLabel: Uni.I18n.translate('general.title.customer', 'ISE', 'Customer'),
                    name: 'customer'
                },
                {
                    itemId: '_location',
                    fieldLabel: Uni.I18n.translate('general.title.location', 'ISE', 'Location'),
                    name: 'service_location'
                },
                {
                    itemId: '_usagepoint',
                    fieldLabel: Uni.I18n.translate('general.title.usagePoint', 'ISE', 'Usage point'),
                    name: 'usage_point'
                },
                {
                    itemId: '_devicename',
                    fieldLabel: Uni.I18n.translate('general.title.device', 'ISE', 'Device'),
                    name: 'device_f'
                }
            ]
        },
        {
            defaults: {
                xtype: 'displayfield',
                labelWidth: 200
            },
            items: [
                {
                    itemId: '_status',
                    fieldLabel: Uni.I18n.translate('general.title.status', 'ISE', 'Status'),
                    name: 'status_name_f'
                },
                {
                    itemId: '_dueDate',
                    fieldLabel: Uni.I18n.translate('general.title.dueDate', 'ISE', 'Due date'),
                    name: 'dueDate',
                    renderer: Ext.util.Format.dateRenderer('M d, Y')
                },
                {
                    itemId: '_assignee',
                    fieldLabel: Uni.I18n.translate('general.title.assignee', 'ISE', 'Assignee'),
                    name: 'assignee_name_f'
                },
                {
                    itemId: '_creationDate',
                    fieldLabel: Uni.I18n.translate('general.title.creationDate', 'ISE', 'Creation date'),
                    name: 'creationDate',
                    renderer: Ext.util.Format.dateRenderer('M d, Y H:i')
                },
                {
                    itemId: '_serviceCat',
                    fieldLabel: Uni.I18n.translate('general.title.serviceCategory', 'ISE', 'Service category'),
                    name: 'service_category'
                }
            ]
        }
    ]
});
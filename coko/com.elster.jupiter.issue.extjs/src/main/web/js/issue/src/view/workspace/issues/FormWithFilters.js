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
                    fieldLabel: Uni.I18n.translate('general.title.reason', 'ISU', 'Reason'),
                    name: 'reason_name_f'
                },
                {
                    itemId: '_customer',
                    fieldLabel: Uni.I18n.translate('general.title.customer', 'ISU', 'Customer'),
                    name: 'customer'
                },
                {
                    itemId: '_location',
                    fieldLabel: Uni.I18n.translate('general.title.location', 'ISU', 'Location'),
                    name: 'service_location'
                },
                {
                    itemId: '_usagepoint',
                    fieldLabel: Uni.I18n.translate('general.title.usagePoint', 'ISU', 'Usage point'),
                    name: 'usage_point'
                },
                {
                    itemId: '_devicename',
                    fieldLabel: Uni.I18n.translate('general.title.device', 'ISU', 'Device'),
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
                    fieldLabel: Uni.I18n.translate('general.title.status', 'ISU', 'Status'),
                    name: 'status_name_f'
                },
                {
                    itemId: '_dueDate',
                    fieldLabel: Uni.I18n.translate('general.title.dueDate', 'ISU', 'Due date'),
                    name: 'dueDate',
                    renderer: Ext.util.Format.dateRenderer('M d, Y')
                },
                {
                    itemId: '_assignee',
                    fieldLabel: Uni.I18n.translate('general.title.assignee', 'ISU', 'Assignee'),
                    name: 'assignee_name_f'
                },
                {
                    itemId: '_creationDate',
                    fieldLabel: Uni.I18n.translate('general.title.creationDate', 'ISU', 'Creation date'),
                    name: 'creationDate',
                    renderer: Ext.util.Format.dateRenderer('M d, Y H:i')
                },
                {
                    itemId: '_serviceCat',
                    fieldLabel: Uni.I18n.translate('general.title.serviceCategory', 'ISU', 'Service category'),
                    name: 'service_category'
                }
            ]
        }
    ]
});
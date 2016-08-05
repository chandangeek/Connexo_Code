Ext.define('Mdc.view.setup.searchitems.bulk.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.searchitems-bulk-step2',
    border: false,
    name: 'selectOperation',
    title: Uni.I18n.translate('searchItems.bulk.step2title', 'MDC', 'Bulk action - Step 2 of 5: Select action'),
    ui: 'large',
    items: {
        itemId: 'searchitemsactionselect',
        xtype: 'radiogroup',
        columns: 1,
        vertical: true,
        defaults: {
            name: 'operation',
            submitValue: false,
            padding: '0 0 30 0'
        },
        items: [
            {
                itemId: 'searchitemsaddschedules',
                boxLabel: '<b>' + Uni.I18n.translate('searchItems.bulk.searchItems.bulk.addSchedules', 'MDC', 'Add shared communication schedules') + '</b>',
                name: 'operation',
                inputValue: 'add',
                checked: true
            },
            {
                itemId: 'searchitemschangeconfig',
                boxLabel: '<b>' + Uni.I18n.translate('searchItems.bulk.changeConfig', 'MDC', 'Change device configuration') + '</b>',
                afterSubTpl: '<span class="x-form-cb-label" style="color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('searchItems.bulk.changeConfigMsg', 'MDC', 'This option is only available on devices of the same device configuration. Please first search on devices of the same device configuration to enable this option.') + '</span>',
                name: 'operation',
                inputValue: 'changeconfig',
                privilege: Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceCommunication'])
            },
            {
                itemId: 'searchitemsremoveschedules',
                boxLabel: '<b>' + Uni.I18n.translate('searchItems.bulk.removeSchedules', 'MDC', 'Remove shared communication schedules') + '</b>',
                afterSubTpl: '<span class="x-form-cb-label" style="color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('searchItems.bulk.removeScheduleMsg', 'MDC', 'Shared communication schedule will no longer be visible and used on the selected devices. A record is kept for tracking purposes. This action cannot be undone.') + '</span>',
                name: 'operation',
                inputValue: 'remove'
            }
        ]
    }
});
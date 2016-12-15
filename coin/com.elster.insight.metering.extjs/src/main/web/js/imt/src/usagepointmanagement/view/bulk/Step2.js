Ext.define('Imt.usagepointmanagement.view.bulk.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.usagepoints-bulk-step2',
    border: false,
    name: 'selectOperation',
    title: Uni.I18n.translate('usagepoints.bulk.step2title', 'IMT', 'Step 2: Select action'),
    ui: 'large',
    items: {
        itemId: 'usagepointsactionselect',
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
                itemId: 'usagepointsaddcalendars',
                boxLabel: '<b>' + Uni.I18n.translate('usagepoints.bulk.addCalendars', 'IMT', 'Add calendar') + '</b>',
                name: 'operation',
                inputValue: 'addCalendar',
                afterSubTpl: '<span class="x-form-cb-label" style="color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('usagepoints.bulk.calendargMsg', 'IMT', 'Add calendar to selected usage points') + '</span>',
                privileges: Imt.privileges.UsagePoint.adminCalendars
            }
        ]
    }
});
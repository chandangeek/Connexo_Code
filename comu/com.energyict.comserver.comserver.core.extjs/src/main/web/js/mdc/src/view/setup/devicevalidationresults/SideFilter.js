Ext.define('Mdc.view.setup.devicevalidationresults.SideFilter', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.form.field.DateTime',
        'Mdc.store.ValidationResultsDurations'
    ],
    alias: 'widget.mdc-device-validation-results-side-filter',
    
    ui: 'medium',
    width: 288,
    cls: 'filter-form',
    title: Uni.I18n.translate('connection.widget.sideFilter.title', 'DSH', 'Filter'),
    items: {
        xtype: 'form',
        itemId: 'frm-device-validation-results-filter',
        ui: 'filter',
        items: [
            {
                xtype: 'fieldcontainer',
                itemId: 'fco-date-container',
                fieldLabel: Uni.I18n.translate('deviceloadprofiles.interval', 'MDC', 'Interval'),
                labelAlign: 'top',
                defaults: {
                    width: '100%'
                },
                items: [
                    {
                        xtype: 'date-time',
                        itemId: 'dtm-end-of-interval',
                        name: 'intervalStart',
                        fieldLabel: Uni.I18n.translate('deviceloadprofiles.filter.from', 'MDC', 'From'),
                        labelAlign: 'top',
                        labelStyle: 'font-weight: normal',
                        dateConfig: {
                            format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                        },
                        hoursConfig: {
                            margin: '0 0 0 -10'
                        }
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
				hidden: true,
                itemId: 'fco-item-type',
				name: 'itemTypeContainer',
				fieldLabel: ''                
            }

        ],
        dockedItems: [
            {
                xtype: 'toolbar',
                dock: 'bottom',
                items: [
                    {
                        itemId: 'btn-device-validation-results-filter-apply',
                        ui: 'action',
                        text: Uni.I18n.translate('general.apply', 'MDC', 'Apply'),
                        action: 'applyfilter'
                    },
                    {
                        itemId: 'btn-device-validation-results-filter-reset',
                        text: Uni.I18n.translate('general.reset', 'MDC', 'Reset'),
                        action: 'resetfilter'
                    }
                ]
            }
        ]
    }
});
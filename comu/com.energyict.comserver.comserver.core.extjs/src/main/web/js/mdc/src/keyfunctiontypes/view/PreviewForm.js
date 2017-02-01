Ext.define('Mdc.keyfunctiontypes.view.PreviewForm', {
    extend: 'Ext.panel.Panel',
    frame: false,
    alias: 'widget.devicetype-key-function-types-preview-form',
    layout: 'fit',

    items: {
        xtype: 'form',
        layout: 'column',
        defaults: {
            xtype: 'container',
            layout: 'form',
            columnWidth: 0.5
        },
        items: [
            {
                defaults: {
                    xtype: 'displayfield',
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                        name: 'name'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.description', 'MDC', 'Description'),
                        name: 'description'
                    },
                    {
                        fieldLabel: Uni.I18n.translate('general.keyType', 'MDC', 'Key type'),
                        name: 'keyType'
                    }
                ]
            },
            {
                defaults: {
                    xtype: 'displayfield',
                },
                items: [
                    {
                        fieldLabel: Uni.I18n.translate('general.validityPeriod', 'MDC', 'Validity period'),
                        name: 'validityPeriod',
                        renderer: function (val) {
                            val ? val = val.count + ' ' + val.timeUnit : '-';
                            return val;
                        }
                    },
                    //{
                    //    fieldLabel: Uni.I18n.translate('keyfunctiontypes.viewPrivileges', 'MDC', 'View privileges'),
                    //},
                    //{
                    //    fieldLabel: Uni.I18n.translate('keyfunctiontypes.edotovomeges', 'MDC', 'Edit privileges'),
                    //}
                ]
            }
        ]
    }
});
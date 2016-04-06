Ext.define('Imt.usagepointmanagement.view.forms.attributes.TechnicalAttributesFormGas', {
    extend: 'Imt.usagepointmanagement.view.forms.attributes.ViewEditForm',
    alias: 'widget.technical-attributes-form-gas',

    requires: [
        'Imt.usagepointmanagement.view.forms.GasInfo',
        'Imt.usagepointmanagement.view.forms.fields.MeasureDisplayField',
        'Imt.usagepointmanagement.view.forms.fields.ThreeValuesDisplayField'
    ],

    initComponent: function () {
        var me = this;

        me.viewForm = [
            {
                xtype: 'threevaluesdisplayfield',
                name: 'grounded',
                itemId: 'fld-up-grounded',
                fieldLabel: Uni.I18n.translate('general.label.grounded', 'IMT', 'Grounded')
            },
            {
                xtype: 'measuredisplayfield',
                name: 'pressure',
                itemId: 'fld-up-pressure',
                fieldLabel: Uni.I18n.translate('general.label.pressure', 'IMT', 'Pressure'),
                unitType: 'pressure'
            },
            {
                xtype: 'measuredisplayfield',
                name: 'physicalCapacity',
                itemId: 'fld-up-capacity',
                fieldLabel: Uni.I18n.translate('general.label.capacity', 'IMT', 'Physical capacity'),
                unitType: 'volume'
            },
            {
                xtype: 'threevaluesdisplayfield',
                name: 'limiter',
                itemId: 'fld-up-limiter',
                fieldLabel: Uni.I18n.translate('general.label.limiter', 'IMT', 'Limiter')
            },
            {
                name: 'loadLimiterType',
                itemId: 'fld-up-load-limiter-type',
                fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Load limiter type'),
                listeners: {
                    beforerender: function (fld){
                        fld.setVisible(me.down('#fld-up-limiter').getValue() == "YES");
                    }
                },
                renderer: function (value) {
                    return value ? value : '-';
                }
            },
            {
                xtype: 'measuredisplayfield',
                name: 'loadLimit',
                itemId: 'fld-up-load-limit',
                fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Load limit'),
                unitType: 'volume',
                listeners: {
                    beforerender: function (fld){
                        fld.setVisible(me.down('#fld-up-limiter').getValue() == "YES");
                    }
                }
            },
            {
                xtype: 'threevaluesdisplayfield',
                name: 'bypass',
                itemId: 'fld-up-bypass',
                fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Bypass')
            },
            {
                name: 'bypassStatus',
                itemId: 'fld-up-bypass-status',
                fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Bypass status'),
                listeners: {
                    beforerender: function (fld){
                        fld.setVisible(me.down('#fld-up-bypass').getValue() == 'YES')
                    }
                },
                renderer: function (value) {
                    var result;

                    if (!Ext.isEmpty(value)) {
                        result = Ext.getStore('Imt.usagepointmanagement.store.BypassStatuses').getById(value).get('displayValue');
                    }

                    return result || '-';
                }
            },
            {
                xtype: 'threevaluesdisplayfield',
                name: 'valve',
                itemId: 'fld-up-valve',
                fieldLabel: Uni.I18n.translate('general.label.valve', 'IMT', 'Valve')
            },
            {
                xtype: 'threevaluesdisplayfield',
                name: 'collar',
                itemId: 'fld-up-collar',
                fieldLabel: Uni.I18n.translate('general.label.collar', 'IMT', 'Collar')
            },
            {
                xtype: 'threevaluesdisplayfield',
                name: 'capped',
                itemId: 'fld-up-capped',
                fieldLabel: Uni.I18n.translate('general.label.capped', 'IMT', 'Capped')
            },
            {
                xtype: 'threevaluesdisplayfield',
                name: 'clamped',
                itemId: 'fld-up-clamped',
                fieldLabel: Uni.I18n.translate('general.label.clamped', 'IMT', 'Clamped')
            },
            {
                xtype: 'threevaluesdisplayfield',
                name: 'interruptible',
                itemId: 'fld-up-interruptible',
                fieldLabel: Uni.I18n.translate('general.label.interruptible', 'IMT', 'Interruptible')
            }
        ];

        me.editForm = {
            xtype: 'gas-info-form',
            itemId: 'edit-form',
            defaults: me.editDefaults
        };

        me.callParent();
    }
});
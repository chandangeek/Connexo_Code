Ext.define('Imt.usagepointmanagement.view.forms.attributes.TechnicalAttributesFormElectricity', {
    extend: 'Imt.usagepointmanagement.view.forms.attributes.ViewEditForm',
    alias: 'widget.technical-attributes-form-electricity',

    requires: [
        'Imt.usagepointmanagement.view.forms.ElectricityInfo',
        'Imt.usagepointmanagement.view.forms.fields.MeasureDisplayField',
        'Imt.usagepointmanagement.view.forms.fields.ThreeValuesDisplayField'
    ],

    initComponent: function () {
        var me = this;

        me.viewForm = [
            {
                name: 'grounded',
                itemId: 'fld-up-grounded',
                fieldLabel: Uni.I18n.translate('general.label.grounded', 'IMT', 'Grounded'),
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.label.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.label.no', 'IMT', 'No');
                }
            },
            {
                xtype: 'measuredisplayfield',
                name: 'nominalServiceVoltage',
                itemId: 'fld-up-service-voltage',
                fieldLabel: Uni.I18n.translate('general.label.voltage', 'IMT', 'Nominal voltage'),
                unitType: 'voltage'
            },
            {
                name: 'phaseCode',
                itemId: 'fld-up-phase',
                fieldLabel: Uni.I18n.translate('general.label.phaseCode', 'IMT', 'Phase code'),
                renderer: function (value) {
                    return value ? value : '-';
                }
            },
            {
                xtype: 'measuredisplayfield',
                name: 'ratedPower',
                itemId: 'fld-up-rated-power',
                fieldLabel: Uni.I18n.translate('general.label.ratedPower', 'IMT', 'Rated power'),
                unitType: 'power'
            },
            {
                xtype: 'measuredisplayfield',
                name: 'estimatedLoad',
                itemId: 'fld-up-estimated-load',
                fieldLabel: Uni.I18n.translate('general.label.estimatedLoad', 'IMT', 'Estimated load'),
                unitType: 'estimationLoad'
            },

            {
                name: 'limiter',
                itemId: 'fld-up-limiter',
                fieldLabel: Uni.I18n.translate('general.label.limiter', 'IMT', 'Limiter'),
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.label.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.label.no', 'IMT', 'No');
                }
            },
            {
                name: 'loadLimiterType',
                itemId: 'fld-up-loadLimiterType',
                hidden: true,
                fieldLabel: Uni.I18n.translate('general.label.loadLimiterType', 'IMT', 'Load limiter type'),
                listeners: {
                    beforerender: function (fld){
                        fld.setVisible(me.down('#fld-up-limiter').getValue())
                    }
                }
            },
            {
                xtype: 'measuredisplayfield',
                name: 'loadLimit',
                itemId: 'fld-up-loadLimit',
                fieldLabel: Uni.I18n.translate('general.label.loadLimit', 'IMT', 'Load limit'),
                unitType: 'power',
                listeners: {
                    beforerender: function (fld){
                        fld.setVisible(me.down('#fld-up-limiter').getValue())
                    }
                }
            },
            {
                xtype: 'threevaluesdisplayfield',
                name: 'collar',
                itemId: 'fld-up-collar',
                fieldLabel: Uni.I18n.translate('general.label.collar', 'IMT', 'Collar')
            },
            {
                name: 'interruptible',
                itemId: 'fld-up-interruptible',
                fieldLabel: Uni.I18n.translate('general.label.interruptible', 'IMT', 'Interruptible'),
                renderer: function (value) {
                    return value ? Uni.I18n.translate('general.label.yes', 'IMT', 'Yes') : Uni.I18n.translate('general.label.no', 'IMT', 'No');
                }
            }
        ];

        me.editForm = {
            xtype: 'electricity-info-form',
            itemId: 'edit-form',
            defaults: me.editDefaults
        };

        me.callParent();
    }
});
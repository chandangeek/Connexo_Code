/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.forms.attributes.TechnicalAttributesFormThermal', {
    extend: 'Imt.usagepointmanagement.view.forms.attributes.ViewEditForm',
    alias: 'widget.technical-attributes-form-thermal',

    requires: [
        'Imt.usagepointmanagement.view.forms.ThermalInfo',
        'Imt.usagepointmanagement.view.forms.fields.MeasureDisplayField',
        'Imt.usagepointmanagement.view.forms.fields.ThreeValuesDisplayField'
    ],

    initComponent: function () {
        var me = this;

        me.viewForm = [
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
                unitType: 'capacity'
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
                fieldLabel: Uni.I18n.translate('general.label.bypassStatus', 'IMT', 'Bypass status'),
                listeners: {
                    beforerender: function (fld){
                        fld.setVisible(me.down('#fld-up-bypass').getValue() == 'YES')
                    }
                },
                renderer: function (data) {
                    var value;
                    value = Ext.getStore('Imt.usagepointmanagement.store.BypassStatuses').getById(data);
                    return value.get('displayValue');
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
            }
        ];

        me.editForm = {
            xtype: 'thermal-info-form',
            itemId: 'edit-form'
        };

        me.callParent();
    }

});
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.forms.WaterInfo', {
    extend: 'Imt.usagepointmanagement.view.forms.BaseInfo',
    alias: 'widget.water-info-form',

    // items are initialized into initComponent due to Ext.container.AbstractContainer#preareItems (applyDefaults makes changes into config)
    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'technical-info-warning',
                xtype: 'uni-form-error-message',
                hidden: true
            },
            {
                xtype: 'techinfo-threevaluesfield',
                name: 'grounded',
                itemId: 'up-grounded-checkbox',
                fieldLabel: Uni.I18n.translate('general.label.grounded', 'IMT', 'Grounded')
            },
            {
                xtype: 'techinfo-measurefield',
                name: 'pressure',
                itemId: 'up-pressure-measurefield',
                fieldLabel: Uni.I18n.translate('general.label.pressure', 'IMT', 'Pressure'),
                store: 'Imt.usagepointmanagement.store.measurementunits.Pressure',
                value: {value: null, unit: 'Pa', multiplier: 0}
            },
            {
                xtype: 'techinfo-measurefield',
                name: 'physicalCapacity',
                itemId: 'up-physicalCapacity-checkbox',
                fieldLabel: Uni.I18n.translate('general.label.physicalCapacity', 'IMT', 'Physical capacity'),
                store: 'Imt.usagepointmanagement.store.measurementunits.Volume',
                value: {value: null, unit: 'm3/h', multiplier: 0}
            },
            {
                xtype: 'techinfo-limiter-combo',
                itemId: 'up-limiter-limitercheckbox'
            },
            {
                xtype: 'techinfo-loadlimitertypefield',
                itemId: 'up-loadLimiterType-textfield',
                hidden: true
            },
            {
                xtype: 'techinfo-loadlimitfield',
                itemId: 'up-loadLimit-loadlimitfield',
                store: 'Imt.usagepointmanagement.store.measurementunits.Volume',
                value: {value: null, unit: 'm3/h', multiplier: 0},
                hidden: true
            },
            {
                xtype: 'techinfo-bypassfield',
                itemId: 'up-bypass-combo'
            },
            {
                xtype: 'techinfo-bypassstatuscombobox',
                itemId: 'up-bypassStatus-combo',
                hidden: true
            },
            {
                xtype: 'techinfo-threevaluesfield',
                name: 'valve',
                itemId: 'up-valve-combo',
                fieldLabel: Uni.I18n.translate('general.label.valve', 'IMT', 'Valve')
            },
            {
                xtype: 'techinfo-threevaluesfield',
                name: 'collar',
                itemId: 'up-collar-combo',
                fieldLabel: Uni.I18n.translate('general.label.collar', 'IMT', 'Collar')
            },
            {
                xtype: 'techinfo-threevaluesfield',
                name: 'capped',
                itemId: 'up-capped-combo',
                fieldLabel: Uni.I18n.translate('general.label.capped', 'IMT', 'Capped')
            },
            {
                xtype: 'techinfo-threevaluesfield',
                name: 'clamped',
                itemId: 'up-clamped-combo',
                fieldLabel: Uni.I18n.translate('general.label.clamped', 'IMT', 'Clamped')
            }
        ];

        me.callParent(arguments);
    }
});
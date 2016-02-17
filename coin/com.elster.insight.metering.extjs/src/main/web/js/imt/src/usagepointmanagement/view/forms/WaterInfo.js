Ext.define('Imt.usagepointmanagement.view.forms.WaterInfo', {
    extend: 'Ext.form.Panel',
    alias: 'widget.water-info-form',
    requires: [
        'Uni.util.FormErrorMessage',
        'Imt.usagepointmanagement.view.forms.fields.MeasureField',
        'Imt.usagepointmanagement.view.forms.fields.ThreeValuesField'
    ],
    defaults: {
        labelWidth: 260,
        width: 595
    },
    items: [
        {
            xtype: 'checkbox',
            name: 'grounded',
            itemId: 'up-grounded-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.grounded', 'IMT', 'Grounded')
        },
        {
            xtype: 'measurefield',
            name: 'physicalCapacity',
            itemId: 'up-physicalCapacity-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.physicalCapacity', 'IMT', 'Physical capacity')
        },
        {
            xtype: 'checkbox',
            name: 'limiter',
            itemId: 'up-limiter-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.limiter', 'IMT', 'Limiter'),
            listeners: {
                change: {
                    fn: function (field, newValue) {
                        if (field.rendered) {
                            Ext.suspendLayouts();
                            field.nextSibling('[name=loadLimiterType]').setVisible(newValue);
                            field.nextSibling('[name=loadLimit]').setVisible(newValue);
                            Ext.resumeLayouts(true);
                        }
                    }
                }
            }
        },
        {
            xtype: 'textfield',
            name: 'loadLimiterType',
            itemId: 'up-loadLimiterType-textfield',
            fieldLabel: Uni.I18n.translate('general.label.loadLimiterType', 'IMT', 'Load limiter type'),
            hidden: true
        },
        {
            xtype: 'measurefield',
            name: 'loadLimit',
            itemId: 'up-loadLimit-measurefield',
            fieldLabel: Uni.I18n.translate('general.label.loadLimit', 'IMT', 'Load limit'),
            hidden: true
        },
        {
            xtype: 'threevaluesfield',
            name: 'bypass',
            itemId: 'up-bypass-combo',
            fieldLabel: Uni.I18n.translate('general.label.bypass', 'IMT', 'Bypass'),
            listeners: {
                change: {
                    fn: function (field, newValue) {
                        if (field.rendered) {
                            field.nextSibling('[name=bypassStatus]').setVisible(newValue);
                        }
                    }
                }
            }
        },
        {
            xtype: 'combobox',
            name: 'bypassStatus',
            itemId: 'up-bypassStatus-combo',
            fieldLabel: Uni.I18n.translate('general.label.bypassStatus', 'IMT', 'Bypass status'),
            store: 'Imt.usagepointmanagement.store.BypassStatuses',
            displayField: 'displayValue',
            valueField: 'id',
            queryMode: 'local',
            forceSelection: true,
            hidden: true,
            listeners: {
                change: {
                    fn: function (field, newValue) {
                        if (Ext.isEmpty(newValue)) {
                            field.reset();
                        }
                    }
                }
            }
        },
        {
            xtype: 'threevaluesfield',
            name: 'valve',
            itemId: 'up-valve-combo',
            fieldLabel: Uni.I18n.translate('general.label.valve', 'IMT', 'Valve')
        },
        {
            xtype: 'threevaluesfield',
            name: 'collar',
            itemId: 'up-collar-combo',
            fieldLabel: Uni.I18n.translate('general.label.collar', 'IMT', 'Collar')
        },
        {
            xtype: 'threevaluesfield',
            name: 'capped',
            itemId: 'up-capped-combo',
            fieldLabel: Uni.I18n.translate('general.label.capped', 'IMT', 'Capped')
        },
        {
            xtype: 'threevaluesfield',
            name: 'clamped',
            itemId: 'up-clamped-combo',
            fieldLabel: Uni.I18n.translate('general.label.clamped', 'IMT', 'Clamped')
        }
    ]
});
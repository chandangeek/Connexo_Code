/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.usagepointmanagement.view.history.MetrologyConfigurationHistoryPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.metrology-configuration-history-preview',
    requires: [
        'Mdc.usagepointmanagement.view.history.MetrologyConfigurationActionMenu'
    ],
    frame: true,
    usagePoint: null,
    record: null,

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                privileges: Mdc.privileges.UsagePoint.canAdmin(),
                itemId: 'actions-button',
                menu: {
                    xtype: 'metrology-configuration-versions-action-menu',
                    itemId: 'metrology-configuration-versions-action-menu-id',
                    usagePoint: me.usagePoint
                }
            }
        ];

        me.callParent(arguments);
    },

    items: [
        {
            xtype: 'form',
            itemId: 'metrology-configuration-history-preview-form-id',
            ui: 'medium',
            defaults: {
                xtype: 'displayfield',
                labelWidth: 300
            },
            items: [
                {
                    fieldLabel: Uni.I18n.translate('general.period', 'MDC', 'Period'),
                    name: 'period',
                    itemId: 'period-field'
                }
                ,
                {
                    fieldLabel: Uni.I18n.translate('general.metrologyConfiguration', 'MDC', 'Metrology configuration'),
                    name: 'name',
                    itemId: 'metrology-configuration-field'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('general.readingTypes', 'MDC', 'Reading types'),
                    itemId: 'reading-types-area'
                }
            ]
        }
    ],
    listeners: {
        afterrender: function () {
            var me = this;
            if (me.down('form').getRecord()) {
                me.down('form').loadRecord(me.down('form').getRecord());
            }
        }
    },

    fillReadings: function (record) {

        var me = this;

        me.down('#reading-types-area').removeAll();
        for (var i = 0; i < record.get('metrologyConfiguration').readingTypes.length; i++) {
            var readingType = record.get('metrologyConfiguration').readingTypes[i];

            this.down('#reading-types-area').add(
                {
                    xtype: 'reading-type-displayfield',
                    fieldLabel: undefined,
                    value: readingType,
                    margin: '0 0 -10 0'
                }
            );
        }
        this.down('form').getForm().loadRecord(record);
    }
})
;
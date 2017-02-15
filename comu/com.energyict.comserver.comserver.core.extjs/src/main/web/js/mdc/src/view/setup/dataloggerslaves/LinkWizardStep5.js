/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.dataloggerslaves.LinkWizardStep5', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.dataloggerslave-link-wizard-step5',
    ui: 'large',

    requires: [
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'label',
                itemId: 'mdc-step5-info-label',
                text: '',
                margin: '20 0 20 0',
                style: { 'font-weight': 'normal' }
            }
        ];

        me.callParent(arguments);
    },

    initialize: function (dataLoggerId, slaveId) {
        var me = this;
        if (me.rendered) {
            me.doInitialize(dataLoggerId, slaveId);
        } else {
            me.on('afterrender', function() {
                me.doInitialize(dataLoggerId, slaveId);
            }, me, {single:true});
        }
    },


    doInitialize: function (dataLoggerId, slaveId) {
        var me = this,
            labelField = me.down('#mdc-step5-info-label');

        if (labelField) {
            labelField.setText(
                Ext.String.format(
                    Uni.I18n.translate('general.question.linkSlaveXToDataLoggerY', 'MDC', "Link slave '{0}' to data logger '{1}'?"),
                    slaveId,
                    dataLoggerId
                )
            );
        }
    }

});

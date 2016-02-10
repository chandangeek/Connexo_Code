Ext.define('Scs.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.servicecalls-preview-form',
    layout: {
        type: 'vbox'
    },
    defaults: {
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.serviceCall', 'SCS', 'Service call'),
                name: 'internalId'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('servicecalls.externalReference', 'SCS', 'External reference'),
                name: 'externalReference'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.type', 'SCS', 'Type'),
                name: 'type'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.status', 'SCS', 'Status'),
                name: 'status'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.receivedDate', 'SCS', 'Received date'),
                name: 'receivedDate',
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.modificationDate', 'SCS', 'Modification date'),
                name: 'modificationDate',
            }
        ];
        me.callParent(arguments);
    },

    updatePreview: function (record) {
        var me = this;

        if (!Ext.isDefined(record)) {
            return;
        }
        if (me.rendered) {
            Ext.suspendLayouts();
        }

        me.loadRecord(record);
        if (me.rendered) {
            Ext.resumeLayouts(true);
        }
    }

});

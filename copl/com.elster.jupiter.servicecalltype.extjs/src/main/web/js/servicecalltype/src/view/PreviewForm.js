Ext.define('Sct.view.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.servicecalltypes-preview-form',
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
                fieldLabel: Uni.I18n.translate('general.type', 'SCT', 'Type'),
                name: 'type'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.version', 'SCT', 'Version'),
                name: 'versionName'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.status', 'SCT', 'Status'),
                name: 'status'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.loglevel', 'SCT', 'Log level'),
                name: 'loglevel'
            },
            {
                xtype: 'displayfield',
                fieldLabel: Uni.I18n.translate('general.lifecycle', 'SCT', 'Life cycle'),
                name: 'lifecycle',
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

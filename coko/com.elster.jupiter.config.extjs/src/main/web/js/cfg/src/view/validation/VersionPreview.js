Ext.define('Cfg.view.validation.VersionPreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.version-preview',
    itemId: 'versionPreview',
    frame: true,

    requires: [
        'Cfg.model.ValidationRule',
        'Cfg.view.validation.RuleActionMenu'
    ],

    title: 'Details',

    layout: {
        type: 'vbox'
    },

    defaults: {
        xtype: 'displayfield',
        labelWidth: 260
    },

    items: [
        {
            name: 'name',
            fieldLabel: Uni.I18n.translate('validation.versionName', 'CFG', 'Name')
        },
        {
            name: 'description',
            fieldLabel: Uni.I18n.translate('validation.versionDescription', 'CFG', 'Description')
        },
		{
            name: 'startDateFormatted',
            fieldLabel: Uni.I18n.translate('validation.versionStart', 'CFG', 'Start')
        }       
    ],

    initComponent: function () {
        this.callParent(arguments);
    },

    updateVersion: function (version) {      
        this.loadRecord(version);       
    }

});

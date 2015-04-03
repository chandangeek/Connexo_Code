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
/*
    tools: [
        {
            xtype: 'button',
            itemId: 'versionPreviewActionsButton',
            //text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
			text: 'Actions2',
            hidden: Uni.Auth.hasNoPrivilege('privilege.administrate.validationConfiguration'),
            iconCls: 'x-uni-action-iconD',
            menu: {
                itemId: 'versionActionMenu',
                xtype: 'version-action-menu'
            }
        }
    ],
*/
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
            name: 'startPeriodFormatted',
            fieldLabel: Uni.I18n.translate('validation.versionDescription', 'CFG', 'Start')
        }       
    ],

    initComponent: function () {
        this.callParent(arguments);
    },

    updateVersion: function (version) {
      //  this.setTitle(version.get('name'));
        this.loadRecord(version);
       // this.destroy();
    }

});

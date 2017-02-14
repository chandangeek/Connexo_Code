/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validation.VersionPreview', {
    extend: 'Ext.form.Panel',
    alias: 'widget.version-preview',
    itemId: 'versionPreview',
    frame: true,

    requires: [
        'Cfg.model.ValidationRule',
        'Cfg.view.validation.RuleActionMenu'
    ],

    title: Uni.I18n.translate('general.details','CFG','Details'),

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
            fieldLabel: Uni.I18n.translate('validation.period', 'CFG', 'Period')
        },
        {
            name: 'description',
            fieldLabel: Uni.I18n.translate('general.description', 'CFG', 'Description'),
            htmlEncode: true,
            renderer: function(value) {
                return Ext.String.htmlEncode(value).replace(/(?:\r\n|\r|\n)/g, '<br />');
            }
        },		
		{
            name: 'numberOfActiveRules',
            fieldLabel: Uni.I18n.translate('validation.activeRules', 'CFG', 'Active rules')
        },       
		{
            name: 'numberOfInactiveRules',
            fieldLabel: Uni.I18n.translate('validation.inactiveRules', 'CFG', 'Inactive rules')
        }       		
    ],

    initComponent: function () {
        this.callParent(arguments);
    },

    updateVersion: function (version) {      
        this.loadRecord(version);       
    }

});

/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationListPreview', {
    extend: 'Imt.metrologyconfiguration.view.MetrologyConfigurationDetailsForm',
    alias: 'widget.metrology-config-details',
    requires: [
        'Imt.metrologyconfiguration.view.MetrologyConfigurationActionMenu'
    ],
    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Imt.privileges.MetrologyConfig.admin,
            menu: {
                xtype: 'metrology-configuration-action-menu',
                itemId: 'metrology-configuration-list-action-menu'
            }
        }
    ],

    setVisibleActionsButton: function(visible){
        if (this.down('#actionButton')) {
            this.down('#actionButton').setVisible(visible);
        }        
    }    
});
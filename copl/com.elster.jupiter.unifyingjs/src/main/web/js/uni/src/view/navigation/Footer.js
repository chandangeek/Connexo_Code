/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.navigation.Footer
 */
Ext.define('Uni.view.navigation.Footer', {
    extend: 'Ext.container.Container',
    alias: 'widget.navigationFooter',
    cls: 'nav-footer',
    height: 30,

    layout: {
        type: 'hbox',
        align: 'middle'
    },

    items: [
        {
            xtype: 'component',
            cls: 'powered-by',
            html: 'Powered by <a href="http://www.energyict.com/en/smart-grid" target="_blank">' +
                'Elster EnergyICT Jupiter 1.0.0' +
                '</a>, <a href="http://www.energyict.com/en/smart-grid" target="_blank">' +
                'Smart data management</a>'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    }
});
/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.loadprofiletype.LoadProfileTypeAddRegisterTypesView', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.loadProfileTypeAddRegisterTypesView',
    itemId: 'loadProfileTypeAddRegisterTypesView',

    content: [
        {
            xtype: 'panel',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: {xtype: 'loadProfileTypeAddRegisterTypesGrid'}
        }
    ]
});
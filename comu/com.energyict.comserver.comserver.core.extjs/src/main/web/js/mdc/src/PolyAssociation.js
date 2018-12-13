/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.PolyAssociation', {
    extend: 'Ext.data.HasManyAssociation',
    alias : 'association.polymorphic',

    constructor: function(config) {
        this.reader = {
            type: 'polymorphic',
            getTypeDiscriminator: config.getTypeDiscriminator
        };

        this.callParent(arguments);
    }
});

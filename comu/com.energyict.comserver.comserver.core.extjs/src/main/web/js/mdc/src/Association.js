/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.Association',{
    override: 'Ext.data.association.Association',

    statics: {
        AUTO_ID: 1000,

        create: function(association){
            if (Ext.isString(association)) {
                association = {
                    type: association
                };
            }
            switch (association.type) {
                case 'belongsTo':
                    return new Ext.data.association.BelongsTo(association);
                case 'hasMany':
                    return new Mdc.PolyAssociation(association);
                case 'hasOne':
                    return new Ext.data.association.HasOne(association);
                default:
                    //<debug>
                    Ext.Error.raise('Unknown Association type: "' + association.type + '"');
                //</debug>
            }
            return association;
        }
    }
});

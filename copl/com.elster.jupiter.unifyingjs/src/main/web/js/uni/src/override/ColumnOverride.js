/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * Created by dvy on 20/05/2015.
 */
Ext.define('Uni.override.ColumnOverride', {
    override: 'Ext.grid.column.Column',
    renderer: function(value){
        if(Ext.isEmpty(value)) {
            return '-';
        }
        return Ext.String.htmlEncode(value);
    }
});
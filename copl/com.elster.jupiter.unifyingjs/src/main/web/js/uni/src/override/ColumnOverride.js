/**
 * Created by dvy on 20/05/2015.
 */
Ext.define('Uni.override.ColumnOverride', {
    override: 'Ext.grid.column.Column',
    renderer: function(value){
        return Ext.String.htmlEncode(value);
    }
});
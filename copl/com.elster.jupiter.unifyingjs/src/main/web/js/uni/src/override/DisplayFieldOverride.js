/**
 * Created by dvy on 21/05/2015.
 */
Ext.define('Uni.override.DisplayFieldOverride', {
    override: 'Ext.form.field.Display',
    renderer: function(value){
        if(Ext.isEmpty(value)) {
            return '-'
        }
        return Ext.String.htmlEncode(value); // htmlEncoding is needed here, since...
    },
    htmlEncode: true  // ... this setting is only applied when you have no renderer defined
});
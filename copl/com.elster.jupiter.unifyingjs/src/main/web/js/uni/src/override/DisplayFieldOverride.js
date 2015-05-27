/**
 * Created by dvy on 21/05/2015.
 */
Ext.define('Uni.override.DisplayFieldOverride', {
    override: 'Ext.form.field.Display',
    //renderer: function(value){
    //    return Ext.String.htmlEncode(value);
    //}
    htmlEncode: true
});
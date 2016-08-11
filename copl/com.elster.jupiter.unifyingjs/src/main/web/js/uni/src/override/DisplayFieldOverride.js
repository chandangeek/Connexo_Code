/**
 * Created by dvy on 21/05/2015.
 */
Ext.define('Uni.override.DisplayFieldOverride', {
    override: 'Ext.form.field.Display',
    emptyValueDisplay: '-',
    renderer: function(value){
        if(Ext.isEmpty(value)) {
            return this.emptyValueDisplay;
        }
        return this.htmlEncode ? Ext.String.htmlEncode(value) : value;

    },
    htmlEncode: true // this setting is only applied when you have no renderer defined
});
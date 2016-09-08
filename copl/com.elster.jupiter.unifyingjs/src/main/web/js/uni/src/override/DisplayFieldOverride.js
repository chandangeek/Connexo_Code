/**
 * Created by dvy on 21/05/2015.
 */
Ext.define('Uni.override.DisplayFieldOverride', {
    override: 'Ext.form.field.Display',
    emptyValueDisplay: '-',

    initComponent: function () {
        this.callParent(arguments);
        this.on('refresh', this.setTooltip);
        this.on('resize', this.setTooltip);
    },

    /**
     * @private
     */
    setTooltip: function (field) {
        if (field.rendered && !field.isHidden()) {
            Ext.suspendLayouts();
            //debugger;
            var
                inputEl = field.getEl().down('#'+field.id+'-inputEl'),
                tm = new Ext.util.TextMetrics(inputEl),
                value
            ;

            if (inputEl) {
                value = inputEl.dom.innerHTML;
                if (inputEl.getWidth() < tm.getWidth(value)) {
                    inputEl.set({'data-qtip': value});
                }
            }

            Ext.resumeLayouts(true);
        }
    },

    renderer: function(value){
        if(Ext.isEmpty(value)) {
            return this.emptyValueDisplay;
        }
        return this.htmlEncode ? Ext.String.htmlEncode(value) : value;

    },
    htmlEncode: true // this setting is only applied when you have no renderer defined
});
/**
 * This class is used as a mixin.
 *
 * This class is to be used to provide basic methods for controllers that handle events of combobox.
 */
Ext.define('Isu.util.IsuCombo', {
    /**
     * Sets tooltip for combobox.
     * Combobox must has 'tooltipText' property otherwise it sets default text.
     */
    setComboTooltip: function (combo) {
        var comboEl = Ext.get(combo.getEl());

        combo.tooltip = Ext.DomHelper.append(Ext.getBody(), {
            tag: 'div',
            html: combo.tooltipText || 'Start typing',
            cls: 'isu-combo-tooltip'
        }, true);

        combo.tooltip.setStyle({
            width: comboEl.getWidth(false) + 'px',
            top: comboEl.getY() + comboEl.getHeight(false) + 'px',
            left: comboEl.getX() + 'px'
        });

        combo.tooltip.hide();

        combo.on('destroy', function () {
            combo.tooltip.destroy();
        });

        return combo.tooltip;
    },

    /**
     * Handle 'focus' event.
     * If value of combobox is null shows tooltip.
     */
    onFocusCombo: function (combo) {
        var qstr = combo.getValue();
        console.log(qstr);
        if (!combo.getValue()) {
            combo.doQuery(combo.getValue());
        }
    },

    /**
     * Handle 'blur' event.
     * Hides tooltip of combobox on blur.
     */
    onBlurCombo: function (combo) {
        var tooltip = combo.tooltip;
        console.log(combo.getValue());
        tooltip && tooltip.hide();
    }

    /**
     * Handle 'change' event.
     * If value of combobox is null resets combobox and shows tooltip otherwise hides tooltip
     * and shows list of values.
     */
});
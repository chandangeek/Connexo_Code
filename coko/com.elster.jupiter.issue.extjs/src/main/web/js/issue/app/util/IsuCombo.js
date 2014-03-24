Ext.define('Isu.util.IsuCombo', {
    setComboTooltip: function (combo) {
        var comboEl = Ext.get(combo.getEl());

        combo.tooltip = Ext.DomHelper.append(Ext.getBody(), {
            tag: 'div',
            html: combo.tooltipText,
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

    onFocusCombo: function (combo) {
        var tooltip = combo.tooltip || this.setComboTooltip(combo);

        if (!combo.getValue()) {
            tooltip && tooltip.show();
        }
    },

    onBlurCombo: function (combo) {
        var tooltip = combo.tooltip;

        tooltip && tooltip.hide();
    },

    clearCombo: function (combo, newValue) {
        var listValues = combo.picker,
            tooltip = combo.tooltip;

        if (newValue == null) {
            combo.reset();
            listValues && listValues.hide();
            tooltip && tooltip.show();
        } else {
            tooltip && tooltip.hide();
            listValues && listValues.show();
        }
    }
});
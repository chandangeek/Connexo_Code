Ext.define('Dxp.view.common.ExportTagsInfoButton', {
    extend: 'Ext.button.Button',
    xtype: 'dxp-export-tags-info-button',
    tooltip: '-- To do -- ',
    iconCls: 'uni-icon-info-small',
    ui: 'blank',
    shadow: false,
    margin: '5 0 0 10',
    width: 16,
    tabIndex: -1, // disallow this 'button' to be focused using the TAB key

    initComponent: function () {
        var me = this;

        me.tooltip = Ext.String.format(
            Uni.I18n.translate('general.possibleTags', 'DES', 'Possible tags')
            + ':</br></br><table>'
            + '<tr><td>&lt;date&gt;</td>            <td>:</td><td>{0}</td></tr>'
            + '<tr><td>&lt;time&gt;</td>            <td>:</td><td>{1}</td></tr>'
            + '<tr><td>&lt;sec&gt;</td>             <td>:</td><td>{2}</td></tr>'
            + '<tr><td>&lt;millisec&gt;</td>        <td>:</td><td>{3}</td></tr>'
            + '<tr><td>&lt;dateyear&gt;</td>        <td>:</td><td>{4}</td></tr>'
            + '<tr><td>&lt;datemonth&gt;</td>       <td>:</td><td>{5}</td></tr>'
            + '<tr><td>&lt;dateday&gt;</td>         <td>:</td><td>{6}</td></tr>'
            + '<tr><td>&lt;datadate&gt;</td>        <td>:</td><td>{7}</td></tr>'
            + '<tr><td>&lt;datatime&gt;</td>        <td>:</td><td>{8}</td></tr>'
            + '<tr><td>&lt;dataenddate&gt;</td>     <td>:</td><td>{9}</td></tr>'
            + '<tr><td>&lt;dataendtime&gt;</td>     <td>:</td><td>{10}</td></tr>'
            + '<tr><td>&lt;seqnrwithinday&gt;</td>  <td>:</td><td>{11}</td></tr>'
            + '<tr><td>&lt;datayearandmonth&gt;</td><td>:</td><td>{12}</td></tr>'
            + '<tr><td>&lt;dateformat:X&gt;</td>    <td>:</td><td>{13}</td></tr>'
            + '<tr><td>&lt;identifier&gt;</td>      <td>:</td><td>{14}</td></tr>'
            + '</table>',
            Uni.I18n.translate('dataExportTasks.dateTag.info', 'DES', 'date of execution'),
            Uni.I18n.translate('dataExportTasks.timeTag.info', 'DES', 'time of execution'),
            Uni.I18n.translate('dataExportTasks.secTag.info', 'DES', 'seconds part of the time of execution'),
            Uni.I18n.translate('dataExportTasks.millisecTag.info', 'DES', 'milliseconds part of the time of execution'),
            Uni.I18n.translate('dataExportTasks.dateyearTag.info', 'DES', 'year part of the date of execution'),
            Uni.I18n.translate('dataExportTasks.datemonthTag.info', 'DES', 'month part of the date of execution'),
            Uni.I18n.translate('dataExportTasks.datedayTag.info', 'DES', 'day part of the date of execution'),
            Uni.I18n.translate('dataExportTasks.datadateTag.info', 'DES', '(start) date of the exported data'),
            Uni.I18n.translate('dataExportTasks.datatimeTag.info', 'DES', '(start) time of the exported data'),
            Uni.I18n.translate('dataExportTasks.dataenddateTag.info', 'DES', 'end date of the exported data'),
            Uni.I18n.translate('dataExportTasks.dataendtimeTag.info', 'DES', 'end time of the exported data'),
            Uni.I18n.translate('dataExportTasks.seqnrwithindayTag.info', 'DES', 'number of times the task has already been executed during the day of execution'),
            Uni.I18n.translate('dataExportTasks.datayearandmonthTag.info', 'DES', 'year and month part of the (start) date of the exported data'),
            Uni.I18n.translate('dataExportTasks.dateformatTag.info', 'DES', 'date of execution formatted your own way (eg. X = yyyyMMddHHmmss)'),
            Uni.I18n.translate('dataExportTasks.identifierTag.info', 'DES', 'configured tag')
        );
        me.callParent(arguments);
    }
});
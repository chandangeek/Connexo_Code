package com.elster.jupiter.issue.impl.database.groups;

import com.elster.jupiter.issue.impl.database.TableSpecs;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.sql.SqlBuilder;

@LiteralSql
public class GroupByReasonImplementer extends GroupIssuesOperation {

    protected GroupByReasonImplementer(DataModel dataModel) {
        super(dataModel);
    }

    /**
     SQL example with 'REASON' grouping column:
     <code>
        SELECT col, num
        FROM
            (SELECT ROWNUM as rnum, intr.*
            FROM
                (SELECT DISTINCT r.REASON_NAME as col, count(isu.REASON_ID) as num
                FROM ISU_ISSUE isu, ISU_REASON r
                WHERE isu.REASON_ID = r.ID
                GROUP BY (r.REASON_NAME)
                ORDER BY num DESC, col ASC
            ) intr
            WHERE ROWNUM <= 5
        ) ext
        WHERE  ext.rnum >= 0;
     </code>
     */
    @Override
    public SqlBuilder buildSQL() {
        SqlBuilder builder = new SqlBuilder();
        builder.append("SELECT " + GROUP_TITLE + ", " + GROUP_COUNT + " FROM " + "(SELECT ROWNUM as rnum, intr.*");
        builder.append(" FROM (SELECT DISTINCT r.REASON_NAME as " + GROUP_TITLE + ", count(isu.REASON_ID) as " + GROUP_COUNT);
        builder.append(" FROM " + TableSpecs.ISU_ISSUE.name() + " isu, " + TableSpecs.ISU_REASON.name());
        builder.append(" r WHERE isu.REASON_ID = r.ID GROUP BY (r.REASON_NAME)");
        builder.append(" ORDER BY " + GROUP_COUNT + " " + (isAsc ? "ASC" : "DESC") + ", " + GROUP_TITLE + " ASC ) intr");
        builder.append(" WHERE ROWNUM <= ? ) ext WHERE ext.rnum >= ? ");
        return builder;
    }
}

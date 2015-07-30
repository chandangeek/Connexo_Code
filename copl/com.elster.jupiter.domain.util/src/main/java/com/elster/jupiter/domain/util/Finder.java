package com.elster.jupiter.domain.util;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlFragment;
import java.util.List;
import java.util.stream.Stream;

/**
 * Generic finder interfaces adding pagability and sortability to any query.
 */
public interface Finder<T> {

    /**
     * adds paging information to the query. Paging information will be automatically
     * set when using the from(QueryParameters) method. One additional element will be
     * returned to indicate a next page exists.
     *
     * @param start first line from the query to return
     * @param pageSize the number of lines to return.
     * @return A Finder that will return the requested page when asked.
     */
    public Finder<T> paged(int start, int pageSize);

    public Finder<T> sorted(String sortColumn, boolean ascending);

    public List<T> find();

    public default Finder<T> from(QueryParameters queryParameters) {
        if (queryParameters.getStart().isPresent() && queryParameters.getLimit().isPresent()) {
            this.paged(queryParameters.getStart().get(), queryParameters.getLimit().get());
        }
        for (Order columnSort : queryParameters.getSortingColumns()) {
            this.sorted(columnSort.getName(), columnSort.ascending());
        }
        return this;
    }

    default Stream<T> stream() {
        return this.find().stream();
    }

    /**
     * This method was added to Finder to allow reuse of this finder/query in other queries.
     * For the use of Subquery, see the documentation there
     */
    public Subquery asSubQuery(String... fieldNames);

    /**
     * This method was added to Finder to allow reuse of this finder/query in other queries.
     * For the use of Subquery, see the documentation there
     */
    SqlFragment asFragment(String... fieldNames);

    default Finder<T> maxPageSize(Thesaurus thesaurus, int maxPageSize) { return this; }

}
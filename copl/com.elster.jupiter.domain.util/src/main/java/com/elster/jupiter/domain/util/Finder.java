package com.elster.jupiter.domain.util;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlFragment;
import java.util.List;
import java.util.stream.Stream;

/**
 * Generic finder interfaces adding pagability and sortability to any query
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

    /**
     * adds paging information to the query. Paging information will be automatically
     * set when using the from(QueryParameters) method. One additional element will be
     * returned to indicate a next page exists.
     *
     * @param start first line from the query to return
     * @param pageSize the number of lines to return.
     * @return A Finder that will return the requested page when asked.
     */
//    Finder<T> defaultSortColumn(String sortColumn);

    public List<T> find();

    public Finder<T> from(QueryParameters queryParameters);

    /**
     * Create stream over the results from the Finder. If the finder was paged, only the results from the requested page
     * will be contained in the stream. If the Finder was unpaged, all results from the Finder will be contained in the stream.
     *
     * @return Stream containing found results.
     */
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

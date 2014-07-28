package io.hgc.exwhy.data;

import io.hgc.exwhy.tables.records.ContributorRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;


import static io.hgc.exwhy.Tables.CONTRIBUTOR;
import static io.hgc.exwhy.Tables.POST;
import static org.jooq.impl.DSL.*;
import static org.jooq.impl.DSL.using;

@Repository
public class ContributorDao {
    @Autowired
    Configuration configuration;

    public List<ContributorRecord> fetchContributors() {
        DSLContext create = DSL.using(configuration);
        return create.selectFrom(CONTRIBUTOR).fetch();
    }
}

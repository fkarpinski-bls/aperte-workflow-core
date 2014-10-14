package pl.net.bluesoft.rnd.processtool.web.view;

import java.util.List;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public interface IBpmTaskQueryCondition
{
    String getJoinCondition(String sortColumnName);

    String getSortQuery(String columnName);

    String getSearchCondition();
}

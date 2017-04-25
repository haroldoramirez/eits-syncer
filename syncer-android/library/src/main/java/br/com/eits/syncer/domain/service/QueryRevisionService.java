package br.com.eits.syncer.domain.service;

import java.util.ArrayList;
import java.util.List;

import br.com.eits.syncer.domain.entity.Revision;
import br.com.eits.syncer.domain.entity.RevisionType;
import br.com.eits.syncer.infrastructure.dao.SQLiteHelper;

/**
 * Created by rodrigo.p.fraga on 19/04/17.
 */
public class QueryRevisionService <T> extends RevisionService<T> implements IQueryRevisionService<T>
{
    /*-------------------------------------------------------------------
    * 		 					ATTRIBUTES
    *-------------------------------------------------------------------*/

    private String tables = "";
    private String joinTable = "";
    private String where = "";

    private List<Object> whereArguments = new ArrayList<Object>();

    private String groupBy = "";
    private String having = "";
    private String orderBy = "";

    /**
     * @param entityClass
     */
    public QueryRevisionService( Class<T> entityClass )
    {
        super(entityClass);

        this.tables = SQLiteHelper.TABLE_REVISION;
        this.joinTable = "json_each(" + SQLiteHelper.COLUMN_ENTITY + ")";
        this.where = this.where.concat( SQLiteHelper.COLUMN_ENTITY_CLASSNAME + " = ? AND " );
        this.whereArguments.add( entityClass.getName() );

        this.groupBy = this.groupBy.concat( SQLiteHelper.COLUMN_ENTITY_ID );
        this.having = this.having.concat( SQLiteHelper.TABLE_REVISION + "." + SQLiteHelper.COLUMN_TYPE + " <> " + RevisionType.REMOVE.ordinal() );
//        this.orderBy = this.orderBy.concat( SQLiteHelper.COLUMN_ID + " DESC" );
    }

    /*-------------------------------------------------------------------
	 * 		 					CONSTRUCTORS
	 *-------------------------------------------------------------------*/
    /**
     * @param field
     * @param value
     * @return
     */
    @Override
    public IQueryRevisionService where( String field, String value )
    {
        this.where = this.where.concat( field + " = ?" );
        this.whereArguments.add( value );
        return this;
    }

    /**
     * @param joinEntity
     * @return
     */
    @Override
    public IQueryRevisionService join( Class<?> joinEntity, long joinEntityId )
    {
        final String simpleClassName = joinEntity.getSimpleName().substring(0, 1).toLowerCase() + joinEntity.getSimpleName().substring(1);
        String entityIdName = Revision.extractEntityIdFieldByEntityClass( joinEntity ).getName();

        this.where = this.where.concat( "json_extract("+ SQLiteHelper.COLUMN_ENTITY + ", '$." + simpleClassName + "." + entityIdName + "') = ?" );
        this.whereArguments.add( joinEntityId );

        return this;
    }

    /**
     *
     * @param filters
     * @return
     */
    @Override
    public IQueryRevisionService filterBy( String filters )
    {
        filters = filters != null ? filters : "";

        this.tables = this.tables.concat( ", " + this.joinTable );
        this.where = this.where.concat( " AND json_each.type NOT IN ( 'object', 'array' ) AND json_each.value LIKE '%" + filters + "%'" );

        return this;
    }

    /**
     *
     * @return
     */
    @Override
    public IQueryRevisionService and() {
        this.where = this.where.concat(" AND ");
        return this;
    }

    /**
     *
     * @return
     */
    @Override
    public IQueryRevisionService or() {
        this.where = this.where.concat(" OR ");
        return this;
    }

    /**
     * @return
     */
    @Override
    public List<T> list()
    {
        final List<Revision<T>> revisions = this.revisionDao.listByCustomQuery( this );

        final List<T> entities = new ArrayList<T>();
        for ( Revision<T> revision : revisions )
        {
            entities.add( revision.getEntity() );
        }

        return entities;
    }

    /*-------------------------------------------------------------------
    * 		 				    GETTERS
    *-------------------------------------------------------------------*/

    /**
     *
     * @return
     */
    public String getGroupBy() {
        return groupBy;
    }

    /**
     *
     * @return
     */
    public String getOrderBy() {
        return orderBy;
    }

    /**
     *
     * @return
     */
    public String getTables() {
        return tables;
    }

    /**
     *
     * @return
     */
    public String getHaving() {
        return having;
    }

    /**
     *
     * @return
     */
    public List<Object> getWhereArguments() {
        return whereArguments;
    }

    /**
     *
     * @return
     */
    public String getWhere() {
        return where;
    }
}

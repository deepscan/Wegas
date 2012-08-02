/*
 * Wegas.
 * http://www.albasim.com/wegas/
 *
 * School of Business and Engineering Vaud, http://www.heig-vd.ch/
 * Media Engineering :: Information Technology Managment :: Comem
 *
 * Copyright (C) 2012
 */
package com.wegas.core.ejb;

import com.wegas.core.ejb.exception.PersistenceException;
import com.wegas.core.persistence.AbstractEntity;
import java.util.List;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @param <T>
 * @author Francois-Xavier Aeberhard <fx@red-agent.com>
 */
public abstract class AbstractFacadeImpl<T extends AbstractEntity> implements AbstractFacade<T> {

    /**
     *
     */
    final Class<T> entityClass;

    /**
     *
     * @param entityClass
     */
    public AbstractFacadeImpl(final Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     *
     * @return
     */
    protected abstract EntityManager getEntityManager();

    /**
     *
     */
    @Override
    public void flush() {
        getEntityManager().flush();
    }

    /**
     *
     * @param entity
     */
    @Override
    public void create(T entity) {
        getEntityManager().persist(entity);
        // getEntityManager().flush();
    }

//    /**
//     *
//     * @param entity
//     */
//    @Override
//    public void edit(final T entity) {
//        getEntityManager().merge(entity);
//    }
    /**
     *
     * @param entityId
     * @param entity
     * @return
     */
    @Override
    public T update(final Long entityId, final T entity) {
        T oldEntity = this.find(entityId);
        oldEntity.merge(entity);
        return oldEntity;
    }

    /**
     *
     * @param entity
     */
    @Override
    public void remove(T entity) {
        getEntityManager().remove(getEntityManager().merge(entity));
    }

    /**
     *
     * @param id
     * @return
     */
    @Override
    public T find(final Long id) {
        return getEntityManager().find(entityClass, id);
    }

    /**
     *
     * @return
     */
    @Override
    public List<T> findAll() {
        CriteriaQuery cq =
                getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        return getEntityManager().createQuery(cq).getResultList();
    }

    /**
     *
     * @param range
     * @return
     */
    @Override
    public List<T> findRange(int[] range) {
        CriteriaQuery cq =
                getEntityManager().getCriteriaBuilder().createQuery();
        cq.select(cq.from(entityClass));
        Query q = getEntityManager().createQuery(cq);
        q.setMaxResults(range[1] - range[0]);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }

    /**
     *
     * @return
     */
    @Override
    public int count() {
        CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
        Root<T> rt = cq.from(entityClass);
        cq.select(getEntityManager().getCriteriaBuilder().count(rt));
        Query q = getEntityManager().createQuery(cq);
        return ( (Long) q.getSingleResult() ).intValue();
    }

    @AroundInvoke
    public Object interceptor(InvocationContext ic) throws Exception {
        Object o = null;
        try {
            o = ic.proceed();
            //if (!sessionContext.getRollbackOnly()) {
            //    entityManager.flush();
            //}
        }
        catch (javax.persistence.NoResultException e) {
            throw new PersistenceException(e);
        }
        return o;
    }
}

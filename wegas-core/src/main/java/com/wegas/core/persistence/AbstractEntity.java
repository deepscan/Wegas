/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.persistence;

import com.wegas.core.persistence.variable.Beanjection;
import com.wegas.core.persistence.views.Views;
import com.wegas.core.rest.util.JsonbProvider;
import com.wegas.core.security.util.WegasPermission;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import javax.json.bind.Jsonb;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheCoordinationType;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Francois-Xavier Aeberhard (fx at red-agent.com)
 */
/**
 * Default EclipseLink coodinationType (SEND_OBJECT_CHANGE) leads to buggy coordination for some object (eg ChoiceDescriptor and result).
 * INVALIDATE_CHANGED_OBJECTS must be set to fix this problem.
 * <p>
 * INVALIDATE OBJECT FIX DirectCollectionMapping NPE
 */
@MappedSuperclass
@Cache(coordinationType = CacheCoordinationType.INVALIDATE_CHANGED_OBJECTS)
public abstract class AbstractEntity implements Serializable, Cloneable, WithPermission, JsonSerializable {

    private static final long serialVersionUID = -2538440276749623728L;

    static final private org.slf4j.Logger logger = LoggerFactory.getLogger(AbstractEntity.class);

    /**
     * Get entity id
     *
     * @return entity id
     */
    abstract public Long getId();

    /**
     * Merge other into this
     *
     * @param other the entity to copy values from
     */
    public abstract void merge(AbstractEntity other);

    @Transient
    @JsonbTransient
    private boolean persisted = false;

    /**
     * this hashCode is base on id and class hashcode
     *
     * @return a hashCode value for this entity
     *
     */
    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + (getId() != null ? getId().hashCode() : 0);
        hash = 31 * hash + getClass().hashCode();
        return hash;
    }

    /**
     * Determine if the given entity equals this. To be equal, both objects must
     * have the id and being instances of the same class
     *
     * @param object entity to compare to
     *
     * @return true if object equals this, false otherwise
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (object == null) {
            return false;
        }

        if (this.getClass() != object.getClass()) {                             // First, the two object shall be instances of the same class
            return false;
        }

        if (object instanceof AbstractEntity) {                                 // Then, object shall be an AbstractEntity
            AbstractEntity other = (AbstractEntity) object;
            return this.getId() != null && this.getId().equals(other.getId());
        }
        return false;
    }

    /**
     * Make a copy of this entity
     *
     * @return the new copied entity
     */
    @Override
    public AbstractEntity clone() {
        AbstractEntity ae = null;
        try {
            ae = this.getClass().newInstance();
            ae.merge(this);
        } catch (InstantiationException | IllegalAccessException ex) {
            logger.error("Error during clone", ex);
        }
        return ae;
    }

    /**
     * Duplicate an entity by using Jackson Mapper and provided view
     *
     * @param view
     *
     * @return copy of this
     *
     * @throws IOException
     */
    public AbstractEntity duplicate(Class view) throws IOException {
        Jsonb jsonb = JsonbProvider.getMapper(view);
        String serialized = jsonb.toJson(this);
        return jsonb.fromJson(serialized, this.getClass());
    }

    /**
     * Same as duplicate(Views.Export)
     *
     * @return copy of this
     *
     * @throws IOException
     */
    public AbstractEntity duplicate() throws IOException {
        return this.duplicate(Views.Export.class);
    }

    /**
     * String representation of this
     *
     * @return a String which contains the class name + id
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "( " + getId() + " )";
    }

    /**
     * Default behaviour is to do nothing
     * <p>
     * Overriding this method may helps to maintain cache integrity after
     * cascaded entity deletion
     *
     * @param beans facade wrapper
     */
    public void updateCacheOnDelete(Beanjection beans) {
    }

    /**
     * Comma-separated list of permission, only one is required to grand the permission
     * <p>
     * <ul>
     * <li>null means no special permission required</li>
     * <li>empty string "" means completely forbidden</li>
     * </ul>
     *
     * @return
     */
    @JsonbTransient
    @Override
    public Collection<WegasPermission> getRequieredCreatePermission() {
        return this.getRequieredUpdatePermission();
    }

    @JsonbTransient
    @Override
    public Collection<WegasPermission> getRequieredReadPermission() {
        return this.getRequieredUpdatePermission();
    }

    @JsonbTransient
    @Override
    public Collection<WegasPermission> getRequieredDeletePermission() {
        return this.getRequieredUpdatePermission();
    }

    public boolean isPersisted() {
        return persisted;
    }

    /**
     * MUST BE PACKAGE PROTECTED !!!
     *
     * @param persisted
     */
    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }
}

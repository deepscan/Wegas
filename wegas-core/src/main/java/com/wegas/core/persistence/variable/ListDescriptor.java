/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.persistence.variable;

import com.fasterxml.jackson.annotation.JsonView;
import com.wegas.core.exception.client.WegasErrorMessage;
import com.wegas.core.exception.client.WegasIncompatibleType;
import com.wegas.core.persistence.AbstractEntity;
import com.wegas.core.persistence.game.GameModel;
import com.wegas.core.rest.util.Views;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Francois-Xavier Aeberhard (fx at red-agent.com)
 */
@Entity
@NamedQuery(name = "ListDescriptor.findDistinctChildrenLabels",
        query = "SELECT DISTINCT(child.label) FROM VariableDescriptor child WHERE child.parentList.id = :containerId")
public class ListDescriptor extends VariableDescriptor<VariableInstance> implements DescriptorListI<VariableDescriptor> {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ListDescriptor.class);
    /**
     *
     */
    //@BatchFetch(BatchFetchType.IN)
    @OneToMany(mappedBy = "parentList", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @OrderColumn(name = "ld_items_order")
    private List<VariableDescriptor> items = new ArrayList<>();

    /**
     * List of allowed children types
     */
    @ElementCollection
    private List<String> allowedTypes = new ArrayList<>();

    private String addShortcut = "";

    /**
     *
     */
    public ListDescriptor() {
        super();
    }

    /**
     *
     * @param name
     */
    public ListDescriptor(String name) {
        super(name);
    }

    /*  @Override
     public Boolean contains(String criteria) {
     if (super.contains(criteria)) {
     return true;
     } else {
     for (VariableDescriptor d : this.getItems()) {
     if (d.contains(criteria)) {
     return true;
     }
     }
     }
     return false;
     }*/
    /**
     *
     * @param gameModel
     */
    @Override
    public void setGameModel(GameModel gameModel) {
        super.setGameModel(gameModel);
        for (VariableDescriptor item : this.getItems()) {
            item.setGameModel(gameModel);
        }
    }

    /**
     * @return the variableDescriptors
     */
    @Override
    @JsonView(Views.ExportI.class)
    public List<VariableDescriptor> getItems() {
        return this.items;
    }

    /**
     * @param items
     */
    @Override
    public void setItems(List<VariableDescriptor> items) {
        this.items = new ArrayList<>();
        for (VariableDescriptor vd : items) {
            this.addItem(vd);
        }
    }

    @Override
    public void setChildParent(VariableDescriptor child) {
        if (isAuthorized(child)) {
            child.setParentList(this);
        } else {
            throw WegasErrorMessage.error(child.getClass().getSimpleName() + " not allowed in this folder");
        }
    }

    /**
     *
     * @param type
     */
    private boolean isAuthorized(String type) {
        return (this.getAllowedTypes().isEmpty() || this.getAllowedTypes().contains(type));
    }

    /**
     *
     * @param child
     */
    private boolean isAuthorized(VariableDescriptor child) {
        return this.isAuthorized(child.getJSONClassName());
    }

    /**
     * Get the list of allowed types
     *
     * @return allowed types
     */
    public List<String> getAllowedTypes() {
        return this.allowedTypes;
    }

    /**
     * set the list of allowed types
     *
     * @param types allowed types
     */
    public void setAllowedTypes(List<String> types) {
        this.allowedTypes = types;
    }

    public String getAddShortcut() {
        return addShortcut;
    }

    public void setAddShortcut(String addShortcut) {
        this.addShortcut = addShortcut;
    }

    public List<VariableDescriptor> flatten() {
        final List<VariableDescriptor> acc = new ArrayList<>();
        for (VariableDescriptor v : this.getItems()) {
            if (v instanceof ListDescriptor) {
                acc.addAll(((ListDescriptor) v).flatten());
            } else {
                acc.add(v);
            }
        }
        return acc;
    }

    @Override
    public void merge(AbstractEntity a) {
        if (a instanceof ListDescriptor) {
            super.merge(a);
            ListDescriptor o = (ListDescriptor) a;
            this.setAllowedTypes(new ArrayList<>());
            this.getAllowedTypes().addAll(o.getAllowedTypes());
            if (o.getAddShortcut() == null || o.getAddShortcut().isEmpty() || isAuthorized(o.getAddShortcut())) {
                this.setAddShortcut(o.getAddShortcut());
            } else {
                throw WegasErrorMessage.error(o.getAddShortcut() + " not allowed in this folder");
            }
        } else {
            throw new WegasIncompatibleType(this.getClass().getSimpleName() + ".merge (" + a.getClass().getSimpleName() + ") is not possible");
        }
    }

}

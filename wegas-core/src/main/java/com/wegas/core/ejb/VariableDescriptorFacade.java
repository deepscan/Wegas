/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.ejb;

import com.wegas.core.AlphanumericComparator;
import com.wegas.core.Helper;
import com.wegas.core.api.VariableDescriptorFacadeI;
import com.wegas.core.exception.client.WegasErrorMessage;
import com.wegas.core.exception.internal.WegasNoResultException;
import com.wegas.core.merge.utils.MergeHelper;
import com.wegas.core.persistence.InstanceOwner;
import com.wegas.core.persistence.game.GameModel;
import com.wegas.core.persistence.game.Player;
import com.wegas.core.persistence.variable.Beanjection;
import com.wegas.core.persistence.variable.DescriptorListI;
import com.wegas.core.persistence.variable.ListDescriptor;
import com.wegas.core.persistence.variable.ListInstance;
import com.wegas.core.persistence.variable.ModelScoped;
import com.wegas.core.persistence.variable.ModelScoped.Visibility;
import com.wegas.core.persistence.variable.VariableDescriptor;
import com.wegas.core.persistence.variable.VariableInstance;
import com.wegas.core.persistence.variable.primitive.NumberInstance;
import com.wegas.core.persistence.variable.scope.AbstractScope;
import com.wegas.core.persistence.variable.scope.GameModelScope;
import com.wegas.core.persistence.variable.scope.GameScope;
import com.wegas.core.persistence.variable.scope.PlayerScope;
import com.wegas.core.persistence.variable.scope.TeamScope;
import com.wegas.core.security.ejb.UserFacade;
import com.wegas.mcq.ejb.QuestionDescriptorFacade;
import com.wegas.mcq.persistence.QuestionDescriptor;
import com.wegas.resourceManagement.ejb.IterationFacade;
import com.wegas.resourceManagement.ejb.ResourceFacade;
import com.wegas.reviewing.ejb.ReviewingFacade;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Francois-Xavier Aeberhard (fx at red-agent.com)
 */
@Stateless
@LocalBean
public class VariableDescriptorFacade extends BaseFacade<VariableDescriptor> implements VariableDescriptorFacadeI {

    private static final Logger logger = LoggerFactory.getLogger(VariableDescriptorFacade.class);

    /**
     *
     */
    @EJB
    private GameModelFacade gameModelFacade;

    @EJB
    private VariableInstanceFacade variableInstanceFacade;

    @Inject
    private ResourceFacade resourceFacade;

    @Inject
    private IterationFacade iterationFacade;

    @Inject
    private ReviewingFacade reviewingFacade;

    @Inject
    private UserFacade userFacade;

    @Inject
    private TeamFacade teamFacade;

    @Inject
    private ModelFacade mergeFacade;

    private QuestionDescriptorFacade questionDescriptorFacade;

    private Beanjection beans = null;

    private Beanjection getBeans() {
        if (beans == null) {
            logger.error("INIT BEANS");
            beans = new Beanjection(variableInstanceFacade, this,
                    resourceFacade, iterationFacade,
                    reviewingFacade, userFacade, teamFacade, questionDescriptorFacade);
        }
        return beans;
    }

    /**
     *
     */
    public VariableDescriptorFacade() {
        super(VariableDescriptor.class);
    }

    /**
     * @param variableDescriptor
     */
    @Override
    public void create(final VariableDescriptor variableDescriptor) {
        throw WegasErrorMessage.error("Unable to call create on Variable descriptor. Use create(gameModelId, variableDescriptor) instead.");
    }

    @Override
    public VariableDescriptor update(final Long entityId, final VariableDescriptor entity) {
        final VariableDescriptor vd = this.find(entityId);
        //entity.setGameModel(vd.getGameModel());
        vd.merge(entity);
        this.revive(vd.getGameModel(), vd, false);
        return vd;
    }

    /**
     * Create a new descriptor in a DescriptorListI
     *
     * @param gameModel the gameModel
     * @param list      new descriptor parent
     * @param entity    new descriptor to create
     *
     * @return the new descriptor
     */
    public VariableDescriptor createChild(final GameModel gameModel, final DescriptorListI<VariableDescriptor> list, final VariableDescriptor entity) {

        List<String> usedNames = this.findDistinctNames(gameModel);
        List<String> usedLabels = this.findDistinctLabels(list);

        boolean hasName = !Helper.isNullOrEmpty(entity.getName());
        boolean hasLabel = !Helper.isNullOrEmpty(entity.getLabel());

        if (hasName && !hasLabel) {
            entity.setLabel(entity.getName());
        } else if (hasLabel && !hasName) {
            entity.setName(entity.getLabel());
        }

        Helper.setUniqueName(entity, usedNames);
        Helper.setUniqueLabel(entity, usedLabels);

        list.addItem(entity);
        this.revive(gameModel, entity, true);

        return entity;
    }

    /**
     * @param gameModel
     * @param entity
     * @param propagate indicate whether default instance should be propagated
     */
    public void shallowRevive(GameModel gameModel, VariableDescriptor entity, boolean propagate) {
        if (entity.getScope() == null) {
            entity.setScope(new TeamScope());
            propagate = true;
        } else if (entity.getScope().getShouldCreateInstance()) {
            propagate = true;
            entity.getScope().setShouldCreateInstance(false);
        }

        if (entity instanceof ListDescriptor) {
            VariableInstance defaultInstance = entity.getDefaultInstance();
            if (defaultInstance instanceof NumberInstance) {
                logger.error("Incompatible default instance {}", defaultInstance);
                entity.setDefaultInstance(new ListInstance());
            }
        }

        /*
         * This flush is required by several EntityRevivedEvent listener, 
         * which opperate some SQL queries (which didn't return anything before
         * entites have been flushed to database
         *
         * for instance, reviving a taskDescriptor needs to fetch others tasks by name, 
         * it will not return any result if this flush not occurs
         */
        this.getEntityManager().flush();

        this.reviveDescriptor(entity);
        variableInstanceFacade.reviveInstance(entity.getDefaultInstance());

        // @TODO find a smarter way to decide to propagate or not to propatate...
        if (propagate) {
            entity.getScope().setBeanjection(new Beanjection(variableInstanceFacade));
            gameModelFacade.resetAndReviveScopeInstances(entity);
        }

        if (gameModel != null) {
            gameModel.addToVariableDescriptors(entity);
        }
    }

    public void revive(GameModel gameModel, VariableDescriptor entity, boolean propagate) {
        this.shallowRevive(gameModel, entity, propagate);
        if (entity instanceof DescriptorListI) {
            this.reviveItems(gameModel, (DescriptorListI) entity, propagate); // also revive children
        }
    }

    public void reviveDescriptor(VariableDescriptor vd) {
        vd.revive(getBeans());
    }

    /**
     * @param gameModel
     * @param entity
     */
    public void reviveItems(GameModel gameModel, DescriptorListI entity, boolean propagate) {
        for (Object vd : entity.getItems()) {
            this.revive(gameModel, (VariableDescriptor) vd, propagate);
        }
    }

    /**
     * @param gameModel
     * @param entity
     */
    public void preDestroy(GameModel gameModel, VariableDescriptor entity) {
        if (gameModel != null) {
            gameModel.removeFromVariableDescriptors(entity);
        }

        Collection<VariableInstance> values = this.getInstances(entity).values();
        logger.debug("PreDestroy: remove {} entities for {}", values.size(), entity);
        for (VariableInstance vi : values) {
            logger.debug("Destroy {}", vi);
            variableInstanceFacade.remove(vi);
        }

        // Cascade base deletion...
        //variableInstanceFacade.remove(entity.getDefaultInstance());
        if (entity instanceof DescriptorListI) {
            this.preDestroyItems(gameModel, (DescriptorListI) entity);
        }
    }

    /**
     * @param gameModel
     * @param entity
     */
    public void preDestroyItems(GameModel gameModel, DescriptorListI entity) {
        for (Object vd : entity.getItems()) {
            this.preDestroy(gameModel, (VariableDescriptor) vd);
        }
    }

    /**
     * @param variableDescriptorId is of the parent
     * @param entity               new descriptor to add to the parent
     *
     * @return the new child
     */
    public VariableDescriptor createChild(final Long variableDescriptorId, final VariableDescriptor entity) {
        VariableDescriptor find = this.find(variableDescriptorId);
        return this.createChild(find.getGameModel(), (DescriptorListI) find, entity);
    }

    /**
     * Create descriptor at gameModel rootLevel
     *
     * @param gameModelId
     * @param variableDescriptor
     */
    public void create(final Long gameModelId, final VariableDescriptor variableDescriptor) {
        GameModel find = this.gameModelFacade.find(gameModelId);
        /*
        for (Game g : find.getGames()) {
            logger.error("Game {}",  g);
            for (Team t : g.getTeams()) {
                logger.error("  Team {} -> {}",  t, t.getStatus());
                for (Player p : t.getPlayers()) {
                    logger.error("    Player {} -> {}",  p, p.getStatus());
                }
            }
        } // */
        this.createChild(find, find, variableDescriptor);
    }

    /**
     * @param entityId
     *
     * @return the new descriptor
     *
     * @throws java.lang.CloneNotSupportedException
     *
     */
    @Override
    public VariableDescriptor duplicate(final Long entityId) throws CloneNotSupportedException {
        final VariableDescriptor oldEntity = this.find(entityId); // Retrieve the entity to duplicate
        final VariableDescriptor newEntity = (VariableDescriptor) oldEntity.duplicate();

        // reset reference id for all new entites within newEntity
        MergeHelper.resetRefIds(newEntity, null);
        if (oldEntity.isProtected()){
            MergeHelper.resetVisibility(newEntity, Visibility.PRIVATE);
        }

        final DescriptorListI list = oldEntity.getParent();
        this.createChild(oldEntity.getGameModel(), list, newEntity);
        return newEntity;
    }

    @Override
    public void remove(VariableDescriptor entity) {
        this.preDestroy(entity.getGameModel(), entity);
        entity.getParent().remove(entity);

        getEntityManager().remove(entity);
    }

    /**
     * @param vd
     *
     * @return descriptor container
     *
     * @deprecated use {@link VariableDescriptor#getParent()}
     */
    @Override
    public DescriptorListI findParentList(VariableDescriptor vd) throws NoResultException {
        return vd.getParent();
    }

    /**
     * @param item
     *
     * @return the parent descriptor
     *
     * @throws WegasNoResultException if the desciptor is at root-level
     * @deprecated use {@link VariableDescriptor#getParentList()}
     */
    @Override
    public ListDescriptor findParentListDescriptor(final VariableDescriptor item) throws WegasNoResultException {
        if (item.getParentList() != null) {
            return item.getParentList();
        } else {
            throw new WegasNoResultException();
        }
    }

    @Override
    public boolean hasVariable(final GameModel gameModel, final String name) {
        try {
            this.find(gameModel, name);
            return true;
        } catch (WegasNoResultException ex) {
            return false;
        }

    }

    /**
     * @param gameModel
     * @param name
     *
     * @return the gameModel descriptor matching the name
     *
     * @throws WegasNoResultException
     */
    @Override
    public VariableDescriptor find(final GameModel gameModel, final String name) throws WegasNoResultException {
//        for (VariableDescriptor vd : gameModel.getVariableDescriptors()) {
//            if (name.equals(vd.getName())) {
//                return vd;
//            }
//        }
//        throw new WegasNoResultException();

        try {
            TypedQuery<VariableDescriptor> query = getEntityManager().createNamedQuery("VariableDescriptor.findByGameModelIdAndName", VariableDescriptor.class);
            query.setParameter("gameModelId", gameModel.getId());
            query.setParameter("name", name);
            return query.getSingleResult();
        } catch (NoResultException ex) {
            throw new WegasNoResultException("Variable \"" + name + "\" not found in gameModel " + gameModel, ex);
        }
    }

    /**
     * @param gameModel
     *
     * @return all descriptor names already in use within the gameModel
     */
    public List<String> findDistinctNames(final GameModel gameModel) {
        TypedQuery<String> query;
        if (gameModel.isModel()) {
            // names from the models and all scenarios
            query = getEntityManager().createNamedQuery("VariableDescriptor.findAllNamesInModelAndItsScenarios", String.class);
        } else {
            // names from the gamemodel and its model
            query = getEntityManager().createNamedQuery("VariableDescriptor.findAllNamesInScenarioAndItsModel", String.class);
        }

        query.setParameter("gameModelId", gameModel.getId());

        return query.getResultList();
    }

    /**
     * @param container
     *
     * @return all descriptor labels already in use within the given descriptor
     *         container
     */
    public List<String> findDistinctLabels(final DescriptorListI<? extends VariableDescriptor> container) {
        if (container instanceof GameModel) {
            TypedQuery<String> distinctLabels = getEntityManager().createNamedQuery("GameModel.findDistinctChildrenLabels", String.class);
            distinctLabels.setParameter("containerId", container.getId());
            return distinctLabels.getResultList();
        } else if (container instanceof ListDescriptor) {
            TypedQuery<String> distinctLabels = getEntityManager().createNamedQuery("ListDescriptor.findDistinctChildrenLabels", String.class);
            distinctLabels.setParameter("containerId", container.getId());
            return distinctLabels.getResultList();
        } else if (container instanceof QuestionDescriptor) {
            TypedQuery<String> distinctLabels = getEntityManager().createNamedQuery("QuestionDescriptor.findDistinctChildrenLabels", String.class);
            distinctLabels.setParameter("containerId", container.getId());
            return distinctLabels.getResultList();
        } else {
            // fallback case
            List<String> list = new ArrayList<>();
            for (VariableDescriptor child : container.getItems()) {
                list.add(child.getLabel());
            }
            return list;
        }
    }

    /**
     * For backward compatibility, use find(final GameModel gameModel, final
     * String name) instead.
     *
     * @param gameModel
     * @param name
     *
     * @return the gameModel descriptor matching the name
     *
     * @throws com.wegas.core.exception.internal.WegasNoResultException
     * @deprecated
     */
    @Override
    @Deprecated
    public VariableDescriptor findByName(final GameModel gameModel, final String name) throws WegasNoResultException {
        return this.find(gameModel, name);
    }

    /**
     * @param gameModel
     * @param label
     *
     * @return the gameModel descriptor matching the label
     *
     * @throws com.wegas.core.exception.internal.WegasNoResultException
     */
    @Override
    public VariableDescriptor findByLabel(final GameModel gameModel, final String label) throws WegasNoResultException {
        // TODO update to handle label duplicata
        final CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<VariableDescriptor> cq = cb.createQuery(VariableDescriptor.class);
        final Root<VariableDescriptor> variableDescriptor = cq.from(VariableDescriptor.class);
        cq.where(cb.and(
                cb.equal(variableDescriptor.get("gameModel").get("id"), gameModel.getId()),
                cb.equal(variableDescriptor.get("label"), label)));
        final TypedQuery<VariableDescriptor> q = getEntityManager().createQuery(cq);
        try {
            return q.getSingleResult();
        } catch (NoResultException ex) {
            throw new WegasNoResultException(ex);
        }
    }

    /**
     * @param gameModel
     * @param title     title we look for
     *
     * @return all gameModel descriptors with the given title
     */
    @Override
    public List<VariableDescriptor> findByTitle(final GameModel gameModel, final String title) {

        List<VariableDescriptor> result = new ArrayList<>();
        if (title != null) {
            for (VariableDescriptor vd : gameModel.getVariableDescriptors()) {
                if (title.equals(vd.getTitle())) {
                    result.add(vd);
                }
            }
        }
        return result;
    }

    /**
     * @param gameModelId
     *
     * @return all gameModel descriptors
     */
    @Override
    public Collection<VariableDescriptor> findAll(final Long gameModelId) {
        return gameModelFacade.find(gameModelId).getVariableDescriptors();
    }

    /**
     * @param gameModelId
     *
     * @return gameModel root-level descriptor
     */
    @Override
    public List<VariableDescriptor> findByGameModelId(final Long gameModelId) {
        return gameModelFacade.find(gameModelId).getChildVariableDescriptors();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public <T extends VariableDescriptor> List<T> findByClass(final GameModel gamemodel, final Class<T> variableDescriptorClass) {
        //Cannot be a namedQuery, find by TYPE() removes subclasses
        final CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<T> cq = cb.createQuery(variableDescriptorClass);
        final Root<T> variableDescriptor = cq.from(variableDescriptorClass);
        cq.where(cb.equal(variableDescriptor.get("gameModel").get("id"), gamemodel.getId()));
        final TypedQuery<T> q = getEntityManager().createQuery(cq);
        return q.getResultList();

        //Query findVariableDescriptorsByClass = getEntityManager().createQuery("SELECT DISTINCT variableDescriptor FROM " + variableDescriptorClass.getSimpleName() + " variableDescriptor LEFT JOIN variableDescriptor.gameModel AS gm WHERE gm.id =" + gameModelId, variableDescriptorClass);
        //return findVariableDescriptorsByClass.getResultList();
    }

    /**
     *
     * @param descriptorId         id of the descriptor to move
     * @param targetListDescriptor new parent
     * @param index                index in new parent
     *
     */
    private void move(final Long descriptorId, final DescriptorListI<VariableDescriptor> targetListDescriptor, final int index) {

        final VariableDescriptor vd = this.find(descriptorId);
        DescriptorListI from = vd.getParent();

        Visibility targetVisibility = targetListDescriptor instanceof ModelScoped ? ((ModelScoped) targetListDescriptor).getVisibility() : Visibility.INHERITED;

        if (!vd.isProtected() || (vd.getVisibility() == ModelScoped.Visibility.PRIVATE)) {
            if (!vd.isProtected() || targetVisibility == Visibility.INHERITED || targetVisibility == Visibility.PRIVATE) {
                from.localRemove(vd);
                targetListDescriptor.addItem(index, vd);
            } else {
                if (targetListDescriptor instanceof VariableDescriptor) {
                    throw WegasErrorMessage.error("Updating " + ((VariableDescriptor<VariableInstance>) targetListDescriptor).getLabel() + " is not authorized");
                } else {
                    throw WegasErrorMessage.error("Updating " + targetListDescriptor + " is not authorized");
                }
            }
        } else {
            throw WegasErrorMessage.error("Moving " + vd.getLabel() + " is not authorized");
        }
    }

    /**
     * This method will move the target entity to the root level of the game
     * model at index i
     *
     * @param descriptorId
     * @param index
     */
    public void move(final Long descriptorId, final int index) {
        this.move(descriptorId, this.find(descriptorId).getGameModel(), index);
    }

    /**
     * @param descriptorId
     * @param targetListDescriptorId
     * @param index
     */
    public void move(final Long descriptorId, final Long targetListDescriptorId, final int index) {
        this.move(descriptorId, (DescriptorListI) this.find(targetListDescriptorId), index);
    }

    /**
     * Sort naturally items in ListDescriptor by label
     *
     * @param descriptorId ListDescriptor's id to sort
     *
     * @return sorted VariableDescriptor
     */
    public VariableDescriptor sort(final Long descriptorId) {
        VariableDescriptor variableDescriptor = this.find(descriptorId);
        if (variableDescriptor instanceof ListDescriptor) {
            /*
             * Collection cannot be sorted directly, must pass through methods remove / add
             */
            ListDescriptor listDescriptor = (ListDescriptor) variableDescriptor;
            List<VariableDescriptor> list = new ArrayList<>(listDescriptor.getItems());
            final AlphanumericComparator<String> alphanumericComparator = new AlphanumericComparator<>();
            final Comparator<VariableDescriptor> comparator = new Comparator<VariableDescriptor>() {
                @Override
                public int compare(VariableDescriptor o1, VariableDescriptor o2) {
                    return alphanumericComparator.compare(o1.getLabel(), o2.getLabel());
                }
            };

            Collections.sort(list, comparator);

            for (VariableDescriptor vd : list) {
                listDescriptor.remove(vd);
                listDescriptor.addItem(vd);
            }
        }
        return variableDescriptor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<? extends InstanceOwner, VariableInstance> getInstances(VariableDescriptor vd) {
        return variableInstanceFacade.getAllInstances(vd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Long, VariableInstance> getInstancesByKeyId(VariableDescriptor vd) {
        return variableInstanceFacade.getAllInstancesById(vd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VariableInstance getInstance(VariableDescriptor vd, Player player) {
        AbstractScope scope = vd.getScope();
        if (scope instanceof TeamScope) {
            return variableInstanceFacade.getTeamInstance((TeamScope) scope, player.getTeam());
        } else if (scope instanceof GameScope) {
            return variableInstanceFacade.getGameInstance((GameScope) scope, player.getGame());
        } else if (scope instanceof PlayerScope) {
            return variableInstanceFacade.getPlayerInstance((PlayerScope) scope, player);
        } else if (scope instanceof GameModelScope) {
            return scope.getVariableInstance(player.getGameModel());
        }
        return null;
    }

    /**
     *
     * @param vd
     * @param newScope
     */
    public void updateScope(VariableDescriptor vd, AbstractScope newScope) {
        if (vd.getScope() != null) {
            Collection<VariableInstance> values = variableInstanceFacade.getAllInstances(vd).values();

            for (VariableInstance vi : values) {
                variableInstanceFacade.remove(vi);
            }
        }
        vd.setScope(newScope);
        this.getEntityManager().persist(vd);
        vd = this.find(vd.getId());
        gameModelFacade.resetAndReviveScopeInstances(vd);
    }

    /**
     * @return Looked-up EJB
     */
    public static VariableDescriptorFacade lookup() {
        try {
            return Helper.lookupBy(VariableDescriptorFacade.class);
        } catch (NamingException ex) {
            logger.error("Error retrieving var desc facade", ex);
            return null;
        }
    }
}

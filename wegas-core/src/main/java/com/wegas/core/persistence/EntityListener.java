/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.persistence;

import com.wegas.core.ejb.RequestManager;
import com.wegas.core.ejb.TeamFacade;
import com.wegas.core.ejb.VariableDescriptorFacade;
import com.wegas.core.ejb.VariableInstanceFacade;
import com.wegas.core.persistence.game.Game;
import com.wegas.core.persistence.game.GameModel;
import com.wegas.core.persistence.game.Player;
import com.wegas.core.persistence.game.Team;
import com.wegas.core.persistence.variable.Beanjection;
import com.wegas.core.persistence.variable.VariableDescriptor;
import com.wegas.core.persistence.variable.VariableInstance;
import com.wegas.core.security.ejb.UserFacade;
import com.wegas.mcq.ejb.QuestionDescriptorFacade;
import com.wegas.resourceManagement.ejb.IterationFacade;
import com.wegas.resourceManagement.ejb.ResourceFacade;
import com.wegas.reviewing.ejb.ReviewingFacade;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Maxence Laurent (maxence.laurent at gmail.com)
 */
public class EntityListener {

    private static final Logger logger = LoggerFactory.getLogger(EntityListener.class);

    @Inject
    private RequestManager requestManager;

    @Inject
    private VariableInstanceFacade variableInstanceFacade;

    @Inject
    private VariableDescriptorFacade variableDescriptorFacade;

    @Inject
    private ResourceFacade resourceFacade;

    @Inject
    private IterationFacade iterationFacade;

    @Inject
    private UserFacade userFacade;

    @Inject
    private ReviewingFacade reviewingFacade;

    @Inject
    private QuestionDescriptorFacade questionDescriptorFacade;

    @Inject
    private TeamFacade teamFacade;

    private Beanjection getBeansjection() {
        return new Beanjection(variableInstanceFacade, variableDescriptorFacade, resourceFacade, iterationFacade, reviewingFacade, userFacade, teamFacade, questionDescriptorFacade);
    }

    @PrePersist
    void onPrePersist(Object o) {
        //Do not remove this empty method nor its @PrePersist annotation !!!!
        // Remove this method  makes CDI injection fails ...
    }

    @PostPersist
    void onPostPersist(Object o) {
        if (o instanceof AbstractEntity) {
            logger.debug("PostPersist {}", o);
            if (requestManager != null) {
                requestManager.assertCreateRight((AbstractEntity) o);
            } else {
                logger.error("PostPersist NO SECURITY FACADE");
            }
        }

        if (o instanceof Broadcastable) {
            Broadcastable b = (Broadcastable) o;
            Map<String, List<AbstractEntity>> entities = b.getEntities();
            if (b instanceof Team || b instanceof Player || b instanceof VariableInstance || b instanceof VariableDescriptor) {
                logger.debug("PropagateNew: {} :: {}", b.getClass().getSimpleName(), ((AbstractEntity) b).getId());
                requestManager.addUpdatedEntities(entities);
            } else {
                logger.debug("Unhandled new broadcastable entity: {}", b);
            }
        }
    }

    @PostUpdate
    void onPostUpdate(Object o) {

        if (o instanceof AbstractEntity) {
            logger.debug("PostUpdate {}", o);
            if (requestManager != null) {
                requestManager.assertUpdateRight((AbstractEntity) o);
            } else {
                logger.error("PostUpdate NO SECURITY FACADE");
            }
        }

        if (o instanceof Broadcastable) {
            Broadcastable b = (Broadcastable) o;
            if (b instanceof AbstractEntity) {
                logger.debug("PropagateUpdate: {} :: {}", b.getClass().getSimpleName(), ((AbstractEntity) b).getId());
                Map<String, List<AbstractEntity>> entities = b.getEntities();
                requestManager.addUpdatedEntities(entities);
            }
        }
    }

    @PreRemove
    void onPreRemove(Object o) {
        if (o instanceof AbstractEntity) {
            AbstractEntity ae = (AbstractEntity) o;
            if (requestManager != null) {
                requestManager.assertDeleteRight(ae);
            } else {
                logger.error("PreREMOVE NO SECURITY FACADE");
            }
        }

        if (o instanceof Broadcastable) {
            Broadcastable b = (Broadcastable) o;
            Map<String, List<AbstractEntity>> entities = b.getEntities();
            if (entities != null) {
                if (b instanceof VariableDescriptor || b instanceof VariableInstance || b instanceof Game || b instanceof GameModel) {
                    logger.debug("PropagateDestroy (#: {}): {} :: {}", entities.size(), b.getClass().getSimpleName(), ((AbstractEntity) b).getId());
                    requestManager.addDestroyedEntities(entities);
                } else if (b instanceof Team || b instanceof Player) {
                    logger.debug("PropagateUpdateOnDestroy (#: {}): {} :: {}", entities.size(), b.getClass().getSimpleName(), ((AbstractEntity) b).getId());
                    requestManager.addUpdatedEntities(entities);
                } else {
                    logger.debug("Unhandled destroyed broadcastable entity: {}", b);
                }
            }
        }

        if (o instanceof AbstractEntity) {
            AbstractEntity ae = (AbstractEntity) o;
            ae.updateCacheOnDelete(getBeansjection());
        }
    }

    @PostLoad
    void onPostLoad(Object o) {
        if (o instanceof AcceptInjection) {
            AcceptInjection id = (AcceptInjection) o;
            id.setBeanjection(getBeansjection());
        }

        if (o instanceof AbstractEntity) {
            logger.debug("PostLoad {}", o);
            ((AbstractEntity) o).setPersisted(true);
            if (requestManager != null) {
                requestManager.assertReadRight((AbstractEntity) o);
            } else {
                logger.error("PostLOAD NO SECURITY FACADE");
            }
        }
    }
}

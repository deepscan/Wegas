/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wegas.ejb;

import com.wegas.persistence.variableinstance.ListVariableInstanceEntity;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Francois-Xavier Aeberhard <fx@red-agent.com>
 */
@Stateless
public class ListVariableInstanceEntityFacade extends AbstractFacade<ListVariableInstanceEntity> {
    @PersistenceContext(unitName = "wegasPU")
    private EntityManager em;

    /**
     * 
     * @return
     */
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * 
     */
    public ListVariableInstanceEntityFacade() {
        super(ListVariableInstanceEntity.class);
    }
    
}

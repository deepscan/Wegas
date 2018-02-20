/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.persistence.variable.statemachine;

import com.wegas.core.persistence.JsonSerializable;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author Cyril Junod (cyril.junod at gmail.com)
 */
@Embeddable
public class Coordinate implements Serializable, JsonSerializable {

    private static final long serialVersionUID = 1L;
    @Column(columnDefinition = "SMALLINT")
    private Integer x;
    @Column(columnDefinition = "SMALLINT")
    private Integer y;

    /**
     *
     */
    public Coordinate() {
    }

    /**
     *
     * @return x coordinate
     */
    public Integer getX() {
        return x;
    }

    /**
     *
     * @param x
     */
    public void setX(Integer x) {
        this.x = x;
    }

    /**
     *
     * @return Y coordinate
     */
    public Integer getY() {
        return y;
    }

    /**
     *
     * @param y
     */
    public void setY(Integer y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "EditorPositions{" + "X=" + x + ", Y=" + y + '}';
    }
}

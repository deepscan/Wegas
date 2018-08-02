/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */


YUI.add('wegas-model-propagator', function(Y) {
    'use strict';
    var ModelPropagator, ModelPropagatorModal, ModelPropagatorAction;

    ModelPropagator = Y.Base.create("wegas-model-propagator", Y.Widget,
        [Y.WidgetParent, Y.WidgetChild, Y.Wegas.Editable, Y.Wegas.Parent], {
        renderUI: function() {
            this.add(new Y.Wegas.Text({
                content: "Propagate Model to senarios:"
            }));
        },
        propagate: function() {
            Y.Wegas.Facade.GameModel.sendRequest({
                request: "/propagateModel/" + this.get("gameModel").get("id"),
                cfg: {
                    method: "PUT",
                    updateCache: false
                },
                on: {
                    success: Y.bind(function(e) {
                        Y.Wegas.Alerts.showMessage("info", "Successfull", 1000);
                        this.fire("model:propagated");
                    }, this),
                    failure: Y.bind(function(e) {
                        debugger;
                    }, this)
                }
            });
        }
    }, {
        ATTRS: {
            gameModel: {
                type: "Object"
            }
        }
    });
    Y.Wegas.ModelPropagator = ModelPropagator;

    ModelPropagatorModal = Y.Base.create("wegas-model-propagator-modal", Y.Wegas.Modal, [], {
        initializer: function() {
            var gameModel = Y.Wegas.Facade.GameModel.cache.getCurrentGameModel(),
                actions;

            if (gameModel) {
                actions = [{
                        "types": ["primary"],
                        "label": "<i class='fa fa-rocket'> Propagate",
                        "do": function() {
                            this.item(0).propagate();
                        }
                    }, {
                        "label": 'Cancel',
                        "do": function() {
                            this.close();
                        }
                    }];
                this.set("title", "Propagate Model");
                this.set("icon", "cubes");
                this.add(new Y.Wegas.ModelPropagator({
                    "gameModel": gameModel
                }));
                this.set("actions", actions);
            }
        }
    }, {
        ATTRS: {
            mode: {
                type: "string",
                value: "Create"
            },
            title: {
                type: "string",
                value: "Create A New Scenario Based On A Player"
            },
            openPopup: {
                type: "boolean",
                value: true
            }
        }
    });
    Y.Wegas.ModelPropagatorModal = ModelPropagatorModal;



    ModelPropagatorAction = Y.Base.create("ModelPropagatorAction", Y.Plugin.Action, [], {
        execute: function() {
            new Y.Wegas.ModelPropagatorModal({
                "on": {
                    "model:propagated": function() {
                        this.close();
                    }
                }
            }).render();
        }
    }, {
        NS: "ModelPropagatorAction"
    });
    Y.Plugin.ModelPropagatorAction = ModelPropagatorAction;

});
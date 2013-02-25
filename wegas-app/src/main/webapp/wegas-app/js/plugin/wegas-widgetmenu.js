/*
 * Wegas
 * http://www.albasim.ch/wegas/
 *
 * Copyright (c) 2013 School of Business and Engineering Vaud, Comem
 * Licensed under the MIT License
 */
/**
 * @author Francois-Xavier Aeberhard <fx@red-agent.com>
 */
YUI.add('wegas-widgetmenu', function(Y) {
    "use strict";

    /**
     * @name Y.Plugin.WidgetMenu
     * @extends Y.Plugin.Base
     * @class Plugin to be attached to a Y.Widget, adds a menu
     *  that shows up when the target event is triggered (by default click). Uses
     *  a Y.Wegas.Menu to display the menu.
     * @constructor
     */
    var HOST = "host", BOUNDINGBOX = "boundingBox",
    WidgetMenu = function() {
        WidgetMenu.superclass.constructor.apply(this, arguments);
    };

    Y.extend(WidgetMenu, Y.Plugin.Base, {
        /** @lends Y.Plugin.WidgetMenu# */

        // *** Lifecycle methods *** //
        /**
         * @function
         * @private
         */
        initializer: function() {
            var host = this.get(HOST);

            if (host instanceof Y.Node) {
                this.createMenuEvent(host);
            } else if (host instanceof Y.Widget) {
                this.afterHostEvent("render", function() {
                    this.createMenuEvent(this.get(HOST).get(BOUNDINGBOX));
                });
            }

        },
        createMenuEvent: function(node) {
            //var bb = this.get(HOST).get(BOUNDINGBOX);

            node.delegate(this.get("event"), function(e) {
                var menu = this.getMenu();                                      // Get a menu instance

                menu.attachTo(e.target);                                        // Attach it to the target node
                e.halt(true);                                                   // Prevent event from bubbling
                this.fire("menuOpen");                                          // Notify the parent the menu has been opened
            }, this.get("selector"), this);


            //bb.append('<span class="wegas-widgetmenu-submenuindicator"></span>');      // Add submenu indicator
            node.addClass("wegas-widgetmenu-hassubmenu");
            //bb.all(this.get("selector")).addClass("wegas-widgetmenu-hassubmenu");
            if (this.get("menuCfg.points") && this.get("menuCfg.points")[0].indexOf("b") < 0) {
                node.addClass("wegas-widgetmenu-hassubmenuright");
            }
        },
        /**
         * @function
         * @private
         */
        show: function() {
            this.getMenu().attachTo(// Get a menu instance
                this.get(HOST).get(BOUNDINGBOX).one(this.get("selector")));     // Attach it to the target node
        },

        /*on: function () {
         var menu = this.getMenu();                                             // Get a menu instance
         menu.on.apply(menu, arguments);
         },*/

        // *** Private methods *** //
        /**
         * @function
         * @private
         */
        getMenu: function() {
            if (!this.menu) {
                var cfg = this.get("menuCfg"),
                host = this.get(HOST),
                parentWidget = host.get("parent");
                cfg.children = this.get("children");

                this.menu = new Menu(cfg);
                this.menu.addTarget(this);                                      // Catch any event generated by the menu

                if (parentWidget &&
                    parentWidget instanceof Menu) {                          // Handle nested menus, events are forwarded to the parent widget
                    this.menu.on("timerCanceled", function() {
                        parentWidget.cancelMenuTimer();
                    }, this);
                    this.menu.on("timerStarted", function() {
                        parentWidget.startMenuHideTimer();
                    }, this);

                }
                //else {
                (host.get("contentBox") || host).delegate("mouseleave", function() {
                    this.menu.startMenuHideTimer(false);
                }, this.get("selector"), this);
            // }
            }
            return this.menu;
        }
    }, {
        /** @lends Y.Plugin.WidgetMenu */

        NS: "menu",
        NAME: "widgetmenu",
        ATTRS: {
            children: {
                value: []
            },
            selector: {
                value: "*"
            },
            menuCfg: {
                value: {}
            },
            event: {
                value: "click"
            }
        }
    });
    Y.namespace('Plugin').WidgetMenu = WidgetMenu;

    /**
     * @name Y.Wegas.Menu
     * @class Menu Widget, an positionnalbe overlay, intendend to be used by the menu plugin.
     * @contstructor
     * @extends Y.Widget
     * @augments Y.WidgetPosition
     * @augments Y.WidgetPositionAlign
     * @augments Y.WidgetStack
     * @augments Y.WidgetParent
     * @augments Y.WidgetPositionConstrain
     */
    var Menu = Y.Base.create("menu", Y.Widget, [Y.WidgetPosition, Y.WidgetPositionAlign, Y.WidgetStack, Y.WidgetParent, Y.WidgetPositionConstrain], {
        /** @lends Y.Wegas.Menu# */

        // *** private fields *** //
        timer: null,

        // *** Lifecycle methods *** //
        initializer: function() {
            this.publish("click", {
                emitFacade: true,
                bubbles: true
            });
            this.publish("cancelMenuTimer", {
                bubbles: true
            });
        },

        renderUI: function() {
            var bb = this.get(BOUNDINGBOX);

            bb.on("clickoutside", this.clickOutside, this);
            bb.on("click", this.menuClick, this);
            bb.on("mouseenter", this.cancelMenuTimer, this);
            bb.on("mouseleave", this.startMenuHideTimer, this);
        },

        bindUI: function() {
            this.on("*:click", function(e) { /*Y.log("fix");*/
                }, this);        // @hack in order for event to be bubbled up
        },

        hide: function() {
            Menu.superclass.hide.call(this);
        },

        // *** Public methods *** //
        /**
         *
         *  Displays the menu next to the provided node and add mouseenter and
         *  mouseleave callbacks to the node
         *
         * @function
         */
        attachTo: function(node) {

            //node.on("mouseenter", this.show, this);
            //node.on("mouseleave", this.hide, this);
            this.currentNode = node;

            this.set("align", {
                node: node,
                points: this.get("points")
            });

            this.cancelMenuTimer();
            this.show();
        // console.log("attachTo", this.get("contentBox").one("button").getHTML());
        },

        // *** Private methods *** //
        menuClick: function() {
            this.hide();
            this.fire("menuClick");
        },

        clickOutside: function(e) {
            if (this.currentNode !== e.target) {
                this.hide();
            }
        },

        startMenuHideTimer: function(fireEvent) {
            //console.log("startMenuHideTimer",this.get("contentBox").one("button").getHTML());
            this.cancelMenuTimer();
            this.timer = Y.later(500, this, this.hide);

            if (!!fireEvent) {
                this.fire("timerStarted");
            }
        },

        cancelMenuTimer: function() {
            //console.log("cancelMenuTimer", this.get("contentBox").one("button").getHTML());
            if (this.timer) {
                this.timer.cancel();
            }
            this.fire("timerCanceled");
        }

    }, {
        /** @lends Y.Wegas.Menu */
        ATTRS: {
            points: {
                value: ["tl", "bl"]
            },
            constrain: {
                value: true
            },
            zIndex: {
                value: 25
            },
            render: {
                value: true
            },
            visible: {
                value: false
            },
            defaultChildType: {
                value: "Button"
            }
        }
    });
    Y.namespace('Wegas').Menu = Menu;

});

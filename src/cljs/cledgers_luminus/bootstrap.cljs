(ns cledgers-luminus.bootstrap
  (:require [reagent.core :as r]
            cljsjs.react-bootstrap))

(def Nav (r/adapt-react-class js/ReactBootstrap.Nav))
(def Navbar (r/adapt-react-class js/ReactBootstrap.Navbar))
(def Navbar.Header (r/adapt-react-class js/ReactBootstrap.Navbar.Header))
(def Navbar.Brand (r/adapt-react-class js/ReactBootstrap.Navbar.Brand))
(def Navbar.Collapse (r/adapt-react-class js/ReactBootstrap.Navbar.Collapse))
(def Navbar.Toggle (r/adapt-react-class js/ReactBootstrap.Navbar.Toggle))

(def NavDropdown (r/adapt-react-class js/ReactBootstrap.NavDropdown))
(def MenuItem (r/adapt-react-class js/ReactBootstrap.MenuItem))
(def NavItem (r/adapt-react-class js/ReactBootstrap.NavItem))

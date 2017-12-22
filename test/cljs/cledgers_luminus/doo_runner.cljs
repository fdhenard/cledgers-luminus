(ns cledgers-luminus.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [cledgers-luminus.core-test]))

(doo-tests 'cledgers-luminus.core-test)


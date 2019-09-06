# cledgers-luminus

generated using Luminus version "2.9.11.32"

FIXME

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Dev Prerequisites

- Java 11
- [Leiningen][1] 2.0 or above installed.
- db setup
    - postgresql installed
    - PSQL - `> create role cledgers_luminus with createdb login;`
    - `$ createdb cledgers_luminus -O cledgers_luminus`
    - run migrations
        - `$ lein run migrate`
    - insert self as user
        - in repl
            - `> (mount/start)`
            - compile `cledgers-luminus.dev.scripts`
            - `> (cledgers-luminus.dev.scripts/create-user-s! :username "frank" :last-name "Henard" :first-name "Frank" :email "fdhenard@gmail.com" :pass "tanky" :is-admin? true :is-active? true)`


## Running

### In emacs (cider)

- in terminal
    - `$ lein figwheel`
- in emacs
    - `M-x <ret> cider-jack-in`
    - `user> (mount/start)`

### In console

To start a web server for the application, run:

    lein run

## License

Copyright © 2017 FIXME

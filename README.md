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
        - use homebrew to install and use `$ brew services start postgresql`
        - if first time installing, you may need to run `$ createdb` to create the db with username
    - `$ psql postgres`
    - `> create role cledgers_luminus with createdb login;`
    - exit psql `^d`
    - `$ createdb cledgers_luminus -O cledgers_luminus`
    - run migrations
        - see [readme for DDDL](https://github.com/fdhenard/declarative-ddl)
        - as of 6/16/2021
            - `$ cd ~/dev/repos/declarative-ddl`
            - dry run

                    $ lein run -d ../cledgers-luminus -b "postgresql://localhost/cledgers_luminus?user=cledgers_luminus" migrate

            - execute:

                    $ lein run -d ../cledgers-luminus -b "postgresql://localhost/cledgers_luminus?user=cledgers_luminus" -e migrate

    - insert self as user
        - eval the code in the Rich Comment of `cledgers-luminus.dev.scripts`

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

# database-61b
A fully functional relational database management system (DBMS) and Domain Specific Language (DSL) written in Java. The database utilizes a SQL-esque declarative programming language to interface with the user.

## Usage
Run ``Main.java`` to initialize the database prompt. Relations can be pre-loaded in a .tbl file using the ``load <table name>.tbl`` command or created dynamically within the console using the format ``create table <table name> (<column0 name> <type0>, <column1 name> <type1>, ...)``. Arithmetic operations can be performed in a manner similar to SQL commands. Other operations allowed include ``store, drop, insert, print, select``.

## Additonal Info
Refer to the [spec](http://datastructur.es/sp17/materials/proj/proj2/proj2.html) for more info.
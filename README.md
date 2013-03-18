rdb2graph - Relational Database To Property Graph Converter
===========================================================

*rdb2graph can be used to convert a relational database on-line into a property graph model and store it in a graph database*

The tool uses [Apache DdlUtils](http://db.apache.org/ddlutils/) to read the schema of a relational database. Based on table definitions and 
foreign key constraints, nodes and relationships are created. Link tables (n:m) have to be explicitly declared in a config file if they shall 
be converted into a single relation. Properties (table attributes) and some additional meta-data (origin, type, id) are stored at the nodes 
and relationships.

Currently, the tool supports MySQL as a source database system and Neo4j as a target system. The architecture is flexible and can be easily
extended by implementing some predefined interfaces.

Usage
-----

Config: copy resources/sample-config.xml to resources/config.xml and look inside for details

Install: `mvn install`

Run: `mvn exec:java`

Todo's
------

* flexible support of different source database systems
* support of multiple source and target database systems
* configuration of link types (others than fk-names)
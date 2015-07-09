## rdb2graph - Relational Database To Property Graph Converter and Analysis Tool


*rdb2graph can be used to convert a relational database on-line into a property graph model and store it in a graph database*

The tool uses [Apache DdlUtils](http://db.apache.org/ddlutils/) to read the schema of a relational database. Based on table definitions and 
foreign key constraints, nodes and relationships are created. Link tables (n:m) have to be explicitly declared in a config file if they shall 
be converted into a single relation. Properties (table attributes) and some additional meta-data (origin, type, id) are stored at the nodes 
and relationships.

Currently, the tool supports MySQL as a source database system and Neo4j as a target system. The architecture is flexible and can be easily
extended by implementing some predefined interfaces.

#### Build

* clone rdb2graph project

`git clone https://github.com/s1ck/rdb2graph`

To run properly, ddl utils needed to be patched.

* checkout ddlutils from their svn

`svn co http://svn.apache.org/repos/asf/db/ddlutils/trunk ddlutils`

`cd ddlutils`

* apply patches

`patch -p0 -i ../rdb2graph/doc/ddlutils_graphbi_patch/ddlutils_graphbi_2013-04-16.patch`

`patch -p0 --binary -i ../rdb2graph/doc/ddlutils_graphbi_patch/ddlutils_graphbi_2014-08-04.patch`

* build and install ddl utils

`mvn clean install`

* build and install rdb2graph

`cd ../rdb2graph`

`mvn clean install`

#### Run

* copy resources/sakila-sample-config.properties to resources/config.properties and look inside for details

* list available options

`mvn exec:exec -Dexec.executable="java"`

* apply your own config xml (default is resources/config.xml)

`mvn exec:exec -Dexec.executable="java" -Dexec.args="-c config.xml"` 

* extract the database schema into ddl.xml

`mvn exec:exec -Dexec.executable="java" -Dexec.args="-e ddl.xml"`

* transform the relational database form source system to target system

`mvn exec:exec -Dexec.executable="java" -Dexec.args="-t"`

* read the schema from a local file instead of the live database

`mvn exec:exec -Dexec.executable="java" -Dexec.args="-r erpnext-sample-ddl.xml -t"`

* read the schema from a local file and analyze the graph for operation graphs

`mvn exec:exec -Dexec.executable="java" -Dexec.args="-r erpnext-sample-ddl.xml -a opgraph"`

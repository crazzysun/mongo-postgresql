PGMongo experimental driver
===========================
[![Build Status](https://travis-ci.org/laravel/framework.svg)](https://travis-ci.org/laravel/framework)

An experimental driver that makes a PostgreSQL database appear like a MongoDB database to its customers.
Works in interactive mode.


Move data from mongoDB to PostgreSQL:
-------------------------------------

1. Export collections to json:

		mongoexport --db [db_name] --collection [collection_name] --out [name_output_file].json
 
 (In order to connect to a mongod that enforces authorization with the --auth option, 
 you must use the --username and --password options.)

2. Import json to PostgreSQL:

 		CREATE TABLE [table_name](json_data jsonb);
		\copy [table_name] FROM '[json_file_name]'
		VACUUM ANALYZE [table_name];

3. Create column '_id', make it primary key:

		alter table [table_name] add column _id text;
		update [table_name] set _id = json_data->[json_path_to_id]->'_id';
		alter table [table_name] add primary key (_id);    

Usage:
-----
#### Build jar

		mvn assembly:assembly

#### Run
		java -jar target/pgmongo.jar

#### All commands:
		help		show help
		connect		connection to DB
			Options:
			-u			user name
			-p			password
			-url		url to db
			-debug		debug mod on
		[query]		query to DB
		exit		close pgmongo driver

#### How to work:

1. Connect to PostgreSQL DB:

		connect -u [user name] -p [password] -url [url_to_db] -debug
			
			
2. Write requests:

		db.[collection_name].[query_name]([query_json]);

3. ...

4. PROFIT!

5. go to 2.

Supported requests:
-------------------

	find
	insert
	delete

Supported operators:
-------------------

##### Comparison:

    $gt		>
    $gte	>=
    $lt		<
    $lte	<=
    $ne		<>
    $eq		=
    $in		IN
    $nin	NOT IN

##### Logical:

    $or 	OR
    $and 	AND
    $not 	NOT
    $nor 	NOT(... OR ...)

System requirements:
-------------------

- Java SE Runtime Environment 8 or later;
- Java SE Development Kit 8 or later;
- Maven 3.3 or later;
- PostgresSQL 9.4 or later.


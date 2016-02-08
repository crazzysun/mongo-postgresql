1. Export collections to json:

	mongoexport --db [db_name] --collection [collection_name] --out [name_output_file].json

	# In order to connect to a mongod that enforces authorization 	with the --auth option, you must use the --username and --password options. 

2. Import json to PostgreSQL

	CREATE TABLE [table_name]([column_name] jsonb);
	\copy [table_name] FROM '[json_file_name]'
	VACUUM ANALYZE [table_name];

3. Create column '_id', make it primary key

	alter table [table_name] add column _id text;
	update [table_name] set _id = [json_path_to_id]->'_id';
	alter table [table_name] add primary key (_id);

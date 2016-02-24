package org.pgmongo;

import org.bson.Document;

import java.util.*;

public class QueryTranslator {
    private Map<String, String> querySelectorsMNG;
    private String name_json_data;

    public QueryTranslator(String name_jd) {
        name_json_data = name_jd;
        querySelectorsMNG = new HashMap<>();
        String[] key = {"$eq", "$gt", "$gte", "$lt", "$lte", "$ne", "$in", "$nin", "$or", "$and", "$not", "$nor"};
        String[] value = {" = ", " > ", " >= ", " < ", " <= ", " != ", " IN ", " NOT IN ", " OR ", " AND ", " NOT ", " OR "};
        for (int i = 0; i < value.length; i++) {
            querySelectorsMNG.put(key[i], value[i]);
        }
    }

    //find for tests
    public QueryResult find(String tableName, String inputQuery, String inputProjection) throws Exception {
        Document queryDoc = Document.parse(inputQuery);
        Document projectionDoc = !inputProjection.equals("") ? Document.parse(inputProjection) : new Document();
        return find(tableName, queryDoc, projectionDoc);
    }

    public QueryResult find(String tableName, Document queryDoc, Document projectionDoc) throws Exception {
        ArrayList<Object> parameters = new ArrayList<>();
        String queryParse = queryFindToString(queryDoc, parameters);
        String projParse = projectionFindToString(projectionDoc);
        String query = "select " + projParse + " from " + tableName + " where " + queryParse + ";";
        return new QueryResult(parameters, query.replaceAll("  ", " "));
    }

    private String queryFindToString(Document doc, ArrayList<Object> parameters) throws Exception {
        StringBuilder result = new StringBuilder();

        for (String key : doc.keySet()) {
            if (result.length() != 0) result.append(" AND ");
            result.append(docToStringRec(key, doc.get(key), parameters));
        }

        return result.toString();
    }

    private String projectionFindToString(Document projDoc) {
        ArrayList<String> keys = new ArrayList<>();
        boolean addId = true;

        for (String key : projDoc.keySet()) {
            if (projDoc.get(key).equals(0)) {
                keys.remove(key);
                if (key.equals("_id")) addId = false;
            } else {
                if (!keys.contains(key)) {
                    keys.add(key);
                }
            }
        }

        if (keys.isEmpty()) {
            return name_json_data;
        }
        if (addId && !keys.contains("_id")) keys.add(0, "_id");

        StringBuilder retValue = new StringBuilder();
        retValue.append(stringToJson(keys.get(0))).append(" as \"").append(keys.get(0)).append("\"");
        for (int i = 1; i < keys.size(); i++) {
            retValue.append(", ").append(stringToJson(keys.get(i))).append(" as \"").append(keys.get(i)).append("\"");
        }

        return retValue.toString();
    }

    private String docToStringRec(String field, Object value, ArrayList<Object> parameters) throws Exception {
        StringBuilder result = new StringBuilder();

        if (querySelectorsMNG.containsKey(field)) {  // and, or, nor
            if (value instanceof List) {
                if (field.equals("$nor")) {
                    result.append(" NOT ");
                }

                ArrayList arr = (ArrayList) value;
                if (!(arr.get(0) instanceof Document))
                    throw new Exception("Bad types. Required: Document. Found: " + arr.get(0).getClass());

                Document firstDoc = (Document) arr.get(0);
                String firstKey = getFirstKey(firstDoc);

                result.append("(").append(docToStringRec(firstKey, firstDoc.get(firstKey), parameters));
                for (int i = 1; i < arr.size(); i++) {
                    firstDoc = (Document) arr.get(i);
                    firstKey = getFirstKey(firstDoc);
                    result.append(querySelectorsMNG.get(field)).append(docToStringRec(firstKey, firstDoc.get(firstKey), parameters));
                }
                result.append(")");
            } else {
                throw new Exception("Bad value. Required: ArrayList. Found: " + value.getClass());
            }
        } else {
            if (!field.startsWith(name_json_data)) field = stringToJson(field);
            if (value instanceof Document) {
                Document doc = (Document) value;
                String keyDoc = getFirstKey(doc);

                if (keyDoc == null) throw new Exception("Bad value. Key must be not null.");

                switch (keyDoc) {
                    case "$in":
                    case "$nin":
                        result.append(field).append(querySelectorsMNG.get(keyDoc)).append("(");

                        if (!(doc.get(keyDoc) instanceof ArrayList))
                            throw new Exception("Bad types. Required: ArrayList. Found: " + doc.get(keyDoc).getClass());

                        ArrayList arrForIn = (ArrayList) doc.get(keyDoc);
                        parameters.add(arrForIn.get(0).toString());
                        result.append("?::jsonb");
                        for (int i = 1; i < arrForIn.size(); i++) {
                            parameters.add(arrForIn.get(i).toString());
                            result.append(", ?::jsonb");
                        }
                        result.append(")");
                        break;
                    case "$not":
                        result = new StringBuilder(querySelectorsMNG.get(keyDoc));
                        if (!(doc.get(keyDoc) instanceof Document))
                            throw new Exception("Bad types. Required: Document. Found: " + doc.get(keyDoc).getClass());
                        result.append(docToStringRec(field, doc.get(keyDoc), parameters));
                        break;
                    default:
                        for (String kd : doc.keySet()) {
                            parameters.add(doc.get(kd).toString());
                            if (result.length() != 0) result.append(" and ");
                            result.append(field).append(querySelectorsMNG.get(kd)).append("?::jsonb");
                        }
                        break;
                }
            } else {
                parameters.add(value.toString());
                result.append(field).append(" = ?::jsonb");
            }
        }
        return result.toString();
    }

    //delete for tests
    public QueryResult delete(String tableName, String inputQuery, int justOne) throws Exception {
        Document queryDoc = Document.parse(inputQuery);
        return delete(tableName, queryDoc, justOne);
    }

    //delete for tests
    public QueryResult delete(String tableName, String inputQuery) throws Exception {
        return delete(tableName, inputQuery, 0);
    }

    public QueryResult delete(String tableName, Document inputQuery, int justOne) throws Exception {
        ArrayList<Object> parameters = new ArrayList<>();
        StringBuilder result = new StringBuilder("delete from " + tableName);
        StringBuilder tmpRes = new StringBuilder();

        if (inputQuery.isEmpty()) {
            result.append(";");
        } else {
            result.append(" where ");
            for (String key : inputQuery.keySet()) {
                if (tmpRes.length() != 0) tmpRes.append(" AND ");
                tmpRes.append(docToStringRec(key, inputQuery.get(key), parameters));
            }
        }

        result.append(tmpRes);
        if (justOne != 0) result.append(" LIMIT 1");
        return new QueryResult(parameters, result.append(";").toString().replaceAll("  ", " "));
    }

    //insert for tests
    public QueryResult insert(String tableName, ArrayList<String> inputQuery, String s) throws Exception {
        ArrayList<Document> arr = new ArrayList<>();
        for (String anInputQuery : inputQuery) {
            arr.add(Document.parse(anInputQuery));
        }

        return insert(tableName, arr);
    }

    public QueryResult insert(String tableName, List<Document> inputQuery) throws Exception {
        ArrayList<Object> param = new ArrayList<>();
        if (!inputQuery.get(0).containsKey("_id")) throw new Exception("Cannot be find key \"_id\".");
        StringBuilder result = new StringBuilder("insert into " + tableName + " (_id, " + name_json_data + ") values (?, ?::jsonb)");
        param.add(inputQuery.get(0).get("_id"));
        param.add(inputQuery.get(0).toJson());

        for (int i = 1; i < inputQuery.size(); i++) {
            if (!inputQuery.get(i).containsKey("_id")) throw new Exception("Cannot be find key \"_id\".");
            result.append(", (?, ?::jsonb)");
            param.add(inputQuery.get(i).get("_id"));
            param.add(inputQuery.get(i).toJson());
        }

        result.append(";");
        return new QueryResult(param, result.toString());
    }

    private static String getFirstKey(Document doc) {
        Iterator<String> i = doc.keySet().iterator();
        return i.hasNext() ? i.next() : null;
    }

    private String stringToJson(String str) {
        String[] arr = str.trim().split("\\.");
        StringBuilder result = new StringBuilder();
        result.append(name_json_data);

        for (String anArr : arr) {
            result.append("->'").append(anArr).append("'");
        }

        return result.toString();
    }

    public class QueryResult {
        private String query;
        private ArrayList<Object> parameters;

        public QueryResult(ArrayList<Object> parameters, String query) {
            this.parameters = parameters;
            this.query = query;
        }

        public String getQuery() {
            return query;
        }

        public List<Object> getParameters() {
            return Collections.unmodifiableList(parameters);
        }
    }
}
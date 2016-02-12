package translator;

import org.bson.Document;

import java.util.*;

public class QueryTranslator {
    private Map<String, String> querySelectorsMNG;

    public QueryTranslator() {
        querySelectorsMNG = new HashMap<>();
        String[] key = {"", "$eq", "$gt", "$gte", "$lt", "$lte", "$ne", "$in", "$nin", "$or", "$and", "$not", "$nor"};
        String[] value = {"", " = ", " > ", " >= ", " < ", " <= ", " != ", " IN ", " NOT IN ", " OR ", " AND ", " NOT ", " OR "};
        for (int i = 0; i < value.length; i++) {
            querySelectorsMNG.put(key[i], value[i]);
        }
    }

    //Find for unitTests
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
            return "*";
        }
        if (addId && !keys.contains("_id")) keys.add(0, "_id");
        StringBuilder retValue = new StringBuilder();
        retValue.append(keys.get(0).replaceAll("\\.", "->"));
        for (int i = 1; i < keys.size(); i++) {
            retValue.append(", ").append(keys.get(i).replaceAll("\\.", "->"));
        }

        return retValue.toString();
    }

    private String queryFindToString(Document doc, ArrayList<Object> parameters) throws Exception {
        String result = "";

        for (String key : doc.keySet()) {
            if (!result.equals("")) result += " AND ";
            result += docToStringRec(key, doc.get(key), parameters);
        }

        return result;
    }

    private String docToStringRec(String field, Object value, ArrayList<Object> parameters) throws Exception {
        String result = "";

        if (querySelectorsMNG.containsKey(field)) {  // and, or, nor
            if (value instanceof List) {
                if (field.equals("$nor")) {
                    result += " NOT ";
                }

                ArrayList arr = (ArrayList) value;
                if (!(arr.get(0) instanceof Document))
                    throw new Exception("Bad types. Required: Document. Found: " + arr.get(0).getClass());

                Document firstDoc = (Document) arr.get(0);
                String firstKey = getFirstKey(firstDoc);
                result += "(" + docToStringRec(firstKey, firstDoc.get(firstKey), parameters);
                for (int i = 1; i < arr.size(); i++) {
                    firstDoc = (Document) arr.get(i);
                    firstKey = getFirstKey(firstDoc);
                    result += querySelectorsMNG.get(field) + docToStringRec(firstKey, firstDoc.get(firstKey), parameters);
                }
                result += ")";
            } else {
                throw new Exception("Bad value. Required: ArrayList. Found: " + value.getClass());
            }
        } else {
            field = field.replaceAll("\\.", "->");
            if (value instanceof Document) {
                Document doc = (Document) value;
                String keyDoc = getFirstKey(doc);
                if (keyDoc == null) throw new Exception("Bad value. Key must be not null.");

                switch (keyDoc) {
                    case "$in":
                    case "$nin":
                        result = field + querySelectorsMNG.get(keyDoc) + "(";

                        if (!(doc.get(keyDoc) instanceof ArrayList))
                            throw new Exception("Bad types. Required: ArrayList. Found: " + doc.get(keyDoc).getClass());

                        ArrayList arrForIn = (ArrayList) doc.get(keyDoc);
                        parameters.add(arrForIn.get(0));
                        result += "?";
                        for (int i = 1; i < arrForIn.size(); i++) {
                            parameters.add(arrForIn.get(i));
                            result += ", " + "?";
                        }
                        result += ")";
                        break;
                    case "$not":
                        result = querySelectorsMNG.get(keyDoc);
                        if (!(doc.get(keyDoc) instanceof Document))
                            throw new Exception("Bad types. Required: Document. Found: " + doc.get(keyDoc).getClass());
                        result += docToStringRec(field, doc.get(keyDoc), parameters);
                        break;
                    default:
                        for (String kd : doc.keySet()) {
                            parameters.add(doc.get(kd));
                            if (!result.equals("")) result += " and ";
                            result += field + querySelectorsMNG.get(kd) + "?";
                        }
                        break;
                }
            } else {
                parameters.add(value);
                result += field + " = ?";
            }
        }
        return result;
    }

    private static String getFirstKey(Document doc) {
        Iterator<String> i = doc.keySet().iterator();
        return i.hasNext() ? i.next() : null;
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
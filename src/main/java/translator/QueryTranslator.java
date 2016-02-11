package translator;

import org.bson.Document;

import java.util.*;

public class QueryTranslator {
    private Map<String, String> querySelectorsMNG;

    public QueryTranslator() {
        querySelectorsMNG = new HashMap<String, String>();

        String[] key = {"", "$eq", "$gt", "$gte", "$lt", "$lte", "$ne", "$in", "$nin", "$or", "$and", "$not", "$nor"};
        String[] value = {"", " = ", " > ", " >= ", " < ", " <= ", " != ", " IN ", " NOT IN ", " OR ", " AND ", " NOT ", " $nor "};

        for (int i = 0; i < value.length; i++) {
            querySelectorsMNG.put(key[i], value[i]);
        }
    }

    //Find for unitTests
    public QueryResult find(String tableName, String inputQuery, String inputProjection) {
        Document queryDoc = Document.parse(inputQuery);
        Document projectionDoc = !inputProjection.equals("") ? Document.parse(inputProjection) : new Document();
        return find(tableName, queryDoc, projectionDoc);
    }

    public QueryResult find(String tableName, Document queryDoc, Document projectionDoc) {
        ArrayList<Object> parameters = new ArrayList<Object>();
        String queryParse = queryFindToStringRec(queryDoc, parameters);
        String projParse = projectionFindToString(projectionDoc);
        String query = "select " + projParse + " from " + tableName + " where " + queryParse + ";";
        return new QueryResult(parameters, query);
    }

    private String projectionFindToString(Document queryDoc) {
        ArrayList<String> keys = new ArrayList<String>();
        //keys.add("_id");
        boolean addId = true;

        for (String key : queryDoc.keySet()) {
            if (queryDoc.get(key).equals(0)) {
                keys.remove(key);

                if (key.equals("_id")) addId = false;
            } else {
                if (!queryDoc.containsKey(key)) {
                    keys.add(key);
                }
            }
        }

        if (keys.isEmpty()) {
            return "*";
        }

        if (addId) keys.add(0, "_id");
        StringBuilder retValue = new StringBuilder();
        retValue.append(keys.get(0).replaceAll("\\.", "->"));
        for (int i = 1; i < keys.size(); i++) {
            retValue.append(", ").append(keys.get(i).replaceAll("\\.", "->"));
        }

        return retValue.toString();
    }

    private String queryFindToStringRec(Document doc, ArrayList<Object> parameters) {
        String retValue = "";

        for (String key : doc.keySet()) {
            String curKey = "";
            String curValue = "";
            String curOp = "";

            if (!querySelectorsMNG.containsKey(key)) {
                if (curKey.equals("")) {
                    curKey = key;
                }
                if (doc.get(key).getClass().equals(Document.class)) {
                    curValue = queryFindToStringRec((Document) doc.get(key), parameters);
                } else {
                    if (curOp.equals("")) curOp = "$eq";
                    parameters.add(doc.get(key));
                    curValue = "?";
                }
            } else {
                curOp = key;

                if (doc.get(key).getClass().equals(Document.class)) {
                    curValue = queryFindToStringRec((Document) doc.get(key), parameters);
                } else {
                    parameters.add(doc.get(key));
                    curValue = "?";
                }
            }

            curKey = curKey.replaceAll("\\.", "->");
            if (retValue.equals("")) {
                retValue = curKey + querySelectorsMNG.get(curOp) + curValue;
            } else {
                retValue = retValue + " AND " + curKey + querySelectorsMNG.get(curOp) + curValue;
            }
        }

        return retValue;
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

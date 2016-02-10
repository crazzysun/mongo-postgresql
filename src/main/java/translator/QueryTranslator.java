package translator;

import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QueryTranslator {
    private Map<String, String> querySelectorsMNG;
    private queryResult qr;

    public QueryTranslator() {
        querySelectorsMNG = new HashMap<String, String>();

        String[] key = {"", "$eq", "$gt", "$gte", "$lt", "$lte", "$ne", "$in", "$nin", "$or", "$and", "$not", "$nor"};
        String[] value = {"", " = ", " > ", " >= ", " < ", " <= ", " != ", " IN ", " NOT IN ", " OR ", " AND ", " NOT ", " $nor "};

        for (int i = 0; i < value.length; i++) {
            querySelectorsMNG.put(key[i], value[i]);
        }
    }

    //Find for unitTetsts
    public String find(String tableName, String inputQuery, String inputProjection) {
        Document queryDoc = Document.parse(inputQuery);
        Document projectionDoc = !inputProjection.equals("") ? Document.parse(inputProjection) : new Document();
        qr = new queryResult();
        String queryParse = queryFindToStringRec(queryDoc);
        String projParse = projectionFindToString(projectionDoc);

//        //<debug>
//        for (int i = 0; i < qr.parametrs.size(); i++) {
//            System.out.print(qr.parametrs.get(i) + ", ");
//        }
//        System.out.println();
//        //</debug>

        return "select " + projParse + " from " + tableName + " where " + queryParse + ";";
    }

    public queryResult find(String tableName, Document queryDoc, Document projectionDoc) {
        String queryParse = queryFindToStringRec(queryDoc);
        String projParse = projectionFindToString(projectionDoc);
        qr = new queryResult();
        qr.query = "select " + projParse + " from " + tableName + " where " + queryParse + ";";

        return qr;
    }

    private String projectionFindToString(Document queryDoc) {
        ArrayList<String> keys = new ArrayList<String>();

        keys.add("_id");

        for (String key : queryDoc.keySet()) {
            if (queryDoc.get(key).equals(0)) {
                keys.remove(key);
            } else {
                keys.add(key);
            }
        }


        if (keys.size() < 2) {
            return "*";
        }

        StringBuilder retValue = new StringBuilder();
        retValue.append(keys.get(0).replaceAll("\\.", "->"));
        for (int i = 1; i < keys.size(); i++) {
            retValue.append(", ").append(keys.get(i).replaceAll("\\.", "->"));
        }

        return retValue.toString();
    }

    private String queryFindToStringRec(Document doc) {
        String retValue = "";

        for (String key : doc.keySet()) {
            String curKey = "";
            Object curValue = "";
            String curOp = "";

            if (!querySelectorsMNG.containsKey(key)) {
                if (curKey.equals("")) {
                    curKey = key;
                }
                if (doc.get(key).getClass().equals(Document.class)) {
                    curValue = queryFindToStringRec((Document) doc.get(key));
                } else {
                    if (curOp.equals("")) curOp = "$eq";
                    qr.parametrs.add(doc.get(key));
                    curValue = "?";
                }
            } else {
                curOp = key;

                if (doc.get(key).getClass().equals(Document.class)) {
                    curValue = queryFindToStringRec((Document) doc.get(key));
                } else {
                    qr.parametrs.add(doc.get(key));
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

    class queryResult {
        String query;
        ArrayList<Object> parametrs;

        public queryResult() {
            this.parametrs = new ArrayList<Object>();
        }
    }
}

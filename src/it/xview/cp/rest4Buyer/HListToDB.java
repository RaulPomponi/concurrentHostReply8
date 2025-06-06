package it.xview.cp.rest4Buyer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HListToDB {
    private String tableName = "isola_4b_vendors";
    private Connection connection = null;
    private String orgId = "orgId";
    private String requestId = "reqId";
    public HListToDB(Connection connection, String tableName) {
		super();
		this.tableName = tableName;
		this.connection = connection;
	}

	public void insert(List<HashMap<String, Object>> vendorsList) {
        String insertSQL = buildInsertSQL(tableName, vendorsList.get(0)); // Costruisce l'SQL con le colonne
        System.out.println(insertSQL);

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            for (HashMap<String, Object> vendorMap : vendorsList) {
                try {
                    setPreparedStatementValues(preparedStatement, vendorMap);
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    System.err.println("Errore durante l'inserimento per: "  );
                    printHashMap(vendorMap);
                    System.err.println("Messaggio di errore: " + e.getMessage());
                }
            }
//            connection.commit();
        } catch (SQLException e) {
            System.err.println("Errore durante la preparazione dello statement: " + e.getMessage());
        }
    }

    private String buildInsertSQL(String tableName, HashMap<String, Object> vendorMap) {
        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();

        for (String key : vendorMap.keySet()) {
            columns.append(key).append(", ");
            placeholders.append("?, ");
        }
        columns.append("org_id, request_id"); // Aggiungi colonne extra
        placeholders.append("?, ?"); // Aggiungi placeholders extra
        String sInsert =  "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";
        return sInsert;
    }

    private void setPreparedStatementValues(PreparedStatement preparedStatement, HashMap<String, Object> vendorMap) throws SQLException {
        int parameterIndex = 1;
        for (Object value : vendorMap.values()) {
        	String s = (String) value; 
            preparedStatement.setString(parameterIndex++, s);
        }
        preparedStatement.setString(parameterIndex++, orgId); // org_id
        preparedStatement.setString(parameterIndex, requestId); // request_id
    }

    public void printHashMap(HashMap<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            System.out.println("    Chiave: " + entry.getKey() + ", Valore: " + entry.getValue());
        }
    }

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

}
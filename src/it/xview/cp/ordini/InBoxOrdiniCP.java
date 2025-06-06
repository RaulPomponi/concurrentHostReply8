package it.xview.cp.ordini;
/*
 *  Programma 		XXH: Pec Leggi Ordini e Rilasci
 *  Eseguibile 		XXH_PEC_LEGGI_ORDINI
 *  Parametri
 *  			P_ORG_ID				FND_NUMBER
 *  
 *  Responsabilità 
 *  			Amministratore di sistema
 *  Gruppo di Richieste
 *  			System Administrator Reports
 */


import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import it.xview.cp.util.ConcurrentHostConnection;
import it.xview.cp.util.Pec;
import it.xview.cp.util.PecReadBean;
import it.xview.cp.util.TempSecurityStorage;

public class InBoxOrdiniCP {

	private int exitStatus=0;
	private String exitMsg = "Processo di lettura PEC concluso correttamente.";
	private ConcurrentHostConnection chc;
	private String sOrgId;
	private String arg0;
	private String arg1;

	private String sqlA 	= "select 'A' STATO, ARSS_REQUEST_ID, NOME_FILE, ORIGINE, OGGETTO  from XXX_GESTIONE_ORDINIPO_A_V  WHERE ORG_ID =:1";
	private String sqlI 	= "select 'I' STATO, ARSS_REQUEST_ID, NOME_FILE, ORIGINE, OGGETTO  from XXX_GESTIONE_ORDINIPO_I_V  WHERE ORG_ID =:1";
//	private String sqlInsert = "INSERT INTO xxh_read_mail_ordini_all (READ_MAIL_ORDINE_ID, FILE_NAME, FILE_DATA, DATA_RICEZIONE_MAIL, DATA_ELABORAZIONE, ORG_ID, REQUEST_ID, TIPO_NUM, TIPO_POSTA) VALUES (?,?,EMPTY_BLOB(),?,to_char(sysdate,'DD-MON-RRRR'),?,?,?,?)";
	private String sqlUpdateBlob = "SELECT  FILE_DATA FROM xxh.xxx_gestione_ordinipo_read where READ_MAIL_ORDINE_ID = :1 FOR UPDATE";
	private String sqlCredential="select distinct MITTENTE, PWD, SMTP from XXH.XXX_GESTIONE_ORDINIPO where ORG_ID=:1";
	public static void main(String[] args) {
		/**/
		InBoxOrdiniCP obj = new InBoxOrdiniCP();
		String args0 = args[0];
		String args1 = args[1];
		
//		String args0="/data1/TESTR12/apps/apps_st/appl/xxh/12.0.0/bin/XXH_TEST_HOST";
//		String args1="XXH_TEST_HOST FCP_REQID=31164734 FCP_LOGIN="APPS/A2p0p2s2TE5t1G3m377i" FCP_USERID=5675 FCP_USERNAME="SB002449" FCP_PRINTER="noprint" FCP_SAVE_OUT=Y FCP_NUM_COPIES=0 "Distinta81-070324-12:03:40" """;

		
		obj.arg0 = args0;
		obj.arg1 = args1;
		
		
		obj.run();
		System.out.println(obj.getExitStatus() + " - " + obj.getExitMsg());
		System.exit(obj.getExitStatus());

	}

	private void run() {
		int errCounter=100;
		try {
			chc = new ConcurrentHostConnection(arg0,arg1);
			HashMap<String,Object> param = chc.getConcurrentParameter();
			sOrgId = (String)param.get("P_ORG_ID");
			//TEST Locale
			if(chc.getRequestId()==31148923)sOrgId="81";
			System.out.println("Lettura pec Inviati");
			List<PecReadBean> listaPecI = getListaPecReadFromDB(sqlI);
			if (listaPecI!=null && !listaPecI.isEmpty()) {
				HashMap<String,String> pecCredential = chc.getSingleRow(sqlCredential, sOrgId);
				SecretKeySpec sKey = TempSecurityStorage.createSecretKey(chc.getConnection());
				String pecPwd = TempSecurityStorage.decrypt(pecCredential.get("pwd"), sKey);
				String pecUsr = pecCredential.get("mittente");
				String pecSmtp = pecCredential.get("smtp");
				Pec.set_mittente(pecUsr, pecPwd);
				Pec.set_smpt(pecSmtp);
				listaPecI=Pec.readAll(listaPecI);

				System.out.println("Aggiornamento DB I");
				for(PecReadBean pec : listaPecI) {
					String esito = setPecEsito(pec);
					System.out.println("{" + esito + "}" + pec.toString());
				}
			}

			System.out.println("Lettura pec Accettati");
			List<PecReadBean> listaPecA = getListaPecReadFromDB(sqlA);
			if (listaPecA!=null && !listaPecA.isEmpty()) {
				HashMap<String,String> pecCredential = chc.getSingleRow(sqlCredential, sOrgId);
				SecretKeySpec sKey = TempSecurityStorage.createSecretKey(chc.getConnection());
				String pecPwd = TempSecurityStorage.decrypt(pecCredential.get("pwd"), sKey);
				String pecUsr = pecCredential.get("mittente");
				String pecSmtp = pecCredential.get("smtp");
				Pec.set_mittente(pecUsr, pecPwd);
				Pec.set_smpt(pecSmtp);
				listaPecA=Pec.readAll(listaPecA);

				System.out.println("Aggiornamento DB A");
				for(PecReadBean pec : listaPecA) {
					String esito = setPecEsito(pec);
					System.out.println("{" + esito + "}" + pec.toString());
				}
			}
        setAllegaProcedurePO();
		chc.closeConnection();
		
		} catch (Exception e) {
			this.exitStatus=errCounter;
			e.printStackTrace();
		}
	}


	private List<PecReadBean> getListaPecReadFromDB(String sqlPecR) throws SQLException, Exception {
		List<PecReadBean> lista =new ArrayList<PecReadBean>();
		PreparedStatement ps = chc.getConnection().prepareStatement(sqlPecR);
		ps.setString(1, sOrgId);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			PecReadBean pecR = new PecReadBean();
			pecR.setStato(rs.getString("STATO"));
			pecR.setNomeFile(rs.getString("NOME_FILE"));
			pecR.setOggetto(rs.getString("OGGETTO"));
			lista.add(pecR);
		}
		rs.close();
		ps.close();
		return lista;
	}

	public int getExitStatus() {
		return exitStatus;
	}

	public void setExitStatus(int exitStatus) {
		this.exitStatus = exitStatus;
	}

	public String getExitMsg() {
		return exitMsg;
	}

	public void setExitMsg(String exitMsg) {
		this.exitMsg = exitMsg;
	}

	private String setPecEsito(PecReadBean pecR) {
		String ret = null;
    	try {
            CallableStatement cs = null;
            String call = "call XXX_XXVIEW_GESTIONE_ORDINIPO.SET_ESITO_READ(?,?,?,?,?,?)";
            cs = chc.getConnection().prepareCall(call);
			cs.registerOutParameter(1, java.sql.Types.VARCHAR);
            
            cs.setInt(2,chc.getRequestId()); 

            cs.setString(3,pecR.getNomeFile());
            cs.setString(4,pecR.getOggetto());
            cs.setString(5,pecR.getDataRicezione());
            cs.setString(6,pecR.getStato());
            
            cs.executeUpdate();
        	ret =cs.getString(1);
            cs.close();

            if(Integer.parseInt(ret)>0)updateBlob(ret, pecR.getContenuto());
            
            
        } catch (Exception e) {
            e.printStackTrace();
            exitStatus=99;
        }
		return ret;
	}

	private String setAllegaProcedurePO() {
		String ret = null;
    	try {
            CallableStatement cs = null;
            String call = "call XXX_XXVIEW_GESTIONE_ORDINIPO.allega_ricevute_po(?)";
            System.out.println("call XXX_XXVIEW_GESTIONE_ORDINIPO.allega_ricevute_po(" + "'"+chc.getRequestId()+"');");
            cs = chc.getConnection().prepareCall(call);
            cs.setString(1,String.valueOf(chc.getRequestId()));
            
            cs.executeUpdate();
            cs.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            exitStatus=99;
        }
		return ret;
	}

	private void updateBlob(String readMailOrdineId, byte[] value) throws SQLException, Exception {

        PreparedStatement pstmt = chc.getConnection().prepareStatement(sqlUpdateBlob);
        pstmt.setInt(1, Integer.parseInt(readMailOrdineId));
        ResultSet rset = pstmt.executeQuery();
        while(rset.next()) {
                    Blob blobU = rset.getBlob(1);
                       BufferedOutputStream out = new BufferedOutputStream(blobU.setBinaryStream(1L));

                       try {
                          out.write(value);
                       }catch(Exception e) {
                    	   e.printStackTrace();
                       } finally {
                          if (out != null) {
                             out.close();
                          }

                       }
              }
        rset.close();
        pstmt.close();
     		
	}


}

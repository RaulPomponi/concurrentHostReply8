package oracle.apps.fnd.cp.xxh_fs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import oracle.apps.fnd.cp.request.CpContext;
import oracle.apps.fnd.cp.request.JavaConcurrentProgram;
import oracle.apps.fnd.cp.request.LogFile;

public class Xxh_FS_Ritorno_Documenti implements JavaConcurrentProgram {
	public void runProgram(CpContext var1) {
		LogFile var2 = var1.getLogFile();
		String var3 = "Errori durante invio ordini a fornitore";
		String var4 = "";
		String var5 = "";
		String[] var6 = new String[]{";"};
		String var7 = "";
		String var8 = "";
		Connection var9 = var1.getJDBCConnection();
		var1.getReqCompletion().setCompletion(0, "");

		try {
			Xxh_fs_mail.set_appl(var1);
			String var10 = "";
			String var11 = "";
			String var12 = "X";

			try {
				var11 = "select  MITTENTE_MAIL, PWD_MAIL, SMPT from XXH_GESTIONE_MAIL a where key_programma = 'EMAIL'";
				PreparedStatement var13 = var9.prepareStatement(var11);
				ResultSet var14 = var13.executeQuery();
				if (var14.next()) {
					Xxh_fs_mail.set_mittente(var14.getString("MITTENTE_MAIL"), var14.getString("PWD_MAIL"));
					Xxh_fs_mail.set_smpt(var14.getString("SMPT"));
				}

				var14.close();
				var13.close();
			} catch (Exception var19) {
				var2.writeln("Errore invio mail :" + var19.toString(), 0);
			}

			try {
				String var22 = "select distinct email_address, a.id_ordine, a.status, a.errore, ph.segment1 ordine from XXH_ORDINI_PDF_DA_FIRMARE a, po_headers_all ph, PER_PEOPLE_f pa where a.status in ('I', 'E') and a.id_ordine = ph.po_header_id and pa.person_id = ph.agent_id and pa.email_address is not null order by 1";
				PreparedStatement var23 = var9.prepareStatement(var22);
				ResultSet var15 = var23.executeQuery();

				while (true) {
					if (!var15.next()) {
						var15.close();
						var23.close();
						break;
					}

					var8 = null;
					var10 = var15.getString("status");
					var4 = var15.getString("email_address");
					var7 = var15.getString("ordine");
					var8 = var15.getString("errore");
					var2.writeln("Stato " + var10 + " " + var8, 0);
					if ("E".equals(var10)) {
						var5 = var5 + "Ordine " + var7 + "- ERRORE: " + var8 + "\n";
						var2.writeln("Corpo mail " + var5, 0);
					}

					if ("X".equals(var12)) {
						var12 = var4;
					}

					if (!var12.equals(var4)) {
						try {
							if (Xxh_fs_mail.send_mail(var4, var3, var5, var6).equals("KO")) {
								throw new Xxh_fs_Exception();
							}
						} catch (Exception var18) {
							var2.writeln("Errore invio mail :" + var18.toString(), 0);
						}

						var5 = "";
						var12 = var4;
					}
				}
			} catch (Exception var20) {
				var2.writeln("Errore lettura XXH_ORDINI_PDF_DA_FIRMARE :" + var20.toString(), 0);
				throw new Xxh_fs_Exception();
			}

			if (!"X".equals(var12)) {
				try {
					if (Xxh_fs_mail.send_mail(var4, var3, var5, var6).equals("KO")) {
						throw new Xxh_fs_Exception();
					}
				} catch (Exception var17) {
					var2.writeln("Errore invio mail :" + var17.toString(), 0);
				}

				var5 = null;
			}
		} catch (Xxh_fs_Exception var21) {
			var1.getReqCompletion().setCompletion(2, "");
		}

	}
}
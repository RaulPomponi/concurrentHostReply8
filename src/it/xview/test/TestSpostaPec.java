package it.xview.test;

import java.util.Date;
import java.util.List;

import it.xview.cp.util.Pec2;

public class TestSpostaPec {

    public static void main(String[] args) {
        TestSpostaPec obj = new TestSpostaPec();
        obj.testGmail();
    }

    public void testGmail()
    {
        String usr = "pmprla@gmail.com";
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        System.out.print("Enter password: ");
        String pwd = scanner.nextLine();
        String smtp = "smtp.gmail.com";
        String imap = "imap.gmail.com";
        Pec2.set_mittente(usr, pwd);
        Pec2.set_smpt(smtp);
        try {
            Date now;
            now = new Date();
            System.out.printf("Inizio : %1$td/%1$tm/%1$tY %1$tH:%1$tM:%1$tS.%1$tL%n", now);
            Pec2.leggiTuttoGmail(usr, pwd);
            now = new Date();
            System.out.printf("Fine : %1$td/%1$tm/%1$tY %1$tH:%1$tM:%1$tS.%1$tL%n", now);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }
    public void test1() {
        /**/
        String folder = "Archivio2024";
        String pecUsr = "direzioneict.test@pec.policlinicogemelli.it";
        String pecPwd = "D!rezioneict2023";
        String pecSmtp = "smtp.pec.aruba.it";
        Pec2.set_mittente(pecUsr, pecPwd);
        Pec2.set_smpt(pecSmtp);
        try {
            Date now;
            now = new Date();
            System.out.printf("Inizio POP: %1$td/%1$tm/%1$tY %1$tH:%1$tM:%1$tS.%1$tL%n", now);
            Pec2.leggiTuttoPop();
            now = new Date();
            System.out.printf("Fine POP: %1$td/%1$tm/%1$tY %1$tH:%1$tM:%1$tS.%1$tL%n", now);

            now = new Date();
            System.out.printf("Inizio IMAP: %1$td/%1$tm/%1$tY %1$tH:%1$tM:%1$tS.%1$tL%n", now);
            Pec2.leggiTuttoImap();
            now = new Date();
            System.out.printf("Fine IMAP: %1$td/%1$tm/%1$tY %1$tH:%1$tM:%1$tS.%1$tL%n", now);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
/*       
        Date dataLimite;
        java.time.LocalDate localDate = java.time.LocalDate.of(2024, 8, 31);
        dataLimite = java.sql.Date.valueOf(localDate);
        try {
            List<String> listaPec = Pec.listAllFolders();
            for (String pec : listaPec) {
                System.out.println(pec);
            }
            Pec.spostaPecPerDataImap(dataLimite, folder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Pec spostate in " + folder);
*/
    }


}

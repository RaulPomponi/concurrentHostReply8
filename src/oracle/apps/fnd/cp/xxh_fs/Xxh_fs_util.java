package oracle.apps.fnd.cp.xxh_fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public final class Xxh_fs_util {
	public static boolean copyfile(String var0, String var1) {
		try {
			File var2 = new File(var0);
			File var3 = new File(var1);

			try {
				var3.delete();
			} catch (Exception var8) {
				;
			}

			FileInputStream var4 = new FileInputStream(var2);
			FileOutputStream var5 = new FileOutputStream(var3);
			byte[] var6 = new byte[1024];

			int var7;
			while ((var7 = var4.read(var6)) > 0) {
				var5.write(var6, 0, var7);
			}

			var4.close();
			var5.close();
			System.out.println("File copied.");
			return true;
		} catch (FileNotFoundException var9) {
			System.out.println(var9.getMessage() + " in the specified directory.");
			return false;
		} catch (IOException var10) {
			System.out.println(var10.getMessage());
			return false;
		}
	}
}
package oracle.apps.fnd.cp.xxh_fs;

import java.util.HashMap;

public class ElabParamiters {
	public static HashMap<Integer, String> start(String paramenters) {
		String parCur = null;
		String[] param = new String[]{"FCP_REQID=", "FCP_LOGIN=", "FCP_USERID=", "FCP_USERNAME=", "FCP_PRINTER=",
				"FCP_SAVE_OUT=", "FCP_NUM_COPIES=", "PAR_CONC"};
		HashMap<String, Integer> paramVal = new HashMap();
		HashMap<Integer, String> paramValOut = new HashMap();
		paramVal.put("FCP_REQID=", 4);
		paramVal.put("FCP_LOGIN=", 1);
		paramVal.put("FCP_USERID=", 2);
		paramVal.put("FCP_USERNAME=", 3);
		paramVal.put("FCP_PRINTER=", 0);
		paramVal.put("FCP_SAVE_OUT=", 0);
		paramVal.put("FCP_NUM_COPIES=", 0);
		paramVal.put("PAR_CONC", 5);

		for (int i = 0; i < param.length; ++i) {
			int j = i + 1;
			if (j == param.length) {
				parCur = paramenters.substring(
						paramenters.indexOf(param[i], paramenters.indexOf(param[i - 1]) + param[i - 1].length())
								+ param[i].length() + 1);
				paramValOut.put((Integer) paramVal.get(param[i]), parCur.replace("\"", ""));
				int vCount = (Integer) paramVal.get(param[i]);

				for (String vString = null; parCur != null && !parCur.equals(""); ++vCount) {
					vString = parCur.substring(parCur.indexOf("\"") + 1,
							parCur.indexOf("\"", parCur.indexOf("\"") + 1));
					paramValOut.put(vCount, vString);
					parCur = parCur.substring(vString.length() + 3);
				}
			} else {
				parCur = paramenters.substring(paramenters.indexOf(param[i]) + param[i].length(),
						paramenters.indexOf(param[i + 1], paramenters.indexOf(param[i]) + param[i].length()) - 1);
				paramValOut.put((Integer) paramVal.get(param[i]), parCur.replace("\"", ""));
			}
		}

		return paramValOut;
	}
}
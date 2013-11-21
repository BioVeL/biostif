package de.fraunhofer.iais.kd.biovel.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18nUtils {

	private static final String FILENAME = "de.fraunhofer.iais.kd.biovel.i18n.Messages";

	private static final ResourceBundle resourceBundle;

	static {
		resourceBundle = ResourceBundle.getBundle(FILENAME);
	}

	public static String getString(String key) {
		return resourceBundle.getString(key);
	}

	public static String toJson(String lang) {

		//UT hmmm, this is not quite safe yet, as the default locale may be something 
		//different than "de_"
		//actually it'd need a call to Local.setDefault, but this might break other apps
		//running in the same VM
		Locale oldLoc =  Locale.getDefault();
		
		Locale loc = lang == null ? Locale.getDefault() : new Locale(lang);
		if( lang == null ){
			loc = Locale.ENGLISH;//we make English the default
			lang = loc.getLanguage();
		}
		Locale.setDefault(Locale.ENGLISH);
		ResourceBundle tResourceBundle = ResourceBundle
				.getBundle(FILENAME, loc);

		Locale.setDefault(oldLoc);
		
		StringBuilder sb = new StringBuilder(5000);
		sb.append("{ 'lang':'").append(lang).append("'");
		for (String key : tResourceBundle.keySet()) {
			sb.append(",'").append(key).append("':'")
					.append(tResourceBundle.getString(key)).append("'");
		}

		sb.append("}");

		return sb.toString();
	}

	public static void f() {
		ResourceBundle rb = ResourceBundle
				//.getBundle("de.fraunhofer.iais.kd.biovel.i18n.Messages");
				.getBundle(FILENAME);

		try {
			System.out.println("--------------------------- "
					+ rb.getObject("greeting"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.out.print(toJson("en")+"\n");
		System.out.print(toJson(null)+"\n");

	}

}

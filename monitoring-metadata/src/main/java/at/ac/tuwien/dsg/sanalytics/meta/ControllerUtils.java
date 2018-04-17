package at.ac.tuwien.dsg.sanalytics.meta;

import javax.servlet.http.HttpServletRequest;

public class ControllerUtils {

	public static String getBaseUrl(HttpServletRequest req) {
		StringBuffer url = req.getRequestURL();
		String uri = req.getRequestURI();
		String ctx = req.getContextPath();
		String base = url.substring(0, url.length() - uri.length() + ctx.length()) + "/";
		return base;
	}

}

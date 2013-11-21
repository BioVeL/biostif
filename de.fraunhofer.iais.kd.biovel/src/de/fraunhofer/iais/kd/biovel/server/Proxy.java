package de.fraunhofer.iais.kd.biovel.server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Generic Proxy class. It proxies requests to a URL given by the 'url'
 * parameter.
 * 
 * @author utaddei started this for a simple get proxy
 * @author mfabritius did some more work for post and xm only requests
 * @author anonymous made a mess out of it, added allowed hosts, username and passwords, etc etc
 */
public class Proxy extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 761939148453645554L;
	private final String[] allowedHosts = { "http://www.openlayers.org", "http://openlayers.org",
			"http://labs.metacarta.com", "http://world.freemap.in", "http://prototype.openmnnd.org",
			"http://geo.openplans.org", "http://www.openstreetmap.org", "http://localhost:8080",
			"https://biovel.iais.fraunhofer.de", "http://biovel.iais.fraunhofer.de",
			"http://tile.openstreetmap.org", "http://data.gbif.or",
			"http://a.tile.openstreetmap.org","http://b.tile.openstreetmap.org","http://c.tile.openstreetmap.org",
	"http://nominatim.openstreetmap.org/"};

	//TODO put here the right user/password or pass it through
//	private final String user = "demouser";
//	private final String pwd = "topicmaps";
//	private Authenticator auth = null;

	public Proxy() {
		super();
//		this.auth = new Authenticator() {
//			@Override
//			protected PasswordAuthentication getPasswordAuthentication() {
//				return new PasswordAuthentication(Proxy.this.user, Proxy.this.pwd.toCharArray());
//			}
//		};
//		Authenticator.setDefault(this.auth);

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		long t = System.currentTimeMillis();

		String target = req.getParameter("url");
		String format = req.getParameter("FORMAT");
		// test if url is allowed
		if (target == null || target.length() == 0) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing 'url' parameter.");
			return;
		}
		int i = 0;
		// System.out.println("Proxy: Compare target " + target + " with " +
		// allowedHosts.toString());
		for (i = 0; i < this.allowedHosts.length; i++) {
			if (target.indexOf(this.allowedHosts[i]) == 0) {
				break;
			}
		}
		// System.out.println("Proxy: Compare target " + target + ", found "
		// + (i >= this.allowedHosts.length ? "false" : this.allowedHosts[i]));

		if (i >= this.allowedHosts.length) {
			resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "The proxy does not allow you to access the location "
					+ target);
			return;
		}

		if (format != null && format.contains("image")) {
			rewriteImageToResponse(req, resp, format, target);
		} else {
			StringBuilder sb = new StringBuilder();
			Enumeration<String> e = req.getParameterNames();
			while (e.hasMoreElements()) {
				String k = e.nextElement();
				if (!k.equalsIgnoreCase("url")) {
					sb.append('&').append(k).append('=').append(req.getParameter(k));
				}
			}

			rewriteToResponse(target, sb.toString(), req, resp, getbody(req));
		}
	}

	public final void rewriteToResponse(String target, String pars, HttpServletRequest req, HttpServletResponse resp,
			String body) throws IOException {

		resp.setContentType("text/xml");
		try {

			if (req.getMethod().contains("GET")) {
				if (pars != null && pars.length() > 0) {
					target += "?" + pars;
				}
			}
			// System.out.println("Redirecting to " + target);
			URL u = new URL(target);

			URLConnection conn = u.openConnection();

			if (body.length() != 0) {
				conn.setDoOutput(true);
				conn.setRequestProperty("Content-Type", "application/xml");
//				conn.setRequestProperty("Authorization", this.user + ":" + this.pwd);
				OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
				// System.out.println("writing Body to target: " + target + ": "
				// + body);

				wr.write(body);
				wr.flush();
			} else if (req.getMethod().contains("POST")) {
				conn.setDoOutput(true);
//				conn.setRequestProperty("Authorization", this.user + ":" + this.pwd);
				OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
				wr.write(pars);
				wr.flush();
			}
			// for (int i = 0; i < conn.getHeaderFields().size(); i++) {
			// System.out.println("Connection header :" +
			// conn.getHeaderFieldKey(i) + " = "
			// + conn.getHeaderField(conn.getHeaderFieldKey(i)));
			// }

			InputStream is = conn.getInputStream();

			BufferedReader isBufferedReader = new BufferedReader(new InputStreamReader(is));
			if (target.contains("JSON") || (pars != null && pars.contains("JSON"))) {
				resp.setContentType("text/plain");
			}
			resp.setCharacterEncoding("UTF-8");

			String line;
			String messagebody = "";
			while ((line = isBufferedReader.readLine()) != null) {
				messagebody += line;
			}
			// System.out.println("Answer is: " + messagebody);

			isBufferedReader.close();
			OutputStream os = resp.getOutputStream();

			byte[] messagebytes = messagebody.getBytes();
			ByteArrayOutputStream baos = new ByteArrayOutputStream(messagebytes.length);
			baos.write(messagebytes);
			baos.writeTo(os);
			os.flush();
			os.close();

		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}

	}

	public void rewriteImageToResponse(HttpServletRequest req, HttpServletResponse resp, String imageType, String target) {
		try {
			URL url = new URL(target);
			URLConnection conn = url.openConnection();

			// BufferedImage img = ImageIO.read(conn.getInputStream());
			Map<String, List<String>> headers = conn.getHeaderFields();

			List<String> values;
			for (String key : headers.keySet()) {
				values = headers.get(key);
				resp.setHeader(key, values.get(0));
			}
			// String etag = conn.getHeaderField("ETAG");
			// resp.setContentType(imageType);
			// resp.setContentLength(conn.getContentLength());
			// resp.setHeader("ETAG", etag);

			InputStream in = conn.getInputStream();
			OutputStream out = resp.getOutputStream();

			byte[] buf = new byte[1024];
			int count = 0;
			while ((count = in.read(buf)) >= 0) {
				out.write(buf, 0, count);
			}
			in.close();
			out.close();
			// int length = imageType.length();
			// ImageIO.write(img, imageType.substring(length-3),
			// resp.getOutputStream());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	IOException {
		doGet(request, response);
	}

	public final String getbody(HttpServletRequest req) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
			InputStream inputStream = req.getInputStream();
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					throw ex;
				}
			}
		}
		String body = stringBuilder.toString();

		return body;
	}
}

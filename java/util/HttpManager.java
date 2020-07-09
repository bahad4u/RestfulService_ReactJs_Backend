package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

//import ADMDService;
import exceptions.HttpException;

public class HttpManager {

	private static Logger log = LogManager.getLogger(HttpManager.class.getSimpleName());
	private static Properties prop = PropertyConfiguration.getProperties();

	public static String[] httpCall(String url, Map<String, String> headers, int timeout, String data, String httpType)
			throws HttpException, IOException {
		System.out.println("URL: "+url);
		StringBuilder response = new StringBuilder();
		String[] responseArray = new String[2];
		int responseCode = 0;
		OutputStream os = null;
		BufferedReader in = null;
		
		try {
			URL obj = new URL(url);

			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			if(url.contains("sonarqube")) {
			String userpass = prop.getProperty("sonaruser") + ":" + prop.getProperty("sonarpwd");
			String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
			con.setRequestProperty ("Authorization", basicAuth);}

			con.setDoInput(true); // To set the connection

			if (headers != null) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					con.setRequestProperty(entry.getKey(), entry.getValue());
				}
			} else {
				con.setRequestProperty("Content-Type", "application/json");
			}

			con.setConnectTimeout(timeout);
			con.setRequestMethod(httpType);

			String httpTypes = prop.getProperty("httpBody");

			if (Arrays.asList(httpTypes.split(",")).contains(httpType)) {
				con.setDoOutput(true);
				if (data != null) {
					os = con.getOutputStream();
					os.write(data.getBytes());
				}
			}
			responseCode = con.getResponseCode();

			//log.info("Http Type: " + httpType + "\nUrl: " + url + "\n" + "Response Code: " + responseCode);
			if (responseCode == 200) {
				in = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
			}
		} catch (Exception e) {
			throw new HttpException(responseCode, responseArray[1]);
		}finally {
			if(in!=null) {
				in.close();
			}
			if(os!=null) {
				os.close();
			}
		}
		responseArray[0] = Integer.toString(responseCode);
		responseArray[1] = response.toString();

		return responseArray;
	}
}

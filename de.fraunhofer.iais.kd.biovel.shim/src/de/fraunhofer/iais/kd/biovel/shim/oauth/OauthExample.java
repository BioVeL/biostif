package de.fraunhofer.iais.kd.biovel.shim.oauth;


import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.scribe.model.Request;
import org.scribe.model.Verb;

import com.sun.jersey.api.client.Client;

/**
 * a simple program to get flickr token and token secret.
 * 
 * @author Mark Zang
 * 
 * @author Pavle Jonoski -  accommodated the example for megx.net OAuth API (changed the key / secret and the appropriate URL)
 */
public class OauthExample {

    private static String key = "YjI2NzE3NTQtNWViMi00Yzk0LThiMjUtMTJhZjQzYTUwN2Vm";
    private static String secret = "ZEyv7nWc-SjKu3EU6XDqUPnVtSQYkqwIae9KY_n89ZaAtUBMcW4X24fab_SyHevYqMnncUq3latBYFrA2iq8KA";
    
    private static String token;
    private static String token_secret;

    private static final String HMAC_SHA1 = "HmacSHA1";

    private static final String ENC = "UTF-8";

    private static Base64 base64 = new Base64();
    
    private static Client client;

    /**
     * 
     * @param url
     *            the url for "request_token" URLEncoded.
     * @param params
     *            parameters string, URLEncoded.
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private static String getSignature(String url, String params)
            throws UnsupportedEncodingException, NoSuchAlgorithmException,
            InvalidKeyException {
        /**
         * base has three parts, they are connected by "&": 1) protocol 2) URL
         * (need to be URLEncoded) 3) Parameter List (need to be URLEncoded).
         */
        StringBuilder base = new StringBuilder();
        base.append("GET&");
        base.append(url);
        base.append("&");
        base.append(params);
        System.out.println("Stirng for oauth_signature generation:" + base);
        // yea, don't ask me why, it is needed to append a "&" to the end of
        // secret key.
        
        if(token_secret == null){
            token_secret = "";
        }
        
        byte[] keyBytes = (secret + "&" + token_secret).getBytes(ENC);

        SecretKey key = new SecretKeySpec(keyBytes, HMAC_SHA1);

        Mac mac = Mac.getInstance(HMAC_SHA1);
        mac.init(key);

        // encode it, base64 it, change it to string and return.
        return new String(base64.encode(mac.doFinal(base.toString().getBytes(
                ENC))), ENC).trim();
    }

    /**
     * @param args
     * @throws IOException
     * @throws ClientProtocolException
     * @throws URISyntaxException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static void main(String[] args) throws ClientProtocolException,
            IOException, URISyntaxException, InvalidKeyException,
            NoSuchAlgorithmException {

        HttpClient httpclient = new DefaultHttpClient();
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        // These params should ordered in key
        qparams.add(new BasicNameValuePair("oauth_callback", "oob"));
        qparams.add(new BasicNameValuePair("oauth_consumer_key", key));
        qparams.add(new BasicNameValuePair("oauth_nonce", ""
                + (int) (Math.random() * 100000000)));
        qparams.add(new BasicNameValuePair("oauth_signature_method",
                "HMAC-SHA1"));
        qparams.add(new BasicNameValuePair("oauth_timestamp", ""
                + (System.currentTimeMillis() / 1000)));
        qparams.add(new BasicNameValuePair("oauth_version", "1.0"));

        // generate the oauth_signature
        String signature = getSignature(URLEncoder.encode(
                "http://iwgate.poweredbyclear.com:9080/megx.net/oauth/request_token", ENC),
                URLEncoder.encode(URLEncodedUtils.format(qparams, ENC), ENC));

        // add it to params list
        qparams.add(new BasicNameValuePair("oauth_signature", signature));

        // generate URI which lead to access_token and token_secret.
        URI uri = URIUtils.createURI("http", "iwgate.poweredbyclear.com:9080", -1,
                "/megx.net/oauth/request_token",
                URLEncodedUtils.format(qparams, ENC), null);

        System.out.println("Get Token and Token Secrect from:"
                + uri.toString());

        HttpGet httpget = new HttpGet(uri);
        // output the response content.
        System.out.println("oken and Token Secrect:");

        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        String tokenResponse = "";
        if (entity != null) {
            InputStream instream = entity.getContent();
            int len;
            byte[] tmp = new byte[2048];
            while ((len = instream.read(tmp)) != -1) {
//                System.out.println(new String(tmp, 0, len, ENC));
                tokenResponse = new String(tmp, 0, len, ENC);
            }
        }
        
        System.out.println("tokenResponse: " + tokenResponse);
        String[] values = tokenResponse.split("&");
        System.out.println("num token: " + values.length);
        String oauth_token = values[0].replace("oauth_token=", "");
        token_secret = values[0].replace("oauth_token_secret=", "");
        System.out.println("oauth_token: " + oauth_token);
        System.out.println("oauth_token_secret: " + token_secret);
        
        
       String authServiceProvider = "http://iwgate.poweredbyclear.com:9080/megx.net/ws/v1/verify_credentials/v1.0.0";
//      authServiceProvider = "http://megx.net/ws/v1/verify_credentials/v1.0.0";
      
      //check the auth
      authServiceProvider = StringCodec.urldecode(authServiceProvider);
      
      List<NameValuePair> qparams1 = new ArrayList<NameValuePair>();
      // These params should ordered in key
      qparams1.add(new BasicNameValuePair("realm", "http://iwgate.poweredbyclear.com:9080/megx.net/"));
      qparams1.add(new BasicNameValuePair("oauth_consumer_key", key));
      qparams1.add(new BasicNameValuePair("oauth_token", oauth_token));
      qparams1.add(new BasicNameValuePair("oauth_nonce", ""
              + (int) (Math.random() * 100000000)));
      qparams1.add(new BasicNameValuePair("oauth_signature_method",
              "HMAC-SHA1"));
      qparams1.add(new BasicNameValuePair("oauth_timestamp", ""
              + (System.currentTimeMillis() / 1000)));
      qparams1.add(new BasicNameValuePair("oauth_version", "1.0"));

      // generate the oauth_signature
      String signature1 = getSignature(URLEncoder.encode(
              "http://iwgate.poweredbyclear.com:9080/megx.net/ws/v1/verify_credentials/v1.0.0", ENC),
              URLEncoder.encode(URLEncodedUtils.format(qparams1, ENC), ENC));

      // add it to params list
      qparams1.add(new BasicNameValuePair("oauth_signature", signature1));
      

      StringBuffer buffer = new StringBuffer();
      for(NameValuePair pair: qparams1){
          
        if(buffer.length() > 0){
          buffer.append(",");
        } else {
            buffer.append("OAuth ");
        }
        System.out.println("key: " + pair.getName() + " - " + StringCodec.urlencode((pair.getValue())));
        buffer.append(StringCodec.urlencode(pair.getName()));
        buffer.append("=\"");
        buffer.append(StringCodec.urlencode(pair.getValue()));
        buffer.append("\"");
      }
      
      String result = buffer.toString();

      System.out.println("\nauth header:\n" + result);
      
          
          Request vcRequest = new Request(Verb.GET, authServiceProvider);
          vcRequest.getHeaders().put("Authorization", result);
          
          org.scribe.model.Response vcResponse = vcRequest.send();
          
          System.out.println("res status: " + vcResponse.getCode());
          String body = vcResponse.getBody();
          System.out.println("response_body:\n"+body);
       
        
    }

}
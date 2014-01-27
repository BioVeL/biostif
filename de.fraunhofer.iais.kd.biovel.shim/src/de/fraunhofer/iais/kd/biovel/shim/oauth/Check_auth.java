package de.fraunhofer.iais.kd.biovel.shim.oauth;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.model.Request;
import org.scribe.model.Verb;

/**
 * a simple program to get flickr token and token secret.
 * 
 * @author Mark Zang
 * 
 * @author Pavle Jonoski -  accommodated the example for megx.net OAuth API (changed the key / secret and the appropriate URL)
 */
public class Check_auth {

    private static String key = "ZGI0MjlkYzctZjEzMi00N2ViLWEwMGUtMDM4Zjg4MzM0ZDA2";// "YjI2NzE3NTQtNWViMi00Yzk0LThiMjUtMTJhZjQzYTUwN2Vm";
    private static String secret = "PWQaJF3Qw7JU2YPDTG57KUJ7htzJ0dE_Rp5FWX491q32aGLVm-FBldTZ9eiaaqJN9UhpeCvuZijyg0kRiOiqjg";//"ZEyv7nWc-SjKu3EU6XDqUPnVtSQYkqwIae9KY_n89ZaAtUBMcW4X24fab_SyHevYqMnncUq3latBYFrA2iq8KA";
    private static String token_secret;

    private static final String HMAC_SHA1 = "HmacSHA1";

    private static final String ENC = "UTF-8";

    private static Base64 base64 = new Base64();
    

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
//    private static String getSignature(String url, String params)
//            throws UnsupportedEncodingException, NoSuchAlgorithmException,
//            InvalidKeyException {
//        /**
//         * base has three parts, they are connected by "&": 1) protocol 2) URL
//         * (need to be URLEncoded) 3) Parameter List (need to be URLEncoded).
//         */
//        StringBuilder base = new StringBuilder();
//        base.append("GET&");
//        base.append(url);
//        base.append("&");
//        base.append(params);
//        System.out.println("Stirng for oauth_signature generation:" + base);
//        // yea, don't ask me why, it is needed to append a "&" to the end of
//        // secret key.
//        
//        if(token_secret == null){
//            token_secret = "";
//        }
//        
//        byte[] keyBytes = (secret + "&" + token_secret).getBytes(ENC);
//
//        SecretKey key = new SecretKeySpec(keyBytes, HMAC_SHA1);
//
//        Mac mac = Mac.getInstance(HMAC_SHA1);
//        mac.init(key);
//
//        // encode it, base64 it, change it to string and return.
//        return new String(base64.encode(mac.doFinal(base.toString().getBytes(
//                ENC))), ENC).trim();
//    }

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

//        HttpClient httpclient = new DefaultHttpClient();
////        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
////        // These params should ordered in key
////        qparams.add(new BasicNameValuePair("oauth_callback", "oob"));
////        qparams.add(new BasicNameValuePair("oauth_consumer_key", key));
////        qparams.add(new BasicNameValuePair("oauth_nonce", ""
////                + (int) (Math.random() * 100000000)));
////        qparams.add(new BasicNameValuePair("oauth_signature_method",
////                "HMAC-SHA1"));
////        qparams.add(new BasicNameValuePair("oauth_timestamp", ""
////                + (System.currentTimeMillis() / 1000)));
////        qparams.add(new BasicNameValuePair("oauth_version", "1.0"));
////
////        // generate the oauth_signature
////        String signature = getSignature(URLEncoder.encode(
////                "http://iwgate.poweredbyclear.com:9080/megx.net/oauth/request_token", ENC),
////                URLEncoder.encode(URLEncodedUtils.format(qparams, ENC), ENC));
////
////        // add it to params list
////        qparams.add(new BasicNameValuePair("oauth_signature", signature));
////
////        // generate URI which lead to access_token and token_secret.
////        URI uri = URIUtils.createURI("http", "iwgate.poweredbyclear.com:9080", -1,
////                "/megx.net/oauth/request_token",
////                URLEncodedUtils.format(qparams, ENC), null);
////
////        System.out.println("Get Token and Token Secrect from:"
////                + uri.toString());
////
////        HttpGet httpget = new HttpGet(uri);
////        // output the response content.
////        System.out.println("oken and Token Secrect:");
////
////        HttpResponse response = httpclient.execute(httpget);
////        HttpEntity entity = response.getEntity();
////        String tokenResponse = "";
////        if (entity != null) {
////            InputStream instream = entity.getContent();
////            int len;
////            byte[] tmp = new byte[2048];
////            while ((len = instream.read(tmp)) != -1) {
//////                System.out.println(new String(tmp, 0, len, ENC));
////                tokenResponse = new String(tmp, 0, len, ENC);
////            }
////        }
////        
////        String[] values = tokenResponse.split("&");
////        String oauth_token = values[0].replace("oauth_token=", "");
////        token_secret = values[0].replace("oauth_token_secret=", "");
//        
//      
//        String oauth_token = "YzFlYjUwYjktMGM5MS00MzZmLTkxNDQtYzY3ZTY0ZDg5NTY5";
//        token_secret = "VNJTycBufqOHPrMSLBa_oNdPpFpTjV2Uv_Rdy2sMPR4R2CTCfgl-eef5FxmoeK-JKe3_83-sEojPrV9gFZ6lkQ";
//        
//        String authURL = "http://iwgate.poweredbyclear.com:9080/megx.net/ws/v1/verify_credentials/v1.0.0";
//        
//      List<NameValuePair> qparams1 = new ArrayList<NameValuePair>();
//      // These params should ordered in key
////      qparams1.add(new BasicNameValuePair("realm", "http://iwgate.poweredbyclear.com:9080/megx.net/"));
//      qparams1.add(new BasicNameValuePair("oauth_consumer_key", key));
//      qparams1.add(new BasicNameValuePair("oauth_nonce", "" + (int) (Math.random() * 100000000)));
//      qparams1.add(new BasicNameValuePair("oauth_signature_method", "HMAC-SHA1"));
//      qparams1.add(new BasicNameValuePair("oauth_timestamp", "" + (System.currentTimeMillis() / 1000)));
//      qparams1.add(new BasicNameValuePair("oauth_token", oauth_token));
//      qparams1.add(new BasicNameValuePair("oauth_version", "1.0"));
//      
////      HashMap<String, String> requestTokenHash = new HashMap<String, String>();
////      requestTokenHash.put("oauth_consumer_key", key);
////      requestTokenHash.put("oauth_nonce", "" +(int) (Math.random() * 100000000));
////      requestTokenHash.put("oauth_signature_method", "HMAC-SHA1");
////      requestTokenHash.put("oauth_timestamp", "" + (System.currentTimeMillis() / 1000));
////      requestTokenHash.put("oauth_token", oauth_token);
////      requestTokenHash.put("oauth_version", "1.0");
//      
////      requestTokenHash.put("oauth_signature", generateAccessSignature(generateSignatureBaseString("GET", this.authServiceProvider, requestTokenHash, null)));
//      
//      
////      GET&
////      http%3A%2F%2Fiwgate.poweredbyclear.com%3A9080%2Fmegx.net%2Fws%2Fv1%2Fverify_credentials%2Fv1.0.0&
////      oauth_consumer_key%3DZGI0MjlkYzctZjEzMi00N2ViLWEwMGUtMDM4Zjg4MzM0ZDA2%26
////      oauth_nonce%3D44881949%26
////      oauth_signature_method%3DHMAC-SHA1%26
////      oauth_timestamp%3D1363193881%26
////      oauth_token%3DYzFlYjUwYjktMGM5MS00MzZmLTkxNDQtYzY3ZTY0ZDg5NTY5%26
////      oauth_version%3D1.0
//
//      // generate the oauth_signature
////      String signature1 = getSignature(URLEncoder.encode(
////              "http://iwgate.poweredbyclear.com:9080/megx.net/ws/v1/verify_credentials/v1.0.0", ENC),
////              URLEncoder.encode(URLEncodedUtils.format(qparams1, ENC), ENC));
//      
//      StringBuilder base = new StringBuilder();
//      base.append("GET&");
//      base.append(URLEncoder.encode(authURL, ENC));
//      base.append("&");
//      base.append(URLEncoder.encode(URLEncodedUtils.format(qparams1, ENC), ENC));
//      System.out.println("Stirng for oauth_signature generation:" + base);
//      // yea, don't ask me why, it is needed to append a "&" to the end of
//      // secret key.
//      
//      if(token_secret == null){
//          token_secret = "";
//      }
//      
//      byte[] keyBytes = (secret + "&" + token_secret).getBytes(ENC);
//
//      SecretKey key = new SecretKeySpec(keyBytes, HMAC_SHA1);
//
//      Mac mac = Mac.getInstance(HMAC_SHA1);
//      mac.init(key);
//
//      // encode it, base64 it, change it to string and return.
//      String sig = new String(base64.encode(mac.doFinal(base.toString().getBytes(
//              ENC))), ENC).trim();
//      
//
//      // add it to params list
//      qparams1.add(new BasicNameValuePair("oauth_signature", URLEncoder.encode(sig,ENC)));
//      
//
//      StringBuffer sBuffer = new StringBuffer();
//      for(NameValuePair pair: qparams1){
//          
//        if(sBuffer.length() > 0){
//          sBuffer.append(",");
//        } else {
//            sBuffer.append("OAuth ");
//        }
//        System.out.println("key: " + pair.getName() + " - " + ((pair.getValue())));
//        sBuffer.append((pair.getName()));
//        sBuffer.append("=\"");
//        sBuffer.append((pair.getValue()));
//        sBuffer.append("\"");
//      }
//      
//      String authHeader = sBuffer.toString();
//      System.out.println("authHeader: " + authHeader);
        
        String authHeader = "OAuth oauth_consumer_key=\"ZGI0MjlkYzctZjEzMi00N2ViLWEwMGUtMDM4Zjg4MzM0ZDA2\",oauth_nonce=\"67506854\",oauth_signature_method=\"HMAC-SHA1\",oauth_timestamp=\"1363358323\",oauth_token=\"YzFlYjUwYjktMGM5MS00MzZmLTkxNDQtYzY3ZTY0ZDg5NTY5\",oauth_version=\"1.0\",oauth_signature=\"d7mIQlEHGO1KN7PVxL9lp1jJ2UI%3D\"";

          Request vcRequest = new Request(Verb.GET, "http://iwgate.poweredbyclear.com:9080/megx.net/ws/v1/verify_credentials/v1.0.0");
          vcRequest.getHeaders().put("Authorization", authHeader);
          
          org.scribe.model.Response vcResponse = vcRequest.send();
          
          System.out.println("res status: " + vcResponse.getCode());
          String body = vcResponse.getBody();
          System.out.println("response_body:\n"+body);
          
          
//          User user = null;
          try {
              JSONObject respBody = new JSONObject(body);
              
              System.out.println("userN: " + respBody.getJSONObject("data").getString("username"));
              
              String username = respBody.getJSONObject("data").getString("username");
//              user.firstName = respBody.getJSONObject("data").optString("firstName");
//              user.lastName = respBody.getJSONObject("data").optString("lastName");
//              user.initials = respBody.getJSONObject("data").optString("initials");
//              user.email = respBody.getJSONObject("data").optString("email");
              
              System.out.println("User name: " + username);
              
          } catch (JSONException e) {
              final String msg = "Errot creating user from auth data";
              System.out.println(msg + " - " + e.toString());
          }
          
        
    }

}
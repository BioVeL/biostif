package de.fraunhofer.iais.kd.biovel.shim.oauth;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.TreeSet;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * a simple program to get flickr token and token secret.
 * 
 * @author Mark Zang
 * 
 * @author Pavle Jonoski -  accommodated the example for megx.net OAuth API (changed the key / secret and the appropriate URL)
 */
public class OauthExampleWithToken1 {

    private static String key = "ZGI0MjlkYzctZjEzMi00N2ViLWEwMGUtMDM4Zjg4MzM0ZDA2";// "YjI2NzE3NTQtNWViMi00Yzk0LThiMjUtMTJhZjQzYTUwN2Vm";
    private static String secret = "PWQaJF3Qw7JU2YPDTG57KUJ7htzJ0dE_Rp5FWX491q32aGLVm-FBldTZ9eiaaqJN9UhpeCvuZijyg0kRiOiqjg";//"ZEyv7nWc-SjKu3EU6XDqUPnVtSQYkqwIae9KY_n89ZaAtUBMcW4X24fab_SyHevYqMnncUq3latBYFrA2iq8KA";
    private static String token_secret;

    private static final String HMAC_SHA1 = "HmacSHA1";
    private static final String ENC = "UTF-8";
    private static Base64 base64 = new Base64();
    

    public static void main(String[] args){
        
      
        String oauth_token = "YzFlYjUwYjktMGM5MS00MzZmLTkxNDQtYzY3ZTY0ZDg5NTY5";
        token_secret = "VNJTycBufqOHPrMSLBa_oNdPpFpTjV2Uv_Rdy2sMPR4R2CTCfgl-eef5FxmoeK-JKe3_83-sEojPrV9gFZ6lkQ";
        
        String authURL = "http://iwgate.poweredbyclear.com:9080/megx.net/ws/v1/verify_credentials/v1.0.0";
        
        
//        without apache
        
        HashMap<String, String> requestTokenHash = new HashMap<String, String>();
        requestTokenHash.put("oauth_consumer_key", key);
        requestTokenHash.put("oauth_nonce", "" +(int) (Math.random() * 100000000));
        requestTokenHash.put("oauth_signature_method", "HMAC-SHA1");
        requestTokenHash.put("oauth_timestamp", "" + (System.currentTimeMillis() / 1000));
        requestTokenHash.put("oauth_token", oauth_token);
        requestTokenHash.put("oauth_version", "1.0");
        
        //getNormalizedRequestParameter        
        StringBuffer requestParameterBuffer = new StringBuffer();
             
        TreeSet<String> sortedKeys = null;
        if(requestTokenHash.size() > 0){
          sortedKeys = new TreeSet<String>(requestTokenHash.keySet());  //sort keys
          
          for(String key: sortedKeys){
            try {
                requestParameterBuffer.append("&");
                requestParameterBuffer.append(URLEncoder.encode(key, ENC));
                requestParameterBuffer.append("=");
                requestParameterBuffer.append(URLEncoder.encode(requestTokenHash.get(key),ENC));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
          }
        }
        String requestParameter = requestParameterBuffer.toString().substring(1,requestParameterBuffer.toString().length());
        System.out.println("requestParameter: " + requestParameter);
        //getNormalizedRequestParameter end
        
        
        StringBuffer signatureBaseBuffer = new StringBuffer();
        try {
            signatureBaseBuffer.append("GET");
            signatureBaseBuffer.append("&");
            signatureBaseBuffer.append(URLEncoder.encode(authURL, ENC));
            signatureBaseBuffer.append("&");
            signatureBaseBuffer.append(URLEncoder.encode(requestParameter, ENC));
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
//        buffer.append(StringCodec.urlencode((url)));
//        buffer.append(url);
        
        String signatureBaseResult = signatureBaseBuffer.toString();
        
        System.out.println("\nsignature base string:\n" + signatureBaseResult);
        
        //ENC
        byte[] keyBytes = null;
        try {
            keyBytes = (secret + "&" + token_secret).getBytes(ENC);
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        SecretKey key = new SecretKeySpec(keyBytes, HMAC_SHA1);

        Mac mac = null;
        try {
            mac = Mac.getInstance(HMAC_SHA1);
            mac.init(key);
        } catch (NoSuchAlgorithmException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // encode it, base64 it, change it to string and return.
        String signature = null;
        try {
            signature = new String(base64.encode(mac.doFinal(signatureBaseResult.toString().getBytes(ENC))), ENC).trim();
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IllegalStateException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        ///
        
        StringBuffer oAuthHeaderBuffer = new StringBuffer();
        for(String reqkey: sortedKeys){
            
            try {
                  if(oAuthHeaderBuffer.length() > 0){
                      oAuthHeaderBuffer.append(",");
                  } else {
                      oAuthHeaderBuffer.append("OAuth ");
                  }
                  
                  oAuthHeaderBuffer.append(URLEncoder.encode(reqkey, ENC));
                  oAuthHeaderBuffer.append("=\"");
                  oAuthHeaderBuffer.append(URLEncoder.encode(requestTokenHash.get(reqkey),ENC));
                  oAuthHeaderBuffer.append("\"");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            oAuthHeaderBuffer.append(",");
            oAuthHeaderBuffer.append(URLEncoder.encode("oauth_signature", ENC));
            oAuthHeaderBuffer.append("=\"");
            oAuthHeaderBuffer.append(URLEncoder.encode(signature,ENC));
            oAuthHeaderBuffer.append("\"");
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
//        ...requestTokenHash.put("oauth_signature", URLEncoder.encode(signature,ENC));
        
        String authHeader = oAuthHeaderBuffer.toString();

        System.out.println("\nauth header:\n" + authHeader);
  
      
//          Request vcRequest = new Request(Verb.GET, "http://iwgate.poweredbyclear.com:9080/megx.net/ws/v1/verify_credentials/v1.0.0");
//          vcRequest.getHeaders().put("Authorization", authHeader);
//          
//          org.scribe.model.Response vcResponse = vcRequest.send();
//          
//          System.out.println("res status: " + vcResponse.getCode());
//          String body = vcResponse.getBody();
//          System.out.println("response_body:\n"+body);
//          
//          
////          User user = null;
//          try {
//              JSONObject respBody = new JSONObject(body);
//              
//              System.out.println("userN: " + respBody.getJSONObject("data").getString("username"));
//              
//              String username = respBody.getJSONObject("data").getString("username");
////              user.firstName = respBody.getJSONObject("data").optString("firstName");
////              user.lastName = respBody.getJSONObject("data").optString("lastName");
////              user.initials = respBody.getJSONObject("data").optString("initials");
////              user.email = respBody.getJSONObject("data").optString("email");
//              
//              System.out.println("User name: " + username);
//              
//          } catch (JSONException e) {
//              final String msg = "Errot creating user from auth data";
//              System.out.println(msg + " - " + e.toString());
//          }
          
        
    }

}
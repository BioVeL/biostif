package de.fraunhofer.iais.kd.biovel.shim.oauth;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class OauthClient {
    
    private static final Logger LOG = Logger.getLogger(OauthClient.class.getName());
    private Client client;
    
    private String authServiceProvider = null;
    private String credentialsAuthorizationToken = null;
    
    String ENC = "UTF-8";
    
    
    /**
     * 
     * check the oAuthecho credentials against the oauth provider
     * 
     * @param oauth_client_id
     * @param access_token
     * @param authServiceProvider
     * @return
     * @throws UnsupportedEncodingException 
     */
    
    public Response checkAuthCredentials (String credentialsAuthorizationToken, String authServiceProvider){
        
        //check the auth
        this.client = Client.create();
        ClientResponse authCheck = null;

        try {
            this.authServiceProvider = URLDecoder.decode(authServiceProvider, ENC);
            this.credentialsAuthorizationToken = URLDecoder.decode(credentialsAuthorizationToken, ENC);
        } catch (UnsupportedEncodingException e1) {
            String msg = "Something wrong while decode auth headers: "+ e1.getMessage();
            LOG.info(msg);
            return Response.status(500).entity(msg).build();
        }
        
        try {
            authCheck = this.client.resource(this.authServiceProvider).header("Authorization", this.credentialsAuthorizationToken).get(ClientResponse.class);
        } catch (Exception e) {
              String msg = "Something wrong when check credentials: "+ e.getMessage();
              if(authCheck != null){
                  msg += " status: " +authCheck.getStatus() + " cause: " + authCheck.getEntity(String.class);
              }
              LOG.info(msg);
              return Response.status(500).entity(msg).build();
        }
            
        if (authCheck.getStatus() != 200) {
            String msg = "Authentification error - credential not veryfied: " + authCheck.getEntity(String.class);
            LOG.info(msg);
            return Response.status(authCheck.getStatus()).entity(msg).build();
        } else {
            
          String userData = authCheck.getEntity(String.class);
          System.out.println("userData: " + userData);
          String username = null;
          
          try {
              JSONObject respBody = new JSONObject(userData);              
              username = respBody.getJSONObject("data").getString("username");
    //          user.firstName = respBody.getJSONObject("data").optString("firstName");
    //          user.lastName = respBody.getJSONObject("data").optString("lastName");
    //          user.initials = respBody.getJSONObject("data").optString("initials");
    //          user.email = respBody.getJSONObject("data").optString("email");
          } catch (JSONException e) {
              final String msg = "Error creating user from auth data";
              LOG.info(msg + " - " + e.toString());
              return Response.status(500).entity(msg).build();
          }
          
          return Response.status(200).entity(username).build();
//          return Response.status(200).entity(userData).build();
        }
    }
}

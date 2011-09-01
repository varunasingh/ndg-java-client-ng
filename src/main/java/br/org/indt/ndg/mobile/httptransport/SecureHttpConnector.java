/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.org.indt.ndg.mobile.httptransport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

/**
 *
 * @author pwielgolaski
 */
public class SecureHttpConnector implements HttpConnection {

    private HttpConnection m_connection; //TODO remove
    private String m_url;
    private int m_mode;
    private Hashtable m_properties;
    private static String user = null;
    private static String password = null;
    private static boolean logged = false;


    private static DigestAuthResponse digestResponse = null;

    private static final String DIGEST = "digest";
    private static final String BASIC = "basic";
    private static final String AUTHORIZATION = "Authorization";
    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";

    public static HttpConnection open(String url) throws IOException, AuthorizationException{
        return open(url, Connector.READ);
    }
    
    public static HttpConnection open(String url, int mode) throws IOException, AuthorizationException {        
        authenticate();

        HttpConnection conn = null;
        conn = ( HttpConnection )Connector.open( url, mode );
        if(digestResponse != null){
            conn.setRequestProperty(AUTHORIZATION, digestResponse.getDigestHeader(conn));
        }
        return conn;
    }

    public static void authenticate() throws IOException, AuthorizationException{
        if(!logged){
            logged = tryLogin();
            if(!logged){
                throw new AuthorizationException();
            }
        }
    }

    public static void setCredentials(String aUser, String aPassword) {
        user = aUser;
        password = aPassword;
    }

    public static String getCurrentUser(){
        return user;
    }

    public static String getCurrentPassword(){
        return password;
    }

    public static void setAuthenticationFail(){
        logged = false;
    }

    private HttpConnection connection() {
        return m_connection;
    }

    public static boolean tryLogin() throws IOException{

        if(user == null || password == null){
            return false;
        }

        HttpConnection testConnection = null;
        boolean authorized = false;
        try {
            String url = "http://10.132.152.150:8080/ndg/ReceiveSurvey";//TODO change this URL

            testConnection = (HttpConnection)Connector.open( url );
            if (testConnection.getResponseCode() == HttpConnection.HTTP_UNAUTHORIZED) {
                String header = testConnection.getHeaderField(WWW_AUTHENTICATE);
                String authHeaderVal = "";
                if (header.toLowerCase().startsWith(BASIC)) {
                    authHeaderVal = "Basic " + BasicAuth.encode(user, password);
                } else if (header.toLowerCase().startsWith(DIGEST)) {
                    String args = header.substring(header.indexOf(' ') + 1);
                    digestResponse = new DigestAuthResponse(args, user, password); //TODO save user and passowrd
                    authHeaderVal =  digestResponse.getDigestHeader(testConnection);
                    testConnection.close();
                    testConnection = (HttpConnection)Connector.open( url );
                }

                testConnection.setRequestProperty(AUTHORIZATION, authHeaderVal);
                if(testConnection.getResponseCode() == HttpConnection.HTTP_UNAUTHORIZED){
                    authorized = false;
                }else{
                    authorized = true;
                }
            }else{
                authorized = true;
            }
        } catch (IOException ex) {
            authorized = false;
            throw ex;
        }finally{
            try{
                if(testConnection != null){
                    testConnection.close();
                }
            }catch(IOException ex){}
        }
        return authorized;
    }

//    private void checkIfAuthenticated() throws IOException {
//        if (!m_checked) {
//            m_checked = true;
//
//            HttpConnection testConnection = null;
//            boolean useClonedConnection = false;
//            if (m_connection.getRequestMethod().equals(HttpConnection.GET)) {
//                testConnection = m_connection;
//            } else if (m_connection.getRequestMethod().equals(HttpConnection.POST)) {
//                testConnection = duplicateConnection( );
//                useClonedConnection = true;
//            } else {
//                throw new UnsupportedEncodingException("HTTP Method is not supported by SecureHttpConnector");
//            }
//
//            if (testConnection.getResponseCode() == HttpConnection.HTTP_UNAUTHORIZED) {
//                // check type of authentication
//                String header = testConnection.getHeaderField(WWW_AUTHENTICATE);
//                String token = null;
//                if (header.toLowerCase().startsWith(BASIC)) {
//                     token =  "Basic " + BasicAuth.encode(m_user, m_password);
//                } else if (header.toLowerCase().startsWith(DIGEST)) {
//                    String args = header.substring(header.indexOf(' ') + 1);
////                    token =  DigestAuthResponse.digestResponse(testConnection,args, m_user, m_password );//TODO
//                }
//
//                if (useClonedConnection) {
//                    testConnection.close();
//                    setRequestProperty(AUTHORIZATION, token);
//                } else {
//                    // open new connection with copied properties plus authenticate token
//                    HttpConnection tmpConnection = duplicateConnection();
//                    m_connection.close();
//                    m_connection = tmpConnection;
//                    setRequestProperty(AUTHORIZATION, token);
//                }
//
//            } else {
//                if (useClonedConnection) {
//                    testConnection.close();
//                }
//            }
//        }
//    }

    private  HttpConnection duplicateConnection() throws IOException {
        HttpConnection connection = ( HttpConnection )Connector.open( m_url, m_mode);
        connection.setRequestMethod(connection().getRequestMethod());

        Enumeration keys = m_properties.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            connection.setRequestProperty((String)key, (String)m_properties.get(key));
        }
        return connection;
    }

    public void setRequestProperty(String key, String value) throws IOException {
        m_properties.put(key, value);
        connection().setRequestProperty(key, value);
    }

    public void setRequestMethod(String method) throws IOException {
        connection().setRequestMethod(method);
    }

    // <editor-fold desc="The following methods cause the transition to the Connected state when the connection is in Setup state.">

    public int getHeaderFieldInt(String name, int def) throws IOException {
//        checkIfAuthenticated();
        return connection().getHeaderFieldInt(name, def);
    }

    public int getResponseCode() throws IOException {
//        checkIfAuthenticated();
        return connection().getResponseCode();
    }

    public String getHeaderField(int n) throws IOException {
//        checkIfAuthenticated();
        return connection().getHeaderField(n);
    }

    public String getHeaderField(String name) throws IOException {
//        checkIfAuthenticated();
        return connection().getHeaderField(name);
    }

    public String getHeaderFieldKey(int n) throws IOException {
//        checkIfAuthenticated();
        return connection().getHeaderFieldKey(n);
    }

    public String getResponseMessage() throws IOException {
//        checkIfAuthenticated();
        return connection().getResponseMessage();
    }

    public long getDate() throws IOException {
//        checkIfAuthenticated();
        return connection().getDate();
    }

    public long getExpiration() throws IOException {
//         checkIfAuthenticated();
        return connection().getExpiration();
    }

    public long getHeaderFieldDate(String name, long def) throws IOException {
//        checkIfAuthenticated();
        return connection().getHeaderFieldDate(name, def);
    }

    public long getLastModified() throws IOException {
//        checkIfAuthenticated();
        return connection().getLastModified();
    }

    public String getEncoding() {
//        try {
//            checkIfAuthenticated();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
        return connection().getEncoding();
    }

    public String getType() {
//        try {
//            checkIfAuthenticated();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
        return connection().getType();
    }

    public long getLength() {
//        try {
//            checkIfAuthenticated();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
        return connection().getLength();
    }

    public DataInputStream openDataInputStream() throws IOException {
//        checkIfAuthenticated();
        return connection().openDataInputStream();
    }

    public InputStream openInputStream() throws IOException {
//        checkIfAuthenticated();
        return connection().openInputStream();
    }


    public DataOutputStream openDataOutputStream() throws IOException {
//        checkIfAuthenticated();
        return connection().openDataOutputStream();
    }

    public OutputStream openOutputStream() throws IOException {
//        checkIfAuthenticated();
        return connection().openOutputStream();
    }
    // </editor-fold>

    // <editor-fold desc="Methods can be called during setup or connected state.">
    public void close() throws IOException {
        connection().close();
    }

    public String getRequestMethod() {
        return connection().getRequestMethod();
    }

    public String getRequestProperty(String key) {
        return connection().getRequestProperty(key);
    }

    public String getURL() {
        return connection().getURL();
    }

    public String getProtocol() {
        return connection().getProtocol();
    }

    public String getHost() {
        return connection().getHost();
    }

    public String getFile() {
        return connection().getFile();
    }
    public String getRef() {
        return connection().getRef();
    }

    public int getPort() {
        return connection().getPort();
    }

    public String getQuery() {
        return connection().getQuery();
    }

    // </editor-fold>

}

package br.org.indt.ndg.mobile.httptransport;

import br.org.indt.ndg.mobile.AppMIDlet;
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
//    private int m_mode;
    private Hashtable m_properties;
    private static String user = null;
    private static String password = null;
    private static boolean logged = false;


    private static DigestAuthResponse digestResponse = null;

    private static final String DIGEST = "digest";
    private static final String BASIC = "basic";
    private static final String AUTHORIZATION = "Authorization";
    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";


    public static HttpConnection open(String url, String httpMethod) throws IOException {
        HttpConnection conn = null;
        conn = new SecureHttpConnector( url );
        conn.setRequestMethod(httpMethod);
        if(digestResponse != null){
            conn.setRequestProperty(AUTHORIZATION, digestResponse.getDigestHeader(conn));
        }
        return conn;
    }

    private SecureHttpConnector(String url) throws IOException {
        m_url = url;
        m_properties = new Hashtable();
        m_connection = ( HttpConnection )Connector.open( m_url );
    }

    public static void setCredentials(String aUser, String aPassword) {
        user = aUser;
        password = aPassword;
        if(digestResponse != null)
        {
            digestResponse.initParameters(user, password);
        }
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

    private  HttpConnection duplicateConnection() throws IOException {
        HttpConnection connection = ( HttpConnection )Connector.open( m_url);
        connection.setRequestMethod(m_connection.getRequestMethod());

        Enumeration keys = m_properties.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            connection.setRequestProperty((String)key, (String)m_properties.get(key));
        }
        connection.setRequestProperty(AUTHORIZATION, digestResponse.getDigestHeader(connection));
        return connection;
    }

    public void setRequestProperty(String key, String value) throws IOException {
        m_properties.put(key, value);
        m_connection.setRequestProperty(key, value);
    }

    public void setRequestMethod(String method) throws IOException {
        m_connection.setRequestMethod(method);
    }

    // <editor-fold desc="The following methods cause the transition to the Connected state when the connection is in Setup state.">

    public int getHeaderFieldInt(String name, int def) throws IOException {
        return m_connection.getHeaderFieldInt(name, def);
    }

    //                    authHeaderVal = "Basic " + BasicAuth.encode(user, password);

    public int getResponseCode() throws IOException {
        if(m_connection.getResponseCode() == HttpConnection.HTTP_UNAUTHORIZED ){
                String header = m_connection.getHeaderField(WWW_AUTHENTICATE);
                if (header.toLowerCase().startsWith(BASIC)) {

                } else if (header.toLowerCase().startsWith(DIGEST)) {
                    String args = header.substring(header.indexOf(' ') + 1);
                    digestResponse = new DigestAuthResponse(args, user, password); //TODO save user and passowrd
                }
                if(digestResponse.hasCredentials() && !m_connection.getRequestMethod().equals("POST"))
                {
                m_connection = duplicateConnection();
                loginIfOk();
                return m_connection.getResponseCode();
                }
        } else {
            loginIfOk();
        }
        return m_connection.getResponseCode();
    }

    private void loginIfOk() throws IOException {
        if(m_connection.getResponseCode() == HttpConnection.HTTP_OK)
        {
            logged = true;
        }
    }

    public String getHeaderField(int n) throws IOException {
        return m_connection.getHeaderField(n);
    }

    public String getHeaderField(String name) throws IOException {
        return m_connection.getHeaderField(name);
    }

    public String getHeaderFieldKey(int n) throws IOException {
        return m_connection.getHeaderFieldKey(n);
    }

    public String getResponseMessage() throws IOException {
        return m_connection.getResponseMessage();
    }

    public long getDate() throws IOException {
        return m_connection.getDate();
    }

    public long getExpiration() throws IOException {
        return m_connection.getExpiration();
    }

    public long getHeaderFieldDate(String name, long def) throws IOException {
        return m_connection.getHeaderFieldDate(name, def);
    }

    public long getLastModified() throws IOException {
        return m_connection.getLastModified();
    }

    public String getEncoding() {
        return m_connection.getEncoding();
    }

    public String getType() {
        return m_connection.getType();
    }

    public long getLength() {
        return m_connection.getLength();
    }

    public DataInputStream openDataInputStream() throws IOException {
        return m_connection.openDataInputStream();
    }

    public InputStream openInputStream() throws IOException {
        return m_connection.openInputStream();
    }


    public DataOutputStream openDataOutputStream() throws IOException {
        return m_connection.openDataOutputStream();
    }

    public OutputStream openOutputStream() throws IOException {
        return m_connection.openOutputStream();
    }
    // </editor-fold>

    // <editor-fold desc="Methods can be called during setup or connected state.">
    public void close() throws IOException {
        m_connection.close();
    }

    public String getRequestMethod() {
        return m_connection.getRequestMethod();
    }

    public String getRequestProperty(String key) {
        return m_connection.getRequestProperty(key);
    }

    public String getURL() {
        return m_connection.getURL();
    }

    public String getProtocol() {
        return m_connection.getProtocol();
    }

    public String getHost() {
        return m_connection.getHost();
    }

    public String getFile() {
        return m_connection.getFile();
    }
    public String getRef() {
        return m_connection.getRef();
    }

    public int getPort() {
        return m_connection.getPort();
    }

    public String getQuery() {
        return m_connection.getQuery();
    }

    public static boolean isAuthorizedForPost() throws IOException
    {
        if(!logged)
        {
            HttpConnection connector =
                  (HttpConnection) SecureHttpConnector.open(AppMIDlet.getInstance().getSettings().getStructure().getTestAuthorizationUrl(), "GET");
            return connector.getResponseCode() == HttpConnection.HTTP_OK;
        } else {
            return true;
        }
    }

    // </editor-fold>

}

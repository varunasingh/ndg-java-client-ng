/**
 *
 */
package br.org.indt.ndg.mobile.httptransport;

import com.twmacinta.util.MD5;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import javax.microedition.io.HttpConnection;

public class DigestAuthResponse {

    public static final String QOP_UNSPECIFIED = "unspecified";
    public static final String QOP_AUTH = "auth";
    public static final String QOP_AUTH_INT = "auth-int";
    private String HA1;
    private Hashtable authParameters;

    private Hashtable connectionParameters;

    public DigestAuthResponse( String authArgs, String user, String password) {
        //Parse out the parameters of the challenge
        connectionParameters = AuthUtils.getQuotedParameters( authArgs );
        //Generate HA1
        String HA1 = AuthUtils.MD5(user + ":" + connectionParameters.get("realm") + ":" + password);

        //Create a response which will be used to create the header (and can be cached).
        this.authParameters = new Hashtable();
        this.HA1 = HA1;

        initParameters(user);
    }


    private void initParameters(String user){

        String qop;
        if (!connectionParameters.containsKey("qop")) {
            qop = DigestAuthResponse.QOP_UNSPECIFIED;
        } else {
            Vector qops = AuthUtils.split((String) connectionParameters.get("qop"), ",", false);
            if (qops.contains(DigestAuthResponse.QOP_AUTH_INT) && qops.contains(DigestAuthResponse.QOP_AUTH)) {
                //choose between auth-int and auth if both are available;
                qop = DigestAuthResponse.QOP_AUTH;
            } else if (qops.size() == 1) {
                if (qops.elementAt(0).equals(DigestAuthResponse.QOP_AUTH)) {
                    qop = DigestAuthResponse.QOP_AUTH;
                } else if (qops.elementAt(0).equals(DigestAuthResponse.QOP_AUTH_INT)) {
                    qop = DigestAuthResponse.QOP_AUTH_INT;
                } else {
                    return;
                }
            } else {
                //These are really the only possibilities...
                return;
            }
        }

        String nonce = (String) connectionParameters.get("nonce");
        String opaque = (String) connectionParameters.get("opaque");

        put("username", AuthUtils.quote(user));
        put("realm", AuthUtils.quote((String) connectionParameters.get("realm")));
        put("nonce", AuthUtils.quote(nonce));

        if (!qop.equals(DigestAuthResponse.QOP_UNSPECIFIED)) {
            put("qop", qop);
        }

        if (opaque != null) {
            put("opaque", AuthUtils.quote(opaque));
        }
    }

    public String getDigestHeader(HttpConnection connection) throws UnsupportedEncodingException {

        String qop;
        //Determine the authentication scheme.
        if (!connectionParameters.containsKey("qop")) {
            qop = DigestAuthResponse.QOP_UNSPECIFIED;
        } else {
            Vector qops = AuthUtils.split((String) connectionParameters.get("qop"), ",", false);
            if (qops.contains(DigestAuthResponse.QOP_AUTH_INT) && qops.contains(DigestAuthResponse.QOP_AUTH)) {
                //choose between auth-int and auth if both are available;
                qop = DigestAuthResponse.QOP_AUTH;
            } else if (qops.size() == 1) {
                if (qops.elementAt(0).equals(DigestAuthResponse.QOP_AUTH)) {
                    qop = DigestAuthResponse.QOP_AUTH;
                } else if (qops.elementAt(0).equals(DigestAuthResponse.QOP_AUTH_INT)) {
                    qop = DigestAuthResponse.QOP_AUTH_INT;
                } else {
                    return null;
                }
            } else {
                //These are really the only possibilities...
                return null;
            }
        }

        //Read out the necessary parameters for the response.
        String uri;//uri can be added to parameters later

        if (connectionParameters.containsKey("domain")) { //TODO move to buildResponse
            uri = (String) connectionParameters.get("domain");
        } else {
            String fileUri = connection.getFile();
            uri = connection.getURL();
            int index = 0;
            if ((index = uri.indexOf(fileUri)) > 0) {
                uri = uri.substring(index);
            }
        }

        String nonce = (String) connectionParameters.get("nonce");

        String opaque = (String) connectionParameters.get("opaque");

        put("realm", AuthUtils.quote((String) connectionParameters.get("realm")));
        put("nonce", AuthUtils.quote(nonce));
        put("uri", AuthUtils.quote(uri));

        if (!qop.equals(DigestAuthResponse.QOP_UNSPECIFIED)) {
            put("qop", qop);
        }

        if (opaque != null) {
            put("opaque", AuthUtils.quote(opaque));
        }
        //Build the response header from these conditions
        return buildResponse(connection);
    }

    public String get(String key) {
        return (String) authParameters.get(key);
    }

    public String put(String key, String value) {
        return (String) authParameters.put(key, value);
    }

    /**
     * Builds an auth response for the provided message based on the
     * parameters currently available for authentication
     * @param connection
     * @return An Authenticate HTTP header for the message if one could be
     * created, null otherwise.
     */
    public String buildResponse(HttpConnection connection) throws UnsupportedEncodingException {

        String uri = "";
        if (connectionParameters.containsKey("domain")) { //TODO move to buildResponse
            uri = (String) connectionParameters.get("domain");
        } else {
            String fileUri = connection.getFile();
            uri = connection.getURL();
            int index = 0;
            if ((index = uri.indexOf(fileUri)) > 0) {
                uri = uri.substring(index);
            }
        }

        String qop = (String) authParameters.get("qop");
        String nonce = AuthUtils.unquote((String) authParameters.get("nonce"));

        String method = connection.getRequestMethod();

        String HA2 = null;
        if (!qop.equals(DigestAuthResponse.QOP_AUTH_INT)) {
            HA2 = AuthUtils.MD5(method + ":" + uri);
        } else {
            // TODO most likely we should think about supporting it
            throw new UnsupportedEncodingException(
                    "Unsupported authentication parameter: " + DigestAuthResponse.QOP_AUTH_INT);
        }

        if (qop.equals(DigestAuthResponse.QOP_UNSPECIFIED)) {
            //RFC 2069 Auth
            authParameters.put("response", AuthUtils.quote(AuthUtils.MD5(HA1 + ":" + nonce + ":" + HA2)));
        } else {
            String nc = getNonceCount();

            //Generate client nonce
            String cnonce = getClientNonce();

            //Calculate response
            authParameters.put("response", AuthUtils.quote(AuthUtils.MD5(HA1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + HA2)));
        }
        return "Digest " + AuthUtils.encodeQuotedParameters(authParameters);
    }

    private String getClientNonce() {
        if (!authParameters.containsKey("cnonce")) {
            Random r = new Random();
            r.setSeed(System.currentTimeMillis());
            byte[] b = new byte[8];
            for (int i = 0; i < b.length; ++i) {
                b[i] = (byte) r.nextInt(256);
            }

            String cnonce = MD5.toHex(b);
            authParameters.put("cnonce", AuthUtils.quote(cnonce));
            return cnonce;
        } else {
            return AuthUtils.unquote((String) authParameters.get("cnonce"));
        }
    }

    private String getNonceCount() {
        //The nonce count represents the number of
        //times that the nonce has been used for authentication
        //and must be incremented for each request. Otherwise
        //the nonce data becomes unavailable.
        if (!authParameters.containsKey("nc")) {
            String nc = "00000001";
            authParameters.put("nc", nc);

            return nc;
        } else {
            String nc = (String) authParameters.get("nc");
            int count = Integer.parseInt(nc);
            count += 1;

            nc = String.valueOf(count);
            //Buffer to the left
            while (nc.length() < 8) {
                nc = "0" + nc;
            }
            authParameters.put("nc", nc);
            return nc;
        }
    }
}

package br.org.indt.ndg.mobile.submit;

import br.org.indt.ndg.mobile.httptransport.SecureHttpConnector;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.io.HttpConnection;

/**
 * Sends given bytes as a file using HTTP POST with multipart/form-data
 */
public class HttpMultipartPostRequest {

    private static final String BOUNDARY = "----------V2ymHFg03ehbqgZCaKO6jy";

    private byte[] m_postBytes = null;
    private String m_postUrl = null;
    private boolean stopRequest = false;

    /**
     *
     * @param url           POST destination
     * @param params        key-value pairs of POST params
     * @param fileField     Name of multipart/form-data filefiled
     * @param fileName      Name of actual file
     * @param fileType      File content type
     * @param fileBytes     File contents
     * @throws IOException
     */
    public HttpMultipartPostRequest(String url, Hashtable params, String fileField,
            String fileName, String fileType, byte[] fileBytes, String surveyId) throws IOException {
        m_postUrl = url;
        String boundary = getBoundaryString();
        String boundaryMessage = getBoundaryMessage(boundary, params, fileField, fileName, fileType);
        String endBoundary = "\r\n--" + boundary + "--\r\n";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(boundaryMessage.getBytes());
        bos.write(fileBytes);
        bos.write(endBoundary.getBytes());
        m_postBytes = bos.toByteArray();
        bos.close();
    }

    private String getBoundaryString() {
        return BOUNDARY;
    }

    private String getBoundaryMessage(String boundary, Hashtable params, String fileField,
            String fileName, String fileType) {
        StringBuffer res = new StringBuffer("--").append(boundary).append("\r\n");
        Enumeration keys = params.keys();

        while ( keys.hasMoreElements() ) {
            String key = (String)keys.nextElement();
            String value = (String)params.get(key);
            res.append("Content-Disposition: form-data; name=\"").append(key).append("\"\r\n")
                    .append("\r\n").append(value).append("\r\n")
                    .append("--").append(boundary).append("\r\n");
        }
        res.append("Content-Disposition: form-data; name=\"").append(fileField).append("\"; filename=\"").append(fileName).append("\"\r\n")
                .append("Content-Type: ").append(fileType).append("\r\n\r\n");
        return res.toString();
    }


    public void cancel() {
        stopRequest = true;
    }

    public int send() throws IOException {
        HttpConnection hc = null;
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        OutputStream dout = null;

        int res = 0;
        try{
            hc = (HttpConnection) SecureHttpConnector.open(m_postUrl, HttpConnection.POST);
            hc.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + getBoundaryString());

            // sending request
            if (!stopRequest) {
                dout = hc.openOutputStream();
                dout.write(m_postBytes);
            }
            res = hc.getResponseCode();

        }catch(IOException ex){
            throw ex;
        }finally{
            try {
                if(dout != null){
                    dout.close();
                }
                if(bos != null)
                    bos.close();
                if(is != null)
                    is.close();
                if(hc != null)
                    hc.close();
            } catch( Exception e2 ) {}
        }
        return res;
    }
}